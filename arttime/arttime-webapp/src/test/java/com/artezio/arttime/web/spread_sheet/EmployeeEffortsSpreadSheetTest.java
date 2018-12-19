package com.artezio.arttime.web.spread_sheet;

import com.artezio.arttime.datamodel.*;
import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.services.ProjectService;
import com.artezio.arttime.web.criteria.RangePeriodSelector;
import junitx.framework.ListAssert;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

@RunWith(EasyMockRunner.class)
public class EmployeeEffortsSpreadSheetTest {

    private EmployeeEffortsSpreadSheet spreadSheet;
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private Filter filter;
    @Mock
    private HoursService hoursService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private ProjectService projectService;
    private List<Employee> employees;
    private String department1 = "Department1";
    private String department2 = "Department2";
    private String department3 = "Department3";
    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @Before
    public void setUp() throws ParseException, NoSuchFieldException {
        createEmployees();
        Date date1 = sdf.parse("1-01-2014");
        Date date2 = sdf.parse("2-01-2014");
        Period period = new Period(date1, date2);
        filter = new Filter();
        filter.setRangePeriodSelector(new RangePeriodSelector(period));
        expect(employeeService.getEffortsEmployees()).andReturn(employees);
        replay(employeeService);
        spreadSheet = new EmployeeEffortsSpreadSheet(hoursService, projectService, employeeService, filter);
        setField(spreadSheet, "rows", new LinkedList<SpreadSheetRow<?>>());
    }

    private void createEmployees() {
        employee1 = new Employee("1", "1", "1","1", department1);
        employee2 = new Employee("2", "2", "2", "2", department2);
        employee3 = new Employee("3", "3", "3","3", department3);
        employees = Arrays.asList(employee1, employee2, employee3);
    }

    @Test
    public void testBuildSpreadSheetRows_FilterIsEmpty() {
        List<Employee> project1Team = asList(employee1);
        List<Employee> project2Team = asList(employee1, employee2, employee3);
        HourType hourType1 = new HourType("1");
        HourType hourType2 = new HourType("2");
        Project project1 = buildProject(1L, project1Team, asList(hourType1, hourType2), ACTIVE, null);
        project1.addManager(employee1);
        Project project2 = buildProject(2L, project2Team, asList(hourType1), ACTIVE, null);
        project2.addManager(employee1);
        List<Hours> hours = getHours(asList(project1, project2));

        EasyMock.expect(hoursService.getHours(filter)).andReturn(hours);
        List<Project> managedProjects = asList(project1, project2);
        EasyMock.expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects).anyTimes();
        EasyMock.replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        EasyMock.verify(hoursService, projectService);

        SpreadSheetRow<?> expected1 = new HeadSpreadSheetRow(employee1);
        SpreadSheetRow<?> expected2 = createSpreadSheetRow(project1, employee1, hourType1, findHours(hours, project1, employee1, hourType1));
        SpreadSheetRow<?> expected3 = createSpreadSheetRow(project1, employee1, hourType2, findHours(hours, project1, employee1, hourType2));
        SpreadSheetRow<?> expected4 = createSpreadSheetRow(project2, employee1, hourType1, findHours(hours, project2, employee1, hourType1));
        SpreadSheetRow<?> expected5 = createTotalsSpreadSheetRow(employee1, hourType1, asList((HoursSpreadSheetRow) expected2, (HoursSpreadSheetRow) expected4));
        SpreadSheetRow<?> expected6 = createTotalsSpreadSheetRow(employee1, hourType2, asList((HoursSpreadSheetRow) expected3));
        SpreadSheetRow<?> expected7 = new HeadSpreadSheetRow(employee2);
        SpreadSheetRow<?> expected8 = createSpreadSheetRow(project2, employee2, hourType1, findHours(hours, project2, employee2, hourType1));
        SpreadSheetRow<?> expected9 = createTotalsSpreadSheetRow(employee2, hourType1, asList((HoursSpreadSheetRow) expected8));
        SpreadSheetRow<?> expected10 = new HeadSpreadSheetRow(employee3);
        SpreadSheetRow<?> expected11 = createSpreadSheetRow(project2, employee3, hourType1, findHours(hours, project2, employee3, hourType1));
        SpreadSheetRow<?> expected12 = createTotalsSpreadSheetRow(employee3, hourType1, asList((HoursSpreadSheetRow) expected11));

        assertEquals(12, actual.size());
        assertEquals(expected1, actual.get(0));
        assertEquals(expected2, actual.get(1));
        assertEquals(expected3, actual.get(2));
        assertEquals(expected4, actual.get(3));
        assertEquals(expected5, actual.get(4));
        assertEquals(expected6, actual.get(5));
        assertEquals(expected7, actual.get(6));
        assertEquals(expected8, actual.get(7));
        assertEquals(expected9, actual.get(8));
        assertEquals(expected10, actual.get(9));
        assertEquals(expected11, actual.get(10));
        assertEquals(expected12, actual.get(11));
    }

    @Test
    public void testBuildSpreadSheetRows_FilterHasProjects() throws NoSuchFieldException {
        List<Employee> project1Team = asList(employee1);
        List<Employee> project2Team = asList(employee1, employee2, employee3);
        HourType hourType1 = new HourType("1");
        HourType hourType2 = new HourType("2");
        Project project1 = buildProject(1L, project1Team, asList(hourType1, hourType2), ACTIVE, null);
        Project project2 = buildProject(2L, project2Team, asList(hourType1), ACTIVE, null);
        project1.addManager(employee1);
        project2.addManager(employee1);
        List<Hours> hours = getHours(asList(project1, project2));
        filter.setProjects(asList(project1, project2));
        setField(spreadSheet, "filter", filter);

        EasyMock.expect(hoursService.getHours(filter)).andReturn(hours);
        List<Project> managedProjects = asList(project1, project2);
        EasyMock.expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects).anyTimes();
        EasyMock.replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        EasyMock.verify(hoursService, projectService);

        SpreadSheetRow<?> expected1 = new HeadSpreadSheetRow(employee1);
        SpreadSheetRow<?> expected2 = createSpreadSheetRow(project1, employee1, hourType1, findHours(hours, project1, employee1, hourType1));
        SpreadSheetRow<?> expected3 = createSpreadSheetRow(project1, employee1, hourType2, findHours(hours, project1, employee1, hourType2));
        SpreadSheetRow<?> expected4 = createSpreadSheetRow(project2, employee1, hourType1, findHours(hours, project2, employee1, hourType1));
        SpreadSheetRow<?> expected5 = createTotalsSpreadSheetRow(employee1, hourType1, asList((HoursSpreadSheetRow) expected2, (HoursSpreadSheetRow) expected4));
        SpreadSheetRow<?> expected6 = createTotalsSpreadSheetRow(employee1, hourType2, asList((HoursSpreadSheetRow) expected3));
        SpreadSheetRow<?> expected7 = new HeadSpreadSheetRow(employee2);
        SpreadSheetRow<?> expected8 = createSpreadSheetRow(project2, employee2, hourType1, findHours(hours, project2, employee2, hourType1));
        SpreadSheetRow<?> expected9 = createTotalsSpreadSheetRow(employee2, hourType1, asList((HoursSpreadSheetRow) expected8));
        SpreadSheetRow<?> expected10 = new HeadSpreadSheetRow(employee3);
        SpreadSheetRow<?> expected11 = createSpreadSheetRow(project2, employee3, hourType1, findHours(hours, project2, employee3, hourType1));
        SpreadSheetRow<?> expected12 = createTotalsSpreadSheetRow(employee3, hourType1, asList((HoursSpreadSheetRow) expected11));

        assertEquals(12, actual.size());
        assertEquals(expected1, actual.get(0));
        assertEquals(expected2, actual.get(1));
        assertEquals(expected3, actual.get(2));
        assertEquals(expected4, actual.get(3));
        assertEquals(expected5, actual.get(4));
        assertEquals(expected6, actual.get(5));
        assertEquals(expected7, actual.get(6));
        assertEquals(expected8, actual.get(7));
        assertEquals(expected9, actual.get(8));
        assertEquals(expected10, actual.get(9));
        assertEquals(expected11, actual.get(10));
        assertEquals(expected12, actual.get(11));
    }

    @Test
    public void testBuildSpreadSheetRows_FilterHasDepartments() throws NoSuchFieldException {
        List<Employee> project1Team = asList(employee1);
        List<Employee> project2Team = asList(employee1, employee2, employee3);
        HourType hourType1 = new HourType("1");
        HourType hourType2 = new HourType("2");
        Project project1 = buildProject(1L, project1Team, asList(hourType1, hourType2), ACTIVE, null);
        project1.addManager(employee1);
        Project project2 = buildProject(2L, project2Team, asList(hourType1), ACTIVE, null);
        project2.addManager(employee1);
        List<Hours> hours = getHours(asList(project1, project2));
        filter.setDepartments(asList(department1, department2));
        setField(spreadSheet, "filter", filter);

        EasyMock.expect(hoursService.getHours(filter)).andReturn(hours);
        List<Project> managedProjects = asList(project1, project2);
        EasyMock.expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects).anyTimes();
        EasyMock.replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        EasyMock.verify(hoursService, projectService);

        SpreadSheetRow<?> expected1 = new HeadSpreadSheetRow(employee1);
        SpreadSheetRow<?> expected2 = createSpreadSheetRow(project1, employee1, hourType1, findHours(hours, project1, employee1, hourType1));
        SpreadSheetRow<?> expected3 = createSpreadSheetRow(project1, employee1, hourType2, findHours(hours, project1, employee1, hourType2));
        SpreadSheetRow<?> expected4 = createSpreadSheetRow(project2, employee1, hourType1, findHours(hours, project2, employee1, hourType1));
        SpreadSheetRow<?> expected5 = createTotalsSpreadSheetRow(employee1, hourType1, asList((HoursSpreadSheetRow) expected2, (HoursSpreadSheetRow) expected4));
        SpreadSheetRow<?> expected6 = createTotalsSpreadSheetRow(employee1, hourType2, asList((HoursSpreadSheetRow) expected3));
        SpreadSheetRow<?> expected7 = new HeadSpreadSheetRow(employee2);
        SpreadSheetRow<?> expected8 = createSpreadSheetRow(project2, employee2, hourType1, findHours(hours, project2, employee2, hourType1));
        SpreadSheetRow<?> expected9 = createTotalsSpreadSheetRow(employee2, hourType1, asList((HoursSpreadSheetRow) expected8));

        assertEquals(9, actual.size());
        assertEquals(expected1, actual.get(0));
        assertEquals(expected2, actual.get(1));
        assertEquals(expected3, actual.get(2));
        assertEquals(expected4, actual.get(3));
        assertEquals(expected5, actual.get(4));
        assertEquals(expected6, actual.get(5));
        assertEquals(expected7, actual.get(6));
        assertEquals(expected8, actual.get(7));
        assertEquals(expected9, actual.get(8));
    }

    @Test
    public void testBuildSpreadSheetRows_FilterHasEmployees() throws NoSuchFieldException {
        List<Employee> project1Team = asList(employee1);
        List<Employee> project2Team = asList(employee1, employee2, employee3);
        HourType hourType1 = new HourType("1");
        HourType hourType2 = new HourType("2");
        Project project1 = buildProject(1L, project1Team, asList(hourType1, hourType2), ACTIVE, null);
        Project project2 = buildProject(2L, project2Team, asList(hourType1), ACTIVE, null);
        project1.addManager(employee1);
        project2.addManager(employee1);
        List<Hours> hours = getHours(asList(project1, project2));
        filter.setEmployees(asList(employee1, employee2));
        setField(spreadSheet, "filter", filter);

        EasyMock.expect(hoursService.getHours(filter)).andReturn(hours);
        List<Project> managedProjects = asList(project1, project2);
        EasyMock.expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects).anyTimes();
        EasyMock.replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        EasyMock.verify(hoursService, projectService);

        SpreadSheetRow<?> expected1 = new HeadSpreadSheetRow(employee1);
        SpreadSheetRow<?> expected2 = createSpreadSheetRow(project1, employee1, hourType1, findHours(hours, project1, employee1, hourType1));
        SpreadSheetRow<?> expected3 = createSpreadSheetRow(project1, employee1, hourType2, findHours(hours, project1, employee1, hourType2));
        SpreadSheetRow<?> expected4 = createSpreadSheetRow(project2, employee1, hourType1, findHours(hours, project2, employee1, hourType1));
        SpreadSheetRow<?> expected5 = createTotalsSpreadSheetRow(employee1, hourType1, asList((HoursSpreadSheetRow) expected2, (HoursSpreadSheetRow) expected4));
        SpreadSheetRow<?> expected6 = createTotalsSpreadSheetRow(employee1, hourType2, asList((HoursSpreadSheetRow) expected3));
        SpreadSheetRow<?> expected7 = new HeadSpreadSheetRow(employee2);
        SpreadSheetRow<?> expected8 = createSpreadSheetRow(project2, employee2, hourType1, findHours(hours, project2, employee2, hourType1));
        SpreadSheetRow<?> expected9 = createTotalsSpreadSheetRow(employee2, hourType1, asList((HoursSpreadSheetRow) expected8));

        assertEquals(9, actual.size());
        assertEquals(expected1, actual.get(0));
        assertEquals(expected2, actual.get(1));
        assertEquals(expected3, actual.get(2));
        assertEquals(expected4, actual.get(3));
        assertEquals(expected5, actual.get(4));
        assertEquals(expected6, actual.get(5));
        assertEquals(expected7, actual.get(6));
        assertEquals(expected8, actual.get(7));
        assertEquals(expected9, actual.get(8));
    }

    @Test
    public void testBuildSpreadSheetRows_FilterHasHourTypes() throws NoSuchFieldException {
        List<Employee> project1Team = asList(employee1);
        List<Employee> project2Team = asList(employee1, employee2, employee3);
        HourType hourType1 = new HourType("1");
        HourType hourType2 = new HourType("2");
        Project project1 = buildProject(1L, project1Team, asList(hourType1, hourType2), ACTIVE, null);
        Project project2 = buildProject(2L, project2Team, asList(hourType1), ACTIVE, null);
        project1.addManager(employee1);
        project2.addManager(employee1);
        List<Hours> hours = getHours(asList(project1, project2));
        filter.setHourTypes(asList(hourType1));
        setField(spreadSheet, "filter", filter);

        EasyMock.expect(hoursService.getHours(filter)).andReturn(hours);
        List<Project> managedProjects = asList(project1, project2);
        EasyMock.expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects).anyTimes();
        EasyMock.replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        EasyMock.verify(hoursService, projectService);

        SpreadSheetRow<?> expected1 = new HeadSpreadSheetRow(employee1);
        SpreadSheetRow<?> expected2 = createSpreadSheetRow(project1, employee1, hourType1, findHours(hours, project1, employee1, hourType1));
        SpreadSheetRow<?> expected3 = createSpreadSheetRow(project2, employee1, hourType1, findHours(hours, project2, employee1, hourType1));
        SpreadSheetRow<?> expected4 = createTotalsSpreadSheetRow(employee1, hourType1, asList((HoursSpreadSheetRow) expected2, (HoursSpreadSheetRow) expected3));
        SpreadSheetRow<?> expected5 = new HeadSpreadSheetRow(employee2);
        SpreadSheetRow<?> expected6 = createSpreadSheetRow(project2, employee2, hourType1, findHours(hours, project2, employee2, hourType1));
        SpreadSheetRow<?> expected7 = createTotalsSpreadSheetRow(employee2, hourType1, asList((HoursSpreadSheetRow) expected6));
        SpreadSheetRow<?> expected8 = new HeadSpreadSheetRow(employee3);
        SpreadSheetRow<?> expected9 = createSpreadSheetRow(project2, employee3, hourType1, findHours(hours, project2, employee3, hourType1));
        SpreadSheetRow<?> expected10 = createTotalsSpreadSheetRow(employee3, hourType1, asList((HoursSpreadSheetRow) expected9));

        assertEquals(10, actual.size());
        assertEquals(expected1, actual.get(0));
        assertEquals(expected2, actual.get(1));
        assertEquals(expected3, actual.get(2));
        assertEquals(expected4, actual.get(3));
        assertEquals(expected5, actual.get(4));
        assertEquals(expected6, actual.get(5));
        assertEquals(expected7, actual.get(6));
        assertEquals(expected8, actual.get(7));
        assertEquals(expected9, actual.get(8));
        assertEquals(expected10, actual.get(9));
    }

    @Test
    public void testBuildSpreadSheetRows_FilterHasAll() throws NoSuchFieldException {
        HourType hourType1 = new HourType("1");
        HourType hourType2 = new HourType("2");
        Project project1 = buildProject(1L, asList(employee1), asList(hourType1, hourType2), ACTIVE, null);
        Project project2 = buildProject(2L, employees, asList(hourType1), ACTIVE, null);
        project1.addManager(employee1);
        project2.addManager(employee1);
        List<Hours> hours = getHours(asList(project1, project2));
        filter.setProjects(asList(project1, project2));
        filter.setDepartments(asList(department1));
        filter.setEmployees(asList(employee1));
        filter.setHourTypes(asList(hourType1));
        setField(spreadSheet, "filter", filter);

        EasyMock.expect(hoursService.getHours(filter)).andReturn(hours);
        List<Project> managedProjects = asList(project1, project2);
        EasyMock.expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects).anyTimes();
        EasyMock.replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        EasyMock.verify(hoursService, projectService);

        SpreadSheetRow<?> expected1 = new HeadSpreadSheetRow(employee1);
        SpreadSheetRow<?> expected2 = createSpreadSheetRow(project1, employee1, hourType1, findHours(hours, project1, employee1, hourType1));
        SpreadSheetRow<?> expected3 = createSpreadSheetRow(project2, employee1, hourType1, findHours(hours, project2, employee1, hourType1));
        SpreadSheetRow<?> expected4 = createTotalsSpreadSheetRow(employee1, hourType1, asList((HoursSpreadSheetRow) expected2, (HoursSpreadSheetRow) expected3));

        assertEquals(4, actual.size());
        assertEquals(expected1, actual.get(0));
        assertEquals(expected2, actual.get(1));
        assertEquals(expected3, actual.get(2));
        assertEquals(expected4, actual.get(3));
    }

    @Test
    public void testBuildSpreadSheetRows_EmployeesNotSorted() {
        List<Employee> notSortedEmployees = asList(employee2, employee1, employee3);
        HourType hourType = new HourType("1");
        Project project1 = buildProject(1L, notSortedEmployees, asList(hourType), ACTIVE, null);
        project1.addManager(employee1);
        List<Hours> hours = getHours(asList(project1));

        EasyMock.expect(hoursService.getHours(filter)).andReturn(hours);
        List<Project> managedProjects = asList(project1);
        EasyMock.expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects).anyTimes();
        EasyMock.replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        EasyMock.verify(hoursService, projectService);

        SpreadSheetRow<?> expected1 = new HeadSpreadSheetRow(employee1);
        SpreadSheetRow<?> expected2 = createSpreadSheetRow(project1, employee1, hourType, findHours(hours, project1, employee1, hourType));
        SpreadSheetRow<?> expected3 = createTotalsSpreadSheetRow(employee1, hourType, asList((HoursSpreadSheetRow) expected2));
        SpreadSheetRow<?> expected4 = new HeadSpreadSheetRow(employee2);
        SpreadSheetRow<?> expected5 = createSpreadSheetRow(project1, employee2, hourType, findHours(hours, project1, employee2, hourType));
        SpreadSheetRow<?> expected6 = createTotalsSpreadSheetRow(employee2, hourType, asList((HoursSpreadSheetRow) expected5));
        SpreadSheetRow<?> expected7 = new HeadSpreadSheetRow(employee3);
        SpreadSheetRow<?> expected8 = createSpreadSheetRow(project1, employee3, hourType, findHours(hours, project1, employee3, hourType));
        SpreadSheetRow<?> expected9 = createTotalsSpreadSheetRow(employee3, hourType, asList((HoursSpreadSheetRow) expected8));

        assertEquals(9, actual.size());
        assertEquals(expected1, actual.get(0));
        assertEquals(expected2, actual.get(1));
        assertEquals(expected3, actual.get(2));
        assertEquals(expected4, actual.get(3));
        assertEquals(expected5, actual.get(4));
        assertEquals(expected6, actual.get(5));
        assertEquals(expected7, actual.get(6));
        assertEquals(expected8, actual.get(7));
        assertEquals(expected9, actual.get(8));
    }

    @Test
    public void testBuildSpreadSheetRows_ProjectsFromEmployeesNotSorted() {
        List<Employee> projectTeam = asList(employee1);
        HourType hourType = new HourType("1");
        Project project1 = buildProject(1L, projectTeam, asList(hourType), ACTIVE, null);
        Project project2 = buildProject(2L, projectTeam, asList(hourType), ACTIVE, null);
        Project project3 = buildProject(3L, projectTeam, asList(hourType), ACTIVE, null);
        Project project4 = buildProject(4L, projectTeam, asList(hourType), ACTIVE, null);
        project1.addManager(employee1);
        project2.addManager(employee1);
        project3.addManager(employee1);
        project4.addManager(employee1);
        List<Project> projects = asList(project1, project2, project3, project4);
        List<Hours> hours = getHours(projects);

        EasyMock.expect(hoursService.getHours(filter)).andReturn(hours);
        List<Project> managedProjects = asList(project4, project1, project3, project2);
        EasyMock.expect(projectService.getEffortsProjects(filter)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(managedProjects)).andReturn(managedProjects);
        EasyMock.expect(projectService.fetchComplete(projects)).andReturn(projects);
        EasyMock.replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        EasyMock.verify(hoursService, projectService);

        SpreadSheetRow<?> expected1 = new HeadSpreadSheetRow(employee1);
        SpreadSheetRow<?> expected2 = createSpreadSheetRow(project1, employee1, hourType, findHours(hours, project1, employee1, hourType));
        SpreadSheetRow<?> expected3 = createSpreadSheetRow(project2, employee1, hourType, findHours(hours, project2, employee1, hourType));
        SpreadSheetRow<?> expected4 = createSpreadSheetRow(project3, employee1, hourType, findHours(hours, project3, employee1, hourType));
        SpreadSheetRow<?> expected5 = createSpreadSheetRow(project4, employee1, hourType, findHours(hours, project4, employee1, hourType));
        SpreadSheetRow<?> expected6 = createTotalsSpreadSheetRow(employee1, hourType,
                asList((HoursSpreadSheetRow) expected2, (HoursSpreadSheetRow) expected3, (HoursSpreadSheetRow) expected4,
                        (HoursSpreadSheetRow) expected5));

        assertEquals(6, actual.size());
        assertEquals(expected1, actual.get(0));
        assertEquals(expected2, actual.get(1));
        assertEquals(expected3, actual.get(2));
        assertEquals(expected4, actual.get(3));
        assertEquals(expected5, actual.get(4));
        assertEquals(expected6, actual.get(5));
    }

    @Test
    public void testCalculateKeysOfTotals() throws NoSuchFieldException {
        Date date = new Date(2017, 1, 1);
        Employee employee = new Employee("test");
        Project project = new Project();
        HourType hourType = new HourType("test");
        List<Hours> hours = asList(new Hours(project, date, employee, hourType));
        List<HoursSpreadSheetRow> hoursSpreadSheetRows = asList(
                new HoursSpreadSheetRow(project, employee, hourType, hours));
        List<Integer> expected = asList(employee.hashCode() + project.hashCode() + hourType.hashCode() + hoursSpreadSheetRows.get(0).getKey());
        TotalsSpreadSheetRow totalRow = new TotalsSpreadSheetRow(project, hourType, hoursSpreadSheetRows);
        totalRow.setEmployee(employee);
        List<SpreadSheetRow> spreadSheetRows = asList(
                new HeadSpreadSheetRow(employee),
                new HoursSpreadSheetRow(project, employee, hourType, hours),
                totalRow);
        setField(spreadSheet, "rows", spreadSheetRows);
        SpreadSheetRow spreadSheetRow = new HoursSpreadSheetRow(project, employee, hourType, hours);

        List<Integer> actual = spreadSheet.calculateKeysOfTotalsRows(spreadSheetRow);

        ListAssert.assertEquals(expected, actual);
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

    private List<Hours> findHours(List<Hours> hours, Project project, Employee employee, HourType hourType) {
        return hours.stream()
                .filter(hour -> project.equals(hour.getProject())
                        && employee.equals(hour.getEmployee())
                        && hourType.equals(hour.getType()))
                .collect(Collectors.toList());
    }

    private SpreadSheetRow<?> createTotalsSpreadSheetRow(Employee employee, HourType hourType, List<HoursSpreadSheetRow> hoursRows) {
        return new TotalsSpreadSheetRow(employee, hourType, hoursRows);
    }

    private SpreadSheetRow<?> createSpreadSheetRow(Project project, Employee employee, HourType hourType, List<Hours> hours) {
        return new HoursSpreadSheetRow(project, employee, hourType, hours);
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

}
