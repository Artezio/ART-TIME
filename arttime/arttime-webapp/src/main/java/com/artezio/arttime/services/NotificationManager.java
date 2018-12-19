package com.artezio.arttime.services;

import com.artezio.arttime.config.ApplicationSettings;
import com.artezio.arttime.config.Settings;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.repositories.HourTypeRepository;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.services.mailing.*;
import com.artezio.arttime.web.interceptors.FacesMessage;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;
import static com.artezio.arttime.security.auth.UserRoles.*;

@Named
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NotificationManager implements NotificationManagerLocal {

    @Inject
    private MailingEngine mailingEngine;
    @Inject
    private MailTemplateManager mailTemplateManager;
    @Inject
    private WorkTimeService workTimeService;
    @Inject
    private HourTypeRepository hourTypeRepository;
    @Inject
    private HourTypeService hourTypeService;
    @Inject
    private ProjectRepository projectRepository;
    @Inject
    private EmployeeRepository employeeRepository;
    @Inject
    @ApplicationSettings
    private Settings settings;
    @Resource
    private SessionContext sessionContext;

    @Override
    @RolesAllowed({EXEC_ROLE, OFFICE_MANAGER})
    @FacesMessage(onCompleteMessageKey = "message.notificationsAreSent")
    public void notifyAboutWorkTimeProblems(List<Employee> employees, Period period, String notificationComment) {
        notifyEmployees(employees, period, notificationComment);
        notifyProjectManagers(employees, period, notificationComment);
    }

    @Override
    @RolesAllowed({EXEC_ROLE, PM_ROLE, OFFICE_MANAGER})
    public void requestWorkTimeReport(String recipientEmail, Period period) {
        RequestReportMailBuilder mailBuilder = new RequestReportMailBuilder(mailTemplateManager);
        Employee sender = employeeRepository.get(sessionContext.getCallerPrincipal().getName());
        Mail mail = mailBuilder
                .setSenderEmailAddress(sender.getEmail())
                .setRecipientEmailAddress(recipientEmail)
                .setPeriod(period)
                .setAppHost(settings.getApplicationBaseUrl())
                .build();
        mailingEngine.send(mail);
    }

    @RolesAllowed(SYSTEM_ROLE)
    public void notifyAboutTeamChanges(Project project, List<Employee> formerTeamMembers, List<Employee> newTeamMembers) {
        TeamSynchronizationMailBuilder mailBuilder = new TeamSynchronizationMailBuilder(mailTemplateManager);
        mailBuilder.setSenderEmailAddress(settings.getSmtpSender())
                .setManagedProject(project)
                .setNewEmployees(newTeamMembers)
                .setClosedEmployees(formerTeamMembers)
                .setAppUrl(settings.getApplicationBaseUrl());
        project.getManagers().forEach(manager ->
                mailingEngine.send(mailBuilder.setRecipientEmailAddress(manager.getEmail()).build()));
    }

    protected void notifyEmployees(List<Employee> employees, Period period, String comment) {
        Map<Employee, Map<Date, BigDecimal>> timeProblems = workTimeService.getWorkTimeDeviations(period, employees);
        HourType actualTime = hourTypeService.findActualTime();
        TimeProblemsMailBuilder mailTemplateBuilder = new TimeProblemsMailBuilder(mailTemplateManager);
        mailTemplateBuilder.setSenderEmailAddress(getSenderEmailAddress())
                .setHourType(actualTime)
                .setPeriod(period)
                .setComment(comment)
                .setAppHost(settings.getApplicationBaseUrl());
        employees.forEach(employee -> {
            Map<Date, BigDecimal> approvedWorkTimeProblems = timeProblems.get(employee);
            Mail mail = mailTemplateBuilder.setRecipientEmailAddress(employee.getEmail())
                    .setRecipient(employee)
                    .setDeviation(sumDeviation(approvedWorkTimeProblems.values()))
                    .setDeviationDetails(approvedWorkTimeProblems.entrySet().toArray())
                    .build();
            mailingEngine.send(mail);
        });
    }

    protected void notifyProjectManagers(List<Employee> employees, Period period, String comment) {
        List<Project> projects = getProjects(employees);
        List<Employee> projectManagers = getProjectManagers(projects);
        Map<Employee, Map<Date, BigDecimal>> timeProblems = workTimeService.getWorkTimeDeviations(period, employees);
        HourType actualTime = hourTypeService.findActualTime();
        TimeProblemsForPmMailBuilder mailTemplateBuilder = new TimeProblemsForPmMailBuilder(mailTemplateManager);
        mailTemplateBuilder.setSenderEmailAddress(getSenderEmailAddress())
                .setHourType(actualTime)
                .setPeriod(period)
                .setComment(comment)
                .setAppHost(settings.getApplicationBaseUrl());
        projectManagers.forEach(projectManager -> {
            Set<WorkTimeProblems> worktimeProblems = getEmployeeWorktimeProblems(projectManager, projects, timeProblems);
            Mail mail = mailTemplateBuilder
                    .setRecipientEmailAddress(projectManager.getEmail())
                    .setRecipient(projectManager)
                    .setEmployeeWorkTimeProblems(worktimeProblems)
                    .setUserNames(getEmployeesAsString(worktimeProblems))
                    .build();
            mailingEngine.send(mail);
        });
    }

    protected List<Project> getProjects(List<Employee> employees) {
        return projectRepository.query()
                .status(ACTIVE)
                .teamMembers(employees)
                .fetchManagers()
                .fetchTeam()
                .list();
    }

    protected List<Employee> getProjectManagers(Collection<Project> projects) {
        return projects.stream()
            .flatMap(p -> p.getManagers().stream())
            .distinct()
            .collect(Collectors.toList());
    }

    protected Set<WorkTimeProblems> getEmployeeWorktimeProblems(Employee manager, List<Project> projects,
            Map<Employee, Map<Date, BigDecimal>> timeProblems) {
        return projects.stream()
                .filter(p -> p.getManagers().contains(manager))
                .flatMap(p -> p.getTeam().stream())
                .filter(timeProblems::containsKey)
                .map(employee -> new WorkTimeProblems(employee, timeProblems.get(employee)))
                .collect(Collectors.toSet());
    }

    protected String getEmployeesAsString(Collection<WorkTimeProblems> worktimeProblems) {
        return worktimeProblems.stream()
                .map(problem -> problem.getEmployee().getUserName())
                .collect(Collectors.joining(","));
    }

    protected BigDecimal sumDeviation(Collection<BigDecimal> deviations) {
        return deviations.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public class WorkTimeProblems {

        private final Employee employee;
        private final Map<Date, BigDecimal> approvedWorkTimeProblems;

        public WorkTimeProblems(Employee employee, Map<Date, BigDecimal> approvedWorkTimeProblems) {
            this.employee = employee;
            this.approvedWorkTimeProblems = approvedWorkTimeProblems;
        }

        public BigDecimal getDeviation() {
            return sumDeviation(approvedWorkTimeProblems.values());
        }

        public Object[] getDeviationDetails() {
            return approvedWorkTimeProblems.entrySet().toArray();
        }

        public Employee getEmployee() {
            return employee;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((employee == null) ? 0 : employee.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            WorkTimeProblems other = (WorkTimeProblems) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (employee == null) {
                if (other.employee != null)
                    return false;
            } else if (!employee.equals(other.employee))
                return false;
            return true;
        }

        private NotificationManager getOuterType() {
            return NotificationManager.this;
        }

    }

    private String getSenderEmailAddress() {
        Employee employee = employeeRepository.find(settings.getSmtpUsername());
        return employee != null ? employee.getEmail() : "";
    }

}
