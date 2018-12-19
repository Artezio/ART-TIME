package com.artezio.arttime.services.mailing;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Period;

import java.math.BigDecimal;

import static com.artezio.arttime.services.mailing.MailTemplate.TIME_PROBLEM_BODY;
import static com.artezio.arttime.services.mailing.MailTemplate.TIME_PROBLEM_SUBJECT;

public class TimeProblemsMailBuilder extends MailBuilder {

    public TimeProblemsMailBuilder(MailTemplateManager mailTemplateManager) {
        this.mailTemplateManager = mailTemplateManager;
    }

    public TimeProblemsMailBuilder setRecipient(Employee recipient) {
        putParameter("recipient", recipient);
        return this;
    }

    public TimeProblemsMailBuilder setHourType(HourType hourType) {
        putParameter("hourType", hourType);
        return this;
    }

    public TimeProblemsMailBuilder setDeviation(BigDecimal deviation) {
        putParameter("deviation", deviation);
        return this;
    }

    public TimeProblemsMailBuilder setPeriod(Period period) {
        putParameter("period", period);
        return this;
    }

    public TimeProblemsMailBuilder setComment(String comment) {
        putParameter("comment", comment);
        return this;
    }

    public TimeProblemsMailBuilder setDeviationDetails(Object[] array) {
        putParameter("deviationDetails", array);
        return this;
    }

    public TimeProblemsMailBuilder setAppHost(String appHost) {
        putParameter("appHost", appHost);
        return this;
    }

    public TimeProblemsMailBuilder setSenderEmailAddress(String senderEmailAddress) {
        this.senderEmailAddress = senderEmailAddress;
        return this;
    }

    public TimeProblemsMailBuilder setRecipientEmailAddress(String recipientEmailAddress) {
        this.recipientEmailAddress = recipientEmailAddress;
        return this;
    }

    public Mail build() {
        return super.build(TIME_PROBLEM_SUBJECT.getFileName(), TIME_PROBLEM_BODY.getFileName());
    }

}
