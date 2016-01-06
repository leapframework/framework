/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app3.controllers;

import leap.core.annotation.Inject;
import leap.oauth2.wac.token.WacTokenManager;
import leap.web.Request;
import leap.web.Results;
import leap.web.view.ViewData;

public class HomeController {
    
    protected @Inject WacTokenManager atm;

    public void index(Request request, ViewData vd) {
        vd.put("at", atm.currentAccessToken(request));
    }
    
    public void refreshToken(Request request, ViewData vd) {
        atm.refreshAndSaveAccessToken(request);
        vd.put("at", atm.currentAccessToken(request));
        Results.renderView("/index");
    }
    
}
