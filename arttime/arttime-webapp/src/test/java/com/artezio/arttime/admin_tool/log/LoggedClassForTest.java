package com.artezio.arttime.admin_tool.log;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@Cacheable
public class LoggedClassForTest implements Serializable {

    private static final long serialVersionUID = -6472074386372872022L;
    private static final Integer FULL_WORK_LOAD_HOURS = 8;
    public static final Comparator<LoggedClassForTest> NAME_COMPARATOR = (e1, e2) -> e1.getFullName()
            .compareToIgnoreCase(e2.getFullName());

    @Id
    @NotNull
    private String userName;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private String email;
    @XmlTransient
    @NotNull
    private Integer workLoad = 100;
    @NotNull
    private String department;
    private boolean former;

    public LoggedClassForTest() {
        super();
    }

    public LoggedClassForTest(String userName) {
        this.userName = userName;
    }

    public LoggedClassForTest(String userName, String firstName, String lastName, String email) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public BigDecimal getWorkLoadHours() {
        if (workLoad != null) {
            BigDecimal percent = new BigDecimal(FULL_WORK_LOAD_HOURS).divide(new BigDecimal(100));
            BigDecimal result = percent.multiply(new BigDecimal(workLoad));
            return result;
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userName == null) ? 0 : userName.toLowerCase().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LoggedClassForTest other = (LoggedClassForTest) obj;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equalsIgnoreCase(other.userName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} '{'userName={1}'}'", new Object[] { getClass().getSimpleName(), userName });
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
