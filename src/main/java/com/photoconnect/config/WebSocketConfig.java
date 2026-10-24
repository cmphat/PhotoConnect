package com.photoconnect.config;

import com.photoconnect.security.StompSecurityChannelInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Arrays;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final String[] allowedOrigins;
    private final org.springframework.beans.factory.ObjectProvider<StompSecurityChannelInterceptor> stompSecurityChannelInterceptorProvider;

    @Autowired
    public WebSocketConfig(@Value("${photoconnect.websocket.allowed-origins:http://localhost:8080}") String allowedOrigins,
                           org.springframework.beans.factory.ObjectProvider<StompSecurityChannelInterceptor> stompSecurityChannelInterceptorProvider) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
        if (this.allowedOrigins.length == 0 || Arrays.stream(this.allowedOrigins).anyMatch("*"::equals)) {
            throw new IllegalArgumentException("At least one explicit WebSocket origin is required; wildcard origins are not allowed.");
        }
        this.stompSecurityChannelInterceptorProvider = stompSecurityChannelInterceptorProvider;
    }

    public WebSocketConfig(String allowedOrigins) {
        this(allowedOrigins, null);
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS();

        registry.addEndpoint("/ws-raw")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOriginPatterns(allowedOrigins);
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        StompSecurityChannelInterceptor interceptor = stompSecurityChannelInterceptorProvider != null
                ? stompSecurityChannelInterceptorProvider.getIfAvailable()
                : null;
        if (interceptor != null) {
            registration.interceptors(interceptor);
        } else {
            throw new IllegalStateException("CRITICAL: StompSecurityChannelInterceptor must be configured and registered on WebSocket inbound channel.");
        }
    }
}
