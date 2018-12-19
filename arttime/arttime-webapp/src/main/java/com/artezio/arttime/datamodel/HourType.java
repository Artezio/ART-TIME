package com.artezio.arttime.datamodel;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Comparator;

@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@Table(uniqueConstraints = {
	@UniqueConstraint(name = "constraint_unique_hourtype_name", columnNames = {"type"})
})
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class HourType implements Serializable {

    private static final long serialVersionUID = -7365565539438274143L;
    public static final int DEFAULT_PRIORITY = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String type;
    private boolean actualTime;
    @NotNull
    private int priority = DEFAULT_PRIORITY;

    @XmlTransient
    public static final Comparator<HourType> ACTUALTIME_TYPE_COMPARATOR = Comparator.comparing(HourType::isActualTime, Comparator.reverseOrder())
                                                                                    .thenComparing(HourType::getPriority, Comparator.reverseOrder())
                                                                                    .thenComparing(HourType::getType, String.CASE_INSENSITIVE_ORDER);

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HourType() {
    }

    public HourType(String type) {
        this.type = type;
    }

    public void setActualTime(boolean isActualTime) {
        this.actualTime = isActualTime;
    }

    public boolean isActualTime() {
        return actualTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    ///CLOVER:OFF
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (actualTime ? 1231 : 1237);
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
        HourType other = (HourType) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (actualTime != other.actualTime)
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
        return MessageFormat.format("{0} '{'id={1}, type={2}'}'",
                new Object[]{getClass().getSimpleName(), id, type});
    }
}
