package com.artezio.arttime.datamodel;

import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.markers.IntegrationTest;
import com.artezio.arttime.services.ApplyHoursChangeException;
import com.artezio.arttime.services.HoursChange;
import com.artezio.arttime.test.utils.security.SessionContextProducer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class ProjectAbacIntegrationTest extends AbacIntegrationTest {
    @PersistenceContext(unitName = "secured-test-pu")
    private EntityManager abacEntityManager;

    @Deployment
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage("com.artezio.arttime.datamodel")
                .addPackage("com.artezio.arttime.web.criteria")
                .addPackage("com.artezio.arttime.utils")
                .addPackage("com.artezio.arttime.repositories")
                .addPackages(true, "org.apache.commons")
                .addPackage("com.artezio.arttime.exceptions")
                .addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("com.artezio.javax.jpa:abac").withoutTransitivity().asSingleFile())
                .addClass(IntegrationTest.class)
                .addClasses(Filter.class, ApplyHoursChangeException.class, HoursChange.class, SessionContextProducer.class)
                .addPackages(true, "com.artezio.arttime.test.utils")
                .addAsResource("META-INF/arquillian-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    private Employee anotherManager;
    private Employee managerOfAllProjects;
    private Employee exec;
    private Employee employee;
    private HourType hourType;
    private Project projectManagedByPm;
    private Project subProjectManagedByPm;
    private Project projectNotManagedByPm;
    private Project subProjectNotManagedByPm;
    private Project projectWithEmployeeInTeam;
    private String department = "dept";
    private String department2 = "dept2";

    @Before
    public void setUp() throws Exception {
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            exec = new Employee("exec_username", "exec_firstname", "exec_lastname", "exec_email", "exec_dept");
            managerOfAllProjects = new Employee("superman", "super", "manager", "xm", "deptxx");
            anotherManager = new Employee("another_pm_username", "another_pm_firstname", "another_pm_lastname", "another_pm_email", "another_pm_dept");
            employee = new Employee("emp_username", "emp_firstname", "emp_lastname", "emp_email", "emp_dept");
            hourType = new HourType("Hourtype1");
            hourType.setActualTime(true);
            projectNotManagedByPm = createProject("NotManagedMaster", hourType);
            projectNotManagedByPm.addManager(anotherManager);
            projectNotManagedByPm.addManager(managerOfAllProjects);
            subProjectNotManagedByPm = createProject("NotManagedSub", hourType);
            subProjectNotManagedByPm.setMaster(projectNotManagedByPm);
            subProjectNotManagedByPm.addManager(anotherManager);
            subProjectNotManagedByPm.addManager(managerOfAllProjects);
            projectManagedByPm = createProject("ManagedMaster", hourType);
            projectManagedByPm.addManager(managerOfAllProjects);
            subProjectManagedByPm = createProject("ManagedSub", hourType);
            subProjectManagedByPm.setMaster(projectManagedByPm);
            subProjectManagedByPm.addManager(anotherManager);
            subProjectManagedByPm.addManager(managerOfAllProjects);
            projectWithEmployeeInTeam = createProject("WithEmployeeInTeam", hourType);
            projectWithEmployeeInTeam.addManager(anotherManager);
            projectWithEmployeeInTeam.addManager(managerOfAllProjects);
            projectWithEmployeeInTeam.addTeamMember(employee);
            unsecuredEntityManager.persist(exec);
            unsecuredEntityManager.persist(employee);
            unsecuredEntityManager.persist(managerOfAllProjects);
            unsecuredEntityManager.persist(anotherManager);
            unsecuredEntityManager.persist(hourType);
            unsecuredEntityManager.persist(projectManagedByPm);
            unsecuredEntityManager.persist(projectNotManagedByPm);
            unsecuredEntityManager.persist(subProjectManagedByPm);
            unsecuredEntityManager.persist(subProjectNotManagedByPm);
            unsecuredEntityManager.persist(projectWithEmployeeInTeam);
        });
    }

    @After
    public void tearDown() throws Exception {
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.createNativeQuery("DELETE FROM employee_accessibledepartments").executeUpdate();
            unsecuredEntityManager.createQuery("DELETE FROM Project").executeUpdate();
            unsecuredEntityManager.createQuery("DELETE FROM Employee").executeUpdate();
            unsecuredEntityManager.createQuery("DELETE FROM HourType").executeUpdate();
        });
    }

    @Test
    public void testGet_viewTimesheetContext() throws Exception {
        Employee caller = createAnonymousEmployee(department);
        Project projectWithEmployeeInTeamInactive = createProject("InTeamInactive", hourType);
        projectWithEmployeeInTeamInactive.addManager(anotherManager);
        projectWithEmployeeInTeamInactive.setStatus(Project.Status.CLOSED);
        projectWithEmployeeInTeamInactive.addTeamMember(caller);
        projectWithEmployeeInTeam.addTeamMember(caller);
        projectManagedByPm.addManager(caller);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(projectWithEmployeeInTeamInactive);
            projectWithEmployeeInTeam = unsecuredEntityManager.merge(projectWithEmployeeInTeam);
            projectManagedByPm = unsecuredEntityManager.merge(projectManagedByPm);
        });
        List<Project> actualList = runInNewTx.call(() ->
                callUsingAbacContext.callInViewTimesheetContext(() ->
                        abacEntityManager.createQuery("SELECT p FROM Project p", Project.class).getResultList()));
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(projectWithEmployeeInTeam));
        assertFalse(actualList.contains(projectManagedByPm));
        assertFalse(actualList.contains(projectWithEmployeeInTeamInactive));
        assertFalse(actualList.contains(projectNotManagedByPm));
        assertFalse(actualList.contains(subProjectNotManagedByPm));
        assertFalse(actualList.contains(subProjectManagedByPm));
    }

    @Test
    public void testGet() throws Exception {
        Employee caller = createAnonymousEmployee(department);
        Project projectWithEmployeeInTeamInactive = createProject("InTeamInactive", hourType);
        projectWithEmployeeInTeamInactive.addManager(anotherManager);
        projectWithEmployeeInTeamInactive.setStatus(Project.Status.CLOSED);
        projectWithEmployeeInTeamInactive.addTeamMember(caller);
        projectWithEmployeeInTeam.addTeamMember(caller);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(projectWithEmployeeInTeamInactive);
            projectWithEmployeeInTeam = unsecuredEntityManager.merge(projectWithEmployeeInTeam);
        });
        List<Project> actualList = runInNewTx.call(() ->
                abacEntityManager.createQuery("SELECT p FROM Project p", Project.class).getResultList());
        assertTrue(actualList.isEmpty());
    }

    @Test
    public void testGet_callerIsExec() throws Exception {
        Employee caller = createAnonymousEmployee(department, new String[]{department});
        List<Project> actualList = runAsExec.call(() -> runInNewTx.call(() ->
                abacEntityManager.createQuery("SELECT p FROM Project p", Project.class).getResultList()));
        assertContainsAllProjects(actualList);
    }

    @Test
    public void testGet_callerIsSystem() throws Exception {
        Employee caller = createAnonymousEmployee(department, new String[]{department});
        List<Project> actualList = runAsSystem.call(() -> runInNewTx.call(() ->
                abacEntityManager.createQuery("SELECT p FROM Project p", Project.class).getResultList()));
        assertContainsAllProjects(actualList);
    }

    @Test
    public void testGet_callerIsProjectManager() throws Exception {
        Employee caller = createAnonymousEmployee(department, new String[]{department});
        Project masterManagedProject = createProject("MasterManaged", hourType);
        masterManagedProject.addManager(caller);
        Project masterProjectSubproject = createProject("MasterManagedSub", hourType);
        masterProjectSubproject.setMaster(masterManagedProject);
        masterProjectSubproject.addManager(managerOfAllProjects);
        Project masterUnManagedProject = createProject("MasterUnmanaged", hourType);
        masterUnManagedProject.addManager(managerOfAllProjects);
        Project managedSubProject = createProject("ManagedSub", hourType);
        managedSubProject.setMaster(masterUnManagedProject);
        managedSubProject.addManager(caller);
        Project unmanagedProject = createProject("Unmanaged", hourType);
        unmanagedProject.addManager(managerOfAllProjects);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(masterManagedProject);
            unsecuredEntityManager.persist(masterProjectSubproject);
            unsecuredEntityManager.persist(masterUnManagedProject);
            unsecuredEntityManager.persist(managedSubProject);
            unsecuredEntityManager.persist(unmanagedProject);
        });
        List<Project> actualList = runAsProjectManager.call(() -> runInNewTx.call(() ->
                abacEntityManager.createQuery("SELECT p FROM Project p", Project.class).getResultList()));
        assertTrue(actualList.contains(masterManagedProject));
        assertTrue(actualList.contains(masterProjectSubproject));
        assertFalse(actualList.contains(masterUnManagedProject));
        assertTrue(actualList.contains(managedSubProject));
        assertFalse(actualList.contains(unmanagedProject));
    }

    @Test
    public void testGet_callerIsOfficeManager() throws Exception {
        Employee caller = createAnonymousEmployee(department, new String[]{department2});
        Employee managedEmployee = createEmployee("managedEmployee", department2, new String[]{});
        Employee unmanagedEmployee = createEmployee("unmanagedEmployee", department, new String[]{});
        Project projectWithManagedEmployee = createProject("WithManaged", hourType);
        projectWithManagedEmployee.addTeamMember(managedEmployee);
        projectWithManagedEmployee.addManager(managerOfAllProjects);
        Project projectWithUnmanagedEmployee = createProject("WithUnmanaged", hourType);
        projectWithUnmanagedEmployee.addTeamMember(unmanagedEmployee);
        projectWithUnmanagedEmployee.addManager(managerOfAllProjects);
        Project managedProject = createProject("ManagedProject", hourType);
        managedProject.addTeamMember(unmanagedEmployee);
        managedProject.addManager(caller);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(projectWithManagedEmployee);
            unsecuredEntityManager.persist(projectWithUnmanagedEmployee);
            unsecuredEntityManager.persist(managedProject);
        });
        List<Project> actualList = runAsOfficeManager.call(() -> runInNewTx.call(() ->
                abacEntityManager.createQuery("SELECT p FROM Project p", Project.class).getResultList()));
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(projectWithManagedEmployee));
        assertTrue(actualList.contains(managedProject));
        assertFalse(actualList.contains(projectWithUnmanagedEmployee));
    }

    @Test
    public void testGet_callerIsAccountant() throws Exception {
        Employee caller = createAnonymousEmployee(department, new String[]{department2});
        Employee managedEmployee = createEmployee("managedEmployee", department2, new String[]{});
        Employee unmanagedEmployee = createEmployee("unmanagedEmployee", department, new String[]{});
        Project projectWithManagedEmployee = createProject("WithManaged", hourType);
        projectWithManagedEmployee.addTeamMember(managedEmployee);
        projectWithManagedEmployee.addManager(managerOfAllProjects);
        Project projectWithUnmanagedEmployee = createProject("WithUnmanaged", hourType);
        projectWithUnmanagedEmployee.addTeamMember(unmanagedEmployee);
        projectWithUnmanagedEmployee.addManager(managerOfAllProjects);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(projectWithManagedEmployee);
            unsecuredEntityManager.persist(projectWithUnmanagedEmployee);
        });
        List<Project> actualList = runAsAccountant.call(() ->
                runInNewTx.call(() ->
                        abacEntityManager.createQuery("SELECT p FROM Project p", Project.class).getResultList()));
        assertFalse(actualList.isEmpty());
        assertTrue(actualList.contains(projectWithManagedEmployee));
        assertFalse(actualList.contains(projectWithUnmanagedEmployee));
    }

    @Test
    public void testGet_manageProjectsContext() throws Exception {
        Employee caller = createAnonymousEmployee(department);
        Project projectWithEmployeeInTeamInactive = createProject("InTeamInactive", hourType);
        projectWithEmployeeInTeamInactive.addManager(anotherManager);
        projectWithEmployeeInTeamInactive.setStatus(Project.Status.CLOSED);
        projectWithEmployeeInTeamInactive.addTeamMember(caller);
        projectWithEmployeeInTeam.addTeamMember(caller);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(projectWithEmployeeInTeamInactive);
            projectWithEmployeeInTeam = unsecuredEntityManager.merge(projectWithEmployeeInTeam);
        });
        List<Project> actualList = runInNewTx.call(() ->
                callUsingAbacContext.callInManageProjectsContext(() ->
                        abacEntityManager.createQuery("SELECT p FROM Project p", Project.class).getResultList()));
        assertTrue(actualList.isEmpty());
    }

    @Test
    public void testGet_manageProjectsContext_callerIsExec() throws Exception {
        Employee caller = createAnonymousEmployee(department, new String[]{department});
        List<Project> actualList = runAsExec.call(() -> runInNewTx.call(() ->
                callUsingAbacContext.callInManageProjectsContext(() ->
                        abacEntityManager.createQuery("SELECT p FROM Project p", Project.class).getResultList())));
        assertContainsAllProjects(actualList);
    }

    @Test
    public void testGet_manageProjectsContext_callerIsProjectManager() throws Exception {
        Employee caller = createAnonymousEmployee(department, new String[]{department});
        Project masterManagedProject = createProject("MasterManaged", hourType);
        masterManagedProject.addManager(caller);
        Project masterProjectSubproject = createProject("MasterManagedSub", hourType);
        masterProjectSubproject.setMaster(masterManagedProject);
        masterProjectSubproject.addManager(managerOfAllProjects);
        Project masterUnManagedProject = createProject("MasterUnmanaged", hourType);
        masterUnManagedProject.addManager(managerOfAllProjects);
        Project managedSubProject = createProject("ManagedSub", hourType);
        managedSubProject.setMaster(masterUnManagedProject);
        managedSubProject.addManager(caller);
        Project unmanagedProject = createProject("Unmanaged", hourType);
        unmanagedProject.addManager(managerOfAllProjects);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(masterManagedProject);
            unsecuredEntityManager.persist(masterProjectSubproject);
            unsecuredEntityManager.persist(masterUnManagedProject);
            unsecuredEntityManager.persist(managedSubProject);
            unsecuredEntityManager.persist(unmanagedProject);
        });
        List<Project> actualList = runAsProjectManager.call(() -> runInNewTx.call(() ->
                callUsingAbacContext.callInManageProjectsContext(() ->
                        abacEntityManager.createQuery("SELECT p FROM Project p", Project.class).getResultList())));
        assertTrue(actualList.contains(masterManagedProject));
        assertTrue(actualList.contains(masterProjectSubproject));
        assertFalse(actualList.contains(masterUnManagedProject));
        assertTrue(actualList.contains(managedSubProject));
        assertFalse(actualList.contains(unmanagedProject));
    }

    @Test
    public void testGet_manageProjectsContext_callerIsOfficeManager() throws Exception {
        Employee caller = createAnonymousEmployee(department, new String[]{department});
        Project masterManagedProject = createProject("MasterManaged", hourType);
        masterManagedProject.addManager(caller);
        Project masterProjectSubproject = createProject("MasterManagedSub", hourType);
        masterProjectSubproject.setMaster(masterManagedProject);
        masterProjectSubproject.addManager(managerOfAllProjects);
        Project masterUnManagedProject = createProject("MasterUnmanaged", hourType);
        masterUnManagedProject.addManager(managerOfAllProjects);
        Project managedSubProject = createProject("ManagedSub", hourType);
        managedSubProject.setMaster(masterUnManagedProject);
        managedSubProject.addManager(caller);
        Project unmanagedProject = createProject("Unmanaged", hourType);
        unmanagedProject.addManager(managerOfAllProjects);
        runInNewTx.run(() -> {
            unsecuredEntityManager.joinTransaction();
            unsecuredEntityManager.persist(masterManagedProject);
            unsecuredEntityManager.persist(masterProjectSubproject);
            unsecuredEntityManager.persist(masterUnManagedProject);
            unsecuredEntityManager.persist(managedSubProject);
            unsecuredEntityManager.persist(unmanagedProject);
        });
        List<Project> actualList = runAsOfficeManager.call(() -> runInNewTx.call(() ->
                callUsingAbacContext.callInManageProjectsContext(() ->
                        abacEntityManager.createQuery("SELECT p FROM Project p", Project.class).getResultList())));
        assertTrue(actualList.contains(masterManagedProject));
        assertTrue(actualList.contains(masterProjectSubproject));
        assertFalse(actualList.contains(masterUnManagedProject));
        assertTrue(actualList.contains(managedSubProject));
        assertFalse(actualList.contains(unmanagedProject));
    }

    protected void assertContainsAllProjects(Collection<Project> projects) {
        assertFalse(projects.isEmpty());
        assertTrue(projects.contains(projectManagedByPm));
        assertTrue(projects.contains(subProjectManagedByPm));
        assertTrue(projects.contains(projectNotManagedByPm));
        assertTrue(projects.contains(subProjectNotManagedByPm));
    }

    protected Project createProject(String code, HourType... accountableHours) {
        Project project = new Project();
        project.setCode(code);
        project.setTeamFilter(new TeamFilter(TeamFilter.Type.DISABLED));
        project.setAccountableHours(Arrays.asList(accountableHours));
        project.setStatus(Project.Status.ACTIVE);
        return project;
    }

}
