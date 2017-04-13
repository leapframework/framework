/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests;

import leap.core.annotation.Inject;
import leap.core.web.path.PathTemplateFactory;
import leap.web.WebTestCase;
import leap.web.action.ActionManager;
import leap.web.action.FuncActionBuilder;
import leap.web.action.FuncArgumentBuilder;
import leap.web.route.RouteBuilder;
import leap.web.route.RouteManager;
import leap.web.route.Routes;
import org.junit.Test;

public class FuncActionTest extends WebTestCase {

    protected @Inject RouteManager        rm;
    protected @Inject ActionManager       am;
    protected @Inject PathTemplateFactory ptf;

    @Test
    public void testCreateFuncAction() {
        Routes routes = rm.createRoutes();

        FuncActionBuilder action = new FuncActionBuilder();
        action.addArgument(new FuncArgumentBuilder("p", String.class));
        action.setFunction((params) -> {
            return params.get(0) + "_hello";
        });

        RouteBuilder route = new RouteBuilder("*", ptf.createPathTemplate("/t"),action.build());
        rm.loadRoute(routes, route);
    }

}
