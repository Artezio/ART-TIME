package com.artezio.arttime.services.synchronization;

import com.artezio.arttime.config.ApplicationSettings;
import com.artezio.arttime.config.Settings;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.*;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.artezio.arttime.security.auth.UserRoles.*;

@Named
@Startup
@Singleton
@RunAs(SYSTEM_ROLE)
public class Scheduler {

    private final static String TIMER_INFO = "com.artezio.arttime.synchronization";

    @Resource
    private TimerService timerService;
    @Inject
    @ApplicationSettings
    private Settings settings;
    @Inject
    private Synchronizer synchronizer;

    @PostConstruct
    public void initialize() {
        setInterval(settings.getTimerInterval());
    }

    @Timeout
    void timeout() {
        synchronizer.synchronize();
    }

    @RolesAllowed(ADMIN_ROLE)
    public void setInterval(Duration interval) {
        if (interval.compareTo(Duration.ofMinutes(1)) < 0) {
            interval = Duration.ofHours(1);
        }
        cancelExistedTimers();
        createIntervalTimer(interval.toMillis());
    }

    @RolesAllowed({EXEC_ROLE, PM_ROLE, OFFICE_MANAGER})
    public Duration getTimeRemaining() {
        return findActiveTimers().stream().findFirst()
                .map(timer -> Duration.ofMillis(timer.getTimeRemaining()))
                .orElse(null);
    }

    protected void createIntervalTimer(long intervalMillis) {
        TimerConfig config = new TimerConfig();
        config.setInfo(TIMER_INFO);
        config.setPersistent(false);
        timerService.createIntervalTimer(intervalMillis, intervalMillis, config);
    }

    protected void cancelExistedTimers() {
        findActiveTimers().forEach(Timer::cancel);
    }

    protected List<Timer> findActiveTimers() {
        return timerService.getTimers().stream()
                .filter(timer -> TIMER_INFO.equals(timer.getInfo()))
                .collect(Collectors.toList());
    }
}
