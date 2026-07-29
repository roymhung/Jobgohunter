package vn.proy.jobgohunter.domain.request.interview;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ReqCreateInterviewOrderDTO {
    @NotBlank
    @Pattern(regexp = "year|lifetime")
    private String plan;

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }
}
