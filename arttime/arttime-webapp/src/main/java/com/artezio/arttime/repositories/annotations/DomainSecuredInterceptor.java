package com.artezio.arttime.repositories.annotations;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import com.artezio.arttime.datamodel.Project;
import com.artezio.arttime.repositories.ProjectRepository;
import com.artezio.arttime.security.auth.UserRoles;


@DomainSecured
@Interceptor
public class DomainSecuredInterceptor {

    @Resource
    private javax.ejb.SessionContext sessionContext;
    @Inject
    private ProjectRepository projectRepository;

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ic) throws Exception {
        Object[] paramValues = ic.getParameters();
        Parameter[] parameters = ic.getMethod().getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(CallerCanManage.class)) {
                checkParameter(parameter, paramValues[i]);
            }
        }
        return ic.proceed();
    }

    private void checkParameter(Parameter parameter, Object value) {
        if (getAccessCheckFunction(parameter).test(value)) {
            throw new InvalidParameterException("Caller has no access to: " + value);
        }
    }

    private Predicate getAccessCheckFunction(Parameter parameter) {
        CompositeKey key = getCompositeKey(parameter);
        Map<CompositeKey, Predicate<?>> accessCheckFunctions = new HashMap<>();
        accessCheckFunctions.put(CompositeKey.forSingleValue(Project.class), p -> sessionContext.isCallerInRole(UserRoles.EXEC_ROLE)
                || projectRepository.query()
                .managedBy(getCallerName())
                .distinct()
                .list().contains(p));
        accessCheckFunctions.put(CompositeKey.forCollection(Project.class), p -> sessionContext.isCallerInRole(UserRoles.EXEC_ROLE)
            || projectRepository.query()
                .managedBy(getCallerName())
                .distinct()
                .list().containsAll((Collection)p));
        return accessCheckFunctions.get(key);
    }

    private String getCallerName() {
    return sessionContext.getCallerPrincipal().getName();
    }

    private CompositeKey getCompositeKey(Parameter parameter) {
        if (parameter.getType().isAssignableFrom(Collection.class)) {
            ParameterizedType parameterizedType = (ParameterizedType)parameter.getParameterizedType();
            Class type = (Class) parameterizedType.getActualTypeArguments()[0];
            return CompositeKey.forCollection(type);
        } else {
            return CompositeKey.forSingleValue(parameter.getType());
        }
    }

    public static class CompositeKey {
        private Boolean isCollection = Boolean.FALSE;
        private Class clazz;

        private CompositeKey(Class clazz, Boolean isCollection) {
            this.clazz = clazz;
            this.isCollection = isCollection;
        }

        static CompositeKey forSingleValue(Class clazz) {
            return new CompositeKey(clazz, Boolean.FALSE);
        }
        static CompositeKey forCollection(Class clazz) {
            return new CompositeKey(clazz, Boolean.TRUE);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
            result = prime * result + ((isCollection == null) ? 0 : isCollection.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CompositeKey other = (CompositeKey) obj;
            if (clazz == null) {
                if (other.clazz != null)
                    return false;
            } else if (!clazz.equals(other.clazz))
                return false;
            if (isCollection == null) {
                if (other.isCollection != null)
                    return false;
            } else if (!isCollection.equals(other.isCollection))
                return false;
            return true;
        }


        }
}
