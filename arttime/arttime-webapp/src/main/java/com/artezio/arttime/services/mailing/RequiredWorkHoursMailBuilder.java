package com.artezio.arttime.services.mailing;

import static com.artezio.arttime.services.mailing.MailTemplate.REQUIRED_WORK_HOURS_BODY;
import static com.artezio.arttime.services.mailing.MailTemplate.REQUIRED_WORK_HOURS_SUBJECT;

import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Period;

public class RequiredWorkHoursMailBuilder extends MailBuilder {

    public RequiredWorkHoursMailBuilder(MailTemplateManager mailTemplateManager) {
        this.mailTemplateManager = mailTemplateManager;
    }

    public RequiredWorkHoursMailBuilder setHourType(HourType hourType) {
        putParameter("hourType", hourType);
        return this;
    }

    public RequiredWorkHoursMailBuilder setPeriod(Period period) {
        putParameter("period", period);
        return this;
    }

    public RequiredWorkHoursMailBuilder setAppHost(String appHost) {
        putParameter("appHost", appHost);
        return this;
    }

    public RequiredWorkHoursMailBuilder setSenderEmailAddress(String senderEmailAddress) {
        this.senderEmailAddress = senderEmailAddress;
        return this;
    }

    public RequiredWorkHoursMailBuilder setRecipientEmailAddress(String recipientEmailAddress) {
        this.recipientEmailAddress = recipientEmailAddress;
        return this;
    }

    public Mail build() {
        return super.build(REQUIRED_WORK_HOURS_SUBJECT.getFileName(), REQUIRED_WORK_HOURS_BODY.getFileName());
    }

}
