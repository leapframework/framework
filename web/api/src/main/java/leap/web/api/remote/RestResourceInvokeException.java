package leap.web.api.remote;

import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.client.HttpResponse;
import leap.lang.json.JSON;
import leap.web.Content;
import leap.web.Request;
import leap.web.Response;
import leap.web.api.mvc.ApiError;
import leap.web.exception.ResponseException;

public class RestResourceInvokeException extends ResponseException {
    private final HttpResponse response;

    public  RestResourceInvokeException(HttpResponse response){
        super(response.getStatus(), tryGetErrorMessage(response), new Content() {
            @Override
            public String getContentType(Request request) throws Throwable {
                return response.getContentType().toString();
            }

            @Override
            public void render(Request request, Response res) throws Throwable {
                res.getWriter().write(response.getString());
            }
        });
        this.response = response;
    }

    public HttpResponse getResponse() {
        return response;
    }

    private static String tryGetErrorMessage(HttpResponse response) {
        String content = response.getString();
        if (!Strings.isEmpty(content) && ContentTypes.APPLICATION_JSON_TYPE.isCompatible(response.getContentType())) {
            try {
                return JSON.decode(content, ApiError.class).getMessage();
            } catch (Exception ignored) {

            }
        }
        return String.valueOf(response.getStatus());
    }
}