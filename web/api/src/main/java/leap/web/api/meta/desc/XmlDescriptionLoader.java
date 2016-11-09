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

package leap.web.api.meta.desc;

import leap.core.AppConfig;
import leap.core.AppResources;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by kael on 2016/11/8.
 */
public class XmlDescriptionLoader implements DescriptionLoader {

    private static final Log log = LogFactory.get(XmlDescriptionLoader.class);

    @Inject
    protected AppConfig config;

    public static final String CONTROLLER_ELEMENT = "controller";
    public static final String METHOD_ELEMENT     = "method";
    public static final String PARAMETER_ELEMENT  = "parameter";

    public static final String METHOD_ATTR_NAME   = "name";
    public static final String PARAMETER_ARRT_NAME= "name";

    @Override
    public OperationDescSet load(ApiDescContainer container, Object controller) {
        XmlReader reader = getConfigReader(controller);
        if(reader == null){
            return null;
        }
        try {
            return readDescSet(container,controller,reader);
        }finally {
            IO.close(reader);
        }
    }

    protected OperationDescSet readDescSet(ApiDescContainer container, Object controller,XmlReader reader){
        while (reader.nextToStartElement(CONTROLLER_ELEMENT)){

        }

        return null;
    }

    protected XmlReader getConfigReader(Object controller){
        URL url = controller.getClass().getResource(controller.getClass().getSimpleName()+".xml");
        if(url == null){
            return null;
        }
        try (InputStream is = url.openStream()){
            return XML.createReader(is);
        }catch (IOException e){
            log.error(e);
            return null;
        }

    }

}
