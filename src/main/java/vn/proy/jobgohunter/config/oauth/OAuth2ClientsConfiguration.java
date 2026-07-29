package vn.proy.jobgohunter.config.oauth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class OAuth2ClientsConfiguration {

    private final Environment environment;

    public OAuth2ClientsConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();
        if (OAuthClientSupport.isProviderEnabled(environment, "google")) {
            registrations.add(googleRegistration());
        }
        if (OAuthClientSupport.isProviderEnabled(environment, "github")) {
            registrations.add(githubRegistration());
        }
        if (OAuthClientSupport.isProviderEnabled(environment, "facebook")) {
            registrations.add(facebookRegistration());
        }
        if (registrations.isEmpty()) {
            return new InMemoryClientRegistrationRepository(placeholderRegistration());
        }
        return new InMemoryClientRegistrationRepository(registrations);
    }

    /** OIDC discovery — tự gắn jwkSetUri; fallback tường minh nếu không gọi được Google lúc start. */
    private ClientRegistration googleRegistration() {
        try {
            return ClientRegistrations.fromIssuerLocation("https://accounts.google.com")
                    .registrationId("google")
                    .clientId(OAuthClientSupport.clientId(environment, "google"))
                    .clientSecret(OAuthClientSupport.clientSecret(environment, "google"))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "profile", "email")
                    .clientName("Google")
                    .build();
        } catch (Exception ex) {
            return ClientRegistration.withRegistrationId("google")
                    .clientId(OAuthClientSupport.clientId(environment, "google"))
                    .clientSecret(OAuthClientSupport.clientSecret(environment, "google"))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token")
                    .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                    .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                    .userNameAttributeName("sub")
                    .clientName("Google")
                    .build();
        }
    }

    private ClientRegistration githubRegistration() {
        return ClientRegistration.withRegistrationId("github")
                .clientId(OAuthClientSupport.clientId(environment, "github"))
                .clientSecret(OAuthClientSupport.clientSecret(environment, "github"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("read:user", "user:email")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id")
                .clientName("GitHub")
                .build();
    }

    private ClientRegistration facebookRegistration() {
        return ClientRegistration.withRegistrationId("facebook")
                .clientId(OAuthClientSupport.clientId(environment, "facebook"))
                .clientSecret(OAuthClientSupport.clientSecret(environment, "facebook"))
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("email", "public_profile")
                .authorizationUri("https://www.facebook.com/v18.0/dialog/oauth")
                .tokenUri("https://graph.facebook.com/v18.0/oauth/access_token")
                .userInfoUri("https://graph.facebook.com/v18.0/me?fields=id,name,email")
                .userNameAttributeName("id")
                .clientName("Facebook")
                .build();
    }

    private ClientRegistration placeholderRegistration() {
        return ClientRegistration.withRegistrationId("noop-oauth-not-configured")
                .clientId("disabled")
                .clientSecret("disabled")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://example.com/oauth/authorize")
                .tokenUri("https://example.com/oauth/token")
                .userInfoUri("https://example.com/oauth/userinfo")
                .userNameAttributeName("sub")
                .clientName("Disabled")
                .build();
    }
}
