package com.artezio.arttime.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

import org.junit.Test;

public class SettingsTest {
    
    @Test
    public void testGetEmployeeMappingParams() {
        Settings settings = new Settings(new EnumMap<>(Setting.Name.class));
        settings.setLdapUserNameAttribute("ldapUserNameAttribute");
        settings.setLdapFirstNameAttribute("ldapFirstNameAttribute");
        settings.setLdapLastNameAttribute("ldapLastNameAttribute");
        settings.setLdapMailAttribute("ldapMailAttribute");
        settings.setLdapDepartmentAttribute("ldapDepartmentAttribute");

        Map<String, String> actual = settings.getEmployeeMappingParams();

        assertEquals(5, actual.size());
        assertEquals("userName", actual.get("ldapUserNameAttribute"));
        assertEquals("firstName", actual.get("ldapFirstNameAttribute"));
        assertEquals("lastName", actual.get("ldapLastNameAttribute"));
        assertEquals("department", actual.get("ldapDepartmentAttribute"));
        assertEquals("email", actual.get("ldapMailAttribute"));
    }

    @Test
    public void testGetTimerInterval_hoursNorMinutesNotSet() {
        Settings settings = new Settings(new EnumMap<>(Setting.Name.class));

        Duration interval = settings.getTimerInterval();

        assertEquals(interval.toMillis(), 0);
    }

    @Test
    public void testGetTimerInterval_hoursAndMinutesSet() {
        Settings settings = new Settings(new EnumMap<>(Setting.Name.class));
        settings.setTimerHoursInterval(4);
        settings.setTimerMinutesInterval(5);

        Duration interval = settings.getTimerInterval();

        assertEquals(interval, Duration.ofHours(4).plusMinutes(5));
    }
    
    
    @Test
    public void testGetAsString_ifPresent() {
        Map<Setting.Name, String> valueMap = new EnumMap<>(Setting.Name.class);
        valueMap.put(Setting.Name.APPLICATION_BASE_URL, "value1");
        Settings settings = new Settings(valueMap);
        assertEquals(settings.getAsString(Setting.Name.APPLICATION_BASE_URL), "value1");
    }

    @Test
    public void testGetAsString_ifNotPresent() {
        Settings settings = new Settings(new EnumMap<>(Setting.Name.class));
        assertNull(settings.getAsString(Setting.Name.APPLICATION_BASE_URL));
    }

    @Test
    public void testSet_ifPresent() {
        Map<Setting.Name, String> valueMap = new EnumMap<>(Setting.Name.class);
        valueMap.put(Setting.Name.APPLICATION_BASE_URL, "value1");
        Settings settings = new Settings(valueMap);
        settings.set(Setting.Name.APPLICATION_BASE_URL, "newValue");
        assertEquals(settings.getAsString(Setting.Name.APPLICATION_BASE_URL), "newValue");
    }

    @Test
    public void testSet_ifNotPresent() {
        Settings settings = new Settings(new EnumMap<>(Setting.Name.class));
        settings.set(Setting.Name.APPLICATION_BASE_URL, "value1");
        assertEquals(settings.getAsString(Setting.Name.APPLICATION_BASE_URL), "value1");
    }

    @Test
    public void testSetBoolean_true() {
        Settings settings = new Settings(new EnumMap<>(Setting.Name.class));
        settings.set(Setting.Name.EMPLOYEE_SYNCHRONIZATION_ENABLED, true);
        settings.set(Setting.Name.TEAM_SYNCHRONIZATION_ENABLED, false);
        assertEquals(settings.getValuesBySettingNames().get(Setting.Name.EMPLOYEE_SYNCHRONIZATION_ENABLED), "true");
        assertEquals(settings.getValuesBySettingNames().get(Setting.Name.TEAM_SYNCHRONIZATION_ENABLED), "false");
    }

    @Test
    public void testSetInteger() {
        Settings settings = new Settings(new EnumMap<>(Setting.Name.class));
        settings.set(Setting.Name.TIMER_HOURS_INTERVAL, 273);
        assertEquals(settings.getValuesBySettingNames().get(Setting.Name.TIMER_HOURS_INTERVAL), "273");
    }

    @Test
    public void testGetAsBoolean_IfPresent() {
        Map<Setting.Name, String> valueMap = new EnumMap<>(Setting.Name.class);
        valueMap.put(Setting.Name.EMPLOYEE_SYNCHRONIZATION_ENABLED, "true");
        Settings settings = new Settings(valueMap);
        assertEquals(settings.getAsBoolean(Setting.Name.EMPLOYEE_SYNCHRONIZATION_ENABLED), true);
    }

    @Test
    public void testGetAsBoolean_IfNotPresent() {
        Settings settings = new Settings(new EnumMap<>(Setting.Name.class));
        assertEquals(settings.getAsBoolean(Setting.Name.EMPLOYEE_SYNCHRONIZATION_ENABLED), false);
    }

    @Test
    public void testGetAsInt_IfPresent() {
        Map<Setting.Name, String> valueMap = new EnumMap<>(Setting.Name.class);
        valueMap.put(Setting.Name.TIMER_HOURS_INTERVAL, "498");
        Settings settings = new Settings(valueMap);
        assertEquals(settings.getAsInt(Setting.Name.TIMER_HOURS_INTERVAL), 498);
    }

    @Test
    public void testGetAsInt_IfNotPresent() {
        Settings settings = new Settings(new EnumMap<>(Setting.Name.class));
        assertEquals(settings.getAsInt(Setting.Name.TIMER_HOURS_INTERVAL), 0);
    }

}
