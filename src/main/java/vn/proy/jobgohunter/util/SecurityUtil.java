package vn.proy.jobgohunter.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;


@Service
public class SecurityUtil {

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    @Value("${jobgohunter.jwt.base64-secret}")
    private String jwtKey;

    @Value("${jobgohunter.jwt.token-validity-in-seconds}")
    private String jwtExpiration;



    public void createToken(Authentication authentication) {}
}
