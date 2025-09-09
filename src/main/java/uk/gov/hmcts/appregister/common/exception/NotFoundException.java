package uk.gov.hmcts.appregister.common.exception;

public class NotFoundException extends AppRegistryException {
    public NotFoundException(ErrorCodeEnum errorCodeEnum, String message, Throwable cause) {
        super(
                DefaultErrorCode.getEnumEntry(
                        DefaultErrorCode.createNotFoundRequest(errorCodeEnum)),
                message,
                cause);
    }

    public NotFoundException(ErrorCodeEnum errorCodeEnum, Throwable cause) {
        super(
                DefaultErrorCode.getEnumEntry(
                        DefaultErrorCode.createNotFoundRequest(errorCodeEnum)),
                "",
                cause);
    }
}
