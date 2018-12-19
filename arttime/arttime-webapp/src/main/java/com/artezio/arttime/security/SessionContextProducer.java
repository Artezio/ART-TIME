package com.artezio.arttime.security;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

public class SessionContextProducer {
    @Resource
    private SessionContext sessionContext;
    
    @Produces @Named
    public SessionContext getSessionContext() {
        return sessionContext;
    }
}
