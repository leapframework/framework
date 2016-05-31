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

import leap.core.ds.DataSourceConfig;
import leap.core.sys.SysPermissionDef;
import leap.lang.accessor.PropertyAccessor;
import leap.lang.reflect.Reflection;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.text.PlaceholderResolver;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The context interface for {@link AppConfigProcessor} and {@link AppConfigReader}.
 */
public interface AppConfigContext extends AppConfigContextBase, PropertyAccessor {

    /**
     * Returns the {@link AppConfig#INIT_PROPERTY_DEBUG} property or null.
     */
    Boolean getDebug();

    /**
     * Returns the {@link AppConfig#INIT_PROPERTY_BASE_PACKAGE} property or null.
     */
    String getBasePackage();

    /**
     * Returns the {@link AppConfig#INIT_PROPERTY_DEFAULT_LOCALE} property or null.
     */
    Locale getDefaultLocale();

    /**
     * Returns the {@link AppConfig#INIT_PROPERTY_DEFAULT_CHARSET} property or null.
     */
    Charset getDefaultCharset();

    /**
     * Sets the {@link AppConfig#INIT_PROPERTY_DEBUG} property.
     */
    void setDebug(boolean debug);

    /**
     * Sets the {@link AppConfig#INIT_PROPERTY_BASE_PACKAGE} property.
     */
    void setBasePackage(String bp);

    /**
     * Sets the {@link AppConfig#INIT_PROPERTY_DEFAULT_LOCALE} property.
     */
    void setDefaultLocale(Locale locale);

    /**
     * Sets the {@link AppConfig#INIT_PROPERTY_DEFAULT_CHARSET} property.
     */
    void setDefaultCharset(Charset charset);

    /**
     * Returns a mutable map contains the properties.
     */
    Map<String,String> getProperties();

    /**
     * Returns true if the property exists.
     */
    boolean hasProperty(String name);

    /**
     * Puts all the config properties.
     */
    void putProperties(Map<String,String> props);

    /**
     * Return a mutable map contains the array properties.
     */
    Map<String,List<String>> getArrayProperties();

    /**
     * Returns true if the array property exists.
     */
    boolean hasArrayProperty(String name);

    /**
     * Returns the config extension or null if not exists.
     */
	<T> T getExtension(Class<T> type);

    /**
     * Returns the config extension of creates a new one if not exists.
     */
	default <T> T getOrCreateExtension(Class<T> type) {
		T e = getExtension(type);
		if(null == e) {
			e = Reflection.newInstance(type);
			setExtension(e);
		}
		return e;
	}

    /**
     * Sets the config extension.
     */
	<T> void setExtension(T extension);

    /**
     * Sets the config extension.
     */
	<T> void setExtension(Class<T> type, T extension);

    /**
     * Adds a managed resource.
     */
	void addResource(Resource r);

    /**
     * Adds a managed resource set.
     */
	void addResources(ResourceSet rs);

    /**
     * Returns true if the app already has a default configuration of data source.
     */
	boolean hasDefaultDataSourceConfig();

    /**
     * Returns true if the app already has a named configuration of data source.
     */
	boolean hasDataSourceConfig(String name);

    /**
     * Sets the configuration of data source.
     */
	void setDataSourceConfig(String name,DataSourceConfig.Builder conf);

    /**
     * Returns a mutable set contains all the additional packages.
     */
    Set<String> getAdditionalPackages();

    /**
     * Returns all the {@link SysPermissionDef}.
     */
    List<SysPermissionDef> getPermissions();

    /**
     * Adds a {@link SysPermissionDef}.
     */
    void addPermission(SysPermissionDef p, boolean override);

    //todo :
    PlaceholderResolver getPlaceholderResolver();
}