package uk.gov.hmcts.appregister.common.concurrency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.http.HttpRequest;

/**
 * Represents a match request containing a match header
 */
@Component
@RequiredArgsConstructor
public class MatchRequest {
    private HttpRequest request;

    public String getEtag() {
        return request.headers().firstValue("If-Match").orElse(null);
    }
}
