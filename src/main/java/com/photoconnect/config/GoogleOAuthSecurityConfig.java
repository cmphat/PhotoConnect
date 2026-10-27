package com.photoconnect.config;

import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.security.GoogleOAuth2FailureHandler;
import com.photoconnect.security.GoogleOidcAuthenticationSuccessHandler;
import com.photoconnect.security.JwtCookieService;
import com.photoconnect.security.TransientOAuth2AuthorizedClientRepository;
import com.photoconnect.service.ExternalAuthService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class GoogleOAuthSecurityConfig {

    @Bean
    @Conditional(GoogleOAuthConfiguredCondition.class)
    ClientRegistrationRepository googleClientRegistrationRepository(GoogleOAuthProperties properties) {
        ClientRegistration google = ClientRegistration.withRegistrationId("google")
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("openid", "profile", "email")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .issuerUri("https://accounts.google.com")
                .clientName("Google")
                .build();
        return new InMemoryClientRegistrationRepository(google);
    }

    @Bean
    OAuth2AuthorizedClientRepository transientOAuth2AuthorizedClientRepository() {
        return new TransientOAuth2AuthorizedClientRepository();
    }

    @Bean
    GoogleOidcAuthenticationSuccessHandler googleOidcAuthenticationSuccessHandler(
            GoogleOAuthProperties properties,
            ExternalAuthService externalAuthService,
            JwtCookieService jwtCookieService,
            PhotographerProfileRepository photographerProfileRepository) {
        return new GoogleOidcAuthenticationSuccessHandler(properties, externalAuthService,
                jwtCookieService, photographerProfileRepository);
    }

    @Bean
    GoogleOAuth2FailureHandler googleOAuth2FailureHandler() {
        return new GoogleOAuth2FailureHandler();
    }

    @Bean
    SecurityFilterChain photoConnectSecurityFilterChain(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> registrations,
            OAuth2AuthorizedClientRepository authorizedClients,
            GoogleOidcAuthenticationSuccessHandler successHandler,
            GoogleOAuth2FailureHandler failureHandler) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        if (registrations.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                    .loginPage("/login")
                    .authorizedClientRepository(authorizedClients)
                    .successHandler(successHandler)
                    .failureHandler(failureHandler));
        }
        return http.build();
    }
}
