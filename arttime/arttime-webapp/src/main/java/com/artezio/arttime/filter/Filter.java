package com.artezio.arttime.filter;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.utils.ListAdapter;
import com.artezio.arttime.web.criteria.RangePeriodSelector;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.artezio.javax.jpa.abac.AbacRule;
import com.artezio.javax.jpa.abac.ParamValue;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

//TODO move to datamodel
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_filter_name_for_owner", columnNames = {"owner", "name" }))
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@FilterDef(name = "Filter.canBeAccessedByOwner",
        parameters = {@ParamDef(name = "callerName", type = "string")})
@org.hibernate.annotations.Filter(name = "Filter.canBeAccessedByOwner", condition = "(owner=:callerName)")
@AbacRule(
        filtersUsed = "Filter.canBeAccessedByOwner",
        paramValues = {@ParamValue(paramName = "callerName", elExpression = "#{sessionContext.getCallerPrincipal().getName()}")})
public class Filter implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String owner;
    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<Project> projects = new HashSet<>();
    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<Employee> employees = new HashSet<>();
    @ElementCollection
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<String> departments = new HashSet<>();
    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<HourType> hourTypes = new HashSet<>();
    @Valid
    @Transient
    private RangePeriodSelector rangePeriodSelector = new RangePeriodSelector();
    @Transient
    private Boolean approved;
    @Transient
    private Boolean predefined = false;

    public Filter() {
    }

    public Filter(String name, String owner, Boolean predefined) {
        this.name = name;
        this.owner = owner;
        this.predefined = predefined;
    }

    public Filter(String name, String owner, Boolean predefined, List<Project> projects) {
        this(name, owner, predefined);
        this.projects = new HashSet<>(projects);
    }

    public Filter(Filter filter) {
        this.approved = filter.approved;
        this.departments = filter.departments;
        this.employees = filter.employees;
        this.hourTypes = filter.hourTypes;
        this.name = filter.name;
        this.owner = filter.owner;
        this.projects = filter.projects;
        this.rangePeriodSelector = filter.rangePeriodSelector;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<Project> getProjects() {
        return new ListAdapter<>(projects);
    }

    public void setProjects(List<Project> projects) {
        this.projects = new HashSet<>(projects);
    }

    public List<Employee> getEmployees() {
        return new ListAdapter<>(employees);
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = new HashSet<>(employees);
    }

    public List<String> getDepartments() {
        return new ListAdapter<>(departments);
    }

    public void setDepartments(List<String> departments) {
        this.departments = new HashSet<>(departments);
    }

    public List<HourType> getHourTypes() {
        return new ListAdapter<>(hourTypes);
    }

    public void setHourTypes(List<HourType> hourTypes) {
        this.hourTypes = new HashSet<>(hourTypes);
    }

    public RangePeriodSelector getRangePeriodSelector() {
        return rangePeriodSelector;
    }

    public void setRangePeriodSelector(RangePeriodSelector rangePeriodSelector) {
        this.rangePeriodSelector = rangePeriodSelector;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} '{'name={1}, owner={2}'}'",
                new Object[] { getClass().getSimpleName(), name, owner });
    }
    
    //TODO: 'containsProject' is enough, isn't it?
    public boolean containsAtLeastOneProject() {
        return !(projects == null || projects.isEmpty());
    }

    public boolean containsAtLeastOneHourType() {
        return !(hourTypes == null || hourTypes.isEmpty());
    }

    public boolean containsAtLeastOneEmployee() {
        return !(employees == null || employees.isEmpty());
    }

    public boolean containsAtLeastOneDepartment() {
        return !(departments == null || departments.isEmpty());
    }

    public Boolean isApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Period getPeriod() {
        return rangePeriodSelector.getPeriod();
    }

    public Boolean isPredefined() {
        return predefined;
    }

    public void setPredefined(Boolean predefined) {
        this.predefined = predefined;
    }

    ///CLOVER:OFF
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
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
        Filter other = (Filter) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (owner == null) {
            if (other.owner != null)
                return false;
        } else if (!owner.equals(other.owner))
            return false;
        return true;
    }

    public void setCustomPeriod(Period period) {
        rangePeriodSelector.setCustomPeriod();
        rangePeriodSelector.getPeriod().setStart(period.getStart());
        rangePeriodSelector.getPeriod().setFinish(period.getFinish());
    }

    public List<Project> getMasterProjects() {
        return projects.stream().filter(p -> p.isMasterProject()).collect(Collectors.toList());
    }
}
