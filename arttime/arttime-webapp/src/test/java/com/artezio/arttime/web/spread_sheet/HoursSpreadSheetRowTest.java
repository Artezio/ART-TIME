package com.artezio.arttime.web.spread_sheet;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.web.EffortsGrouping;
import com.ibm.icu.text.SimpleDateFormat;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class HoursSpreadSheetRowTest {

    private HoursSpreadSheetRow row;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    @Test
    public void testGet_ifContainsValue() throws Exception {
        Project project = new Project();
        Employee employee = new Employee();
        HourType hourType = new HourType();
        Date date1 = sdf.parse("1-01-2014");
        Date date2 = sdf.parse("2-01-2014");
        Hours hour1 = new Hours(project, date1, employee, hourType);
        Hours hour2 = new Hours(project, date2, employee, hourType);
        List<Hours> hours = Arrays.asList(hour1, hour2);
        row = new HoursSpreadSheetRow(project, employee, hourType, hours);
        Hours actual = row.get(date1);

        assertSame(hour1, actual);
    }

    @Test
    public void testGet_ifNotContainsValue() throws Exception {
        Employee employee = new Employee();
        HourType hourType = new HourType();
        Date date = sdf.parse("1-01-2014");
        Project project = new Project();
        row = new HoursSpreadSheetRow(project, employee, hourType, new ArrayList<>());

        Hours actual = row.get(date);

        assertNull(actual.getQuantity());
    }

    @Test
    public void testGetRowTotal() throws Exception {
        Project project = new Project();
        Employee employee = new Employee();
        HourType hourType = new HourType();
        Date date1 = sdf.parse("1-01-2014");
        Date date2 = sdf.parse("2-01-2014");
        Date date3 = sdf.parse("3-01-2014");
        Date date4 = sdf.parse("4-01-2014");
        Hours hour1 = new Hours(project, date1, employee, hourType);
        hour1.setQuantity(BigDecimal.ONE);
        Hours hour2 = new Hours(project, date2, employee, hourType);
        hour2.setQuantity(BigDecimal.TEN);
        List<Hours> hours = Arrays.asList(hour1, hour2);
        row = new HoursSpreadSheetRow(project, employee, hourType, hours);
        row.getValuesMap().put(date3, new Hours(project, date3, employee, hourType));
        row.getValuesMap().put(date4, null);

        BigDecimal actual = row.getRowTotal();

        assertEquals(new BigDecimal("11"), actual);
    }

    @Test
    public void testGetFirstColValue_ifGroupingByEmployees() {
        Project project = new Project();
        project.setCode("project");
        HourType hourType = new HourType();
        Employee employee = new Employee("iivanov", "Ivan", "Ivanov", "ivanov@email");
        row = new HoursSpreadSheetRow(project, employee, hourType, new ArrayList<>());

        String actual = row.getFirstColValue(EffortsGrouping.BY_EMPLOYEES);

        assertEquals(project.getCode(), actual);
    }

    @Test
    public void testGetFirstColValue_ifGroupingByProjects() {
        Project project = new Project();
        project.setCode("project");
        HourType hourType = new HourType();
        Employee employee = new Employee("iivanov", "Ivan", "Ivanov", "ivanov@email");
        row = new HoursSpreadSheetRow(project, employee, hourType, new ArrayList<>());

        String actual = row.getFirstColValue(EffortsGrouping.BY_PROJECTS);

        assertEquals(employee.getFullName(), actual);
    }

}
