/*
 * Copyright 2016 the original author or authors.
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
package tested.beans;

public class RefBean {

    private String  name;
    private RefBean refBean;
    private Object  refPrimaryBean1;
    private Object  refPrimaryBean2;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RefBean getRefBean() {
        return refBean;
    }

    public void setRefBean(RefBean refBean) {
        this.refBean = refBean;
    }

    public Object getRefPrimaryBean1() {
        return refPrimaryBean1;
    }

    public void setRefPrimaryBean1(Object refPrimaryBean1) {
        this.refPrimaryBean1 = refPrimaryBean1;
    }

    public Object getRefPrimaryBean2() {
        return refPrimaryBean2;
    }

    public void setRefPrimaryBean2(Object refPrimaryBean2) {
        this.refPrimaryBean2 = refPrimaryBean2;
    }
}
