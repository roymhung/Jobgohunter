package vn.proy.jobgohunter.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import vn.proy.jobgohunter.config.InterviewProperties;
import vn.proy.jobgohunter.domain.InterviewQuestion;
import vn.proy.jobgohunter.domain.InterviewSession;
import vn.proy.jobgohunter.domain.InterviewSessionAnswer;
import vn.proy.jobgohunter.domain.InterviewSessionQuestion;
import vn.proy.jobgohunter.domain.InterviewTopic;
import vn.proy.jobgohunter.domain.InterviewUserQuota;
import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.domain.request.interview.ReqCreateInterviewSessionDTO;
import vn.proy.jobgohunter.domain.request.interview.ReqSaveInterviewAnswersDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewConfigDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewHistoryItemDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewMeDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewQuestionDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewSessionDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewTopicDTO;
import vn.proy.jobgohunter.repository.InterviewQuestionRepository;
import vn.proy.jobgohunter.repository.InterviewSessionAnswerRepository;
import vn.proy.jobgohunter.repository.InterviewSessionQuestionRepository;
import vn.proy.jobgohunter.repository.InterviewSessionRepository;
import vn.proy.jobgohunter.repository.InterviewSubscriptionRepository;
import vn.proy.jobgohunter.repository.InterviewTopicRepository;
import vn.proy.jobgohunter.repository.InterviewUserQuotaRepository;
import vn.proy.jobgohunter.util.SecurityUtil;
import vn.proy.jobgohunter.util.enums.InterviewSessionStatusEnum;
import vn.proy.jobgohunter.util.enums.InterviewSubscriptionStatusEnum;
import vn.proy.jobgohunter.util.error.IdInvalidException;

@Service
public class InterviewService {

    private final InterviewProperties props;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final InterviewTopicRepository topicRepository;
    private final InterviewQuestionRepository questionRepository;
    private final InterviewSessionRepository sessionRepository;
    private final InterviewSessionQuestionRepository sessionQuestionRepository;
    private final InterviewSessionAnswerRepository sessionAnswerRepository;
    private final InterviewUserQuotaRepository quotaRepository;
    private final InterviewSubscriptionRepository subscriptionRepository;
    private final InterviewSubscriptionService subscriptionService;

    public InterviewService(
            InterviewProperties props,
            ObjectMapper objectMapper,
            UserService userService,
            InterviewTopicRepository topicRepository,
            InterviewQuestionRepository questionRepository,
            InterviewSessionRepository sessionRepository,
            InterviewSessionQuestionRepository sessionQuestionRepository,
            InterviewSessionAnswerRepository sessionAnswerRepository,
            InterviewUserQuotaRepository quotaRepository,
            InterviewSubscriptionRepository subscriptionRepository,
            InterviewSubscriptionService subscriptionService) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.sessionRepository = sessionRepository;
        this.sessionQuestionRepository = sessionQuestionRepository;
        this.sessionAnswerRepository = sessionAnswerRepository;
        this.quotaRepository = quotaRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
    }

    public ResInterviewConfigDTO getConfig() {
        ResInterviewConfigDTO dto = new ResInterviewConfigDTO();
        dto.setFreeSessions(props.getFreeSessions());
        dto.setFreeQuestionsPerSession(props.getFreeQuestionsPerSession());
        dto.setProQuestionsPerSession(props.getProQuestionsPerSession());
        dto.setDurationMinutes(props.getDurationMinutes());
        dto.setPassPercent(props.getPassPercent());
        dto.setMaxTopics(props.getMaxTopics());
        return dto;
    }

    public List<ResInterviewTopicDTO> getTopics() {
        return topicRepository.findByActiveTrueOrderByGroupNameAscNameAsc().stream()
                .map(this::toTopicDto)
                .collect(Collectors.toList());
    }

    public ResInterviewMeDTO getMe() throws IdInvalidException {
        User user = requireUser();
        boolean pro = isProActive(user.getId());
        InterviewUserQuota quota = getOrCreateQuota(user.getId());
        ResInterviewMeDTO dto = new ResInterviewMeDTO();
        dto.setProActive(pro);
        dto.setFreeSessionsLeft(quota.getFreeSessionsLeft());
        dto.setFreeSessionsTotal(props.getFreeSessions());
        dto.setRecentSessions(
                sessionRepository
                        .findByUserIdAndStatusOrderBySubmittedAtDesc(user.getId(),
                                InterviewSessionStatusEnum.SUBMITTED)
                        .stream()
                        .limit(10)
                        .map(s -> {
                            ResInterviewHistoryItemDTO h = new ResInterviewHistoryItemDTO();
                            h.setSessionId(s.getId());
                            h.setSubmittedAt(s.getSubmittedAt());
                            h.setScorePercent(s.getScorePercent());
                            h.setPassed(scorePassed(s.getScorePercent()));
                            return h;
                        })
                        .collect(Collectors.toList()));
        subscriptionService.findPendingForUser(user.getId())
                .map(subscriptionService::mapPending)
                .ifPresent(dto::setPendingOrder);
        return dto;
    }

    @Transactional
    public ResInterviewSessionDTO createSession(ReqCreateInterviewSessionDTO req)
            throws IdInvalidException {
        User user = requireUser();
        if (req.getTopics() == null || req.getTopics().isEmpty()) {
            throw new IdInvalidException("Chọn ít nhất một chủ đề");
        }
        if (req.getTopics().size() > props.getMaxTopics()) {
            throw new IdInvalidException("Tối đa " + props.getMaxTopics() + " chủ đề");
        }
        List<InterviewTopic> validTopics =
                topicRepository.findByCodeInAndActiveTrue(req.getTopics());
        if (validTopics.size() != req.getTopics().size()) {
            throw new IdInvalidException("Chủ đề không hợp lệ");
        }
        boolean pro = isProActive(user.getId());
        if (!pro) {
            InterviewUserQuota quota = getOrCreateQuota(user.getId());
            if (quota.getFreeSessionsLeft() <= 0) {
                throw new IdInvalidException("Đã hết lượt Free");
            }
            quota.setFreeSessionsLeft(quota.getFreeSessionsLeft() - 1);
            quotaRepository.save(quota);
        }
        int limit = pro ? props.getProQuestionsPerSession() : props.getFreeQuestionsPerSession();
        List<InterviewQuestion> picked = questionRepository.pickRandom(
                req.getTopics(),
                req.getQuestionType(),
                req.getLevel(),
                PageRequest.of(0, limit));
        if (picked.size() < limit) {
            throw new IdInvalidException(
                    "Không đủ câu hỏi trong ngân hàng (cần " + limit + ", có " + picked.size() + ")");
        }
        Instant now = Instant.now();
        InterviewSession session = new InterviewSession();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(user.getId());
        session.setStatus(InterviewSessionStatusEnum.IN_PROGRESS);
        session.setSetupJson(writeSetup(req));
        session.setStartedAt(now);
        session.setEndsAt(now.plus(props.getDurationMinutes(), ChronoUnit.MINUTES));
        sessionRepository.save(session);

        for (int i = 0; i < picked.size(); i++) {
            InterviewSessionQuestion sq = new InterviewSessionQuestion();
            sq.getId().setSessionId(session.getId());
            sq.getId().setOrderIndex(i);
            sq.setQuestionId(picked.get(i).getId());
            sessionQuestionRepository.save(sq);

            InterviewSessionAnswer ans = new InterviewSessionAnswer();
            ans.getId().setSessionId(session.getId());
            ans.getId().setOrderIndex(i);
            ans.setSelectedIndex(null);
            sessionAnswerRepository.save(ans);
        }
        return getSession(session.getId(), false);
    }

    public ResInterviewSessionDTO getSession(String id, boolean includeCorrect)
            throws IdInvalidException {
        User user = requireUser();
        InterviewSession session = sessionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy phiên"));
        return buildSessionDto(session, includeCorrect);
    }

    @Transactional
    public ResInterviewSessionDTO saveAnswers(String id, ReqSaveInterviewAnswersDTO req)
            throws IdInvalidException {
        User user = requireUser();
        InterviewSession session = sessionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy phiên"));
        if (session.getStatus() != InterviewSessionStatusEnum.IN_PROGRESS) {
            throw new IdInvalidException("Phiên đã kết thúc");
        }
        for (ReqSaveInterviewAnswersDTO.AnswerItem item : req.getAnswers()) {
            InterviewSessionAnswer.SessionOrderId pk = new InterviewSessionAnswer.SessionOrderId();
            pk.setSessionId(id);
            pk.setOrderIndex(item.getOrderIndex());
            InterviewSessionAnswer ans = sessionAnswerRepository.findById(pk)
                    .orElseThrow(() -> new IdInvalidException("Câu hỏi không thuộc phiên"));
            ans.setSelectedIndex(item.getSelectedIndex());
            sessionAnswerRepository.save(ans);
        }
        return buildSessionDto(session, false);
    }

    @Transactional
    public ResInterviewSessionDTO submitSession(String id) throws IdInvalidException {
        User user = requireUser();
        InterviewSession session = sessionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy phiên"));
        if (session.getStatus() == InterviewSessionStatusEnum.SUBMITTED) {
            return buildSessionDto(session, true);
        }
        if (session.getStatus() != InterviewSessionStatusEnum.IN_PROGRESS) {
            throw new IdInvalidException("Phiên không thể nộp");
        }
        int correct = 0;
        int total = 0;
        List<InterviewSessionQuestion> sqList =
                sessionQuestionRepository.findByIdSessionIdOrderByIdOrderIndexAsc(id);
        for (InterviewSessionQuestion sq : sqList) {
            InterviewQuestion q = questionRepository.findById(sq.getQuestionId()).orElse(null);
            if (q == null) {
                continue;
            }
            total++;
            InterviewSessionAnswer.SessionOrderId pk = new InterviewSessionAnswer.SessionOrderId();
            pk.setSessionId(id);
            pk.setOrderIndex(sq.getId().getOrderIndex());
            InterviewSessionAnswer ans = sessionAnswerRepository.findById(pk).orElse(null);
            if (ans != null && ans.getSelectedIndex() != null
                    && ans.getSelectedIndex().equals(q.getCorrectIndex())) {
                correct++;
            }
        }
        int percent = total == 0 ? 0 : (correct * 100) / total;
        session.setStatus(InterviewSessionStatusEnum.SUBMITTED);
        session.setSubmittedAt(Instant.now());
        session.setScorePercent(percent);
        sessionRepository.save(session);
        return buildSessionDto(session, true);
    }

    private ResInterviewSessionDTO buildSessionDto(InterviewSession session, boolean includeCorrect)
            throws IdInvalidException {
        Map<String, Object> setup = readSetup(session.getSetupJson());
        ResInterviewSessionDTO dto = new ResInterviewSessionDTO();
        dto.setId(session.getId());
        dto.setStatus(session.getStatus());
        @SuppressWarnings("unchecked")
        List<String> topics = (List<String>) setup.get("topics");
        dto.setTopics(topics);
        dto.setQuestionType((String) setup.get("questionType"));
        dto.setLevel((String) setup.get("level"));
        dto.setStartedAt(session.getStartedAt());
        dto.setEndsAt(session.getEndsAt());
        dto.setSubmittedAt(session.getSubmittedAt());
        dto.setScorePercent(session.getScorePercent());
        dto.setPassed(scorePassed(session.getScorePercent()));

        List<InterviewSessionQuestion> sqList = sessionQuestionRepository
                .findByIdSessionIdOrderByIdOrderIndexAsc(session.getId());
        List<ResInterviewQuestionDTO> questions = new ArrayList<>();
        for (InterviewSessionQuestion sq : sqList) {
            InterviewQuestion q = questionRepository.findById(sq.getQuestionId())
                    .orElseThrow(() -> new IdInvalidException("Câu hỏi không tồn tại"));
            InterviewSessionAnswer.SessionOrderId pk = new InterviewSessionAnswer.SessionOrderId();
            pk.setSessionId(session.getId());
            pk.setOrderIndex(sq.getId().getOrderIndex());
            InterviewSessionAnswer ans = sessionAnswerRepository.findById(pk).orElse(null);

            ResInterviewQuestionDTO qdto = new ResInterviewQuestionDTO();
            qdto.setOrderIndex(sq.getId().getOrderIndex());
            qdto.setQuestionId(q.getId());
            qdto.setTopicCode(q.getTopicCode());
            qdto.setContent(q.getContent());
            qdto.setOptions(q.getOptionsJson());
            qdto.setSelectedIndex(ans != null ? ans.getSelectedIndex() : null);
            if (includeCorrect) {
                qdto.setCorrectIndex(q.getCorrectIndex());
                qdto.setExplanation(q.getExplanation());
            }
            questions.add(qdto);
        }
        dto.setQuestions(questions);
        return dto;
    }

    private boolean scorePassed(Integer scorePercent) {
        return scorePercent != null && scorePercent >= props.getPassPercent();
    }

    private User requireUser() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        if (email.isBlank()) {
            throw new IdInvalidException("Unauthorized");
        }
        User user = userService.handleGetUserByUsername(email);
        if (user == null) {
            throw new IdInvalidException("User not found");
        }
        return user;
    }

    private boolean isProActive(Long userId) {
        return subscriptionRepository
                .findActiveForUser(userId, InterviewSubscriptionStatusEnum.ACTIVE)
                .isPresent();
    }

    private InterviewUserQuota getOrCreateQuota(Long userId) {
        return quotaRepository.findByUserId(userId).orElseGet(() -> {
            InterviewUserQuota q = new InterviewUserQuota();
            q.setUserId(userId);
            q.setFreeSessionsLeft(props.getFreeSessions());
            return quotaRepository.save(q);
        });
    }

    private ResInterviewTopicDTO toTopicDto(InterviewTopic t) {
        ResInterviewTopicDTO dto = new ResInterviewTopicDTO();
        dto.setCode(t.getCode());
        dto.setName(t.getName());
        dto.setGroupName(t.getGroupName());
        dto.setQuestionCount(questionRepository.countByTopicCodeAndActiveTrue(t.getCode()));
        return dto;
    }

    private String writeSetup(ReqCreateInterviewSessionDTO req) throws IdInvalidException {
        Map<String, Object> map = new HashMap<>();
        map.put("topics", req.getTopics());
        map.put("questionType", req.getQuestionType());
        map.put("level", req.getLevel());
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new IdInvalidException("Setup invalid");
        }
    }

    private Map<String, Object> readSetup(String json) throws IdInvalidException {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new IdInvalidException("Setup corrupt");
        }
    }
}
