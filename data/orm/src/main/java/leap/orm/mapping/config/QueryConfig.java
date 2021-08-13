/*
 * Copyright 2020 the original author or authors.
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

package leap.orm.mapping.config;

public class QueryConfig {

    private final Integer maxPageSize;
    private final Integer defaultPageSize;

    public QueryConfig(Integer maxPageSize, Integer defaultPageSize) {
        this.maxPageSize = maxPageSize;
        this.defaultPageSize = defaultPageSize;
    }

    public Integer getMaxPageSize() {
        return maxPageSize;
    }

    public Integer tryGetMaxPageSize(Integer maxPageSize) {
        if (null == this.maxPageSize) {
            return maxPageSize;
        }
        return this.maxPageSize;
    }

    public Integer getDefaultPageSize() {
        return defaultPageSize;
    }

    public int mustGetDefaultPageSize(int defaultPageSize) {
        if (null == this.defaultPageSize) {
            return defaultPageSize;
        }
        return this.defaultPageSize;
    }

}
