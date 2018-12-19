package com.artezio.arttime.services.integration;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Documented
@Retention(RUNTIME)
@Target( { TYPE, METHOD, PARAMETER, FIELD })
public @interface TrackingSystemImplementation {
}
