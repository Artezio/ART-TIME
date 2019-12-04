package com.artezio.javax.jpa.abac;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.interceptor.Interceptor;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

//@Alternative
//@AbacContext("")
//@Interceptor
//@Priority(value = Interceptor.Priority.PLATFORM_AFTER)
//public class SecuredAbacContextInterceptor extends AbacContextInterceptor {
//    @PersistenceContext(unitName = "test-pu")
//    protected void setEntityManager(EntityManager entityManager) {
//        super.setEntityManager(entityManager);
//    }
//}
