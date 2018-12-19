package com.artezio.arttime.services;

import javax.ejb.ApplicationException;
import javax.xml.bind.annotation.XmlElement;

@ApplicationException(rollback = true)
public class ApplyHoursChangeException extends Exception {

    @XmlElement
    private HoursChange change;

    public ApplyHoursChangeException(String message) {
        super(message);
    }

    public ApplyHoursChangeException(HoursChange change, String message) {
        this(message);
        this.change = change;
    }

    public HoursChange getChange() {
        return change;
    }
}
