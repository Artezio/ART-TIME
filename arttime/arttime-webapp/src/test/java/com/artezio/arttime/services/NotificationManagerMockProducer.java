package com.artezio.arttime.services;

import org.mockito.Mockito;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

@Named
public class NotificationManagerMockProducer {
    @Produces
    public NotificationManagerLocal notificationManagerLocal() {
        return Mockito.mock(NotificationManagerLocal.class);
    }
}
