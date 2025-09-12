package uk.gov.hmcts.appregister.applicationfee.service.exception;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.appregister.common.exception.DefaultErrorCode;
import uk.gov.hmcts.appregister.common.exception.ErrorCode;
import uk.gov.hmcts.appregister.common.exception.ErrorCodeEnum;

public enum ApplicationFeeCode implements ErrorCodeEnum {
    // TODO: We need to apply an application code here
    AMBIGUOUS_FEE(
            DefaultErrorCode.create(
                    HttpStatus.INTERNAL_SERVER_ERROR, "To many fees returned", "feecode")),
    NO_MAIN_FEE(
            DefaultErrorCode.create(
                    HttpStatus.INTERNAL_SERVER_ERROR, "No main fee returned", "feecode-nomain"));

    private final DefaultErrorCode defaultErrorCode;

    ApplicationFeeCode(DefaultErrorCode defaultErrorCode) {
        this.defaultErrorCode = defaultErrorCode;
    }

    @Override
    public ErrorCode getCode() {
        return defaultErrorCode;
    }
}
