package vn.proy.jobgohunter.domain.request.interview;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class ReqSaveInterviewAnswersDTO {
    @Valid
    @NotNull
    private List<AnswerItem> answers;

    public List<AnswerItem> getAnswers() { return answers; }
    public void setAnswers(List<AnswerItem> answers) { this.answers = answers; }

    public static class AnswerItem {
        @NotNull
        private Integer orderIndex;
        private Integer selectedIndex;

        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
        public Integer getSelectedIndex() { return selectedIndex; }
        public void setSelectedIndex(Integer selectedIndex) { this.selectedIndex = selectedIndex; }
    }
}
