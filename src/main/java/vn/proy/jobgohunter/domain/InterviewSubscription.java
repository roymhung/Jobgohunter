package vn.proy.jobgohunter.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.proy.jobgohunter.util.enums.InterviewPaymentMethodEnum;
import vn.proy.jobgohunter.util.enums.InterviewSubscriptionPlanEnum;
import vn.proy.jobgohunter.util.enums.InterviewSubscriptionStatusEnum;

@Entity
@Table(name = "interview_subscription")
@Getter
@Setter
public class InterviewSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_code", nullable = false, length = 30)
    private InterviewSubscriptionPlanEnum planCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewSubscriptionStatusEnum status;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "transfer_submitted_at")
    private Instant transferSubmittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private InterviewPaymentMethodEnum paymentMethod;

    @Column(name = "vnpay_txn_ref", length = 64)
    private String vnpayTxnRef;

    @Column(name = "vnpay_transaction_no", length = 32)
    private String vnpayTransactionNo;

    @Column(name = "paid_at")
    private Instant paidAt;
}
