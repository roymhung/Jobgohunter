package vn.proy.jobgohunter.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import vn.proy.jobgohunter.config.VnpayProperties;
import vn.proy.jobgohunter.util.error.IdInvalidException;

@Service
public class VnpayPaymentService {

    private static final Logger log = LoggerFactory.getLogger(VnpayPaymentService.class);
    private static final TimeZone VN_TIMEZONE = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");

    private final VnpayProperties props;

    public VnpayPaymentService(VnpayProperties props) {
        this.props = props;
    }

    public String createPaymentUrl(long amountVnd, String txnRef, String orderInfo, String clientIp)
            throws IdInvalidException {
        if (!props.isEnabled()) {
            throw new IdInvalidException("Thanh toán VNPay chưa được bật");
        }
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", props.getVersion());
        params.put("vnp_Command", props.getCommand());
        params.put("vnp_TmnCode", props.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amountVnd * 100L));
        params.put("vnp_CurrCode", props.getCurrCode());
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", props.getOrderType());
        params.put("vnp_Locale", props.getLocale());
        params.put("vnp_ReturnUrl", props.getReturnUrl());
        params.put("vnp_IpAddr", normalizeClientIp(clientIp));

        Calendar cal = Calendar.getInstance(VN_TIMEZONE);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        fmt.setTimeZone(VN_TIMEZONE);
        params.put("vnp_CreateDate", fmt.format(cal.getTime()));

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(urlEncode(fieldValue));
                query.append(urlEncode(fieldName)).append('=').append(urlEncode(fieldValue));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String secureHash = hmacSha512(props.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);
        String url = props.getPayUrl() + "?" + query;
        log.info("VNPay pay url created txnRef={}", txnRef);
        return url;
    }

    public VnpayCallbackResult verifyCallback(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return VnpayCallbackResult.invalid();
        }
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) {
            return VnpayCallbackResult.invalid();
        }
        Map<String, String> copy = new HashMap<>(params);
        copy.remove("vnp_SecureHash");
        String expected = hmacSha512(props.getHashSecret(), hashAllFields(copy));
        if (!expected.equalsIgnoreCase(receivedHash)) {
            return VnpayCallbackResult.invalid();
        }
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");
        long amount = 0;
        try {
            amount = Long.parseLong(params.getOrDefault("vnp_Amount", "0")) / 100L;
        } catch (NumberFormatException ignored) {
            // keep 0
        }
        boolean success = "00".equals(responseCode);
        return new VnpayCallbackResult(success, txnRef, transactionNo, amount, responseCode);
    }

    public String buildTxnRef(Long subscriptionId) {
        return subscriptionId + "" + System.currentTimeMillis() % 1_000_000_000L;
    }

    public String buildOrderInfo(Long orderId) {
        return "Thanh toan don hang " + orderId;
    }

    public String buildFrontendResultUrl(Long orderId, boolean success) {
        String base = props.getFrontendResultUrl().replaceAll("/+$", "");
        return base + "/" + orderId + (success ? "?status=success" : "?status=failed");
    }

    private static String hashAllFields(Map<String, String> fields) {
        List<String> names = new ArrayList<>(fields.keySet());
        Collections.sort(names);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = names.iterator();
        while (itr.hasNext()) {
            String name = itr.next();
            String value = fields.get(name);
            if (value != null && !value.isEmpty()) {
                sb.append(name).append('=').append(urlEncode(value));
                if (itr.hasNext()) {
                    sb.append('&');
                }
            }
        }
        return sb.toString();
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }

    private static String normalizeClientIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "127.0.0.1";
        }
        if ("0:0:0:0:0:0:0:1".equals(clientIp) || "::1".equals(clientIp)) {
            return "127.0.0.1";
        }
        int comma = clientIp.indexOf(',');
        if (comma > 0) {
            return clientIp.substring(0, comma).trim();
        }
        return clientIp;
    }

    private static String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("VNPay HMAC error", e);
        }
    }

    public record VnpayCallbackResult(
            boolean valid,
            boolean success,
            String txnRef,
            String transactionNo,
            long amountVnd,
            String responseCode) {

        public static VnpayCallbackResult invalid() {
            return new VnpayCallbackResult(false, false, null, null, 0, null);
        }

        public VnpayCallbackResult(boolean success, String txnRef, String transactionNo, long amountVnd, String responseCode) {
            this(true, success, txnRef, transactionNo, amountVnd, responseCode);
        }
    }
}
