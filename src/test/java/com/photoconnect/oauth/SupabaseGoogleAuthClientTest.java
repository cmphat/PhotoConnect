package com.photoconnect.oauth;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SupabaseGoogleAuthClientTest {

    @Test
    void authorizationUrlUsesGooglePkceAndConfiguredCallback() {
        SupabaseGoogleAuthClient client = client(RestClient.builder());
        String url = client.buildAuthorizationUrl("challenge-value", "state-value");
        assertTrue(url.startsWith("https://project.supabase.co/auth/v1/authorize?"));
        assertTrue(url.contains("provider=google"));
        assertTrue(url.contains("code_challenge=challenge-value"));
        assertTrue(url.contains("code_challenge_method=s256"));
        String redirectTo = UriComponentsBuilder.fromUriString(url).build()
                .getQueryParams().getFirst("redirect_to");
        redirectTo = URLDecoder.decode(redirectTo, StandardCharsets.UTF_8);
        assertEquals("http://localhost:8080/auth/google/callback?state=state-value", redirectTo);
    }

    @Test
    void codeIsExchangedServerSideAndIdentityIsRefetchedAndVerified() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("https://project.supabase.co/auth/v1/token?grant_type=pkce"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("apikey", "anon-key"))
                .andExpect(content().json("{\"auth_code\":\"code\",\"code_verifier\":\"verifier\"}"))
                .andRespond(withSuccess("{\"access_token\":\"supabase-access-token\"}", MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("https://project.supabase.co/auth/v1/user"))
                .andExpect(method(HttpMethod.GET)).andExpect(header("Authorization", "Bearer supabase-access-token"))
                .andRespond(withSuccess("""
                        {"email":"person@example.com","email_confirmed_at":"2026-01-01T00:00:00Z",
                         "user_metadata":{"full_name":"Verified Person"},
                         "identities":[{"id":"identity-id","provider":"google",
                           "identity_data":{"sub":"google-123","email":"person@example.com"}}]}
                        """, MediaType.APPLICATION_JSON));
        VerifiedExternalIdentity identity = client(builder).exchangeAndVerify("code", "verifier");
        assertEquals("google-123", identity.subject());
        assertEquals("person@example.com", identity.email());
        assertEquals("Verified Person", identity.fullName());
        server.verify();
    }

    @Test
    void nonGoogleIdentityIsRejectedEvenAfterTokenExchange() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://project.supabase.co/auth/v1/token?grant_type=pkce"))
                .andRespond(withSuccess("{\"access_token\":\"token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://project.supabase.co/auth/v1/user"))
                .andRespond(withSuccess("{\"email\":\"person@example.com\",\"email_confirmed_at\":\"yes\",\"identities\":[]}", MediaType.APPLICATION_JSON));
        assertThrows(ExternalProviderException.class, () -> client(builder).exchangeAndVerify("code", "verifier"));
    }

    private SupabaseGoogleAuthClient client(RestClient.Builder builder) {
        return new SupabaseGoogleAuthClient("https://project.supabase.co", "anon-key",
                "http://localhost:8080/auth/google/callback", builder);
    }
}
