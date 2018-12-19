package com.artezio.arttime.services.synchronization;

import com.artezio.arttime.config.ApplicationSettings;
import com.artezio.arttime.config.Setting;
import com.artezio.arttime.config.Settings;
import com.artezio.arttime.filter.Filter;
import com.artezio.arttime.markers.IntegrationTest;
import com.artezio.arttime.services.*;
import com.artezio.arttime.services.integration.*;
import com.artezio.arttime.services.integration.spi.UserInfo;
import com.artezio.arttime.services.integration.spi.ldap.LdapAdapter;
import com.artezio.arttime.services.integration.spi.ldap.LdapClient;
import com.artezio.arttime.test.utils.security.SessionContextProducer;
import com.artezio.arttime.test.utils.security.runas.*;
import com.artezio.arttime.utils.CalendarUtils;
import com.artezio.arttime.utils.StringUtil;
import com.artezio.arttime.web.criteria.RangePeriodSelector;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.ejb.EJBAccessException;
import javax.inject.Inject;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class SynchronizerRbacIntegrationTest {

    @Inject
    private RunAsAdmin runAsAdmin;
    @Inject
    private RunAsExec runAsExec;
    @Inject
    private RunAsIntegrationClient runAsIntegrationClient;
    @Inject
    private RunAsAccountant runAsAccountant;
    @Inject
    private RunAsOfficeManager runAsOfficeManager;
    @Inject
    private RunAsSystem runAsSystem;
    @Inject
    private SynchronizerSync synchronizer;

    @Deployment
    public static WebArchive createDeployment() {
        PomEquippedResolveStage resolveStage = Maven.configureResolver().workOffline(false).loadPomFromFile("pom.xml");
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(RangePeriodSelector.class)
                .addClass(CalendarUtils.class)
                .addClass(Settings.class)
                .addClass(Setting.class)
                .addClass(NotificationManagerMockProducer.class)
                .addClass(ApplicationSettings.class)
                .addPackages(true, "com.artezio.arttime.repositories")
                .addClass(Synchronizer.class)
                .addClass(SynchronizerSync.class)
                .addClass(EmployeeService.class)
                .addClass(ProjectService.class)
                .addClass(WorkdaysCalendarService.class)
                .addClass(SettingsService.class)
                .addClass(EmployeeSynchronizerLocal.class)
                .addClass(EmployeeSynchronizer.class)
                .addClass(EmployeeTrackingSystem.class)
                .addClass(TeamTrackingSystem.class)
                .addClass(DepartmentTrackingSystem.class)
                .addClass(StringUtil.class)
                .addClass(LdapAdapter.class)
                .addClass(LdapClient.class)
                .addClass(UserInfo.class)
                .addClass(NotificationManagerLocal.class)
                .addClass(TeamSynchronizer.class)
                .addClass(TrackingSystem.class)
                .addClass(IntegrationTest.class)
                .addPackage("com.artezio.arttime.datamodel")
                .addPackages(true, "org.apache.commons")
                .addPackage("com.artezio.arttime.exceptions")
                .addPackages(true, "com.artezio.arttime.test.utils")
                .addClasses(Filter.class, ApplyHoursChangeException.class, HoursChange.class, SessionContextProducer.class)
                .addAsLibraries(resolveStage.resolve("com.artezio.javax.jpa:abac").withoutTransitivity().asSingleFile())
                .addAsResource("META-INF/arquillian-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testSynchronize_RunAsAdmin() throws Exception {
        runAsAdmin.call(() -> {
            synchronizer.synchronize();
            return null;
        });
    }

    @Test
    public void testSynchronize_RunAsSystem() throws Exception {
        runAsSystem.call(() -> {
            synchronizer.synchronize();
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testSynchronize_RunAsExec() throws Exception {
        runAsExec.call(() -> {
            synchronizer.synchronize();
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testSynchronize_RunAsIntegrationClient() throws Exception {
        runAsIntegrationClient.call(() -> {
            synchronizer.synchronize();
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testSynchronize_RunAsAccountant() throws Exception {
        runAsAccountant.call(() -> {
            synchronizer.synchronize();
            return null;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testSynchronize_RunAsOfficeManager() throws Exception {
        runAsOfficeManager.call(() -> {
            synchronizer.synchronize();
            return null;
        });
    }

}
