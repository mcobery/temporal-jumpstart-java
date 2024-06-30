package io.temporal.onboardings.api.controllers;

import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowNotFoundException;
import io.temporal.client.WorkflowOptions;
import io.temporal.onboardings.api.messages.OnboardingsGetV2;
import io.temporal.onboardings.api.messages.OnboardingsPutV2;
import io.temporal.onboardings.domain.messages.commands.ApproveEntityRequest;
import io.temporal.onboardings.domain.messages.commands.RejectEntityRequest;
import io.temporal.onboardings.domain.messages.orchestrations.OnboardEntityRequest;
import io.temporal.onboardings.domain.messages.values.ApprovalStatus;
import io.temporal.onboardings.domain.orchestrations.EntityOnboarding;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/onboardings")
public class OnboardingsControllerV2 {

  Logger logger = LoggerFactory.getLogger(OnboardingsControllerV2.class);
  @Autowired WorkflowClient temporalClient;

  @Value("${spring.curriculum.task-queue}")
  String taskQueue;

  @GetMapping("/{id}")
  public ResponseEntity<OnboardingsGetV2> onboardingGet(@PathVariable("id") String id) {
    try {
      var workflowStub = temporalClient.newWorkflowStub(EntityOnboarding.class, id);
      var state = workflowStub.getState();
      return new ResponseEntity<>(
          new OnboardingsGetV2(
              state.id(),
              state.currentValue(),
              state.approval().approvalStatus().name(),
              state.approval().comment()),
          HttpStatus.OK);
    } catch (WorkflowNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PutMapping(
      value = "/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  ResponseEntity<String> onboardingPut(
      @PathVariable String id, @RequestBody OnboardingsPutV2 params) {
    if (params.approval().approvalStatus().equals(ApprovalStatus.PENDING)) {
      return startOnboardEntity(id, params);
    }
    try {
      var wfStub = temporalClient.newWorkflowStub(EntityOnboarding.class, id);
      if (params.approval().approvalStatus().equals(ApprovalStatus.APPROVED)) {
        wfStub.approve(new ApproveEntityRequest(params.approval().comment()));
      } else if (params.approval().approvalStatus().equals(ApprovalStatus.REJECTED)) {
        wfStub.reject(new RejectEntityRequest(params.approval().comment()));
      } else {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } catch (WorkflowNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<String> startOnboardEntity(String id, OnboardingsPutV2 params) {
    final WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setTaskQueue(taskQueue)
            .setWorkflowId(id)
            .setRetryOptions(null)
            .setWorkflowIdReusePolicy(
                WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
            .build();
    var workflowStub = temporalClient.newWorkflowStub(EntityOnboarding.class, options);

    var wfArgs = new OnboardEntityRequest(params.id(), params.value(), 7 * 86400, null, false);
    // Start the workflow execution.
    try {
      var run = WorkflowClient.start(workflowStub::execute, wfArgs);
      var headers = new HttpHeaders();
      headers.setLocation(URI.create(String.format("/api/onboardings/%s", id)));
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (WorkflowExecutionAlreadyStarted was) {
      logger.info("Workflow execution already started: {}", id);
      return new ResponseEntity<>(HttpStatus.ACCEPTED);
    } catch (Exception e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
