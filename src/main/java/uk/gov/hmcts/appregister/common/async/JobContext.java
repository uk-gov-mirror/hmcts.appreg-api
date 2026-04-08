package uk.gov.hmcts.appregister.common.async;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A context that allows us to control the flow of job events.
 */
@Getter
@Setter
public class JobContext {
    private List<String> validationFailureMessages = new ArrayList<>();

    private boolean isStopped = false;

    public boolean hasFailure() {
        return !validationFailureMessages.isEmpty();
    }

    public String getFailureMessage() {
        return String.join(", ", validationFailureMessages);
    }

    public void logError(String errorMsg) {
        validationFailureMessages.add(errorMsg);
    }
}
