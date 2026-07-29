package vn.proy.jobgohunter.config.oauth;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.service.UserService;
import vn.proy.jobgohunter.service.auth.AuthTokenService;
import vn.proy.jobgohunter.service.auth.AuthTokenService.OAuthTokens;
import vn.proy.jobgohunter.util.enums.AuthProvider;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuthClientProperties oauthClientProperties;
    private final AuthTokenService authTokenService;
    private final UserService userService;

    public OAuth2LoginSuccessHandler(OAuthClientProperties oauthClientProperties,
            AuthTokenService authTokenService, UserService userService) {
        this.oauthClientProperties = oauthClientProperties;
        this.authTokenService = authTokenService;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        if (!AuthProvider.isOAuthRegistration(registrationId)) {
            redirectError(response, "unsupported_provider");
            return;
        }
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        AuthProvider provider = AuthProvider.fromRegistrationId(registrationId);
        String providerId = String.valueOf(oauth2User.getName());
        User user = userService.findByAuthProviderAndProviderId(provider, providerId);
        if (user == null) {
            redirectError(response, "user_not_found");
            return;
        }
        OAuthTokens tokens = authTokenService.issueTokensForUser(user);
        ResponseCookie cookie = ResponseCookie.from("refresh_token", tokens.refreshToken())
                .httpOnly(true).secure(true).path("/")
                .maxAge(authTokenService.refreshTokenMaxAgeSeconds()).build();
        response.addHeader("Set-Cookie", cookie.toString());
        getRedirectStrategy().sendRedirect(request, response,
                oauthClientProperties.frontendRedirectUrl() + "?access_token="
                        + URLEncoder.encode(tokens.accessToken(), StandardCharsets.UTF_8));
    }

    private void redirectError(HttpServletResponse response, String code) throws IOException {
        response.sendRedirect(oauthClientProperties.frontendRedirectUrl() + "?error="
                + URLEncoder.encode(code, StandardCharsets.UTF_8));
    }
}
