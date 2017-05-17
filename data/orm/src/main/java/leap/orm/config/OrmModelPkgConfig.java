/*
 *
 *  * Copyright 2013 the original author or authors.
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

package leap.orm.config;

/**
 * @author kael.
 */
public class OrmModelPkgConfig {
    private String pkg;
    private Object source;

    public OrmModelPkgConfig(String pkg, Object source) {
        this.setPkg(pkg);
        this.source = source;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        if(!pkg.endsWith(".")) {
            pkg = pkg + ".";
        }
        this.pkg = pkg;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }
}
