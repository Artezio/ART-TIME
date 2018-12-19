package com.artezio.arttime.services.mailing;

import java.util.HashMap;
import java.util.Map;

public abstract class MailBuilder {

    protected MailTemplateManager mailTemplateManager;
    protected Map<String, Object> parameters = new HashMap<>();
    protected String senderEmailAddress;
    protected String recipientEmailAddress;

    public Mail build(String subjectTemplateFileName, String bodyTemplateFileName) {
        String subject = mailTemplateManager.getTemplateText(subjectTemplateFileName, parameters);
        String body = mailTemplateManager.getTemplateText(bodyTemplateFileName, parameters);
        return new Mail(subject, body, senderEmailAddress, recipientEmailAddress);
    }

    protected void putParameter(String key, Object value) {
        if (parameters.putIfAbsent(key, value) != null) {
            parameters.replace(key, value);
        }
    }

}
