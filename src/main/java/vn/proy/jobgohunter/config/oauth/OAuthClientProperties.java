package vn.proy.jobgohunter.config.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jobgohunter.oauth2")
public record OAuthClientProperties(String frontendRedirectUrl) {
}
