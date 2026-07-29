package vn.proy.jobgohunter.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;

import vn.proy.jobgohunter.config.oauth.OAuth2LoginFailureHandler;
import vn.proy.jobgohunter.config.oauth.OAuth2LoginSuccessHandler;
import vn.proy.jobgohunter.service.auth.OAuthUserService;
import vn.proy.jobgohunter.util.SecurityUtil;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    @Value("${jobgohunter.jwt.base64-secret}")
    private String jwtKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Xóa bỏ cặp dấu ngoặc nhọn dư thừa ở đây
    // @formatter:off => ngăn format code mặc định
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            OAuthUserService oAuthUserService,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
            OAuth2LoginFailureHandler oAuth2LoginFailureHandler) throws Exception {

        String[] whiteList = {
                "/",
                "/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/register",
                "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password",
                "/api/v1/auth/reset-password/validate",
                "/api/v1/auth/oauth/**",
                "/oauth2/**",
                "/login/oauth2/**",
                "/storage/**",
                "/api/v1/companies/**", "/api/v1/jobs/**",
                "/api/v1/email/**", 
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
        };

        http
                .csrf(c -> c.disable())    
                .cors(Customizer.withDefaults())   
                .authorizeHttpRequests(
                        authz -> authz
                                .requestMatchers(whiteList).permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/companies/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/jobs/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/skills/**").permitAll()

                                .anyRequest().authenticated())
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                // .exceptionHandling(
                // exceptions -> exceptions
                // .authenticationEntryPoint(customAuthenticationEntryPoint) // 401
                // .accessDeniedHandler(new BearerTokenAccessDeniedHandler())) // 403

                .formLogin(f -> f.disable())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(u -> u.userService(oAuthUserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
                new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("permission");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        return token -> {
            try {
                return jwtDecoder.decode(token);
            } catch (Exception e) {
                System.out.println(">>> JWT error: " + e.getMessage());
                throw e;
            }
        };
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length,
                SecurityUtil.JWT_ALGORITHM.getName());
    }
}
