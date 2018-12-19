package com.artezio.arttime.datamodel;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.artezio.arttime.security.auth.UserRoles;
import com.artezio.javax.jpa.abac.AbacRule;
import com.artezio.javax.jpa.abac.ParamValue;
import org.apache.commons.lang.WordUtils;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import static com.artezio.arttime.security.AbacContexts.*;

@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "constraint_unique_calendar_name", columnNames = {"name"})})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@FilterDef(name = "WorkdaysCalendar.canBeManaged",
        parameters = {
                @ParamDef(name = "isExec", type = "boolean"),
                @ParamDef(name = "isOfficeManager", type = "boolean"),
                @ParamDef(name = "callerUsername", type = "string")})
@Filter(name = "WorkdaysCalendar.canBeManaged", condition = "(:isExec OR (:isOfficeManager AND" +
                "(NOT EXISTS(SELECT 1 FROM workdayscalendar_departments wdna WHERE wdna.workdayscalendar_id = id) " +
                " OR EXISTS (SELECT 1 FROM workdayscalendar_departments wda " +
                "              INNER JOIN employee_accessibledepartments ead " +
                "              ON wda.departments=ead.accessibledepartments " +
                "            WHERE ead.employee_username=:callerUsername AND wda.workdayscalendar_id = id))))")
@AbacRule(
        contexts = MANAGE_CALENDARS,
        filtersUsed = {"WorkdaysCalendar.canBeManaged"},
        paramValues = {
                @ParamValue(paramName = "callerUsername", elExpression = "#{sessionContext.getCallerPrincipal().getName()}"),
                @ParamValue(paramName = "isExec", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.EXEC_ROLE + "')}"),
                @ParamValue(paramName = "isOfficeManager", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.OFFICE_MANAGER + "')}")
        })
public class WorkdaysCalendar implements Serializable {

    private static final long serialVersionUID = 945234047123854630L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String name;
    @XmlElement(name = "department")
    @XmlElementWrapper(name = "departments")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> departments = new HashSet<>();

    public WorkdaysCalendar() {
    }

    public WorkdaysCalendar(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WorkdaysCalendar)) {
            return false;
        }

        WorkdaysCalendar that = (WorkdaysCalendar) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    ///CLOVER:OFF
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    ///CLOVER:ON

    public Set<String> getDepartments() {
        return departments;
    }

    public void setDepartments(Set<String> departments) {
        this.departments = departments.stream().map(this::castDepartmentToNameCase).collect(Collectors.toSet());
    }

    private String castDepartmentToNameCase(String department) {
        return WordUtils.capitalizeFully(department, new char[]{'-', ' '});
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} '{'id={1}, name={2}'}'",
                new Object[]{getClass().getSimpleName(), id, name});
    }
}
