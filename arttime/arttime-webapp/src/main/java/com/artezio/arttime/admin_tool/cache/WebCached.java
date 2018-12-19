package com.artezio.arttime.admin_tool.cache;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@InterceptorBinding
@Retention(RUNTIME)
@Target({METHOD, TYPE})
public @interface WebCached {

    enum Scope {
        REQUEST_SCOPED,
        APPLICATION_SCOPED,
        SESSION_SCOPED,
        VIEW_SCOPED
    }

    @Nonbinding Scope scope() default Scope.REQUEST_SCOPED;

    @Nonbinding boolean resetCache() default false;

}
