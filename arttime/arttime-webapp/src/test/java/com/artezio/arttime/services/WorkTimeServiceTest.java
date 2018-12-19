package com.artezio.arttime.services;

import com.artezio.arttime.datamodel.*;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.repositories.HoursRepository;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.repositories.WorkdaysCalendarRepository;
import com.artezio.arttime.services.WorkTimeService.WorkTime;
import junitx.framework.ListAssert;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Tuple;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.artezio.arttime.test.utils.CalendarUtils.createHours;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(EasyMockRunner.class)
public class WorkTimeServiceTest {

    @TestSubject
    private WorkTimeService workTimeService = new WorkTimeService();
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private WorkdaysCalendarService workdaysCalendarService;
    @Mock
    private HoursRepository hoursRepository;
    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void setUp() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();
        setField(projectRepository, "entityManager", entityManager);
        setField(employeeRepository, "entityManager", entityManager);
        setField(hoursRepository, "entityManager", entityManager);
    }

    @Test
    public void testGetWorkTimeDeviations_DeviationExists_PeriodHasWorkdays() {
        Period period = new Period();
        Project project = new Project();
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar");
        Date date1 = new Date();
        Date date2 = new Date(date1.getTime() + 1000);
        Date date3 = new Date(date2.getTime() + 1000);
        period.setStart(date1);
        period.setFinish(date3);
        List<Day> days = asList(new Day(date1, calendar, true), new Day(date2, calendar, true), new Day(date3, calendar, true));
        Employee employee = buildEmployee("1", calendar);
        project.addTeamMember(employee);
        List<Employee> employees = asList(employee);
        Hours hours = new Hours(project, date1, employee, new HourType());
        hours.setQuantity(new BigDecimal(4.0));

        expect(hoursRepository.getActualHours(employees, period, true)).andReturn(asList(hours));
        expect(workdaysCalendarService.getDays(EasyMock.anyObject(), EasyMock.anyObject())).andReturn(days);
        EasyMock.replay(hoursRepository, workdaysCalendarService);

        Map<Date, BigDecimal> expectedDeviations = new HashMap<>();
        expectedDeviations.put(date1, new BigDecimal("-4.00"));
        expectedDeviations.put(date2, new BigDecimal("-8.00"));
        expectedDeviations.put(date3, new BigDecimal("-8.00"));
        Map<Employee, Map<Date, BigDecimal>> actual = workTimeService.getWorkTimeDeviations(period, employees);

        EasyMock.verify(hoursRepository, workdaysCalendarService);
        assertEquals(expectedDeviations, actual.get(employee));
    }

    @Test
    public void testGetWorkTimeDeviations_DeviationExists_PeriodHasWorkdaysAndNonWorkdays() {
        Period period = new Period();
        Project project = new Project();
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar");
        Date date1 = new Date();
        Date date2 = new Date(date1.getTime() + 1000);
        Date date3 = new Date(date2.getTime() + 1000);
        List<Day> days = asList(new Day(date1, calendar, true), new Day(date2, calendar, false), new Day(date3, calendar, true));
        Employee employee = buildEmployee("1", calendar);
        project.addTeamMember(employee);
        List<Employee> employees = asList(employee);
        Hours hours = new Hours(project, date1, employee, new HourType());
        hours.setQuantity(new BigDecimal(4.0));

        expect(hoursRepository.getActualHours(employees, period, true)).andReturn(asList(hours));
        expect(workdaysCalendarService.getDays(calendar, period)).andReturn(days);
        EasyMock.replay(hoursRepository, workdaysCalendarService);

        Map<Date, BigDecimal> expectedDeviations = new HashMap<>();
        expectedDeviations.put(date1, new BigDecimal("-4.00"));
        expectedDeviations.put(date3, new BigDecimal("-8.00"));
        Map<Employee, Map<Date, BigDecimal>> actual = workTimeService.getWorkTimeDeviations(period, employees);

        EasyMock.verify(hoursRepository, workdaysCalendarService);
        assertEquals(expectedDeviations, actual.get(employee));
    }

    @Test
    public void testGetWorkTimeDeviations_DeviationExists_PeriodHasNonWorkdays() {
        Period period = new Period();
        Project project = new Project();
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar");
        Date date1 = new Date();
        Date date2 = new Date(date1.getTime() + 1000);
        Date date3 = new Date(date2.getTime() + 1000);
        List<Day> days = asList(new Day(date1, calendar, false), new Day(date2, calendar, false), new Day(date3, calendar, false));
        Employee employee = buildEmployee("1", calendar);
        project.addTeamMember(employee);
        List<Employee> employees = asList(employee);
        Hours hours = new Hours(project, date1, employee, new HourType());
        hours.setQuantity(new BigDecimal(4.0));

        expect(hoursRepository.getActualHours(employees, period, true)).andReturn(asList(hours));
        expect(workdaysCalendarService.getDays(calendar, period)).andReturn(days);
        EasyMock.replay(hoursRepository, workdaysCalendarService);

        Map<Employee, Map<Date, BigDecimal>> actual = workTimeService.getWorkTimeDeviations(period, employees);

        EasyMock.verify(hoursRepository, workdaysCalendarService);
        assertTrue(actual.get(employee).isEmpty());
    }

    @Test
    public void testGetWorkTimeDeviations_DeviationNotExists() {
        Period period = new Period();
        Project project = new Project();
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar");
        Date date1 = new Date();
        Date date2 = new Date(date1.getTime() + 1000);
        Date date3 = new Date(date2.getTime() + 1000);
        List<Day> days = asList(new Day(date1, calendar, true), new Day(date2, calendar, true), new Day(date3, calendar, true));
        Employee employee = buildEmployee("1", calendar);
        project.addTeamMember(employee);
        List<Employee> employees = asList(employee);
        Hours hours1 = new Hours(project, date1, employee, new HourType());
        hours1.setQuantity(new BigDecimal(8.0));
        Hours hours2 = new Hours(project, date2, employee, new HourType());
        hours2.setQuantity(new BigDecimal(8.0));
        Hours hours3 = new Hours(project, date3, employee, new HourType());
        hours3.setQuantity(new BigDecimal(8.0));

        expect(hoursRepository.getActualHours(employees, period, true)).andReturn(asList(hours1, hours2, hours3));
        expect(workdaysCalendarService.getDays(calendar, period)).andReturn(days);
        EasyMock.replay(hoursRepository, workdaysCalendarService);

        Map<Employee, Map<Date, BigDecimal>> actual = workTimeService.getWorkTimeDeviations(period, employees);

        EasyMock.verify(hoursRepository, workdaysCalendarService);
        assertTrue(actual.get(employee).isEmpty());
    }

    @Test
    public void testGetWorkTimeDeviations_HoursNotExist() {
        Period period = new Period();
        Project project = new Project();
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar");
        Date date1 = new Date();
        Date date2 = new Date(date1.getTime() + 1000);
        Date date3 = new Date(date2.getTime() + 1000);
        List<Day> days = asList(new Day(date1, calendar, true), new Day(date2, calendar, true), new Day(date3, calendar, true));
        Employee employee = buildEmployee("1", calendar);
        project.addTeamMember(employee);
        List<Employee> employees = asList(employee);

        expect(hoursRepository.getActualHours(employees, period, true)).andReturn(emptyList());
        expect(workdaysCalendarService.getDays(calendar, period)).andReturn(days);
        EasyMock.replay(hoursRepository, workdaysCalendarService);

        Map<Date, BigDecimal> expectedDeviations = new HashMap<>();
        expectedDeviations.put(date1, new BigDecimal("-8.00"));
        expectedDeviations.put(date2, new BigDecimal("-8.00"));
        expectedDeviations.put(date3, new BigDecimal("-8.00"));
        Map<Employee, Map<Date, BigDecimal>> actual = workTimeService.getWorkTimeDeviations(period, employees);

        EasyMock.verify(hoursRepository, workdaysCalendarService);
        assertEquals(expectedDeviations, actual.get(employee));
    }

    @Test
    public void testGetProblemWorkTime_PeriodHasWorkingDays() throws NoSuchFieldException {
        Date date1 = new Date();
        Date date2 = new Date();
        Period period = new Period();
        Employee employee = new Employee("employee");
        Map<Employee, Map<Boolean, BigDecimal>> actualWorkTimeByEmployee = new HashMap<>();
        actualWorkTimeByEmployee.put(employee, new HashMap<Boolean, BigDecimal>() {{
            put(false, new BigDecimal(8).setScale(2));
            put(true, new BigDecimal(8).setScale(2));
        }});
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar");
        employee.setCalendar(calendar);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Map<Employee, List<Employee>> managersForApproveByEmployee = new HashMap<Employee, List<Employee>>() {{
            put(employee, asList(manager1));
        }};
        Map<Employee, List<Employee>> managersByEmployee = new HashMap<Employee, List<Employee>>() {{
            put(employee, asList(manager1, manager2));
        }};
        List<Employee> employees = asList(employee);
        Filter filter = new Filter();
        filter.setEmployees(employees);
        filter.setCustomPeriod(period);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);
        EmployeeRepository.EmployeeQuery employeeQuery = Mockito.mock(EmployeeRepository.EmployeeQuery.class, Mockito.RETURNS_DEEP_STUBS);
        Tuple tuple1 = Mockito.mock(Tuple.class);
        Tuple tuple2 = Mockito.mock(Tuple.class);

        expect(workdaysCalendarService.getDays(calendar, period))
                .andReturn(asList(new Day(date1, calendar, true), new Day(date2, calendar, true)));
        expect(hoursRepository.getActualTimeByEmployeeAndApproval(employees, period))
                .andReturn(actualWorkTimeByEmployee);
        expect(hoursRepository.getManagersForApproveByEmployee(employees, period))
                .andReturn(managersForApproveByEmployee);
        expect(projectRepository.query(true)).andReturn(projectQuery);
        expect(employeeRepository.query()).andReturn(employeeQuery);
        Mockito.when(employeeQuery
                .filter(filter)
                .notFormer()
                .list())
                .thenReturn(employees);
        Mockito.when(tuple1.get(0, Employee.class)).thenReturn(employee);
        Mockito.when(tuple1.get(1, Employee.class)).thenReturn(manager1);
        Mockito.when(tuple2.get(0, Employee.class)).thenReturn(employee);
        Mockito.when(tuple2.get(1, Employee.class)).thenReturn(manager2);
        Mockito.when(projectQuery
                .teamMembers(employees)
                .getManagers())
                .thenReturn(asList(tuple1, tuple2));
        EasyMock.replay(workdaysCalendarService, hoursRepository, projectRepository, employeeRepository);

        List<WorkTime> expected = asList(new WorkTime(employee, new BigDecimal("16.00"),
                actualWorkTimeByEmployee.get(employee), managersForApproveByEmployee.get(employee),
                managersByEmployee.get(employee)));
        List<WorkTime> actual = workTimeService.getProblemWorkTime(filter);

        EasyMock.verify(workdaysCalendarService, hoursRepository, projectRepository, employeeRepository);
        assertEqualsWorkTime(expected.get(0), actual.get(0));
    }

    @Test
    public void testGetProblemWorkTime_PeriodHasWorkingAndNonWorkingDays() throws NoSuchFieldException {
        Date date1 = new Date();
        Date date2 = new Date();
        Period period = new Period();
        Employee employee = new Employee("employee");
        Map<Employee, Map<Boolean, BigDecimal>> actualWorkTimeByEmployee = new HashMap<>();
        actualWorkTimeByEmployee.put(employee, new HashMap<Boolean, BigDecimal>() {{
            put(false, new BigDecimal(8).setScale(2));
        }});
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar");
        employee.setCalendar(calendar);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Map<Employee, List<Employee>> managersForApproveByEmployee = new HashMap<Employee, List<Employee>>() {{
            put(employee, asList(manager1));
        }};
        Map<Employee, List<Employee>> managersByEmployee = new HashMap<Employee, List<Employee>>() {{
            put(employee, asList(manager1, manager2));
        }};
        List<Employee> employees = asList(employee);
        Filter filter = new Filter();
        filter.setEmployees(employees);
        filter.setCustomPeriod(period);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);
        EmployeeRepository.EmployeeQuery employeeQuery = Mockito.mock(EmployeeRepository.EmployeeQuery.class, Mockito.RETURNS_DEEP_STUBS);
        Tuple tuple1 = Mockito.mock(Tuple.class);
        Tuple tuple2 = Mockito.mock(Tuple.class);

        expect(workdaysCalendarService.getDays(anyObject(WorkdaysCalendar.class), anyObject(Period.class)))
                .andReturn(asList(new Day(date1, calendar, true), new Day(date2, calendar, false)));
        expect(hoursRepository.getActualTimeByEmployeeAndApproval(employees, period))
                .andReturn(actualWorkTimeByEmployee);
        expect(hoursRepository.getManagersForApproveByEmployee(employees, period))
                .andReturn(managersForApproveByEmployee);
        expect(projectRepository.query(true)).andReturn(projectQuery);
        expect(employeeRepository.query()).andReturn(employeeQuery);
        Mockito.when(employeeQuery
                .filter(filter)
                .notFormer()
                .list())
                .thenReturn(employees);
        Mockito.when(tuple1.get(0, Employee.class)).thenReturn(employee);
        Mockito.when(tuple1.get(1, Employee.class)).thenReturn(manager1);
        Mockito.when(tuple2.get(0, Employee.class)).thenReturn(employee);
        Mockito.when(tuple2.get(1, Employee.class)).thenReturn(manager2);
        Mockito.when(projectQuery
                .teamMembers(employees)
                .getManagers())
                .thenReturn(asList(tuple1, tuple2));
        EasyMock.replay(workdaysCalendarService, hoursRepository, projectRepository, employeeRepository);

        List<WorkTime> expected = asList(new WorkTime(employee, new BigDecimal("8.00"),
                actualWorkTimeByEmployee.get(employee), managersForApproveByEmployee.get(employee),
                managersByEmployee.get(employee)));
        List<WorkTime> actual = workTimeService.getProblemWorkTime(filter);

        EasyMock.verify(workdaysCalendarService, hoursRepository, projectRepository, employeeRepository);
        assertEqualsWorkTime(expected.get(0), actual.get(0));
    }

    @Test
    public void testGetProblemWorkTime_PeriodHasNonWorkingDays() throws NoSuchFieldException {
        Date date1 = new Date();
        Date date2 = new Date();
        Period period = new Period();
        Employee employee = new Employee("employee");
        Map<Employee, Map<Boolean, BigDecimal>> actualWorkTimeByEmployee = new HashMap<>();
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar");
        employee.setCalendar(calendar);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Map<Employee, List<Employee>> managersForApproveByEmployee = new HashMap<Employee, List<Employee>>() {{
            put(employee, asList(manager1));
        }};
        List<Employee> employees = asList(employee);
        Filter filter = new Filter();
        filter.setEmployees(employees);
        filter.setCustomPeriod(period);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);
        EmployeeRepository.EmployeeQuery employeeQuery = Mockito.mock(EmployeeRepository.EmployeeQuery.class, Mockito.RETURNS_DEEP_STUBS);
        Tuple tuple1 = Mockito.mock(Tuple.class);
        Tuple tuple2 = Mockito.mock(Tuple.class);

        expect(workdaysCalendarService.getDays(calendar, period))
                .andReturn(asList(new Day(date1, calendar, false), new Day(date2, calendar, false)));
        expect(hoursRepository.getActualTimeByEmployeeAndApproval(employees, period))
                .andReturn(actualWorkTimeByEmployee);
        expect(hoursRepository.getManagersForApproveByEmployee(employees, period))
                .andReturn(managersForApproveByEmployee);
        expect(projectRepository.query(true)).andReturn(projectQuery);
        expect(employeeRepository.query()).andReturn(employeeQuery);
        Mockito.when(employeeQuery
                .filter(filter)
                .notFormer()
                .list())
                .thenReturn(employees);
        Mockito.when(tuple1.get(0, Employee.class)).thenReturn(employee);
        Mockito.when(tuple1.get(1, Employee.class)).thenReturn(manager1);
        Mockito.when(tuple2.get(0, Employee.class)).thenReturn(employee);
        Mockito.when(tuple2.get(1, Employee.class)).thenReturn(manager2);
        Mockito.when(projectQuery
                .teamMembers(employees)
                .getManagers())
                .thenReturn(asList(tuple1, tuple2));
        EasyMock.replay(workdaysCalendarService, hoursRepository, projectRepository, employeeRepository);

        List<WorkTime> actual = workTimeService.getProblemWorkTime(filter);

        EasyMock.verify(workdaysCalendarService, hoursRepository, projectRepository, employeeRepository);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetRequiredWorkHours_PeriodHasWorkDays_EmployeeHasFullWorkLoad() throws Exception {
        Employee employee = new Employee("test_employee");
        Period period = new Period();
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();
        employee.setCalendar(workdaysCalendar);
        List<Day> days = asList(
                new Day(new Date(), workdaysCalendar, true),
                new Day(new Date(), workdaysCalendar, true),
                new Day(new Date(), workdaysCalendar, true));

        expect(workdaysCalendarService.getDays(workdaysCalendar, period))
                .andReturn(days);
        EasyMock.replay(workdaysCalendarService);

        BigDecimal actual = workTimeService.getRequiredWorkHours(employee, period);

        EasyMock.verify(workdaysCalendarService);

        assertEquals(new BigDecimal("24.00"), actual);
    }

    @Test
    public void testGetRequiredWorkHours_PeriodHasWorkDaysAndNonWorkDays_EmployeeHasFullWorkLoad() throws Exception {
        Employee employee = new Employee("test_employee");
        Period period = new Period();
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();
        employee.setCalendar(workdaysCalendar);
        List<Day> days = asList(
                new Day(new Date(), workdaysCalendar, true),
                new Day(new Date(), workdaysCalendar, false),
                new Day(new Date(), workdaysCalendar, true));

        expect(workdaysCalendarService.getDays(workdaysCalendar, period))
                .andReturn(days);
        EasyMock.replay(workdaysCalendarService);

        BigDecimal actual = workTimeService.getRequiredWorkHours(employee, period);

        EasyMock.verify(workdaysCalendarService);

        assertEquals(new BigDecimal("16.00"), actual);
    }

    @Test
    public void testGetRequiredWorkHours_PeriodHasWorkDays_EmployeeHasNotFullWorkLoad() throws Exception {
        Employee employee = new Employee("test_employee");
        employee.setWorkLoad(50);
        Period period = new Period();
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();
        employee.setCalendar(workdaysCalendar);
        List<Day> days = asList(
                new Day(new Date(), workdaysCalendar, true),
                new Day(new Date(), workdaysCalendar, true),
                new Day(new Date(), workdaysCalendar, true));

        expect(workdaysCalendarService.getDays(workdaysCalendar, period))
                .andReturn(days);
        EasyMock.replay(workdaysCalendarService);

        BigDecimal actual = workTimeService.getRequiredWorkHours(employee, period);

        EasyMock.verify(workdaysCalendarService);

        assertEquals(new BigDecimal("12.00"), actual);
    }

    @Test
    public void testGetRequiredWorkHours_PeriodHasWorkDaysAndNonWorkDays_EmployeeHasNotFullWorkLoad() throws Exception {
        Employee employee = new Employee("test_employee");
        employee.setWorkLoad(50);
        Period period = new Period();
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();
        employee.setCalendar(workdaysCalendar);
        List<Day> days = asList(
                new Day(new Date(), workdaysCalendar, true),
                new Day(new Date(), workdaysCalendar, false),
                new Day(new Date(), workdaysCalendar, true));

        expect(workdaysCalendarService.getDays(workdaysCalendar, period))
                .andReturn(days);
        EasyMock.replay(workdaysCalendarService);

        BigDecimal actual = workTimeService.getRequiredWorkHours(employee, period);

        EasyMock.verify(workdaysCalendarService);

        assertEquals(new BigDecimal("8.00"), actual);
    }

    @Test
    public void testGetActualTime() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Employee employee = new Employee("username");
        Date start = sdf.parse("2014-11-01");
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar");
        Project project = createActiveProjectWithTeamMember(employee, calendar);
        HourType actualType = new HourType("actual");
        actualType.setActualTime(true);
        HourType notActualType = new HourType("not_actual");
        Hours approved = createHours(project, employee,
                sdf.parse("2014-11-02"), actualType, new BigDecimal("8"));
        approved.setApproved(true);
        Period period = new Period(start, sdf.parse("2014-11-30"));
        HoursRepository.HoursQuery hoursQuery = createMock(HoursRepository.HoursQuery.class);
        expect(hoursRepository.tupleQuery()).andReturn(hoursQuery);
        expect(hoursQuery.period(period)).andReturn(hoursQuery);
        expect(hoursQuery.employee(employee)).andReturn(hoursQuery);
        expect(hoursQuery.actualTime()).andReturn(hoursQuery);
        expect(hoursQuery.getTime()).andReturn(BigDecimal.valueOf(24.00));
        replay(hoursRepository, hoursQuery);

        BigDecimal actual = workTimeService.getActualWorkHours(employee, period);

        verify(hoursRepository);
        assertTrue( BigDecimal.valueOf(24.00d).compareTo(actual) == 0);
    }

    private Project createActiveProjectWithTeamMember(Employee employee, WorkdaysCalendar calendar) {
        employee.setCalendar(calendar);
        Project project = new Project();
        project.addTeamMember(employee);
        project.setStatus(Project.Status.ACTIVE);
        entityManager.persist(calendar);
        entityManager.persist(employee);
        entityManager.persist(project);
        return project;
    }

    private Employee buildEmployee(String userName, WorkdaysCalendar calendar) {
        Employee employee = new Employee(userName);
        employee.setCalendar(calendar);
        return employee;
    }

    private void assertEqualsWorkTime(WorkTime expected, WorkTime actual) {
        assertEquals(expected.getEmployee(), actual.getEmployee());
        ListAssert.assertEquals(expected.getWaitingApprovalBy(), actual.getWaitingApprovalBy());
        ListAssert.assertEquals(expected.getAllManagers(), actual.getAllManagers());
        assertEquals(expected.getApprovedTime(), actual.getApprovedTime());
        assertEquals(expected.getUnapprovedTime(), actual.getUnapprovedTime());
        assertEquals(expected.getRequiredTime(), actual.getRequiredTime());
        assertEquals(expected.getTimeDeviation(), actual.getTimeDeviation());
    }

}
