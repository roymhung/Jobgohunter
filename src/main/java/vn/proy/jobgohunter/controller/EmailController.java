package vn.proy.jobgohunter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.proy.jobgohunter.service.EmailService;
import vn.proy.jobgohunter.service.SubscriberService;
import vn.proy.jobgohunter.util.SecurityUtil;
import vn.proy.jobgohunter.util.annotation.ApiMessage;
import vn.proy.jobgohunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class EmailController {

    private final EmailService emailService;
    private final SubscriberService subscriberService;


    public EmailController(EmailService emailService, SubscriberService subscriberService) {
        this.emailService = emailService;
        this.subscriberService = subscriberService;
    }

    @GetMapping("/email")
    @ApiMessage("Send job alert emails to all subscribers (admin/cron)")
    public ResponseEntity<java.util.Map<String, Object>> sendSimpleEmail() {
        int sentCount = this.subscriberService.sendSubscribersEmailJobs();
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("sentCount", sentCount);
        result.put("message", sentCount > 0
                ? "Đã gửi email job mới tới " + sentCount + " subscriber"
                : "Không gửi — không có job mới kể từ lần email trước");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/email/me")
    @ApiMessage("Send job alert email to current user only")
    public ResponseEntity<java.util.Map<String, Object>> sendEmailForCurrentUser()
            throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        if (email.isEmpty()) {
            throw new IdInvalidException("Bạn cần đăng nhập để nhận email job");
        }

        int jobCount = this.subscriberService.sendSubscriberEmailJobsByEmail(email);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("email", email);
        result.put("jobCount", jobCount);
        result.put("sent", jobCount > 0);
        result.put("message", jobCount > 0
                ? "Đã gửi " + jobCount + " job mới khớp skill tới " + email
                : "Không có job mới kể từ lần gửi trước — hệ thống tự gửi lúc 8h sáng khi có job mới");
        return ResponseEntity.ok(result);
    }
}
