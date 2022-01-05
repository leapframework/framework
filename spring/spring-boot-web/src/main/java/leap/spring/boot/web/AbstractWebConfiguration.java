package leap.spring.boot.web;

import leap.core.AppConfig;
import leap.core.AppContext;
import leap.core.BeanFactory;
import leap.spring.boot.Global;
import leap.web.AppBootstrap;
import leap.web.AppFilter;

import javax.servlet.ServletContext;

public class AbstractWebConfiguration {

    static AppFilter filter;
    static ServletContext startedServletContext;

    protected static boolean booted;

    static {
        if (null == Global.leap) {
            Global.leap = new Global.LeapContext() {
                @Override
                public AppConfig config() {
                    return null == filter ? null : filter.config();
                }

                @Override
                public BeanFactory factory() {
                    return null == filter ? null : filter.factory();
                }

                @Override
                public AppContext context() {
                    return null == filter ? null : filter.context();
                }
            };
        }
    }

    protected static void boot(ServletContext sc) {
        if (AppBootstrap.isInitialized(sc)) {
            return;
        }

        booted = true;
        final AppBootstrap bootstrap = new AppBootstrap();
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
        bootstrap.initialize(sc, Global.extraInitPropertiesFromEnv());
        AppContext.setStandalone(bootstrap.getAppContext());
    }

}