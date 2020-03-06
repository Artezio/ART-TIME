package com.artezio.arttime.repositories;

import static com.artezio.arttime.test.utils.CalendarUtils.getOffsetDate;
import static java.util.Arrays.asList;
import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PessimisticLockException;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.Project;

import junitx.framework.ListAssert;

@RunWith(PowerMockRunner.class)
public class HoursRepositoryTest {

    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private HoursRepository hoursRepository;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        hoursRepository = new HoursRepository();

        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();

        setField(hoursRepository, "entityManager", entityManager);
        entityManager.getTransaction().begin();
    }

    @After
    public void tearDown() throws Exception {
        if (entityManager.getTransaction().isActive()) {
            if (entityManager.getTransaction().getRollbackOnly()) {
                entityManager.getTransaction().rollback();
            } else {
                entityManager.getTransaction().commit();
            }
            entityManagerFactory.close();
        }
    }



    @Test
    public void testLockEmployee() throws Exception {
        expectedException.expectCause(CoreMatchers.isA(PessimisticLockException.class));
        Employee employee = new Employee("employee");
        entityManager.persist(employee);
        entityManager.flush();
        entityManager.clear();

        hoursRepository.lock(employee);

        Runnable runner = () -> {
            employee.setFirstName("name");
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.merge(employee);
            entityManager.getTransaction().commit();
        };
        runner.run();
        Thread.sleep(3000);

    }

    @Test
    public void testCreate() throws Exception {
        Employee employee = new Employee("employe");
        Project project = new Project();
        HourType hourType = new HourType();
        entityManager.persist(project);
        entityManager.persist(employee);
        entityManager.persist(hourType);
        Hours hours = new Hours(project, sdf.parse("1-01-2015"), employee, hourType);

        Hours actual = hoursRepository.create(hours);

        assertNotNull(actual.getId());
    }

    @Test
    public void testUpdate() throws Exception {
        Employee employee = new Employee("employe");
        Project project = new Project();
        HourType hourType = new HourType();
        Hours hours = new Hours(project, sdf.parse("1-01-2015"), employee, hourType);
        entityManager.persist(project);
        entityManager.persist(employee);
        entityManager.persist(hourType);
        entityManager.persist(hours);
        hours.setQuantity(BigDecimal.TEN);

        Hours actual = hoursRepository.update(hours);

        assertEquals(BigDecimal.TEN, actual.getQuantity());
    }

    @Test
    public void testRemove() throws Exception {
        Employee employee = new Employee("employe");
        Project project = new Project();
        HourType hourType = new HourType();
        Hours hours = new Hours(project, sdf.parse("1-01-2015"), employee, hourType);
        entityManager.persist(project);
        entityManager.persist(employee);
        entityManager.persist(hourType);
        entityManager.persist(hours);

        hoursRepository.remove(hours);

        Hours actual = entityManager.find(Hours.class, hours.getId());
        assertNull(actual);
    }

    @Test
    public void testGetActualHours() throws NoSuchFieldException {
        Date start = new Date();
        Date finish = getOffsetDate(2);
        Period period = new Period(start, finish);
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        Project project = new Project();
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        BigDecimal quantity = new BigDecimal(8);
        Hours hours1 = createHours(project, employee1, getOffsetDate(1), actualType, quantity);
        Hours hours2 = createHours(project, employee1, getOffsetDate(2), actualType, quantity);
        Hours hours3 = createHours(project, employee2, getOffsetDate(0), actualType, quantity);
        hours1.setApproved(true);
        hours2.setApproved(false);
        hours3.setApproved(true);
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(project);
        entityManager.persist(actualType);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        entityManager.persist(hours3);

        List<Employee> employees = asList(employee1, employee2);
        List<Hours> actualHours = hoursRepository.getActualHours(employees, period, false);
        List<Hours> approvedActualHours = hoursRepository.getActualHours(employees, period, true);

        ListAssert.assertEquals(asList(hours1, hours2, hours3), actualHours);
        ListAssert.assertEquals(asList(hours1, hours3), approvedActualHours);
    }

    @Test
    public void testGetActualHours_ifHoursForFinishPeriod() throws NoSuchFieldException {
        Date start = new Date();
        Date finish = getOffsetDate(2);
        Period period = new Period(start, finish);
        Employee employee = new Employee("employee");
        Project project = new Project();
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        BigDecimal quantity = new BigDecimal(8);
        Hours hours = createHours(project, employee, finish, actualType, quantity);
        entityManager.persist(employee);
        entityManager.persist(project);
        entityManager.persist(actualType);
        entityManager.persist(hours);

        List<Hours> actuals = hoursRepository.getActualHours(Collections.singletonList(employee), period, false);

        assertEquals(Collections.singletonList(hours), actuals);
    }

    @Test
    public void testGetActualHours_ifHoursForStartPeriod() throws NoSuchFieldException {
        Date start = new Date();
        Date finish = getOffsetDate(2);
        Period period = new Period(start, finish);
        Employee employee = new Employee("employee");
        Project project = new Project();
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        BigDecimal quantity = new BigDecimal(8);
        Hours hours = createHours(project, employee, start, actualType, quantity);
        entityManager.persist(employee);
        entityManager.persist(project);
        entityManager.persist(actualType);
        entityManager.persist(hours);

        List<Hours> actuals = hoursRepository.getActualHours(Collections.singletonList(employee), period, false);

        assertEquals(Collections.singletonList(hours), actuals);
    }

    @Test
    public void testGetActualHours_ifNoHours() throws NoSuchFieldException {
        Date start = new Date();
        Date finish = getOffsetDate(2);
        Period period = new Period(start, finish);
        Employee employee = new Employee("employee");
        Project project = new Project();
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        BigDecimal quantity = new BigDecimal(8);
        Hours hours1 = createHours(project, employee, getOffsetDate(3), actualType, quantity);
        Hours hours2 = createHours(project, employee, getOffsetDate(-1), actualType, quantity);
        entityManager.persist(employee);
        entityManager.persist(project);
        entityManager.persist(actualType);
        entityManager.persist(hours1);
        entityManager.persist(hours2);

        List<Hours> actuals = hoursRepository.getActualHours(Collections.singletonList(employee), period, false);

        assertTrue(actuals.isEmpty());
    }
    
    
    @Test
    public void testGetActualHours_ifEmployeesIsEmpty() throws NoSuchFieldException {
        Date start = new Date();
        Date finish = getOffsetDate(2);
        Period period = new Period(start, finish);
        Employee employee = new Employee("employee");
        Project project = new Project();
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        BigDecimal quantity = new BigDecimal(8);
        Hours hours1 = createHours(project, employee, getOffsetDate(1), actualType, quantity);
        
        entityManager.persist(employee);
        entityManager.persist(project);
        entityManager.persist(actualType);
        entityManager.persist(hours1);

        List<Hours> actuals = hoursRepository.getActualHours(Collections.emptyList(), period, false);

        assertTrue(actuals.isEmpty());
    }
    
    
    @Test
    public void testGetActualTimeByEmployeeAndApproval() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Project project = new Project();
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        Employee employee3 = new Employee("employee3");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        
        Hours hours1 = createHours(project, employee1, start, actualType, new BigDecimal(8), true);
        Hours hours2 = createHours(project, employee1, getOffsetDate(1), actualType, new BigDecimal(7), true);
        Hours hours3 = createHours(project, employee1, getOffsetDate(2), actualType, new BigDecimal(6), false);
        Hours hours4 = createHours(project, employee1, getOffsetDate(3), actualType, new BigDecimal(5), false);
        Hours hours5 = createHours(project, employee2, getOffsetDate(1), actualType, new BigDecimal(4), true);
        Hours hours6 = createHours(project, employee2, getOffsetDate(2), actualType, new BigDecimal(3), false);
        Hours hours7 = createHours(project, employee3, getOffsetDate(1), actualType, new BigDecimal(2), false);
        
        
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(employee3);
        entityManager.persist(project);
        entityManager.persist(actualType);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        entityManager.persist(hours3);
        entityManager.persist(hours4);
        entityManager.persist(hours5);
        entityManager.persist(hours6);
        entityManager.persist(hours7);
        
        Map<Employee, Map<Boolean, BigDecimal>> actual = hoursRepository
                .getActualTimeByEmployeeAndApproval(Arrays.asList(employee1, employee2, employee3), period);
        
        assertEquals(3, actual.size());
        assertEquals(11.0, actual.get(employee1).get(false).doubleValue(), 0.0);
        assertEquals(15.0, actual.get(employee1).get(true).doubleValue(), 0.0);
        assertEquals(3.0, actual.get(employee2).get(false).doubleValue(), 0.0);
        assertEquals(4.0, actual.get(employee2).get(true).doubleValue(), 0.0);
        assertEquals(2.0, actual.get(employee3).get(false).doubleValue(), 0.0);
    }
    
    @Test
    public void testGetActualTimeByEmployeeAndApproval_byPeriod() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Project project = new Project();
        Employee employee = new Employee("employee");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        BigDecimal quantity = new BigDecimal(8);
        Hours hours1 = createHours(project, employee, getOffsetDate(-1), actualType, quantity);
        Hours hours2 = createHours(project, employee, start, actualType, quantity);
        Hours hours3 = createHours(project, employee, getOffsetDate(2), actualType, quantity);
        Hours hours4 = createHours(project, employee, finish, actualType, quantity);
        Hours hours5 = createHours(project, employee, getOffsetDate(4), actualType, quantity);
        
        entityManager.persist(employee);
        entityManager.persist(project);
        entityManager.persist(actualType);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        entityManager.persist(hours3);
        entityManager.persist(hours4);
        entityManager.persist(hours5);
        
        Map<Employee, Map<Boolean, BigDecimal>> actual = hoursRepository
                .getActualTimeByEmployeeAndApproval(Arrays.asList(employee), period);
        
        assertEquals(24.0, actual.get(employee).get(false).doubleValue(), 0.0);
    }
    
    
    @Test
    public void testGetActualTimeByEmployeeAndApproval_byEmployees() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Project project = new Project();
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        Hours hours1 = createHours(project, employee1, getOffsetDate(2), actualType, new BigDecimal(7));
        Hours hours2 = createHours(project, employee2, getOffsetDate(2), actualType, new BigDecimal(8));
        
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(project);
        entityManager.persist(actualType);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        
        Map<Employee, Map<Boolean, BigDecimal>> actual = hoursRepository
                .getActualTimeByEmployeeAndApproval(Arrays.asList(employee1), period);
        
        assertEquals(1, actual.size());
        assertEquals(7.0, actual.get(employee1).get(false).doubleValue(), 0.0);
    }
    
    @Test
    public void testGetActualTimeByEmployeeAndApproval_ifNotActualTime() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Project project = new Project();
        Employee employee = new Employee("employee");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        HourType notActualType = new HourType("not actual type");
        Hours hours1 = createHours(project, employee, getOffsetDate(2), notActualType, new BigDecimal(7));
        Hours hours2 = createHours(project, employee, getOffsetDate(2), actualType, new BigDecimal(8));
        
        entityManager.persist(employee);
        entityManager.persist(project);
        entityManager.persist(actualType);
        entityManager.persist(notActualType);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        
        Map<Employee, Map<Boolean, BigDecimal>> actual = hoursRepository
                .getActualTimeByEmployeeAndApproval(Arrays.asList(employee), period);
        
        assertEquals(8.0, actual.get(employee).get(false).doubleValue(), 0.0);
    }
    
    @Test
    public void testGetActualTimeByEmployeeAndApproval_ifQuantityIsNull() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Project project = new Project();
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        Hours hours1 = createHours(project, employee1, getOffsetDate(1), actualType, new BigDecimal(7));
        Hours hours2 = createHours(project, employee1, getOffsetDate(2), actualType, null);
        Hours hours3 = createHours(project, employee2, getOffsetDate(2), actualType, null);
        
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(project);
        entityManager.persist(actualType);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        entityManager.persist(hours3);
        
        Map<Employee, Map<Boolean, BigDecimal>> actual = hoursRepository
                .getActualTimeByEmployeeAndApproval(Arrays.asList(employee1, employee2), period);
        
        assertEquals(1, actual.size());
        assertEquals(7.0, actual.get(employee1).get(false).doubleValue(), 0.0);
    }
    
    @Test
    public void testGetManagersForApproveByEmployee() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Employee manager3 = new Employee("manager3");
        Employee manager4 = new Employee("manager4");
        Employee manager5 = new Employee("manager5");
        Project project1 = new Project(); 
        project1.setManagers(Arrays.asList(manager1)); 
        Project project2 = new Project(); 
        project2.setManagers(Arrays.asList(manager2, manager3));
        Project project3 = new Project(); 
        project3.setManagers(Arrays.asList(manager2, manager4, manager5));
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        Hours hours1 = createHours(project1, employee1, getOffsetDate(2), actualType, new BigDecimal(8));
        Hours hours2 = createHours(project2, employee1, getOffsetDate(2), actualType, new BigDecimal(8));
        Hours hours3 = createHours(project3, employee1, getOffsetDate(2), actualType, new BigDecimal(8));
        Hours hours4 = createHours(project2, employee2, getOffsetDate(2), actualType, new BigDecimal(8));
        
        entityManager.persist(manager1);
        entityManager.persist(manager2);
        entityManager.persist(manager3);
        entityManager.persist(manager4);
        entityManager.persist(manager5);
        entityManager.persist(actualType);
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(project3);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        entityManager.persist(hours3);
        entityManager.persist(hours4);
        
        Map<Employee, List<Employee>> actual = hoursRepository
                .getManagersForApproveByEmployee(Arrays.asList(employee1, employee2), period);
        
        assertEquals(2, actual.size());
        ListAssert.assertEquals(Arrays.asList(manager1, manager2, manager3, manager4, manager5), actual.get(employee1));
        ListAssert.assertEquals(Arrays.asList(manager2, manager3), actual.get(employee2));
    }
    
    @Test
    public void testGetManagersForApproveByEmployee_byPeriod() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Employee manager3 = new Employee("manager3");
        Employee manager4 = new Employee("manager4");
        Employee manager5 = new Employee("manager5");
        Project project1 = new Project(); project1.setManagers(Arrays.asList(manager1)); 
        Project project2 = new Project(); project2.setManagers(Arrays.asList(manager2));
        Project project3 = new Project(); project3.setManagers(Arrays.asList(manager3));
        Project project4 = new Project(); project4.setManagers(Arrays.asList(manager4));
        Project project5 = new Project(); project5.setManagers(Arrays.asList(manager5));
        Employee employee = new Employee("employee");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        Hours hours1 = createHours(project1, employee, getOffsetDate(-1), actualType, new BigDecimal(8));
        Hours hours2 = createHours(project2, employee, start, actualType, new BigDecimal(8));
        Hours hours3 = createHours(project3, employee, getOffsetDate(2), actualType, new BigDecimal(8));
        Hours hours4 = createHours(project4, employee, finish, actualType, new BigDecimal(8));
        Hours hours5 = createHours(project5, employee, getOffsetDate(4), actualType, new BigDecimal(8));
        
        entityManager.persist(manager1);
        entityManager.persist(manager2);
        entityManager.persist(manager3);
        entityManager.persist(manager4);
        entityManager.persist(manager5);
        entityManager.persist(actualType);
        entityManager.persist(employee);
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(project3);
        entityManager.persist(project4);
        entityManager.persist(project5);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        entityManager.persist(hours3);
        entityManager.persist(hours4);
        entityManager.persist(hours5);
        
        Map<Employee, List<Employee>> actual = hoursRepository
                .getManagersForApproveByEmployee(Arrays.asList(employee), period);
        
        ListAssert.assertEquals(Arrays.asList(manager2, manager3, manager4), actual.get(employee));
    }
    
    @Test
    public void testGetManagersForApproveByEmployee_byEmployee() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Project project1 = new Project(); project1.setManagers(Arrays.asList(manager1)); 
        Project project2 = new Project(); project2.setManagers(Arrays.asList(manager2));
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        Hours hours1 = createHours(project1, employee1, getOffsetDate(2), actualType, new BigDecimal(8));
        Hours hours2 = createHours(project2, employee2, getOffsetDate(2), actualType, new BigDecimal(8));
        
        entityManager.persist(manager1);
        entityManager.persist(manager2);
        entityManager.persist(actualType);
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        
        Map<Employee, List<Employee>> actual = hoursRepository
                .getManagersForApproveByEmployee(Arrays.asList(employee1), period);
        
        ListAssert.assertEquals(Arrays.asList(manager1), actual.get(employee1));
    }
    
    
    @Test
    public void testGetManagersForApproveByEmployee_ifNotActualTime() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Project project1 = new Project(); project1.setManagers(Arrays.asList(manager1)); 
        Project project2 = new Project(); project2.setManagers(Arrays.asList(manager2));
        Employee employee = new Employee("employee");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        HourType notActualType = new HourType("not actual type");
        Hours hours1 = createHours(project1, employee, getOffsetDate(2), notActualType, new BigDecimal(8));
        Hours hours2 = createHours(project2, employee, getOffsetDate(2), actualType, new BigDecimal(8));
        
        entityManager.persist(manager1);
        entityManager.persist(manager2);
        entityManager.persist(actualType);
        entityManager.persist(notActualType);
        entityManager.persist(employee);
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        
        Map<Employee, List<Employee>> actual = hoursRepository
                .getManagersForApproveByEmployee(Arrays.asList(employee), period);
        
        ListAssert.assertEquals(Arrays.asList(manager2), actual.get(employee));
    }
    
    @Test
    public void testGetManagersForApproveByEmployee_ifQuantityIsNull() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Project project1 = new Project(); project1.setManagers(Arrays.asList(manager1)); 
        Project project2 = new Project(); project2.setManagers(Arrays.asList(manager2));
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        Hours hours1 = createHours(project1, employee1, getOffsetDate(2), actualType, new BigDecimal(8));
        Hours hours2 = createHours(project2, employee1, getOffsetDate(2), actualType, null);
        Hours hours3 = createHours(project1, employee2, getOffsetDate(2), actualType, null);
        
        entityManager.persist(manager1);
        entityManager.persist(manager2);
        entityManager.persist(actualType);
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        entityManager.persist(hours3);
        
        Map<Employee, List<Employee>> actual = hoursRepository
                .getManagersForApproveByEmployee(Arrays.asList(employee1, employee2), period);
        
        assertEquals(1, actual.size());
        ListAssert.assertEquals(Arrays.asList(manager1), actual.get(employee1));
    }
    
    @Test
    public void testGetManagersForApproveByEmployee_ifApproved() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Project project1 = new Project(); project1.setManagers(Arrays.asList(manager1)); 
        Project project2 = new Project(); project2.setManagers(Arrays.asList(manager2));
        Employee employee = new Employee("employee");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        Hours hours1 = createHours(project1, employee, getOffsetDate(2), actualType, new BigDecimal(8), false);
        Hours hours2 = createHours(project2, employee, getOffsetDate(2), actualType, new BigDecimal(8), true);
        
        entityManager.persist(manager1);
        entityManager.persist(manager2);
        entityManager.persist(actualType);
        entityManager.persist(employee);
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        
        Map<Employee, List<Employee>> actual = hoursRepository
                .getManagersForApproveByEmployee(Arrays.asList(employee), period);
        
        ListAssert.assertEquals(Arrays.asList(manager1), actual.get(employee));
    }
    
    @Test
    public void testGetManagersByEmployee() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Employee manager3 = new Employee("manager3");
        Employee manager4 = new Employee("manager4");
        Employee manager5 = new Employee("manager5");
        Project project1 = new Project(); 
        project1.setManagers(Arrays.asList(manager1)); 
        Project project2 = new Project(); 
        project2.setManagers(Arrays.asList(manager2, manager3));
        Project project3 = new Project(); 
        project3.setManagers(Arrays.asList(manager2, manager4, manager5));
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        Hours hours1 = createHours(project1, employee1, getOffsetDate(2), actualType, new BigDecimal(8));
        Hours hours2 = createHours(project2, employee1, getOffsetDate(2), actualType, new BigDecimal(8));
        Hours hours3 = createHours(project3, employee1, getOffsetDate(2), actualType, new BigDecimal(8));
        Hours hours4 = createHours(project2, employee2, getOffsetDate(2), actualType, new BigDecimal(8));
        
        entityManager.persist(manager1);
        entityManager.persist(manager2);
        entityManager.persist(manager3);
        entityManager.persist(manager4);
        entityManager.persist(manager5);
        entityManager.persist(actualType);
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(project3);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        entityManager.persist(hours3);
        entityManager.persist(hours4);
        
        Map<Employee, List<Employee>> actual = hoursRepository
                .getManagersByEmployee(Arrays.asList(employee1, employee2), period);
        
        assertEquals(2, actual.size());
        ListAssert.assertEquals(Arrays.asList(manager1, manager2, manager3, manager4, manager5), actual.get(employee1));
        ListAssert.assertEquals(Arrays.asList(manager2, manager3), actual.get(employee2));
    }
    
    @Test
    public void testGetManagersByEmployee_byPeriod() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Employee manager3 = new Employee("manager3");
        Employee manager4 = new Employee("manager4");
        Employee manager5 = new Employee("manager5");
        Project project1 = new Project(); project1.setManagers(Arrays.asList(manager1)); 
        Project project2 = new Project(); project2.setManagers(Arrays.asList(manager2));
        Project project3 = new Project(); project3.setManagers(Arrays.asList(manager3));
        Project project4 = new Project(); project4.setManagers(Arrays.asList(manager4));
        Project project5 = new Project(); project5.setManagers(Arrays.asList(manager5));
        Employee employee = new Employee("employee");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        Hours hours1 = createHours(project1, employee, getOffsetDate(-1), actualType, new BigDecimal(8));
        Hours hours2 = createHours(project2, employee, start, actualType, new BigDecimal(8));
        Hours hours3 = createHours(project3, employee, getOffsetDate(2), actualType, new BigDecimal(8));
        Hours hours4 = createHours(project4, employee, finish, actualType, new BigDecimal(8));
        Hours hours5 = createHours(project5, employee, getOffsetDate(4), actualType, new BigDecimal(8));
        
        entityManager.persist(manager1);
        entityManager.persist(manager2);
        entityManager.persist(manager3);
        entityManager.persist(manager4);
        entityManager.persist(manager5);
        entityManager.persist(actualType);
        entityManager.persist(employee);
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(project3);
        entityManager.persist(project4);
        entityManager.persist(project5);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        entityManager.persist(hours3);
        entityManager.persist(hours4);
        entityManager.persist(hours5);
        
        Map<Employee, List<Employee>> actual = hoursRepository
                .getManagersByEmployee(Arrays.asList(employee), period);
        
        ListAssert.assertEquals(Arrays.asList(manager2, manager3, manager4), actual.get(employee));
    }
    
    @Test
    public void testGetManagersByEmployee_byEmployee() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Project project1 = new Project(); project1.setManagers(Arrays.asList(manager1)); 
        Project project2 = new Project(); project2.setManagers(Arrays.asList(manager2));
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        Hours hours1 = createHours(project1, employee1, getOffsetDate(2), actualType, new BigDecimal(8));
        Hours hours2 = createHours(project2, employee2, getOffsetDate(2), actualType, new BigDecimal(8));
        
        entityManager.persist(manager1);
        entityManager.persist(manager2);
        entityManager.persist(actualType);
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        
        Map<Employee, List<Employee>> actual = hoursRepository
                .getManagersByEmployee(Arrays.asList(employee1), period);
        
        ListAssert.assertEquals(Arrays.asList(manager1), actual.get(employee1));
    }
    
    
    @Test
    public void testGetManagersByEmployee_ifNotActualTime() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Project project1 = new Project(); project1.setManagers(Arrays.asList(manager1)); 
        Project project2 = new Project(); project2.setManagers(Arrays.asList(manager2));
        Employee employee = new Employee("employee");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        HourType notActualType = new HourType("not actual type");
        Hours hours1 = createHours(project1, employee, getOffsetDate(2), notActualType, new BigDecimal(8));
        Hours hours2 = createHours(project2, employee, getOffsetDate(2), actualType, new BigDecimal(8));
        
        entityManager.persist(manager1);
        entityManager.persist(manager2);
        entityManager.persist(actualType);
        entityManager.persist(notActualType);
        entityManager.persist(employee);
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        
        Map<Employee, List<Employee>> actual = hoursRepository
                .getManagersByEmployee(Arrays.asList(employee), period);
        
        ListAssert.assertEquals(Arrays.asList(manager2), actual.get(employee));
    }
    
    @Test
    public void testGetManagersByEmployee_ifQuantityIsNull() {
        Date start = new Date();
        Date finish = getOffsetDate(3);
        Period period = new Period(start, finish);
        Employee manager1 = new Employee("manager1");
        Employee manager2 = new Employee("manager2");
        Project project1 = new Project(); project1.setManagers(Arrays.asList(manager1)); 
        Project project2 = new Project(); project2.setManagers(Arrays.asList(manager2));
        Employee employee1 = new Employee("employee1");
        Employee employee2 = new Employee("employee2");
        HourType actualType = new HourType("actual type");
        actualType.setActualTime(true);
        Hours hours1 = createHours(project1, employee1, getOffsetDate(2), actualType, new BigDecimal(8));
        Hours hours2 = createHours(project2, employee1, getOffsetDate(2), actualType, null);
        Hours hours3 = createHours(project1, employee2, getOffsetDate(2), actualType, null);
        
        entityManager.persist(manager1);
        entityManager.persist(manager2);
        entityManager.persist(actualType);
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.persist(project1);
        entityManager.persist(project2);
        entityManager.persist(hours1);
        entityManager.persist(hours2);
        entityManager.persist(hours3);
        
        Map<Employee, List<Employee>> actual = hoursRepository
                .getManagersByEmployee(Arrays.asList(employee1, employee2), period);
        
        assertEquals(1, actual.size());
        ListAssert.assertEquals(Arrays.asList(manager1), actual.get(employee1));
    }
    
    private Hours createHours(Project project, Employee employee, Date date, HourType actualType, BigDecimal quantity) {
        return createHours(project, employee, date, actualType, quantity, false);
    }
    
    private Hours createHours(Project project, Employee employee, Date date, HourType actualType, BigDecimal quantity,
            boolean approved) {
        Hours hours = new Hours();
        hours.setEmployee(employee);
        hours.setDate(date);
        hours.setQuantity(quantity);
        hours.setType(actualType);
        hours.setProject(project);
        hours.setApproved(approved);
        return hours;
    }

}
