package com.artezio.arttime.services;

import com.artezio.arttime.config.ApplicationSettings;
import com.artezio.arttime.config.Setting;
import com.artezio.arttime.config.Settings;
import com.artezio.arttime.datamodel.*;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.markers.IntegrationTest;
import com.artezio.arttime.services.integration.DepartmentTrackingSystem;
import com.artezio.arttime.services.integration.EmployeeTrackingSystem;
import com.artezio.arttime.services.integration.TrackingSystem;
import com.artezio.arttime.test.utils.RunInNewTx;
import com.artezio.arttime.test.utils.security.SessionContextProducer;
import com.artezio.arttime.test.utils.security.runas.*;
import com.artezio.arttime.utils.CalendarUtils;
import com.artezio.arttime.utils.ListAdapter;
import com.artezio.arttime.web.criteria.RangePeriodSelector;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.ejb.EJBAccessException;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static java.util.Arrays.asList;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class ProjectServiceRbacIntegrationTest {

    @Inject
    private RunAsProjectManager runAsProjectManager;
    @Inject
    private RunAsAccountant runAsAccountant;
    @Inject
    private RunAsOfficeManager runAsOfficeManager;
    @Inject
    private RunAsIntegrationClient runAsIntegrationClient;
    @Inject
    private RunAsSystem runAsSystem;
    @Inject
    private RunAsAdmin runAsAdmin;
    @Inject
    private RunAsExec runAsExec;
    @Inject
    private RunInNewTx runInNewTx;
    @PersistenceContext(unitName = "test-pu")
    private EntityManager entityManager;
    @Inject
    private ProjectService projectService;

    private Project project;
    private Project projectForCreationByEmployee;
    private Employee manager;
    private HourType hourType;
    private Filter filter = new Filter();

    @Deployment
    public static WebArchive deploy() {
        PomEquippedResolveStage resolveStage = Maven.configureResolver().workOffline(false).loadPomFromFile("pom.xml");
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(RangePeriodSelector.class)
                .addClass(CalendarUtils.class)
                .addClass(Settings.class)
                .addClass(Setting.class)
                .addClass(ApplicationSettings.class)
                .addPackages(true, "com.artezio.arttime.repositories")
                .addPackages(true, "com.artezio.arttime.test.utils")
                .addClass(ProjectService.class)
                .addClass(SettingsService.class)
                .addClass(TrackingSystem.class)
                .addClass(DepartmentTrackingSystem.class)
                .addClass(EmployeeTrackingSystem.class)
                .addClass(IntegrationTest.class)
                .addClass(ListAdapter.class)
                .addPackage("com.artezio.arttime.datamodel")
                .addPackages(true, "org.apache.commons")
                .addPackage("com.artezio.arttime.exceptions")
                .addPackages(true, "com.artezio.arttime.test.utils")
                .addClasses(Filter.class, ApplyHoursChangeException.class, HoursChange.class, SessionContextProducer.class)
                .addAsLibraries(resolveStage.resolve("com.artezio.javax.jpa:abac").withoutTransitivity().asSingleFile())
                .addAsLibraries(resolveStage.resolve("org.mockito:mockito-core:1.10.19").withTransitivity().asFile())
                .addAsLibraries(resolveStage.resolve("junit-addons:junit-addons:1.4").withTransitivity().asFile())
                .addAsResource("META-INF/arquillian-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void setUp() throws Exception {
        runInNewTx.run(() -> {
            entityManager.joinTransaction();
            hourType = new HourType("type");
            manager = new Employee("uname", "fname", "lname", "email",
                    "department1");
            project = buildProject("project", hourType, manager);
            projectForCreationByEmployee = buildProject("projectForCreationByEmployee", hourType, manager);
            entityManager.persist(manager);
            entityManager.persist(hourType);
            entityManager.persist(project);
        });
    }

    private Project buildProject(String code, HourType hourType, Employee manager) {
        Project project = new Project();
        project.setCode(code);
        project.setTeamFilter(new TeamFilter(TeamFilter.Type.DISABLED));
        project.addAccountableHours(hourType);
        project.addManager(manager);
        return project;
    }

    @After
    public void tearDown() throws Exception {
        runInNewTx.run(() -> {
            entityManager.joinTransaction();
            entityManager.remove(entityManager.find(Employee.class, manager.getUserName()));
            entityManager.remove(entityManager.find(HourType.class, hourType.getId()));
            if (this.projectForCreationByEmployee.getId() != null) {
                entityManager.remove(entityManager.find(Project.class, this.projectForCreationByEmployee.getId()));
            }
            Project project = entityManager.find(Project.class, this.project.getId());
            if (project != null) {
                entityManager.remove(project);
            }
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testCreate() {
        projectService.create(projectForCreationByEmployee);
    }

    @Test(expected = EJBAccessException.class)
    public void testCreate_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> projectService.create(projectForCreationByEmployee));
    }

    @Test(expected = EJBAccessException.class)
    public void testCreate_userIsSystem() throws Exception {
        runAsSystem.call(() -> projectService.create(projectForCreationByEmployee));
    }

    @Test
    public void testCreate_userIsExec() throws Exception {
        runAsExec.call(() -> projectService.create(projectForCreationByEmployee));
    }

    @Test
    public void testCreate_userIsProjectManager() throws Exception {
        runAsProjectManager.call(() -> projectService.create(projectForCreationByEmployee));
    }

    @Test(expected = EJBAccessException.class)
    public void testCreate_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> projectService.create(projectForCreationByEmployee));
    }

    @Test(expected = EJBAccessException.class)
    public void testCreate_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> projectService.create(projectForCreationByEmployee));
    }

    @Test(expected = EJBAccessException.class)
    public void testCreate_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> projectService.create(projectForCreationByEmployee));
    }
    
    @Test(expected = EJBAccessException.class)
    public void testUpdateCollection() {
        projectService.update(asList(project));
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdateCollection_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> {
            projectService.update(asList(project));
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdateCollection_userIsSystem() throws Exception {
        runAsSystem.call(() -> {
            projectService.update(asList(project));
            return null;
        });
    }

    @Test
    public void testUpdateCollection_userIsExec() throws Exception {
        runAsExec.call(() -> {
            projectService.update(asList(project));
            return null;
        });
    }

    @Test
    public void testUpdateCollection_userIsProjectManager() throws Exception {
        runAsProjectManager.call(() -> {
            projectService.update(asList(project));
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdateCollection_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> {
            projectService.update(asList(project));
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdateCollection_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> {
            projectService.update(asList(project));
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testUpdateCollection_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> {
            projectService.update(asList(project));
            return null;
        });
    }
    
    @Test(expected = EJBAccessException.class)
    public void testRemove() {
        projectService.remove(project);
    }

    @Test(expected = EJBAccessException.class)
    public void testRemove_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> {
            projectService.remove(project);
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testRemove_userIsSystem() throws Exception {
        runAsSystem.call(() -> {
            projectService.remove(project);
            return null;
        });
    }

    @Test
    public void testRemove_userIsExec() throws Exception {
        runAsExec.call(() -> {
            projectService.remove(project);
            return null;
        });
    }

    @Test
    public void testRemove_userIsProjectManager() throws Exception {
        runAsProjectManager.call(() -> {
            projectService.remove(project);
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testRemove_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> {
            projectService.remove(project);
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testRemove_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> {
            projectService.remove(project);
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testFetchComplete() {
        projectService.fetchComplete(asList(project));
    }

    @Test(expected = EJBAccessException.class)
    public void testFetchComplete_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> projectService.fetchComplete(asList(project)));
    }

    @Test(expected = EJBAccessException.class)
    public void testFetchComplete_userIsSystem() throws Exception {
        runAsSystem.call(() -> projectService.fetchComplete(asList(project)));
    }

    @Test
    public void testFetchComplete_userIsExec() throws Exception {
        runAsExec.call(() -> projectService.fetchComplete(asList(project)));
    }

    @Test
    public void testFetchComplete_userIsProjectManager() throws Exception {
        runAsProjectManager.call(() -> projectService.fetchComplete(asList(project)));
    }

    @Test
    public void testFetchComplete_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> projectService.fetchComplete(asList(project)));
    }

    @Test
    public void testFetchComplete_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> projectService.fetchComplete(asList(project)));
    }

    @Test
    public void testFetchComplete_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> projectService.fetchComplete(asList(project)));
    }
    
    @Test
    public void testGetMyProjects() {
        projectService.getMyProjects();
    }

    @Test
    public void testGetMyProjects_userIsAdmin() throws Exception {
        runAsAdmin.call(projectService::getMyProjects);
    }

    @Test
    public void testGetMyProjects_userIsSystem() throws Exception {
        runAsSystem.call(projectService::getMyProjects);
    }

    @Test
    public void testGetMyProjects_userIsExec() throws Exception {
        runAsExec.call(projectService::getMyProjects);
    }

    @Test
    public void testGetMyProjects_userIsProjectManager() throws Exception {
        runAsProjectManager.call(projectService::getMyProjects);
    }

    @Test
    public void testGetMyProjects_userIsAccountant() throws Exception {
        runAsAccountant.call(projectService::getMyProjects);
    }

    @Test
    public void testGetMyProjects_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(projectService::getMyProjects);
    }

    @Test
    public void testGetMyProjects_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(projectService::getMyProjects);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetManagedProjects() {
        projectService.getManagedProjects();
    }

    @Test(expected = EJBAccessException.class)
    public void testGetManagedProjects_userIsAdmin() throws Exception {
        runAsAdmin.call(projectService::getManagedProjects);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetManagedProjects_userIsSystem() throws Exception {
        runAsSystem.call(projectService::getManagedProjects);
    }

    @Test
    public void testGetManagedProjects_userIsExec() throws Exception {
        runAsExec.call(projectService::getManagedProjects);
    }

    @Test
    public void testGetManagedProjects_userIsProjectManager() throws Exception {
        runAsProjectManager.call(projectService::getManagedProjects);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetManagedProjects_userIsAccountant() throws Exception {
        runAsAccountant.call(projectService::getManagedProjects);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetManagedProjects_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(projectService::getManagedProjects);
    }

    @Test
    public void testGetManagedProjects_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(projectService::getAll);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll() {
        projectService.getAll();
    }

    @Test(expected = EJBAccessException.class)
    public void testGetAll_userIsAdmin() throws Exception {
        runAsAdmin.call(projectService::getAll);
    }

    @Test
    public void testGetAll_userIsSystem() throws Exception {
        runAsSystem.call(projectService::getAll);
    }

    @Test
    public void testGetAll_userIsExec() throws Exception {
        runAsExec.call(projectService::getAll);
    }

    @Test
    public void testGetAll_userIsProjectManager() throws Exception {
        runAsProjectManager.call(projectService::getAll);
    }

    @Test
    public void testGetAll_userIsAccountant() throws Exception {
        runAsAccountant.call(projectService::getAll);
    }

    @Test
    public void testGetAll_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(projectService::getAll);
    }

    @Test
    public void testGetAll_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(projectService::getAll);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEffortsProjects() {
        projectService.getEffortsProjects(filter);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEffortsProjects_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> {
            projectService.getEffortsProjects(filter);
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEffortsProjects_userIsSystem() throws Exception {
        runAsSystem.call(() -> {
            projectService.getEffortsProjects(filter);
            return null;
        });
    }

    @Test
    public void testGetEffortsProjects_userIsExec() throws Exception {
        runAsExec.call(() -> {
            projectService.getEffortsProjects(filter);
            return null;
        });
    }

    @Test
    public void testGetEffortsProjects_userIsProjectManager() throws Exception {
        runAsProjectManager.call(() -> {
            projectService.getEffortsProjects(filter);
            return null;
        });
    }

    @Test
    public void testGetEffortsProjects_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> {
            projectService.getEffortsProjects(filter);
            return null;
        });
    }

    @Test
    public void testGetEffortsProjects_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> {
            projectService.getEffortsProjects(filter);
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testGetEffortsProjects_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> {
            projectService.getEffortsProjects(filter);
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testLoadProject() {
        projectService.loadProject(project.getId());
    }

    @Test(expected = EJBAccessException.class)
    public void testLoadProject_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> projectService.loadProject(project.getId()));
    }

    @Test(expected = EJBAccessException.class)
    public void testLoadProject_userIsSystem() throws Exception {
        runAsSystem.call(() -> projectService.loadProject(project.getId()));
    }

    @Test
    public void testLoadProject_userIsExec() throws Exception {
        runAsExec.call(() -> projectService.loadProject(project.getId()));
    }

    @Test
    public void testLoadProject_userIsProjectManager() throws Exception {
        runAsProjectManager.call(() -> projectService.loadProject(project.getId()));
    }

    @Test(expected = EJBAccessException.class)
    public void testLoadProject_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> projectService.loadProject(project.getId()));
    }

    @Test(expected = EJBAccessException.class)
    public void testLoadProject_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> projectService.loadProject(project.getId()));
    }

    @Test(expected = EJBAccessException.class)
    public void testLoadProject_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> projectService.loadProject(project.getId()));
    }

    @Test(expected = EJBAccessException.class)
    public void testGetProjects() {
        projectService.getProjects(asList(1L));
    }

    @Test(expected = EJBAccessException.class)
    public void testGetProjects_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> projectService.getProjects(asList(1L)));
    }

    @Test(expected = EJBAccessException.class)
    public void testGetProjects_userIsSystem() throws Exception {
        runAsSystem.call(() -> projectService.getProjects(asList(1L)));
    }

    @Test
    public void testGetProjects_userIsExec() throws Exception {
        runAsExec.call(() -> projectService.getProjects(asList(1L)));
    }

    @Test
    public void testGetProjects_userIsProjectManager() throws Exception {
        runAsProjectManager.call(() -> projectService.getProjects(asList(1L)));
    }

    @Test
    public void testGetProjects_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> projectService.getProjects(asList(1L)));
    }

    @Test
    public void testGetProjects_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> projectService.getProjects(asList(1L)));
    }

    @Test(expected = EJBAccessException.class)
    public void testGetProjects_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> projectService.getProjects(asList(1L)));
    }

    @Test(expected = EJBAccessException.class)
    public void testGetProjectHierarchy() {
        projectService.getProjectHierarchy(project);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetProjectHierarchy_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> projectService.getProjectHierarchy(project));
    }

    @Test(expected = EJBAccessException.class)
    public void testGetProjectHierarchy_userIsSystem() throws Exception {
        runAsSystem.call(() -> projectService.getProjectHierarchy(project));
    }

    @Test
    public void testGetProjectHierarchy_userIsExec() throws Exception {
        runAsExec.call(() -> projectService.getProjectHierarchy(project));
    }

    @Test
    public void testGetProjectHierarchy_userIsProjectManager() throws Exception {
        runAsProjectManager.call(() -> projectService.getProjectHierarchy(project));
    }

    @Test
    public void testGetProjectHierarchy_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> projectService.getProjectHierarchy(project));
    }

    @Test
    public void testGetProjectHierarchy_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> projectService.getProjectHierarchy(project));
    }

    @Test
    public void testGetProjectHierarchy_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> projectService.getProjectHierarchy(project));
    }

    @Test(expected = EJBAccessException.class)
    public void testGetManagedProjectHierarchy() {
        projectService.getManagedProjectHierarchy(project);
    }

    @Test(expected = EJBAccessException.class)
    public void testGetManagedProjectHierarchy_userIsAdmin() throws Exception {
        runAsAdmin.call(() -> projectService.getManagedProjectHierarchy(project));
    }

    @Test(expected = EJBAccessException.class)
    public void testGetManagedProjectHierarchy_userIsSystem() throws Exception {
        runAsSystem.call(() -> projectService.getManagedProjectHierarchy(project));
    }

    @Test
    public void testGetManagedProjectHierarchy_userIsExec() throws Exception {
        runAsExec.call(() -> projectService.getManagedProjectHierarchy(project));
    }

    @Test
    public void testGetManagedProjectHierarchy_userIsProjectManager() throws Exception {
        runAsProjectManager.call(() -> projectService.getManagedProjectHierarchy(project));
    }

    @Test(expected = EJBAccessException.class)
    public void testGetManagedProjectHierarchy_userIsAccountant() throws Exception {
        runAsAccountant.call(() -> projectService.getManagedProjectHierarchy(project));
    }

    @Test(expected = EJBAccessException.class)
    public void testGetManagedProjectHierarchy_userIsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> projectService.getManagedProjectHierarchy(project));
    }

    @Test(expected = EJBAccessException.class)
    public void testGetManagedProjectHierarchy_userIsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> projectService.getManagedProjectHierarchy(project));
    }
    
}
