package vn.proy.jobgohunter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.proy.jobgohunter.service.EmailService;
import vn.proy.jobgohunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/email")
    @ApiMessage("Send simple email")
    public String sendSimpleEmail() {
        // this.emailService.sendEmailSync("royhung123cxz@gmail.com",
        // "Testing send email from Spring Boot",
        // "<h1><b>Hello World test send email</b></h1>", false, true);

        this.emailService.sendEmailFromTemplateSync("royhung123cxz@gmail.com",
                "Testing send email from Spring Boot", "job.html");
        return "ok";
    }
}
