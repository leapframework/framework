/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package leap.web.view;

import leap.core.AppConfig;
import leap.core.AppConfigException;
import leap.core.config.reader.XmlConfigReaderBase;
import leap.lang.New;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.servlet.Servlets;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;
import leap.web.AppBootstrap;
import leap.web.AppFilter;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static leap.web.AppBootstrap.BOOTSTRAP_ATTR_NAME;

/**
 * web listener, load filter when web app starting
 * @author kael.
 */
@WebListener
public class WebAppFilterStarter extends XmlConfigReaderBase implements ServletContextListener {
    private static final Log log = LogFactory.get(WebAppFilterStarter.class);

    public static final String AUTO_LOAD_FILTER_PROPERTY                 = "auto-load-appfilter";
    public static final String DEFAULT_APP_FILTER_NAME                   = "app";
    public static final boolean DEFAULT_AUTO_LOAD_FILTER                 = false;
    public static final String[] DEFAULT_APP_FILTER_URL_PATTERNS         = {"/*"};
    public static final boolean DEFAULT_APP_FILTER_MATCH_AFTER           = false;
    public static final Class<? extends Filter> DEFAULT_APP_FILTER_CLASS = AppFilter.class;
    
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        AppBootstrap bootstrap = AppBootstrap.tryGet(sc);

        if(null == bootstrap) {
            bootstrap = new AppBootstrap(){
                @Override
                protected void onAppConfigReady(AppConfig config, Map<String, String> initParams) {
                    initDynamicFilter(sc, config,initParams);
                    super.onAppConfigReady(config, initParams);
                }
            };
            Map<String, String> params = New.hashMap();
            params.putAll(Servlets.getInitParamsMap(sc));
            bootstrap.contextInitialized(sce);
        }
    }

    private void initDynamicFilter(ServletContext sc, AppConfig config, Map<String, String> initParams){
        boolean autoLoadFilter = config.getBooleanProperty(AUTO_LOAD_FILTER_PROPERTY,DEFAULT_AUTO_LOAD_FILTER);
        if(autoLoadFilter){
            FilterRegistration.Dynamic dynamic = sc.addFilter(DEFAULT_APP_FILTER_NAME, DEFAULT_APP_FILTER_CLASS);
            dynamic.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST),DEFAULT_APP_FILTER_MATCH_AFTER,DEFAULT_APP_FILTER_URL_PATTERNS);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
