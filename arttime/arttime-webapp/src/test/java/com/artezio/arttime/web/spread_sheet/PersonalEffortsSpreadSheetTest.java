package com.artezio.arttime.web.spread_sheet;

import com.artezio.arttime.datamodel.*;
import static com.artezio.arttime.datamodel.Project.Status.ACTIVE;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;
import com.artezio.arttime.services.HoursService;
import com.artezio.arttime.services.ProjectService;
import com.artezio.arttime.test.utils.CalendarUtils;
import com.artezio.arttime.web.criteria.RangePeriodSelector;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.artezio.arttime.datamodel.Project.Status.CLOSED;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.stream.Collectors;
import junitx.framework.ListAssert;
import static junitx.util.PrivateAccessor.setField;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyMockRunner.class)
public class PersonalEffortsSpreadSheetTest {

    private PersonalEffortsSpreadSheet spreadSheet;
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private Filter filter;
    @Mock
    private HoursService hoursService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private ProjectService projectService;

    @Before
    public void setUp() throws ParseException, NoSuchFieldException {
        filter = createFilter();
        expect(employeeService.getLoggedEmployee()).andReturn(Optional.of(filter.getEmployees().get(0)));
        replay(employeeService);
        spreadSheet = new PersonalEffortsSpreadSheet(hoursService, projectService, employeeService, filter);
        verify(employeeService);
    }

    @Test
    public void testBuildSpreadSheetRows() throws NoSuchFieldException {
        Employee employee = filter.getEmployees().get(0);
        Project project1 = filter.getProjects().get(0);
        Project project2 = filter.getProjects().get(1);
        HourType hourType1 = filter.getHourTypes().get(0);
        HourType hourType2 = filter.getHourTypes().get(1);
        List<Hours> hours = getHours(asList(project1, project2));
        setField(spreadSheet, "rows", new ArrayList<>());
        expect(hoursService.getTimesheetHours(filter.getPeriod())).andReturn(hours);
        expect(projectService.getMyProjects()).andReturn(asList(project1, project2));

        EasyMock.replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        EasyMock.verify(hoursService, projectService);
        assertEquals(5, actual.size());
        assertRowMatch(actual.get(0), project1, employee, hourType1, findHours(hours, project1, employee, hourType1));
        assertRowMatch(actual.get(1), project1, employee, hourType2, findHours(hours, project1, employee, hourType2));
        assertRowMatch(actual.get(2), project2, employee, hourType1, findHours(hours, project2, employee, hourType1));
        assertRowMatch(actual.get(3), null, employee, hourType1, emptyList());
        assertRowMatch(actual.get(4), null, employee, hourType2, emptyList());
    }

    @Test
    public void testBuildSpreadSheetRows_inactiveProjects_withHours() throws NoSuchFieldException {
        Employee employee = filter.getEmployees().get(0);
        Project project1 = filter.getProjects().get(0);
        project1.setStatus(Project.Status.CLOSED);
        Project project2 = filter.getProjects().get(1);
        project2.setStatus(Project.Status.CLOSED);
        HourType hourType1 = filter.getHourTypes().get(0);
        HourType hourType2 = filter.getHourTypes().get(1);
        Hours hour1 = new Hours(project1, filter.getPeriod().getStart(), employee, hourType1);
        List<Hours> hours = asList(hour1);
        setField(spreadSheet, "rows", new ArrayList<>());
        expect(hoursService.getTimesheetHours(filter.getPeriod())).andReturn(hours);
        expect(projectService.getMyProjects()).andReturn(asList(project1));
        EasyMock.replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();
        EasyMock.verify(hoursService, projectService);
        assertEquals(4, actual.size());
        assertRowMatch(actual.get(0), project1, employee, hourType1, hours);
        assertRowMatch(actual.get(1), project1, employee, hourType2, emptyList());
        assertRowMatch(actual.get(2), null, employee, hourType1, emptyList());
        assertRowMatch(actual.get(3), null, employee, hourType2, emptyList());
    }

    @Test
    public void testBuildSpreadSheetRows_ProjectsFromEmployeeNotSorted() {
        Employee employee = filter.getEmployees().get(0);
        HourType hourType = filter.getHourTypes().get(0);
        Project project1 = buildProject(1L, asList(employee), asList(hourType), ACTIVE, null);
        Project project2 = buildProject(2L, asList(employee), asList(hourType), ACTIVE, null);
        Project project3 = buildProject(3L, asList(employee), asList(hourType), ACTIVE, null);
        Project project4 = buildProject(4L, asList(employee), asList(hourType), ACTIVE, null);
        Project project5 = buildProject(5L, asList(employee), asList(hourType), ACTIVE, null);
        List<Hours> hours = getHours(asList(project1, project2, project3, project4));
        hours.add(new Hours(project5, filter.getPeriod().getStart(), employee, hourType));
        expect(hoursService.getTimesheetHours(filter.getPeriod())).andReturn(hours);
        expect(projectService.getMyProjects()).andReturn(asList(project2, project4, project1, project3));
        EasyMock.replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        EasyMock.verify(hoursService, projectService);
        assertEquals(6, actual.size());
        assertRowMatch(actual.get(0), project1, employee, hourType, findHours(hours, project1, employee, hourType));
        assertRowMatch(actual.get(1), project2, employee, hourType, findHours(hours, project2, employee, hourType));
        assertRowMatch(actual.get(2), project3, employee, hourType, findHours(hours, project3, employee, hourType));
        assertRowMatch(actual.get(3), project4, employee, hourType, findHours(hours, project4, employee, hourType));
        assertRowMatch(actual.get(4), project5, employee, hourType, findHours(hours, project5, employee, hourType));
        assertRowMatch(actual.get(5), null, employee, hourType, emptyList());
    }

    @Test
    public void testBuildSpreadSheetRows_ProjectsWithSubprojectsFromEmployeeNotSorted() {
        Employee employee = filter.getEmployees().get(0);
        HourType hourType = filter.getHourTypes().get(0);
        Project project1 = buildProject(1L, asList(employee), asList(hourType), ACTIVE, null);
        Project project4 = buildProject(4L, asList(employee), asList(hourType), ACTIVE, project1);
        Project project2 = buildProject(2L, asList(employee), asList(hourType), ACTIVE, project1);
        Project project3 = buildProject(3L, asList(employee), asList(hourType), ACTIVE, project1);
        List<Hours> hours = getHours(asList(project1, project2, project3, project4));
        EasyMock.expect(hoursService.getTimesheetHours(filter.getPeriod())).andReturn(hours);
        EasyMock.expect(projectService.getMyProjects()).andReturn(asList(project1,project4, project2, project3));
        EasyMock.replay(hoursService, projectService);

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        EasyMock.verify(hoursService, projectService);
        assertEquals(5, actual.size());
        assertRowMatch(actual.get(0), project1, employee, hourType, findHours(hours, project1, employee, hourType));
        assertRowMatch(actual.get(1), project2, employee, hourType, findHours(hours, project2, employee, hourType));
        assertRowMatch(actual.get(2), project3, employee, hourType, findHours(hours, project3, employee, hourType));
        assertRowMatch(actual.get(3), project4, employee, hourType, findHours(hours, project4, employee, hourType));
        assertRowMatch(actual.get(4), null, employee, hourType, emptyList());
    }

    @Test
    public void testBuildSpreadSheetRows_NoProjects() throws NoSuchFieldException {
        spreadSheet.getFilter().setProjects(emptyList());
        Project prj1 = new Project();
        Project prj2 = new Project();
        prj1.setCode("prj1");
        prj2.setCode("prj2");
        setField(spreadSheet, "rows", new ArrayList<>());

        List<SpreadSheetRow<?>> actual = spreadSheet.buildSpreadSheetRows();

        assertTrue(actual.isEmpty());
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

    @Test
    public void testGetValue_FromHoursSpreadSheetRow_EmployeeIsTeamMember() {
        Project prj = new Project();
        Employee emp = new Employee();
        prj.getTeam().add(emp);
        HourType hourType = new HourType();
        Date date = new Date();
        List<Hours> hours = asList(new Hours(prj, date, emp, hourType));
        HoursSpreadSheetRow hoursSpreadSheetRow = new HoursSpreadSheetRow(prj, emp, hourType, hours);
        Hours expected = hours.get(0);

        Object actual = spreadSheet.getValue(hoursSpreadSheetRow, date);
        
        assertEquals(Hours.class, actual.getClass());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetValue_FromHoursSpreadSheetRow_EmployeeIsNoLongerTeamMember_HoursExist() {
        Project prj = new Project();
        Employee emp = new Employee();
        HourType hourType = new HourType();
        Date date = new Date();
        List<Hours> hours = asList(new Hours(prj, date, emp, hourType));
        hours.get(0).setQuantity(new BigDecimal(10));
        HoursSpreadSheetRow hoursSpreadSheetRow = new HoursSpreadSheetRow(prj, emp, hourType, hours);
        Hours expected = hours.get(0);

        Object actual = spreadSheet.getValue(hoursSpreadSheetRow, date);
        
        assertEquals(Hours.class, actual.getClass());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetValue_FromHoursSpreadSheetRow_EmployeeIsNoLongerTeamMember_HoursNotExist() {
        Project prj = new Project();
        Employee emp = new Employee();
        HourType hourType = new HourType();
        Date date = new Date();
        List<Hours> hours = asList(new Hours(prj, date, emp, hourType));
        HoursSpreadSheetRow hoursSpreadSheetRow = new HoursSpreadSheetRow(prj, emp, hourType, hours);

        Object actual = spreadSheet.getValue(hoursSpreadSheetRow, date);
        
        assertNull(actual);
    }

    @Test
    public void testGetValue_FromTotalsSpreadSheetRow() {
        Project prj = new Project();
        Employee emp = new Employee();
        HourType hourType = new HourType();
        Date date = new Date();
        List<Hours> hours = asList(new Hours(prj, date, emp, hourType));
        HoursSpreadSheetRow hoursSpreadSheetRow = new HoursSpreadSheetRow(prj, emp, hourType, hours);
        TotalsSpreadSheetRow totalsSpreadSheetRow = new TotalsSpreadSheetRow(prj, hourType, asList(hoursSpreadSheetRow));

        hours.get(0).setQuantity(new BigDecimal(10));
        BigDecimal expected = hours.get(0).getQuantity();
        Object actual = spreadSheet.getValue(totalsSpreadSheetRow, date);
        assertEquals(BigDecimal.class, actual.getClass());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetActiveOrReportedProjects() throws NoSuchFieldException {
        Employee employee = new Employee("uname");
        HourType hourType = new HourType("type");
        Project project1 = createProject("pr1", asList(hourType), asList(employee));
        setField(project1, "id", 1L);
        project1.setStatus(CLOSED);
        Hours hours = new Hours(project1, CalendarUtils.getOffsetDate(0), employee, hourType);
        SpreadSheet.HoursIndexedBundle indexedBundle = spreadSheet.new HoursIndexedBundle(asList(hours));
        Project project2 = createProject("pr2", asList(hourType), asList(employee));
        project2.setStatus(ACTIVE);
        setField(project2, "id", 2L);

        List<Project> actual = spreadSheet.getActiveOrReportedProjects(asList(project2), indexedBundle);

        assertTrue(actual.contains(project1));
        assertTrue(actual.contains(project2));
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
        Employee employee1 = new Employee("iivanov", "ivan", "ivanov", "ivanov@mail.ru");
        employee1.setDepartment("Minsk");
        List<Employee> employees = asList(employee1);
        Project project1 = createProject("project1", asList(hourType1, hourType2), asList(employee1));
        setField(project1, "id", 1L);
        Project project2 = createProject("project2", asList(hourType1), asList(employee1));
        setField(project2, "id", 2L);
        List<Project> projects = asList(project1, project2);
        Filter filter = new Filter();
        filter.setEmployees(employees);
        filter.setProjects(projects);
        filter.setHourTypes(hourTypes);
        filter.setDepartments(asList("Minsk"));
        filter.setRangePeriodSelector(new RangePeriodSelector(period));
        return filter;
    }

    private Project createProject(String code, List<HourType> hourTypes, List<Employee> employees) {
        Project project = new Project();
        project.setAccountableHours(hourTypes);
        project.setCode(code);
        project.getTeam().addAll(employees);
        return project;
    }

    private List<Hours> findHours(List<Hours> hours, Project project, Employee employee, HourType hourType) {
        List<Hours> result = new ArrayList<>();
        hours.forEach(hour -> {
            if (project.equals(hour.getProject())) {
                if (employee.equals(hour.getEmployee())) {
                    if (hourType.equals(hour.getType())) {
                        result.add(hour);
                    }
                }
            }
        });
        return result;
    }

    private void assertRowMatch(SpreadSheetRow row, Project project, Employee employee, HourType type, List<Hours> hours) {
        assertEquals(project, row.getProject());
        assertEquals(employee, row.getEmployee());
        assertEquals(type, row.getHourType());
        Map<Date, Hours> hoursMap = hours.stream()
                .collect(Collectors.toMap(Hours::getDate, h -> h));
        assertEquals(hoursMap, row.getValuesMap());
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
