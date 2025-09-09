package uk.gov.hmcts.appregister.testutils.controller;

import io.restassured.response.Response;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.appregister.testutils.client.RestAssuredClient;
import uk.gov.hmcts.appregister.testutils.client.RoleEnum;
import uk.gov.hmcts.appregister.testutils.stubs.TokenAndJwksKey;

/** An enumeration that represents the data surrounding a REST API endpoint for test purposes. */
@Builder
@Getter
public class RestTestParameter {

    /** The url to call. */
    private URL url;

    /** The method to call. */
    private HttpMethod method;

    /** The invalid role. */
    @Builder.Default private RoleEnum invalidRole = RoleEnum.NONE;

    /** The success role. */
    @Builder.Default private RoleEnum successRole = RoleEnum.NONE;

    /** The payload for the operation. */
    private Object payload;

    private Consumer<Response> responseConsumer;

    public Response process(RestAssuredClient client, TokenAndJwksKey tokenAndJwksKey)
            throws URISyntaxException {
        if (method == HttpMethod.GET) {
            Response response = client.executeGetRequest(url, tokenAndJwksKey);

            if (responseConsumer != null) {
                responseConsumer.accept(response);
            }
            return response;
        } else if (method == HttpMethod.POST) {
            if (payload == null) {
                throw new IllegalArgumentException("Expected payload" + method);
            }
            Response response = client.executePostRequest(url, tokenAndJwksKey, payload);
            if (responseConsumer != null) {
                responseConsumer.accept(response);
            }
            return response;
        } else if (method == HttpMethod.PUT) {
            if (payload == null) {
                throw new IllegalArgumentException("Expected payload" + method);
            }

            Response response = client.executePutRequest(url, tokenAndJwksKey, payload);
            if (responseConsumer != null) {
                responseConsumer.accept(response);
            }
            return response;
        }

        throw new IllegalArgumentException("Expected GET, POST or PUT method" + method);
    }
}
