package vn.proy.jobgohunter.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.proy.jobgohunter.config.InterviewProperties;
import vn.proy.jobgohunter.domain.InterviewSubscription;
import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.domain.request.interview.ReqCreateInterviewOrderDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewOrderDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewPendingOrderDTO;
import vn.proy.jobgohunter.repository.InterviewSubscriptionRepository;
import vn.proy.jobgohunter.repository.UserRepository;
import vn.proy.jobgohunter.util.SecurityUtil;
import vn.proy.jobgohunter.util.enums.InterviewSubscriptionPlanEnum;
import vn.proy.jobgohunter.util.enums.InterviewSubscriptionStatusEnum;
import vn.proy.jobgohunter.util.error.IdInvalidException;

@Service
public class InterviewSubscriptionService {

    private final InterviewProperties props;
    private final UserService userService;
    private final UserRepository userRepository;
    private final InterviewSubscriptionRepository subscriptionRepository;

    public InterviewSubscriptionService(
            InterviewProperties props,
            UserService userService,
            UserRepository userRepository,
            InterviewSubscriptionRepository subscriptionRepository) {
        this.props = props;
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
            return toOrderDto(pending.get(), user.getEmail());
        }
        InterviewSubscription sub = new InterviewSubscription();
        sub.setUserId(user.getId());
        sub.setPlanCode(plan);
        sub.setStatus(InterviewSubscriptionStatusEnum.PENDING);
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
        sub.setTransferSubmittedAt(Instant.now());
        subscriptionRepository.save(sub);
        return toOrderDto(sub, user.getEmail());
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
        Instant now = Instant.now();
        sub.setStatus(InterviewSubscriptionStatusEnum.ACTIVE);
        sub.setStartsAt(now);
        if (sub.getPlanCode() == InterviewSubscriptionPlanEnum.PRO_YEAR) {
            sub.setEndsAt(now.plus(365, ChronoUnit.DAYS));
        } else {
            sub.setEndsAt(null);
        }
        subscriptionRepository.save(sub);
        User owner = userRepository.findById(sub.getUserId()).orElse(null);
        String email = owner != null ? owner.getEmail() : "";
        return toOrderDto(sub, email);
    }

    public List<InterviewSubscription> listPendingOrders() throws IdInvalidException {
        requireSuperAdmin(requireUser());
        return subscriptionRepository.findByStatusOrderByIdDesc(InterviewSubscriptionStatusEnum.PENDING);
    }

    public Optional<InterviewSubscription> findPendingForUser(Long userId) {
        return subscriptionRepository.findFirstByUserIdAndStatusOrderByIdDesc(
                userId, InterviewSubscriptionStatusEnum.PENDING);
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
