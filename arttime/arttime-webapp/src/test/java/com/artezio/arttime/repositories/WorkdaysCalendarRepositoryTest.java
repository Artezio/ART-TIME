package com.artezio.arttime.repositories;

import static com.artezio.arttime.test.utils.CalendarUtils.getOffsetDate;
import static junitx.util.PrivateAccessor.setField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.artezio.arttime.datamodel.Day;
import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.datamodel.WorkdaysCalendar;

public class WorkdaysCalendarRepositoryTest {

    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private WorkdaysCalendarRepository workdaysCalendarRepository;

    @Before
    public void setUp() throws Exception {
        workdaysCalendarRepository = new WorkdaysCalendarRepository();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("javax.persistence.validation.mode", "none");
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
        entityManager = entityManagerFactory.createEntityManager();
        setField(workdaysCalendarRepository, "entityManager", entityManager);
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
    public void testCreateWorkdaysCalendar() throws NoSuchFieldException {
        WorkdaysCalendar expected = new WorkdaysCalendar("expected calendar");

        WorkdaysCalendar actual = workdaysCalendarRepository.create(expected);

        assertSame(expected, actual);
    }

    @Test
    public void testCreateOrUpdateDays_Update() {
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar("calendar-testCreateOrUpdateDays_Update");
        Day day = new Day(new Date(), workdaysCalendar);
        entityManager.persist(workdaysCalendar);
        entityManager.persist(day);
        WorkdaysCalendar calendar = new WorkdaysCalendar();
        String expected = "expected calendar name";
        calendar.setName(expected);
        entityManager.persist(calendar);

        day.setWorkdaysCalendar(calendar);
        workdaysCalendarRepository.update(Arrays.asList(day));

        assertEquals(expected, day.getWorkdaysCalendar().getName());
    }

    @Test
    public void testCreateOrUpdateDays_Create() {
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar("calendarTtestCreateOrUpdateDays_Create");
        entityManager.persist(workdaysCalendar);
        Day expected = new Day(new Date(), workdaysCalendar);

        workdaysCalendarRepository.update(Arrays.asList(expected));

        assertNotNull(expected.getId());
        Day actual = entityManager.find(Day.class, expected.getId());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetSpecialDays_ByCalendar() {
        WorkdaysCalendar expectedCalendar = new WorkdaysCalendar();
        WorkdaysCalendar unexpectedCalendar = new WorkdaysCalendar();
        String expectedName = "expected calendar name";
        String unexpectedName = "unexpected calendar name";
        expectedCalendar.setName(expectedName);
        unexpectedCalendar.setName(unexpectedName);
        Date start = new Date();
        Date finish = getOffsetDate(1);
        Day expectedDay = new Day(start, expectedCalendar);
        Day unexpectedDay = new Day(start, unexpectedCalendar);
        entityManager.persist(expectedCalendar);
        entityManager.persist(unexpectedCalendar);
        entityManager.persist(expectedDay);
        entityManager.persist(unexpectedDay);

        Period period = new Period(start, finish);
        List<Day> actuals = workdaysCalendarRepository.getSpecialDays(expectedCalendar, period);

        assertEquals(1, actuals.size());
        assertTrue(actuals.contains(expectedDay));
    }

    @Test
    public void testGetSpecialDays_ByPeriod() {
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar();
        workdaysCalendar.setName("calendarName");
        Date start = new GregorianCalendar(2011, 0, 2).getTime();
        Date finish = new GregorianCalendar(2011, 0, 29).getTime();
        Date expectedDate = new GregorianCalendar(2011, 0, 15).getTime();
        Date unexpectedDate1 = new GregorianCalendar(2011, 0, 1).getTime();
        Date unexpectedDate2 = new GregorianCalendar(2011, 0, 30).getTime();
        Period period = new Period(start, finish);
        Day expected1 = new Day(start, workdaysCalendar);
        Day expected2 = new Day(expectedDate, workdaysCalendar);
        Day expected3 = new Day(finish, workdaysCalendar);
        Day unexpected1 = new Day(unexpectedDate1, workdaysCalendar);
        Day unexpected2 = new Day(unexpectedDate2, workdaysCalendar);
        entityManager.persist(workdaysCalendar);
        entityManager.persist(expected1);
        entityManager.persist(expected3);
        entityManager.persist(expected2);
        entityManager.persist(unexpected1);
        entityManager.persist(unexpected2);

        List<Day> actuals = workdaysCalendarRepository.getSpecialDays(workdaysCalendar, period);

        assertEquals(3, actuals.size());
        assertFalse(actuals.contains(unexpected1));
        assertFalse(actuals.contains(unexpected2));
    }

    @Test
    public void testGetWorkdaysCalendars() {
        WorkdaysCalendar workdaysCalendar1 = new WorkdaysCalendar("calendar1");
        WorkdaysCalendar workdaysCalendar2 = new WorkdaysCalendar("calendar2");
        entityManager.persist(workdaysCalendar1);
        entityManager.persist(workdaysCalendar2);

        List<WorkdaysCalendar> actuals = workdaysCalendarRepository.getWorkdaysCalendars();

        assertEquals(2, actuals.size());
        assertTrue(actuals.contains(workdaysCalendar1));
        assertTrue(actuals.contains(workdaysCalendar2));
    }

    @Test(expected = PersistenceException.class)
    public void testRemoveWorkdaysCalendar_RemoveForbidden() {
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar("calendar");
        Project project = new Project();
        Employee employee = new Employee("employee");
        employee.setCalendar(workdaysCalendar);
        project.addTeamMember(employee);
        entityManager.persist(workdaysCalendar);
        entityManager.persist(employee);
        entityManager.persist(project);

        workdaysCalendarRepository.remove(workdaysCalendar);
        entityManager.flush();
    }

    @Test
    public void testRemoveWorkdaysCalendar() {
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar("calendar");
        Day day = new Day(new Date(), workdaysCalendar);
        entityManager.persist(workdaysCalendar);
        entityManager.persist(day);
        Long dayId = day.getId();
        Long workdaysCalendarId = workdaysCalendar.getId();

        workdaysCalendarRepository.remove(workdaysCalendar);

        entityManager.flush();
        entityManager.clear();
        WorkdaysCalendar actualCalendar = entityManager.find(WorkdaysCalendar.class, workdaysCalendarId);
        Day actualDay = entityManager.find(Day.class, dayId);
        assertNull(actualCalendar);
        assertNull(actualDay);
    }

    @Test
    public void testRemoveWorkdaysCalendar_NotUsedCalendarWithDepartment() {
        WorkdaysCalendar calendar = new WorkdaysCalendar("calendar");
        calendar.getDepartments().add("minsk");
        entityManager.persist(calendar);
        entityManager.flush();
        entityManager.clear();

        workdaysCalendarRepository.remove(calendar);
        entityManager.flush();
        entityManager.clear();

        WorkdaysCalendar actual = entityManager.find(WorkdaysCalendar.class, calendar.getId());
        assertNull(actual);
    }

    @Test
    public void testUpdateWorkdaysCalendar() {
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar("calendar");
        entityManager.persist(workdaysCalendar);
        String expected = "calendar new name";
        workdaysCalendar.setName(expected);

        workdaysCalendarRepository.update(workdaysCalendar, new ArrayList<Day>());
        WorkdaysCalendar actual = entityManager.find(WorkdaysCalendar.class, workdaysCalendar.getId());

        assertEquals(expected, actual.getName());
    }

    @Test
    public void testFindDefaultCalendar() {
        WorkdaysCalendar expected = new WorkdaysCalendar();
        expected.setDepartments(new HashSet<>(Arrays.asList("dep1", "dep2")));
        WorkdaysCalendar unexpected = new WorkdaysCalendar();
        unexpected.setDepartments(new HashSet<>(Arrays.asList("dep2", "dep3")));
        entityManager.persist(expected);
        entityManager.persist(unexpected);

        WorkdaysCalendar actual = workdaysCalendarRepository.findDefaultCalendar("dep1");

        assertEquals(expected, actual);
    }

    @Test
    public void testFindDefaultCalendar_ifNotFound() {
        WorkdaysCalendar actual = workdaysCalendarRepository.findDefaultCalendar("");

        assertNull(actual);
    }

    @Test
    public void testSaveDayWithNullFieldsThenLoadNotNull() {
        WorkdaysCalendar workdaysCalendar = new WorkdaysCalendar("calendar");
        Day day = new Day(new Date(), workdaysCalendar);
        day.setHoliday(null);
        day.setWorking(null);
        entityManager.persist(workdaysCalendar);
        entityManager.persist(day);
        Long dayId = day.getId();
        entityManager.flush();
        entityManager.clear();

        Day persistedDay = entityManager.find(Day.class, dayId);
        assertNotNull(persistedDay.isWorking());
        assertNotNull(persistedDay.isHoliday());
        assertFalse(persistedDay.isHoliday());
    }

    @Test
    public void testFindByDepartment_exists() {
        WorkdaysCalendar expected = new WorkdaysCalendar();
        expected.setDepartments(new HashSet<>(Arrays.asList("dep1", "dep2")));
        WorkdaysCalendar unexpected = new WorkdaysCalendar();
        unexpected.setDepartments(new HashSet<>(Arrays.asList("dep3", "dep4")));
        entityManager.persist(expected);
        entityManager.persist(unexpected);

        WorkdaysCalendar actual = workdaysCalendarRepository.findByDepartment("dep1");

        assertEquals(expected, actual);
    }

    @Test
    public void testFindByDepartment_notExists() {
        WorkdaysCalendar actual = workdaysCalendarRepository.findByDepartment("dep1");
        assertNull(actual);
    }

    @Test
    public void testUpdate() {
        WorkdaysCalendar calendar = new WorkdaysCalendar("WDCal");
        entityManager.persist(calendar);
        entityManager.flush();

        String newName = "NewName";
        calendar.setName(newName);

        WorkdaysCalendar actual = workdaysCalendarRepository.update(calendar);

        assertEquals(newName, actual.getName());
    }

    @Test
    public void testAttachAndRefresh() {
        String savedName = "SavedName1";
        String externallyChangedName = "ExternChangedName";
        WorkdaysCalendar calendar = new WorkdaysCalendar(savedName);
        entityManager.persist(calendar);
        entityManager.flush();
        entityManager.clear();

        entityManager.createQuery(
                "UPDATE WorkdaysCalendar wc " +
                "SET wc.name=:newName WHERE wc.name=:savedName")
                .setParameter("savedName", savedName)
                .setParameter("newName", externallyChangedName)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        calendar = workdaysCalendarRepository.attachAndRefresh(calendar);
        assertEquals(externallyChangedName, calendar.getName());
    }
}
