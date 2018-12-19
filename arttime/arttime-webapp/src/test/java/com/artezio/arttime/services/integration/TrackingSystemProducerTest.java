package com.artezio.arttime.services.integration;

import com.artezio.arttime.config.Settings;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static junitx.util.PrivateAccessor.setField;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CDI.class)
public class TrackingSystemProducerTest {

    private TrackingSystemProducer trackingSystemProducer;

    @Test
    public void testGetEmployeeTrackingSystem() throws NoSuchFieldException {
        Settings settings = createMock(Settings.class);
        Instance instance = createMock(Instance.class);
        trackingSystemProducer = createMockBuilder(TrackingSystemProducer.class)
                .addMockedMethod("findImplementationByName", Instance.class, String.class)
                .createMock();
        setField(trackingSystemProducer, "employeeTrackingSystems", instance);
        setField(trackingSystemProducer, "settings", settings);
        EmployeeTrackingSystem expectedImpl = createMock(EmployeeTrackingSystem.class);
        expect(trackingSystemProducer.findImplementationByName(instance, "ExpectedImpl")).andReturn(expectedImpl);
        expect(settings.getEmployeeTrackingSystemName()).andReturn("ExpectedImpl");
        replayAll(instance, settings, expectedImpl, trackingSystemProducer);

        EmployeeTrackingSystem actual = trackingSystemProducer.getEmployeeTrackingSystem();

        PowerMock.verifyAll();
        assertEquals(expectedImpl, actual);
    }

    @Test
    public void testGetTeamTrackingSystem() throws NoSuchFieldException {
        Settings settings = createMock(Settings.class);
        Instance instance = createMock(Instance.class);
        trackingSystemProducer = createMockBuilder(TrackingSystemProducer.class)
                .addMockedMethod("findImplementationByName", Instance.class, String.class)
                .createMock();
        setField(trackingSystemProducer, "teamTrackingSystems", instance);
        setField(trackingSystemProducer, "settings", settings);
        TeamTrackingSystem expectedImpl = createMock(TeamTrackingSystem.class);
        expect(trackingSystemProducer.findImplementationByName(instance, "ExpectedImpl")).andReturn(expectedImpl);
        expect(settings.getTeamTrackingSystemName()).andReturn("ExpectedImpl");
        replayAll(instance, settings, expectedImpl, trackingSystemProducer);

        TeamTrackingSystem actual = trackingSystemProducer.getTeamTrackingSystem();

        PowerMock.verifyAll();
        assertEquals(expectedImpl, actual);
    }

    @Test
    public void testGetDepartmentTrackingSystem() throws NoSuchFieldException {
        Settings settings = createMock(Settings.class);
        Instance instance = createMock(Instance.class);
        trackingSystemProducer = createMockBuilder(TrackingSystemProducer.class)
                .addMockedMethod("findImplementationByName", Instance.class, String.class)
                .createMock();
        setField(trackingSystemProducer, "departmentTrackingSystems", instance);
        setField(trackingSystemProducer, "settings", settings);
        DepartmentTrackingSystem expectedImpl = createMock(DepartmentTrackingSystem.class);
        expect(trackingSystemProducer.findImplementationByName(instance, "ExpectedImpl")).andReturn(expectedImpl);
        expect(settings.getDepartmentTrackingSystemName()).andReturn("ExpectedImpl");
        replayAll(instance, settings, expectedImpl, trackingSystemProducer);

        DepartmentTrackingSystem actual = trackingSystemProducer.getDepartmentTrackingSystem();

        PowerMock.verifyAll();
        assertEquals(expectedImpl, actual);
    }

    @Test
    public void testGetEmployeeTrackingSystemNames() throws NoSuchFieldException {
        Instance instance = createMock(Instance.class);
        trackingSystemProducer = createMockBuilder(TrackingSystemProducer.class)
                .addMockedMethod("getTrackingSystemsNames", Instance.class)
                .createMock();
        setField(trackingSystemProducer, "employeeTrackingSystems", instance);
        Set<String> expected = Sets.newHashSet("A", "B");
        expect(trackingSystemProducer.getTrackingSystemsNames(instance)).andReturn(expected);
        replayAll(instance, trackingSystemProducer);

        Set<String> actual = trackingSystemProducer.getEmployeeTrackingSystemNames();

        verifyAll();
        assertEquals(expected, actual);
    }

    @Test
    public void getTeamTrackingSystemNames() throws NoSuchFieldException {
        Instance instance = createMock(Instance.class);
        trackingSystemProducer = createMockBuilder(TrackingSystemProducer.class)
                .addMockedMethod("getTrackingSystemsNames", Instance.class)
                .createMock();
        setField(trackingSystemProducer, "teamTrackingSystems", instance);
        Set<String> expected = Sets.newHashSet("A", "B");
        expect(trackingSystemProducer.getTrackingSystemsNames(instance)).andReturn(expected);
        replayAll(instance, trackingSystemProducer);

        Set<String> actual = trackingSystemProducer.getTeamTrackingSystemNames();

        verifyAll();
        assertEquals(expected, actual);
    }

    @Test
    public void getDepartmentTrackingSystemNames() throws NoSuchFieldException {
        Instance instance = createMock(Instance.class);
        trackingSystemProducer = createMockBuilder(TrackingSystemProducer.class)
                .addMockedMethod("getTrackingSystemsNames", Instance.class)
                .createMock();
        setField(trackingSystemProducer, "departmentTrackingSystems", instance);
        Set<String> expected = Sets.newHashSet("A", "B");
        expect(trackingSystemProducer.getTrackingSystemsNames(instance)).andReturn(expected);
        replayAll(instance, trackingSystemProducer);

        Set<String> actual = trackingSystemProducer.getDepartmentTrackingSystemNames();

        verifyAll();
        assertEquals(expected, actual);
    }

    @Test
    public void testFindImplementationByName() {
        Instance instance = createMock(Instance.class);
        trackingSystemProducer = createMockBuilder(TrackingSystemProducer.class).createMock();
        TrackingSystem expectedImpl = createMock(TrackingSystem.class);
        TrackingSystem unexpectedImpl = createMock(TrackingSystem.class);
        List<TrackingSystem> implementations = Arrays.asList(expectedImpl, unexpectedImpl);
        expect(instance.iterator()).andReturn(implementations.iterator());
        expect(expectedImpl.getName()).andReturn("Expected").anyTimes();
        expect(unexpectedImpl.getName()).andReturn("UnExpected").anyTimes();

        replayAll(trackingSystemProducer, instance, expectedImpl, unexpectedImpl);

        TrackingSystem actual = trackingSystemProducer.findImplementationByName(instance, "Expected");

        PowerMock.verifyAll();
        assertEquals(expectedImpl, actual);
    }

    @Test
    public void getTrackingSystemsNames() {
        Instance instance = createMock(Instance.class);
        trackingSystemProducer = createMockBuilder(TrackingSystemProducer.class).createMock();
        TrackingSystem impl1 = createMock(TrackingSystem.class);
        TrackingSystem impl2 = createMock(TrackingSystem.class);
        List<TrackingSystem> implementations = Arrays.asList(impl1, impl2);
        expect(instance.spliterator()).andReturn(implementations.spliterator());
        expect(impl1.getName()).andReturn("Impl1");
        expect(impl2.getName()).andReturn("Impl2");

        replayAll(trackingSystemProducer, instance, impl1, impl2);

        Set<String> actual = trackingSystemProducer.getTrackingSystemsNames(instance);

        PowerMock.verifyAll();
        assertTrue(actual.contains("Impl1"));
        assertTrue(actual.contains("Impl2"));
    }
}