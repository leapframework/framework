/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core;

import leap.core.sys.SysPermissionDefinition;
import leap.lang.resource.Resource;
import leap.lang.text.PlaceholderResolver;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface AppConfigReaderContext extends AppConfigContext {

    Boolean getDebug();

    void setDebug(boolean debug);

    String getBasePackage();

    void setBasePackage(String bp);

    Locale getDefaultLocale();

    void setDefaultLocale(Locale locale);

    Charset getDefaultCharset();

    void setDefaultCharset(Charset charset);

    Set<String> getAdditionalPackages();

    /**
     * Returns a mutable map contains the read properties.
     */
    Map<String,String> getProperties();

    boolean hasProperty(String name);

    Map<String,List<String>> getArrayProperties();

    boolean hasArrayProperty(String name);

    List<SysPermissionDefinition> getPermissions();

    void addPermission(SysPermissionDefinition p, boolean override);

    void readImportedResource(Resource resource, boolean override);

    /**
     * Returns the {@link PlaceholderResolver}.
     */
    PlaceholderResolver getPlaceholderResolver();

    /**
     * Returns the {@link AppPropertyProcessor} or null.
     */
    AppPropertyProcessor getPropertyProcessor();
}