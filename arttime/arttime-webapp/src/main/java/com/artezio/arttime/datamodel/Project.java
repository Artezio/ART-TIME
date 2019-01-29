package com.artezio.arttime.datamodel;

import com.artezio.arttime.datamodel.TeamFilter.Type;
import com.artezio.arttime.security.auth.UserRoles;
import com.artezio.arttime.utils.ListAdapter;
import com.artezio.javax.jpa.abac.AbacRule;
import com.artezio.javax.jpa.abac.ParamValue;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

import static com.artezio.arttime.security.AbacContexts.*;

@Entity
@XmlAccessorType(XmlAccessType.PROPERTY)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(uniqueConstraints = {@UniqueConstraint(name = "uniqueProjectCodes", columnNames = {"masterIdOrEmptyString", "code"})})
@FilterDef(name = "Project.visibleInTimesheet", parameters = {@ParamDef(name = "callerUsername", type = "string")})
@FilterDef(name = "Project.defaultFilter", parameters = {
        @ParamDef(name = "callerUsername", type = "string"),
        @ParamDef(name = "isExec", type = "boolean"),
        @ParamDef(name = "isSystem", type = "boolean"),
        @ParamDef(name = "isIntegrationClient", type = "boolean"),
        @ParamDef(name = "isProjectManager", type = "boolean"),
        @ParamDef(name = "isOfficeManager", type = "boolean"),
        @ParamDef(name = "isAccountant", type = "boolean")
})
@FilterDef(name = "Project.canBeManaged",
        parameters = {
                @ParamDef(name = "callerUsername", type = "string"),
                @ParamDef(name = "isSystem", type = "boolean"),
                @ParamDef(name = "isExec", type = "boolean"),
                @ParamDef(name = "isProjectManager", type = "boolean")})
@Filter(name = "Project.visibleInTimesheet", condition = "(status = 'ACTIVE' AND EXISTS(SELECT 1 FROM Project_Employee pe WHERE pe.project_id=id AND pe.team_userName=:callerUsername))")
@Filter(name = "Project.defaultFilter",
        condition = "(:isExec OR :isIntegrationClient OR :isSystem " +
                "OR (:isProjectManager AND EXISTS(" +
                "  SELECT 1 FROM Project_Manager pm INNER JOIN Project prj ON prj.id=pm.project_id LEFT JOIN Project subproject ON subproject.master_id=prj.id " +
                "  WHERE (prj.id=id OR subproject.id=id) AND pm.manager_username = :callerUsername))" +
                "OR ((:isOfficeManager OR :isAccountant) AND EXISTS(" +
                "  SELECT 1 FROM Project_Employee prjemp INNER JOIN Employee emp ON emp.username = prjemp.team_username " +
                "  INNER JOIN employee_accessibledepartments ead ON emp.department=ead.accessibledepartments WHERE prjemp.project_id = id AND ead.employee_username = :callerUsername)))")
@Filter(name = "Project.canBeManaged",
        condition = "(:isExec OR :isSystem " +
                "OR (:isProjectManager AND EXISTS(" +
                "  SELECT 1 FROM Project_Manager pm INNER JOIN Project prj ON prj.id=pm.project_id LEFT JOIN Project subproject ON subproject.master_id=prj.id " +
                "  WHERE (prj.id=id OR subproject.id=id) AND pm.manager_username = :callerUsername)))")
@AbacRule(
        contexts = VIEW_TIMESHEET,
        filtersUsed = {"Project.visibleInTimesheet"},
        paramValues = {@ParamValue(paramName = "callerUsername", elExpression = "#{sessionContext.getCallerPrincipal().getName()}")})
@AbacRule(
        contexts = MANAGE_PROJECTS,
        filtersUsed = {"Project.canBeManaged"},
        paramValues = {
                @ParamValue(paramName = "callerUsername", elExpression = "#{sessionContext.getCallerPrincipal().getName()}"),
                @ParamValue(paramName = "isExec", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.EXEC_ROLE + "')}"),
                @ParamValue(paramName = "isSystem", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.SYSTEM_ROLE + "')}"),
                @ParamValue(paramName = "isProjectManager", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.PM_ROLE + "')}")
        })
@AbacRule(
        filtersUsed = {"Project.defaultFilter"},
        paramValues = {
                @ParamValue(paramName = "callerUsername", elExpression = "#{sessionContext.getCallerPrincipal().getName()}"),
                @ParamValue(paramName = "isExec", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.EXEC_ROLE + "')}"),
                @ParamValue(paramName = "isIntegrationClient", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.INTEGRATION_CLIENT_ROLE + "')}"),
                @ParamValue(paramName = "isSystem", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.SYSTEM_ROLE + "')}"),
                @ParamValue(paramName = "isProjectManager", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.PM_ROLE + "')}"),
                @ParamValue(paramName = "isOfficeManager", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.OFFICE_MANAGER + "')}"),
                @ParamValue(paramName = "isAccountant", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.ACCOUNTANT + "')}")
        })
public class Project implements Serializable {

    private static final long serialVersionUID = 227312916678442090L;

    public enum Status {
        ACTIVE("com.artezio.arttime.status.active", 1),
        FROZEN("com.artezio.arttime.status.onHold", 2),
        CLOSED("com.artezio.arttime.status.closed", 3);
        private String key;
        private int weight;

        Status(String key, int weight) {
            this.key = key;
            this.weight = weight;
        }

        public String getKey() {
            return key;
        }

        public int getWeight() {
            return weight;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    @Size(max = 1000, message = "{constraints.string.size.message}")
    private String description;

    @Valid
    @NotNull
    private TeamFilter teamFilter;

    @NotNull
    private String code;

    @NotEmpty
    @ManyToMany
    @JoinTable(name = "Project_Manager",
            joinColumns = {@JoinColumn(name = "project_id")},
            inverseJoinColumns = {@JoinColumn(name = "manager_userName")})
    private Set<Employee> managers = new HashSet<>();

    @NotEmpty
    @ManyToMany
    private Set<HourType> accountableHours = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Valid
    @ManyToMany
    private Set<Employee> team = new HashSet<>();

    private boolean allowEmployeeReportTime = true;

    @ManyToOne
    @JoinColumn(updatable = false)
    @Fetch(FetchMode.JOIN)
    private Project master;

    /*
     *   Non-nullable column used together with code field for unique constraint (master, code) in place of nullable master_id field
     *   String representation is chosen instead of numeric, because there are no numbers which could be used as
     *   default (empty) non-occupied value on different RDBMS's
     *   Nullable column master_id cannot be used in a compound unique constraint because pair (null, 1) is not equal to (null, 1) by
     *   ANSI SQL standard (null always not equals to null)
     */
    @Column(nullable = false)
    private String masterIdOrEmptyString = "";

    public static final Comparator<Project> CODE_COMPARATOR = (p1, p2) -> p1.getCode().compareToIgnoreCase(p2.getCode());
    public static final Comparator<Project> DISPLAY_CODE_COMPARATOR = (p1, p2) -> p1.getDisplayCode().compareToIgnoreCase(p2.getDisplayCode());

    @PrePersist
    protected void setMasterIdOrEmptyString() {
        masterIdOrEmptyString = (master == null ? "" : master.getId().toString());
    }

    public Project() {
    }

    public Project(Project master) {
        this.master = master;
    }

    @XmlElement
    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(name = "manager")
    @XmlElementWrapper(name = "managers")
    public List<Employee> getManagers() {
        return new ListAdapter<>(managers);
    }

    public void setManagers(List<Employee> managers) {
        this.managers = new HashSet<>(managers);
    }

    @XmlElement(name = "hourType")
    @XmlElementWrapper(name = "accountableHours")
    public List<HourType> getAccountableHours() {
        return new ListAdapter<>(accountableHours);
    }

    public void setAccountableHours(List<HourType> accountableHours) {
        this.accountableHours = new HashSet<>(accountableHours);
    }

    public Status getStatus() {
        if (isSubproject()) {
            Status masterStatus = master.getStatus();
            return status.getWeight() > masterStatus.getWeight() ? status : masterStatus;
        }
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @XmlTransient
    public List<Employee> getTeam() {
        return new ListAdapter<>(team);
    }

    public void setTeam(List<Employee> team) {
        this.team = new HashSet<>(team);
    }

    @XmlID
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayCode() {
        return isSubproject() ? master.code + " / " + code : code;
    }

    @XmlIDREF
    public Project getMaster() {
        return master;
    }

    public void setMaster(Project master) {
        this.master = master;
    }

    public void addTeamMember(Employee employee) {
        if (!team.contains(employee)) {
            team.add(employee);
        }
    }

    public void removeTeamMember(Employee employee) {
        team.remove(employee);
    }

    public void removeTeamMembers(Collection<Employee> employees) {
        team.removeAll(employees);
    }

    public void addAccountableHours(HourType type) {
        accountableHours.add(type);
    }

    @XmlTransient
    public TeamFilter getTeamFilter() {
        if (teamFilter == null) {
            teamFilter = isSubproject()
                    ? new TeamFilter(Type.BASED_ON_MASTER)
                    : new TeamFilter(Type.PROJECT_CODES);
        }
        return teamFilter;
    }

    public boolean isTeamFilterDisabled() {
        return getTeamFilter().getFilterType() == Type.DISABLED
                || (isValidTeamFilter()
                && getTeamFilter().getFilterType() == Type.BASED_ON_MASTER
                && getMaster().isTeamFilterDisabled());
    }

    public void setTeamFilter(TeamFilter teamFilter) {
        this.teamFilter = teamFilter;
    }

    public boolean isTeamMember(Employee employee) {
        return team.contains(employee);
    }

    ///CLOVER:OFF
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        Project other = (Project) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} '{'id={1}, code={2}'}'",
                new Object[]{getClass().getSimpleName(), id, code});
    }

    public boolean isAllowEmployeeReportTime() {
        return allowEmployeeReportTime;
    }

    public void setAllowEmployeeReportTime(boolean allowEmployeeReportTime) {
        this.allowEmployeeReportTime = allowEmployeeReportTime;
    }

    public void addManager(Employee manager) {
        managers.add(manager);
    }

    public boolean isSubproject() {
        return master != null;
    }

    public boolean canBeManaged(Employee employee) {
        return managers.contains(employee) || ((master != null) && master.canBeManaged(employee));
    }

    public Project getRootProject() {
        return (isSubproject()) ? master.getRootProject() : this;
    }

    @AssertTrue(message = "Not valid team filter")
    public boolean isValidTeamFilter() {
        return isSubproject() || teamFilter.getFilterType() != Type.BASED_ON_MASTER;
    }

    public boolean basedOnImportSettingsOf(Project project) {
        return this.equals(project) ||
                (teamFilter.getFilterType() == Type.BASED_ON_MASTER && master.basedOnImportSettingsOf(project));
    }

    public boolean isMasterProject() {
        return !isSubproject();
    }

}

