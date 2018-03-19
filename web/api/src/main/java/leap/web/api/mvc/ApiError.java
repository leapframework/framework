/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.web.api.mvc;

import leap.lang.http.ContentTypes;
import leap.lang.json.JsonStringable;
import leap.lang.json.JsonWriter;
import leap.lang.meta.annotation.ComplexType;
import leap.web.Renderable;
import leap.web.Request;
import leap.web.Response;

public final class ApiError implements JsonStringable, Renderable {
    public String code;
    public String message;

    public ApiError(){
    }

    public ApiError(String message) {
        this.message = message;
    }

    public ApiError(String code, String message) {
        this.code    = code;
        this.message = message;
    }

    @Override
    public void toJson(JsonWriter w) {
        w.startObject()
                .propertyOptional("code", code)
                .property("message", message)
                .endObject();
    }

    @Override
    public void render(Request request, Response response) throws Throwable {
        response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);
        toJson(response.getJsonWriter());
    }

    void response(Response response){
        response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);
        toJson(response.getJsonWriter());
    }

    @Override
    public String toString() {
        return toJson();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}