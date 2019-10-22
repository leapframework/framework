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

import leap.core.config.AppProperty;
import leap.core.config.AppPropertyPrinter;
import leap.core.config.SimpleAppProperty;
import leap.core.config.dyna.*;
import leap.core.config.dyna.exception.UnsupportedBindDynaPropertyException;
import leap.core.config.dyna.exception.UnsupportedDynaPropertyException;
import leap.core.monitor.SimpleMonitorProvider;
import leap.core.sys.SysPermissionDef;
import leap.lang.*;
import leap.lang.annotation.Name;
import leap.lang.asm.tree.analysis.Value;
import leap.lang.convert.Converts;
import leap.lang.io.IO;
import leap.lang.json.JSON;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.FileResource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import leap.lang.security.RSA;
import leap.lang.security.RSA.RsaKeyPair;
import leap.lang.text.DefaultPlaceholderResolver;
import leap.lang.text.PlaceholderResolver;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A default implementation of {@link AppConfig}
 */
public class DefaultAppConfig extends AppConfigBase implements AppConfig {
    private static final Log log = LogFactory.get(DefaultAppConfig.class);

    protected AppConfigSupport[]         initialSupports     = new AppConfigSupport[0];
    protected AppConfigSupport[]         preSupports         = new AppConfigSupport[0];
    protected AppConfigSupport[]         postSupports        = new AppConfigSupport[0];
    protected PropertyProvider           propertyProvider    = null;
    protected String                     profile             = null;
    protected Boolean                    debug               = null;
    protected Boolean                    lazyTemplate        = null;
    protected String                     basePackage         = null;
    protected Set<String>                additionalPackages  = new LinkedHashSet<>();
    protected Locale                     defaultLocale       = null;
    protected Charset                    defaultCharset      = null;
    protected Boolean                    reloadEnabled       = null;
    protected String                     secret              = null;
    protected PrivateKey                 privateKey          = null;
    protected PublicKey                  publicKey           = null;
    protected Map<Class<?>, Object>      extensions          = new HashMap<>();
    protected Map<Class<?>, Object>      extensionsReadonly  = Collections.unmodifiableMap(extensions);
    protected Map<String, Object>        properties          = new ConcurrentHashMap<>();
    protected Map<String, List<String>>  arrayProperties     = new ConcurrentHashMap<>();
    protected List<SysPermissionDef>     permissions         = new ArrayList<>();
    protected List<SysPermissionDef>     permissionsReadonly = Collections.unmodifiableList(permissions);
    protected ResourceSet                resources           = null;
    protected DefaultPlaceholderResolver placeholderResolver = new DefaultPlaceholderResolver(this);

    public DefaultAppConfig(String profile, AppConfigSupport[] initialSupports) {
        this.profile = profile;
        this.initialSupports = initialSupports;
        this.properties.put(INIT_PROPERTY_PROFILE, profile);
    }

    @Override
    public void setPreSupports(AppConfigSupport... supports) {
        this.preSupports = supports;
    }

    @Override
    public void setPostSupports(AppConfigSupport... supports) {
        this.postSupports = supports;
    }

    public void setPropertyProvider(PropertyProvider p) {
        this.propertyProvider = p;
    }

    protected void loadProperty(String name, AppProperty p) {
        if(null == p) {
            return;
        }

        String value = p.getValue();
        if (null == value) {
            return;
        }

        //array property
        if (name.endsWith("[]")) {
            name = name.substring(0, name.length() - 2);

            List<String> values = arrayProperties.get(name);
            if (null == values) {
                values = new ArrayList<>();
                arrayProperties.put(name, values);
            }

            values.add(value);
        } else {
            properties.put(name, p);
        }
    }

    protected void loadProperties(Map<String, AppProperty> map) {
        for (Entry<String, AppProperty> entry : map.entrySet()) {
            loadProperty(entry.getKey(), entry.getValue());
        }
    }

    protected void loadArrayProperties(Map<String, List<String>> map) {
        for (Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();

            List<String> list = arrayProperties.get(key);
            if (null == list) {
                list = new ArrayList<>();
                arrayProperties.put(key, list);
            }

            list.addAll(entry.getValue());
        }
    }

    @Override
    public String getProfile() {
        return profile;
    }

    @Override
    public boolean isDev() {
        return PROFILE_DEVELOPMENT.equals(profile);
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public boolean isLazyTemplate() {
        return lazyTemplate;
    }

    @Override
    public String getBasePackage() {
        return basePackage;
    }

    @Override
    public Set<String> getAdditionalPackages() {
        return additionalPackages;
    }

    @Override
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    @Override
    public Charset getDefaultCharset() {
        return defaultCharset;
    }

    @Override
    public boolean isReloadEnabled() {
        return reloadEnabled;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    @Override
    public String ensureGetSecret() {
        if (Strings.isEmpty(secret)) {
            secret = loadOrGenerateSecret();
        }
        return secret;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public PrivateKey ensureGetPrivateKey() {
        if (null == privateKey) {
            privateKey = loadOrGeneratePrivateKey();
        }
        return privateKey;
    }

    @Override
    public PublicKey ensureGetPublicKey() {
        if (null == publicKey) {
            publicKey = loadOrGeneratePublicKey();
        }
        return publicKey;
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> map = new LinkedHashMap<>();
        getPropertyNames().forEach(name -> map.put(name, getProperty(name)));
        return map;
    }

    @Override
    public Set<String> getPropertyNames() {
        Set<String> set = new LinkedHashSet<>();

        addPropertyNames(set, initialSupports);
        addPropertyNames(set, preSupports);
        set.addAll(properties.keySet());
        addPropertyNames(set, postSupports);

        return set;
    }

    protected void addPropertyNames(Set<String> set, AppConfigSupport[] supports) {
        for (AppConfigSupport support : supports) {
            Set<String> names = support.getPropertyNames();
            if (null != names) {
                set.addAll(names);
            }
        }
    }

    @Override
    public List<SysPermissionDef> getPermissions() {
        return permissionsReadonly;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExtension(Class<T> type) {
        return (T) extensions.get(type);
    }

    @Override
    public Map<Class<?>, Object> getExtensions() {
        return extensionsReadonly;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T removeExtension(Class<T> type) {
        return (T) extensions.remove(type);
    }

    @Override
    public String getProperty(String name) {
        String v;

        for (AppConfigSupport support : initialSupports) {
            if ((v = support.getProperty(name)) != null) {
                return v;
            }
        }

        for (AppConfigSupport support : preSupports) {
            if ((v = support.getProperty(name)) != null) {
                return v;
            }
        }

        Object p = properties.get(name);

        if (null == p) {
            for (AppConfigSupport support : postSupports) {
                if ((v = support.getProperty(name)) != null) {
                    return v;
                }
            }
        }

        if(null == p) {
            return null;
        }

        if(p instanceof String) {
            return (String)p;
        }else {
            return ((AppProperty)p).getValue();
        }
    }

    protected AppProperty getPropertyWithSource(String name) {
        String v;

        for (AppConfigSupport support : initialSupports) {
            if ((v = support.getProperty(name)) != null) {
                return new SimpleAppProperty(getObjectName(support), name, v);
            }
        }

        for (AppConfigSupport support : preSupports) {
            if ((v = support.getProperty(name)) != null) {
                return new SimpleAppProperty(getObjectName(support), name, v);
            }
        }

        Object p = properties.get(name);

        if (null == p) {
            for (AppConfigSupport support : postSupports) {
                if ((v = support.getProperty(name)) != null) {
                    return new SimpleAppProperty(getObjectName(support), name, v);
                }
            }
        }

        if(p instanceof String) {
            return new SimpleAppProperty("runtime", name, (String)p);
        }else {
            return ((AppProperty)p);
        }
    }

    private static String getObjectName(Object o) {
        Name name = o.getClass().getAnnotation(Name.class);
        if(null != name) {
            return name.value();
        }else if(o instanceof Named){
            return ((Named) o).getName();
        }else {
            return o.getClass().getSimpleName();
        }
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        String v = getProperty(name);
        return Strings.isEmpty(v) ? defaultValue : v;
    }

    @Override
    public <T> T getProperty(String name, Class<T> type) {
        String v = getProperty(name);
        return Strings.isEmpty(v) ? null : Converts.convert(v, type);
    }

    @Override
    public <T> T getProperty(String name, Class<T> type, T defaultValue) {
        String v = getProperty(name);
        return Strings.isEmpty(v) ? defaultValue : Converts.convert(v, type);
    }

    @Override
    public String[] getArrayProperty(String name) {
        List<String> values = arrayProperties.get(name);

        return null == values ? null : values.toArray(Arrays2.EMPTY_STRING_ARRAY);
    }

    @Override
    public StringProperty getDynaProperty(String name) {
        if (null != propertyProvider) {
            try {
                StringProperty p = propertyProvider.getDynaProperty(name);
                log.info("property {} provide by {}", name, propertyProvider.getClass());
                return p;
            } catch (UnsupportedDynaPropertyException e) {
                log.debug("property {} provide by local config", name);
            }
        }
        return new SimpleStringProperty(getProperty(name));
    }

    @Override
    public <T> Property<T> getDynaProperty(String name, Type type, Class<T> cls) {
        if (null != propertyProvider) {
            try {
                Property<T> p = propertyProvider.getDynaProperty(name, type, cls);
                log.info("property {} provide by {}", name, propertyProvider.getClass());
                return p;
            } catch (UnsupportedDynaPropertyException e) {
                log.debug("property {} provide by local config", name);
            }
        }
        String v = getProperty(name);
        if (null == v || v.isEmpty()) {
            return new NullProperty<>();
        }

        TypeInfo ti = Types.getTypeInfo(cls, null);
        if (ti.isComplexType()) {
            return new SimpleProperty<>(cls, JSON.decode(v, cls));
        } else {
            return new SimpleProperty<>(cls, Converts.convert(v, cls));
        }
    }

    @Override
    public IntegerProperty getDynaIntegerProperty(String name) {
        if (null != propertyProvider) {
            try {
                IntegerProperty property = propertyProvider.getDynaIntegerProperty(name);
                log.info("property {} provide by {}", name, propertyProvider.getClass());
                return property;
            } catch (UnsupportedDynaPropertyException e) {
                log.debug("property {} provide by local config", name);
            }
        }
        return new SimpleIntegerProperty(Converts.convert(getProperty(name), Integer.class));
    }

    @Override
    public LongProperty getDynaLongProperty(String name) {
        if (null != propertyProvider) {
            try {
                LongProperty property = propertyProvider.getDynaLongProperty(name);
                log.info("property {} provide by {}", name, propertyProvider.getClass());
                return property;
            } catch (UnsupportedDynaPropertyException e) {
                log.debug("property {} provide by local config", name);
            }
        }
        return new SimpleLongProperty(Converts.convert(getProperty(name), Long.class));
    }

    @Override
    public BooleanProperty getDynaBooleanProperty(String name) {
        if (null != propertyProvider) {
            try {
                BooleanProperty property = propertyProvider.getDynaBooleanProperty(name);
                log.info("property {} provide by {}", name, propertyProvider.getClass());
                return property;
            } catch (UnsupportedDynaPropertyException e) {
                log.debug("property {} provide by local config", name);
            }
        }
        return new SimpleBooleanProperty(Converts.convert(getProperty(name), Boolean.class));
    }

    @Override
    public DoubleProperty getDynaDoubleProperty(String name) {
        if (null != propertyProvider) {
            try {
                DoubleProperty property = propertyProvider.getDynaDoubleProperty(name);
                log.info("property {} provide by {}", name, propertyProvider.getClass());
                return property;
            } catch (UnsupportedDynaPropertyException e) {
                log.debug("property {} provide by local config", name);
            }
        }
        return new SimpleDoubleProperty(Converts.convert(getProperty(name), Double.class));
    }

    @Override
    public <T> void bindDynaProperty(String name, Class<T> type, Property<T> p) {
        if (null == p) {
            return;
        }
        if (null != propertyProvider) {
            try {
                propertyProvider.bindDynaProperty(name, type, p);
                log.info("property {} bind by {}", name, propertyProvider.getClass());
            } catch (UnsupportedBindDynaPropertyException e) {
                log.info("property {} bind by local config", name, propertyProvider.getClass());
                p.convert(getProperty(name));
            }
        } else {
            String v = getProperty(name);
            if(null != v) {
                p.convert(v);
            }
        }
    }

    @Override
    public ResourceSet getResources() {
        return resources;
    }

    @Override
    public PlaceholderResolver getPlaceholderResolver() {
        return placeholderResolver;
    }

    @Override
    public void printProperties(AppPropertyPrinter printer) {
        if(log.isInfoEnabled()){
            Set<AppProperty> props = new HashSet<>();
            getPropertyNames().forEach(name -> {
                AppProperty p = getPropertyWithSource(name);
                if (null != p) {
                    String v = p.getValue();
                    if(v.equals(System.getProperty(name))) {
                        p = new SimpleAppProperty("<sys>", name, v);
                    }else if(v.equals(System.getenv(name))) {
                        p = new SimpleAppProperty("<env>", name, v);
                    }
                    props.add(p);
                }
            });
            StringWriter msg    = new StringWriter();
            PrintWriter  writer = new PrintWriter(msg);
            printer.printProperties(props, writer);
            log.info("Print properties : \n\n{}",msg.toString());
        }
    }

    protected void postLoad() {
        if (null == this.reloadEnabled) {
            Boolean reloadEnabled = getProperty(PROPERTY_RELOAD_ENABLED, Boolean.class);
            if (null == reloadEnabled) {
                this.reloadEnabled = isDebug() ? true : false;
            } else {
                this.reloadEnabled = reloadEnabled;
            }
        }

        if (Strings.isEmpty(secret)) {
            this.secret = getProperty(PROPERTY_SECRET);
        }

        if (null == privateKey) {
            String base64PrivateKey = getProperty(PROPERTY_PRIVATE_KEY);
            if (!Strings.isEmpty(base64PrivateKey)) {
                this.privateKey = RSA.decodePrivateKey(base64PrivateKey);
            }
        }
    }

    public String[] getProfiled(String[] templates) {
        String[] array = new String[templates.length];

        for (int i = 0; i < array.length; i++) {
            array[i] = Strings.replace(templates[i], "{profile}", profile);
        }

        return array;
    }

    protected String loadOrGenerateSecret() {
        FileResource userDir   = Resources.createFileResource(System.getProperty("user.dir"));
        FileResource targetDir = userDir.createRelative("./target");

        File secretFile = targetDir.exists() ? targetDir.createRelative("./.secret").getFile() : userDir.createRelative("./.secret").getFile();

        if (secretFile.exists()) {
            String secret = Strings.trim(IO.readString(secretFile, Charsets.UTF_8));
            if (!Strings.isEmpty(secret)) {
                return secret;
            }
        }

        String secret = Randoms.nextString(32);

        if (getBooleanProperty("app.config.save-generated-secret", true)) {
            IO.writeString(secretFile, secret, Charsets.UTF_8);
        }

        return secret;
    }

    protected PrivateKey loadOrGeneratePrivateKey() {
        String keyContent       = generateOrGetKeyFileContent();
        int    index0           = keyContent.indexOf('#', 1) + 1;
        int    index1           = keyContent.indexOf('#', index0);
        String base64PrivateKey = keyContent.substring(index0, index1).trim();
        return RSA.decodePrivateKey(base64PrivateKey);
    }

    protected PublicKey loadOrGeneratePublicKey() {
        String keyContent      = generateOrGetKeyFileContent();
        int    index0          = keyContent.indexOf('#', 1) + 1;
        int    index1          = keyContent.indexOf('#', index0) + 1;
        int    index2          = keyContent.indexOf('#', index1) + 1;
        String base64PublicKey = keyContent.substring(index2).trim();
        return RSA.decodePublicKey(base64PublicKey);
    }

    protected String generateOrGetKeyFileContent() {
        FileResource userDir   = Resources.createFileResource(System.getProperty("user.dir"));
        FileResource targetDir = userDir.createRelative("./target");

        File keyFile = targetDir.exists() ? targetDir.createRelative("./.rsa_key").getFile() : userDir.createRelative("./.rsa_key").getFile();

        if (keyFile.exists()) {
            String keyContent = Strings.trim(IO.readString(keyFile, Charsets.UTF_8));
            if (Strings.isEmpty(keyContent)) {
                keyContent = writeKeyContent(keyFile);
            }
            return keyContent;
        }
        return writeKeyContent(keyFile);
    }

    protected String writeKeyContent(File file) {
        /*
        #base64 rsa private key#

        ...

        #base64 rsa public key#

        ...

         */
        RsaKeyPair    kp      = RSA.generateKeyPair();
        StringBuilder content = new StringBuilder();
        content.append("#base64 rsa private key#\n")
                .append(kp.getBase64PrivateKey())
                .append("\n\n")
                .append("#base64 rsa public key#\n")
                .append(kp.getBase64PublicKey());

        IO.writeString(file, content.toString(), Charsets.UTF_8);
        return content.toString();
    }

}