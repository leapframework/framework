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

package tested.beans.proxy;

public class TBeanProxy1 implements TBeanType1 {

    private final TBeanType1 targetBean;

    public TBeanProxy1(TBeanType1 targetBean) {
        this.targetBean = targetBean;
    }

    public TBeanType1 getTargetBean() {
        return targetBean;
    }

    @Override
    public int getCount() {
        return targetBean.getCount() + 1;
    }
}