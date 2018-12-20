package com.artezio.arttime.services;

import com.artezio.arttime.config.Settings;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.Project;
import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.repositories.HourTypeRepository;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.services.NotificationManager.WorkTimeProblems;
import com.artezio.arttime.services.mailing.Mail;
import com.artezio.arttime.services.mailing.MailTemplate;
import com.artezio.arttime.services.mailing.MailTemplateManager;
import com.artezio.arttime.services.mailing.MailingEngine;
import com.google.common.collect.Lists;
import com.ibm.icu.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;
import java.util.stream.Stream;
import javax.ejb.SessionContext;
import javax.mail.MessagingException;
import junitx.framework.ListAssert;
import static junitx.util.PrivateAccessor.setField;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.easymock.Mock;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

@PowerMockIgnore("javax.security.*")
@RunWith(PowerMockRunner.class)
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
