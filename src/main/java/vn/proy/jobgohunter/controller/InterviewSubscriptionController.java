package vn.proy.jobgohunter.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.proy.jobgohunter.domain.InterviewSubscription;
import vn.proy.jobgohunter.domain.request.interview.ReqCreateInterviewOrderDTO;
import vn.proy.jobgohunter.domain.response.interview.ResInterviewOrderDTO;
import vn.proy.jobgohunter.service.InterviewSubscriptionService;
import vn.proy.jobgohunter.util.annotation.ApiMessage;
import vn.proy.jobgohunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/interview/subscriptions")
public class InterviewSubscriptionController {

    private final InterviewSubscriptionService subscriptionService;

    public InterviewSubscriptionController(InterviewSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/orders")
    @ApiMessage("Create Pro subscription order")
    public ResponseEntity<ResInterviewOrderDTO> createOrder(@Valid @RequestBody ReqCreateInterviewOrderDTO req)
            throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.createOrder(req));
    }

    @PostMapping("/orders/{id}/transfer-submitted")
    @ApiMessage("Mark bank transfer submitted")
    public ResponseEntity<ResInterviewOrderDTO> transferSubmitted(@PathVariable Long id) throws IdInvalidException {
        return ResponseEntity.ok(subscriptionService.markTransferSubmitted(id));
    }

    @PostMapping("/orders/{id}/activate")
    @ApiMessage("Activate Pro subscription (admin)")
    public ResponseEntity<ResInterviewOrderDTO> activate(@PathVariable Long id) throws IdInvalidException {
        return ResponseEntity.ok(subscriptionService.activateOrder(id));
    }

    @GetMapping("/orders/pending")
    @ApiMessage("List pending Pro orders (admin)")
    public ResponseEntity<List<InterviewSubscription>> listPending() throws IdInvalidException {
        return ResponseEntity.ok(subscriptionService.listPendingOrders());
    }
}
