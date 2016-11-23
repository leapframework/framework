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

import leap.web.security.SecurityTestCase;
import org.junit.Test;

/**
 * Created by kael on 2016/11/23.
 */
public class SecureControllerTest extends SecurityTestCase {
    @Test
    public void testMultiPermission(){
        login();

        get("/secure/p1p2p3").assert403();
        get("/secure/p1p2").assertOk();
        get("/secure/r1r2p1p2").assertOk();
        get("/secure/r1r3p1p2").assertOk();
        get("/secure/r3p1p2").assert403();

        logout();
    }

}
