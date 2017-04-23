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

package leap.web.api.config.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class RestdConfig {

    protected String      dataSourceName;
    protected boolean     anonymous;
    protected Set<String> includedModels = new LinkedHashSet<>();
    protected Set<String> excludedModels = new LinkedHashSet<>();

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public Set<String> getIncludedModels() {
        return includedModels;
    }

    public void addIncludedModel(String name) {
        includedModels.add(name);
    }

    public Set<String> getExcludedModels() {
        return excludedModels;
    }

    public void addExcludedModel(String name) {
        excludedModels.add(name);
    }

}