package com.artezio.arttime.services.mailing;

import org.mockito.Mockito;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

@Named
public class MailTemplateManagerMockProducer {
    @Produces
    public MailTemplateManager mailTemplateManager() {
        return Mockito.mock(MailTemplateManager.class);
    }
}
