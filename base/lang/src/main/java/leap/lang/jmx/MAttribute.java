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

package leap.lang.jmx;

import leap.lang.reflect.ReflectValued;

import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenType;

class MAttribute  {

    private final MBeanAttributeInfo info;
    private final ReflectValued      valued;
    private final OpenType           openType;

    public MAttribute(MBeanAttributeInfo info, ReflectValued value) {
        this.info = info;
        this.valued = value;
        this.openType = info instanceof OpenMBeanAttributeInfo ? ((OpenMBeanAttributeInfo) info).getOpenType() : null;
    }

    public MBeanAttributeInfo info() {
        return info;
    }

    public ReflectValued valued() {
        return valued;
    }

    public boolean isOpen() {
        return null != openType;
    }

    public OpenType getOpenType() {
        return openType;
    }

}