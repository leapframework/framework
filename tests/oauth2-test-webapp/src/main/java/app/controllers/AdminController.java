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
package app.controllers;

import leap.core.security.annotation.AllowAnonymous;
import leap.core.security.annotation.AllowClientOnly;
import leap.core.security.annotation.Permissions;

@AllowClientOnly
public class AdminController {

    public void hello() {
        
    }

    @Permissions("admin:status")
    public void status() {

    }

    @Permissions("admin:test")
    public void test(){

    }

    @AllowAnonymous
    public String allowAnonymous(){
        return "success";
    }
}