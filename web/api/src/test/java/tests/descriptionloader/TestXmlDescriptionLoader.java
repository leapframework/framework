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

package tests.descriptionloader;

import app.controllers.api.RestPathController;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.web.App;
import leap.web.api.meta.desc.ApiDescContainer;
import leap.web.api.meta.desc.DefaultApiDescContainer;
import leap.web.api.meta.desc.DescriptionLoader;
import leap.web.api.meta.desc.OperationDescSet;
import leap.web.route.Route;
import leap.webunit.WebTestBase;
import org.junit.Test;

/**
 * Created by kael on 2016/11/9.
 */
public class TestXmlDescriptionLoader extends WebTestBase {
    @Inject
    private DescriptionLoader loader;
    @Inject
    private BeanFactory factory;
    @Inject
    private ApiDescContainer container;

    @Test
    public void testLoad(){
        //OperationDescSet ods = loader.load(new RestApiController());
        RestPathController controller = factory.getBean(RestPathController.class);
        OperationDescSet ods = loader.load(container,controller);
        assertNotNull(ods);
        

    }

}
