package leap.web.api.remote;

import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.MimeType;
import leap.lang.http.client.HttpResponse;
import leap.lang.json.JSON;
import leap.web.api.mvc.ApiError;

public class RestResourceInvokeException extends RuntimeException {
    private int httpStatus;
    private MimeType responseContentType;
    private String content;
    private HttpResponse response;

    public RestResourceInvokeException(){

    }

    public  RestResourceInvokeException(String msg){
        super(msg);
        this.content=msg;
    }

    public  RestResourceInvokeException(HttpResponse response){
        super(response.getString());
        this.content=response.getString();
        this.responseContentType=response.getContentType();
        this.httpStatus=response.getStatus();
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }


    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public MimeType getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(MimeType responseContentType) {
        this.responseContentType = responseContentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public ApiError getErrorObject(){
        ApiError error=null;
        if(ContentTypes.APPLICATION_JSON_TYPE.isCompatible(this.responseContentType)
            && Strings.isNotEmpty(this.content)){
            error= JSON.decode(this.content, ApiError.class);
        }
        return error;
    }
}
