package vn.proy.jobgohunter.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import vn.proy.jobgohunter.config.VnpayProperties;
import vn.proy.jobgohunter.service.InterviewSubscriptionService;
import vn.proy.jobgohunter.service.VnpayPaymentService;
import vn.proy.jobgohunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/payments/vnpay")
public class VnpayPaymentController {

    private final VnpayProperties vnpayProperties;
    private final VnpayPaymentService vnpayPaymentService;
    private final InterviewSubscriptionService subscriptionService;

    public VnpayPaymentController(
            VnpayProperties vnpayProperties,
            VnpayPaymentService vnpayPaymentService,
            InterviewSubscriptionService subscriptionService) {
        this.vnpayProperties = vnpayProperties;
        this.vnpayPaymentService = vnpayPaymentService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/return")
    @ApiMessage("VNPay return URL")
    public ResponseEntity<Void> returnUrl(@RequestParam Map<String, String> params) {
        VnpayPaymentService.VnpayCallbackResult result = vnpayPaymentService.verifyCallback(params);
        Long orderId = subscriptionService.handleVnpayCallback(result);
        String redirect;
        if (orderId != null) {
            redirect = vnpayPaymentService.buildFrontendResultUrl(orderId, result.success());
        } else {
            String base = vnpayProperties.getFrontendResultUrl().replaceAll("/+$", "");
            redirect = base + "?status=failed";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(java.net.URI.create(redirect));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/ipn")
    @ApiMessage("VNPay IPN")
    public ResponseEntity<String> ipn(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);
        VnpayPaymentService.VnpayCallbackResult result = vnpayPaymentService.verifyCallback(params);
        if (!result.valid()) {
            return ResponseEntity.ok("RspCode=97&Message=Invalid signature");
        }
        subscriptionService.handleVnpayCallback(result);
        if (result.success()) {
            return ResponseEntity.ok("RspCode=00&Message=Confirm Success");
        }
        return ResponseEntity.ok("RspCode=00&Message=Confirm Success");
    }

    private static Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }
}
