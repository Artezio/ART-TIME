package com.artezio.arttime.web.spread_sheet;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

import com.google.common.collect.Sets;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.services.EmployeeService;

import junitx.framework.ListAssert;

@RunWith(EasyMockRunner.class)
public class SpreadSheetTest {
    private SpreadSheet spreadSheet;
    private Filter filter;
    @Mock
    private EmployeeService employeeService;
    private List<Employee> employees;
    private String department1 = "Department1";
    private String department2 = "Department2";
    private String department3 = "Department3";
    private Employee employee1;
    private Employee employee2;
    private Employee employee3;

    @Before
    public void setUp() throws NoSuchFieldException, ParseException {
        filter = new Filter();
        createEmployees();
        spreadSheet = new SpreadSheet() {
            @Override
            protected List<SpreadSheetRow<?>> buildSpreadSheetRows() { return null; }

            @Override
            protected List<Integer> calculateKeysOfTotalsRows(SpreadSheetRow<?> updatedRow) { return null; }
        };
        setField(spreadSheet, "filter", filter);
        setField(spreadSheet, "availableEmployees", employees);
    }

    private void createEmployees() {
        employee1 = new Employee("1", "1", "1","1", department1);
        employee2 = new Employee("2", "2", "2", "2", department2);
        employee3 = new Employee("3", "3", "3","3", department3);
        employees = Arrays.asList(employee1, employee2, employee3);
    }

    @Test
    public void testGetFilteredTeamMembersUnion_FilterEmpty_TeamMembers() {
        List<Employee> teamMembers = emptyList();
        List<Employee> teamMembersWithHours = employees;

        List<Employee> actual = spreadSheet.getFilteredTeamMembersUnion(teamMembers, teamMembersWithHours);

        ListAssert.assertEquals(teamMembersWithHours, actual);
    }

    @Test
    public void testGetFilteredTeamMembersUnion_FilterEmpty_TeamMembersWithHoursEmpty() {
        List<Employee> teamMembers = employees;
        List<Employee> teamMembersWithHours = emptyList();

        List<Employee> actual = spreadSheet.getFilteredTeamMembersUnion(teamMembers, teamMembersWithHours);

        ListAssert.assertEquals(teamMembers, actual);
    }

    @Test
    public void testGetFilteredTeamMembersUnion_FilterEmpty_TeamMembersAndTeamMembersWithHoursHaveDiffEmployees() {
        List<Employee> teamMembers = asList(employee1, employee2);
        List<Employee> teamMembersWithHours = asList(employee3);
        List<Employee> expected = new LinkedList<>(teamMembers);
        expected.addAll(teamMembersWithHours);

        List<Employee> actual = spreadSheet.getFilteredTeamMembersUnion(teamMembers, teamMembersWithHours);

        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetFilteredTeamMembersUnion_FilterEmpty_TeamMembersAndTeamMembersWithHoursHaveSameEmployees() {
        List<Employee> teamMembers = asList(employee1, employee2);
        List<Employee> teamMembersWithHours = asList(employee2, employee3);
        List<Employee> expected = new LinkedList<>(teamMembers);
        expected.add(teamMembersWithHours.get(1));

        List<Employee> actual = spreadSheet.getFilteredTeamMembersUnion(teamMembers, teamMembersWithHours);

        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetFilteredTeamMembersUnion_TeamMembersAndTeamMembersUnionWithHoursContainEmployeeFromFilter() {
        List<Employee> teamMembers = asList(employee1, employee2);
        List<Employee> teamMembersWithHours = asList(employee2, employee3);
        List<Employee> teamMembersUnion = new LinkedList<>(teamMembers);
        teamMembersUnion.add(teamMembersWithHours.get(1));
        filter.getEmployees().add(employee2);
        List<Employee> expected = asList(employee2);

        List<Employee> actual = spreadSheet.getFilteredTeamMembersUnion(teamMembers, teamMembersWithHours);

        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetFilteredTeamMembersUnion_TeamMembersAndTeamMembersUnionWithHoursNotContainEmployeeFromFilter() {
        List<Employee> teamMembers = asList(employee1);
        List<Employee> teamMembersWithHours = asList(employee2);
        List<Employee> teamMembersUnion = new LinkedList<>(teamMembers);
        teamMembersUnion.addAll(teamMembersWithHours);
        filter.getEmployees().add(employee3);

        List<Employee> actual = spreadSheet.getFilteredTeamMembersUnion(teamMembers, teamMembersWithHours);

        assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetFilteredHourTypes_IndexedHourTypesNotEmpty_FilterEmpty() {
        HourType hourType1 = new HourType("1");
        HourType hourType2 = new HourType("2");
        Project project = new Project();
        project.setTeam(singletonList(employee1));
        project.setAccountableHours(asList(hourType1, hourType2));
        SpreadSheet.HoursIndexedBundle indexedHours = EasyMock.mock(SpreadSheet.HoursIndexedBundle.class);

        expect(indexedHours.getHourTypes(project, employee1)).andReturn(asList(hourType1, hourType2));
        EasyMock.replay(indexedHours);

        List<HourType> expected = asList(hourType1, hourType2);
        List<HourType> actual = spreadSheet.getFilteredHourTypesUnion(employee1, project, indexedHours);
        
        EasyMock.verify(indexedHours);
        ListAssert.assertEquals(expected, actual);
    }

    @Test 
    public void testGetFilteredHourTypes_IndexedHourTypesNotEmpty_FilterNotEmpty() {
        HourType hourType1 = new HourType("1");
        HourType hourType2 = new HourType("2");
        filter.setHourTypes(asList(hourType2));
        Project project = new Project();
        project.setTeam(singletonList(employee1));
        project.setAccountableHours(asList(hourType1, hourType2));
        SpreadSheet.HoursIndexedBundle indexedHours = EasyMock.mock(SpreadSheet.HoursIndexedBundle.class);

        expect(indexedHours.getHourTypes(project, employee1)).andReturn(asList(hourType1, hourType2));
        EasyMock.replay(indexedHours);

        List<HourType> expected = asList(hourType2);
        List<HourType> actual = spreadSheet.getFilteredHourTypesUnion(employee1, project, indexedHours);
        
        EasyMock.verify(indexedHours);
        ListAssert.assertEquals(expected, actual);
    }
    
    @Test 
    public void testGetFilteredHourTypes_IndexedHoursEmpty_FilterEmpty() {
        HourType hourType1 = new HourType("1");
        HourType hourType2 = new HourType("2");
        Project project = new Project();
        project.setTeam(singletonList(employee1));
        project.setAccountableHours(asList(hourType1, hourType2));
        SpreadSheet.HoursIndexedBundle indexedHours = EasyMock.mock(SpreadSheet.HoursIndexedBundle.class);

        expect(indexedHours.getHourTypes(project, employee1)).andReturn(emptyList());
        EasyMock.replay(indexedHours);

        List<HourType> expected = asList(hourType1, hourType2);
        List<HourType> actual = spreadSheet.getFilteredHourTypesUnion(employee1, project, indexedHours);
        
        EasyMock.verify(indexedHours);
        ListAssert.assertEquals(expected, actual);
    }

    @Test 
    public void testGetFilteredHourTypes_IndexedHoursEmpty_FilterNotEmpty() {
        HourType hourType1 = new HourType("1");
        HourType hourType2 = new HourType("2");
        filter.setHourTypes(asList(hourType2));
        Project project = new Project();
        project.setTeam(singletonList(employee1));
        project.setAccountableHours(asList(hourType1, hourType2));
        SpreadSheet.HoursIndexedBundle indexedHours = EasyMock.mock(SpreadSheet.HoursIndexedBundle.class);
        
        expect(indexedHours.getHourTypes(project, employee1)).andReturn(emptyList());
        EasyMock.replay(indexedHours);
        List<HourType> expected = asList(hourType2);
        
        List<HourType> actual = spreadSheet.getFilteredHourTypesUnion(employee1, project, indexedHours);
        
        EasyMock.verify(indexedHours);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGetValue_FromHoursSpreadSheetRow() {
        Project prj = new Project();
        HourType hourType = new HourType();
        Date date = new Date();
        List<Hours> hours = buildHourList(prj, singletonList(employee1), hourType, date);
        HoursSpreadSheetRow hoursSpreadSheetRow = new HoursSpreadSheetRow(prj, employee1, hourType, hours);
        Hours expected = hours.get(0);

        Object actual = spreadSheet.getValue(hoursSpreadSheetRow, date);
        
        assertEquals(Hours.class, actual.getClass());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetValue_FromTotalsSpreadSheetRow() {
        Project prj = new Project();
        HourType hourType = new HourType();
        Date date = new Date();
        List<Hours> hours = buildHourList(prj, singletonList(employee1), hourType, date);
        HoursSpreadSheetRow hoursSpreadSheetRow = new HoursSpreadSheetRow(prj, employee1, hourType, hours);
        TotalsSpreadSheetRow totalsSpreadSheetRow = new TotalsSpreadSheetRow(prj, hourType, singletonList(hoursSpreadSheetRow));

        hours.get(0).setQuantity(new BigDecimal(10));
        BigDecimal expected = hours.get(0).getQuantity();
        Object actual = spreadSheet.getValue(totalsSpreadSheetRow, date);
        assertEquals(BigDecimal.class, actual.getClass());
        assertEquals(expected, actual);
    }
    
    @Test
    public void testGetEmployeesUnion_BothEmployeeListsEmpty() {
        List<Employee> employees1 = emptyList();
        List<Employee> employees2 = emptyList();

        List<Employee> actual = spreadSheet.getFilteredTeamMembersUnion(employees1, employees2);

        assertTrue(actual.isEmpty());
    }

    @Test
    public void testGetEmployeesUnion_EmployeeList1Empty() {
        List<Employee> employees1 = emptyList();
        List<Employee> employees2 = asList(employee1, employee2);

        List<Employee> actual = spreadSheet.getFilteredTeamMembersUnion(employees1, employees2);

        assertEquals(employees2, actual);
    }

    @Test
    public void testGetEmployeesUnion_EmployeeList2Empty() {
        List<Employee> employees1 = asList(employee1, employee2);
        List<Employee> employees2 = emptyList();

        List<Employee> actual = spreadSheet.getFilteredTeamMembersUnion(employees1, employees2);

        assertEquals(employees1, actual);
    }

    @Test
    public void testGetEmployeesUnion_EmployeeListHaveDiffEmployees() {
        List<Employee> employees1 = asList(employee1, employee2);
        List<Employee> employees2 = asList(employee3);
        List<Employee> expected = new LinkedList<>(employees1);
        expected.addAll(employees2);

        List<Employee> actual = spreadSheet.getFilteredTeamMembersUnion(employees1, employees2);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetEmployeesUnion_notAllEmployeesAreAvailable() throws NoSuchFieldException {
        List<Employee> employees1 = asList(employee1, employee2);
        List<Employee> employees2 = asList(employee2, employee3);
        List<Employee> expected = asList(employee2);
        List<Employee> availableEmployees = asList(employee2);
        setField(spreadSheet, "availableEmployees", availableEmployees);
        replay(employeeService);

        List<Employee> actual = spreadSheet.getFilteredTeamMembersUnion(employees1, employees2);

        verify(employeeService);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetEmployeesUnion_EmployeeListHaveSameEmployee() {
        List<Employee> employees1 = asList(employee1, employee2);
        List<Employee> employees2 = asList(employee2, employee3);
        List<Employee> expected = new LinkedList<>(employees1);
        expected.add(employees2.get(1));

        List<Employee> actual = spreadSheet.getFilteredTeamMembersUnion(employees1, employees2);

        assertEquals(expected, actual);
    }

    private List<Hours> buildHourList(Project project, List<Employee> employees, HourType hourType, Date date) {
        List<Hours> result = new LinkedList<>();
        employees.forEach(employee -> result.add(new Hours(project, date, employee, hourType)));
        return result;
    }

}
