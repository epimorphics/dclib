package com.epimorphics.dclib;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.extension.ExtendWith;

@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(EndpointAvailabilityCondition.class)
public @interface SkipWhenEndpointUnavailable {

    String uri();

}