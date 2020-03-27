package com.artezio.arttime.services.mailing;

import static com.artezio.arttime.services.mailing.MailTemplate.INCORRECT_TIMESHEET_BODY;
import static com.artezio.arttime.services.mailing.MailTemplate.INCORRECT_TIMESHEET_SUBJECT;

import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Period;

public class IncorrectTimesheetMailBuilder extends MailBuilder {

    public IncorrectTimesheetMailBuilder(MailTemplateManager mailTemplateManager) {
        this.mailTemplateManager = mailTemplateManager;
    }

    public IncorrectTimesheetMailBuilder setHourType(HourType hourType) {
        putParameter("hourType", hourType);
        return this;
    }

    public IncorrectTimesheetMailBuilder setPeriod(Period period) {
        putParameter("period", period);
        return this;
    }

    public IncorrectTimesheetMailBuilder setAppHost(String appHost) {
        putParameter("appHost", appHost);
        return this;
    }

    public IncorrectTimesheetMailBuilder setSenderEmailAddress(String senderEmailAddress) {
        this.senderEmailAddress = senderEmailAddress;
        return this;
    }

    public IncorrectTimesheetMailBuilder setRecipientEmailAddress(String recipientEmailAddress) {
        this.recipientEmailAddress = recipientEmailAddress;
        return this;
    }

    public Mail build() {
        return super.build(INCORRECT_TIMESHEET_SUBJECT.getFileName(), INCORRECT_TIMESHEET_BODY.getFileName());
    }

}
