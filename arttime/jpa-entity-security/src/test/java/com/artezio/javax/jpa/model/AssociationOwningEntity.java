package com.artezio.javax.jpa.model;

import com.artezio.javax.jpa.abac.AbacRule;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@FilterDef(name = "securedOwningEntity")
@Filter(name = "securedOwningEntity", condition = "EXISTS (SELECT 1 FROM Owning_Child oc WHERE oc.owning_id = id)")
@AbacRule(filtersUsed = "securedOwningEntity")
public class AssociationOwningEntity implements Serializable {

    @ManyToMany//(cascade = {CascadeType.REFRESH, CascadeType.DETACH})
    @JoinTable(name = "Owning_Child",
            joinColumns = {@JoinColumn(name = "owning_id")},
            inverseJoinColumns = {@JoinColumn(name = "child_id")})
    private Set<AssociationChildEntity> childEntities = new HashSet<>();

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String value;

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Set<AssociationChildEntity> getChildEntities() {
        return childEntities;
    }

    public void setChildEntities(Set<AssociationChildEntity> childEntities) {
        this.childEntities = childEntities;
    }
}
