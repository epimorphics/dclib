package com.epimorphics.dclib;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import org.junit.jupiter.api.extension.*;

public class EndpointAvailabilityCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        final var optional = findAnnotation(context.getElement(), SkipWhenEndpointUnavailable.class);
        if (optional.isPresent()) {
            final SkipWhenEndpointUnavailable annotation = optional.get();
            final String uri = annotation.uri();
            boolean result = false;
            try {
                org.apache.jena.http.HttpOp.httpHead(uri);
                result = true;
            } catch (Exception e) {
                result = false;
            }
            if (result) {
                return ConditionEvaluationResult.enabled("Server present and responding");
            } else {
                return ConditionEvaluationResult.disabled("Server not responding with 2xx status code");
            }
        }
        return ConditionEvaluationResult.enabled("No assumptions, moving on...");
    }

}