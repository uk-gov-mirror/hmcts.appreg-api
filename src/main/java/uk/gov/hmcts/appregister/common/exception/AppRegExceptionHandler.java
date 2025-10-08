package uk.gov.hmcts.appregister.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class AppRegExceptionHandler {

    public static final String HDR_CORRELATION_ID = "x-correlation-id";

    // TODO - CHeck if the below works.

    @ExceptionHandler(AppRegistryException.class)
    ResponseEntity<ProblemDetail> handleAppRegisterApiException(AppRegistryException exception) {

        // gets the core exception code that we used to apply the application specific code
        ErrorCodeEnum error = exception.getCode();

        log.error("A app register exception occurred", exception);

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        error.getCode().getHttpCode(), error.getCode().getMessage());

        // map the type and title if we have a code
        if (error.getCode().getType().isPresent()) {
            problemDetail.setType(error.getCode().getType().get());
        }

        if (error.getCode().getMessage() != null) {
            problemDetail.setTitle(error.getCode().getMessage());
        }

        return new ResponseEntity<>(problemDetail, error.getCode().getHttpCode());
    }

    // 400 family (validation, binding, parse)
    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        ConstraintViolationException.class,
        BindException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class,
        HttpMessageNotReadableException.class
    })
    ResponseEntity<ProblemDetail> handleBadRequest(Exception ex, HttpServletRequest request) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        setCommonProblemFields(pd, request);
        pd.setTitle("Invalid request parameters");
        pd.setType(URI.create("https://errors.hmcts.net/appreg/bad-request"));
        log.warn("400 Bad Request: {}", ex.getMessage());
        return withProblemHeaders(pd, HttpStatus.BAD_REQUEST, correlationId(request));
    }

    // 401 / 403
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    ResponseEntity<ProblemDetail> handleUnauthorized(Exception ex, HttpServletRequest request) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNAUTHORIZED, "Authentication required or token invalid.");
        setCommonProblemFields(pd, request);
        pd.setTitle("Unauthorized");
        pd.setType(URI.create("https://errors.hmcts.net/appreg/unauthorized"));
        log.warn("401 Unauthorized: {}", ex.getMessage());
        return withProblemHeaders(pd, HttpStatus.UNAUTHORIZED, correlationId(request));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> handleForbidden(Exception ex, HttpServletRequest request) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.FORBIDDEN,
                        "Authenticated but not permitted to perform this action.");
        setCommonProblemFields(pd, request);
        pd.setTitle("Forbidden");
        pd.setType(URI.create("https://errors.hmcts.net/appreg/forbidden"));
        log.warn("403 Forbidden: {}", ex.getMessage());
        return withProblemHeaders(pd, HttpStatus.FORBIDDEN, correlationId(request));
    }

    // 404 / 405 / 406 / 415
    @ExceptionHandler(NoHandlerFoundException.class)
    ResponseEntity<ProblemDetail> handleNotFound(
            NoHandlerFoundException ex, HttpServletRequest request) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND, "The requested resource was not found.");
        setCommonProblemFields(pd, request);
        pd.setTitle("Not Found");
        pd.setType(URI.create("https://errors.hmcts.net/appreg/not-found"));
        log.warn("404 Not Found: {}", request.getRequestURI());
        return withProblemHeaders(pd, HttpStatus.NOT_FOUND, correlationId(request));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ProblemDetail> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
        setCommonProblemFields(pd, request);
        pd.setTitle("Method Not Allowed");
        pd.setType(URI.create("https://errors.hmcts.net/appreg/method-not-allowed"));
        log.warn("405 Method Not Allowed: {}", ex.getMethod());
        return withProblemHeaders(pd, HttpStatus.METHOD_NOT_ALLOWED, correlationId(request));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    ResponseEntity<ProblemDetail> handleNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
        setCommonProblemFields(pd, request);
        pd.setTitle("Not Acceptable");
        pd.setType(URI.create("https://errors.hmcts.net/appreg/not-acceptable"));
        log.warn("406 Not Acceptable: {}", ex.getMessage());
        return withProblemHeaders(pd, HttpStatus.NOT_ACCEPTABLE, correlationId(request));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ProblemDetail> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
        setCommonProblemFields(pd, request);
        pd.setTitle("Unsupported Media Type");
        pd.setType(URI.create("https://errors.hmcts.net/appreg/unsupported-media-type"));
        log.warn("415 Unsupported Media Type: {}", ex.getContentType());
        return withProblemHeaders(pd, HttpStatus.UNSUPPORTED_MEDIA_TYPE, correlationId(request));
    }

    // Ultimate catch-all → 500
    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        ProblemDetail pd =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error.");
        setCommonProblemFields(pd, request);
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create("https://errors.hmcts.net/appreg/internal-server-error"));
        log.error("500 Unexpected error", ex);
        return withProblemHeaders(pd, HttpStatus.INTERNAL_SERVER_ERROR, correlationId(request));
    }

    // ---- helpers ----

    private static void setCommonProblemFields(ProblemDetail pd, HttpServletRequest request) {
        pd.setInstance(currentInstanceUri());
        // Include correlationId in the body per your schema
        pd.setProperty("correlationId", correlationId(request));
    }

    private static URI currentInstanceUri() {
        return ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
    }

    private static String correlationId(HttpServletRequest request) {
        String header = request.getHeader(HDR_CORRELATION_ID);
        if (header != null && !header.isBlank()) {
            return header;
        }
        String mdc = MDC.get("correlationId");
        return (mdc != null && !mdc.isBlank()) ? mdc : null;
    }

    private static ResponseEntity<ProblemDetail> withProblemHeaders(
            ProblemDetail pd, HttpStatus status, String correlationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        if (correlationId != null) {
            headers.set(HDR_CORRELATION_ID, correlationId);
        }
        return new ResponseEntity<>(pd, headers, status);
    }
}
