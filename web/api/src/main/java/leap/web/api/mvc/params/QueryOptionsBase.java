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

package leap.web.api.mvc.params;

import leap.lang.json.JsonIgnore;
import leap.web.Params;
import leap.web.Request;
import leap.web.annotation.NonParam;
import leap.web.annotation.ParamsWrapper;
import leap.web.annotation.QueryParam;
import leap.web.api.query.Expand;
import leap.web.api.query.ExpandParser;

@ParamsWrapper
public class QueryOptionsBase {

    protected @QueryParam("select") String select;
    protected @QueryParam("expand") String expand;

    protected Params params;

    @NonParam
    @JsonIgnore
    protected String sqlView;

    @NonParam
    @JsonIgnore
    protected Expand[] resolvedExpands;

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public String getExpand() {
        return expand;
    }

    public void setExpand(String expand) {
        this.expand = expand;
    }

    /**
     * The request params from {@link Request#params()}.
     */
    public Params getParams() {
        return params;
    }

    /**
     * Auto set by {@link leap.web.action.ContextArgumentResolver}.
     */
    public void setParams(Params params) {
        this.params = params;
    }

    public String getSqlView() {
        return sqlView;
    }

    public void setSqlView(String sqlView) {
        this.sqlView = sqlView;
    }

    public Expand[] getResolvedExpands() {
        if (null == resolvedExpands) {
            resolvedExpands = ExpandParser.parse(expand);
        }
        return resolvedExpands;
    }

    public void setResolvedExpands(Expand[] resolvedExpands) {
        this.resolvedExpands = resolvedExpands;
    }
}