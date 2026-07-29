package vn.proy.jobgohunter.service.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.domain.response.ResLoginDTO;
import vn.proy.jobgohunter.service.UserService;
import vn.proy.jobgohunter.util.SecurityUtil;

@Service
public class AuthTokenService {

    private final SecurityUtil securityUtil;
    private final UserService userService;

    @Value("${jobgohunter.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthTokenService(SecurityUtil securityUtil, UserService userService) {
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    public record OAuthTokens(String accessToken, String refreshToken) {
    }

    public OAuthTokens issueTokensForUser(User user) {
        ResLoginDTO res = new ResLoginDTO();
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                user.getId(), user.getEmail(), user.getName(), user.getRole());
        res.setUser(userLogin);
        String accessToken = securityUtil.createAccessToken(user.getEmail(), res);
        String refreshToken = securityUtil.createRefreshToken(user.getEmail(), res);
        userService.updateUserToken(refreshToken, user.getEmail());
        return new OAuthTokens(accessToken, refreshToken);
    }

    public long refreshTokenMaxAgeSeconds() {
        return refreshTokenExpiration;
    }
}
