package uk.gov.hmcts.appregister.nationalcourthouse.exception;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.appregister.common.exception.DefaultErrorDetail;
import uk.gov.hmcts.appregister.common.exception.ErrorCodeEnum;
import uk.gov.hmcts.appregister.common.exception.ErrorDetail;

public enum CourtLocationError implements ErrorCodeEnum {
    COURT_NOT_FOUND(
            DefaultErrorDetail.create(HttpStatus.NOT_FOUND, "Court Location not found", "CL-1")),

    DUPLICATE_COURT_FOUND(
            DefaultErrorDetail.create(
                    HttpStatus.CONFLICT,
                    "Multiple Court Locations found when only one was expected",
                    "CL-2"));

    private final DefaultErrorDetail defaultErrorCode;

    CourtLocationError(DefaultErrorDetail defaultErrorCode) {
        this.defaultErrorCode = defaultErrorCode;
    }

    @Override
    public ErrorDetail getCode() {
        return defaultErrorCode;
    }
}
