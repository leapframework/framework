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

package app.controllers.api;

import leap.core.security.annotation.AllowAnonymous;
import leap.web.action.ControllerBase;
import leap.web.api.annotation.Log;

/**
 * Created by kael on 2016/10/11.
 */
@AllowAnonymous
public class LogController extends ControllerBase {
    @Log(title = "测试操作", description = "测试操作${name}")
    public void index(String name){

    }

}
