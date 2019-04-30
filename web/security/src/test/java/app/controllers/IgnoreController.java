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

import leap.core.security.annotation.Ignore;
import leap.lang.http.HTTP;
import leap.web.Request;
import leap.web.Response;

/**
 * @author kael.
 */
@Ignore
public class IgnoreController {
    
    public void ignore(Request request, Response response){
        if(null == request.getUser()){
            response.setStatus(HTTP.SC_OK);
        }else {
            response.setStatus(HTTP.SC_FORBIDDEN);
        }
    }
    
}
