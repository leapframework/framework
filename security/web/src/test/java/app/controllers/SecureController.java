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

package app.controllers;

import leap.core.security.annotation.Secured;

/**
 * Created by kael on 2016/11/23.
 */
public class SecureController {
    @Secured(permissions = {"p1","p2","p3"})
    public String p1p2p3(){
        return "ok";
    }
    @Secured(permissions = {"p1","p2"})
    public String p1p2(){
        return "ok";
    }
    @Secured(roles = {"r1","r2"},permissions = {"p1","p2"})
    public String r1r2p1p2(){
        return "ok";
    }
    @Secured(roles = {"r1","r3"},permissions = {"p1","p2"})
    public String r1r3p1p2(){
        return "ok";
    }
    @Secured(roles = "r3",permissions = {"p1","p2"})
    public String r3p1p2(){
        return "ok";
    }
}
