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
package leap.core.config;

import leap.core.sys.SysPermissionDef;
import leap.lang.reflect.Reflection;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.text.PlaceholderResolver;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The context interface for {@link AppConfigProcessor} and {@link AppConfigReader}.
 */
public interface AppConfigContext extends AppConfigContextBase {

    /**
     * Returns an immutable view of current properties.
     */
    Map<String,String> getProperties();

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
     * Returns the config extension of creates a new one if not exists.
     */
    default <T> T getOrCreateExtension(Class<? super T> type, Class<T> instanceType) {
        T e = (T)getExtension(type);
        if(null == e) {
            e = Reflection.newInstance(instanceType);
            setExtension(type, e);
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