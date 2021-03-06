package com.artezio.arttime.services;

import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.ejb.SessionContext;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.artezio.arttime.config.Settings;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.services.NotificationManager.WorkTimeProblems;
import com.artezio.arttime.services.mailing.Mail;
import com.artezio.arttime.services.mailing.MailTemplate;
import com.artezio.arttime.services.mailing.MailTemplateManager;
import com.artezio.arttime.services.mailing.MailingEngine;
import com.google.common.collect.Lists;

import junitx.framework.ListAssert;

@RunWith(EasyMockRunner.class)
public class NotificationManagerTest {

    private NotificationManager notificationManager = new NotificationManager();
    @Mock
    private SessionContext sessionContext;
    @Mock
    private Settings settings;
    @Mock
    private MailingEngine mailingEngine;
    @Mock
    private WorkTimeService workTimeService;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private HourTypeService hourTypeService;
    @Mock
    private MailTemplateManager mailTemplateManager;
    @Mock
    private EmployeeRepository employeeRepository;

    @Before
    public void setUp() throws NoSuchFieldException {
        setField(notificationManager, "sessionContext", sessionContext);
    }

    @Test
    public void testNotifyAboutWorkTimeProblem() {
        notificationManager = createMockBuilder(NotificationManager.class)
                .addMockedMethod("notifyEmployees", List.class, Period.class, String.class)
                .addMockedMethod("notifyProjectManagers", List.class, Period.class, String.class)
                .createMock();
        List<Employee> employees = new ArrayList<>();
        Period period = new Period();
        String comment = "";

        notificationManager.notifyEmployees(employees, period, comment);
        notificationManager.notifyProjectManagers(employees, period, comment);
        replay(notificationManager);

        notificationManager.notifyAboutWorkTimeProblems(employees, period, comment);

        verify(notificationManager);
    }

    @Test
    public void testNotifyAboutOwnTimeProblems() throws NoSuchFieldException {
        setMockServices(notificationManager);
        Employee employee = new Employee("iivanov");
        employee.setEmail("iivanov@mail.com");
        List<Employee> employees = Arrays.asList(employee);
        Map<Employee, Map<Date, BigDecimal>> timeProblems = new HashMap<>();
        timeProblems.put(employee, new HashMap<>());
        Period period = new Period();
        String comment = "comment";
        String subject = "subject";
        String body = "body";

        EasyMock.expect(hourTypeService.findActualTime()).andReturn(new HourType());
        EasyMock.expect(workTimeService.getWorkTimeDeviations(period, employees)).andReturn(timeProblems);
        EasyMock.expect(mailTemplateManager.getTemplateText(eq(MailTemplate.TIME_PROBLEM_BODY.getFileName()), anyObject(Map.class))).andReturn(body);
        EasyMock.expect(mailTemplateManager.getTemplateText(eq(MailTemplate.TIME_PROBLEM_SUBJECT.getFileName()), anyObject(Map.class))).andReturn(subject);
        EasyMock.expect(settings.getSmtpUsername()).andReturn("username");
        EasyMock.expect(settings.getSmtpSender()).andReturn("sender");
        EasyMock.expect(settings.getApplicationBaseUrl()).andReturn("appHost");
        mailingEngine.send(new Mail(subject, body, "sender", "iivanov@mail.com"));
        String principalName = "someuser";
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(principalName);
        EasyMock.expect(employeeRepository.get(principalName)).andReturn(employee);
        EasyMock.replay(hourTypeService, workTimeService, mailTemplateManager, mailingEngine, settings, employeeRepository, sessionContext);

        notificationManager.notifyEmployees(employees, period, comment);

        EasyMock.verify(hourTypeService, workTimeService, mailTemplateManager, mailingEngine);
    }

    @Test
    public void testNotifyPmAboutTimeProblems() throws NoSuchFieldException {
        notificationManager = createMockBuilder(NotificationManager.class)
                .addMockedMethod("getProjects", List.class)
                .addMockedMethod("getProjectManagers", Collection.class)
                .createMock();
        setMockServices(notificationManager);
        Employee sender = new Employee("sender", "sender", "sender", "sender");
        Employee employee = new Employee("iivanov");
        Employee pm = new Employee("ppetrov");
        pm.setEmail("ppetrov@mail.com");
        Project project = createProject(1L, employee);
        List<Employee> employees = Arrays.asList(employee);
        List<Project> projects = Arrays.asList(project);
        Map<Employee, Map<Date, BigDecimal>> timeProblems = new HashMap<>();
        timeProblems.put(employee, new HashMap<>());
        Period period = new Period();
        String comment = "comment";
        String subject = "subject";
        String body = "body";

        EasyMock.expect(notificationManager.getProjects(employees)).andReturn(projects);
        EasyMock.expect(notificationManager.getProjectManagers(projects)).andReturn(Lists.newArrayList(pm));
        EasyMock.expect(hourTypeService.findActualTime()).andReturn(new HourType());
        EasyMock.expect(workTimeService.getWorkTimeDeviations(period, employees)).andReturn(timeProblems);
        EasyMock.expect(mailTemplateManager.getTemplateText(eq(MailTemplate.TIME_PROBLEM_BODY_FOR_PM.getFileName()), anyObject(Map.class))).andReturn(body);
        EasyMock.expect(mailTemplateManager.getTemplateText(eq(MailTemplate.TIME_PROBLEM_SUBJECT.getFileName()), anyObject(Map.class))).andReturn(subject);
        EasyMock.expect(settings.getSmtpUsername()).andReturn("testSmtpUsername");
        EasyMock.expect(settings.getSmtpSender()).andReturn("sender");
        EasyMock.expect(settings.getApplicationBaseUrl()).andReturn("appHost");
        mailingEngine.send(new Mail(subject, body, sender.getEmail(), pm.getEmail()));
        EasyMock.replay(notificationManager, hourTypeService, workTimeService, mailTemplateManager, mailingEngine, settings);

        notificationManager.notifyProjectManagers(employees, period, comment);

        EasyMock.verify(notificationManager, hourTypeService, workTimeService, mailTemplateManager, mailingEngine);
    }

    @Test
    public void testRequestReport() throws Exception {
        setMockServices(notificationManager);
        expect(mailTemplateManager.getTemplateText(eq(MailTemplate.REQUEST_REPORT_BODY.getFileName()), anyObject(Map.class))).andReturn("body");
        expect(mailTemplateManager.getTemplateText(eq(MailTemplate.REQUEST_REPORT_SUBJECT.getFileName()), anyObject(Map.class))).andReturn("subject");
        Period period = new Period();
        Employee pm = new Employee("ppetrov");
        pm.setEmail("ppetrov@mail.com");
        mailingEngine.send(new Mail("subject", "body", "ppetrov@mail.com", "iivanov@mail.com"));
        String principalName = "someuser";
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn(principalName);
        expect(sessionContext.getCallerPrincipal()).andReturn(principal);
        expect(employeeRepository.get(principalName)).andReturn(pm);
        expect(settings.getApplicationBaseUrl()).andReturn("appHost");
        replay(mailTemplateManager, mailingEngine, employeeRepository, settings, sessionContext);

        notificationManager.requestWorkTimeReport("iivanov@mail.com", period);

        verify(mailTemplateManager, mailingEngine);
    }

    @Test
    public void testGetProjects() throws NoSuchFieldException {
        setMockServices(notificationManager);
        Employee employee1 = createEmployee("iivanov", "Ivan", "Ivanov");
        Employee employee2 = createEmployee("ppetrov", "Petr", "Petrov");
        Employee employee3 = createEmployee("asidorov", "Andrey", "Sidorov");
        Employee employee4 = createEmployee("afedorov", "Andrey", "Fedorov");
        Project project1 = createProject(1L, employee1);
        Project project2 = createProject(2L, employee1, employee2, employee3);
        List<Employee> employees = Arrays.asList(employee1, employee2, employee4);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);
        List<Project> expected = Arrays.asList(project1, project2);
        EasyMock.expect(projectRepository.query()).andReturn(projectQuery);
        Mockito.when(projectQuery
                .status(ACTIVE)
                .teamMembers(employees)
                .fetchManagers()
                .fetchTeam()
                .list()).thenReturn(expected);
        EasyMock.replay(projectRepository);

        List<Project> actual = notificationManager.getProjects(employees);
        assertEquals(expected, actual);
        EasyMock.verify(projectRepository);
    }

    @Test
    public void testGetProjectManagers() throws Exception {
        Employee employee1 = createEmployee("iivanov", "Ivan", "Ivanov");
        Employee employee2 = createEmployee("ppetrov", "Petr", "Petrov");
        Employee employee3 = createEmployee("asidorov", "Andrey", "Sidorov");
        Project project1 = createProject(1L);
        Project project2 = createProject(2L);
        project1.addManager(employee1);
        project1.addManager(employee2);
        project2.addManager(employee1);
        project2.addManager(employee3);
        List<Project> projects = Arrays.asList(project1, project2);

        List<Employee> expected = Arrays.asList(employee1, employee2, employee3);
        List<Employee> actual = notificationManager.getProjectManagers(projects);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetSubordinateProblems() throws Exception {
        Employee employee1 = createEmployee("iivanov", "Ivan", "Ivanov");
        Employee employee2 = createEmployee("ppetrov", "Petr", "Petrov");
        Employee employee3 = createEmployee("asidorov", "Andrey", "Sidorov");
        Project project1 = createProject(1L, employee2);
        Project project2 = createProject(2L, employee3, employee1);
        project1.addManager(employee1);
        project2.addManager(employee2);
        List<Project> projects = Arrays.asList(project1, project2);
        Map<Employee, Map<Date, BigDecimal>> timeProblems = new HashMap<>();
        timeProblems.put(employee1, new HashMap<>());
        timeProblems.put(employee2, new HashMap<>());
        timeProblems.put(employee3, new HashMap<>());

        Set<WorkTimeProblems> actual = notificationManager.getEmployeeWorktimeProblems(employee1, projects, timeProblems);
        assertEquals(1, actual.size());
        assertEquals(employee2, actual.iterator().next().getEmployee());
    }

    @Test
    public void testGetSubordinatesAsString() {
        Employee employee1 = createEmployee("iivanov", "Ivan", "Ivanov");
        Employee employee2 = createEmployee("ppetrov", "Petr", "Petrov");
        Employee employee3 = createEmployee("asidorov", "Andrey", "Sidorov");
        WorkTimeProblems subordinate1 = notificationManager.new WorkTimeProblems(employee1, new HashMap<>());
        WorkTimeProblems subordinate2 = notificationManager.new WorkTimeProblems(employee2, new HashMap<>());
        WorkTimeProblems subordinate3 = notificationManager.new WorkTimeProblems(employee3, new HashMap<>());
        List<WorkTimeProblems> subordinates = Arrays.asList(subordinate1, subordinate2, subordinate3);

        String expected = "iivanov,ppetrov,asidorov";
        String actual = notificationManager.getEmployeesAsString(subordinates);
        assertEquals(expected, actual);
    }

    @Test
    public void testSumDeviation() {
        BigDecimal val1 = new BigDecimal(15);
        BigDecimal val2 = new BigDecimal(-3);
        BigDecimal val3 = BigDecimal.ZERO;
        List<BigDecimal> values = Arrays.asList(val1, val2, val3);

        BigDecimal actual = notificationManager.sumDeviation(values);
        assertEquals(new BigDecimal(12), actual);
    }

    @Test
    public void testSubordinateGetDeviation() throws Exception {
        Employee employee = createEmployee("iivanov", "Ivan", "Ivanov");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Map<Date, BigDecimal> approvedWorkTimeProblems = new HashMap<>();
        approvedWorkTimeProblems.put(sdf.parse("1-01-2015"), BigDecimal.ONE);
        approvedWorkTimeProblems.put(sdf.parse("2-01-2015"), BigDecimal.TEN);
        approvedWorkTimeProblems.put(sdf.parse("3-01-2015"), BigDecimal.ZERO);
        WorkTimeProblems subordinateProblem = notificationManager.new WorkTimeProblems(employee, approvedWorkTimeProblems);

        BigDecimal actual = subordinateProblem.getDeviation();
        assertEquals(new BigDecimal(11), actual);
    }

    @Test
    public void testSubordinateGetDeviationDetails() throws Exception {
        Employee employee = createEmployee("iivanov", "Ivan", "Ivanov");
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Map<Date, BigDecimal> approvedWorkTimeProblems = new TreeMap<>();
        approvedWorkTimeProblems.put(sdf.parse("1-01-2015"), BigDecimal.ONE);
        approvedWorkTimeProblems.put(sdf.parse("2-01-2015"), BigDecimal.TEN);
        approvedWorkTimeProblems.put(sdf.parse("3-01-2015"), BigDecimal.ZERO);
        WorkTimeProblems subordinateProblem = notificationManager.new WorkTimeProblems(employee, approvedWorkTimeProblems);

        Object[] expected = new Object[]{new AbstractMap.SimpleEntry<>(sdf.parse("1-01-2015"), BigDecimal.ONE),
                new AbstractMap.SimpleEntry<>(sdf.parse("2-01-2015"), BigDecimal.TEN),
                new AbstractMap.SimpleEntry<>(sdf.parse("3-01-2015"), BigDecimal.ZERO)};
        Object[] actual = subordinateProblem.getDeviationDetails();
        assertEquals(expected, actual);//TODO use assertArrayEquals
    }

    @Test
    public void testNotifyAboutTeamChanges() throws NoSuchFieldException {
        setMockServices(notificationManager);
        Project project = new Project();
        Employee manager = new Employee();
        manager.setEmail("manager@email");
        String senderEmail = "sender@se.nder";
        project.addManager(manager);
        List<Employee> newEmployees = new ArrayList<>();
        List<Employee> closedEmployees = new ArrayList<>();

        expect(mailTemplateManager.getTemplateText(eq(MailTemplate.TEAM_SYNCHRONIZATION_BODY.getFileName()),
                anyObject(Map.class))).andReturn("body");
        expect(mailTemplateManager.getTemplateText(eq(MailTemplate.TEAM_SYNCHRONIZATION_SUBJECT.getFileName()),
                anyObject(Map.class))).andReturn("subject");
        expect(settings.getSmtpSender()).andReturn(senderEmail);
        expect(settings.getApplicationBaseUrl()).andReturn("appHost");
        mailingEngine.send(new Mail("subject", "body", senderEmail, manager.getEmail()));
        replay(mailTemplateManager, mailingEngine, settings);

        notificationManager.notifyAboutTeamChanges(project, closedEmployees, newEmployees);

        verify(mailTemplateManager, mailingEngine);
    }
    
    @Test
    public void testNotifyAboutIncorrectTimesheet() throws Exception {
        notificationManager = createMockBuilder(NotificationManager.class)
                .addMockedMethod("notifyAboutIncorrectTimesheet", Employee.class, Period.class)
                .createMock();
        setMockServices(notificationManager);
        
        Employee goodEmployee = new Employee("good_employee");
        Employee badEmployee1 = new Employee("bad_employee1");
        Employee badEmployee2 = new Employee("bad_employee2");
        List<Employee> employees = Arrays.asList(goodEmployee, badEmployee1, badEmployee2);
        
        Date start =  new GregorianCalendar(2020, 1, 1).getTime();
        Date finish =  new GregorianCalendar(2020, 1, 29).getTime();
        Period period = new Period(start, finish);
        
        expect(workTimeService.getRequiredWorkHours(goodEmployee, period)).andReturn(BigDecimal.valueOf(160));
        expect(workTimeService.getRequiredWorkHours(badEmployee1, period)).andReturn(BigDecimal.valueOf(160));
        expect(workTimeService.getRequiredWorkHours(badEmployee2, period)).andReturn(BigDecimal.valueOf(160));
        expect(workTimeService.getActualWorkHours(goodEmployee, period)).andReturn(BigDecimal.valueOf(160));
        expect(workTimeService.getActualWorkHours(badEmployee1, period)).andReturn(BigDecimal.valueOf(8));
        expect(workTimeService.getActualWorkHours(badEmployee2, period)).andReturn(BigDecimal.valueOf(168));
        notificationManager.notifyAboutIncorrectTimesheet(badEmployee1, period);
        notificationManager.notifyAboutIncorrectTimesheet(badEmployee2, period);
        replay(notificationManager, workTimeService);
        
        notificationManager.notifyAboutIncorrectTimesheet(employees, period);
        
        verify(notificationManager, workTimeService);
        
    }
    
    @Test
    public void testNotifyAboutIncorrectTimesheet_oneEmployee() throws Exception {
        setMockServices(notificationManager);
        
        Employee employee = new Employee("user_name");
        String employeeEmail = "user_name@mail.com";
        employee.setEmail(employeeEmail);
        Period period = mock(Period.class);
        HourType actualTime = new HourType("actual");
        
        String senderEmail = "sender@mail.com";
        String body = "mail_body";
        String subject = "mail_subject";
        Mail mail = new Mail(subject, body, senderEmail, employeeEmail);
        
        String applicationBaseUrl = "url";
        Map<String, Object> mailParameters = new HashMap<>();
        mailParameters.put("hourType", actualTime);
        mailParameters.put("period", period);
        mailParameters.put("appHost", applicationBaseUrl);
        
        expect(hourTypeService.findActualTime()).andReturn(actualTime);
        expect(settings.getSmtpSender()).andReturn(senderEmail);
        expect(settings.getApplicationBaseUrl()).andReturn(applicationBaseUrl);
        expect(mailTemplateManager.getTemplateText(MailTemplate.INCORRECT_TIMESHEET_BODY.getFileName(), mailParameters)).andReturn(body);
        expect(mailTemplateManager.getTemplateText(MailTemplate.INCORRECT_TIMESHEET_SUBJECT.getFileName(), mailParameters)).andReturn(subject);
        mailingEngine.send(mail);
        replay(hourTypeService, settings, mailTemplateManager, mailingEngine);
        
        notificationManager.notifyAboutIncorrectTimesheet(employee, period);
        
        verify(hourTypeService, settings, mailTemplateManager, mailingEngine);
        
    }
    
    @Test
    public void testNotifyAboutUnapprovedHours() throws Exception {
        notificationManager = createMockBuilder(NotificationManager.class)
                .addMockedMethod("notifyAboutUnapprovedHours", Employee.class, Period.class)
                .createMock();
        setMockServices(notificationManager);
        
        Date start =  new GregorianCalendar(2020, 1, 1).getTime();
        Date finish =  new GregorianCalendar(2020, 1, 29).getTime();
        Period period = new Period(start, finish);
        
        Employee manager1 = new Employee("user1");
        Employee manager2 = new Employee("user2");;
        List<Employee> managers = Arrays.asList(manager1, manager2);
        
        notificationManager.notifyAboutUnapprovedHours(manager1, period);
        notificationManager.notifyAboutUnapprovedHours(manager2, period);
        replay(notificationManager);
        
        notificationManager.notifyAboutUnapprovedHours(managers, period);
        
        verify(notificationManager);
    }
    
    @Test
    public void testNotifyAboutUnapprovedHours_oneEmployee() throws Exception {
        setMockServices(notificationManager);
        
        Employee employee = new Employee("user_name");
        String employeeEmail = "user_name@mail.com";
        employee.setEmail(employeeEmail);
        Period period = mock(Period.class);
        
        String senderEmail = "sender@mail.com";
        String body = "mail_body";
        String subject = "mail_subject";
        Mail mail = new Mail(subject, body, senderEmail, employeeEmail);
        
        String applicationBaseUrl = "url";
        Map<String, Object> mailParameters = new HashMap<>();
        mailParameters.put("period", period);
        mailParameters.put("appHost", applicationBaseUrl);
        
        expect(settings.getSmtpSender()).andReturn(senderEmail);
        expect(settings.getApplicationBaseUrl()).andReturn(applicationBaseUrl);
        expect(mailTemplateManager.getTemplateText(MailTemplate.UNAPPROVED_HOURS_BODY.getFileName(), mailParameters)).andReturn(body);
        expect(mailTemplateManager.getTemplateText(MailTemplate.UNAPPROVED_HOURS_SUBJECT.getFileName(), mailParameters)).andReturn(subject);
        mailingEngine.send(mail);
        replay(settings, mailTemplateManager, mailingEngine);
        
        notificationManager.notifyAboutUnapprovedHours(employee, period);
        
        verify(settings, mailTemplateManager, mailingEngine);
        
    }

    private void setMockServices(NotificationManager manager) throws NoSuchFieldException {
        setField(manager, "settings", settings);
        setField(manager, "employeeRepository", employeeRepository);
        setField(manager, "mailingEngine", mailingEngine);
        setField(manager, "workTimeService", workTimeService);
        setField(manager, "projectRepository", projectRepository);
        setField(manager, "hourTypeService", hourTypeService);
        setField(manager, "mailTemplateManager", mailTemplateManager);
    }

    private Employee createEmployee(String username, String firstName, String lastName) {
        Employee employee = new Employee(username);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        return employee;
    }

    private Project createProject(Long id, Employee...employees) throws NoSuchFieldException {
        Project project = new Project();
        setField(project, "id", id);
        Stream.of(employees).forEach(project::addTeamMember);
        return project;
    }

}
