package com.artezio.arttime.web.spread_sheet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.web.EffortsGrouping;

public class HeadSpreadSheetRowTest {
    private HeadSpreadSheetRow row;

    @Test
    public void testGetRowTotal() {
        Employee employee = new Employee();
        row = new HeadSpreadSheetRow(employee);
        Object actual = row.getRowTotal();
        assertNull(actual);
    }

    @Test
    public void testGet() {
        Employee employee = new Employee();
        row = new HeadSpreadSheetRow(employee);
        Date date = new Date();
        row.getValuesMap().put(date, 555);
        Object actual = row.get(date);
        assertNull(actual);
    }

    @Test
    public void testGetFirstColValue_ifGroupingByEmployees() {
        Employee employee = new Employee("iivanov", "Ivan", "Ivanov", "ivanov@email");
        row = new HeadSpreadSheetRow(employee);
        String actual = row.getFirstColValue(EffortsGrouping.BY_EMPLOYEES);
        assertEquals(employee.getFullName(), actual);
    }

    @Test
    public void testGetFirstColValue_ifGroupingByProjects() {
        Project project = new Project();
        project.setCode("project");
        row = new HeadSpreadSheetRow(project);
        String actual = row.getFirstColValue(EffortsGrouping.BY_PROJECTS);
        assertEquals(project.getCode(), actual);
    }
}
