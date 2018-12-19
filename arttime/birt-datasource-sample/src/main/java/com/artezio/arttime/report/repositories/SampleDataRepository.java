package com.artezio.arttime.report.repositories;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.datamodel.Project.Status;

public class SampleDataRepository {

    private static List<Project> projects;
    private static List<HourType> hourTypes;
    private static List<Hours> hours;
    private static List<Employee> employees;
    
    static {
        init();
    }

    private static void init() {
        try {
            initHourTypes();
            initEmployees();
            initProjects();
            initHours();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<Project> getProjects() {
        return projects;
    }

    public List<HourType> getHourTypes() {
        return hourTypes;
    }

    public List<Hours> getHours() {
        return hours;
    }

    private static void initHours() throws IllegalAccessException {
        hours = new ArrayList<>();
        
        Hours hours1 = new Hours(projects.get(0), new GregorianCalendar(2018, 2, 3).getTime(), employees.get(0), hourTypes.get(0));
        FieldUtils.writeField(hours1, "id", 1L, true);
        hours1.setQuantity(BigDecimal.valueOf(8));
        hours1.setApproved(true);
        hours.add(hours1);

        Hours hours2 = new Hours(projects.get(1), new GregorianCalendar(2018, 2, 4).getTime(), employees.get(1), hourTypes.get(1));
        FieldUtils.writeField(hours2, "id", 2L, true);
        hours2.setQuantity(BigDecimal.valueOf(8));
        hours2.setApproved(true);
        hours.add(hours2);

        Hours hours3 = new Hours(projects.get(2), new GregorianCalendar(2018, 2, 5).getTime(), employees.get(2), hourTypes.get(2));
        FieldUtils.writeField(hours3, "id", 3L, true);
        hours3.setQuantity(BigDecimal.valueOf(8));
        hours3.setApproved(true);
        hours.add(hours3);
        
        
        Hours hours4 = new Hours(projects.get(2), new GregorianCalendar(2018, 2, 6).getTime(), employees.get(3), hourTypes.get(2));
        FieldUtils.writeField(hours4, "id", 4L, true);
        hours4.setQuantity(BigDecimal.valueOf(7));
        hours4.setApproved(true);
        hours.add(hours4);
    }

    private static void initProjects() throws IllegalAccessException {
        projects = new ArrayList<>();
        
        Project project1 = new Project();
        FieldUtils.writeField(project1, "id", 1L, true);
        project1.setCode("Project_without_parent");
        project1.setAccountableHours(Arrays.asList(hourTypes.get(0)));
        project1.setDescription("Project without parent");
        project1.setManagers(employees.subList(0, 1));
        project1.setStatus(Status.ACTIVE);
        project1.setTeam(employees.subList(0, 2));
        projects.add(project1);

        Project project2 = new Project();
        FieldUtils.writeField(project2, "id", 2L, true);
        project2.setCode("Parent_project");
        project2.setAccountableHours(Arrays.asList(hourTypes.get(0), hourTypes.get(1)));
        project2.setDescription("Parent project");
        project2.setManagers(employees.subList(0, 1));
        project2.setStatus(Status.ACTIVE);
        project2.setTeam(employees.subList(2, 4));
        projects.add(project2);

        Project project3 = new Project();
        FieldUtils.writeField(project3, "id", 3L, true);
        project3.setCode("Project_with_parent");
        project3.setMaster(project2);
        project3.setAccountableHours(Arrays.asList(hourTypes.get(0), hourTypes.get(1)));
        project3.setDescription("Project with parent");
        project3.setManagers(employees.subList(1, 2));
        project3.setStatus(Status.ACTIVE);
        project3.setTeam(employees.subList(2, 4));
        projects.add(project3);
    }

    private static void initEmployees() {
        employees = new ArrayList<>();
        
        Employee employee1 = new Employee("user1", "User1", "First", "first@mail.com", "Minsk");
        employees.add(employee1);
        Employee employee2 = new Employee("user2", "User2", "Second", "second@mail.com", "Minsk");
        employees.add(employee2);
        Employee employee3 = new Employee("user3", "User3", "Third", "third@mail.com", "Moscow");
        employees.add(employee3);
        Employee employee4 = new Employee("user4", "User4", "Fourth", "fourth@mail.com", "Moscow");
        employees.add(employee4);
    }

    private static void initHourTypes() throws IllegalAccessException {
        hourTypes = new ArrayList<>();
        
        HourType hourType1 = new HourType("actual_type_priority_1");
        FieldUtils.writeField(hourType1, "id", 1L, true);
        hourType1.setActualTime(true);
        hourType1.setPriority(1);
        hourTypes.add(hourType1);

        HourType hourType2 = new HourType("not_actual_type_priority_2");
        FieldUtils.writeField(hourType2, "id", 2L, true);
        hourType2.setActualTime(false);
        hourType2.setPriority(2);
        hourTypes.add(hourType2);

        HourType hourType3 = new HourType("not_actual_type_priority_3");
        FieldUtils.writeField(hourType3, "id", 3L, true);
        hourType3.setActualTime(false);
        hourType3.setPriority(3);
        hourTypes.add(hourType3);
    }
    
}
