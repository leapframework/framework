package leap.web;

import leap.lang.New;
import leap.lang.servlet.Servlets;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Map;

public abstract class AbstractServletContextInitializer {

    protected AppBootstrap tryBootstrap(ServletContext servletContext) throws ServletException {
        AppBootstrap bootstrap = AppBootstrap.tryGet(servletContext);
        if (null == bootstrap) {
            bootstrap = new AppBootstrap();

            Map<String, String> params = New.hashMap();
            params.putAll(Servlets.getInitParamsMap(servletContext));

            bootstrap.bootApplication(servletContext, params);
            return bootstrap;
        }
        return null;
    }

}