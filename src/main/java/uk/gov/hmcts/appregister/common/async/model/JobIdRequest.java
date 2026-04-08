package uk.gov.hmcts.appregister.common.async.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.appregister.generated.model.JobType;

import java.util.UUID;

/**
 * A job id is a combination of the id and the user name
*/
@EqualsAndHashCode
@Builder
@Getter
public class JobIdRequest {
    UUID id;
    String userName;
}
