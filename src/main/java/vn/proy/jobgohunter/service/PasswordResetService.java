package vn.proy.jobgohunter.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.proy.jobgohunter.config.PasswordResetProperties;
import vn.proy.jobgohunter.domain.PasswordResetToken;
import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.domain.response.ResResetTokenValidateDTO;
import vn.proy.jobgohunter.repository.PasswordResetTokenRepository;
import vn.proy.jobgohunter.util.enums.AuthProvider;
import vn.proy.jobgohunter.util.error.IdInvalidException;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final PasswordResetTokenRepository tokenRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetProperties properties;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository, UserService userService,
            EmailService emailService, PasswordEncoder passwordEncoder,
            PasswordResetProperties properties) {
        this.tokenRepository = tokenRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    /**
     * Luôn im lặng nếu email không tồn tại / OAuth — tránh lộ thông tin.
     */
    @Transactional
    public void requestReset(String email) {
        String normalized = email.trim();
        User user = userService.handleGetUserByUsername(normalized);
        if (user == null) {
            log.info("Forgot password: no user for email (no mail sent)");
            return;
        }
        if (!canResetWithEmail(user)) {
            log.info("Forgot password: OAuth account, skip mail for {}", normalized);
            return;
        }

        tokenRepository.deleteAllByUser(user);

        String rawToken = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken entity = new PasswordResetToken();
        entity.setUser(user);
        entity.setTokenHash(hashToken(rawToken));
        entity.setExpiresAt(Instant.now().plus(properties.tokenValidityMinutes(), ChronoUnit.MINUTES));
        tokenRepository.save(entity);

        String resetLink = properties.frontendUrl() + "?token=" + rawToken;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetLink,
                properties.tokenValidityMinutes());
        log.info("Password reset email sent to {}", user.getEmail());
    }

    private boolean canResetWithEmail(User user) {
        AuthProvider provider = user.getAuthProvider();
        return provider == null || provider == AuthProvider.LOCAL;
    }

    public ResResetTokenValidateDTO validateToken(String rawToken) {
        ResResetTokenValidateDTO res = new ResResetTokenValidateDTO();
        PasswordResetToken token = findActiveToken(rawToken);
        if (token == null) {
            res.setValid(false);
            res.setMessage("Link không hợp lệ hoặc đã hết hạn");
            return res;
        }
        res.setValid(true);
        res.setMessage("OK");
        return res;
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) throws IdInvalidException {
        PasswordResetToken token = findActiveToken(rawToken);
        if (token == null) {
            throw new IdInvalidException("Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRefreshToken(null);
        userService.saveUser(user);

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }

    private PasswordResetToken findActiveToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }
        return tokenRepository.findByTokenHashAndUsedAtIsNull(hashToken(rawToken.trim()))
                .filter(t -> !t.isExpired())
                .orElse(null);
    }

    static String hashToken(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
