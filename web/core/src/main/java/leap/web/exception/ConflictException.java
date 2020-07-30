package leap.web.exception;

import leap.lang.http.HTTP;
import leap.web.Content;

public class ConflictException extends ClientErrorException {

    public ConflictException() {
        super(HTTP.SC_CONFLICT, "Conflict");
    }

    public ConflictException(String message) {
        super(HTTP.SC_CONFLICT, message);
    }

    public ConflictException(Content content) {
        super(HTTP.SC_CONFLICT, content);
    }

    public ConflictException(Throwable cause) {
        super(HTTP.SC_CONFLICT, cause);
    }

    public ConflictException(String message, Throwable cause) {
        super(HTTP.SC_CONFLICT, message, cause);
    }

    public ConflictException(String message, Content content) {
        super(HTTP.SC_CONFLICT, message, content);
    }

    public ConflictException(String message, Content content, Throwable cause) {
        super(HTTP.SC_CONFLICT, message, content, cause);
    }

}