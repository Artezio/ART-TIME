package com.artezio.arttime.config;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class Settings implements Serializable {

    private static final long serialVersionUID = 2L;

    private final static String KEYCLOAK_SERVER_URL = System.getProperty("KEYCLOAK_SERVER_URL", "http://localhost:8180/auth");
    private final static String KEYCLOAK_CLIENT_ID = System.getProperty("KEYCLOAK_CLIENT_ID", "arttime");
    private final static String KEYCLOAK_REALM = System.getProperty("KEYCLOAK_REALM", "master");
    private final static String KEYCLOAK_LOGIN = System.getProperty("KEYCLOAK_LOGIN", "admin");
    private final static String KEYCLOAK_PASSWORD = System.getProperty("KEYCLOAK_PASSWORD", "password");

    // TODO: Remove this enum
    public enum Locale {
        ENGLISH("en"),
        RUSSIAN("ru");

        private String language;

        Locale(String language) {
            this.language = language;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }

    private Map<Setting.Name, String> valuesBySettingNames;

    public Settings(Map<Setting.Name, String> valuesBySettingNames) {
        this.valuesBySettingNames = valuesBySettingNames;
    }

    public Settings() {
    }

    public String getLdapServerHost() {
        return getAsString(Setting.Name.LDAP_SERVER_HOST);
    }

    public void setLdapServerHost(String ldapServerHost) {
        set(Setting.Name.LDAP_SERVER_HOST, ldapServerHost);
    }

    public Integer getLdapServerPort() {
        return getAsInt(Setting.Name.LDAP_SERVER_PORT);
    }

    public void setLdapServerPort(Integer ldapServerPort) {
        set(Setting.Name.LDAP_SERVER_PORT, ldapServerPort);
    }

    public String getLdapPrincipalUsername() {
        return getAsString(Setting.Name.LDAP_PRINCIPAL_USERNAME);
    }

    public void setLdapPrincipalUsername(String ldapPrincipalUsername) {
        set(Setting.Name.LDAP_PRINCIPAL_USERNAME, ldapPrincipalUsername);
    }

    public String getLdapBindCredentials() {
        return getAsString(Setting.Name.LDAP_BIND_CREDENTIALS);
    }

    public void setLdapBindCredentials(String ldapBindCredentials) {
        set(Setting.Name.LDAP_BIND_CREDENTIALS, ldapBindCredentials);
    }

    public String getLdapUserContextDN() {
        return getAsString(Setting.Name.LDAP_USER_CONTEXT_DN);
    }

    public void setLdapUserContextDN(String ldapUserContextDN) {
        set(Setting.Name.LDAP_USER_CONTEXT_DN, ldapUserContextDN);
    }

    public String getLdapUserNameAttribute() {
        return getAsString(Setting.Name.LDAP_USER_NAME_ATTRIBUTE);
    }

    public void setLdapUserNameAttribute(String ldapUserNameAttribute) {
        set(Setting.Name.LDAP_USER_NAME_ATTRIBUTE, ldapUserNameAttribute);
    }

    public String getLdapMailAttribute() {
        return getAsString(Setting.Name.LDAP_MAIL_ATTRIBUTE);
    }

    public void setLdapMailAttribute(String ldapMailAttribute) {
        set(Setting.Name.LDAP_MAIL_ATTRIBUTE, ldapMailAttribute);
    }

    public String getLdapLastNameAttribute() {
        return getAsString(Setting.Name.LDAP_LAST_NAME_ATTRIBUTE);
    }

    public void setLdapLastNameAttribute(String ldapLastNameAttribute) {
        set(Setting.Name.LDAP_LAST_NAME_ATTRIBUTE, ldapLastNameAttribute);
    }

    public void setLdapFirstNameAttribute(String ldapFirstNameAttribute) {
        set(Setting.Name.LDAP_FIRST_NAME_ATTRIBUTE, ldapFirstNameAttribute);
    }

    public void setLdapDepartmentAttribute(String ldapDepartmentAttribute) {
        set(Setting.Name.LDAP_DEPARTMENT_ATTRIBUTE, ldapDepartmentAttribute);
    }

    public String getLdapDepartmentAttribute() {
        return getAsString(Setting.Name.LDAP_DEPARTMENT_ATTRIBUTE);
    }

    public void setLdapDepartmentFilterDepartmentAttribute(String ldapDepartmentFilterDepartmentAttribute) {
        set(Setting.Name.LDAP_DEPARTMENT_FILTER_DEPARTMENT_ATTRIBUTE, ldapDepartmentFilterDepartmentAttribute);
    }

    public String getLdapDepartmentFilterDepartmentAttribute() {
        return getAsString(Setting.Name.LDAP_DEPARTMENT_FILTER_DEPARTMENT_ATTRIBUTE);
    }

    public String getLdapFirstNameAttribute() {
        return getAsString(Setting.Name.LDAP_FIRST_NAME_ATTRIBUTE);
    }

    public void setTimerHoursInterval(int timerHoursInterval) {
        set(Setting.Name.TIMER_HOURS_INTERVAL, timerHoursInterval);
    }

    public int getTimerHoursInterval() {
        return getAsInt(Setting.Name.TIMER_HOURS_INTERVAL);
    }

    public void setTimerMinutesInterval(int timerMinutesInterval) {
        set(Setting.Name.TIMER_MINUTES_INTERVAL, timerMinutesInterval);
    }

    public int getTimerMinutesInterval() {
        return getAsInt(Setting.Name.TIMER_MINUTES_INTERVAL);
    }

    public Duration getTimerInterval() {
        return Duration
                .ofHours(getTimerHoursInterval())
                .plusMinutes(getTimerMinutesInterval());
    }

    public String getSmtpHostName() {
        return getAsString(Setting.Name.SMTP_HOST_NAME);
    }

    public void setSmtpHostName(String smtpHostName) {
        set(Setting.Name.SMTP_HOST_NAME, smtpHostName);
    }

    public String getSmtpPortNumber() {
        return getAsString(Setting.Name.SMTP_PORT_NUMBER);
    }

    public void setSmtpPortNumber(String smtpPortNumber) {
        set(Setting.Name.SMTP_PORT_NUMBER, smtpPortNumber);
    }

    public String getSmtpUsername() {
        return getAsString(Setting.Name.SMTP_USERNAME);
    }

    public String getSmtpPassword() {
        return getAsString(Setting.Name.SMTP_PASSWORD);
    }

    public void setSmtpUsername(String smtpUsername) {
        set(Setting.Name.SMTP_USERNAME, smtpUsername);
    }

    public void setSmtpPassword(String smtpPassword) {
        set(Setting.Name.SMTP_PASSWORD, smtpPassword);
    }

    public String getSmtpSender() {
        return getAsString(Setting.Name.SMTP_SENDER);
    }

    public void setSmtpSender(String smtpSender) {
        set(Setting.Name.SMTP_SENDER, smtpSender);
    }

    public boolean isEmployeesSynchronizationEnabled() {
        return getAsBoolean(Setting.Name.EMPLOYEE_SYNCHRONIZATION_ENABLED);
    }

    public void setEmployeesSynchronizationEnabled(boolean employeesSynchronizationEnabled) {
        set(Setting.Name.EMPLOYEE_SYNCHRONIZATION_ENABLED, employeesSynchronizationEnabled);
    }

    public boolean isTeamSynchronizationEnabled() {
        return getAsBoolean(Setting.Name.TEAM_SYNCHRONIZATION_ENABLED);
    }

    public void setTeamSynchronizationEnabled(boolean teamSynchronizationEnabled) {
        set(Setting.Name.TEAM_SYNCHRONIZATION_ENABLED, teamSynchronizationEnabled);
    }

    public String getLdapEmployeeFilter() {
        return getAsString(Setting.Name.LDAP_EMPLOYEE_FILTER);
    }

    public void setLdapEmployeeFilter(String ldapEmployeeFilter) {
        set(Setting.Name.LDAP_EMPLOYEE_FILTER, ldapEmployeeFilter);
    }

    public String getLdapGroupMemberFilter() {
        return getAsString(Setting.Name.LDAP_GROUP_MEMBER_FILTER);
    }

    public void setLdapGroupMemberFilter(String ldapGroupMemberFilter) {
        set(Setting.Name.LDAP_GROUP_MEMBER_FILTER, ldapGroupMemberFilter);
    }

    public String getLdapUserFilter() {
        return getAsString(Setting.Name.LDAP_USER_FILTER);
    }

    public void setLdapUserFilter(String ldapUserFilter) {
        set(Setting.Name.LDAP_USER_FILTER, ldapUserFilter);
    }

    public void setLdapDepartmentFilter(String ldapDepartmentFilter) {
        set(Setting.Name.LDAP_DEPARTMENT_FILTER, ldapDepartmentFilter);
    }

    public String getLdapDepartmentFilter() {
        return getAsString(Setting.Name.LDAP_DEPARTMENT_FILTER);
    }

    public String getLdapPrincipalSuffix() {
        return getAsString(Setting.Name.LDAP_PRINCIPAL_SUFFIX);
    }

    public void setLdapPrincipalSuffix(String ldapPrincipalSuffix) {
        set(Setting.Name.LDAP_PRINCIPAL_SUFFIX, ldapPrincipalSuffix);
    }

    public String getApplicationBaseUrl() {
        return getAsString(Setting.Name.APPLICATION_BASE_URL);
    }

    public void setApplicationBaseUrl(String applicationBaseUrl) {
        set(Setting.Name.APPLICATION_BASE_URL, applicationBaseUrl);
    }

    public String getHelpPageUrl() {
        return getAsString(Setting.Name.HELP_PAGE_URL);
    }

    public void setHelpPageUrl(String helpPageUrl) {
        set(Setting.Name.HELP_PAGE_URL, helpPageUrl);
    }

    public String getEmployeeTrackingSystemName() {
        return getAsString(Setting.Name.EMPLOYEE_TRACKING_SYSTEM_NAME);
    }

    public void setEmployeeTrackingSystemName(String name) {
        set(Setting.Name.EMPLOYEE_TRACKING_SYSTEM_NAME, name);
    }

    public String getTeamTrackingSystemName() {
        return getAsString(Setting.Name.TEAM_TRACKING_SYSTEM_NAME);
    }

    public void setTeamTrackingSystemName(String name) {
        set(Setting.Name.TEAM_TRACKING_SYSTEM_NAME, name);
    }

    public String getDepartmentTrackingSystemName() {
        return getAsString(Setting.Name.DEPARTMENT_TRACKING_SYSTEM_NAME);
    }

    public void setDepartmentTrackingSystemName(String name) {
        set(Setting.Name.DEPARTMENT_TRACKING_SYSTEM_NAME, name);
    }

    public Map<String, String> getEmployeeMappingParams() {
        Map<String, String> result = new HashMap<>();
        if (getLdapUserNameAttribute() != null) {
            result.put(getLdapUserNameAttribute(), "userName");
        }
        if (getLdapFirstNameAttribute() != null) {
            result.put(getLdapFirstNameAttribute(), "firstName");
        }
        if (getLdapLastNameAttribute() != null) {
            result.put(getLdapLastNameAttribute(), "lastName");
        }
        if (getLdapMailAttribute() != null) {
            result.put(getLdapMailAttribute(), "email");
        }
        if (getLdapDepartmentAttribute() != null) {
            result.put(getLdapDepartmentAttribute(), "department");
        }
        return result;
    }

    public Map<Setting.Name, String> getValuesBySettingNames() {
        return valuesBySettingNames;
    }

    public void setValuesBySettingNames(Map<Setting.Name, String> valuesBySettingNames) {
        this.valuesBySettingNames = valuesBySettingNames;
    }

    protected String getAsString(Setting.Name name) {
        return valuesBySettingNames.getOrDefault(name, null);
    }

    protected int getAsInt(Setting.Name name) {
        String stringValue = getAsString(name);
        int value = 0;
        try {
            value = Integer.parseInt(stringValue);
        } catch (Exception ignored) {
        }
        return value;
    }

    protected boolean getAsBoolean(Setting.Name name) {
        String stringValue = getAsString(name);
        return Boolean.parseBoolean(stringValue);
    }

    protected void set(Setting.Name name, String value) {
        valuesBySettingNames.put(name, value);
    }

    protected void set(Setting.Name name, int value) {
        this.set(name, Integer.toString(value));
    }

    protected void set(Setting.Name name, boolean value) {
        this.set(name, Boolean.toString(value));
    }

    public String getKeycloakUserDepartmentAttribute() {
        return getAsString(Setting.Name.KEYCLOAK_USER_DEPARTMENT_ATTRIBUTE);
    }

    public void setKeycloakUserDepartmentAttribute(String keycloakUserDepartmentAttribute) {
        set(Setting.Name.KEYCLOAK_USER_DEPARTMENT_ATTRIBUTE, keycloakUserDepartmentAttribute);
    }

    public String getKeycloakServerUrl() {
        return KEYCLOAK_SERVER_URL;
    }

    public String getKeycloakRealm() {
        return KEYCLOAK_REALM;
    }

    public String getKeycloakUserName() {
        return KEYCLOAK_LOGIN;
    }

    public String getKeycloakPassword() {
        return KEYCLOAK_PASSWORD;
    }

    public String getKeycloakClientId() {
        return KEYCLOAK_CLIENT_ID;
    }

    public Integer getKeycloakCacheRefreshIntervalMinutes() {
        return getAsInt(Setting.Name.KEYCLOAK_CACHE_REFRESH_INTERVAL_MINUTES);
    }

    public void setKeycloakCacheRefreshIntervalMinutes(Integer minutes) {
        set(Setting.Name.KEYCLOAK_CACHE_REFRESH_INTERVAL_MINUTES, minutes);
    }

    public Duration getKeycloakCacheRefreshInterval() {
        return Duration.ofMinutes(getKeycloakCacheRefreshIntervalMinutes());
    }

}
