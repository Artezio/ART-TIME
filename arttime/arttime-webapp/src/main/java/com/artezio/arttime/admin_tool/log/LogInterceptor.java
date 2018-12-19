package com.artezio.arttime.admin_tool.log;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import org.apache.commons.beanutils.PropertyUtils;

@Log
@Interceptor
public class LogInterceptor {

    @Inject
    private Principal principal;

    @AroundInvoke
    public Object process(InvocationContext invocationContext) throws Exception {
        Method method = invocationContext.getMethod();
        Log annotation = method.getAnnotation(Log.class);
        Level level = Level.parse(annotation.level().name());
        Logger logger = getLogger(invocationContext);
        if (!logger.isLoggable(level)) {
            return invocationContext.proceed();
        }

        Object result;
        String principalName = getPrincipalName();
        if (principalName == null && annotation.principalsOnly()) {
            return invocationContext.proceed();
        }
        String prefix = MessageFormat.format("({0}) {1}", new Object[]{principalName, method.getName()});
        if (!annotation.beforeExecuteMessage().isEmpty()) {
            logger.log(level, MessageFormat.format("{0} - {1}", new Object[]{prefix, annotation.beforeExecuteMessage()}));
        }
        if (annotation.logParams()) {
            Parameter[] metaParams = method.getParameters();
            Object[] parameters = invocationContext.getParameters();
            List<String> paramsAsStr = new ArrayList<>();
            for (int i = 0; i < parameters.length; i++) {
                String paramAsStr = showDetails(metaParams[i]) ? getDetailedString(parameters[i])
                        : String.valueOf(parameters[i]);
                paramsAsStr.add(paramAsStr);
            }
            logger.log(level, MessageFormat.format("{0}({1})", new Object[]{prefix, String.join(", ", paramsAsStr)}));
        }

        try {
            result = invocationContext.proceed();
        } catch (Exception e) {
            logger.log(Level.SEVERE, MessageFormat.format("Thrown exception {0}: {1}. Details in server stack trace.",
                    e.getClass(), e.getMessage()));
            throw e;
        }

        if (annotation.logResult()) {
            logger.log(level,
                    MessageFormat.format("{0} -  Result is  {1}", new Object[]{prefix, String.valueOf(result)}));
        }
        if (!annotation.afterExecuteMessage().isEmpty()) {
            logger.log(level,
                    MessageFormat.format("{0} - {1}", new Object[]{prefix, annotation.afterExecuteMessage()}));
        }
        return result;
    }

    private String getPrincipalName() {
        return principal != null ? principal.getName() : null;
    }

    protected Logger getLogger(InvocationContext invocationContext) {
        return Logger.getLogger(invocationContext.getTarget().getClass().getName());
    }

    private boolean showDetails(Parameter parameter) {
        DetailedLogged detailedFormat = parameter.getAnnotation(DetailedLogged.class);
        return detailedFormat != null;
    }

    protected String getDetailedString(Object parameter) {
        return (parameter instanceof Collection) ? getDescriptions((Collection<?>) parameter)
                : getDescription(parameter);
    }

    protected String getDescription(Object object) {
        try {
            Map<String, Object> description = PropertyUtils.describe(object);
            new ArrayList<>(description.keySet()).stream()
                    .filter(attrName -> !PropertyUtils.isWriteable(object, attrName))
                    .forEach(description::remove);

            return MessageFormat.format("{0} {1}",
                    new Object[]{object.getClass().getSimpleName(), String.valueOf(description)});
        } catch (Exception e) {
            return String.valueOf(object);
        }
    }

    private String getDescriptions(Collection<?> collection) {
        List<String> descriptions = collection.stream().map(this::getDescription).collect(Collectors.toList());
        return String.join(", ", descriptions);
    }

}
