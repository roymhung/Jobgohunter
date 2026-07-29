package vn.proy.jobgohunter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jobgohunter.password-reset")
public record PasswordResetProperties(String frontendUrl, int tokenValidityMinutes) {
}
