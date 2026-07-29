package vn.proy.jobgohunter.service.auth;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.repository.UserRepository;
import vn.proy.jobgohunter.util.enums.AuthProvider;

@Service
public class OAuthUserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuthUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!AuthProvider.isOAuthRegistration(registrationId)) {
            return oauth2User;
        }

        resolveOAuthUser(registrationId, oauth2User);
        return oauth2User;
    }

    /**
     * Tìm hoặc tạo user DB sau OAuth (dùng chung loadUser + success handler).
     */
    @Transactional
    public User resolveOAuthUser(String registrationId, OAuth2User oauth2User) {
        AuthProvider provider = AuthProvider.fromRegistrationId(registrationId);
        String providerId = resolveProviderId(registrationId, oauth2User);
        Map<String, Object> attributes = oauth2User.getAttributes();

        User user = userRepository.findByAuthProviderAndProviderId(provider, providerId);
        if (user == null) {
            String email = resolveEmail(registrationId, attributes, providerId);
            user = userRepository.findByEmail(email);
            if (user == null) {
                user = createOAuthUser(provider, providerId, email, attributes);
            } else {
                linkOAuthAccount(user, provider, providerId);
            }
        } else {
            updateNameIfPresent(user, attributes);
        }
        return user;
    }

    private String resolveProviderId(String registrationId, OAuth2User oauth2User) {
        String fromName = oauth2User.getName();
        if (fromName != null && !fromName.isBlank()) {
            return String.valueOf(fromName);
        }
        Map<String, Object> attributes = oauth2User.getAttributes();
        Object sub = attributes.get("sub");
        if (sub != null) {
            return String.valueOf(sub);
        }
        Object id = attributes.get("id");
        if (id != null) {
            return String.valueOf(id);
        }
        throw new OAuth2AuthenticationException(
                "OAuth provider id missing for " + registrationId);
    }

    private User createOAuthUser(AuthProvider provider, String providerId, String email,
            Map<String, Object> attributes) {
        User user = new User();
        user.setEmail(email);
        user.setName(resolveName(attributes));
        user.setAuthProvider(provider);
        user.setProviderId(providerId);
        user.setPassword(passwordEncoder.encode("oauth-" + UUID.randomUUID()));
        return userRepository.save(user);
    }

    private void linkOAuthAccount(User user, AuthProvider provider, String providerId) {
        user.setAuthProvider(provider);
        user.setProviderId(providerId);
        userRepository.save(user);
    }

    private void updateNameIfPresent(User user, Map<String, Object> attributes) {
        String name = resolveName(attributes);
        if (name != null && !name.isBlank() && !name.equals(user.getName())) {
            user.setName(name);
            userRepository.save(user);
        }
    }

    private String resolveEmail(String registrationId, Map<String, Object> attributes,
            String providerId) {
        Object emailAttr = attributes.get("email");
        if (emailAttr instanceof String email && !email.isBlank()) {
            return email;
        }
        if ("github".equalsIgnoreCase(registrationId)) {
            Object login = attributes.get("login");
            if (login instanceof String ghLogin && !ghLogin.isBlank()) {
                return ghLogin + "@users.noreply.github.com";
            }
        }
        return registrationId + "_" + providerId + "@oauth.jobgohunter.local";
    }

    private String resolveName(Map<String, Object> attributes) {
        Object name = attributes.get("name");
        if (name instanceof String s && !s.isBlank()) {
            return s;
        }
        Object login = attributes.get("login");
        if (login instanceof String s && !s.isBlank()) {
            return s;
        }
        return "OAuth User";
    }
}
