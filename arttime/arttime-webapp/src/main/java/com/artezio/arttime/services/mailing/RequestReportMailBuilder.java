package com.artezio.arttime.services.mailing;

import com.artezio.arttime.datamodel.Period;

import static com.artezio.arttime.services.mailing.MailTemplate.REQUEST_REPORT_BODY;
import static com.artezio.arttime.services.mailing.MailTemplate.REQUEST_REPORT_SUBJECT;

public class RequestReportMailBuilder extends MailBuilder {

    public RequestReportMailBuilder(MailTemplateManager mailTemplateManager) {
        this.mailTemplateManager = mailTemplateManager;
    }

    public RequestReportMailBuilder setPeriod(Period period) {
        putParameter("period", period);
        return this;
    }

    public RequestReportMailBuilder setAppHost(String appHost) {
        putParameter("appHost", appHost);
        return this;
    }

    public RequestReportMailBuilder setSenderEmailAddress(String senderEmailAddress) {
        this.senderEmailAddress = senderEmailAddress;
        return this;
    }

    public RequestReportMailBuilder setRecipientEmailAddress(String recipientEmailAddress) {
        this.recipientEmailAddress = recipientEmailAddress;
        return this;
    }

    public Mail build() {
        return super.build(REQUEST_REPORT_SUBJECT.getFileName(), REQUEST_REPORT_BODY.getFileName());
    }

}
