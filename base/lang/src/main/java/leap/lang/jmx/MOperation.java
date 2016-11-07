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

import leap.lang.reflect.ReflectMethod;

import javax.management.MBeanOperationInfo;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.OpenType;

class MOperation {

    private final MBeanOperationInfo info;
    private final ReflectMethod      method;
    private final OpenType           returnOpenType;

    public MOperation(MBeanOperationInfo info, ReflectMethod method) {
        this.info = info;
        this.method = method;
        this.returnOpenType = info instanceof OpenMBeanOperationInfo ? ((OpenMBeanOperationInfo) info).getReturnOpenType() : null;
    }

    public MBeanOperationInfo getInfo() {
        return info;
    }

    public ReflectMethod getMethod() {
        return method;
    }

    public boolean isOpen() {
        return null != returnOpenType;
    }

    public OpenType getReturnOpenType() {
        return returnOpenType;
    }
}
