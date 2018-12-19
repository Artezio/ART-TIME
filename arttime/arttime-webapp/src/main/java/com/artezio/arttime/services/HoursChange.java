package com.artezio.arttime.services;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

@XmlAccessorType(XmlAccessType.FIELD)
public class HoursChange {
    @XmlElement(required = true)
    @XmlSchemaType(name="date", type=XMLGregorianCalendar.class)
    private Date date;
    @XmlElement(required = true)
    private String projectCode;
    @XmlElement(required = true)
    private String employeeUsername;
    @XmlElement(required = true)
    private Long typeId;
    @XmlElement(required = true)
    private BigDecimal quantityDelta;
    private String comment;

    public HoursChange(String projectCode, Date date, String employeeUsername, Long typeId) {
        this.date = date;
        this.projectCode = projectCode;
        this.employeeUsername = employeeUsername;
        this.typeId = typeId;
    }

    public HoursChange() {}

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getEmployeeUsername() {
        return employeeUsername;
    }

    public void setEmployeeUsername(String employeeUsername) {
        this.employeeUsername = employeeUsername;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public BigDecimal getQuantityDelta() {
        return quantityDelta;
    }

    public void setQuantityDelta(BigDecimal quantityDelta) {
        this.quantityDelta = quantityDelta;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return MessageFormat.format("'{'date={0, date, short}, employee={1}, project={2}, hourType={3}, quantityDelta={4}, comment={5}'}'",
                new Object[]{date, employeeUsername, projectCode, typeId, quantityDelta, comment});
    }
}
