package vn.proy.jobgohunter.service;

import org.junit.jupiter.api.Test;

import vn.proy.jobgohunter.config.VnpayProperties;

class VnpayPaymentServiceTest {

    @Test
    void printDemoPaymentUrl() throws Exception {
        VnpayProperties props = new VnpayProperties();
        props.setReturnUrl("https://sandbox.vnpayment.vn/merchant_webapp/merchant.html");
        props.setIpnUrl("https://sandbox.vnpayment.vn/merchant_webapp/merchant.html");
        VnpayPaymentService svc = new VnpayPaymentService(props);
        String url = svc.createPaymentUrl(199000L, "1234567890", "Thanh toan goi Pro", "127.0.0.1");
        System.out.println("VNPay URL (demo return):");
        System.out.println(url);
    }
}
