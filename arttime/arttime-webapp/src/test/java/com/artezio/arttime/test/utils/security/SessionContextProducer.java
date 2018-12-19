package com.artezio.arttime.test.utils.security;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@Stateless
public class SessionContextProducer {

    @Resource
    private SessionContext sessionContext;

    @Produces @Named
    public SessionContext getSessionContext() {
        return sessionContext;
    }

}
