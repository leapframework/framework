package leap.web.api.remote;

import leap.lang.http.client.HttpResponse;
import leap.web.Content;
import leap.web.Request;
import leap.web.Response;
import leap.web.exception.ResponseException;

public class RestResourceInvokeException extends ResponseException {
    private final HttpResponse response;

    public  RestResourceInvokeException(HttpResponse response){
        super(response.getStatus(), new Content() {
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
}
