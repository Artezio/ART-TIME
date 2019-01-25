package com.artezio.arttime.services.integration;

import com.artezio.arttime.datamodel.*;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.*;
import com.artezio.arttime.repositories.*;
import com.artezio.arttime.web.criteria.RangePeriodSelector;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.*;

import static java.util.Arrays.asList;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(EasyMockRunner.class)
public class IntegrationFacadeTest {

    @TestSubject
    private IntegrationFacade facade = new IntegrationFacade();
    @Mock
    private HoursService hoursService;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private HourTypeService hourTypeService;
    @Mock
    private HoursRepository hoursRepository;
    @Mock
    private WorkdaysCalendarService workdaysCalendarService;

    @Test
    public void testGetProjects() {
        List<Project> expecteds = new ArrayList<>();

        expect(projectService.getAll()).andReturn(expecteds);
        expect(projectService.fetchComplete(expecteds)).andReturn(expecteds);
        EasyMock.replay(projectService);

        List<Project> actuals = facade.getProjects();

        EasyMock.verify(projectService);
        assertSame(expecteds, actuals);
    }

    @Test
    public void testGetHourTypes() {
        List<HourType> expecteds = new ArrayList<>();
        expect(hourTypeService.getAll()).andReturn(expecteds);
        replay(hourTypeService);

        List<HourType> actuals = facade.getHourTypes();

        verify(hourTypeService);
        assertSame(expecteds, actuals);
    }

    @Test
    public void testGetHours_approvedOnly() {
        List<Hours> expecteds = new ArrayList<>();
        Project project = new Project();
        project.setCode("TEST-PROJECT");
        Date from = new GregorianCalendar(2011, 1, 5).getTime();
        Date to = new GregorianCalendar(2011, 1, 15).getTime();
        Period period = new Period(from, to);
        Filter expectedFilter = new Filter();
        expectedFilter.setProjects(asList(project));
        expectedFilter.setApproved(Boolean.TRUE);
        RangePeriodSelector rangePeriodSelector = new RangePeriodSelector(period);
        expectedFilter.setRangePeriodSelector(rangePeriodSelector);
        HoursSearchCriteria criteria = new HoursSearchCriteria(from, to, true, asList("TEST-PROJECT"));
        Project anotherProject = new Project();
        Project anotherSubproject = new Project();
        anotherProject.setCode("ANOTHER-PROJECT");
        anotherSubproject.setCode("ANOTHER-SUBPROJECT");
        List<Project> allProjects = Arrays.asList(project, anotherProject, anotherSubproject);
        expect(projectService.getAll()).andReturn(allProjects).anyTimes();

        expect(hoursService.getHours(eq(expectedFilter))).andReturn(expecteds);
        replay(projectService, hoursRepository, hoursService);

        List<Hours> actuals = facade.getHours(criteria);

        verify(projectService, hoursRepository);
        assertSame(expecteds, actuals);
    }

    @Test
    public void testGetHours() {
        List<Hours> expecteds = new ArrayList<>();
        Project project = new Project();
        project.setCode("TEST-PROJECT");
        Date from = new GregorianCalendar(2011, 1, 5).getTime();
        Date to = new GregorianCalendar(2011, 1, 15).getTime();
        Period period = new Period(from, to);
        Filter expectedFilter = new Filter();
        expectedFilter.setProjects(asList(project));
        expectedFilter.setApproved(null);
        RangePeriodSelector rangePeriodSelector = new RangePeriodSelector(period);
        expectedFilter.setRangePeriodSelector(rangePeriodSelector);
        HoursSearchCriteria hoursSearchCriteria = new HoursSearchCriteria(from, to, true, asList("TEST-PROJECT"));

        Project anotherProject = new Project();
        anotherProject.setCode("OtherProject");
        List<Project> allProjects = Arrays.asList(project, anotherProject);
        expect(projectService.getAll()).andReturn(allProjects).anyTimes();
        EasyMock.expect(hoursService.getHours(expectedFilter)).andReturn(expecteds);
        EasyMock.replay(projectService, hoursService);

        List<Hours> actuals = facade.getHours(hoursSearchCriteria);

        verify(projectService, hoursService);
        assertSame(expecteds, actuals);
    }

    @Test
    public void testGetHours_includeSubprojects() {
        List<Hours> expecteds = new ArrayList<>();
        Project project = new Project();
        project.setCode("TEST-PROJECT");
        Project subproject = new Project(project);
        subproject.setCode("TEST_SUBPROJECT");
        Date from = new GregorianCalendar(2011, 1, 5).getTime();
        Date to = new GregorianCalendar(2011, 1, 15).getTime();
        Period period = new Period(from, to);
        List<Project> projects = asList(project, subproject);
        Filter filter = new Filter();
        filter.setProjects(projects);
        filter.setApproved(null);
        RangePeriodSelector rangePeriodSelector = new RangePeriodSelector(period);
        filter.setRangePeriodSelector(rangePeriodSelector);
        HoursSearchCriteria criteria = new HoursSearchCriteria(from, to, false, asList("TEST-PROJECT"));
        criteria.setIncludeSubprojects(true);
        Project anotherProject = new Project();
        Project anotherSubproject = new Project();
        anotherProject.setCode("ANOTHER-PROJECT");
        anotherSubproject.setCode("ANOTHER-SUBPROJECT");
        List<Project> allProjects = Arrays.asList(project, subproject, anotherProject, anotherSubproject);
        expect(projectService.getAll()).andReturn(allProjects).anyTimes();
        expect(hoursService.getHours(eq(filter))).andReturn(expecteds);
        replay(projectService, hoursRepository, hoursService);

        List<Hours> actuals = facade.getHours(criteria);

        verify(projectService, hoursRepository);
        assertSame(expecteds, actuals);
    }

    @Test
    public void testGetHours_includeSubprojects_ExpectDistinct() {
        List<Hours> expecteds = new ArrayList<>();
        HourType type = new HourType("Type");
        Project project = new Project();
        project.setCode("TEST-PROJECT");
        Project subproject = new Project(project);
        subproject.setCode("TEST_SUBPROJECT");
        Project subproject2 = new Project(project);
        subproject2.setCode("TEST_SUBPROJECT_2");
        Employee employee = new Employee("employee");
        Date from = new GregorianCalendar(2011, 1, 5).getTime();
        Date to = new GregorianCalendar(2011, 1, 15).getTime();
        Period period = new Period(from, to);
        List<Project> allProjects = asList(project, subproject, subproject2);
        Filter filter = new Filter();
        filter.setProjects(allProjects);
        filter.setApproved(null);
        Hours h1 = new Hours(project, from, employee, type);
        Hours h2 = new Hours(subproject, from, employee, type);
        Hours h3 = new Hours(subproject2, from, employee, type);
        expecteds.add(h1);
        expecteds.add(h2);
        expecteds.add(h3);
        RangePeriodSelector rangePeriodSelector = new RangePeriodSelector(period);
        filter.setRangePeriodSelector(rangePeriodSelector);
        HoursSearchCriteria criteria = new HoursSearchCriteria(from, to, false, asList("TEST-PROJECT", "TEST_SUBPROJECT"));
        criteria.setIncludeSubprojects(true);
        Project anotherProject = new Project();
        Project anotherSubproject = new Project();
        anotherProject.setCode("ANOTHER-PROJECT");
        anotherSubproject.setCode("ANOTHER-SUBPROJECT");
        List<Project> allStoredProjects = Arrays.asList(project, subproject, subproject2, anotherProject, anotherSubproject);
        expect(projectService.getAll()).andReturn(allStoredProjects).anyTimes();
        EasyMock.expect(hoursService.getHours(filter)).andReturn(expecteds);
        EasyMock.replay(projectService, hoursService);

        List<Hours> actuals = facade.getHours(criteria);

        EasyMock.verify(projectService, hoursService);
        assertSame(expecteds, actuals);
    }

    @Test
    public void testGetHours_emptyProjectsList_expectAllProjects() {
        List<Hours> expectedHours = new ArrayList<>();
        HourType type = new HourType("Type");
        Project project = new Project();
        project.setCode("TEST-PROJECT");
        Project subproject = new Project(project);
        subproject.setCode("TEST_SUBPROJECT");
        Project subproject2 = new Project(project);
        subproject2.setCode("TEST_SUBPROJECT_2");
        Employee employee = new Employee("employee");
        Date from = new GregorianCalendar(2011, 1, 5).getTime();
        Date to = new GregorianCalendar(2011, 1, 15).getTime();
        Period period = new Period(from, to);
        List<Project> allProjects = asList(project, subproject, subproject2);
        Filter filter = new Filter();
        filter.setProjects(allProjects);
        filter.setApproved(null);
        Hours h1 = new Hours(project, from, employee, type);
        Hours h2 = new Hours(subproject, from, employee, type);
        Hours h3 = new Hours(subproject2, from, employee, type);
        expectedHours.add(h1);
        expectedHours.add(h2);
        expectedHours.add(h3);
        RangePeriodSelector rangePeriodSelector = new RangePeriodSelector(period);
        filter.setRangePeriodSelector(rangePeriodSelector);
        HoursSearchCriteria criteria = new HoursSearchCriteria(from, to, false, Collections.emptyList());
        criteria.setIncludeSubprojects(true);
        Project anotherProject = new Project();
        Project anotherSubproject = new Project();
        anotherProject.setCode("ANOTHER-PROJECT");
        anotherSubproject.setCode("ANOTHER-SUBPROJECT");
        List<Project> allStoredProjects = Arrays.asList(project, subproject, subproject2, anotherProject, anotherSubproject);
        expect(projectService.getAll()).andReturn(allStoredProjects).anyTimes();
        EasyMock.expect(hoursService.getHours(filter)).andReturn(expectedHours);
        EasyMock.replay(projectService, hoursService);

        List<Hours> actualHours = facade.getHours(criteria);

        EasyMock.verify(projectService, hoursService);
        assertSame(expectedHours, actualHours);
    }

    @Test
    public void testGetCalendars() {
        List<WorkdaysCalendar> calendars = new ArrayList<WorkdaysCalendar>();
        expect(workdaysCalendarService.getAll()).andReturn(calendars);
        replay(workdaysCalendarService);

        List<WorkdaysCalendar> actual = facade.getCalendars();

        verify(workdaysCalendarService);
    }

    @Test
    public void testGetCalendarDays() {
        List<Day> days = new ArrayList<Day>();
        Period period = new Period(new Date(), new Date());
        WorkdaysCalendar calendar = new WorkdaysCalendar();
        expect(workdaysCalendarService.getDays(calendar, period)).andReturn(days);
        replay(workdaysCalendarService);

        List<Day> actual = facade.getCalendarDays(calendar, period.getStart(), period.getFinish());

        verify(workdaysCalendarService);
    }

    @Test
    public void testGetProjectTeam() throws Exception {
        Project project = new Project();
        project.addTeamMember(new Employee("Emp1"));
        project.addTeamMember(new Employee("Emp2"));
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(projectRepository.query()).andReturn(projectQuery);
        Mockito.when(projectQuery
                .code("project_code")
                .fetchTeam()
                .getSingleResultOrNull())
                .thenReturn(project);
        EasyMock.replay(projectRepository);

        List<Employee> actual = facade.getProjectTeam("project_code");

        EasyMock.verify(projectRepository);
        assertEquals(project.getTeam(), new HashSet<>(actual));
    }

    @Test(expected = ProjectNotFoundException.class)
    public void testGetProjectTeamWhenProjectCodeIsNull() throws Exception {
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(projectRepository.query()).andReturn(projectQuery);
        Mockito.when(projectQuery
                .code(null)
                .fetchTeam()
                .getSingleResultOrNull())
                .thenReturn(null);
        EasyMock.replay(projectRepository);

        facade.getProjectTeam(null);

        EasyMock.verify(projectRepository);
    }

    @Test(expected = ProjectNotFoundException.class)
    public void testGetProjectTeamWhenProjectNotExist() throws Exception {
        String invalidCode = "InvalidProjectCode";
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(projectRepository.query()).andReturn(projectQuery);
        Mockito.when(projectQuery
                .code(invalidCode)
                .fetchTeam()
                .getSingleResultOrNull())
                .thenReturn(null);
        EasyMock.replay(projectRepository);

        facade.getProjectTeam(invalidCode);

        EasyMock.verify(projectRepository);
    }

    @Test
    public void testGetAllSubprojects() {
        Project project = new Project();
        project.setCode("MasterProject");
        Project subProject = new Project(project);
        subProject.setCode("SubProject");
        Project subProject2 = new Project(subProject);
        subProject2.setCode("SubProject2");
        List<Project> projects = new ArrayList<>(asList(project, subProject, subProject2));
        ProjectRepository.ProjectQuery projectQuery = Mockito.mock(ProjectRepository.ProjectQuery.class, Mockito.RETURNS_DEEP_STUBS);

        EasyMock.expect(projectRepository.query()).andReturn(projectQuery);
        Mockito.when(projectQuery
                .code("MasterProject")
                .getSingleResultOrNull())
                .thenReturn(project);
        EasyMock.expect(projectService.getProjectHierarchy(project)).andReturn(projects);
        EasyMock.expect(projectService.fetchComplete(projects)).andReturn(projects);
        EasyMock.replay(projectService, projectRepository);

        List<Project> actual = facade.getAllSubprojects("MasterProject");

        EasyMock.verify(projectRepository, projectService);
        assertEquals(asList(subProject, subProject2), actual);
    }

    @Test
    public void testModifyHours() throws Exception {
        HoursChange change = new HoursChange();
        hoursService.apply(asList(change));
        expectLastCall().times(1);
        replay(hoursService);

        facade.modifyHours(asList(change));
        verify(hoursService);
    }

}
