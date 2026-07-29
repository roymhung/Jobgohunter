package vn.proy.jobgohunter.config.oauth;

import org.springframework.core.env.Environment;

public final class OAuthClientSupport {

    private OAuthClientSupport() {
    }

    public static boolean isConfiguredClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return false;
        }
        String n = clientId.trim().toLowerCase();
        return !n.startsWith("your-") && !n.equals("disabled") && !n.contains("placeholder");
    }

    public static String clientId(Environment env, String provider) {
        return env.getProperty(
                "spring.security.oauth2.client.registration." + provider + ".client-id");
    }

    public static String clientSecret(Environment env, String provider) {
        return env.getProperty(
                "spring.security.oauth2.client.registration." + provider + ".client-secret");
    }

    public static boolean isProviderEnabled(Environment env, String provider) {
        String secret = clientSecret(env, provider);
        return isConfiguredClientId(clientId(env, provider)) && secret != null && !secret.isBlank();
    }
}
