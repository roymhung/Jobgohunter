package vn.proy.jobgohunter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.proy.jobgohunter.domain.request.ReqForgotPasswordDTO;
import vn.proy.jobgohunter.domain.request.ReqResetPasswordDTO;
import vn.proy.jobgohunter.domain.response.ResResetTokenValidateDTO;
import vn.proy.jobgohunter.service.PasswordResetService;
import vn.proy.jobgohunter.util.annotation.ApiMessage;
import vn.proy.jobgohunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/auth")
public class PasswordResetController {

    private static final String FORGOT_MSG =
            "Nếu email tồn tại trong hệ thống, chúng tôi đã gửi hướng dẫn đặt lại mật khẩu.";

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    @ApiMessage("Request password reset email")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ReqForgotPasswordDTO dto) {
        passwordResetService.requestReset(dto.getEmail());
        return ResponseEntity.ok(new MessageResponse(FORGOT_MSG));
    }

    @GetMapping("/reset-password/validate")
    @ApiMessage("Validate password reset token")
    public ResponseEntity<ResResetTokenValidateDTO> validateToken(@RequestParam String token) {
        return ResponseEntity.ok(passwordResetService.validateToken(token));
    }

    @PostMapping("/reset-password")
    @ApiMessage("Reset password with token")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ReqResetPasswordDTO dto)
            throws IdInvalidException {
        passwordResetService.resetPassword(dto.getToken(), dto.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Đặt lại mật khẩu thành công. Vui lòng đăng nhập."));
    }

    public record MessageResponse(String message) {
    }
}
