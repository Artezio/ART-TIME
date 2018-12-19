package com.artezio.arttime.services;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.datamodel.*;
import com.artezio.arttime.exceptions.SaveApprovedHoursException;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.repositories.HoursRepository;
import com.artezio.arttime.repositories.HoursRepository.HoursQuery;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.web.interceptors.FacesMessage;
import com.artezio.javax.jpa.abac.AbacContext;
import org.apache.commons.beanutils.BeanUtils;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static com.artezio.arttime.admin_tool.cache.WebCached.Scope.VIEW_SCOPED;
import static com.artezio.arttime.security.AbacContexts.REPORT_TIME;
import static com.artezio.arttime.security.AbacContexts.VIEW_TIMESHEET;
import static com.artezio.arttime.security.auth.UserRoles.*;

@Named
@Stateless
public class HoursService {

    private static final String ALREADY_APPROVED_HOURS_MESSAGE = "You're trying to update hours that are already approved.";

    @Inject
    private HoursRepository hoursRepository;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private HourTypeService hourTypeService;
    @Inject
    private EmployeeRepository employeeRepository;

    //TODO try optimistic lock on Hours
    @RolesAllowed({EXEC_ROLE, PM_ROLE, OFFICE_MANAGER})
    @FacesMessage(onCompleteMessageKey = "message.timesheetIsSaved")
    public void manageHours(Collection<Hours> hours) throws ReflectiveOperationException {
        if (hours.isEmpty()) {
            return;
        }
        Map<Employee, List<Hours>> hoursByEmployees = groupHoursByEmployees(hours);
        Map<Employee, List<Hours>> persistedHoursByEmployees = groupHoursByEmployees(getPersistedHours(hours));
        for (Employee employee : hoursByEmployees.keySet()) {
            hoursRepository.lock(employee);
            saveOrUpdateHours(hoursByEmployees.get(employee), persistedHoursByEmployees.get(employee));
        }
    }

    @PermitAll
    @AbacContext(VIEW_TIMESHEET)
    public List<Hours> getTimesheetHours(Period period) {
        return hoursRepository.query()
                .fetchProjectAccountableHours()
                .fetchProjectTeam()
                .period(period)
                .distinct()
                .list();
    }

    //TODO try optimistic lock on Hours
    @PermitAll
    @AbacContext(REPORT_TIME)
    @FacesMessage(onCompleteMessageKey = "message.timesheetIsSaved")
    public void reportHours(Collection<Hours> hours) throws SaveApprovedHoursException, ReflectiveOperationException {
        if (hours.isEmpty()) {
            return;
        }
        Map<Employee, List<Hours>> hoursByEmployees = groupHoursByEmployees(hours);
        Map<Employee, List<Hours>> persistedHoursByEmployees = groupHoursByEmployees(getPersistedHours(hours));
        for (Employee employee : hoursByEmployees.keySet()) {
            // TODO: Remove check for approved, as it's checked inside ABAC rules
            if (existApprovedHours(hoursByEmployees.get(employee), persistedHoursByEmployees.get(employee))) {
                throw new SaveApprovedHoursException(ALREADY_APPROVED_HOURS_MESSAGE);
            }
            hoursRepository.lock(employee);
            saveOrUpdateHours(hoursByEmployees.get(employee), persistedHoursByEmployees.get(employee));
        }
    }

    @RolesAllowed({INTEGRATION_CLIENT_ROLE, PM_ROLE, EXEC_ROLE, OFFICE_MANAGER, ACCOUNTANT})
    public List<Hours> getHours(Filter filter) {
        HoursQuery query = hoursRepository.query();
        List<Hours> result = query
                .approved(filter.isApproved())
                .projects(filter.getProjects())
                .departments(filter.getDepartments())
                .types(filter.getHourTypes())
                .period(filter.getPeriod())
                .employees(filter.getEmployees())
                .list();
        return result;
    }

    // TODO: Change signature for using Filter
    @RolesAllowed({EXEC_ROLE, PM_ROLE, OFFICE_MANAGER, ACCOUNTANT})
    @WebCached(scope = VIEW_SCOPED)
    public List<Hours> getHours(Period period, List<String> employeeUserNames, List<Long> projectIds,
                                List<Long> hourTypeIds, List<String> departments) {
        return hoursRepository.query()
                .period(period)
                .employeeUserNames(employeeUserNames)
                .projectIds(projectIds)
                .typeIds(hourTypeIds)
                .departments(departments)
                .list();
    }

    @RolesAllowed(INTEGRATION_CLIENT_ROLE)
    public void apply(Collection<HoursChange> changes) throws ApplyHoursChangeException {
        for (HoursChange change : changes) {
            try {
                apply(change);
            } catch (Exception exception) {
                throw new ApplyHoursChangeException(change, exception.getMessage());
            }
        }
    }

    protected void apply(HoursChange change) {
        Employee employee = employeeRepository.get(change.getEmployeeUsername());
        hoursRepository.lock(employee);
        Hours existedHours = getHours(change);
        if (existedHours != null) {
            existedHours.add(change.getQuantityDelta());
            if (change.getComment() != null) {
                existedHours.setComment(change.getComment());
            }
            hoursRepository.update(existedHours);
        } else {
            Hours newHours = toHours(change);
            hoursRepository.create(newHours);
        }
    }

    /*
    * hour.resetId() method was added for the next case:
    * 		User1 opens 'timesheet' tab and user2 opens 'efforts' tab. User1 deletes some hours and
    * 		saves this state. User2 edits received data and saves this state. But data edited by user2
    * 		don't exist in database, so 'persistedHour' variable is null and 'entityManager' attempts to persist
    * 		the data that already has an id and throws an exception.
    *
    * BeanUtils.copyProperties(persistedHour, hour) method was added for the next case:
    * 		User1 opens 'timesheet' tab and user2 opens 'efforts' tab. User1 deletes some hours and
    * 		saves this state. Then user1 creates new hours and saves this state. User2 edits received data and saves
    * 		it. But the data ids in database and sent by user2 are different, so 'entityManager' creates new records
    * 		in database.
    * */
    private void saveOrUpdateHours(Collection<Hours> hours, Collection<Hours> persistedHours)
            throws InvocationTargetException, IllegalAccessException {
        for (Hours hour : hours) {
            Hours persistedHour = findHours(persistedHours, hour);
            if (hour.isApproved() || hour.getQuantity() != null || hour.getComment() != null) {
                if (persistedHour == null) {
                    hour.resetId();
                    hoursRepository.create(hour);
                } else {
                    BeanUtils.copyProperties(persistedHour, hour);
                    hoursRepository.update(persistedHour);
                }
            } else if (persistedHour != null) {
                hoursRepository.remove(persistedHour);
            }
        }
    }

    protected boolean existApprovedHours(Collection<Hours> hours, Collection<Hours> persistedHours) {
        return persistedHours != null && persistedHours.stream()
                .filter(Hours::isApproved)
                .anyMatch(hours::contains);
    }

    private Hours toHours(HoursChange change) {
        Project project = projectRepository.query()
                .code(change.getProjectCode())
                .getSingleResult();
        Employee employee = employeeRepository.get(change.getEmployeeUsername());
        HourType type = hourTypeService.find(change.getTypeId());
        Hours hours = new Hours(project, change.getDate(), employee, type);
        hours.setComment(change.getComment());
        hours.add(change.getQuantityDelta());
        return hours;
    }

    private List<Hours> getPersistedHours(Collection<Hours> hours) {
        Set<Project> projects = hours.stream().map(Hours::getProject).collect(Collectors.toSet());
        Set<Employee> employees = hours.stream().map(Hours::getEmployee).collect(Collectors.toSet());
        Date from = hours.parallelStream().min(Comparator.comparing(Hours::getDate)).get().getDate();
        Date till = hours.parallelStream().max(Comparator.comparing(Hours::getDate)).get().getDate();
        return getHours(employees, projects, from, till);
    }

    private List<Hours> getHours(Collection<Employee> employees, Collection<Project> projects, Date from, Date to) {
        return hoursRepository.query()
                .projects(projects)
                .period(new Period(from, to))
                .employees(employees)
                .uncached()
                .list();
    }

    private Hours getHours(HoursChange hoursChange) {
        return hoursRepository.query()
                .employee(hoursChange.getEmployeeUsername())
                .project(hoursChange.getProjectCode())
                .hourType(hoursChange.getTypeId())
                .date(hoursChange.getDate())
                .uncached()
                .getSingleResultOrNull();
    }

    private Hours findHours(Collection<Hours> hoursCollection, Hours hours) {
        if (hoursCollection == null) {
            return null;
        }
        Optional<Hours> optionalHour = hoursCollection.stream()
                .filter(ph -> hours.getEmployee().equals(ph.getEmployee()) && hours.getProject().equals(ph.getProject()) &&
                        hours.getType().equals(ph.getType()) && hours.getDate().equals(ph.getDate()))
                .findFirst();
        return optionalHour.orElse(null);
    }

    private Map<Employee, List<Hours>> groupHoursByEmployees(Collection<Hours> hours) {
        return hours.stream()
                .collect(Collectors.groupingBy(Hours::getEmployee));
    }

}
