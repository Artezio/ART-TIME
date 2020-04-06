package com.artezio.arttime.datamodel;

import com.artezio.arttime.security.auth.UserRoles;
import com.artezio.javax.jpa.abac.AbacRule;
import com.artezio.javax.jpa.abac.ParamValue;
import org.apache.commons.lang.WordUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.artezio.arttime.security.AbacContexts.*;

@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@FilterDef(name = "Employee.defaultFilter", parameters = {
        @ParamDef(name = "callerUsername", type = "string"),
        @ParamDef(name = "isExec", type = "boolean"),
        @ParamDef(name = "isSystem", type = "boolean"),
        @ParamDef(name = "isIntegrationClient", type = "boolean"),
        @ParamDef(name = "isProjectManager", type = "boolean"),
        @ParamDef(name = "isOfficeManager", type = "boolean"),
        @ParamDef(name = "isAccountant", type = "boolean")
})
@FilterDef(name = "Employee.canBeManaged", parameters = {
        @ParamDef(name = "callerUsername", type = "string"),
        @ParamDef(name = "isExec", type = "boolean"),
        @ParamDef(name = "isSystem", type = "boolean"),
        @ParamDef(name = "isOfficeManager", type = "boolean"),
})
@FilterDef(name = "Employee.canBeAccessedBySelf", parameters = {@ParamDef(name = "callerUsername", type = "string")})
@Filter(name = "Employee.defaultFilter", condition = "(:isExec OR :isSystem OR :isIntegrationClient OR :isProjectManager " +
        "OR ((:isOfficeManager OR :isAccountant) AND EXISTS(" +
        "    SELECT 1 FROM employee_accessibledepartments ead WHERE ead.employee_username=:callerUsername AND ead.accessibledepartments=department)))")
@Filter(name = "Employee.canBeManaged", condition = "(:isExec OR :isSystem OR (:isOfficeManager AND EXISTS(" +
        "    SELECT 1 FROM employee_accessibledepartments ead WHERE ead.employee_username=:callerUsername AND ead.accessibledepartments=department)))")
@Filter(name = "Employee.canBeAccessedBySelf", condition = "(username = :callerUsername)")
@AbacRule(
        contexts = VIEW_TIMESHEET,
        filtersUsed = "Employee.canBeAccessedBySelf",
        paramValues = @ParamValue(paramName = "callerUsername", elExpression = "#{sessionContext.getCallerPrincipal().getName()}")
)
@AbacRule(
        filtersUsed = {"Employee.defaultFilter"},
        paramValues = {
                @ParamValue(paramName = "isExec", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.EXEC_ROLE + "')}"),
                @ParamValue(paramName = "isProjectManager", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.PM_ROLE + "')}"),
                @ParamValue(paramName = "isOfficeManager", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.OFFICE_MANAGER + "')}"),
                @ParamValue(paramName = "isSystem", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.SYSTEM_ROLE + "')}"),
                @ParamValue(paramName = "isIntegrationClient", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.INTEGRATION_CLIENT_ROLE + "')}"),
                @ParamValue(paramName = "callerUsername", elExpression = "#{sessionContext.getCallerPrincipal().getName()}"),
                @ParamValue(paramName = "isAccountant", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.ACCOUNTANT + "')}")
        })
@AbacRule(
        contexts = {MANAGE_EMPLOYEES, REPORT_ACTUAL_WORKTIME_PROBLEMS},
        filtersUsed = {"Employee.canBeManaged"},
        paramValues = {
                @ParamValue(paramName = "isExec", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.EXEC_ROLE + "')}"),
                @ParamValue(paramName = "isSystem", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.SYSTEM_ROLE + "')}"),
                @ParamValue(paramName = "isOfficeManager", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.OFFICE_MANAGER + "')}"),
                @ParamValue(paramName = "callerUsername", elExpression = "#{sessionContext.getCallerPrincipal().getName()}"),
        })
public class Employee implements Serializable {

    private static final long serialVersionUID = -6472074386372872022L;
    private static final Integer FULL_WORK_LOAD_HOURS = 8;
    public static final Comparator<Employee> NAME_COMPARATOR = (e1, e2) -> e1.getFullName().compareToIgnoreCase(e2.getFullName());
    public static final String NO_DEPARTMENT = "";

    @Id
    @NotNull
    private String userName;
    @NotEmpty
    private String firstName;
    @NotEmpty
    private String lastName;
    @NotEmpty
    private String email;
    @XmlTransient
    @NotNull 
    private Integer workLoad = 100;
    @NotNull
    private String department;
    @ManyToOne
    @XmlTransient
    private WorkdaysCalendar calendar;
    @ElementCollection
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @XmlTransient
    private Set<String> accessibleDepartments = new HashSet<>();
    private boolean former;

    public Employee() {
        super();
    }

    public Employee(String userName) {
        setUserName(userName);
    }

    public Employee(String userName, String firstName, String lastName, String email) {
        setUserName(userName);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
    }

    public Employee(String userName, String firstName, String lastName, String email, String department) {
        this(userName, firstName, lastName, email);
        setDepartment(department);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName.toLowerCase();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getWorkLoad() {
        return workLoad;
    }

    public void setWorkLoad(Integer workLoad) {
        this.workLoad = workLoad;
    }

    public void setDepartment(String department) {
        this.department = Optional.ofNullable(department)
                .map(this::castDepartmentToNameCase)
                .orElse(NO_DEPARTMENT);
    }

    public String getDepartment() {
        return department;
    }

    public WorkdaysCalendar getCalendar() {
        return calendar;
    }

    public void setCalendar(WorkdaysCalendar calendar) {
        this.calendar = calendar;
    }

    public BigDecimal getWorkLoadHours() {
        if (workLoad != null) {
            BigDecimal percent = new BigDecimal(FULL_WORK_LOAD_HOURS)
                    .divide(new BigDecimal(100));
            BigDecimal result = percent.multiply(new BigDecimal(workLoad));
            return result;
        }
        return null;
    }

    public Set<String> getAccessibleDepartments() {
        return accessibleDepartments;
    }

    public void setAccessibleDepartments(Set<String> accessToDepartments) {
        this.accessibleDepartments = accessToDepartments;
    }

    public boolean canAccessDepartment(String department) {
        return accessibleDepartments.contains(department);
    }

    ///CLOVER:OFF
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }
    ///CLOVER:ON

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Employee other = (Employee) obj;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} '{'userName={1}'}'",
                new Object[]{getClass().getSimpleName(), userName});
    }

    @PreUpdate
    @PrePersist
    public void castDepartmentsToNameCase() {
        department = castDepartmentToNameCase(department);
        Set<String> departments = new HashSet<>(accessibleDepartments);
        departments.stream()
                .peek(oldDepartment -> accessibleDepartments.remove(oldDepartment))
                .map(this::castDepartmentToNameCase)
                .forEach(accessibleDepartments::add);
    }

    private String castDepartmentToNameCase(String department) {
        return WordUtils.capitalizeFully(department, new char[]{'-', ' '});
    }

    public boolean hasAccessTo(String department) {
        return accessibleDepartments.contains(department);
    }

    public void grantAccessToDepartment(String department) {
        this.accessibleDepartments.add(department);
    }

    public void revokeAccessToDepartment(String department) {
        this.accessibleDepartments.remove(department);
    }

    public String getFullName() {
        return lastName + " " + firstName;
    }

    public boolean isFormer() {
        return former;
    }

    public void setFormer(boolean former) {
        this.former = former;
    }
}
