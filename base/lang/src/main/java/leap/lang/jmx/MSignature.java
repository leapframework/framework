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

import leap.lang.Arrays2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

final class MSignature {

    private final String   actionName;
    private final String[] parameterTypes;

    MSignature(String actionName, Method method) {
        this.actionName = actionName;

        List<String> types = new ArrayList<>();

        for (Class<?> type : method.getParameterTypes()) {
            types.add(type.getName());
        }

        parameterTypes = types.toArray(new String[0]);
    }

    MSignature(String actionName, String[] parameterTypes) {
        this.actionName = actionName;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MSignature s = (MSignature) o;

        if (!actionName.equals(s.actionName)) {
            return false;
        }

        if(!Arrays2.equals(parameterTypes, s.parameterTypes)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = actionName.hashCode();

        result = 31 * result + parameterTypes.hashCode();

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(actionName).append('(');

        boolean first = true;

        for (String type : parameterTypes) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(type);
            first = false;
        }

        sb.append(')');

        return sb.toString();
    }
}
