package com.artezio.arttime.web.spread_sheet;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import junitx.framework.ListAssert;
import org.junit.Test;

import java.util.*;

import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.*;

public class HoursIndexedBundleTest {
    private SpreadSheet.HoursIndexedBundle bundle;

    @Test
    public void testFindHours_ByProject() {
        Project project1 = createProject(1L);
        Project project2 = createProject(2L);
        HourType type1 = new HourType("type1");
        HourType type2 = new HourType("type2");
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        Hours hour1 = createHours(project1, employee1, type1);
        Hours hour2 = createHours(project1, employee2, type2);
        Hours hour3 = createHours(project2, employee2, type2);
        bundle = createHoursIndexedBundle(Arrays.asList(hour1, hour2, hour3));

        List<Hours> actual = bundle.findHours(project1);

        assertEquals(2, actual.size());
        ListAssert.assertEquals(Arrays.asList(hour1, hour2), actual);
    }

    @Test
    public void testFindHours_ByProject_NoHours() {
        Project project1 = createProject(1L);
        Project project2 = createProject(2L);
        HourType type = new HourType("type");
        Employee employee = new Employee("employee1");
        Hours hour = createHours(project1, employee, type);
        bundle = createHoursIndexedBundle(Arrays.asList(hour));

        List<Hours> actual = bundle.findHours(project2);

        assertTrue(actual.isEmpty());
    }

    @Test
    public void testFindHours_ByProjectAndEmployee() {
        Project project1 = createProject(1L);
        Project project2 = createProject(2L);
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        HourType type1 = new HourType("regular");
        HourType type2 = new HourType("overtime");
        Hours hours1 = createHours(project1, employee1, type1);
        Hours hours2 = createHours(project2, employee1, type2);
        Hours hours3 = createHours(project1, employee2, type1);
        bundle = createHoursIndexedBundle(Arrays.asList(hours1, hours2, hours3));

        List<Hours> actual = bundle.findHours(project1, employee1);

        ListAssert.assertEquals(Arrays.asList(hours1), actual);
    }

    @Test
    public void testFindHours_ByProjectAndEmployee_NoHours() {
        Project project1 = createProject(1L);
        Project project2 = createProject(2L);
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        Employee employee3 = new Employee("employee3");
        HourType type1 = new HourType("regular");
        Hours hours1 = createHours(project1, employee1, type1);
        Hours hours2 = createHours(project1, employee2, type1);
        bundle = createHoursIndexedBundle(Arrays.asList(hours1, hours2));

        assertTrue(bundle.findHours(project2, employee1).isEmpty());
        assertTrue(bundle.findHours(project1, employee3).isEmpty());
    }

    private Hours createHours(Project project, Employee employee, HourType type) {
        return new Hours(project, null, employee, type);
    }

    private Project createProject(Long id) {
        Project project = new Project();
        try {
            setField(project, "id", id);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return project;
    }

    @Test
    public void testContainsHours_ByProjectAndEmployee_ifContains() {
        Project project = createProject(1L);
        Employee employee = new Employee("employee");
        HourType hourType = new HourType();
        Hours hour = new Hours(project, new Date(), employee, hourType);
        List<Hours> hours = Arrays.asList(hour);
        bundle = createHoursIndexedBundle(hours);

        boolean actual = bundle.containsHours(project, employee);

        assertTrue(actual);
    }

    @Test
    public void testContainsHours_ByProjectAndEmployee_ifContainsOnlyProject() {
        Project project = createProject(1L);
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        HourType hourType = new HourType();
        Hours hour = new Hours(project, new Date(), employee1, hourType);
        List<Hours> hours = Arrays.asList(hour);
        bundle = createHoursIndexedBundle(hours);

        boolean actual = bundle.containsHours(project, employee2);

        assertFalse(actual);
    }

    @Test
    public void testContainsHours_ByProjectAndEmployee_ifContainsOnlyEmployee() {
        Project project1 = createProject(1L);
        Project project2 = createProject(2L);
        Employee employee = new Employee("employee");
        HourType hourType = new HourType();
        Hours hour = new Hours(project1, new Date(), employee, hourType);
        List<Hours> hours = Arrays.asList(hour);
        bundle = createHoursIndexedBundle(hours);

        boolean actual = bundle.containsHours(project2, employee);

        assertFalse(actual);
    }

    @Test
    public void testContainsHours_ByProjectAndEmployee_ifNotContainsProjectAndEmployee() {
        Project project1 = createProject(1L);
        Project project2 = createProject(2L);
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        HourType hourType = new HourType();
        Hours hour = new Hours(project1, new Date(), employee1, hourType);
        List<Hours> hours = Arrays.asList(hour);
        bundle = createHoursIndexedBundle(hours);

        boolean actual = bundle.containsHours(project2, employee2);

        assertFalse(actual);
    }

    @Test
    public void testContainsHours_ByEmployee_ifContains() {
        Project project = createProject(1L);
        Employee employee = new Employee("employee");
        HourType hourType = new HourType();
        Hours hour = new Hours(project, new Date(), employee, hourType);
        List<Hours> hours = Arrays.asList(hour);
        bundle = createHoursIndexedBundle(hours);

        boolean actual = bundle.containsHours(employee);

        assertTrue(actual);
    }

    @Test
    public void testContainsHours_ByEmployee_ifNotContains() {
        Project project = createProject(1L);
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        HourType hourType = new HourType();
        Hours hour = new Hours(project, new Date(), employee1, hourType);
        List<Hours> hours = Arrays.asList(hour);
        bundle = createHoursIndexedBundle(hours);

        boolean actual = bundle.containsHours(employee2);

        assertFalse(actual);
    }

    @Test
    public void testGetHourTypes() {
        Project project = createProject(1L);
        Employee employee = new Employee("employee");
        HourType hourType = new HourType();
        Hours hour = new Hours(project, new Date(), employee, hourType);
        List<Hours> hours = Arrays.asList(hour);
        bundle = createHoursIndexedBundle(hours);

        List<HourType> expected = Arrays.asList(hourType);
        Collection<HourType> actual = bundle.getHourTypes(project, employee);

        ListAssert.assertEquals(expected, new ArrayList<>(actual));
    }

    @Test
    public void testGetHourTypes_ifEmpty() {
        Project project = createProject(1L);
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        HourType hourType = new HourType();
        Hours hour = new Hours(project, new Date(), employee1, hourType);
        List<Hours> hours = Arrays.asList(hour);
        bundle = createHoursIndexedBundle(hours);

        List<HourType> expected = new ArrayList<>();
        Collection<HourType> actual = bundle.getHourTypes(project, employee2);

        ListAssert.assertEquals(expected, new ArrayList<>(actual));
    }

    public SpreadSheet.HoursIndexedBundle createHoursIndexedBundle(List<Hours> hours) {
        return new SpreadSheet() {
            @Override
            protected List<SpreadSheetRow<?>> buildSpreadSheetRows() {
                return null;
            }

            @Override
            protected List<Integer> calculateKeysOfTotalsRows(SpreadSheetRow<?> updatedRow) {
                return null;
            }
        }.new HoursIndexedBundle(hours);
    }
}
