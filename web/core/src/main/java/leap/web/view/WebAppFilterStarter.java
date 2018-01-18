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
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;
import leap.web.AppFilter;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * web listener, load filter when web app starting
 * @author kael.
 */
@WebListener
public class WebAppFilterStarter extends XmlConfigReaderBase implements ServletContextListener {
    private static final Log log = LogFactory.get(WebAppFilterStarter.class);

    public static final String AUTO_LOAD_FILTER_ELEMENT = "auto-load-filter";
    public static final String FILTER_CLASS_ELEMENT = "filter-class";
    public static final String FILTER_NAME_ELEMENT = "filter-name";
    public static final String FILTER_URL_PATTERN_ELEMENT = "url-pattern";
    public static final String FILTER_MATCH_AFTER_ELEMENT = "match-after";
    public static final String DEFAULT_FILTER_NAME = "app";
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (InputStream is = this.getClass().getResourceAsStream("/conf/config.xml");
             InputStreamReader reader = new InputStreamReader(is, AppConfig.DEFAULT_CHARSET)){
            XmlReader xml = XML.createReader(reader);
            if (xml.nextToStartElement(AUTO_LOAD_FILTER_ELEMENT)){
                String filterName = DEFAULT_FILTER_NAME;
                Class<? extends Filter> filterClass = AppFilter.class;
                boolean matchAfter = false;
                List<String> urlPattern = new ArrayList<>();
                while (xml.nextWhileNotEnd(AUTO_LOAD_FILTER_ELEMENT)){
                    if(xml.isStartElement(FILTER_CLASS_ELEMENT)){
                        Class<?> c = Class.forName(xml.getElementTextAndEnd());
                        if(!Filter.class.isAssignableFrom(c)){
                            throw new AppConfigException(Filter.class.getName() + " is not assignable form " + c.getName());
                        }
                        continue;
                    }
                    if(xml.isStartElement(FILTER_NAME_ELEMENT)){
                        filterName = xml.getElementTextAndEnd();
                        continue;
                    }
                    if(xml.isStartElement(FILTER_URL_PATTERN_ELEMENT)){
                        urlPattern.add(xml.getElementTextAndEnd());
                        continue;
                    }
                    if(xml.isStartElement(FILTER_MATCH_AFTER_ELEMENT)){
                        matchAfter = Boolean.parseBoolean(xml.getElementTextAndEnd());
                        continue;
                    }
                }
                if(urlPattern.size() <= 0){
                    urlPattern.add("/*");
                }
                FilterRegistration.Dynamic dynamic = sce.getServletContext().addFilter(filterName, filterClass);
                dynamic.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST),matchAfter,urlPattern.toArray(new String[urlPattern.size()]));
            }else {
                log.debug(AUTO_LOAD_FILTER_ELEMENT+" has not found in classpath:/conf/config.xml, skip auto load Filter");
            }
        } catch (IOException e) {
            log.warn("read classpath:/conf/config.xml error, skip WebAppFilterStarter",e);
        } catch (ClassNotFoundException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
