package leap.web.security.logout;

import leap.web.Request;
import leap.web.Response;

public interface LogoutViewHandler {

    void handleLogoutSuccess(Request request, Response response, LogoutContext context) throws Throwable;
    
}