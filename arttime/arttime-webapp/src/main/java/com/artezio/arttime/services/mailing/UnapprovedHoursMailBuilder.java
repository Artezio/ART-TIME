package com.artezio.arttime.services.mailing;

import static com.artezio.arttime.services.mailing.MailTemplate.UNAPPROVED_HOURS_BODY;
import static com.artezio.arttime.services.mailing.MailTemplate.UNAPPROVED_HOURS_SUBJECT;

import com.artezio.arttime.datamodel.Period;

public class UnapprovedHoursMailBuilder extends MailBuilder {

    public UnapprovedHoursMailBuilder(MailTemplateManager mailTemplateManager) {
        this.mailTemplateManager = mailTemplateManager;
    }

    public UnapprovedHoursMailBuilder setPeriod(Period period) {
        putParameter("period", period);
        return this;
    }

    public UnapprovedHoursMailBuilder setAppHost(String appHost) {
        putParameter("appHost", appHost);
        return this;
    }

    public UnapprovedHoursMailBuilder setSenderEmailAddress(String senderEmailAddress) {
        this.senderEmailAddress = senderEmailAddress;
        return this;
    }

    public UnapprovedHoursMailBuilder setRecipientEmailAddress(String recipientEmailAddress) {
        this.recipientEmailAddress = recipientEmailAddress;
        return this;
    }

    public Mail build() {
        return super.build(UNAPPROVED_HOURS_SUBJECT.getFileName(), UNAPPROVED_HOURS_BODY.getFileName());
    }

}
