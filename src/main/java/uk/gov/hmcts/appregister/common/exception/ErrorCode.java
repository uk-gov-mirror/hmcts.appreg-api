package uk.gov.hmcts.appregister.common.exception;

import java.net.URI;
import java.util.Optional;
import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus getHttpCode();

    String getMessage();

    String getAppCode();

    default Optional<URI> getType() {
        if (getAppCode() != null) {
            return Optional.of(URI.create(getAppCode()));
        }
        return Optional.empty();
    }
}
