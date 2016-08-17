/*
 *
 *  * Copyright 2016 the original author or authors.
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

package leap.orm.tested.model.api;

import leap.core.validation.annotations.Required;
import leap.lang.http.HTTP;
import leap.orm.annotation.AutoCreateTable;
import leap.orm.annotation.Column;
import leap.orm.annotation.ManyToOne;

import java.util.List;
import java.util.Map;

@AutoCreateTable
public class ApiOperation extends ModelWithDesc {

    @Column
    @Required
    @ManyToOne(Api.class)
    protected String apiId;

    @Column
    @Required
    @ManyToOne(ApiPath.class)
    protected String pathId;

    @Column
    @Required
    protected String name;

    @Column
    protected String title;

    @Column
    protected HTTP.Method httpMethod;

    @Column
    protected List<Map<String,Object>> parameters;

    @Column
    protected Map<String,Object> responses;

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public HTTP.Method getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HTTP.Method httpMethod) {
        this.httpMethod = httpMethod;
    }

    public List<Map<String, Object>> getParameters() {
        return parameters;
    }

    public void setParameters(List<Map<String, Object>> parameters) {
        this.parameters = parameters;
    }

    public Map<String, Object> getResponses() {
        return responses;
    }

    public void setResponses(Map<String, Object> responses) {
        this.responses = responses;
    }

}