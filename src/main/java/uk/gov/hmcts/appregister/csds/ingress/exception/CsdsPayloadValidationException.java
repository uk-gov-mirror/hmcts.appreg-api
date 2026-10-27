package uk.gov.hmcts.appregister.csds.ingress.exception;

import uk.gov.hmcts.appregister.common.exception.AppRegistryException;

/**
 * Indicates that CSDS payload data does not satisfy the schema required by an ingress processor.
 */
public class CsdsPayloadValidationException extends AppRegistryException {
    private static final long serialVersionUID = 1L;

    public CsdsPayloadValidationException(String message) {
        super(CsdsIngestError.INVALID_UPSTREAM_DATA, message);
    }

    public CsdsPayloadValidationException(String message, Throwable cause) {
        super(CsdsIngestError.INVALID_UPSTREAM_DATA, message, cause);
    }
}
