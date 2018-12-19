package com.artezio.javax.jpa.abac;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Repeatable(AbacRule.List.class)
public @interface AbacRule {
    
    String[] filtersUsed();
    String[] contexts() default {};
    ParamValue[] paramValues() default {};

    @Target({TYPE, METHOD})
    @Retention(RUNTIME)
    @interface List {
        AbacRule[] value();
    }

}
