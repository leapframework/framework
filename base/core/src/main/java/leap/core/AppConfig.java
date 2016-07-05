/*
 * Copyright 2013 the original author or authors.
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
package leap.core;

import leap.core.config.AppConfigContext;
import leap.core.config.AppConfigProcessor;
import leap.core.config.dyna.*;
import leap.core.sys.SysPermissionDef;
import leap.lang.Charsets;
import leap.lang.Locales;
import leap.lang.accessor.PropertyGetter;
import leap.lang.resource.ResourceSet;
import leap.lang.text.PlaceholderResolver;

import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface AppConfig extends PropertyGetter {

    String SYS_PROPERTY_PROFILE          = "app.profile";
    String INIT_PROPERTY_PROFILE         = "profile";
    String INIT_PROPERTY_DEBUG           = "debug";
	String INIT_PROPERTY_LAZY_TEMPLATE	 = "lazy-template";
    String INIT_PROPERTY_BASE_PACKAGE    = "base-package";
    String INIT_PROPERTY_DEFAULT_CHARSET = "default-charset";
    String INIT_PROPERTY_DEFAULT_LOCALE  = "default-locale";

    String PROPERTY_SECRET         = "secret";
    String PROPERTY_PRIVATE_KEY    = "private_key";
    String PROPERTY_HOME           = "home";
    String PROPERTY_RELOAD_ENABLED = "reload-enabled";

    String  PROFILE_DEVELOPMENT  = "dev";
    String  PROFILE_PRODUCTION   = "prod";
    String  DEFAULT_BASE_PACKAGE = "app";
    Locale  DEFAULT_LOCALE       = Locales.DEFAULT_LOCALE;
    Charset DEFAULT_CHARSET      = Charsets.UTF_8;

    /**
	 * Returns current profile name.
	 */
	String getProfile();

    /**
     * Returns true if the profile is {@link #PROFILE_DEVELOPMENT}.
     */
    boolean isDev();
	
	/**
	 * Returns <code>true</code> if current app is running in debug mode. 
	 */
	boolean isDebug();

	/**
	 * Returns <code>true</code> if current app is running in lazy template mode.
	 */
	boolean isLazyTemplate();
	/**
	 * Returns the base package name of current application, i.e. <code>com.example</code>
	 * 
	 * <p>
	 * Returns <code>null</code> if not configured.
	 */
	String getBasePackage();
	
	/**
	 * Returns the default {@link Locale} of current application.
	 */
	Locale getDefaultLocale();

	/**
	 * Returns the default character encoding of current application.
	 */
	Charset getDefaultCharset();
	
	/**
	 * Returns <code>true</code> if current app enabled to reload the managed resources such as messages,assets,etc.
	 * 
	 * <p>
	 * In debug mode the default value is <code>true</code> else <code>false</code>
	 */
	boolean isReloadEnabled();
	
	/**
	 * Returns the value of property 'secret'.
	 * 
	 * <p>
	 * Returns <code>null</code> if no secret key was configured.
	 */
	String getSecret();
	
	/**
	 * Returns the secret key if exists.
	 * 
	 * <p>
	 * Generates (or loads from saved key store) a secret key if the key was not configured.
	 */
	String ensureGetSecret();
	
	/**
	 * Returns the decoded private key of property 'private_key';
	 * 
	 * <p>
	 * Returns the <code>null</code> if not private key configured.
	 */
	PrivateKey getPrivateKey();
	
	/**
	 * Return the private key if exists.
	 * 
	 * <p>
	 * Generates (or loads from saved key store) a private key if not exists.
	 */
	PrivateKey ensureGetPrivateKey();

	/**
	 * Return the public key if exists.
	 *
	 * <p>
	 * Generates (or loads from saved key store) a public key if not exists.
	 */
	PublicKey ensureGetPublicKey();

	/**
	 * Returns a config extension object for the given type or <code>null</code> if no extension.
	 * 
	 * @see AppConfigContext
	 * @see AppConfigProcessor
	 */
	<T> T getExtension(Class<T> type);

    /**
     * Returns all the config extensions.
     */
    Map<Class<?>, Object> getExtensions();

	/**
	 * Removes and returns a config extension object for the given type or <code>null</code> if no extension.
	 * 
	 * @see AppConfigContext
	 * @see AppConfigProcessor
	 */
	<T> T removeExtension(Class<T> type);

    /**
     * Returns the raw value of property or null if not exists.
     */
    String getProperty(String name);
	
	/**
	 * Returns the configured property value as String, or returns the default value if empty.
	 */
	String getProperty(String name,String defaultValue);
	
	/**
	 * Returns the configured property value as the given type, or returns <code>null</code> if empty.
	 */
	<T> T getProperty(String name,Class<T> type);
	
	/**
	 * Returns the configured property value as the given type, or returns the default value if empty.
	 */
	<T> T getProperty(String name,Class<T> type,T defaultValue);

    /**
     * Returns the property value as {@link Boolean} type.
     */
    default boolean getBooleanProperty(String name) {
        return getBooleanProperty(name, false);
    }
	
	/**
	 * Returns the property value as {@link Boolean} type.
	 */
	default boolean getBooleanProperty(String name,boolean defaultValue) {
        return getProperty(name, Boolean.class, defaultValue);
    }

    /**
     * Returns the property value as {@link Integer} type.
     */
    default int getIntProperty(String name) {
        return getIntProperty(name, 0);
    }
	
	/**
	 * Returns the property value as {@link Integer} type.
	 */
	default int getIntProperty(String name,int defaultValue) {
        return getProperty(name, Integer.class, defaultValue);
    }

    /**
     * Returns the property value as {@link Long} type.
     */
    default long getLongProperty(String name) {
        return getLongProperty(name, 0L);
    }

    /**
     * Returns the property value as {@link Long} type.
     */
    default long getLongProperty(String name, long defaultValue) {
        return getProperty(name, Long.class, defaultValue);
    }

    /**
     * Returns the property value as {@link Double} type.
     */
    default double getDoubleProperty(String name) {
        return getDoubleProperty(name, 0.0d);
    }

    /**
     * Returns the property value as {@link Double} type.
     */
    default double getDoubleProperty(String name, double defaultValue) {
        return getProperty(name, Double.class, defaultValue);
    }

    /**
     * An array property is ends with chars <code>[]</code>.
     *
     * <p/>
     * Returns null if no the array property.
     *
     * <p/>
     *
     * For example, following properties configuration will return an array : [a,b].
     * <pre>
     *     prop1[] = a
     *     prop1[] = b
     * </pre>
     */
    String[] getArrayProperty(String name);

    /**
     * Returns the wrapped property with or without underlying property value.
     *
     * <p/>
     * Never returns null.
     */
    <T> Property<T> getDynaProperty(String name, Class<T> type);

    /**
     * @see {@link #getDynaProperty(String, Class)}
     */
    StringProperty getDynaProperty(String name);

    /**
     * @see {@link #getDynaProperty(String, Class)}.
     */
    IntegerProperty getDynaIntegerProperty(String name);

    /**
     * @see {@link #getDynaProperty(String, Class)}.
     */
    LongProperty getDynaLongProperty(String name);

    /**
     * @see {@link #getDynaProperty(String, Class)}.
     */
    BooleanProperty getDynaBooleanProperty(String name);

    /**
     * @see {@link #getDynaProperty(String, Class)}.
     */
    DoubleProperty getDynaDoubleProperty(String name);

    /**
     * Binding the property.
     */
    <T> void bindDynaProperty(String name, Class<T> type, Property<T> p);

	/**
	 * Returns an immutable map contains all the configured properties.
	 */
	Map<String,String> getProperties();
	
	/**
	 * Returns all the resources configured in app configs.
	 */
	ResourceSet getResources();
	
	/**
	 * Returns a {@link PlaceholderResolver} to resolve string value.
	 */
	PlaceholderResolver getPlaceholderResolver();

    /**
     * Returns all the sys permissions.
     */
    List<SysPermissionDef> getPermissions();
}