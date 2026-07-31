package vn.proy.jobgohunter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jobgohunter.vnpay")
public class VnpayProperties {

    private boolean enabled = true;
    /** Terminal ID (TMN Code) */
    private String tmnCode = "DEMOV210";
    /** Hash secret */
    private String hashSecret = "RAOEXHYVSDDIIENYWSLDIIZTANXUXZFJ";
    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    /** BE xử lý return từ VNPay rồi redirect FE */
    private String returnUrl = "https://sandbox.vnpayment.vn/merchant_webapp/merchant.html";
    private String ipnUrl = "http://127.0.0.1:8080/api/v1/payments/vnpay/ipn";
    /** FE trang kết quả sau redirect */
    private String frontendResultUrl = "http://localhost:4173/interview/payment/result";
    private String version = "2.1.0";
    private String command = "pay";
    private String orderType = "other";
    private String locale = "vn";
    private String currCode = "VND";
    private int expireMinutes = 30;

    /** Key demo DEMOV210 công khai đã hết hạn — cần merchant riêng từ sandbox.vnpayment.vn/devreg/ */
    public boolean isMerchantConfigured() {
        if (tmnCode == null || tmnCode.isBlank() || hashSecret == null || hashSecret.isBlank()) {
            return false;
        }
        return !"DEMOV210".equalsIgnoreCase(tmnCode.trim());
    }
}
