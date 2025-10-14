package uk.gov.hmcts.appregister.common.concurrency;

import uk.gov.hmcts.appregister.common.entity.base.Versionable;

import java.util.UUID;
import java.util.function.Supplier;

public interface MatchService {
    /**
     * matches on the request etag if present. throws an exception if a match has not been found
     * @param id The id of the entity
     * @param entity The versionable entity to match against
     * @param supplier The supplier to return the updated etag
     */
    <T> MatchResponse<T> matchOnRequest(UUID id,
                                                   Versionable entity,
                                                   Supplier<MatchResponse<T>> supplier);
}
