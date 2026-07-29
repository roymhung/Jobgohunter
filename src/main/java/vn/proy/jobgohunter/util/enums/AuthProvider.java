package vn.proy.jobgohunter.util.enums;

import java.util.Locale;

public enum AuthProvider {
    LOCAL,
    GOOGLE,
    GITHUB,
    FACEBOOK;

    public static AuthProvider fromRegistrationId(String registrationId) {
        if (registrationId == null || registrationId.isBlank()) {
            return LOCAL;
        }
        return switch (registrationId.toLowerCase(Locale.ROOT)) {
            case "google" -> GOOGLE;
            case "github" -> GITHUB;
            case "facebook" -> FACEBOOK;
            default -> LOCAL;
        };
    }

    public static boolean isOAuthRegistration(String registrationId) {
        if (registrationId == null) {
            return false;
        }
        String id = registrationId.toLowerCase(Locale.ROOT);
        return "google".equals(id) || "github".equals(id) || "facebook".equals(id);
    }
}
