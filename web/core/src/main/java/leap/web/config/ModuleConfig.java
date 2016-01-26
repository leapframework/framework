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
package leap.web.config;

/**
 * The configuration of a web module.
 */
public interface ModuleConfig {

    /**
     * Required.
     *
     * Returns the unique name of module.
     */
    String getName();

    /**
     * Required.
     *
     * Returns the base path of module.
     *
     * <p/>
     * The base path must be starts with '/' and ends without '/';
     */
    String getBasePath();

    /**
     * Required.
     *
     * Returns the base package of module.
     */
    String getBasePackage();

}