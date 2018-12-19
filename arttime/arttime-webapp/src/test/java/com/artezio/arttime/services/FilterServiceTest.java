package com.artezio.arttime.services;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.repositories.*;
import com.artezio.arttime.utils.CalendarUtils;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.ejb.SessionContext;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;
import static com.artezio.arttime.services.FilterService.MY_ACTIVE_PROJECTS_FILTER_NAME;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

@RunWith(EasyMockRunner.class)
public class FilterServiceTest {

    private FilterService filterService = new FilterService();
    @Mock
    private FilterRepository filterRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private SessionContext sessionContext;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private HourTypeService hourTypeService;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private Principal principal;

    @Before
    public void setUp() throws Exception {
        setField(filterService, "filterRepository", filterRepository);
        setField(filterService, "employeeService", employeeService);
        setField(filterService, "departmentRepository", departmentRepository);
        setField(filterService, "hourTypeService", hourTypeService);
        setField(filterService, "projectRepository", projectRepository);
        setField(filterService, "principal", principal);
    }

    @Test
    public void testGetActiveProjectsFilter() {
        String loggedUserName = "username";
        Project activeProject = new Project();
        activeProject.setStatus(Project.Status.ACTIVE);
        Project closedProject = new Project();
        closedProject.setStatus(Project.Status.CLOSED);
        List<Project> projects = asList(activeProject, closedProject);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, RETURNS_DEEP_STUBS);

        EasyMock.expect(principal.getName()).andReturn(loggedUserName).anyTimes();
        EasyMock.expect(projectRepository.query()).andReturn(projectQuery);
        Mockito.when(projectQuery
                .managedBy(loggedUserName)
                .status(ACTIVE)
                .list()
        ).thenReturn(projects);
        EasyMock.replay(projectRepository, principal);

        Filter expected = new Filter(MY_ACTIVE_PROJECTS_FILTER_NAME, loggedUserName, true, projects);
        Filter actual = filterService.getActiveProjectsFilter();

        EasyMock.verify(projectRepository, principal);
        Mockito.verify(projectQuery
                .managedBy(loggedUserName)
                .status(ACTIVE)
        ).list();

        assertEquals(expected, actual);
    }

    @Test
    public void testGetTimesheetFilter() {
        String loggedUserName = "username";
        Period period = new Period(CalendarUtils.currentWeekStartDate(), CalendarUtils.currentWeekEndDate());
        List<Project> activeProjects = asList(new Project());
        List<Project> reportedProjects = asList(new Project());
        List<String> departments = asList("dep1");
        List<HourType> hourTypes = asList(new HourType("hourType1"), new HourType("hourType2"));
        List<Employee> employees = asList(new Employee(loggedUserName));
        Employee loggedEmployee = new Employee(loggedUserName);
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, RETURNS_DEEP_STUBS);
        EmployeeRepository.EmployeeQuery employeeQuery = Mockito.mock(EmployeeRepository.EmployeeQuery.class, RETURNS_DEEP_STUBS);


        EasyMock.expect(sessionContext.getCallerPrincipal()).andReturn(principal);
        EasyMock.expect(projectRepository.query()).andReturn(projectQuery).anyTimes();
        EasyMock.expect(employeeService.getLoggedEmployee()).andReturn(Optional.of(loggedEmployee));
        EasyMock.expect(hourTypeService.getAll()).andReturn(hourTypes);
        EasyMock.expect(departmentRepository.getDepartments()).andReturn(departments);
        EasyMock.expect(principal.getName()).andReturn(loggedUserName).anyTimes();
        Mockito.when(employeeQuery.userName(loggedUserName).getSingleResult()).thenReturn(loggedEmployee);
        Mockito.when(projectQuery
                .teamMember(loggedUserName)
                .status(ACTIVE)
                .withoutMasterOrWithMasterStatus(ACTIVE)
                .list())
                .thenReturn(activeProjects);
        Mockito.when(projectQuery
                .withHoursFor(loggedUserName)
                .withHoursFrom(period.getStart())
                .withHoursTill(period.getFinish())
                .distinct()
                .list())
                .thenReturn(reportedProjects);
        EasyMock.replay(projectRepository, employeeService, hourTypeService, departmentRepository, principal);

        Filter expected = new Filter();
        expected.setEmployees(singletonList(loggedEmployee));
        expected.setProjects(activeProjects);
        expected.setDepartments(departments);
        expected.setHourTypes(hourTypes);
        expected.setEmployees(employees);
        Filter actual = filterService.getTimesheetFilter();

        EasyMock.verify(projectRepository, employeeService, hourTypeService, departmentRepository, principal);
        assertEquals(expected, actual);
    }

    @Test
    public void testSave_EquivalentExists() throws NoSuchFieldException {
        setField(filterService, "filterRepository", filterRepository);
        Filter filter = new Filter("filter", "user", false);
        FilterRepository.FilterQuery filterQuery = Mockito.mock(FilterRepository.FilterQuery.class, RETURNS_DEEP_STUBS);

        EasyMock.expect(filterRepository.query()).andReturn(filterQuery);
        Mockito.when(filterQuery
                .name(filter.getName())
                .fetchProjects()
                .fetchDepartments()
                .fetchEmployees()
                .fetchHourTypes()
                .getSingleResultOrNull()
        ).thenReturn(filter);
        EasyMock.expect(filterRepository.update(filter)).andReturn(filter);

        EasyMock.replay(filterRepository);

        Filter actual = filterService.save(filter);

        EasyMock.verify(filterRepository);
        Mockito.verify(filterQuery
                .name(filter.getName())
                .fetchProjects()
                .fetchDepartments()
                .fetchEmployees()
                .fetchHourTypes()
        ).getSingleResultOrNull();

        assertEquals(filter, actual);
    }

    @Test
    public void testSave_EquivalentNotExists() throws NoSuchFieldException {
        setField(filterService, "filterRepository", filterRepository);
        Filter filter = new Filter("filter", "user", false);
        FilterRepository.FilterQuery filterQuery = Mockito.mock(FilterRepository.FilterQuery.class, RETURNS_DEEP_STUBS);

        EasyMock.expect(filterRepository.query()).andReturn(filterQuery);
        Mockito.when(filterQuery
                .name(filter.getName())
                .fetchProjects()
                .fetchDepartments()
                .fetchEmployees()
                .fetchHourTypes()
                .getSingleResultOrNull()
        ).thenReturn(null);
        EasyMock.expect(filterRepository.create(filter)).andReturn(filter);
        EasyMock.replay(filterRepository);

        Filter actual = filterService.save(filter);

        EasyMock.verify(filterRepository);
        Mockito.verify(filterQuery
                .name(filter.getName())
                .fetchProjects()
                .fetchDepartments()
                .fetchEmployees()
                .fetchHourTypes()
        ).getSingleResultOrNull();

        assertEquals(filter, actual);
    }

    @Test
    public void testSave_ifPredefined() throws NoSuchFieldException {
        setField(filterService, "filterRepository", filterRepository);
        Filter filter = new Filter("filter", "user", true);
        setField(filter, "id", 1L);
        Filter expected = new Filter("filter", "user", true);
        setField(expected, "id", 2L);
        FilterRepository.FilterQuery filterQuery = Mockito.mock(FilterRepository.FilterQuery.class, RETURNS_DEEP_STUBS);

        EasyMock.expect(filterRepository.query()).andReturn(filterQuery);
        Mockito.when(filterQuery
                .name(filter.getName())
                .fetchProjects()
                .fetchDepartments()
                .fetchEmployees()
                .fetchHourTypes()
                .getSingleResultOrNull()
        ).thenReturn(filter);
        EasyMock.expect(filterRepository.create(expected)).andReturn(expected);
        EasyMock.replay(filterRepository);

        Filter actual = filterService.save(expected);

        EasyMock.verify(filterRepository);
        Mockito.verify(filterQuery
                .name(filter.getName())
                .fetchProjects()
                .fetchDepartments()
                .fetchEmployees()
                .fetchHourTypes()
        ).getSingleResultOrNull();

        assertEquals(expected, actual);
        assertNotEquals(filter, actual);
    }

}
