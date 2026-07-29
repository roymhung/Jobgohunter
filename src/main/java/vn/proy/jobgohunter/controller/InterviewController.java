package vn.proy.jobgohunter.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.proy.jobgohunter.domain.request.interview.ReqCreateInterviewSessionDTO;
import vn.proy.jobgohunter.domain.request.interview.ReqSaveInterviewAnswersDTO;
import vn.proy.jobgohunter.domain.request.interview.ReqUpsertInterviewQuestionDTO;
import vn.proy.jobgohunter.domain.response.ResultPaginationDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewConfigDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewMeDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewQuestionAdminDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewSessionDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewTopicDTO;
import vn.proy.jobgohunter.service.InterviewQuestionAdminService;
import vn.proy.jobgohunter.service.InterviewService;
import vn.proy.jobgohunter.util.annotation.ApiMessage;
import vn.proy.jobgohunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/interview")
public class InterviewController {

    private final InterviewService interviewService;
    private final InterviewQuestionAdminService questionAdminService;

    public InterviewController(
            InterviewService interviewService,
            InterviewQuestionAdminService questionAdminService) {
        this.interviewService = interviewService;
        this.questionAdminService = questionAdminService;
    }

    @GetMapping("/config")
    @ApiMessage("Interview config")
    public ResponseEntity<ResInterviewConfigDTO> getConfig() {
        return ResponseEntity.ok(interviewService.getConfig());
    }

    @GetMapping("/topics")
    @ApiMessage("Interview topics")
    public ResponseEntity<List<ResInterviewTopicDTO>> getTopics() {
        return ResponseEntity.ok(interviewService.getTopics());
    }

    @GetMapping("/me")
    @ApiMessage("Interview quota and history")
    public ResponseEntity<ResInterviewMeDTO> getMe() throws IdInvalidException {
        return ResponseEntity.ok(interviewService.getMe());
    }

    @PostMapping("/sessions")
    @ApiMessage("Create interview session")
    public ResponseEntity<ResInterviewSessionDTO> createSession(
            @Valid @RequestBody ReqCreateInterviewSessionDTO req) throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(interviewService.createSession(req));
    }

    @GetMapping("/sessions/{id}")
    @ApiMessage("Get interview session")
    public ResponseEntity<ResInterviewSessionDTO> getSession(@PathVariable("id") String id)
            throws IdInvalidException {
        return ResponseEntity.ok(interviewService.getSession(id, false));
    }

    @PutMapping("/sessions/{id}/answers")
    @ApiMessage("Save interview answers")
    public ResponseEntity<ResInterviewSessionDTO> saveAnswers(
            @PathVariable("id") String id,
            @Valid @RequestBody ReqSaveInterviewAnswersDTO req) throws IdInvalidException {
        return ResponseEntity.ok(interviewService.saveAnswers(id, req));
    }

    @PostMapping("/sessions/{id}/submit")
    @ApiMessage("Submit interview session")
    public ResponseEntity<ResInterviewSessionDTO> submit(@PathVariable("id") String id)
            throws IdInvalidException {
        return ResponseEntity.ok(interviewService.submitSession(id));
    }

    @GetMapping("/admin/questions")
    @ApiMessage("List interview questions (admin)")
    public ResponseEntity<ResultPaginationDTO> adminListQuestions(
            @RequestParam(required = false) String topicCode,
            Pageable pageable) throws IdInvalidException {
        return ResponseEntity.ok(questionAdminService.listQuestions(topicCode, pageable));
    }

    @PostMapping("/admin/questions")
    @ApiMessage("Create interview question (admin)")
    public ResponseEntity<ResInterviewQuestionAdminDTO> adminCreateQuestion(
            @Valid @RequestBody ReqUpsertInterviewQuestionDTO req) throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionAdminService.create(req));
    }

    @PutMapping("/admin/questions/{id}")
    @ApiMessage("Update interview question (admin)")
    public ResponseEntity<ResInterviewQuestionAdminDTO> adminUpdateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody ReqUpsertInterviewQuestionDTO req) throws IdInvalidException {
        return ResponseEntity.ok(questionAdminService.update(id, req));
    }

    @DeleteMapping("/admin/questions/{id}")
    @ApiMessage("Delete interview question (admin)")
    public ResponseEntity<Void> adminDeleteQuestion(@PathVariable Long id) throws IdInvalidException {
        questionAdminService.delete(id);
        return ResponseEntity.ok().build();
    }
}
