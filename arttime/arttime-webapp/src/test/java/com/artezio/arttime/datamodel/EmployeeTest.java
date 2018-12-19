package com.artezio.arttime.datamodel;

import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class EmployeeTest {

    @Test
    public void testEqualsWithSameUserName() {
        Employee employee = new Employee("user");
        Employee sameEmppl = new Employee("user");
        assertTrue(employee.equals(sameEmppl));
    }

    @Test
    public void testEqualsWithDiffrentUserName() {
        Employee employee = new Employee("human");
        Employee sameEmppl = new Employee("programmer");
        assertFalse(employee.equals(sameEmppl));
    }

    @Test
    public void testGetWorkLoadHours() {
        Employee employee = new Employee();
        employee.setWorkLoad(15);
        BigDecimal expected = new BigDecimal("1.2");
        BigDecimal actual = employee.getWorkLoadHours();
        assertTrue(expected.compareTo(actual) == 0);
    }

    @Test
    public void testGetWorkLoadHours_ByNullWorkLoad() {
        Employee employee = new Employee();
        BigDecimal actual = employee.getWorkLoadHours();
        assertNotNull(actual);
    }

    @Test
    public void testGetWorkLoadHours_ByZeroWorkLoad() {
        Employee employee = new Employee();
        employee.setWorkLoad(0);
        BigDecimal expected = BigDecimal.ZERO;
        BigDecimal actual = employee.getWorkLoadHours();
        assertTrue(expected.compareTo(actual) == 0);
    }

    @Test
    public void testSetDepartment_notNull() {
        String department = "Dep";
        Employee employee = new Employee();
        employee.setDepartment(department);
        assertEquals(department, employee.getDepartment());
    }

    @Test
    public void testSetDepartment_null() {
        String expected = "";
        Employee employee = new Employee();
        employee.setDepartment(null);
        assertEquals(expected, employee.getDepartment());
    }

    @Test
    public void testEquals() {
        Employee employee1 = new Employee("user");
        Employee employee2 = new Employee("User");

        assertEquals(employee1, employee2);
    }

    @Test
    public void testHashCode() {
        Employee employee1 = new Employee("user");
        Employee employee2 = new Employee("User");

        assertEquals(employee1.hashCode(), employee2.hashCode());
    }
    
    @Test
    public void testSetUserName() {
        Employee employee = new Employee();
        employee.setUserName("MrSmith");
        
        String actual = Whitebox.getInternalState(employee, "userName");
        
        assertEquals("mrsmith", actual);
    }

}
