package leap.web.api.remote;

import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.MimeType;
import leap.lang.http.MimeTypes;
import leap.lang.http.client.HttpResponse;
import leap.lang.json.JSON;
import leap.lang.json.JsonValue;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.Content;
import leap.web.Request;
import leap.web.Response;
import leap.web.api.mvc.ApiError;
import leap.web.api.mvc.ApiErrorHandler;
import leap.web.exception.ResponseException;

public class RestResourceInvokeException extends ResponseException {

    private static final Log log = LogFactory.get(RestResourceInvokeException.class);

    private final HttpResponse response;

    public RestResourceInvokeException(HttpResponse response){
        this(response, null);
    }

    public RestResourceInvokeException(HttpResponse response, ApiErrorHandler apiErrorHandler){
        super(response.getStatus(), tryGetErrorMessage(response), new Content() {
            @Override
            public String getContentType(Request request) throws Throwable {
                return response.getContentType().toString();
            }

            @Override
            public void render(Request request, Response res) throws Throwable {
                final String content = response.getString();
                if (null != apiErrorHandler) {
                    ApiError apiError = null;
                    if (null != content) {
                        final MimeType contentType = response.getContentType();
                        try {
                            if (null != contentType && contentType.isCompatible(MimeTypes.APPLICATION_JSON_TYPE)) {
                                apiError = ((JsonValue) JSON.decode(content)).asJsonObject().convertTo(ApiError.class);
                            }
                        } catch (Exception e) {
                            log.error("Unsupported json error, " + e.getMessage(), e);
                        }
                    }
                    if (null == apiError) {
                        apiError = new ApiError(Strings.abbreviate(content, 200));
                    }
                    apiErrorHandler.responseError(res, response.getStatus(), apiError);
                } else {
                    res.getWriter().write(content);
                }
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