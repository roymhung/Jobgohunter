package vn.proy.jobgohunter.service;



import java.time.Instant;

import java.time.temporal.ChronoUnit;

import java.util.List;

import java.util.Optional;

import java.util.regex.Matcher;

import java.util.regex.Pattern;

import java.util.stream.Collectors;



import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import vn.proy.jobgohunter.config.InterviewProperties;

import vn.proy.jobgohunter.config.VnpayProperties;

import vn.proy.jobgohunter.domain.InterviewSubscription;

import vn.proy.jobgohunter.domain.User;

import vn.proy.jobgohunter.domain.request.interview.ReqCreateInterviewOrderDTO;

import vn.proy.jobgohunter.domain.response.interview.ResInterviewOrderDTO;

import vn.proy.jobgohunter.domain.response.interview.ResInterviewPendingOrderDTO;

import vn.proy.jobgohunter.domain.response.interview.ResVnpayPaymentDTO;

import vn.proy.jobgohunter.repository.InterviewSubscriptionRepository;

import vn.proy.jobgohunter.repository.UserRepository;

import vn.proy.jobgohunter.service.VnpayPaymentService.VnpayCallbackResult;

import vn.proy.jobgohunter.util.SecurityUtil;

import vn.proy.jobgohunter.util.enums.InterviewPaymentMethodEnum;

import vn.proy.jobgohunter.util.enums.InterviewSubscriptionPlanEnum;

import vn.proy.jobgohunter.util.enums.InterviewSubscriptionStatusEnum;

import vn.proy.jobgohunter.util.error.IdInvalidException;



@Service

public class InterviewSubscriptionService {



    private static final Pattern TXN_REF_ORDER = Pattern.compile("^JGH(\\d+)T");



    private final InterviewProperties props;

    private final VnpayProperties vnpayProps;

    private final VnpayPaymentService vnpayPaymentService;

    private final EmailService emailService;

    private final UserService userService;

    private final UserRepository userRepository;

    private final InterviewSubscriptionRepository subscriptionRepository;



    public InterviewSubscriptionService(

            InterviewProperties props,

            VnpayProperties vnpayProps,

            VnpayPaymentService vnpayPaymentService,

            EmailService emailService,

            UserService userService,

            UserRepository userRepository,

            InterviewSubscriptionRepository subscriptionRepository) {

        this.props = props;

        this.vnpayProps = vnpayProps;

        this.vnpayPaymentService = vnpayPaymentService;

        this.emailService = emailService;

        this.userService = userService;

        this.userRepository = userRepository;

        this.subscriptionRepository = subscriptionRepository;

    }



    public ResInterviewPendingOrderDTO mapPending(InterviewSubscription s) {

        if (s == null) {

            return null;

        }

        ResInterviewPendingOrderDTO dto = new ResInterviewPendingOrderDTO();

        dto.setId(s.getId());

        dto.setPlanCode(s.getPlanCode());

        dto.setStatus(s.getStatus());

        dto.setTransferSubmitted(s.getTransferSubmittedAt() != null);

        dto.setPaymentMethod(s.getPaymentMethod());

        return dto;

    }



    @Transactional

    public ResInterviewOrderDTO createOrder(ReqCreateInterviewOrderDTO req) throws IdInvalidException {

        User user = requireUser();

        if (subscriptionRepository.findActiveForUser(user.getId(), InterviewSubscriptionStatusEnum.ACTIVE).isPresent()) {

            throw new IdInvalidException("Bạn đã có gói Pro đang hiệu lực");

        }

        InterviewSubscriptionPlanEnum plan = parsePlan(req.getPlan());

        Optional<InterviewSubscription> pending = subscriptionRepository

                .findFirstByUserIdAndStatusOrderByIdDesc(user.getId(), InterviewSubscriptionStatusEnum.PENDING);

        if (pending.isPresent()) {

            InterviewSubscription sub = pending.get();

            if (sub.getPlanCode() != plan) {

                sub.setPlanCode(plan);

                sub.setTransferSubmittedAt(null);

                sub.setVnpayTxnRef(null);

                sub.setVnpayTransactionNo(null);

                sub.setPaymentMethod(InterviewPaymentMethodEnum.BANK_TRANSFER);

                subscriptionRepository.save(sub);

            }

            return toOrderDto(sub, user.getEmail());

        }

        InterviewSubscription sub = new InterviewSubscription();

        sub.setUserId(user.getId());

        sub.setPlanCode(plan);

        sub.setStatus(InterviewSubscriptionStatusEnum.PENDING);

        sub.setPaymentMethod(InterviewPaymentMethodEnum.BANK_TRANSFER);

        sub.setCreatedAt(Instant.now());

        subscriptionRepository.save(sub);

        return toOrderDto(sub, user.getEmail());

    }



    @Transactional

    public ResInterviewOrderDTO markTransferSubmitted(Long orderId) throws IdInvalidException {

        User user = requireUser();

        InterviewSubscription sub = subscriptionRepository.findByIdAndUserId(orderId, user.getId())

                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đơn"));

        if (sub.getStatus() != InterviewSubscriptionStatusEnum.PENDING) {

            throw new IdInvalidException("Đơn không ở trạng thái chờ duyệt");

        }

        sub.setPaymentMethod(InterviewPaymentMethodEnum.BANK_TRANSFER);

        sub.setTransferSubmittedAt(Instant.now());

        subscriptionRepository.save(sub);

        return toOrderDto(sub, user.getEmail());

    }



    @Transactional

    public ResVnpayPaymentDTO initiateVnpayPayment(Long orderId, String clientIp) throws IdInvalidException {

        if (!vnpayProps.isEnabled()) {

            throw new IdInvalidException("Thanh toán VNPay chưa được bật");

        }

        if (!vnpayProps.isMerchantConfigured()) {

            throw new IdInvalidException(
                    "VNPay chưa có merchant key. Đăng ký tại https://sandbox.vnpayment.vn/devreg/ "
                            + "rồi điền TMN Code + Hash Secret vào application-vnpay.local.properties");

        }

        User user = requireUser();

        InterviewSubscription sub = subscriptionRepository.findByIdAndUserId(orderId, user.getId())

                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đơn"));

        if (sub.getStatus() != InterviewSubscriptionStatusEnum.PENDING) {

            throw new IdInvalidException("Đơn không ở trạng thái chờ thanh toán");

        }

        long amount = amountFor(sub.getPlanCode());

        String txnRef = vnpayPaymentService.buildTxnRef(sub.getId());

        String orderInfo = vnpayPaymentService.buildOrderInfo(sub.getId());

        String payUrl = vnpayPaymentService.createPaymentUrl(amount, txnRef, orderInfo, clientIp);



        sub.setPaymentMethod(InterviewPaymentMethodEnum.VNPAY);

        sub.setVnpayTxnRef(txnRef);

        subscriptionRepository.save(sub);



        ResVnpayPaymentDTO dto = new ResVnpayPaymentDTO();

        dto.setOrderId(sub.getId());

        dto.setPayUrl(payUrl);

        dto.setTxnRef(txnRef);

        return dto;

    }



    @Transactional

    public ResInterviewOrderDTO getOrder(Long orderId) throws IdInvalidException {

        User user = requireUser();

        InterviewSubscription sub = subscriptionRepository.findByIdAndUserId(orderId, user.getId())

                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đơn"));

        return toOrderDto(sub, user.getEmail());

    }



    @Transactional

    public Long handleVnpayCallback(VnpayCallbackResult result) {

        if (!result.valid() || result.txnRef() == null) {

            return parseOrderIdFromTxnRef(result.txnRef());

        }

        Optional<InterviewSubscription> subOpt = subscriptionRepository.findByVnpayTxnRef(result.txnRef());

        InterviewSubscription sub = subOpt.orElseGet(() -> {

            Long id = parseOrderIdFromTxnRef(result.txnRef());

            if (id == null) {

                return null;

            }

            return subscriptionRepository.findById(id).orElse(null);

        });

        if (sub == null) {

            return parseOrderIdFromTxnRef(result.txnRef());

        }

        if (sub.getStatus() == InterviewSubscriptionStatusEnum.ACTIVE) {

            return sub.getId();

        }

        if (result.success() && sub.getStatus() == InterviewSubscriptionStatusEnum.PENDING) {

            long expected = amountFor(sub.getPlanCode());

            if (result.amountVnd() == 0 || result.amountVnd() == expected) {

                activateSubscriptionInternal(sub, result.transactionNo());

                notifyPaymentSuccess(sub);

            }

        }

        return sub.getId();

    }



    @Transactional

    public ResInterviewOrderDTO activateOrder(Long orderId) throws IdInvalidException {

        User admin = requireUser();

        requireSuperAdmin(admin);

        InterviewSubscription sub = subscriptionRepository.findById(orderId)

                .orElseThrow(() -> new IdInvalidException("Không tìm thấy đơn"));

        if (sub.getStatus() != InterviewSubscriptionStatusEnum.PENDING) {

            throw new IdInvalidException("Chỉ kích hoạt đơn PENDING");

        }

        activateSubscription(sub, null);

        User owner = userRepository.findById(sub.getUserId()).orElse(null);

        String email = owner != null ? owner.getEmail() : "";

        return toOrderDto(sub, email);

    }



    public List<ResInterviewOrderDTO> listPendingOrderDtos() throws IdInvalidException {

        requireSuperAdmin(requireUser());

        return subscriptionRepository.findByStatusOrderByIdDesc(InterviewSubscriptionStatusEnum.PENDING).stream()

                .map(sub -> {

                    User owner = userRepository.findById(sub.getUserId()).orElse(null);

                    String email = owner != null ? owner.getEmail() : "";

                    ResInterviewOrderDTO dto = toOrderDto(sub, email);

                    dto.setUserId(sub.getUserId());

                    dto.setUserEmail(email);

                    return dto;

                })

                .collect(Collectors.toList());

    }



    public Optional<InterviewSubscription> findPendingForUser(Long userId) {

        return subscriptionRepository.findFirstByUserIdAndStatusOrderByIdDesc(

                userId, InterviewSubscriptionStatusEnum.PENDING);

    }



    private void activateSubscriptionInternal(InterviewSubscription sub, String vnpayTransactionNo) {

        if (sub.getStatus() == InterviewSubscriptionStatusEnum.ACTIVE) {

            return;

        }

        Instant now = Instant.now();

        sub.setStatus(InterviewSubscriptionStatusEnum.ACTIVE);

        sub.setStartsAt(now);

        sub.setPaidAt(now);

        if (vnpayTransactionNo != null) {

            sub.setVnpayTransactionNo(vnpayTransactionNo);

        }

        if (sub.getPlanCode() == InterviewSubscriptionPlanEnum.PRO_YEAR) {

            sub.setEndsAt(now.plus(365, ChronoUnit.DAYS));

        } else {

            sub.setEndsAt(null);

        }

        subscriptionRepository.save(sub);

    }



    private void activateSubscription(InterviewSubscription sub, String vnpayTransactionNo) {

        activateSubscriptionInternal(sub, vnpayTransactionNo);

        notifyPaymentSuccess(sub);

    }



    private void notifyPaymentSuccess(InterviewSubscription sub) {

        User owner = userRepository.findById(sub.getUserId()).orElse(null);

        if (owner == null || owner.getEmail() == null || owner.getEmail().isBlank()) {

            return;

        }

        String planLabel = sub.getPlanCode() == InterviewSubscriptionPlanEnum.PRO_LIFETIME

                ? "Pro Trọn đời"

                : "Pro Năm";

        String method = sub.getPaymentMethod() == InterviewPaymentMethodEnum.VNPAY ? "VNPay" : "Chuyển khoản";

        emailService.sendPaymentSuccessEmail(

                owner.getEmail(),

                owner.getName(),

                planLabel,

                amountFor(sub.getPlanCode()),

                method,

                sub.getId());

    }



    private Long parseOrderIdFromTxnRef(String txnRef) {

        if (txnRef == null) {

            return null;

        }

        Matcher m = TXN_REF_ORDER.matcher(txnRef);

        if (m.find()) {

            return Long.parseLong(m.group(1));

        }

        return null;

    }



    private ResInterviewOrderDTO toOrderDto(InterviewSubscription sub, String email) {

        ResInterviewOrderDTO dto = new ResInterviewOrderDTO();

        dto.setId(sub.getId());

        dto.setPlanCode(sub.getPlanCode());

        dto.setStatus(sub.getStatus());

        dto.setAmountVnd(amountFor(sub.getPlanCode()));

        dto.setBankName(props.getBankName());

        dto.setBankAccount(props.getBankAccount());

        dto.setBankHolder(props.getBankHolder());

        dto.setTransferContent(transferContent(sub.getPlanCode(), email));

        dto.setCreatedAt(sub.getCreatedAt());

        dto.setTransferSubmittedAt(sub.getTransferSubmittedAt());

        dto.setPaymentMethod(sub.getPaymentMethod());

        dto.setPaidAt(sub.getPaidAt());

        dto.setVnpayEnabled(vnpayProps.isEnabled() && vnpayProps.isMerchantConfigured());

        return dto;

    }



    private long amountFor(InterviewSubscriptionPlanEnum plan) {

        return plan == InterviewSubscriptionPlanEnum.PRO_LIFETIME

                ? props.getProLifetimePriceVnd()

                : props.getProYearPriceVnd();

    }



    private String transferContent(InterviewSubscriptionPlanEnum plan, String email) {

        String tag = plan == InterviewSubscriptionPlanEnum.PRO_LIFETIME ? "JGH PRO LIFE" : "JGH PRO NAM";

        return tag + " " + (email != null ? email : "").trim();

    }



    private InterviewSubscriptionPlanEnum parsePlan(String plan) throws IdInvalidException {

        if ("lifetime".equalsIgnoreCase(plan)) {

            return InterviewSubscriptionPlanEnum.PRO_LIFETIME;

        }

        if ("year".equalsIgnoreCase(plan)) {

            return InterviewSubscriptionPlanEnum.PRO_YEAR;

        }

        throw new IdInvalidException("Gói không hợp lệ");

    }



    private User requireUser() throws IdInvalidException {

        String username = SecurityUtil.getCurrentUserLogin().orElse("");

        if (username.isBlank()) {

            throw new IdInvalidException("Unauthorized");

        }

        User user = userService.handleGetUserByUsername(username);

        if (user == null) {

            throw new IdInvalidException("User not found");

        }

        return user;

    }



    private void requireSuperAdmin(User user) throws IdInvalidException {

        if (user.getRole() == null || !"SUPER_ADMIN".equals(user.getRole().getName())) {

            throw new IdInvalidException("Forbidden");

        }

    }

}


