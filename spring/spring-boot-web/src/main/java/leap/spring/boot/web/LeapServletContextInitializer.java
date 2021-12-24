package leap.spring.boot.web;

import leap.core.AppConfig;
import leap.core.AppContext;
import leap.core.BeanFactory;
import leap.spring.boot.Global;
import leap.web.AbstractServletContextInitializer;
import leap.web.AppBootstrap;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class LeapServletContextInitializer extends AbstractServletContextInitializer implements ServletContextInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        AppBootstrap bootstrap = tryBootstrap(servletContext);
        if (null != bootstrap) {
            Global.leap = new Global.LeapContext() {
                @Override
                public AppConfig config() {
                    return bootstrap.getAppConfig();
                }

                @Override
                public BeanFactory factory() {
                    return bootstrap.getBeanFactory();
                }

                @Override
                public AppContext context() {
                    return bootstrap.getAppContext();
                }
            };
        }
    }
}