package com.artezio.javax.jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import com.artezio.javax.jpa.abac.AbacRule;
import com.artezio.javax.jpa.abac.ParamValue;

@Entity
@FilterDef(name = "contextSecured21", parameters = { @ParamDef(name = "name", type = "string") })
@FilterDef(name = "contextSecured22", parameters = { @ParamDef(name = "name", type = "string") })
@Filter(name = "contextSecured21", condition = "name = :name")
@Filter(name = "contextSecured22", condition = "name = :name")
@AbacRule(
        filtersUsed = "contextSecured21", 
        paramValues = {
        @ParamValue(paramName = "name", elExpression = "#{'one'}") })
@AbacRule(contexts = "contextTwo",
        filtersUsed = "contextSecured22",
        paramValues = {
        @ParamValue(paramName = "name", elExpression = "#{'two'}") })
public class DefaultContextSecuredEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;
    private String name;

    public DefaultContextSecuredEntity() {
        super();
    }

    public DefaultContextSecuredEntity(String name) {
        super();
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SecuredEntity [id=" + id + ", name=" + name + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultContextSecuredEntity other = (DefaultContextSecuredEntity) obj;
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
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}