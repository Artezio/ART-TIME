package com.artezio.arttime.services.synchronization;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static junitx.util.PrivateAccessor.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import com.artezio.arttime.config.Setting;
import com.artezio.arttime.config.Settings;
import junitx.framework.ListAssert;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artezio.arttime.services.SettingsService;

public class SchedulerTest {

    private Scheduler scheduler;
    private Settings settings;
    private Synchronizer synchronizer;

    @Before
    public void setUp() throws NoSuchFieldException {
        scheduler = new Scheduler();
        settings = new Settings(new EnumMap<>(Setting.Name.class));
        IMocksControl mockControl = EasyMock.createControl();
        synchronizer = mockControl.createMock(Synchronizer.class);
        setField(scheduler, "settings", settings);
        setField(scheduler, "synchronizer", synchronizer);
    }

    @Test
    public void testInitialize() throws NoSuchFieldException {
        scheduler = createMockBuilder(Scheduler.class)
                .addMockedMethod("setInterval", Duration.class)
                .createMock();
        setField(scheduler, "settings", settings);
        scheduler.setInterval(anyObject(Duration.class));
        replay(scheduler);

        scheduler.initialize();

        verify(scheduler);
    }

    @Test
    public void testGetTimeRemaining() {
        Timer timer = createMock(Timer.class);
        scheduler = createMockBuilder(Scheduler.class)
                .addMockedMethod("findActiveTimers")
                .createMock();
        expect(scheduler.findActiveTimers()).andReturn(Arrays.asList(timer));
        expect(timer.getTimeRemaining()).andReturn(1000L);
        replay(scheduler, timer);

        Duration actual = scheduler.getTimeRemaining();

        verify(scheduler, timer);
        assertEquals(Duration.ofMillis(1000L), actual);
    }

    @Test
    public void testGetTimeRemaining_ifTimersNotFired() {
        scheduler = createMockBuilder(Scheduler.class)
                .addMockedMethod("findActiveTimers")
                .createMock();
        expect(scheduler.findActiveTimers()).andReturn(new ArrayList<>());
        replay(scheduler);

        Duration actual = scheduler.getTimeRemaining();

        verify(scheduler);
        assertNull(actual);
    }

    @Test
    public void testCreateIntervalTimer() throws Exception {
        Timer timer = createMock(Timer.class);
        TimerService timerService = createMock(TimerService.class);
        expect(timerService.createIntervalTimer(eq(100L), eq(100L), anyObject(TimerConfig.class))).andReturn(timer);
        setField(scheduler, "timerService", timerService);
        replay(timerService);

        scheduler.createIntervalTimer(100L);

        verify(timerService);
    }

    @Test
    public void testSchedule() {
        scheduler = createMockBuilder(Scheduler.class)
                .addMockedMethod("cancelExistedTimers")
                .addMockedMethod("createIntervalTimer", long.class)
                .createMock();
        Duration interval = Duration.ofHours(4);
        scheduler.cancelExistedTimers();
        scheduler.createIntervalTimer(eq(interval.toMillis()));
        replay(scheduler);

        scheduler.setInterval(interval);

        verify(scheduler);
    }

    @Test
    public void testCancelExistedTimers() {
        Timer timer = createMock(Timer.class);
        scheduler = createMockBuilder(Scheduler.class)
                .addMockedMethod("findActiveTimers")
                .createMock();
        expect(scheduler.findActiveTimers()).andReturn(Arrays.asList(timer));
        timer.cancel();
        replay(timer, scheduler);

        scheduler.cancelExistedTimers();

        verify(timer, scheduler);
    }

    @Test
    public void testFindActiveTimers() throws Exception {
        Timer timer1 = createMock(Timer.class);
        Timer timer2 = createMock(Timer.class);
        TimerService timerService = createMock(TimerService.class);
        setField(scheduler, "timerService", timerService);
        timerService.getTimers();
        expectLastCall().andReturn(Arrays.asList(timer1, timer2));
        expect(timer1.getInfo()).andReturn("com.artezio.arttime.synchronization");
        expect(timer2.getInfo()).andReturn("com.artezio.arttime.another_process");
        replay(timerService, timer1, timer2);
        List<Timer> expected = Arrays.asList(timer1);

        List<Timer> actual = scheduler.findActiveTimers();

        verify(timerService, timer1, timer2);
        ListAssert.assertEquals(expected, actual);
    }

    @Test
    public void testTimeout() throws NoSuchFieldException {
        synchronizer = createMockBuilder(Synchronizer.class)
                .addMockedMethod("synchronize")
                .createMock();
        setField(scheduler, "synchronizer", synchronizer);
        synchronizer.synchronize();
        expectLastCall().once();
        replay(synchronizer);

        scheduler.timeout();

        verify(synchronizer);
    }

    @Test
    public void testSetInterval_lessThanMinute() {
        scheduler = createMockBuilder(Scheduler.class)
                .addMockedMethod("cancelExistedTimers")
                .addMockedMethod("createIntervalTimer", long.class)
                .createMock();
        scheduler.cancelExistedTimers();
        Capture<Long> capture = Capture.newInstance();
        scheduler.createIntervalTimer(captureLong(capture));
        replay(scheduler);

        scheduler.setInterval(Duration.ofSeconds(1));

        assertTrue(capture.getValue() > 0);
    }

    @Test
    public void testSetInterval_moreThanMinute() {
        scheduler = createMockBuilder(Scheduler.class)
                .addMockedMethod("cancelExistedTimers")
                .addMockedMethod("createIntervalTimer", long.class)
                .createMock();
        scheduler.cancelExistedTimers();
        Capture<Long> capture = Capture.newInstance();
        scheduler.createIntervalTimer(captureLong(capture));
        replay(scheduler);

        scheduler.setInterval(Duration.ofMinutes(2));

        assertTrue(capture.getValue().equals(Duration.ofMinutes(2).toMillis()));
    }

}

