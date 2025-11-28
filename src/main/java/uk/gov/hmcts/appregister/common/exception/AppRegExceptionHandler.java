package uk.gov.hmcts.appregister.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class AppRegExceptionHandler {

    @ExceptionHandler(AppRegistryException.class)
    ResponseEntity<ProblemDetail> handleAppRegisterApiException(AppRegistryException exception) {

        // gets the core exception code that we used to apply the application specific code
        ErrorCodeEnum error = exception.getCode();

        log.error("A app register exception occurred", exception);

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(error.getCode().getHttpCode(), "");

        // map the type and title if we have a code
        if (error.getCode().getType().isPresent()) {
            problemDetail.setType(error.getCode().getType().get());
        }

        // if the exception has properties, add them to the problem detail as they should be exposed
        if (exception.getDetails() != null && !exception.getDetails().isEmpty()) {
            for (String key : exception.getDetails().keySet()) {
                // add to the map
                problemDetail.setDetail(
                        problemDetail.getDetail()
                                + key
                                + "="
                                + exception.getDetails().get(key)
                                + System.lineSeparator());
            }
        } else {
            // set the detail to the message code message
            problemDetail.setDetail(error.getCode().getMessage());
        }

        if (error.getCode().getMessage() != null) {
            problemDetail.setTitle(error.getCode().getMessage());
        }

        return new ResponseEntity<>(problemDetail, error.getCode().getHttpCode());
    }
}
