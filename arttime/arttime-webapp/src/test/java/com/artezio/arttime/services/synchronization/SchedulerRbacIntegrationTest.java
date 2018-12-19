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
import java.time.Duration;

@RunWith(Arquillian.class)
@Category(IntegrationTest.class)
public class SchedulerRbacIntegrationTest {
    @Inject
    private RunAsProjectManager runAsProjectManager;
    @Inject
    private RunAsAccountant runAsAccountant;
    @Inject
    private RunAsOfficeManager runAsOfficeManager;
    @Inject
    private RunAsIntegrationClient runAsIntegrationClient;
    @Inject
    private RunAsAdmin runAsAdmin;
    @Inject
    private RunAsExec runAsExec;

    @Inject
    private Scheduler scheduler;

    @Deployment
    public static WebArchive deploy() {
        PomEquippedResolveStage resolveStage = Maven.configureResolver().workOffline(false).loadPomFromFile("pom.xml");
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(RangePeriodSelector.class)
                .addClass(CalendarUtils.class)
                .addClass(Settings.class)
                .addClass(Setting.class)
                .addClass(NotificationManagerMockProducer.class)
                .addClass(ApplicationSettings.class)
                .addPackages(true, "com.artezio.arttime.repositories")
                .addClass(Scheduler.class)
                .addClass(Synchronizer.class)
                .addClass(EmployeeService.class)
                .addClass(ProjectService.class)
                .addClass(WorkdaysCalendarService.class)
                .addClass(SettingsService.class)
                .addClass(EmployeeSynchronizerLocal.class)
                .addClass(EmployeeSynchronizer.class)
                .addClass(EmployeeTrackingSystem.class)
                .addClass(TeamTrackingSystem.class)
                .addClass(DepartmentTrackingSystem.class)
                .addClass(LdapAdapter.class)
                .addClass(LdapClient.class)
                .addClass(UserInfo.class)
                .addClass(StringUtil.class)
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

    @Test(expected = EJBAccessException.class)
    public void testSetInterval() throws Exception {
        scheduler.setInterval(Duration.ZERO);
    }

    @Test
    public void testSetInterval_userIsAdmin_allowInvocation() throws Exception {
        runAsAdmin.call(() -> {
            scheduler.setInterval(Duration.ZERO);
            return true;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testSetInterval_userIsExec_disallowInvocation() throws Exception {
        runAsExec.call(() -> {
            scheduler.setInterval(Duration.ZERO);
            return true;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testSetInterval_userIsPM_disallowInvocation() throws Exception {
        runAsProjectManager.call(() -> {
            scheduler.setInterval(Duration.ZERO);
            return true;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testSetInterval_userIsIntegrationClient_disallowInvocation() throws Exception {
        runAsIntegrationClient.call(() -> {
            scheduler.setInterval(Duration.ZERO);
            return true;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testSetInterval_userIsAccountant_disallowInvocation() throws Exception {
        runAsAccountant.call(() -> {
            scheduler.setInterval(Duration.ZERO);
            return true;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testSetInterval_userIsOfficeManager_disallowInvocation() throws Exception {
        runAsOfficeManager.call(() -> {
            scheduler.setInterval(Duration.ZERO);
            return true;
        });
    }

    @Test(expected = EJBAccessException.class)
    public void testGetTimeRemaining() throws Exception {
        scheduler.getTimeRemaining();
    }

    @Test(expected = EJBAccessException.class)
    public void testGetTimeRemaining_userIsAdmin_disallowInvocation() throws Exception {
        runAsAdmin.call(() -> scheduler.getTimeRemaining());
    }

    @Test
    public void testGetTimeRemaining_userIsExec_allowInvocation() throws Exception {
        runAsExec.call(() -> scheduler.getTimeRemaining());
    }

    @Test
    public void testGetTimeRemaining_userIsPM_allowInvocation() throws Exception {
        runAsProjectManager.call(() -> scheduler.getTimeRemaining());
    }

    @Test(expected = EJBAccessException.class)
    public void testGetTimeRemaining_userIsIntegrationClient_disallowInvocation() throws Exception {
        runAsIntegrationClient.call(() -> scheduler.getTimeRemaining());
    }

    @Test(expected = EJBAccessException.class)
    public void testGetTimeRemaining_userIsAccountant_disallowInvocation() throws Exception {
        runAsAccountant.call(() -> scheduler.getTimeRemaining());
    }

    @Test(expected = EJBAccessException.class)
    public void testGetTimeRemaining_userIsOfficeManager_disallowInvocation() throws Exception {
        runAsOfficeManager.call(() -> scheduler.getTimeRemaining());
    }

}
