package vn.proy.jobgohunter.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.proy.jobgohunter.domain.InterviewQuestion;
import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.domain.request.interview.ReqUpsertInterviewQuestionDTO;
import vn.proy.jobgohunter.domain.response.ResultPaginationDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewQuestionAdminDTO;
import vn.proy.jobgohunter.repository.InterviewQuestionRepository;
import vn.proy.jobgohunter.util.SecurityUtil;
import vn.proy.jobgohunter.util.error.IdInvalidException;

@Service
public class InterviewQuestionAdminService {

    private final InterviewQuestionRepository questionRepository;
    private final UserService userService;

    public InterviewQuestionAdminService(
            InterviewQuestionRepository questionRepository,
            UserService userService) {
        this.questionRepository = questionRepository;
        this.userService = userService;
    }

    public ResultPaginationDTO listQuestions(String topicCode, Pageable pageable) throws IdInvalidException {
        requireSuperAdmin(requireUser());
        Page<InterviewQuestion> page = questionRepository.adminSearch(topicCode, pageable);
        List<ResInterviewQuestionAdminDTO> rows = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(rows);
        return rs;
    }

    @Transactional
    public ResInterviewQuestionAdminDTO create(ReqUpsertInterviewQuestionDTO req) throws IdInvalidException {
        requireSuperAdmin(requireUser());
        validateOptions(req);
        InterviewQuestion q = new InterviewQuestion();
        apply(req, q);
        return toDto(questionRepository.save(q));
    }

    @Transactional
    public ResInterviewQuestionAdminDTO update(Long id, ReqUpsertInterviewQuestionDTO req) throws IdInvalidException {
        requireSuperAdmin(requireUser());
        validateOptions(req);
        InterviewQuestion q = questionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Question not found"));
        apply(req, q);
        return toDto(questionRepository.save(q));
    }

    @Transactional
    public void delete(Long id) throws IdInvalidException {
        requireSuperAdmin(requireUser());
        InterviewQuestion q = questionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Question not found"));
        questionRepository.delete(q);
    }

    private void apply(ReqUpsertInterviewQuestionDTO req, InterviewQuestion q) {
        q.setTopicCode(req.getTopicCode().trim());
        q.setQuestionType(req.getQuestionType().trim());
        q.setLevel(req.getLevel().trim());
        q.setContent(req.getContent().trim());
        q.setOptionsJson(req.getOptions());
        q.setCorrectIndex(req.getCorrectIndex());
        q.setExplanation(req.getExplanation() != null ? req.getExplanation().trim() : null);
        if (req.getActive() != null) {
            q.setActive(req.getActive());
        } else if (q.getActive() == null) {
            q.setActive(true);
        }
    }

    private void validateOptions(ReqUpsertInterviewQuestionDTO req) throws IdInvalidException {
        List<String> options = req.getOptions();
        if (options == null || options.size() < 2) {
            throw new IdInvalidException("Can it nhat 2 dap an");
        }
        int idx = req.getCorrectIndex();
        if (idx < 0 || idx >= options.size()) {
            throw new IdInvalidException("correctIndex khong hop le");
        }
    }

    private ResInterviewQuestionAdminDTO toDto(InterviewQuestion q) {
        ResInterviewQuestionAdminDTO dto = new ResInterviewQuestionAdminDTO();
        dto.setId(q.getId());
        dto.setTopicCode(q.getTopicCode());
        dto.setQuestionType(q.getQuestionType());
        dto.setLevel(q.getLevel());
        dto.setContent(q.getContent());
        dto.setOptions(q.getOptionsJson());
        dto.setCorrectIndex(q.getCorrectIndex());
        dto.setExplanation(q.getExplanation());
        dto.setActive(q.getActive());
        return dto;
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
