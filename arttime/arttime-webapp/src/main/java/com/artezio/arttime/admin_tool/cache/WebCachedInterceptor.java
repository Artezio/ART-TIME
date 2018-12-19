package com.artezio.arttime.admin_tool.cache;

import com.artezio.arttime.admin_tool.cache.WebCached.Scope;

import javax.faces.context.FacesContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@WebCached
@Interceptor
public class WebCachedInterceptor {

    @AroundInvoke
    public Object process(InvocationContext invocationContext) throws Exception {
		Method method = invocationContext.getMethod();
		Object targetInstance = invocationContext.getTarget();
		Class<?> targetClass = targetInstance.getClass();
		WebCached annotation = method.getAnnotation(WebCached.class) != null
				? method.getAnnotation(WebCached.class)
				: targetClass.getAnnotation(WebCached.class);

		Map<String, Object> contextProperties = getContextProperties(annotation.scope());
		if(contextProperties == null) return invocationContext.proceed();
		String cacheId = "_webCache:" + targetClass.getName();
		CacheKey cacheKey = new CacheKey(targetClass, method, invocationContext.getParameters());

		if (!annotation.resetCache()) {
			Map<String, Object> cache = getCache(contextProperties, cacheId);
			Object value = cache.get(cacheKey.toString());
			if (value == null) {
				value = invocationContext.proceed();
				cache.put(cacheKey.toString(), value);
			}
			return value;
		} else {
			contextProperties.remove(cacheId);
			return invocationContext.proceed();
		}
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getCache(Map<String, Object> contextProperties, String key) {
		if (contextProperties.get(key) == null) {
		    contextProperties.put(key, new HashMap<String, Object>());
		}
		return (Map<String, Object>) contextProperties.get(key);
    }

    protected Map<String, Object> getContextProperties(Scope contextScope) {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context == null) return null;
		if (Scope.APPLICATION_SCOPED == contextScope) {
		    return context.getExternalContext().getApplicationMap();
		}
		if (Scope.SESSION_SCOPED == contextScope) {
		    return context.getExternalContext().getSessionMap();
		}
		if (Scope.VIEW_SCOPED == contextScope) {
		    return context.getViewRoot().getViewMap();
		}
		return context.getExternalContext().getRequestMap();
    }

    class CacheKey {
        private Class<?> targetClass;
        private Method method;
        private Object[] parameters;

        CacheKey(Class<?> targetClass, Method method, Object[] parameters) {
            this.targetClass = targetClass;
            this.method = method;
            this.parameters = parameters;
        }

        ///CLOVER:OFF
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((targetClass == null) ? 0 : targetClass.hashCode());
            result = prime * result + ((method == null) ? 0 : method.hashCode());
            result = prime * result + Arrays.hashCode(parameters);
            return result;
        }
        ///CLOVER:ON

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CacheKey other = (CacheKey) obj;
            if (targetClass == null) {
                if (other.targetClass != null)
                    return false;
            } else if (!targetClass.equals(other.targetClass))
                return false;
            if (method == null) {
                if (other.method != null)
                    return false;
            } else if (!method.equals(other.method))
                return false;
            if (!Arrays.equals(parameters, other.parameters))
                return false;
            return true;
        }

    }

}
