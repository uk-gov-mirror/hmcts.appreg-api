package uk.gov.hmcts.appregister.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.appregister.audit.AuditEventEnum;
import uk.gov.hmcts.appregister.audit.event.AuditEvent;
import uk.gov.hmcts.appregister.audit.listener.ApplyRequestActionAuditListener;
import uk.gov.hmcts.appregister.audit.listener.AuditOperationLifecycleListener;
import uk.gov.hmcts.appregister.audit.model.AuditRequest;
import uk.gov.hmcts.appregister.audit.model.AuditResponse;

/**
 * Encapsulates a unit of work for the lifecycle of an auditable operation. Behaviour of each audit
 * lifecycle event can be controlled by passing in {@link
 * uk.gov.hmcts.appregister.audit.listener.AuditOperationLifecycleListener} that will be invoked at
 * the start and end of the operation.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AuditOperationServiceImpl implements AuditOperationService {

    /** The trace id name that is inserted by micrometer. */
    private static final String TRACE_ID = "traceId";

    private final ObjectMapper mapper;

    @Override
    public <T> T processAudit(
            AuditEventEnum auditType,
            Function<AuditRequest, Optional<T>> execution,
            AuditOperationLifecycleListener... listener) {
        List<AuditOperationLifecycleListener> allListeners =
                new ArrayList<>(Arrays.stream(listener).toList());
        allListeners.addFirst(new ApplyRequestActionAuditListener(auditType));
        return processAudit(
                execution, allListeners.toArray(new AuditOperationLifecycleListener[0]));
    }

    @Override
    public <T> T processAudit(
            Function<AuditRequest, Optional<T>> execution,
            AuditOperationLifecycleListener... listener) {
        AuditRequest auditRequest =
                AuditRequest.builder()
                        .messageStatus(OperationStatus.STARTED)
                        .messageUuid(getTraceId())
                        .build();
        AuditEvent event = new AuditEvent(auditRequest, Optional.empty());

        // before execution hook
        fireAuditEvent(event, listener);

        log.debug("Processed start of auditable operation: {}", event);
        Optional<T> responsePayload;
        try {
            responsePayload = execution.apply(auditRequest);
            if (responsePayload.isPresent()) {
                AuditResponse response =
                        getResponseFromRequest(
                                auditRequest,
                                OperationStatus.COMPLETED,
                                getBodyAsString(responsePayload.get()));

                // fire after the completed operation
                fireAuditEvent(
                        new AuditEvent(
                                getRequestWithStatus(auditRequest, OperationStatus.COMPLETED),
                                Optional.of(response)),
                        listener);
            } else {
                AuditResponse response =
                        getResponseFromRequest(
                                getRequestWithStatus(auditRequest, OperationStatus.COMPLETED),
                                OperationStatus.COMPLETED,
                                null);

                // fire after the completed operation
                fireAuditEvent(
                        new AuditEvent(
                                getRequestWithStatus(auditRequest, OperationStatus.COMPLETED),
                                Optional.of(response)),
                        listener);
            }

            log.debug("Processed success auditable operation: {}", event);
        } catch (Exception e) {
            AuditResponse response =
                    getResponseFromRequest(
                            getRequestWithStatus(auditRequest, OperationStatus.FAILED),
                            OperationStatus.FAILED,
                            null);

            // fire after the failure of an operation
            fireAuditEvent(
                    new AuditEvent(
                            getRequestWithStatus(auditRequest, OperationStatus.FAILED),
                            Optional.of(response)),
                    listener);

            log.debug("Processed failure auditable operation: {}", event);
            throw e;
        }

        return responsePayload.orElse(null);
    }

    /**
     * gets the json body in string form that will be part of the audit response.
     *
     * @return The body as a string or defaulted on an marshalling error
     */
    private String getBodyAsString(Object body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            log.error("Problem marshalling the json response for auditing", e);
            return "Problem marshalling the json response for auditing";
        }
    }

    private AuditRequest getRequestWithStatus(AuditRequest request, OperationStatus status) {
        return AuditRequest.builder()
                .requestAction(request.getRequestAction())
                .messageStatus(status)
                .messageUuid(request.getMessageUuid())
                .build();
    }

    private AuditResponse getResponseFromRequest(
            AuditRequest auditRequest, OperationStatus status, String message) {
        if (message != null) {
            return AuditResponse.builder()
                    .requestAction(auditRequest.getRequestAction())
                    .messageContent(message)
                    .messageStatus(status)
                    .messageUuid(auditRequest.getMessageUuid())
                    .messageContent(message)
                    .build();
        } else {
            return AuditResponse.builder()
                    .requestAction(auditRequest.getRequestAction())
                    .messageStatus(status)
                    .messageUuid(auditRequest.getMessageUuid())
                    .build();
        }
    }

    /**
     * fires the audit event for an operation.
     *
     * @param listener The listener to fire with the event
     * @param auditEvent The audit event to fire
     */
    private void fireAuditEvent(
            AuditEvent auditEvent, AuditOperationLifecycleListener... listener) {
        for (AuditOperationLifecycleListener l : listener) {
            l.eventPerformed(auditEvent);
        }
    }

    /**
     * gets the trace id from the parsed Sleuth logging value.
     *
     * @return The trace id or a default message if not found
     */
    protected String getTraceId() {
        try {
            String traceId = MDC.get(TRACE_ID);
            if (traceId != null) {
                return MDC.get(TRACE_ID);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Couldn't find the trace id defaulting", e);
        }

        return "No Correlation Id Found";
    }
}
