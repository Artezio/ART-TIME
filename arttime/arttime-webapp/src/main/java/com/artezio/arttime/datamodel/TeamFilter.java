package com.artezio.arttime.datamodel;

import org.apache.commons.lang.StringUtils;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.AssertTrue;
import java.text.MessageFormat;

@Embeddable
public class TeamFilter implements java.io.Serializable {
    private static final long serialVersionUID = 3571041891195400830L;

    public enum Type {
        PROJECT_CODES,
        DEPARTMENTS,
        DISABLED,
        BASED_ON_MASTER
    }

    private String value;
    @Enumerated(EnumType.STRING)
    private Type filterType = Type.PROJECT_CODES;

    public TeamFilter() {
    }

    public TeamFilter(Type filterType) {
        this.filterType = filterType;
    }

    public TeamFilter(Type filterType, String value) {
        this.filterType = filterType;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Type getFilterType() {
        return filterType;
    }

    public void setFilterType(Type type) {
        this.filterType = type;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} '{'type={1}, value={2}'}'",
                new Object[]{getClass().getSimpleName(), filterType, value});
    }

    @AssertTrue(message = "Not valid team filter")
    public boolean isValid() {
        return filterType == Type.DISABLED ||
                filterType == Type.BASED_ON_MASTER ||
                filterType == Type.DEPARTMENTS ||
                (!StringUtils.isBlank(value));
    }
}
