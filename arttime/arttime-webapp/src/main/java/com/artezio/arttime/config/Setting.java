package com.artezio.arttime.config;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class Setting implements Serializable {

    public enum Name {
        LDAP_SERVER_HOST,
        LDAP_SERVER_PORT,
        LDAP_PRINCIPAL_USERNAME,
        LDAP_BIND_CREDENTIALS,
        LDAP_PRINCIPAL_SUFFIX,
        LDAP_USER_CONTEXT_DN,
        LDAP_USER_NAME_ATTRIBUTE,

        LDAP_MAIL_ATTRIBUTE,
        LDAP_FIRST_NAME_ATTRIBUTE,
        LDAP_LAST_NAME_ATTRIBUTE,
        LDAP_DEPARTMENT_ATTRIBUTE,
        LDAP_EMPLOYEE_FILTER,
        LDAP_GROUP_MEMBER_FILTER,
        LDAP_USER_FILTER,
        LDAP_DEPARTMENT_FILTER,
        LDAP_DEPARTMENT_FILTER_DEPARTMENT_ATTRIBUTE,

        KEYCLOAK_USER_DEPARTMENT_ATTRIBUTE,
        KEYCLOAK_SERVER_URL,
        KEYCLOAK_REALM,
        KEYCLOAK_USERNAME,
        KEYCLOAK_PASSWORD,
        KEYCLOAK_CLIENT_ID,
        KEYCLOAK_CACHE_REFRESH_INTERVAL_MINUTES,

        SMTP_HOST_NAME,
        SMTP_PORT_NUMBER,
        SMTP_USERNAME,
        SMTP_PASSWORD,
        SMTP_SENDER,

        TIMER_HOURS_INTERVAL,
        TIMER_MINUTES_INTERVAL,

        EMPLOYEE_SYNCHRONIZATION_ENABLED,
        TEAM_SYNCHRONIZATION_ENABLED,
        APPLICATION_BASE_URL,

        HELP_PAGE_URL,

        EMPLOYEE_TRACKING_SYSTEM_NAME,
        DEPARTMENT_TRACKING_SYSTEM_NAME,
        TEAM_TRACKING_SYSTEM_NAME,
    }

    @Id
    @Column(name = "\"key\"", nullable = false)
    @Enumerated(EnumType.STRING)
    private Name name;
    @Size(max = 255)
    private String value;

    public Setting() {}

    public Setting(Name name, String value) {
        this.name = name;
        this.value = value;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Setting {%s}", this.getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Setting other = (Setting) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return 97 + 3 * Objects.hashCode(this.name);
    }

}
