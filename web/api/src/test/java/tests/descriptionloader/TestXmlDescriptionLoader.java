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

import app.controllers.api.RestApiController;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.web.App;
import leap.web.action.Argument;
import leap.web.api.meta.desc.*;
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

    @Inject
    private App app;

    @Test
    public void testLoad(){
        //OperationDescSet ods = loader.load(new RestApiController());
        Object restApiController = null;
        Object subPathsController = null;
        for(Route route : app.routes()){
            if(route.getController() != null && route.getController() instanceof RestApiController){
                restApiController = route.getController();
            }
            if(route.getController() != null && route.getController() instanceof RestApiController.SubPathsController){
                subPathsController = route.getController();
            }
        }
        OperationDescSet set = container.getAllOperationDescSet(restApiController);
        assertNotNull(set);
        boolean updateRestApi = false;
        boolean idArgument = false;
        boolean getApiPaths = false;
        boolean apiIdArgument = false;
        for(Route route : app.routes().getRoutesByController(restApiController)){
            if(route.getAction() != null){
                String actionName = route.getAction().getName();
                if(Strings.equals(actionName,"updateRestApi")){
                    updateRestApi = true;
                    OperationDesc desc = set.getOperationDesc(route.getAction());
                    assertEquals("简介",desc.getSummary());
                    assertEquals("描述",desc.getDescription());

                    for(Argument argument:route.getAction().getArguments()){
                        if(Strings.equals(argument.getDeclaredName(),"id")){
                            idArgument = true;
                            ParameterDesc paramDesc = desc.getParameter(argument);
                            assertEquals("api主键",paramDesc.getDescription());
                        }
                    }

                }
            }
        }
        for(Route route : app.routes().getRoutesByController(subPathsController)){
            if(route.getAction() != null){
                String actionName = route.getAction().getName();
                if(Strings.equals(actionName,"getApiPaths")){
                    getApiPaths = true;
                    OperationDesc desc = set.getOperationDesc(route.getAction());
                    for(Argument argument:route.getAction().getArguments()){
                        if(Strings.equals(argument.getDeclaredName(),"apiId")){
                            apiIdArgument = true;
                            ParameterDesc paramDesc = desc.getParameter(argument);
                            assertEquals("path的api主键",paramDesc.getDescription());
                        }
                    }

                }
            }
        }

        assertTrue(updateRestApi);
        assertTrue(idArgument);
        assertTrue(getApiPaths);
        assertTrue(apiIdArgument);
    }

}
