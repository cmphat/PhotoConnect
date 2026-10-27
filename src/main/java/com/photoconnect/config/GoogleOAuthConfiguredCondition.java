package com.photoconnect.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

final class GoogleOAuthConfiguredCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String clientId = context.getEnvironment().getProperty("photoconnect.oauth.google.client-id", "");
        String clientSecret = context.getEnvironment().getProperty("photoconnect.oauth.google.client-secret", "");
        return !clientId.isBlank() && !clientSecret.isBlank();
    }
}
