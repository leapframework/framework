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

package leap.web.api.meta.model;

import leap.lang.meta.MType;

public class MApiHeaderBuilder extends MApiNamedWithDescBuilder<MApiHeader> {

    protected MType type;

    public MApiHeaderBuilder() {

    }

    public MApiHeaderBuilder(MApiHeader header) {
        super(header);
        this.type = header.getType();
    }

    public MType getType() {
        return type;
    }

    public void setType(MType type) {
        this.type = type;
    }

    @Override
    public MApiHeader build() {
        return new MApiHeader(name, type, title, summary, description, attrs);
    }
}
