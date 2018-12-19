package com.artezio.javax.jpa.abac;

import java.lang.reflect.Method;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@AbacContext("")
@Interceptor
@Priority(value = 0)
public class AbacContextInterceptor {

    @Inject
    private ActiveAbacContext activeContext;
    
    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {
        String activeContextName = activeContext.getName();
        try {
            AbacContext annotation = getMethodOrClassAnnotation(invocationContext);
            String newContextName = annotation.value();
            activeContext.setName(newContextName);
            return invocationContext.proceed();
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
