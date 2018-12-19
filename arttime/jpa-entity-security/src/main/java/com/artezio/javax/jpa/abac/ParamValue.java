package com.artezio.javax.jpa.abac;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

@Retention(RUNTIME)
public @interface ParamValue {
    String paramName();

    String elExpression();
}
