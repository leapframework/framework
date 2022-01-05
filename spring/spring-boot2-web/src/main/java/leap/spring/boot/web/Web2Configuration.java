package leap.spring.boot.web;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.jetty.ConfigurableJettyWebServerFactory;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.servlet.ServletContext;

@Configuration
public class Web2Configuration extends AbstractWebConfiguration {

    @Configuration
    @ConditionalOnClass(ConfigurableJettyWebServerFactory.class)
    @ConditionalOnProperty(value = "jetty-booting", matchIfMissing = true)
    static class Jetty2Configuration {

        @Bean
        public WebServerFactoryCustomizer<ConfigurableJettyWebServerFactory> leapJettyWebServerFactoryCustomizer(
                Environment environment,
                ServerProperties serverProperties) {
            return new WebServerFactoryCustomizer<ConfigurableJettyWebServerFactory>() {
                @Override
                public void customize(ConfigurableJettyWebServerFactory factory) {
                    factory.addServerCustomizers(new JettyServerCustomizer() {
                        @Override
                        public void customize(Server server) {
                            WebAppContext context = resolveWebAppContext(server.getHandler());
                            if (null == context) {
                                throw new IllegalStateException("Unable to resolve jetty's web application context");
                            }
                            ServletContext servletContext = context.getServletContext();
                            boot(servletContext);
                        }

                        private WebAppContext resolveWebAppContext(Handler handler) {
                            if (handler instanceof WebAppContext) {
                                return (WebAppContext) handler;
                            }
                            if (handler instanceof HandlerWrapper) {
                                return resolveWebAppContext(((HandlerWrapper) handler).getHandler());
                            }
                            return null;
                        }
                    });
                }
            };
        }

    }

}