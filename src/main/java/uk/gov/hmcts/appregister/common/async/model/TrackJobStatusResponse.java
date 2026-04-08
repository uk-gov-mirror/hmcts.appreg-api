package uk.gov.hmcts.appregister.common.async.model;

import lombok.Getter;
import java.util.concurrent.Future;


@Getter
public class TrackJobStatusResponse extends JobStatusResponse {
    private final Future<?> future;

    public TrackJobStatusResponse(JobStatusResponse response, final Future<?> future) {
        super(response.getUuid(), response.getType(), response.getStatus(),
              response.getUserName(), response.getErrorMessage(), response.getPersistence());
        this.future = future;
    }
}
