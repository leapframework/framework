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

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;
import leap.web.api.config.ApiConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by kael on 2016/11/14.
 */
public class ModelDescriptionLoader implements DescriptionLoader<Class<?>,ModelDesc> {

    private static final Log log = LogFactory.get(ModelDescriptionLoader.class);

    public static final String MODEL_ELEMENT               = "model";
    public static final String MODEL_PROPERTY_ELEMENT      = "property";
    public static final String MODEL_PROPERTY_DESC_ELEMENT = "description";
    public static final String PROPERTY_ATTR_NAME          = "name";

    @Override
    public ModelDesc load(ApiDescContainer container, Class<?> modelClass) {
        XmlReader reader = getConfigReader(modelClass);
        if(reader == null){
            return null;
        }
        DefaultModelDesc desc = new DefaultModelDesc();
        container.addModelDesc(modelClass,desc);
        reader.nextToStartElement(MODEL_ELEMENT);
        while(reader.nextWhileNotEnd(MODEL_ELEMENT)){
            if(reader.isStartElement(MODEL_PROPERTY_ELEMENT)){
                readPropertyDesc(container,desc,reader);
            }
        }
        return desc;
    }

    protected void readPropertyDesc(ApiDescContainer container, DefaultModelDesc desc, XmlReader reader) {
        String name = reader.resolveRequiredAttribute(PROPERTY_ATTR_NAME);
        String description = null;
        while(reader.nextWhileNotEnd(MODEL_PROPERTY_ELEMENT)){
            if(reader.isStartElement(MODEL_PROPERTY_DESC_ELEMENT)){
                if(description != null){
                    throw new ApiConfigException("duplicate description of property " +name+ "in "+reader.getSource());
                }
                description = reader.resolveElementTextAndEnd();
            }
        }
        DefaultModelDesc.DefaultPropertyDesc pdesc = new DefaultModelDesc.DefaultPropertyDesc();
        pdesc.setDesc(description);
        desc.addPropertyDesc(name,pdesc);
    }


    protected XmlReader getConfigReader(Class<?> modelClass){
        URL url = modelClass.getResource(modelClass.getSimpleName()+".xml");
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
