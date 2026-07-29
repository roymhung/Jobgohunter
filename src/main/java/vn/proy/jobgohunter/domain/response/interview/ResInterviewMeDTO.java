package vn.proy.jobgohunter.domain.response.interview;

import java.util.List;

public class ResInterviewMeDTO {
    private boolean proActive;
    private int freeSessionsLeft;
    private int freeSessionsTotal;
    private List<ResInterviewHistoryItemDTO> recentSessions;
    private ResInterviewPendingOrderDTO pendingOrder;

    public boolean isProActive() {
        return proActive;
    }

    public void setProActive(boolean proActive) {
        this.proActive = proActive;
    }

    public int getFreeSessionsLeft() {
        return freeSessionsLeft;
    }

    public void setFreeSessionsLeft(int freeSessionsLeft) {
        this.freeSessionsLeft = freeSessionsLeft;
    }

    public int getFreeSessionsTotal() {
        return freeSessionsTotal;
    }

    public void setFreeSessionsTotal(int freeSessionsTotal) {
        this.freeSessionsTotal = freeSessionsTotal;
    }

    public List<ResInterviewHistoryItemDTO> getRecentSessions() {
        return recentSessions;
    }

    public void setRecentSessions(List<ResInterviewHistoryItemDTO> recentSessions) {
        this.recentSessions = recentSessions;
    }

    public ResInterviewPendingOrderDTO getPendingOrder() {
        return pendingOrder;
    }

    public void setPendingOrder(ResInterviewPendingOrderDTO pendingOrder) {
        this.pendingOrder = pendingOrder;
    }
}
