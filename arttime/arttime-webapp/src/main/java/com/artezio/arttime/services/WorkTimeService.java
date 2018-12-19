package com.artezio.arttime.services;

import com.artezio.arttime.admin_tool.cache.WebCached;
import com.artezio.arttime.admin_tool.cache.WebCached.Scope;
import com.artezio.arttime.datamodel.*;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.repositories.HoursRepository;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.javax.jpa.abac.AbacContext;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.artezio.arttime.admin_tool.cache.WebCached.Scope.REQUEST_SCOPED;
import static com.artezio.arttime.security.AbacContexts.REPORT_ACTUAL_WORKTIME_PROBLEMS;
import static com.artezio.arttime.security.AbacContexts.VIEW_TIMESHEET;
import static com.artezio.arttime.security.auth.UserRoles.EXEC_ROLE;
import static com.artezio.arttime.security.auth.UserRoles.OFFICE_MANAGER;

@Named
@Stateless
@WebCached(scope = REQUEST_SCOPED)
@RolesAllowed({EXEC_ROLE, OFFICE_MANAGER})
public class WorkTimeService {

    @Inject
    private HoursRepository hoursRepository;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private WorkdaysCalendarService workdaysCalendarService;
    @Inject
    private EmployeeRepository employeeRepository;

    @AbacContext(REPORT_ACTUAL_WORKTIME_PROBLEMS)
    public List<WorkTime> getProblemWorkTime(Filter filter) {
        Period period = filter.getPeriod();
        List<Employee> employees = getEmployees(filter);
        Map<Employee, BigDecimal> requiredActualTime = getRequiredActualTime(employees, period);
        Map<Employee, Map<Boolean, BigDecimal>> actualTime = hoursRepository.getActualTimeByEmployeeAndApproval(employees, period);
        Map<Employee, List<Employee>> managersForApprove = hoursRepository.getManagersForApproveByEmployee(employees, period);
        Map<Employee, List<Employee>> managersByEmployee = getManagers(employees);
        
        return employees.parallelStream()
                .map(employee -> new WorkTime(employee, requiredActualTime.get(employee), actualTime.get(employee),
                        managersForApprove.get(employee), managersByEmployee.get(employee)))
                .filter(WorkTime::hasProblems)
                .collect(Collectors.toList());
    }

    @PermitAll
    @AbacContext(VIEW_TIMESHEET)
    public BigDecimal getRequiredWorkHours(Employee employee, Period period) {
        List<Date> workDayDates = getWorkDayDates(employee, period);
        BigDecimal workDaysNumber = new BigDecimal(workDayDates.size());
        return workDaysNumber.multiply(employee.getWorkLoadHours());
    }

    @PermitAll
    @AbacContext(VIEW_TIMESHEET)
    public BigDecimal getActualWorkHours(Employee employee, Period period) {
        return hoursRepository.tupleQuery()
                .period(period)
                .employee(employee)
                .actualTime()
                .getTime();
    }

    public Map<Employee, Map<Date, BigDecimal>> getWorkTimeDeviations(Period period, List<Employee> employees) {
        Map<Employee, List<Hours>> approvedActualHours = getApprovedActualHours(employees, period);
        return employees.stream()
                .collect(Collectors.toMap(
                        employee -> employee,
                        employee -> getWorkTimeDeviations(period, approvedActualHours, employee)));
    }

    private Map<Date, BigDecimal> getWorkTimeDeviations(Period period, Map<Employee, List<Hours>> hoursLists, Employee employee) {
        List<Date> workdayDates = getWorkDayDates(employee, period);
        Map<Date, BigDecimal> result = new TreeMap<>();
        BigDecimal requiredWorkTime = employee.getWorkLoadHours();
        workdayDates.forEach(date ->  {
            BigDecimal hoursSum = getHoursSum(hoursLists.get(employee), date);
            if (hasWorkTimeProblems(requiredWorkTime, hoursSum)) {
                BigDecimal deviation = hoursSum.subtract(requiredWorkTime);
                result.put(date, deviation);
            }
        });
        return result;
    }

    private List<Date> getWorkDayDates(Employee employee, Period period) {
        WorkdaysCalendar calendar = employee.getCalendar();
        return workdaysCalendarService.getDays(calendar, period).stream()
                .filter(Day::isWorking)
                .map(Day::getDate)
                .collect(Collectors.toList());
    }

    private Map<Employee, List<Hours>> getApprovedActualHours(List<Employee> employees, Period period) {
        List<Hours> hoursList = hoursRepository.getActualHours(employees, period, true);
        Map<Employee, List<Hours>> result = hoursList.stream()
                .collect(Collectors.groupingBy(Hours::getEmployee));
        employees.forEach(e -> result.putIfAbsent(e, new ArrayList<>()));
        return result;
    }

    private Map<Employee, List<Employee>> getManagers(List<Employee> employees) {
        return projectRepository.query(true)
                .teamMembers(employees)
                .getManagers()
                .stream()
                .collect(Collectors.groupingBy(tuple -> tuple.get(0, Employee.class),
                        Collectors.mapping(tuple -> tuple.get(1, Employee.class), Collectors.toList())));
    }

    private Map<Employee, BigDecimal> getRequiredActualTime(List<Employee> employees, Period period) {
        return employees.stream()
                .collect(Collectors.toMap(
                        employee -> employee,
                        employee -> getRequiredWorkHours(employee, period)));
    }

    private List<Employee> getEmployees(Filter filter) {
        return employeeRepository.query()
                .filter(filter)
                .notFormer()
                .list();
    }

    private BigDecimal getHoursSum(List<Hours> hours, Date date) {
        return hours.stream()
                .filter(h -> date.equals(h.getDate()))
                .map(Hours::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean hasWorkTimeProblems(BigDecimal requiredWorkTime, BigDecimal submittedHours) {
        return requiredWorkTime.compareTo(submittedHours) != 0;
    }

    public static class WorkTime {

        public final static Comparator<WorkTime> EMPLOYEE_COMPARATOR = (wt1, wt2) ->
                Employee.NAME_COMPARATOR.compare(wt1.employee, wt2.employee);

        private Employee employee;
        private BigDecimal requiredTime;
        private BigDecimal unapprovedTime;
        private BigDecimal approvedTime;
        private List<Employee> waitingApprovalBy = new ArrayList<>();
        private List<Employee> allManagers = new ArrayList<>();

        public WorkTime(Employee employee) {
            this.employee = employee;
        }

        public WorkTime(Employee employee, BigDecimal requiredTime, Map<Boolean, BigDecimal> actualHours,
                        List<Employee> managersForApprove, List<Employee> allManagers) {
            this.employee = employee;
            this.requiredTime = requiredTime;
            this.waitingApprovalBy = managersForApprove;
            this.allManagers = allManagers;
            Optional<Map<Boolean, BigDecimal>> oActualHours = Optional.ofNullable(actualHours);
            this.approvedTime = oActualHours.map(actualHoursMap -> actualHoursMap.get(true)).orElse(BigDecimal.ZERO);
            this.unapprovedTime = oActualHours.map(actualHoursMap -> actualHoursMap.get(false)).orElse(BigDecimal.ZERO);
        }

        public BigDecimal getTimeDeviation() {
            return approvedTime.subtract(requiredTime);
        }

        public boolean hasProblems() {
            return requiredTime.compareTo(approvedTime) != 0;
        }

        public Employee getEmployee() {
            return employee;
        }

        public BigDecimal getRequiredTime() {
            return requiredTime;
        }

        public BigDecimal getUnapprovedTime() {
            return unapprovedTime;
        }

        public BigDecimal getApprovedTime() {
            return approvedTime;
        }

        public List<Employee> getWaitingApprovalBy() {
            return waitingApprovalBy;
        }

        public List<Employee> getAllManagers() {
            return allManagers;
        }

        public void setAllManagers(List<Employee> allManagers) {
            this.allManagers = allManagers;
        }

    }

}
