package uk.gov.hmcts.appregister.audit.service;

/** Describes the status of the underlying operation. */
public enum OperationStatus {
    STARTED(1),
    COMPLETED(10),
    FAILED(-1);

    private int status;

    OperationStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
