package com.artezio.arttime.web.spread_sheet;

import com.artezio.arttime.datamodel.*;
import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;

import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.services.ProjectService;
import com.artezio.arttime.web.criteria.RangePeriodSelector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import java.util.stream.Collectors;
import junitx.framework.ListAssert;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;

@RunWith(EasyMockRunner.class)
public class ProjectEffortsSpreadSheetTest {

    @InjectMocks
    private ProjectEffortsSpreadSheet spreadSheet;
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private Filter filter;
    @Mock
    private HoursService hoursService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private ProjectService projectService;
    private String department1 = "Department1";
    private String department2 = "Department2";
    private String department3 = "Department3";
    private Employee employee1;
    private Employee employee2;
    private Employee employee3;
    private List<Employee> employees = Collections.emptyList();

    @Before
    public void setUp() throws ParseException, NoSuchFieldException {
        createEmployees();
        filter = createFilter();
        expect(employeeService.getEffortsEmployees()).andReturn(employees);
        replay(employeeService);
        spreadSheet = new ProjectEffortsSpreadSheet(hoursService, projectService, employeeService, filter);
        setField(spreadSheet, "rows", new ArrayList<SpreadSheetRow<?>>());
    }

    private void createEmployees() {
        employee1 = new Employee("1", "1", "1","1", department1);
        employee2 = new Employee("2", "2", "2", "2", department2);
        employee3 = new Employee("3", "3", "3","3", department3);
        employees = Arrays.asList(employee1, employee2, employee3);
    }

    @Test
    public void testIsHighLevel_ProjectInHighLevelRow() throws NoSuchFieldException {
        Project project = new Project();
        setField(project, "id", 1L);
        HeadSpreadSheetRow row = new HeadSpreadSheetRow(project);

        boolean actual = spreadSheet.isHighLevel(row);
        assertTrue(actual);
    }

    @Test
    public void testIsHighLevel_ProjectNotInHighLevelRow() throws NoSuchFieldException {
        Project project = new Project();
        setField(project, "id", 1L);
        HeadSpreadSheetRow row = new HeadSpreadSheetRow(project);
        setField(spreadSheet, "projectsInLowLevelRows", asList(new Project()));

        boolean actual = spreadSheet.isHighLevel(row);
        assertTrue(actual);
    }

    @Test
    public void testCalculateKeysOfTotalsRows_ForMasterProject() throws NoSuchFieldException {
        Project master = new Project();
        Project subproject = new Project(master);
        setField(master, "id", 1L);
        setField(subproject, "id", 2L);

        HeadSpreadSheetRow row1 = new HeadSpreadSheetRow(master);
        HoursSpreadSheetRow row2 = new HoursSpreadSheetRow(master, null, null, new ArrayList<>());
        TotalsSpreadSheetRow row3 = new TotalsSpreadSheetRow(master, null, asList(row2));
        HeadSpreadSheetRow row4 = new HeadSpreadSheetRow(subproject);
        HoursSpreadSheetRow row5 = new HoursSpreadSheetRow(subproject, null, null, new ArrayList<>());
        TotalsSpreadSheetRow row6 = new TotalsSpreadSheetRow(subproject, null, asList(row5));
        TotalsSpreadSheetRow row7 = new TotalsSpreadSheetRow(subproject, null, Arrays.asList(row2, row5));
        List<SpreadSheetRow<?>> rows = Arrays.asList(row1, row2, row3, row4, row5, row6, row7);
        setField(spreadSheet, "rows", rows);

        List<Integer> expected = Arrays.asList(rows.get(2).getKey(), rows.get(6).getKey());
        List<Integer> actual = spreadSheet.calculateKeysOfTotalsRows(row2);
        assertEquals(2, actual.size());
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testCalculateKeysOfTotalsRows_Subproject() throws NoSuchFieldException {
        Project master = new Project();
        Project subproject = new Project(master);
        setField(master, "id", 1L);
        setField(subproject, "id", 2L);
        HeadSpreadSheetRow row1 = new HeadSpreadSheetRow(master);
        HoursSpreadSheetRow row2 = new HoursSpreadSheetRow(master, null, null, new ArrayList<>());
        TotalsSpreadSheetRow row3 = new TotalsSpreadSheetRow(master, null, asList(row2));
        HeadSpreadSheetRow row4 = new HeadSpreadSheetRow(subproject);
        HoursSpreadSheetRow row5 = new HoursSpreadSheetRow(subproject, null, null, new ArrayList<>());
        TotalsSpreadSheetRow row6 = new TotalsSpreadSheetRow(subproject, null, asList(row5));
        TotalsSpreadSheetRow row7 = new TotalsSpreadSheetRow(subproject, null, Arrays.asList(row2, row5));
        List<SpreadSheetRow<?>> rows = Arrays.asList(row1, row2, row3, row4, row5, row6, row7);
        setField(spreadSheet, "rows", rows);

        List<Integer> expected = Arrays.asList(rows.get(5).getKey(), rows.get(6).getKey());
        List<Integer> actual = spreadSheet.calculateKeysOfTotalsRows(row5);
        assertEquals(2, actual.size());
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testBuildSpreadSheetRows_NoHours() throws NoSuchFieldException {
        List<Employee> teamMembers = asList(employee1);
        HourType hourType1 = new HourType("hourType1");
        HourType hourType2 = new HourType("hourType2");
        Project project1 = buildProject(1L, teamMembers, asList(hourType1, hourType2), ACTIVE, null);
        Project project2 = buildProject(2L, teamMembers, asList(hourType1), ACTIVE, null);
        Project subproject = buildProject(3L, teamMembers, asList(hourType1), ACTIVE, project1);
        Filter filter = new Filter();
        setField(spreadSheet, "filter", filter);
        expect(hoursService.getHours(filter)).andReturn(emptyList());
        List<Project> managedProjects = asList(project1, project2, subproject);
        expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(emptyList())).andReturn(emptyList());
        replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        verify(hoursService, projectService);
        assertEquals(13, actual.size());
        assertRowMatch(actual.get(0), project1, null, null, emptyList());
        assertRowMatch(actual.get(1), project1, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(2), project1, employee1, hourType2, emptyList());
        assertRowMatch(actual.get(3), project1, null, hourType1, emptyList());
        assertRowMatch(actual.get(4), project1, null, hourType2, emptyList());
        assertRowMatch(actual.get(5), subproject, null, null, emptyList());
        assertRowMatch(actual.get(6), subproject, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(7), subproject, null, hourType1, emptyList());
        assertRowMatch(actual.get(8), project1, null, hourType1, emptyList());
        assertRowMatch(actual.get(9), project1, null, hourType2, emptyList());
        assertRowMatch(actual.get(10), project2, null, null, emptyList());
        assertRowMatch(actual.get(11), project2, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(12), project2, null, hourType1, emptyList());
    }

    @Test
    public void testBuildSpreadSheetRows_NoHours_NoSubprojects() throws NoSuchFieldException {
        List<Employee> teamMembers = asList(employee1);
        HourType hourType1 = new HourType("hourType1");
        HourType hourType2 = new HourType("hourType2");
        Project project1 = buildProject(1L, teamMembers, asList(hourType1, hourType2), ACTIVE, null);
        Project project2 = buildProject(2L, teamMembers, asList(hourType1), ACTIVE, null);
        Filter filter = new Filter();
        setField(spreadSheet, "filter", filter);
        expect(hoursService.getHours(filter)).andReturn(emptyList());
        List<Project> managedProjects = asList(project1, project2);
        expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(emptyList())).andReturn(emptyList());
        replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        verify(hoursService, projectService);
        assertEquals(8, actual.size());
        assertRowMatch(actual.get(0), project1, null, null, emptyList());
        assertRowMatch(actual.get(1), project1, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(2), project1, employee1, hourType2, emptyList());
        assertRowMatch(actual.get(3), project1, null, hourType1, emptyList());
        assertRowMatch(actual.get(4), project1, null, hourType2, emptyList());
        assertRowMatch(actual.get(5), project2, null, null, emptyList());
        assertRowMatch(actual.get(6), project2, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(7), project2, null, hourType1, emptyList());
    }

    @Test
    public void testBuildSpreadSheetRows_NoHours_ProjectsAreClosed() throws NoSuchFieldException {
        List<Employee> teamMembers = asList(employee1);
        HourType hourType1 = new HourType("hourType1");
        HourType hourType2 = new HourType("hourType2");
        Project project1 = buildProject(1L, teamMembers, asList(hourType1, hourType2), Project.Status.CLOSED, null);
        Project project2 = buildProject(2L, teamMembers, asList(hourType1), Project.Status.CLOSED, null);
        Filter filter = new Filter();
        setField(spreadSheet, "filter", filter);
        expect(hoursService.getHours(filter)).andReturn(emptyList());
        List<Project> managedProjects = asList(project1, project2);
        expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(emptyList())).andReturn(emptyList());
        replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        verify(hoursService, projectService);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void testBuildSpreadSheetRows_NoHours_SubprojectIsClosed() throws NoSuchFieldException {
        List<Employee> teamMembers = asList(employee1);
        HourType hourType1 = new HourType("hourType1");
        HourType hourType2 = new HourType("hourType2");
        Project project = buildProject(1L, teamMembers, asList(hourType1, hourType2), Project.Status.ACTIVE, null);
        Project subProject = buildProject(2L, teamMembers, asList(hourType1), Project.Status.CLOSED, project);
        Filter filter = new Filter();
        setField(spreadSheet, "filter", filter);
        expect(hoursService.getHours(filter)).andReturn(emptyList());
        List<Project> managedProjects = asList(project, subProject);
        expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(emptyList())).andReturn(emptyList());
        replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        verify(hoursService, projectService);
        assertEquals(5, actual.size());
        assertRowMatch(actual.get(0), project, null, null, emptyList());
        assertRowMatch(actual.get(1), project, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(2), project, employee1, hourType2, emptyList());
        assertRowMatch(actual.get(3), project, null, hourType1, emptyList());
        assertRowMatch(actual.get(4), project, null, hourType2, emptyList());
    }

    @Test
    public void testBuildSpreadSheetRows_SubprojectIsClosedButHasHours() throws NoSuchFieldException {
        List<Employee> teamMembers = asList(employee1);
        HourType hourType1 = new HourType("hourType1");
        HourType hourType2 = new HourType("hourType2");
        Project project = buildProject(1L, teamMembers, asList(hourType1, hourType2), Project.Status.ACTIVE, null);
        Project subProject = buildProject(2L, teamMembers, asList(hourType1), Project.Status.CLOSED, project);
        Filter filter = new Filter();
        setField(spreadSheet, "filter", filter);
        List<Hours> hours = getHours(asList(subProject));
        expect(hoursService.getHours(filter)).andReturn(hours);
        List<Project> managedProjects = asList(project, subProject);
        expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(asList(subProject))).andReturn(asList(subProject));
        replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        verify(hoursService, projectService);
        assertEquals(10, actual.size());
        assertRowMatch(actual.get(0), project, null, null, emptyList());
        assertRowMatch(actual.get(1), project, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(2), project, employee1, hourType2, emptyList());
        assertRowMatch(actual.get(3), project, null, hourType1, emptyList());
        assertRowMatch(actual.get(4), project, null, hourType2, emptyList());
        assertRowMatch(actual.get(5), subProject, null, null, emptyList());
        assertRowMatch(actual.get(6), subProject, employee1, hourType1, findHours(hours, subProject, employee1, hourType1));
        assertRowMatch(actual.get(7), subProject, null, hourType1, emptyList());
        assertRowMatch(actual.get(8), project, null, hourType1, emptyList());
        assertRowMatch(actual.get(9), project, null, hourType2, emptyList());
    }

    @Test
    public void testBuildSpreadSheetRows_NoHours_FilterByEmployees_EmployeesInProjectsEqualToEmployeesInFilter() throws NoSuchFieldException {
        List<Employee> teamMembers = asList(employee1);
        HourType hourType1 = new HourType("hourType1");
        HourType hourType2 = new HourType("hourType2");
        Project project1 = buildProject(1L, teamMembers, asList(hourType1, hourType2), ACTIVE, null);
        Project project2 = buildProject(2L, teamMembers, asList(hourType1), ACTIVE, null);
        Project subproject = buildProject(3L, teamMembers, asList(hourType1), ACTIVE, project1);
        Filter filter = new Filter();
        filter.setEmployees(teamMembers);
        setField(spreadSheet, "filter", filter);

        expect(hoursService.getHours(filter)).andReturn(emptyList());
        List<Project> managedProjects = asList(project1, project2, subproject);
        expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(emptyList())).andReturn(emptyList());
        replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        verify(hoursService, projectService);

        assertEquals(13, actual.size());
        assertRowMatch(actual.get(0), project1, null, null, emptyList());
        assertRowMatch(actual.get(1), project1, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(2), project1, employee1, hourType2, emptyList());
        assertRowMatch(actual.get(3), project1, null, hourType1, emptyList());
        assertRowMatch(actual.get(4), project1, null, hourType2, emptyList());

        assertRowMatch(actual.get(5), subproject, null, null, emptyList());
        assertRowMatch(actual.get(6), subproject, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(7), subproject, null, hourType1, emptyList());
        assertRowMatch(actual.get(8), project1, null, hourType1, emptyList());
        assertRowMatch(actual.get(9), project1, null, hourType2, emptyList());
        assertRowMatch(actual.get(10), project2, null, null, emptyList());
        assertRowMatch(actual.get(11), project2, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(12), project2, null, hourType1, emptyList());
    }

    @Test
    public void testBuildSpreadSheetRows_NoHours_FilterByEmployees_EmployeesInProjectsNotEqualToEmployeesInFilter() throws NoSuchFieldException {
        List<Employee> teamMembers = asList(employee1);
        HourType hourType1 = new HourType("hourType1");
        HourType hourType2 = new HourType("hourType2");
        Project project1 = buildProject(1L, teamMembers, asList(hourType1, hourType2), ACTIVE, null);
        Project project2 = buildProject(2L, teamMembers, asList(hourType1), ACTIVE, null);
        Project subproject = buildProject(3L, teamMembers, asList(hourType1), ACTIVE, project1);
        Filter filter = new Filter();
        setField(spreadSheet, "filter", filter);

        expect(hoursService.getHours(filter)).andReturn(emptyList());
        expect(projectService.getEffortsProjects(filter)).andReturn(emptyList());
        EasyMock.expect(projectService.fetchComplete(eq(emptyList()))).andReturn(emptyList()).anyTimes();
        
        replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        verify(hoursService, projectService);

        assertEquals(0, actual.size());
    }

    @Test
    public void testBuildSpreadSheetRows_NoHours_FilterByHourTypes_HourTypesInProjectsEqualToHourTypesInFilter() throws NoSuchFieldException {
        List<Employee> teamMembers = asList(employee1);
        HourType hourType1 = new HourType("hourType1");
        HourType hourType2 = new HourType("hourType2");
        Project project1 = buildProject(1L, teamMembers, asList(hourType1, hourType2), ACTIVE, null);
        Project project2 = buildProject(2L, teamMembers, asList(hourType1), ACTIVE, null);
        Project subproject = buildProject(3L, teamMembers, asList(hourType1), ACTIVE, project1);
        SpreadSheet.HoursIndexedBundle indexedHours = spreadSheet.new HoursIndexedBundle(emptyList());
        Filter filter = new Filter();
        filter.setHourTypes(asList(hourType1, hourType2));
        setField(spreadSheet, "filter", filter);

        expect(hoursService.getHours(filter)).andReturn(emptyList());
        List<Project> managedProjects = asList(project1, project2, subproject);
        expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(emptyList())).andReturn(emptyList());
        replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        verify(hoursService, projectService);

        assertEquals(13, actual.size());
        assertRowMatch(actual.get(0), project1, null, null, emptyList());
        assertRowMatch(actual.get(1), project1, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(2), project1, employee1, hourType2, emptyList());
        assertRowMatch(actual.get(3), project1, null, hourType1, emptyList());
        assertRowMatch(actual.get(4), project1, null, hourType2, emptyList());

        assertRowMatch(actual.get(5), subproject, null, null, emptyList());
        assertRowMatch(actual.get(6), subproject, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(7), subproject, null, hourType1, emptyList());
        assertRowMatch(actual.get(8), project1, null, hourType1, emptyList());
        assertRowMatch(actual.get(9), project1, null, hourType2, emptyList());
        assertRowMatch(actual.get(10), project2, null, null, emptyList());
        assertRowMatch(actual.get(11), project2, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(12), project2, null, hourType1, emptyList());
    }

    @Test
    public void testBuildSpreadSheetRows_NoHours_FilterByHourTypes_HourTypesInProjectsNotEqualToHourTypesInFilter() throws NoSuchFieldException {
        List<Employee> teamMembers = asList(employee1);
        HourType hourType1 = new HourType("hourType1");
        HourType hourType2 = new HourType("hourType2");
        Project project1 = buildProject(1L, teamMembers, asList(hourType1, hourType2), ACTIVE, null);
        Project project2 = buildProject(2L, teamMembers, asList(hourType1), ACTIVE, null);
        Project subproject = buildProject(3L, teamMembers, asList(hourType1), ACTIVE, project1);
        SpreadSheet.HoursIndexedBundle indexedHours = spreadSheet.new HoursIndexedBundle(emptyList());
        Filter filter = new Filter();
        filter.setHourTypes(asList(hourType2));
        setField(spreadSheet, "filter", filter);

        expect(hoursService.getHours(filter)).andReturn(emptyList());
        List<Project> managedProjects = asList(project1, project2, subproject);
        expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(emptyList())).andReturn(emptyList());
        replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        verify(hoursService, projectService);

        assertEquals(8, actual.size());
        assertRowMatch(actual.get(0), project1, null, null, emptyList());
        assertRowMatch(actual.get(1), project1, employee1, hourType2, emptyList());
        assertRowMatch(actual.get(2), project1, null, hourType2, emptyList());
    }

    @Test
    public void testBuildSpreadSheetRows_NoHours_SomeSubprojecsInHighLevelRow() throws NoSuchFieldException {
        List<Employee> teamMembers = asList(employee1);
        HourType hourType1 = new HourType("hourType1");
        List<HourType> hourTypes = asList(hourType1);
        Project project1 = buildProject(1L, teamMembers, hourTypes, ACTIVE, null);
        Project project2 = buildProject(2L, emptyList(), hourTypes, ACTIVE, null);
        Project subproject1 = buildProject(3L, teamMembers, asList(hourType1), ACTIVE, project1);
        Project subproject2 = buildProject(4L, teamMembers, asList(hourType1), ACTIVE, project2);
        Filter filter = new Filter();
        setField(spreadSheet, "filter", filter);

        expect(hoursService.getHours(filter)).andReturn(emptyList());
        List<Project> managedProjects = asList(project1, subproject1, subproject2);
        expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(emptyList())).andReturn(emptyList());
        replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        verify(hoursService, projectService);

        assertEquals(10, actual.size());
        assertRowMatch(actual.get(0), project1, null, null, emptyList());
        assertRowMatch(actual.get(1), project1, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(2), project1, null, hourType1, emptyList());
        assertRowMatch(actual.get(3), subproject1, null, null, emptyList());
        assertRowMatch(actual.get(4), subproject1, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(5), subproject1, null, hourType1, emptyList());
        assertRowMatch(actual.get(6), project1, null, hourType1, emptyList());
        assertRowMatch(actual.get(7), subproject2, null, null, emptyList());
        assertRowMatch(actual.get(8), subproject2, employee1, hourType1, emptyList());
        assertRowMatch(actual.get(9), subproject2, null, hourType1, emptyList());
    }

    @Test
    public void testBuildSpreadSheetRows_TeamMembersNotSorted() throws NoSuchFieldException {
        HourType hourType = filter.getHourTypes().get(0);
        Project project = buildProject(1L, asList(employee1, employee2, employee3), asList(hourType), ACTIVE, null);
        Filter overrideFilter = new Filter();
        overrideFilter.setHourTypes(asList(hourType));
        overrideFilter.setEmployees(asList(employee1, employee2, employee3));
        overrideFilter.setProjects(asList(project));
        setField(spreadSheet, "filter", overrideFilter);
        project.addManager(employee1);
        List<Hours> hours = getHours(asList(project));

        List<Employee> availableEmployees = asList(employee1, employee2, employee3);
        expect(hoursService.getHours(overrideFilter)).andReturn(hours);
        List<Project> managedProjects = asList(project);
        expect(projectService.getEffortsProjects(overrideFilter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects).anyTimes();
        replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        verify(hoursService, projectService);

        assertEquals(5, actual.size());
        assertRowMatch(actual.get(0), project, null, null, emptyList());
        assertRowMatch(actual.get(1), project, employee1, hourType, findHours(hours, project, employee1, hourType));
        assertRowMatch(actual.get(2), project, employee2, hourType, findHours(hours, project, employee2, hourType));
        assertRowMatch(actual.get(3), project, employee3, hourType, findHours(hours, project, employee3, hourType));
        assertRowMatch(actual.get(4), project, null, hourType, emptyList());
    }

    @Test
    public void testBuildSpreadSheetRows_ProjectsNotSorted() {
        List<Employee> projectTeam = asList(employee1);
        HourType hourType = filter.getHourTypes().get(0);
        Project project1 = buildProject(1L, projectTeam, asList(hourType), ACTIVE, null);
        project1.addManager(employee1);
        Project project2 = buildProject(2L, projectTeam, asList(hourType), ACTIVE, null);
        project2.addManager(employee1);
        Project project3 = buildProject(3L, projectTeam, asList(hourType), ACTIVE, null);
        project3.addManager(employee1);
        List<Project> projects = asList(project1, project2, project3);
        List<Hours> hours = getHours(projects);

        expect(hoursService.getHours(filter)).andReturn(hours);
        List<Project> managedProjects = asList(project3, project2, project1);
        expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects);
        expect(projectService.fetchComplete(projects)).andReturn(projects);
        replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        verify(hoursService, projectService);

        assertEquals(9, actual.size());
        assertRowMatch(actual.get(0), project1, null, null, emptyList());           //master project head
        assertRowMatch(actual.get(1), project1, employee1, hourType, findHours(hours, project1, employee1, hourType));
        assertRowMatch(actual.get(2), project1, null, hourType, emptyList());      //master project total1
        assertRowMatch(actual.get(3), project2, null, null, emptyList());           //master project head
        assertRowMatch(actual.get(4), project2, employee1, hourType, findHours(hours, project2, employee1, hourType));
        assertRowMatch(actual.get(5), project2, null, hourType, emptyList());      //master project total1
        assertRowMatch(actual.get(6), project3, null, null, emptyList());           //master project head
        assertRowMatch(actual.get(7), project3, employee1, hourType, findHours(hours, project3, employee1, hourType));
        assertRowMatch(actual.get(8), project3, null, hourType, emptyList());      //master project total1
    }

    private Project buildProject(Long id, List<Employee> team, List<HourType> accountableHours, Project.Status status,
                                 Project master) {
        Project project = new Project();
        project.setCode(id.toString());
        project.setTeam(team);
        project.setAccountableHours(accountableHours);
        project.setStatus(status);
        if (master != null) {
            project.setMaster(master);
        }
        try {
            setField(project, "id", id);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Error during building test project", e);
        }
        return project;
    }

    private Filter createFilter() throws ParseException, NoSuchFieldException {
        Date date1 = sdf.parse("1-01-2014");
        Date date2 = sdf.parse("2-01-2014");
        Period period = new Period(date1, date2);

        HourType hourType1 = new HourType("regular time");
        setField(hourType1, "id", 1L);
        hourType1.setActualTime(true);
        HourType hourType2 = new HourType("over time");
        setField(hourType1, "id", 2L);
        List<HourType> hourTypes = asList(hourType1, hourType2);

        List<Employee> employees = asList(employee1, employee2);

        Project project1 = buildProject(1L, asList(employee1), asList(hourType1, hourType2), ACTIVE, null);
        Project project2 = buildProject(2L, asList(employee1, employee2), asList(hourType1), ACTIVE, null);
        List<Project> projects = asList(project1, project2);

        Filter filter = new Filter();
        filter.setEmployees(employees);
        filter.setProjects(projects);
        filter.setHourTypes(hourTypes);
        filter.setDepartments(asList(department1, department2, department3));
        filter.setRangePeriodSelector(new RangePeriodSelector(period));
        return filter;
    }

    private List<Hours> getHours(List<Project> projects) {
        List<Hours> hours = new ArrayList<>();
        projects.forEach(prj ->
                prj.getTeam().forEach(employee -> {
                    prj.getAccountableHours().forEach(hourType ->
                            filter.getRangePeriodSelector().getPeriod().getDays().forEach(date -> {
                                Hours hour = new Hours(prj, date, employee, hourType);
                                hours.add(hour);
                            }));
                }));
        return hours;
    }

    private List<Hours> findHours(List<Hours> hours, Project project, Employee employee, HourType hourType) {
        return hours.stream()
                .filter(hour -> hour.getProject().equals(project)
                        && hour.getEmployee().equals(employee)
                        && hour.getType().equals(hourType))
                .collect(Collectors.toList());
    }

    private void assertRowMatch(SpreadSheetRow row, Project project, Employee employee, HourType type, List<Hours> hours) {
        assertEquals(project, row.getProject());
        assertEquals(employee, row.getEmployee());
        assertEquals(type, row.getHourType());
        Map<Date, Hours> hoursMap = hours.stream()
                .collect(Collectors.toMap(Hours::getDate, h -> h));
        assertEquals(hoursMap, row.getValuesMap());
    }

}
