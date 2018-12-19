package com.artezio.arttime.admin_tool.log;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@InterceptorBinding
@Retention(RUNTIME)
@Target({METHOD, TYPE})
public @interface Log {

    public enum Level {
        CONFIG, FINE, FINER, FINEST, INFO, OFF, SEVERE, WARNING, ALL
    }

    @Nonbinding Level level() default Level.INFO;

    @Nonbinding String beforeExecuteMessage() default "";

    @Nonbinding String afterExecuteMessage() default "";

    @Nonbinding boolean logResult() default false;

    @Nonbinding boolean logParams() default false;

    @Nonbinding boolean principalsOnly() default false;
}
