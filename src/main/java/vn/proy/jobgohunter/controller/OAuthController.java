package vn.proy.jobgohunter.controller;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.proy.jobgohunter.config.oauth.OAuthClientSupport;
import vn.proy.jobgohunter.domain.response.ResOAuthStatusDTO;
import vn.proy.jobgohunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/auth/oauth")
public class OAuthController {

    private final Environment environment;

    public OAuthController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/status")
    @ApiMessage("OAuth providers configuration status")
    public ResponseEntity<ResOAuthStatusDTO> status() {
        ResOAuthStatusDTO dto = new ResOAuthStatusDTO();
        dto.setGoogle(OAuthClientSupport.isProviderEnabled(environment, "google"));
        dto.setGithub(OAuthClientSupport.isProviderEnabled(environment, "github"));
        dto.setFacebook(OAuthClientSupport.isProviderEnabled(environment, "facebook"));
        if (!dto.isGoogle() && !dto.isGithub() && !dto.isFacebook()) {
            dto.setMessage(
                    "Tạo src/main/resources/application-oauth.local.properties (xem file .example)");
        }
        return ResponseEntity.ok(dto);
    }
}
