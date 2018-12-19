package com.artezio.arttime.services.mailing;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Period;
import com.artezio.arttime.services.NotificationManager;
import static com.artezio.arttime.services.mailing.MailTemplate.TIME_PROBLEM_BODY_FOR_PM;
import static com.artezio.arttime.services.mailing.MailTemplate.TIME_PROBLEM_SUBJECT;
import java.util.Set;

public class TimeProblemsForPmMailBuilder extends MailBuilder {

    public TimeProblemsForPmMailBuilder(MailTemplateManager mailTemplateManager) {
        this.mailTemplateManager = mailTemplateManager;
    }

    public TimeProblemsForPmMailBuilder setRecipient(Employee recipient) {
        putParameter("recipient", recipient);
        return this;
    }

    public TimeProblemsForPmMailBuilder setHourType(HourType hourType) {
        putParameter("hourType", hourType);
        return this;
    }

    public TimeProblemsForPmMailBuilder setPeriod(Period period) {
        putParameter("period", period);
        return this;
    }

    public TimeProblemsForPmMailBuilder setComment(String comment) {
        putParameter("comment", comment);
        return this;
    }

    public TimeProblemsForPmMailBuilder setEmployeeWorkTimeProblems(Set<NotificationManager.WorkTimeProblems> workTimeProblems) {
        putParameter("employeeWorkTimeProblems", workTimeProblems);
        return this;
    }

    public TimeProblemsForPmMailBuilder setUserNames(String userNames) {
        putParameter("userNames", userNames);
        return this;
    }

    public TimeProblemsForPmMailBuilder setAppHost(String appHost) {
        putParameter("appHost", appHost);
        return this;
    }

    public TimeProblemsForPmMailBuilder setSenderEmailAddress(String senderEmailAddress) {
        this.senderEmailAddress = senderEmailAddress;
        return this;
    }

    public TimeProblemsForPmMailBuilder setRecipientEmailAddress(String recipientEmailAddress) {
        this.recipientEmailAddress = recipientEmailAddress;
        return this;
    }

    public Mail build() {
        return super.build(TIME_PROBLEM_SUBJECT.getFileName(), TIME_PROBLEM_BODY_FOR_PM.getFileName());
    }

}
