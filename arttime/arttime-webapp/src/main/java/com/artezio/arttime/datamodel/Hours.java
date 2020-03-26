package com.artezio.arttime.datamodel;

import com.artezio.arttime.security.auth.UserRoles;
import com.artezio.javax.jpa.abac.AbacRule;
import com.artezio.javax.jpa.abac.ParamValue;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

import static com.artezio.arttime.security.AbacContexts.*;

@Entity
@XmlAccessorType(XmlAccessType.PROPERTY)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "constraint_unique_hours", columnNames = {"date", "employee_userName", "project_id", "type_id"})
})
@FilterDef(name = "Hours.visibleInTimesheet", 
        parameters = {
                @ParamDef(name = "callerUsername", type = "string"),
                @ParamDef(name = "isSystem", type = "boolean")
        })
@FilterDef(name = "Hours.canBeReported", parameters = {@ParamDef(name = "callerUsername", type = "string")})
@FilterDef(name = "Hours.defaultFilter",
        parameters = {
                @ParamDef(name = "callerUsername", type = "string"),
                @ParamDef(name = "isExec", type = "boolean"),
                @ParamDef(name = "isIntegrationClient", type = "boolean"),
                @ParamDef(name = "isProjectManager", type = "boolean"),
                @ParamDef(name = "isOfficeManager", type = "boolean"),
                @ParamDef(name = "isAccountant", type = "boolean")
        })
@Filter(name = "Hours.visibleInTimesheet", condition = "(employee_userName = :callerUsername OR :isSystem)")
@Filter(name = "Hours.canBeReported",
        condition = "((approved = false) AND EXISTS (SELECT 1 FROM Project hp WHERE hp.id = project_id AND hp.allowEmployeeReportTime=true))")
@Filter(name = "Hours.defaultFilter", condition = "(:isExec OR :isIntegrationClient " +
        "OR (:isProjectManager AND EXISTS(" +
        "  SELECT 1 FROM Project_Manager pm INNER JOIN Project prj ON prj.id=pm.project_id LEFT JOIN Project subproject ON subproject.master_id=prj.id " +
        "  WHERE (prj.id=project_id OR subproject.id=project_id) AND pm.manager_userName=:callerUsername)) " +
        "OR ((:isOfficeManager OR :isAccountant) AND EXISTS(" +
        "  SELECT 1 FROM Employee emp INNER JOIN employee_accessibledepartments ead ON emp.department=ead.accessibledepartments " +
        "  WHERE ead.employee_username=:callerUsername AND emp.username=employee_username)))")
@AbacRule(
        contexts = VIEW_TIMESHEET,
        filtersUsed = "Hours.visibleInTimesheet",
        paramValues = {
                @ParamValue(paramName = "callerUsername", elExpression = "#{sessionContext.getCallerPrincipal().getName()}"),
                @ParamValue(paramName = "isSystem", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.SYSTEM_ROLE + "')}")
        })
@AbacRule(
        contexts = REPORT_TIME,
        filtersUsed = {"Hours.visibleInTimesheet", "Hours.canBeReported"},
        paramValues = {
                @ParamValue(paramName = "callerUsername", elExpression = "#{sessionContext.getCallerPrincipal().getName()}"),
                @ParamValue(paramName = "isSystem", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.SYSTEM_ROLE + "')}")
        })
@AbacRule(
        filtersUsed = {"Hours.defaultFilter"},
        paramValues = {
                @ParamValue(paramName = "isExec", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.EXEC_ROLE + "')}"),
                @ParamValue(paramName = "isProjectManager", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.PM_ROLE + "')}"),
                @ParamValue(paramName = "isIntegrationClient", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.INTEGRATION_CLIENT_ROLE + "')}"),
                @ParamValue(paramName = "isOfficeManager", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.OFFICE_MANAGER + "')}"),
                @ParamValue(paramName = "callerUsername", elExpression = "#{sessionContext.getCallerPrincipal().getName()}"),
                @ParamValue(paramName = "isAccountant", elExpression = "#{sessionContext.isCallerInRole('" + UserRoles.ACCOUNTANT + "')}")
        })
public class Hours implements Serializable {

    private static final long serialVersionUID = -2547281134254029051L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(name = "\"date\"")
    @Temporal(TemporalType.DATE)
    private Date date;
    @NotNull
    @ManyToOne
    private Employee employee;
    @Min(value = 0, message = "{constraints.hours.quantity.notnegative.message}")
    private BigDecimal quantity;
    @NotNull
    @ManyToOne
    private HourType type;
    @NotNull
    @ManyToOne(optional = false)
    private Project project;
    private boolean approved;
    @Size(max = 255)
    @Column(name = "\"comment\"")
    private String comment;

    public Hours() {
    }

    public Hours(Date date, HourType type) {
        this.date = date;
        this.type = type;
    }

    public Hours(Project project, Date date, Employee employee, HourType type) {
        this.date = date;
        this.employee = employee;
        this.type = type;
        this.project = project;
    }

    @XmlTransient
    public Long getId() {
        return id;
    }

    public void resetId() {
        this.id = null;
    }

    @XmlSchemaType(name = "date", type = XMLGregorianCalendar.class)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = (BigDecimal.ZERO.equals(quantity))
                ? null
                : quantity;
    }

    @XmlTransient
    public BigDecimal getQuantityNotNullable() {
        return quantity == null
                ? BigDecimal.valueOf(0.0)
                : quantity;
    }

    public HourType getType() {
        return type;
    }

    public void setType(HourType type) {
        this.type = type;
    }

    @XmlTransient
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @XmlElement
    public String getProjectCode() {
        return project.getCode();
    }

    ///CLOVER:OFF
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : DateUtils.truncate(date, Calendar.DAY_OF_MONTH).hashCode());
        result = prime * result
                + ((employee == null) ? 0 : employee.hashCode());
        result = prime * result + ((project == null) ? 0 : project.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Hours other = (Hours) obj;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!DateUtils.isSameDay(date, other.date))
            return false;
        if (employee == null) {
            if (other.employee != null)
                return false;
        } else if (!employee.equals(other.employee))
            return false;
        if (project == null) {
            if (other.project != null)
                return false;
        } else if (!project.equals(other.project))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} '{'date={1, date, short}, employee={2}, project={3}, hourType={4}, quantity={5}, approved={6}, comment={7}'}'",
                new Object[]{getClass().getSimpleName(), date, employee.getUserName(), project.getCode(), type.getType(), quantity, approved, comment});
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void add(BigDecimal quantity) throws IllegalStateException, IllegalArgumentException {
        if (this.approved) {
            throw new IllegalStateException("Trying to add quantity to approved hours");
        }
        BigDecimal finalQuantity = getQuantityNotNullable().add(quantity);
        // Validate prior to persisting to database. Required to avoid case when MySQL persists an invalid entity in transaction
        // and causes immediate EJBTransactionRolledBackException with deeply hidden ConstraintViolationException
        if (finalQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Trying to set negative hours quantity");
        }
        setQuantity(finalQuantity);
    }
}
