package com.artezio.javax.el;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.el.BeanELResolver;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.StandardELContext;
import javax.el.ValueExpression;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

@Dependent
public class ElEvaluator {

    @Inject
    private BeanManager beanManager;
    private ExpressionFactory expressionFactory;
    private StandardELContext elContext;
    private final Map<String, ValueExpression> elExpressionsCache = new ConcurrentHashMap<>();

    public Object evaluate(String elExpression) {
        ValueExpression expression = elExpressionsCache.computeIfAbsent(elExpression, (exp) -> compileExpression(exp));
        return expression.getValue(getElContext());
    }

    private ValueExpression compileExpression(String elExpression) {
        return getExpressionFactory().createValueExpression(getElContext(), elExpression, Object.class);
    }

    private ExpressionFactory getExpressionFactory() {
        if (expressionFactory == null) {
            expressionFactory = beanManager.wrapExpressionFactory(ExpressionFactory.newInstance());
        }
        return expressionFactory;
    }

    private ELContext getElContext() {
        if (elContext == null) {
            elContext = new StandardELContext(getExpressionFactory());
            elContext.addELResolver(beanManager.getELResolver());
            elContext.addELResolver(new EjbELResolver());
        }
        return elContext;
    }

    protected class EjbELResolver extends BeanELResolver {

        @Override
        public Object getValue(ELContext context, Object base, Object property) {
            if (null == base && property != null) {
                Object bean = resolveBean((String) property);
                if (null != bean) {
                    context.setPropertyResolved(true);
                    return bean;
                }
            }
            return super.getValue(context, base, property);
        }

        private Set<Bean<?>> getAllCdiManagedBeans() {
            return beanManager.getBeans(Object.class, new AnnotationLiteral<Any>() {
            });
        }

        private Object resolveBean(String beanName) {
            return getAllCdiManagedBeans().stream()
                    .filter(bean -> beanName.equals(getDefaultBeanName(bean)))
                    .map(bean -> beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean)))
                    .findAny()
                    .orElse(null);
        }

        private Object getDefaultBeanName(Bean<?> bean) {
            String className = bean.getBeanClass().getSimpleName();
            char c[] = className.toCharArray();
            c[0] = Character.toLowerCase(c[0]);
            return new String(c);
        }
    }

}
