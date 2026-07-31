package vn.proy.jobgohunter.domain.response.interview;

import vn.proy.jobgohunter.util.enums.InterviewPaymentMethodEnum;
import vn.proy.jobgohunter.util.enums.InterviewSubscriptionPlanEnum;
import vn.proy.jobgohunter.util.enums.InterviewSubscriptionStatusEnum;

public class ResInterviewPendingOrderDTO {
    private Long id;
    private InterviewSubscriptionPlanEnum planCode;
    private InterviewSubscriptionStatusEnum status;
    private boolean transferSubmitted;
    private InterviewPaymentMethodEnum paymentMethod;

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

    public boolean isTransferSubmitted() {
        return transferSubmitted;
    }

    public void setTransferSubmitted(boolean transferSubmitted) {
        this.transferSubmitted = transferSubmitted;
    }

    public InterviewPaymentMethodEnum getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(InterviewPaymentMethodEnum paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
