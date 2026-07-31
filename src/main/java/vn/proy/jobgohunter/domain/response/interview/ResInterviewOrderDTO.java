package vn.proy.jobgohunter.domain.response.interview;

import java.time.Instant;

import vn.proy.jobgohunter.util.enums.InterviewPaymentMethodEnum;
import vn.proy.jobgohunter.util.enums.InterviewSubscriptionPlanEnum;
import vn.proy.jobgohunter.util.enums.InterviewSubscriptionStatusEnum;

public class ResInterviewOrderDTO {
    private Long id;
    private InterviewSubscriptionPlanEnum planCode;
    private InterviewSubscriptionStatusEnum status;
    private long amountVnd;
    private String bankName;
    private String bankAccount;
    private String bankHolder;
    private String transferContent;
    private Instant createdAt;
    private Instant transferSubmittedAt;
    private Long userId;
    private String userEmail;
    private InterviewPaymentMethodEnum paymentMethod;
    private Instant paidAt;
    private boolean vnpayEnabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InterviewSubscriptionPlanEnum getPlanCode() {
        return planCode;
    }

    public void setPlanCode(InterviewSubscriptionPlanEnum planCode) {
        this.planCode = planCode;
    }

    public InterviewSubscriptionStatusEnum getStatus() {
        return status;
    }

    public void setStatus(InterviewSubscriptionStatusEnum status) {
        this.status = status;
    }

    public long getAmountVnd() {
        return amountVnd;
    }

    public void setAmountVnd(long amountVnd) {
        this.amountVnd = amountVnd;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankHolder() {
        return bankHolder;
    }

    public void setBankHolder(String bankHolder) {
        this.bankHolder = bankHolder;
    }

    public String getTransferContent() {
        return transferContent;
    }

    public void setTransferContent(String transferContent) {
        this.transferContent = transferContent;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getTransferSubmittedAt() {
        return transferSubmittedAt;
    }

    public void setTransferSubmittedAt(Instant transferSubmittedAt) {
        this.transferSubmittedAt = transferSubmittedAt;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public InterviewPaymentMethodEnum getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(InterviewPaymentMethodEnum paymentMethod) { this.paymentMethod = paymentMethod; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public boolean isVnpayEnabled() { return vnpayEnabled; }
    public void setVnpayEnabled(boolean vnpayEnabled) { this.vnpayEnabled = vnpayEnabled; }
}
