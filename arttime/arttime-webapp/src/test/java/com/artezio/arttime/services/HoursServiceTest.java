package com.artezio.arttime.services;

import static com.artezio.arttime.test.utils.CalendarUtils.createPeriod;
import static com.artezio.arttime.test.utils.CalendarUtils.getOffsetDate;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.ejb.SessionContext;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.exceptions.SaveApprovedHoursException;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.repositories.EmployeeRepository;
import com.artezio.arttime.repositories.HoursRepository;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.web.criteria.RangePeriodSelector;

@RunWith(EasyMockRunner.class)
public class HoursServiceTest {

    @Mock
    private HoursRepository hoursRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private HourTypeService hourTypeService;
    @Mock
    private SessionContext sessionContext;
    private HoursService hoursService = new HoursService();

    @Before
    public void setUp() throws NoSuchFieldException {
        setField(hoursService, "hoursRepository", hoursRepository);
        setField(hoursService, "employeeRepository", employeeRepository);
        setField(hoursService, "projectRepository", projectRepository);
        setField(hoursService, "hourTypeService", hourTypeService);
    }

    @Test
    public void testManageHours_HoursNotExistWhenSaving() throws Exception {
        Employee employee = new Employee();
        Project project = new Project();
        HourType hourType = new HourType();
        Date date = new Date();
        Hours hours = createHours(1L, project, date, employee, hourType, true);
        List<Hours> persistedHours = emptyList();
        HoursRepository.HoursQuery query = Mockito.mock(HoursRepository.HoursQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hoursRepository.query()).andReturn(query).anyTimes();
        Mockito.when(query
                .projects(singleton(project))
                .period(new Period(date, date))
                .employees(singleton(employee))
                .uncached()
                .list()).thenReturn(persistedHours);
        hoursRepository.lock(employee);
        EasyMock.expectLastCall();
        EasyMock.expect(hoursRepository.create(hours)).andReturn(hours);
        EasyMock.replay(hoursRepository);

        hoursService.manageHours(asList(hours));

        EasyMock.verify(hoursRepository);
    }

    @Test
    public void testManageHours_HoursExistWhenSaving() throws Exception {
        Employee employee = new Employee();
        Project project = new Project();
        HourType hourType = new HourType();
        Date date = new Date();
        Hours hours = createHours(1L, project, date, employee, hourType, true);
        Hours persistedHours = createHours(2L, project, date, employee, hourType, true);
        List<Hours> persistedHoursList = asList(persistedHours);
        HoursRepository.HoursQuery query = Mockito.mock(HoursRepository.HoursQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hoursRepository.query()).andReturn(query).anyTimes();
        Mockito.when(query
                .projects(singleton(project))
                .period(new Period(date, date))
                .employees(singleton(employee))
                .uncached()
                .list()).thenReturn(persistedHoursList);
        hoursRepository.lock(employee);
        EasyMock.expect(hoursRepository.update(hours)).andReturn(hours);
        EasyMock.replay(sessionContext, hoursRepository);

        hoursService.manageHours(asList(hours));

        EasyMock.verify(sessionContext, hoursRepository);
        assertEquals(persistedHours, hours);
    }

    @Test
    public void testManageHours_HoursWithoutAnyValues() throws Exception {
        Employee employee = new Employee();
        Project project = new Project();
        HourType hourType = new HourType();
        Date date = new Date();
        Hours hours = createHours(1L, project, date, employee, hourType, false);
        Hours persistedHours = createHours(2L, project, date, employee, hourType, true);
        List<Hours> hoursList = asList(hours);
        List<Hours> persistedHoursList = asList(persistedHours);
        HoursRepository.HoursQuery query = Mockito.mock(HoursRepository.HoursQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hoursRepository.query()).andReturn(query).anyTimes();
        Mockito.when(query
                .projects(singleton(project))
                .period(new Period(date, date))
                .employees(singleton(employee))
                .uncached()
                .list()).thenReturn(persistedHoursList);
        hoursRepository.lock(employee);
        hoursRepository.remove(hours);
        EasyMock.replay(hoursRepository);

        hoursService.manageHours(hoursList);

        EasyMock.verifyUnexpectedCalls(hoursRepository);
    }

    @Test
    public void testManageHours_HoursEmpty() throws Exception {
        EasyMock.replay(hoursRepository);

        hoursService.manageHours(new ArrayList<>());

        EasyMock.verifyUnexpectedCalls(hoursRepository);
    }

    @Test(expected = SaveApprovedHoursException.class)
    public void testReportHours_HoursExistWhenSaving() throws Exception {
        Employee employee = new Employee();
        Project project = new Project();
        HourType hourType = new HourType();
        Date date = new Date();
        Hours hours = createHours(1L, project, date, employee, hourType, true);
        Hours persistedHours = createHours(2L, project, date, employee, hourType, true);
        List<Hours> persistedHoursList = asList(persistedHours);
        HoursRepository.HoursQuery query = Mockito.mock(HoursRepository.HoursQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hoursRepository.query()).andReturn(query).anyTimes();
        Mockito.when(query
                .projects(singleton(project))
                .period(new Period(date, date))
                .employees(singleton(employee))
                .uncached()
                .list()).thenReturn(persistedHoursList);
        hoursRepository.lock(employee);
        EasyMock.expect(hoursRepository.create(hours)).andReturn(hours);
        EasyMock.replay(sessionContext, hoursRepository);

        hoursService.reportHours(asList(hours));

        EasyMock.verifyUnexpectedCalls(sessionContext, hoursRepository);
    }

    @Test
    public void testReportHours_HoursNotExistWhenSaving() throws Exception {
        Employee employee = new Employee();
        Project project = new Project();
        HourType hourType = new HourType();
        Date date = new Date();
        Hours hours = createHours(1L, project, date, employee, hourType, true);
        List<Hours> persistedHours = emptyList();
        HoursRepository.HoursQuery query = Mockito.mock(HoursRepository.HoursQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hoursRepository.query()).andReturn(query).anyTimes();
        Mockito.when(query
                .projects(singleton(project))
                .period(new Period(date, date))
                .employees(singleton(employee))
                .uncached()
                .list()).thenReturn(persistedHours);
        hoursRepository.lock(employee);
        EasyMock.expectLastCall();
        EasyMock.expect(hoursRepository.create(hours)).andReturn(hours);
        EasyMock.replay(sessionContext, hoursRepository);

        hoursService.reportHours(asList(hours));

        EasyMock.verify(sessionContext, hoursRepository);
    }

    @Test(expected = SaveApprovedHoursException.class)
    public void testReportHours_HoursWithoutAnyValues_ApprovedHoursExist() throws Exception {
        Employee employee = new Employee();
        Project project = new Project();
        HourType hourType = new HourType();
        Date date = new Date();
        Hours hours = createHours(1L, project, date, employee, hourType, false);
        Hours persistedHours = createHours(2L, project, date, employee, hourType, true);
        List<Hours> hoursList = asList(hours);
        List<Hours> persistedHoursList = asList(persistedHours);
        HoursRepository.HoursQuery query = Mockito.mock(HoursRepository.HoursQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hoursRepository.query()).andReturn(query).anyTimes();
        Mockito.when(query
                .projects(singleton(project))
                .period(new Period(date, date))
                .employees(singleton(employee))
                .uncached()
                .list()).thenReturn(persistedHoursList);
        EasyMock.replay(hoursRepository);

        hoursService.reportHours(hoursList);

        EasyMock.verifyUnexpectedCalls(hoursRepository);
    }

    @Test
    public void testReportHours_HoursWithoutAnyValues_ApprovedHoursNotExist() throws Exception {
        Employee employee = new Employee();
        Project project = new Project();
        HourType hourType = new HourType();
        Date date = new Date();
        Period period = new Period(date, date);
        Hours hours = createHours(1L, project, date, employee, hourType, false);
        List<Hours> hoursList = asList(hours);
        Set<Project> projects = singleton(project);
        Set<Employee> employees = singleton(employee);
        HoursRepository.HoursQuery query = Mockito.mock(HoursRepository.HoursQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hoursRepository.query()).andReturn(query).anyTimes();
        Mockito.when(query
                .projects(projects)
                .period(period)
                .employees(employees)
                .uncached()
                .list()).thenReturn(emptyList());
        hoursRepository.lock(employee);
        hoursRepository.remove(hours);
        EasyMock.replay(hoursRepository);

        hoursService.reportHours(hoursList);

        EasyMock.verifyUnexpectedCalls(hoursRepository);
        Mockito.verify(query
                .projects(projects)
                .period(period)
                .employees(employees)
                .uncached()
        ).list();
    }

    @Test
    public void testReportHours_HoursEmpty() throws Exception {
        EasyMock.replay(hoursRepository);

        hoursService.manageHours(new ArrayList<>());

        EasyMock.verifyUnexpectedCalls(hoursRepository);
    }

    @Test
    public void testExistApprovedHours() throws NoSuchFieldException {
        Employee employee = new Employee("employee");
        Project project = new Project();
        setField(project, "id", 1L);
        HourType hourType = new HourType("regular");
        Hours hour1 = new Hours(project, getOffsetDate(-1), employee, hourType);
        Hours hour2 = new Hours(project, getOffsetDate(1), employee, hourType);
        hour2.setApproved(true);
        List<Hours> persisted = asList(hour1, hour2);
        Hours hour3 = new Hours(project, getOffsetDate(1), employee, hourType);
        hoursService = new HoursService();

        boolean actual = hoursService.existApprovedHours(asList(hour3), persisted);

        assertTrue(actual);
    }

    @Test
    public void testExistApprovedHours_ifNotExists() throws NoSuchFieldException {
        Employee employee = new Employee("employee");
        Project project = new Project();
        setField(project, "id", 1L);
        HourType hourType = new HourType("regular");
        Hours hour1 = new Hours(project, getOffsetDate(-1), employee, hourType);
        Hours hour2 = new Hours(project, getOffsetDate(1), employee, hourType);
        List<Hours> persisted = asList(hour1, hour2);
        Hours hour3 = new Hours(project, getOffsetDate(1), employee, hourType);
        hoursService = new HoursService();

        boolean actual = hoursService.existApprovedHours(asList(hour3), persisted);

        assertFalse(actual);
    }

    @Test
    public void testExistApprovedHours_DifferentHourTypes() throws NoSuchFieldException {
        Employee employee = new Employee("employee");
        Project project = new Project();
        setField(project, "id", 1L);
        HourType hourType1 = new HourType("regular");
        HourType hourType2 = new HourType("overtime");
        Date date = new Date();
        Hours persisted = new Hours(project, date, employee, hourType1);
        persisted.setApproved(true);
        Hours newHour = new Hours(project, date, employee, hourType2);
        hoursService = new HoursService();

        boolean actual = hoursService.existApprovedHours(asList(newHour), asList(persisted));

        assertFalse(actual);
    }

    @Test
    public void testExistApprovedHours_DifferentDates() throws NoSuchFieldException {
        Employee employee = new Employee("employee");
        Project project = new Project();
        setField(project, "id", 1L);
        HourType hourType = new HourType("regular");
        Hours persisted = new Hours(project, getOffsetDate(0), employee, hourType);
        persisted.setApproved(true);
        Hours newHour1 = new Hours(project, getOffsetDate(-1), employee, hourType);
        Hours newHour2 = new Hours(project, getOffsetDate(1), employee, hourType);
        hoursService = new HoursService();

        boolean actual = hoursService.existApprovedHours(asList(newHour1, newHour2), asList(persisted));

        assertFalse(actual);
    }

    @Test
    public void testExistApprovedHours_DifferentProjects() throws NoSuchFieldException {
        Employee employee = new Employee("employee");
        Project project1 = new Project();
        setField(project1, "id", 1L);
        Project project2 = new Project();
        setField(project2, "id", 2L);
        HourType hourType = new HourType("regular");
        Date date = new Date();
        Hours persisted = new Hours(project1, date, employee, hourType);
        persisted.setApproved(true);
        Hours newHour = new Hours(project2, date, employee, hourType);
        hoursService = new HoursService();

        boolean actual = hoursService.existApprovedHours(asList(newHour), asList(persisted));

        assertFalse(actual);
    }

    @Test
    public void testExistApprovedHours_DifferentEmployees() throws NoSuchFieldException {
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        Project project = new Project();
        setField(project, "id", 1L);
        HourType hourType = new HourType("regular");
        Date date = new Date();
        Hours persisted = new Hours(project, date, employee1, hourType);
        persisted.setApproved(true);
        Hours newHour = new Hours(project, date, employee2, hourType);
        hoursService = new HoursService();

        boolean actual = hoursService.existApprovedHours(asList(newHour), asList(persisted));

        assertFalse(actual);
    }

    @Test
    public void testExistApprovedHours_DifferentIds() throws NoSuchFieldException {
        Employee employee = new Employee("employee");
        Project project = new Project();
        setField(project, "id", 1L);
        HourType hourType = new HourType("regular");
        Date date = new Date();
        Hours persisted = new Hours(project, date, employee, hourType);
        persisted.setApproved(true);
        setField(persisted, "id", 1L);
        Hours newHour = new Hours(project, date, employee, hourType);
        setField(newHour, "id", 2L);
        hoursService = new HoursService();

        boolean actual = hoursService.existApprovedHours(asList(newHour), asList(persisted));

        assertTrue(actual);
    }

    @Test(expected = ApplyHoursChangeException.class)
    public void testApply_checkExceptionsRethrown() throws ApplyHoursChangeException {
        hoursService = createMockBuilder(HoursService.class)
                .addMockedMethod("apply", HoursChange.class)
                .createMock();
        hoursService.apply(anyObject(HoursChange.class));

        EasyMock.expectLastCall().andThrow(new IllegalArgumentException());
        EasyMock.replay(hoursService);

        hoursService.apply(asList(new HoursChange()));
    }

    @Test
    public void testApply_HoursNotExists() {
        Employee employee = new Employee("empl1");
        Project project = new Project();
        project.setCode("BBBCode");
        HourType hourType = new HourType("off");
        Date date1 = new GregorianCalendar(2016, 5, 5).getTime();
        Hours hours = new Hours(project, date1, employee, hourType);
        HoursChange hoursChange = new HoursChange(project.getCode(), date1, employee.getUserName(), 1L);
        hoursChange.setQuantityDelta(BigDecimal.valueOf(5.0));
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);
        HoursRepository.HoursQuery hoursQuery = Mockito.mock(HoursRepository.HoursQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(projectRepository.query()).andReturn(projectQuery);
        EasyMock.expect(hoursRepository.query()).andReturn(hoursQuery);
        Mockito.when(hoursQuery
                .employee(hoursChange.getEmployeeUsername())
                .project(hoursChange.getProjectCode())
                .hourType(hoursChange.getTypeId())
                .date(hoursChange.getDate())
                .uncached()
                .getSingleResultOrNull()).thenReturn(null);
        Mockito.when(projectQuery
                .code(project.getCode())
                .getSingleResult()).thenReturn(project);
        hoursRepository.lock(employee);
        EasyMock.expect(hoursRepository.create(hours)).andReturn(hours);
        EasyMock.expect(employeeRepository.get(employee.getUserName())).andReturn(employee).times(2);
        EasyMock.expect(hourTypeService.find(anyLong())).andReturn(hourType);
        EasyMock.replay(hoursRepository, employeeRepository, hourTypeService, projectRepository);

        hoursService.apply(hoursChange);

        EasyMock.verify(hoursRepository, employeeRepository, hourTypeService, projectRepository);
        Mockito.verify(hoursQuery
                .employee(hoursChange.getEmployeeUsername())
                .project(hoursChange.getProjectCode())
                .hourType(hoursChange.getTypeId())
                .date(hoursChange.getDate())
                .uncached()
        ).getSingleResultOrNull();
        Mockito.verify(projectQuery
                .code(project.getCode())
        ).getSingleResult();
    }

    @Test
    public void testApply_HoursExists_quantityIsNotNull_commentIsNotNull() {
        Employee employee = new Employee("empl1");
        Project project = new Project();
        project.setCode("CodeProject");
        HourType hourType = new HourType("off");
        Date date1 = new GregorianCalendar(2016, 5, 5).getTime();;
        HoursChange hoursChange = new HoursChange(project.getCode(), date1, employee.getUserName(), 4L);
        hoursChange.setQuantityDelta(BigDecimal.valueOf(5.0));
        hoursChange.setComment("comment");
        Hours persistedHours = new Hours(project, date1, employee, hourType);
        persistedHours.setQuantity(BigDecimal.valueOf(8.0));
        Hours expectedHours = new Hours(project, date1, employee, hourType);
        expectedHours.setQuantity(BigDecimal.valueOf(13.0));
        expectedHours.setComment("comment");
        HoursRepository.HoursQuery query = Mockito.mock(HoursRepository.HoursQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hoursRepository.query()).andReturn(query);
        Mockito.when(query
                .employee(hoursChange.getEmployeeUsername())
                .project(hoursChange.getProjectCode())
                .hourType(hoursChange.getTypeId())
                .date(hoursChange.getDate())
                .uncached()
                .getSingleResultOrNull()).thenReturn(persistedHours);
        hoursRepository.lock(employee);
        EasyMock.expect(employeeRepository.get(employee.getUserName())).andReturn(employee).anyTimes();
        EasyMock.expect(hoursRepository.update(expectedHours)).andReturn(expectedHours);
        EasyMock.replay(hoursRepository, employeeRepository);

        hoursService.apply(hoursChange);

        EasyMock.verify(hoursRepository, employeeRepository);
        Mockito.verify(query
                .employee(hoursChange.getEmployeeUsername())
                .project(hoursChange.getProjectCode())
                .hourType(hoursChange.getTypeId())
                .date(hoursChange.getDate())
                .uncached()
        ).getSingleResultOrNull();

        assertEquals(expectedHours, persistedHours);
        assertEquals(expectedHours.getComment(), persistedHours.getComment());
        assertEquals(expectedHours.getQuantity(), persistedHours.getQuantity());
    }
    
    @Test
    public void testApply_HoursExists_quantityIsNotNull_commentIsNull() {
        Employee employee = new Employee("empl1");
        Project project = new Project();
        project.setCode("CodeProject");
        HourType hourType = new HourType("off");
        Date date1 = new GregorianCalendar(2016, 5, 5).getTime();;
        HoursChange hoursChange = new HoursChange(project.getCode(), date1, employee.getUserName(), 4L);
        hoursChange.setQuantityDelta(BigDecimal.valueOf(5.0));
        Hours persistedHours = new Hours(project, date1, employee, hourType);
        persistedHours.setQuantity(BigDecimal.valueOf(8.0));
        Hours expectedHours = new Hours(project, date1, employee, hourType);
        expectedHours.setQuantity(BigDecimal.valueOf(13.0));
        HoursRepository.HoursQuery query = Mockito.mock(HoursRepository.HoursQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hoursRepository.query()).andReturn(query);
        Mockito.when(query
                .employee(hoursChange.getEmployeeUsername())
                .project(hoursChange.getProjectCode())
                .hourType(hoursChange.getTypeId())
                .date(hoursChange.getDate())
                .uncached()
                .getSingleResultOrNull()).thenReturn(persistedHours);
        hoursRepository.lock(employee);
        EasyMock.expect(employeeRepository.get(employee.getUserName())).andReturn(employee).anyTimes();
        EasyMock.expect(hoursRepository.update(expectedHours)).andReturn(expectedHours);
        EasyMock.replay(hoursRepository, employeeRepository);

        hoursService.apply(hoursChange);

        EasyMock.verify(hoursRepository, employeeRepository);
        Mockito.verify(query
                .employee(hoursChange.getEmployeeUsername())
                .project(hoursChange.getProjectCode())
                .hourType(hoursChange.getTypeId())
                .date(hoursChange.getDate())
                .uncached()
        ).getSingleResultOrNull();

        assertEquals(expectedHours, persistedHours);
        assertNull(persistedHours.getComment());
        assertEquals(expectedHours.getQuantity(), persistedHours.getQuantity());
    }
    
    @Test
    public void testApply_HoursExists_quantityIsNull_commentIsNotNull() {
        Employee employee = new Employee("empl1");
        Project project = new Project();
        project.setCode("CodeProject");
        HourType hourType = new HourType("off");
        Date date1 = new GregorianCalendar(2016, 5, 5).getTime();;
        HoursChange hoursChange = new HoursChange(project.getCode(), date1, employee.getUserName(), 4L);
        hoursChange.setQuantityDelta(BigDecimal.valueOf(-8.0));
        hoursChange.setComment("comment");
        Hours persistedHours = new Hours(project, date1, employee, hourType);
        persistedHours.setQuantity(BigDecimal.valueOf(8.0));
        Hours expectedHours = new Hours(project, date1, employee, hourType);
        expectedHours.setQuantity(null);
        expectedHours.setComment("comment");
        HoursRepository.HoursQuery query = Mockito.mock(HoursRepository.HoursQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hoursRepository.query()).andReturn(query);
        Mockito.when(query
                .employee(hoursChange.getEmployeeUsername())
                .project(hoursChange.getProjectCode())
                .hourType(hoursChange.getTypeId())
                .date(hoursChange.getDate())
                .uncached()
                .getSingleResultOrNull()).thenReturn(persistedHours);
        hoursRepository.lock(employee);
        EasyMock.expect(employeeRepository.get(employee.getUserName())).andReturn(employee).anyTimes();
        EasyMock.expect(hoursRepository.update(expectedHours)).andReturn(expectedHours);
        EasyMock.replay(hoursRepository, employeeRepository);

        hoursService.apply(hoursChange);

        EasyMock.verify(hoursRepository, employeeRepository);
        Mockito.verify(query
                .employee(hoursChange.getEmployeeUsername())
                .project(hoursChange.getProjectCode())
                .hourType(hoursChange.getTypeId())
                .date(hoursChange.getDate())
                .uncached()
        ).getSingleResultOrNull();

        assertEquals(expectedHours, persistedHours);
        assertEquals(expectedHours.getComment(), persistedHours.getComment());
        assertNull(persistedHours.getQuantity());
    }
    
    @Test
    public void testApply_HoursExists_quantityIsNull_commentIsNull() {
        Employee employee = new Employee("empl1");
        Project project = new Project();
        project.setCode("CodeProject");
        HourType hourType = new HourType("off");
        Date date1 = new GregorianCalendar(2016, 5, 5).getTime();;
        HoursChange hoursChange = new HoursChange(project.getCode(), date1, employee.getUserName(), 4L);
        hoursChange.setQuantityDelta(BigDecimal.valueOf(-8.0));
        Hours persistedHours = new Hours(project, date1, employee, hourType);
        persistedHours.setQuantity(BigDecimal.valueOf(8.0));
        Hours expectedHours = new Hours(project, date1, employee, hourType);
        expectedHours.setQuantity(null);
        HoursRepository.HoursQuery query = Mockito.mock(HoursRepository.HoursQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hoursRepository.query()).andReturn(query);
        Mockito.when(query
                .employee(hoursChange.getEmployeeUsername())
                .project(hoursChange.getProjectCode())
                .hourType(hoursChange.getTypeId())
                .date(hoursChange.getDate())
                .uncached()
                .getSingleResultOrNull()).thenReturn(persistedHours);
        hoursRepository.lock(employee);
        EasyMock.expect(employeeRepository.get(employee.getUserName())).andReturn(employee).anyTimes();
        hoursRepository.remove(expectedHours);
        EasyMock.replay(hoursRepository, employeeRepository);

        hoursService.apply(hoursChange);

        EasyMock.verify(hoursRepository, employeeRepository);
        Mockito.verify(query
                .employee(hoursChange.getEmployeeUsername())
                .project(hoursChange.getProjectCode())
                .hourType(hoursChange.getTypeId())
                .date(hoursChange.getDate())
                .uncached()
        ).getSingleResultOrNull();

        assertEquals(expectedHours, persistedHours);
        assertNull(persistedHours.getComment());
        assertNull(persistedHours.getQuantity());
    }
    
    

    @Test
    public void testGetHours() {
        HourType hourType = new HourType("1");
        List<HourType> hourTypes = Collections.singletonList(hourType);
        Project project = new Project();
        project.setAccountableHours(hourTypes);
        Employee employee = new Employee();
        Hours hour = new Hours(project, new Date(), employee, hourType);
        RangePeriodSelector rangePeriodSelector = new RangePeriodSelector(createPeriod(-1, 1));
        Filter filter = new Filter();
        filter.setRangePeriodSelector(rangePeriodSelector);
        filter.setHourTypes(hourTypes);
        filter.setProjects(Collections.singletonList(project));
        HoursRepository.HoursQuery hoursQuery = Mockito.mock(HoursRepository.HoursQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(hoursRepository.query()).andReturn(hoursQuery);
        Mockito.when(hoursQuery
                .approved(filter.isApproved())
                .projects(Mockito.anyList())
                .departments(Mockito.anyList())
                .types(Mockito.anyList())
                .period(filter.getPeriod())
                .employees(Mockito.anyList())
                .list()
        ).thenReturn(asList(hour));
        EasyMock.replay(hoursRepository);

        List<Hours> actual = hoursService.getHours(filter);
        List<Hours> expected = Collections.singletonList(hour);

        EasyMock.verify(hoursRepository);
        Mockito.verify(hoursQuery
                .approved(filter.isApproved())
                .projects(Mockito.anyList())
                .departments(filter.getDepartments())
                .types(filter.getHourTypes())
                .period(filter.getPeriod())
                .employees(filter.getEmployees())
        ).list();

        assertEquals(expected, actual);
    }

    private Hours createHours(Long id, Project project, Date date, Employee employee, HourType hourType, boolean isApproved) {
        Hours hours = new Hours(project, date, employee, hourType);
        hours.setApproved(isApproved);
        try {
            setField(hours, "id", id);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Error during creation test hours", e);
        }
        return hours;
    }

}
