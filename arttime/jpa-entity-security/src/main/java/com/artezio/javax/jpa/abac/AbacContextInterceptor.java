package com.artezio.javax.jpa.abac;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Method;
import java.util.Optional;

@AbacContext("")
@Interceptor
@Priority(value = Interceptor.Priority.PLATFORM_AFTER)
public class AbacContextInterceptor {

    @Inject
    private ActiveAbacContext activeContext;
    @PersistenceContext
    protected EntityManager entityManager;

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {
        String activeContextName = activeContext.getName();
        try {
            AbacContext annotation = getMethodOrClassAnnotation(invocationContext);
            String newContextName = annotation.value();
            activeContext.setName(newContextName);
            Object result = invocationContext.proceed();
            entityManager.flush();
            return result;
        } finally {
            activeContext.setName(activeContextName);
        }
    }

    private AbacContext getMethodOrClassAnnotation(InvocationContext invocationContext) {
        Method method = invocationContext.getMethod();
        return Optional
                .ofNullable(method.getAnnotation(AbacContext.class))
                .orElse(method.getDeclaringClass().getAnnotation(AbacContext.class));
    }

}
