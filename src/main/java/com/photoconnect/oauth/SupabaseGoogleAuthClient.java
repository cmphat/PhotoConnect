package com.photoconnect.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.photoconnect.entity.ExternalAuthProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

@Component
public class SupabaseGoogleAuthClient implements GoogleAuthClient {

    private final String supabaseUrl;
    private final String anonKey;
    private final String redirectUri;
    private final RestClient restClient;

    public SupabaseGoogleAuthClient(
            @Value("${photoconnect.supabase.url:}") String supabaseUrl,
            @Value("${photoconnect.supabase.anon-key:}") String anonKey,
            @Value("${photoconnect.supabase.oauth-redirect-uri:}") String redirectUri,
            RestClient.Builder restClientBuilder) {
        this.supabaseUrl = stripTrailingSlash(supabaseUrl);
        this.anonKey = anonKey == null ? "" : anonKey.trim();
        this.redirectUri = redirectUri == null ? "" : redirectUri.trim();
        this.restClient = restClientBuilder.build();
    }

    @Override
    public String buildAuthorizationUrl(String codeChallenge, String state) {
        requireConfigured();
        String callback = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("state", state).build().encode().toUriString();
        return UriComponentsBuilder.fromUriString(supabaseUrl + "/auth/v1/authorize")
                .queryParam("provider", "google")
                .queryParam("redirect_to", callback)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "s256")
                .build().encode().toUriString();
    }

    @Override
    public VerifiedExternalIdentity exchangeAndVerify(String authorizationCode, String codeVerifier) {
        requireConfigured();
        try {
            JsonNode tokenResponse = restClient.post()
                    .uri(supabaseUrl + "/auth/v1/token?grant_type=pkce")
                    .header("apikey", anonKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("auth_code", authorizationCode, "code_verifier", codeVerifier))
                    .retrieve().body(JsonNode.class);
            String accessToken = requiredText(tokenResponse, "access_token");

            JsonNode user = restClient.get()
                    .uri(supabaseUrl + "/auth/v1/user")
                    .header("apikey", anonKey)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve().body(JsonNode.class);
            return verifiedGoogleIdentity(user);
        } catch (ExternalProviderException ex) {
            throw ex;
        } catch (RestClientException | IllegalArgumentException ex) {
            throw new ExternalProviderException("Supabase authentication is temporarily unavailable.", ex);
        }
    }

    private VerifiedExternalIdentity verifiedGoogleIdentity(JsonNode user) {
        if (user == null || user.isMissingNode() || user.isNull()) {
            throw new ExternalProviderException("Supabase returned an invalid identity.");
        }
        String email = requiredText(user, "email").trim().toLowerCase(Locale.ROOT);
        if (!user.hasNonNull("email_confirmed_at")) {
            throw new ExternalProviderException("The external email address is not verified.");
        }

        JsonNode googleIdentity = null;
        JsonNode identities = user.path("identities");
        if (identities.isArray()) {
            for (JsonNode identity : identities) {
                if ("google".equalsIgnoreCase(identity.path("provider").asText())) {
                    googleIdentity = identity;
                    break;
                }
            }
        }
        if (googleIdentity == null) {
            throw new ExternalProviderException("Supabase did not verify a Google identity.");
        }
        JsonNode identityData = googleIdentity.path("identity_data");
        String subject = identityData.path("sub").asText();
        if (subject.isBlank()) {
            subject = googleIdentity.path("id").asText();
        }
        if (subject.isBlank()) {
            throw new ExternalProviderException("The Google identity has no stable subject.");
        }
        String identityEmail = identityData.path("email").asText(email).trim().toLowerCase(Locale.ROOT);
        if (!email.equals(identityEmail)) {
            throw new ExternalProviderException("The verified identity email is inconsistent.");
        }
        JsonNode metadata = user.path("user_metadata");
        String name = metadata.path("full_name").asText();
        if (name.isBlank()) name = metadata.path("name").asText();
        if (name.isBlank()) name = email.substring(0, email.indexOf('@'));
        return new VerifiedExternalIdentity(ExternalAuthProvider.GOOGLE, subject, email, name.trim());
    }

    private void requireConfigured() {
        if (supabaseUrl.isBlank() || anonKey.isBlank() || redirectUri.isBlank()) {
            throw new ExternalProviderException("Google sign-in is not configured.");
        }
        URI projectUri;
        URI callbackUri;
        try {
            projectUri = URI.create(supabaseUrl);
            callbackUri = URI.create(redirectUri);
        } catch (IllegalArgumentException ex) {
            throw new ExternalProviderException("Google sign-in configuration is invalid.");
        }
        if (!isAllowedUri(projectUri, true) || !isAllowedUri(callbackUri, false)) {
            throw new ExternalProviderException("Google sign-in configuration is invalid.");
        }
    }

    private boolean isAllowedUri(URI uri, boolean projectUrl) {
        if (uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null) return false;
        boolean secure = "https".equalsIgnoreCase(uri.getScheme());
        boolean local = "http".equalsIgnoreCase(uri.getScheme())
                && ("localhost".equalsIgnoreCase(uri.getHost()) || "127.0.0.1".equals(uri.getHost()));
        return (secure || local) && (!projectUrl || uri.getQuery() == null);
    }

    private String requiredText(JsonNode node, String field) {
        String value = node == null ? "" : node.path(field).asText();
        if (value.isBlank()) throw new ExternalProviderException("Supabase returned an invalid authentication response.");
        return value;
    }

    private static String stripTrailingSlash(String value) {
        String result = value == null ? "" : value.trim();
        while (result.endsWith("/")) result = result.substring(0, result.length() - 1);
        return result;
    }
}
