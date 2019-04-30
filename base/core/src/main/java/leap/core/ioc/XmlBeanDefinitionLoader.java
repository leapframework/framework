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
package leap.core.ioc;

import leap.core.AppClassLoader;
import leap.core.AppConfig;
import leap.core.AppResource;
import leap.core.AppResources;
import leap.core.el.EL;
import leap.lang.*;
import leap.lang.beans.BeanType;
import leap.lang.convert.Converts;
import leap.lang.el.DefaultElParseContext;
import leap.lang.el.ElClasses;
import leap.lang.el.spel.SPEL;
import leap.lang.exception.NestedClassNotFoundException;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.expression.Expression;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.logging.LogUtils;
import leap.lang.reflect.ReflectClass;
import leap.lang.reflect.Reflection;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;

import javax.xml.namespace.QName;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

class XmlBeanDefinitionLoader {

    private static final Log log = LogFactory.get(XmlBeanDefinitionLoader.class);

    private static final DefaultElParseContext parseContext = new DefaultElParseContext();

    static {
        parseContext.setFunction("classes:isPresent", ElClasses.createFunction(Classes.class, "isPresent(java.lang.String)"));
        parseContext.setFunction("strings:isEmpty", ElClasses.createFunction(Strings.class, "isEmpty(java.lang.String)"));
    }

    static final String RUNTIME_SOURCE = "runtime";

    public static final String DESCRIPTION_ELEMENT            = "description";
    public static final String ALIAS_ELEMENT                  = "alias";
    public static final String IMPORT_ELEMENT                 = "import";
    public static final String BEANS_ELEMENT                  = "beans";
    public static final String NAME_ATTRIBUTE                 = "name";
    public static final String INIT_ELEMENT                   = "init";
    public static final String BEAN_ELEMENT                   = "bean";
    public static final String INVOKE_ELEMENT                 = "invoke";
    public static final String METHOD_ARG_ELEMENT             = "method-arg";
    public static final String REF_ELEMENT                    = "ref";
    public static final String IF                             = "if";
    public static final String IF_NOT                         = "if-not";
    public static final String PROFILE                        = "profile";
    public static final String PROPERTY                       = "property";
    public static final String EXPR                           = "expr";
    public static final String CLASS_PRESENT                  = "class-present";
    public static final String SERVLET_ENVIRONMENT            = "servlet-environment";
    public static final String IF_PROFILE                     = "if-profile";
    public static final String IF_PROPERTY                    = "if-property";
    public static final String IF_EXPR                        = "if-expr";
    public static final String IF_CLASS_PRESENT               = "if-class-present";
    public static final String IF_SERVLET_ENVIRONMENT         = "if-servlet-environment";
    public static final String NAMESPACE_ATTRIBUTE            = "namespace";
    public static final String ID_ATTRIBUTE                   = "id";
    public static final String CLASS_ATTRIBUTE                = "class";
    public static final String ALIAS_ATTRIBUTE                = "alias";
    public static final String METHOD_ATTRIBUTE               = "method";
    public static final String SINGLETON_ATTRIBUTE            = "singleton";
    public static final String LAZY_INIT_ATTRIBUTE            = "lazy-init";
    public static final String PRIMARY_ATTRIBUTE              = "primary";
    public static final String INIT_METHOD_ATTRIBUTE          = "init-method";
    public static final String DESTROY_METHOD_ATTRIBUTE       = "destroy-method";
    public static final String CONSTRUCTOR_ARG_ELEMENT        = "constructor-arg";
    public static final String TYPE_ATTRIBUTE                 = "type";
    public static final String VALUE_TYPE_ATTRIBUTE           = "value-type";
    public static final String PROXY_CLASS_ATTRIBUTE          = "proxy-class";
    public static final String TARGET_TYPE_ATTRIBUTE          = "target-type";
    public static final String TARGET_NAMESPACE_ATTRIBUTE     = "target-namespace";
    public static final String TARGET_NAME_ATTRIBUTE          = "target-name";
    public static final String TARGET_ID_ATTRIBUTE            = "target-id";
    public static final String TARGET_PRIMARY_ATTRIBUTE       = "target-primary";
    public static final String KEY_TYPE_ATTRIBUTE             = "key-type";
    public static final String PROPERTY_ELEMENT               = "property";
    public static final String REF_ID_ATTRIBUTE               = "ref-id";
    public static final String REF_TYPE_ATTRIBUTE             = "ref-type";
    public static final String REF_NAME_ATTRIBUTE             = "ref-name";
    public static final String VALUE_ATTRIBUTE                = "value";
    public static final String DEFAULT_VALUE_ATTRIBUTE        = "default-value";
    public static final String VALUE_ELEMENT                  = "value";
    public static final String CLASS_ELEMENT                  = "value";
    public static final String NULL_ELEMENT                   = "null";
    public static final String ARRAY_ELEMENT                  = "array";
    public static final String LIST_ELEMENT                   = "list";
    public static final String SET_ELEMENT                    = "set";
    public static final String MAP_ELEMENT                    = "map";
    public static final String BEAN_PROXY_ELEMENT             = "bean-proxy";
    public static final String BEAN_LIST_ELEMENT              = "bean-list";
    public static final String UTIL_LIST_ELEMENT              = "util-list";
    public static final String UTIL_SET_ELEMENT               = "util-set";
    public static final String UTIL_MAP_ELEMENT               = "util-map";
    public static final String ENTRY_ELEMENT                  = "entry";
    public static final String KEY_ATTRIBUTE                  = "key";
    public static final String KEY_REF_ATTRIBUTE              = "key-ref";
    public static final String VALUE_REF_ATTRIBUTE            = "value-ref";
    public static final String PROPS_ELEMENT                  = "props";
    public static final String PROP_ELEMENT                   = "prop";
    public static final String MERGE_ATTRIBUTE                = "merge";
    public static final String RESOURCE_ATTRIBUTE             = "resource";
    public static final String QUALIFIER_ELEMENT              = "qualifier";
    public static final String QUALIFIER_ATTRIBUTE            = "qualifier";
    public static final String SORT_ORDER_ATTRIBUTE           = "sort-order";
    public static final String OVERRIDE_ATTRIBUTE             = "override";
    public static final String OVERRIDE_ANNOTATION_ATTRIBUTE  = "override-annotation";
    public static final String DEFAULT_OVERRIDE_ATTRIBUTE     = "default-override";
    public static final String DEFAULT_LAZY_INIT_ATTRIBUTE    = "default-lazy-init";
    public static final String LIST_CLASS_ATTRIBUTE           = "list-class";
    public static final String SET_CLASS_ATTRIBUTE            = "set-class";
    public static final String MAP_CLASS_ATTRIBUTE            = "map-class";
    public static final String CHECK_EXISTENCE_ATTRIBUTE      = "check-existence";
    public static final String ADDITIONAL_TYPE_DEF_ELEMENT    = "additional-type-def";
    public static final String REGISTER_BEAN_FACTORY_ELEMENT  = "register-bean-factory";
    public static final String EXPORT_MBEAN                   = "export-mbean";
    public static final String MBEAN_NAME                     = "mbean-name";
    public static final String FACTORY                        = "factory";
    public static final String REMOVE_BEAN                    = "remove-bean";


    protected boolean defaultAutoInject = true;

    private final BeanContainer container;

    public XmlBeanDefinitionLoader(BeanContainer container) {
        this.container = container;
    }

    public boolean isDefaultAutoInject() {
        return defaultAutoInject;
    }

    public void setDefaultAutoInject(boolean defaultAutoInject) {
        this.defaultAutoInject = defaultAutoInject;
    }

    public void load(AppResource[] resources) {
        Args.notNull(resources, "resources");

        for (AppResource ar : resources) {
            Resource resource = ar.getResource();
            if (log.isDebugEnabled()) {
                if (AppResources.isFrameworkAndCoreResource(resource.getURLString())) {
                    log.trace("Load beans : {}", LogUtils.getUrl(resource));
                } else {
                    log.debug("Load beans : {}", LogUtils.getUrl(resource));
                }
            }

            //todo : use AppResource's default override?
            readDefinitions(container, resource, ar.isDefaultOverride());
        }
    }

    protected Class<?> forName(String className) {
        return Classes.forName(className);
    }

    protected Class<?> tryForName(String className) {
        return Classes.tryForName(className);
    }

    public BeanDefinitionBase create(String id, boolean lazyInit, Class<?> beanClass, Object... constructorArguments) {
        return create(id, null, false, null, lazyInit, beanClass, constructorArguments);
    }

    public <T> BeanDefinitionBase create(Class<? super T> typeClass, boolean primary, boolean lazyInit, Class<?> beanClass, Object... constructorArguments) {
        return create(typeClass, primary, null, lazyInit, beanClass, constructorArguments);
    }

    public <T> BeanDefinitionBase create(Class<? super T> typeClass, boolean primary, String name, boolean lazyInit, Class<?> beanClass, Object... constructorArguments) {
        return create(null, typeClass, primary, name, lazyInit, beanClass, constructorArguments);
    }

    public <T> BeanDefinitionBase create(String id, Class<? super T> typeClass, boolean primary, String name, boolean lazyInit, Class<?> beanClass, Object... constructorArguments) {
        BeanDefinitionBase bd = new BeanDefinitionBase(RUNTIME_SOURCE);

        bd.setId(id);
        bd.setName(name);
        bd.setType(typeClass);
        bd.setBeanClass(beanClass);
        bd.setBeanClassType(BeanType.of(beanClass));
        bd.setPrimary(primary);
        bd.setSingleton(true);
        bd.setLazyInit(lazyInit);

        for (Object arg : constructorArguments) {
            bd.addConstructorArgument(new ArgumentDefinition(new ValueDefinition(arg)));
        }

        resolveBeanConstructor(bd);

        return bd;
    }

    protected void readDefinitions(BeanContainer container, Resource resource, boolean defaultOverride) {
        if (!Strings.endsWithIgnoreCase(resource.getFilename(), ".xml")) {
            return;
        }

        XmlReader reader = null;
        try {
            reader = XML.createReader(resource);
            if (reader.nextToStartElement(BEANS_ELEMENT)) {
                readBeans(container, resource, reader, new LoaderContext(defaultOverride));
            }
        } finally {
            IO.close(reader);
        }
    }

    protected void readBeans(BeanContainer container, Resource resource, XmlReader reader, LoaderContext context) {
        try {
            if (!testIfAttributes(container, reader)) {
                reader.nextToEndElement();
                return;
            }

            context.defaultLazyInit = reader.getBooleanAttribute(DEFAULT_LAZY_INIT_ATTRIBUTE, true);

            reader.loopInsideElement(() -> {
                if (reader.isStartElement()) {
                    if (reader.isStartElement(DESCRIPTION_ELEMENT)) {
                        return;
                    }

                    if (reader.isStartElement(IMPORT_ELEMENT)) {
                        boolean checkExistence = reader.getBooleanAttribute(CHECK_EXISTENCE_ATTRIBUTE, true);
                        Boolean override = reader.getBooleanAttribute(OVERRIDE_ATTRIBUTE);
                        String importResourceName = reader.getRequiredAttribute(RESOURCE_ATTRIBUTE);

                        Resource importResource = Resources.getResource(resource, importResourceName);

                        if (null == importResource || !importResource.exists()) {
                            if (checkExistence) {
                                throw new BeanDefinitionException("the import resource '" + importResourceName + "' not exists");
                            }
                        } else {
                            if (null != override) {
                                readDefinitions(container, importResource, override);
                            } else {
                                readDefinitions(container, importResource, context.defaultOverride);
                            }

                        }
                        return;
                    }

                    if (reader.isStartElement(INIT_ELEMENT)) {
                        readInit(container, reader, context);
                        return;
                    }

                    if (reader.isStartElement(ALIAS_ELEMENT)) {
                        container.addAliasDefinition(readAlias(container, reader, context));
                        return;
                    }

                    if (reader.isStartElement(REMOVE_BEAN)) {
                        removeBean(container, reader, context);
                        return;
                    }

                    if (reader.isStartElement(BEAN_ELEMENT)) {
                        BeanDefinitionBase bd = readBean(container, reader, context);
                        if (null != bd) {
                            container.addBeanDefinition(bd);
                        }
                        return;
                    }

                    if (reader.isStartElement(BEAN_PROXY_ELEMENT)) {
                        BeanDefinitionBase bd = readBean(container, reader, context, true);
                        if (null != bd) {
                            container.addBeanDefinition(bd, true);
                        }
                        return;
                    }

                    if (reader.isStartElement(BEAN_LIST_ELEMENT)) {
                        processBeanList(container, reader, context);
                        return;
                    }

                    if (reader.isStartElement(UTIL_LIST_ELEMENT)) {
                        container.addBeanDefinition(readListBean(container, reader, context));
                        return;
                    }

                    if (reader.isStartElement(UTIL_MAP_ELEMENT)) {
                        container.addBeanDefinition(readMapBean(container, reader, context));
                        return;
                    }

                    if (reader.isStartElement(UTIL_SET_ELEMENT)) {
                        container.addBeanDefinition(readSetBean(container, reader, context));
                        return;
                    }

                    if(reader.isStartElement(IF)) {
                        if(testIfElement(container, reader, false)) {
                            readBeans(container, resource, reader, context);
                        }else{
                            reader.nextToEndElement();
                        }
                        return;
                    }

                    if(reader.isStartElement(IF_NOT)) {
                        if(!testIfElement(container, reader, true)) {
                            readBeans(container, resource, reader, context);
                        }else{
                            reader.nextToEndElement();
                        }
                        return;
                    }

                    if (reader.isStartElement(BEANS_ELEMENT)) {
                        readBeans(container, resource, reader, context);
                        return;
                    }

                    throw new BeanDefinitionException("unsupported element '" + reader.getElementLocalName() + "', source : " + reader.getSource());
                }
            });
        } catch (BeanDefinitionException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanDefinitionException("Error reading bean definition : " + e.getMessage() + ", source : " + reader.getSource(), e);
        }
    }

    protected void readInit(BeanContainer container, XmlReader reader, LoaderContext context) {
        if(!testIfAttributes(container, reader)) {
            reader.nextToEndElement();
            return;
        }

        container.addInitDefinition(new InitDefinition(reader.getSource(),
                reader.getRequiredAttribute(CLASS_ATTRIBUTE),
                reader.getAttribute(INIT_METHOD_ATTRIBUTE)));
    }

    protected AliasDefinition readAlias(BeanContainer container, XmlReader reader, LoaderContext context) {
        String alias = reader.getRequiredAttribute(ALIAS_ATTRIBUTE);
        String id = reader.getAttribute(ID_ATTRIBUTE);

        if (!Strings.isEmpty(id)) {
            return new AliasDefinition(reader.getSource(), alias, id);
        }

        return new AliasDefinition(reader.getSource(),
                alias,
                forName(reader.getRequiredAttribute(TYPE_ATTRIBUTE)),
                reader.getRequiredAttribute(NAME_ATTRIBUTE)
        );
    }

    protected void removeBean(BeanContainer container, XmlReader reader, LoaderContext context) {
        if(!testIfAttributes(container, reader)) {
            reader.nextToEndElement();
            return;
        }

        BeanContainer.BeanDefinitionsImpl bds = container.bds;

        String id = reader.getAttribute(ID_ATTRIBUTE);
        if(!Strings.isEmpty(id)) {
            bds.remove(id);
            return;
        }

        String typeName  = reader.getAttribute(TYPE_ATTRIBUTE);
        String beanName  = reader.getAttribute(NAME_ATTRIBUTE);
        String className = reader.getAttribute(CLASS_ATTRIBUTE);
        Boolean primary  = reader.getBooleanAttribute(PRIMARY_ATTRIBUTE);

        if(!Strings.isEmpty(typeName)) {

            if(!Strings.isEmpty(beanName)) {
                //type + name
                bds.remove(forName(typeName), beanName);
                return;
            }else if(!Strings.isEmpty(className)) {
                //type + class
                bds.remove(forName(typeName), forName(className));
                return;
            }else if(null != primary && primary) {
                //type + primary
                //bds.removePrimary(forName(typeName));
                //todo:
                return;
            }

        }else if(!Strings.isEmpty(className)){
            //todo : removes class only.
        }

        //todo : throw exception.
        reader.nextToEndElement();
    }

    protected BeanDefinitionBase readBean(BeanContainer container, XmlReader reader, LoaderContext context) {
        return readBean(container, reader, context, false);
    }

    protected BeanDefinitionBase readBean(BeanContainer container, XmlReader reader, LoaderContext context, boolean proxy) {
        if(!testIfAttributes(container, reader)) {
            reader.nextToEndElement();
            return null;
        }

        BeanDefinitionBase bean = new BeanDefinitionBase(reader.getSource());

        bean.setNamespace(proxy ? reader.getAttribute(TARGET_NAMESPACE_ATTRIBUTE) : reader.getAttribute(NAMESPACE_ATTRIBUTE));
        bean.setId(proxy ? reader.getAttribute(TARGET_ID_ATTRIBUTE) : reader.getAttribute(ID_ATTRIBUTE));
        bean.setName(proxy ? reader.getAttribute(TARGET_NAME_ATTRIBUTE) : reader.getAttribute(NAME_ATTRIBUTE));

        String beanClassName = proxy ? reader.getRequiredAttribute(PROXY_CLASS_ATTRIBUTE) : reader.getRequiredAttribute(CLASS_ATTRIBUTE);
        String initMethodName = reader.getAttribute(INIT_METHOD_ATTRIBUTE);
        String destroyMethodName = reader.getAttribute(DESTROY_METHOD_ATTRIBUTE);
        String typeClassName = proxy ? reader.getRequiredAttribute(TARGET_TYPE_ATTRIBUTE) : reader.getAttribute(TYPE_ATTRIBUTE);
        String qualifierName = reader.getAttribute(QUALIFIER_ATTRIBUTE);
        Float sortOrder = reader.getFloatAttribute(SORT_ORDER_ATTRIBUTE);
        boolean override = reader.getBooleanAttribute(OVERRIDE_ATTRIBUTE, context.defaultOverride);
        boolean overrideAnnotation = reader.getBooleanAttribute(OVERRIDE_ANNOTATION_ATTRIBUTE,false);
        boolean defaultOverride = reader.getBooleanAttribute(DEFAULT_OVERRIDE_ATTRIBUTE, false);

        if (!Strings.isEmpty(beanClassName)) {
            try {
                AppClassLoader.addInstrumentClass(beanClassName);
                log.trace("Resolving bean class '{}'...", beanClassName);
                bean.setBeanClass(forName(beanClassName));
            } catch (NestedClassNotFoundException e) {
                throw new BeanDefinitionException("Error resolving bean class '" + beanClassName + "' , source : " + reader.getSource(), e);
            }

            if (!Strings.isEmpty(initMethodName)) {
                bean.setInitMethod(Reflection.findMethod(bean.getBeanClass(), initMethodName));
                if (null == bean.getInitMethod()) {
                    throw new BeanDefinitionException("init method '" + initMethodName + "' not found in class '" + beanClassName + "', source : " + reader.getSource());
                }
            }

            if (!Strings.isEmpty(destroyMethodName)) {
                bean.setDestroyMethod(Reflection.findMethod(bean.getBeanClass(), destroyMethodName));
                if (null == bean.getDestroyMethod()) {
                    throw new BeanDefinitionException("destroy method '" + initMethodName + "' not found in class '" + beanClassName + "', source : " + reader.getSource());
                }
            }

            bean.setBeanClassType(BeanType.of(bean.getBeanClass()));
        }

        if (!Strings.isEmpty(typeClassName)) {
            Class<?> type = tryForName(typeClassName);

            if (null == type) {
                throw new BeanDefinitionException("bean's type class '" + typeClassName + "' not found, source : " + reader.getSource());
            }

            if (!type.isAssignableFrom(bean.getBeanClass())) {
                throw new BeanDefinitionException("bean's type '" + typeClassName + "' must be assignable from bean's class '" + beanClassName + "', source : " + reader.getSource());
            }

            bean.setType(type);
        } else {
            bean.setType(bean.getBeanClass());
        }


        if (proxy) {
            if (!ProxyBean.class.isAssignableFrom(bean.getBeanClass())) {
                if (null == ReflectClass.of(bean.getBeanClass()).getConstructor(bean.getType())) {
                    throw new BeanDefinitionException("Bean proxy class'" + bean.getBeanClass() +
                            "' must be sub-class of '" + ProxyBean.class +
                            "' or has a constructor with the parameter type '" + bean.getType() +
                            "' , source : " + reader.getSource());
                }
            }
        }

        bean.setSingleton(reader.getBooleanAttribute(SINGLETON_ATTRIBUTE, true));
        bean.setLazyInit(boolAttribute(reader, LAZY_INIT_ATTRIBUTE, context.defaultLazyInit));
        bean.setPrimary(boolAttribute(reader, proxy ? TARGET_PRIMARY_ATTRIBUTE : PRIMARY_ATTRIBUTE, false));
        bean.setOverride(override);
        bean.setOverrideAnnotation(overrideAnnotation);
        bean.setDefaultOverride(defaultOverride);
        bean.setExportMBean(reader.getBooleanAttribute(EXPORT_MBEAN, false));
        bean.setMBeanName(reader.getAttribute(MBEAN_NAME));

        if (!Strings.isEmpty(qualifierName)) {
            bean.addQualifier(qualifierName);
        }

        if (null != sortOrder) {
            bean.setSortOrder(sortOrder);
        }

//		bean.setFactoryBeanName(reader.getAttribute(FACTORY_BEAN_ATTRIBUTE));
//		bean.setFactoryMethodName(reader.getAttribute(FACTORY_METHOD_ATTRIBUTE));
//
//		// TODO the property factoryBeanName and factoryMethodName was set but never used,why?
//		if(null == bean.getBeanClass() && Strings.isEmpty(bean.getFactoryBeanName())){
//			throw new BeanDefinitionException("bean's class or factory-bean must be specified, bean '" + bean.getIdOrName() + "' in source : " + reader.getSource());
//		}
//
//		if(Strings.isEmpty(bean.getFactoryBeanName()) && !Strings.isEmpty(bean.getFactoryMethodName())){
//			bean.setFactoryMethod(Reflection.findMethod(bean.getBeanClass(),bean.getFactoryMethodName()));
//			if(null == bean.getFactoryMethod()){
//				throw new BeanDefinitionException("The factory-method '" + bean.getFactoryMethodName() + "' not found in class '" + bean.getBeanClass().getName() + ", source : " + reader.getSource());
//			}
//			if(!Modifier.isStatic(bean.getFactoryMethod().getModifiers())){
//				throw new BeanDefinitionException("The factory-method '" + bean.getFactoryMethodName() + "' must be static, source : " + reader.getSource());
//			}
//		}

        while (reader.nextWhileNotEnd(proxy ? BEAN_PROXY_ELEMENT : BEAN_ELEMENT)) {
            if (reader.isStartElement()) {

                if (!proxy) {
                    if (reader.isStartElement(ADDITIONAL_TYPE_DEF_ELEMENT)) {
                        readBeanTypeDef(container, reader, context, bean);
                        continue;
                    }

                    if (reader.isStartElement(REGISTER_BEAN_FACTORY_ELEMENT)) {
                        readBeanFactoryDef(container, reader, context, bean);
                        continue;
                    }

                    if (reader.isStartElement(QUALIFIER_ELEMENT)) {
                        bean.addQualifier(reader.resolveRequiredAttribute(VALUE_ATTRIBUTE));
                        continue;
                    }
                }

                if(reader.isStartElement(FACTORY)) {
                    readFactoryDefinition(container, reader, context, bean);
                    continue;
                }

                if (reader.isStartElement(CONSTRUCTOR_ARG_ELEMENT)) {
                    readConstructorArgument(container, reader, context, bean);
                    continue;
                }

                if (reader.isStartElement(PROPERTY_ELEMENT)) {
                    readProperty(container, reader, context, bean);
                    continue;
                }


                if (reader.isStartElement(INVOKE_ELEMENT)) {
                    readInvoke(container, reader, context, bean);
                    continue;
                }

                if (reader.isStartElement(DESCRIPTION_ELEMENT)) {
                    reader.nextToEndElement(DESCRIPTION_ELEMENT);
                    continue;
                }

                throw new BeanDefinitionException("unsupported child element '" + reader.getElementLocalName() + "' in element 'bean', source : " + reader.getSource());
            }
        }
        reader.next();

        resolveBeanConstructor(bean);

        return bean;
    }

    protected void resolveBeanConstructor(BeanDefinitionBase bean) {
        Class<?> beanClass = bean.getBeanClass();
        if (null != beanClass && !bean.getConstructorArguments().isEmpty()) {
            Constructor<?> beanConstructor = null;
            List<Constructor<?>> matchContructors = New.arrayList();
            for (Constructor<?> constructor : beanClass.getConstructors()) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
                if (paramTypes.length == bean.getConstructorArguments().size()) {
                    boolean matched = true;
                    for (int i = 0; i < paramTypes.length; i++) {
                        Class<?> pt = paramTypes[i];
                        ArgumentDefinition ad = bean.getConstructorArguments().get(i);
                        String ptClassName = ad.getTypeClassName();
                        if (!Strings.isEmpty(ptClassName) && !(ptClassName.equals(pt.getSimpleName()) || ptClassName.equals(pt.getName()))) {
                            matched = false;
                            break;
                        } else if (Strings.isEmpty(ptClassName)) {
                            ValueDefinition vd = ad.getValueDefinition();
                            if (vd == null) {
                                continue;
                            }
                            Class<?> valType = vd.getDefinedElementType();
                            if (valType == null && vd.getDefinedValue() != null) {
                                valType = vd.getDefinedValue().getClass();
                            }
                            if (valType == null) {
                                continue;
                            }
                            if (Types.isSimpleType(pt, null)) {
                                matched = Types.isSimpleType(valType, null);
                                if (!matched) {
                                    break;
                                }
                                continue;
                            }
                            if (valType == BeanReference.class) {
                                BeanReference beanRef = (BeanReference) vd.getDefinedValue();
                                if (beanRef.getTargetBeanDefinition() != null) {
                                    Class<?> refCls = beanRef.getTargetBeanDefinition().getSource().getClass();
                                    matched = pt.isAssignableFrom(refCls);
                                    if (!matched) {
                                        break;
                                    }
                                } else {
                                    // TODO read BeanDefinition of bean
                                }
                                continue;
                            }
                            if (valType == BeanDefinitionBase.class) {
                                BeanDefinitionBase bd = (BeanDefinitionBase) vd.getDefinedValue();
                                Class<?> definedType = bd.getBeanClass();
                                if (definedType == null) {
                                    continue;
                                }
                                matched = pt.isAssignableFrom(definedType);
                                if (!matched) {
                                    break;
                                }
                                continue;
                            }
                            matched = pt.isAssignableFrom(valType);
                            if (!matched) {
                                break;
                            }
                        }
                    }
                    if (matched) {
                        matchContructors.add(constructor);
                    }
                }
            }
            if (matchContructors.size() <= 0) {
                throw new BeanDefinitionException("no match constructor for bean :" + bean);
            }
            if (matchContructors.size() >= 2) {
                throw new BeanDefinitionException("too many constructor match for bean :" + bean
                        + ".you may add the 'type' attribute in the 'constructor-arg' tag to appoint"
                        + "the constructor argument type so it can ensure use which constructor.");
            }
            beanConstructor = matchContructors.get(0);
            bean.setConstructor(beanConstructor);
            for (int i = 0; i < bean.getConstructorArguments().size(); i++) {
                ArgumentDefinition ad = bean.getConstructorArguments().get(i);
                Class<?> type = beanConstructor.getParameterTypes()[i];

                if (Strings.isEmpty(ad.getTypeClassName())) {
                    ad.setTypeClassName(type.getName());
                }

                if (null == ad.getValueDefinition().getDefinedType()) {
                    ad.getValueDefinition().setDefinedType(type);
                }
            }
        }
    }

    protected void processBeanList(BeanContainer container, XmlReader reader, LoaderContext context) {
        Object source = reader.getSource();
        Class<?> beanType = classAttribute(reader, TYPE_ATTRIBUTE, true);
        String qualifier = reader.getAttribute(QUALIFIER_ATTRIBUTE);
        boolean override = reader.getBooleanAttribute(OVERRIDE_ATTRIBUTE, context.defaultOverride);

        List<ValueDefinition> values = new ArrayList<ValueDefinition>();

        final QName elementName = reader.getElementName();

        while (reader.nextWhileNotEnd(elementName)) {
            if (reader.isStartElement()) {
                if (reader.isStartElement(DESCRIPTION_ELEMENT)) {
                    continue;
                }

                if (reader.isStartElement(REF_ELEMENT)) {
                    values.add(readRefElement(context, reader));
                    continue;
                }

                if (reader.isStartElement(BEAN_ELEMENT)) {
                    BeanDefinition bd = readBean(container, reader, context);
                    if (null != bd) {
                        values.add(new ValueDefinition(bd));
                    }
                    continue;
                }

                throw new BeanDefinitionException("The value element '" + reader.getElementLocalName() + "' not supported in 'bean-list' element, source : " + reader.getSource());
            }
        }

        container.addBeanList(new BeanListDefinition(source, beanType, qualifier, override, values));
    }

    protected BeanDefinitionBase readListBean(BeanContainer container, XmlReader reader, LoaderContext context) {
        BeanDefinitionBase bean = new BeanDefinitionBase(reader.getSource());

        readValuedBeanAttributes(container, reader, context, bean);
        bean.setBeanClass(classAttribute(reader, LIST_CLASS_ATTRIBUTE, false));
        bean.setValueDefinition(readList(container, reader, context));
        bean.getValueDefinition().setDefinedType(bean.getBeanClass());

        if (null == bean.getBeanClass()) {
            bean.setBeanClass(ArrayList.class);
        }

        return bean;
    }

    protected BeanDefinitionBase readSetBean(BeanContainer container, XmlReader reader, LoaderContext context) {
        BeanDefinitionBase bean = new BeanDefinitionBase(reader.getSource());

        readValuedBeanAttributes(container, reader, context, bean);
        bean.setBeanClass(classAttribute(reader, SET_CLASS_ATTRIBUTE, false));
        bean.setValueDefinition(readSet(container, reader, context));
        bean.getValueDefinition().setDefinedType(bean.getBeanClass());

        if (null == bean.getBeanClass()) {
            bean.setBeanClass(LinkedHashSet.class);
        }

        return bean;
    }

    protected BeanDefinitionBase readMapBean(BeanContainer container, XmlReader reader, LoaderContext context) {
        BeanDefinitionBase bean = new BeanDefinitionBase(reader.getSource());

        readValuedBeanAttributes(container, reader, context, bean);
        bean.setBeanClass(classAttribute(reader, MAP_CLASS_ATTRIBUTE, false));
        bean.setValueDefinition(readMap(container, reader, context));
        bean.getValueDefinition().setDefinedType(bean.getBeanClass());

        if (null == bean.getBeanClass()) {
            bean.setBeanClass(LinkedHashMap.class);
        }

        return bean;
    }

    protected void readValuedBeanAttributes(BeanContainer container, XmlReader reader, LoaderContext context, BeanDefinitionBase bean) {
        bean.setId(reader.getAttribute(ID_ATTRIBUTE));
        bean.setSingleton(reader.getBooleanAttribute(SINGLETON_ATTRIBUTE, true));
    }

    protected void readBeanTypeDef(BeanContainer container, XmlReader reader, LoaderContext context, BeanDefinitionBase bean) {
        TypeDefinitionBase def = new TypeDefinitionBase();

        String typeClassName = reader.getRequiredAttribute(TYPE_ATTRIBUTE);
        Class<?> typeClass = tryForName(typeClassName);

        if (null == typeClass) {
            throw new BeanDefinitionException("Class '" + typeClassName + "' not found, check source : " + bean.getSource());
        }

        if (!typeClass.isAssignableFrom(bean.getBeanClass())) {
            throw new BeanDefinitionException("bean's type '" + typeClassName + "' must be assignable from bean's class '" + bean.getBeanClass().getName() + "', source : " + reader.getSource());
        }

        def.setType(typeClass);
        def.setName(reader.getAttribute(NAME_ATTRIBUTE));
        def.setPrimary(reader.getBooleanAttribute(PRIMARY_ATTRIBUTE, false));
        def.setOverride(reader.getBooleanAttribute(OVERRIDE_ATTRIBUTE, context.defaultOverride));

        bean.addAdditionalTypeDef(def);
    }

    protected void readBeanFactoryDef(BeanContainer container, XmlReader reader, LoaderContext context, BeanDefinitionBase bean) {
        if (!(FactoryBean.class.isAssignableFrom(bean.getBeanClass()))) {
            throw new BeanDefinitionException("Bean [" + bean + "] must be implements FactoryBean");
        }

        String targetTypeName = reader.getRequiredAttribute(TARGET_TYPE_ATTRIBUTE);

        if (Strings.isEmpty(targetTypeName)) {
            throw new BeanDefinitionException("Attribute '" + TARGET_TYPE_ATTRIBUTE + "' of element '" + REGISTER_BEAN_FACTORY_ELEMENT + "' must not be empty, source : " + reader.getSource());
        }

        Class<?> targetType = tryForName(targetTypeName);
        if (null == targetType) {
            throw new BeanDefinitionException("Target type '" + targetTypeName + "' not found, source : " + reader.getSource());
        }

        bean.addFactoryBeanDef(new FactoryBeanDefinitionBase(targetType));
    }

    protected void readFactoryDefinition(BeanContainer container, XmlReader reader, LoaderContext context, BeanDefinitionBase bd) {
        FactoryDefinition fd = new FactoryDefinition();

        String className = reader.getRequiredAttribute(CLASS_ATTRIBUTE);

        if(!Strings.isEmpty(className)) {

            Class<?> cls = tryForName(className);
            if(null == cls) {
                throw new BeanDefinitionException("Invalid factory class name '" + className + "' in " + bd);
            }

            readMethodDefinition(reader, context, bd, cls, fd, false);
        }

        bd.setFactoryDefinition(fd);
    }

    protected void readConstructorArgument(BeanContainer container, XmlReader reader, LoaderContext context, BeanDefinitionBase bean) {
        ArgumentDefinition arg = new ArgumentDefinition();

        arg.setTypeClassName(reader.getAttribute(TYPE_ATTRIBUTE));
        arg.setDefaultValue(reader.getAttribute(DEFAULT_VALUE_ATTRIBUTE));
        arg.setValueDefinition(readValue(container, reader, context, CONSTRUCTOR_ARG_ELEMENT));

        bean.addConstructorArgument(arg);
    }

    protected void readProperty(BeanContainer container, XmlReader reader, LoaderContext context, BeanDefinitionBase bean) {
        if(!testIfAttributes(container, reader)) {
            reader.nextToEndElement();
            return;
        }

        PropertyDefinition prop = new PropertyDefinition();

        prop.setName(reader.getRequiredAttribute(NAME_ATTRIBUTE));
        prop.setDefaultValue(reader.getAttribute(DEFAULT_VALUE_ATTRIBUTE));
        prop.setValueDefinition(readValue(container, reader, context, PROPERTY_ELEMENT));
        prop.setProperty(bean.getBeanClassType().getProperty(prop.getName()));

        if (null == prop.getProperty()) {
            throw new BeanDefinitionException("property '" + prop.getName() + "' not found in bean class '" + bean.getBeanClass().getName() + ", source : " + reader.getSource());
        }

        if (null == prop.getValueDefinition()) {
            throw new BeanDefinitionException("property '" + prop.getName() + "' must define a value, check source : " + bean.getSource());
        }

        bean.addProperty(prop);
    }

    protected void readInvoke(BeanContainer container, XmlReader reader, LoaderContext context, BeanDefinitionBase bean) {
        InvokeDefinition invoke = new InvokeDefinition();

        readMethodDefinition(reader, context, bean, bean.getBeanClass(), invoke, true);

        bean.addInvoke(invoke);
    }

    protected void readMethodDefinition(XmlReader reader, LoaderContext context, BeanDefinition bd, Class<?> cls, MethodDefinition md, boolean instance) {
        String methodName = reader.getRequiredAttribute(METHOD_ATTRIBUTE);

        reader.loopInsideElement(() -> {
            if (reader.isStartElement(METHOD_ARG_ELEMENT)) {
                ArgumentDefinition arg = new ArgumentDefinition();
                arg.setTypeClassName(reader.getAttribute(TYPE_ATTRIBUTE));
                arg.setValueDefinition(readValue(container, reader, context, CONSTRUCTOR_ARG_ELEMENT));
                md.addArgument(arg);
            }
        });

        Method method = null;
        if (cls.getMethods().length > 0) {
            for (Method m : cls.getMethods()) {

                if(!m.getName().equals(methodName)) {
                    continue;
                }

                if(instance && Modifier.isStatic(m.getModifiers())) {
                    continue;
                }

                if(!instance && !Modifier.isStatic(m.getModifiers())) {
                    continue;
                }

                Class<?>[] paramTypes = m.getParameterTypes();
                if (paramTypes.length == md.getArguments().size()) {
                    boolean matched = true;
                    for (int i = 0; i < paramTypes.length; i++) {
                        Class<?> pt = paramTypes[i];
                        ArgumentDefinition ad = md.getArguments().get(i);

                        String ptClassName = ad.getTypeClassName();
                        if (!Strings.isEmpty(ptClassName) && !(ptClassName.equals(pt.getSimpleName()) || ptClassName.equals(pt.getName()))) {
                            matched = false;
                            break;
                        }
                    }
                    if (matched) {
                        method = m;
                        break;
                    }
                }
            }
        }

        if (null == method) {
            throw new BeanDefinitionException("No matched method '" + methodName + "' with " + md.getArguments().size() + " arguments in '" + bd + "', source : " + bd.getSource());
        }

        md.setMethod(method);
    }

    protected ValueDefinition readValue(BeanContainer container, XmlReader reader, LoaderContext context, String elementName) {
        String value = reader.getAttributeOrNull(VALUE_ATTRIBUTE);
        if (null != value) {
            return resolvedString(value);
        }


        ValueDefinition br = tryReadRefAttributes(context, reader);
        if (null != br) {
            return br;
        }

        while (reader.nextWhileNotEnd(elementName)) {
            if (reader.isStartElement()) {
                if (reader.isStartElement(DESCRIPTION_ELEMENT)) {
                    continue;
                }
                if (reader.isStartElement()) {
                    return readValueElement(container, reader, context);
                }
                throw new BeanDefinitionException("unsupported child element '" + reader.getElementLocalName() + "' in element '" + elementName + "', source : " + reader.getSource());
            }
        }

        throw new BeanDefinitionException("No value definition at line " + reader.getLineNumber() + ", element '" + elementName + "', in xml '" + reader.getSource() + "'");
    }

    protected ValueDefinition readRefElement(LoaderContext context, XmlReader reader) {
        String refId = reader.resolveAttribute(ID_ATTRIBUTE);
        String refType = reader.resolveAttribute(TYPE_ATTRIBUTE);
        String refName = reader.resolveAttribute(NAME_ATTRIBUTE);

        if (Strings.isAllEmpty(refId, refType, refName)) {
            throw new BeanDefinitionException("Invalid bean reference in '" + reader.getCurrentLocation() + "'");
        }

        if (Strings.isNotEmpty(refName) && Strings.isEmpty(refType)) {
            throw new BeanDefinitionException("The referenced type must be exists in '" + reader.getCurrentLocation() + "'");
        }

        return tryCreateBeanReference(context, reader, refId, refType, refName);
    }

    protected ValueDefinition tryReadRefAttributes(LoaderContext context, XmlReader reader) {
        String refId = reader.resolveAttribute(REF_ID_ATTRIBUTE);
        String refType = reader.resolveAttribute(REF_TYPE_ATTRIBUTE);
        String refName = reader.resolveAttribute(REF_NAME_ATTRIBUTE);

        return tryCreateBeanReference(context, reader, refId, refType, refName);
    }

    protected ValueDefinition tryCreateBeanReference(LoaderContext context, XmlReader reader, String refId, String refType, String refName) {
        if (!Strings.isEmpty(refId)) {
            return beanReference(context, refId);
        }

        if (!Strings.isAllEmpty(refType, refName)) {

            if (Strings.isEmpty(refType)) {
                throw new BeanDefinitionException("'The referenced bean type must not be empty, check :" + reader.getCurrentLocation());
            }

            try {
                Class<?> c = forName(refType);
                return beanReference(context, c, refName);
            } catch (NestedClassNotFoundException e) {
                throw new BeanDefinitionException("Invalid class name '" + refType + "', source : " + reader.getSource(), e);
            }
        }

        return null;
    }

    protected ValueDefinition readValueElement(BeanContainer container, XmlReader reader, LoaderContext context) {
        if (reader.isStartElement(VALUE_ELEMENT)) {
            return resolvedString(javaTypeAttribute(reader, TYPE_ATTRIBUTE), reader.getElementTextAndEnd());
        }

        if (reader.isStartElement(CLASS_ELEMENT)) {
            String className = reader.getElementTextAndEnd();
            if (Strings.isEmpty(className)) {
                return null;
            }
            try {
                Class<?> c = forName(className);
                return resolvedValue(c);
            } catch (ObjectNotFoundException e) {
                throw new BeanDefinitionException("Invallid class name '" + className + "', source : " + reader.getSource(), e);
            }
        }

        if (reader.isStartElement(REF_ELEMENT)) {
            return readRefElement(context, reader);
        }

        if (reader.isStartElement(NULL_ELEMENT)) {
            return resolvedValue(null);
        }

        if (reader.isStartElement(ARRAY_ELEMENT)) {
            return readArray(container, reader, context);
        }

        if (reader.isStartElement(LIST_ELEMENT)) {
            return readList(container, reader, context);
        }

        if (reader.isStartElement(SET_ELEMENT)) {
            return readSet(container, reader, context);
        }

        if (reader.isStartElement(MAP_ELEMENT)) {
            return readMap(container, reader, context);
        }

        if (reader.isStartElement(PROPS_ELEMENT)) {
            return readProps(container, reader, context);
        }

        if (reader.isStartElement(BEAN_ELEMENT)) {
            BeanDefinition bd = readBean(container, reader, context);
            if (null != bd) {
                return new ValueDefinition(bd);
            } else {
                return null;
            }
        }

        throw new BeanDefinitionException("unsupported value '" + reader.getElementLocalName() + "', source : " + reader.getSource());
    }

    protected ValueDefinition readArray(BeanContainer container, XmlReader reader, LoaderContext context) {
        boolean merge = boolAttribute(reader, MERGE_ATTRIBUTE, false);
        Class<?> valueType = javaTypeAttribute(reader, VALUE_TYPE_ATTRIBUTE);

        List<ValueDefinition> values = new ArrayList<>();

        while (reader.nextWhileNotEnd(ARRAY_ELEMENT)) {
            if (reader.isStartElement()) {
                if (reader.isStartElement(DESCRIPTION_ELEMENT)) {
                    continue;
                }
                if (reader.isStartElement()) {
                    ValueDefinition vd = readValueElement(container, reader, context);
                    if (null != vd) {
                        values.add(vd);
                    }
                    continue;
                }
                throw new BeanDefinitionException("unsupported child element '" + reader.getElementLocalName() + "' in element 'array', source : " + reader.getSource());
            }
        }

        return new ValueDefinition(values.toArray(), merge, null, null, valueType);
    }

    protected ValueDefinition readList(BeanContainer container, XmlReader reader, LoaderContext context) {
        boolean merge = boolAttribute(reader, MERGE_ATTRIBUTE, false);
        Class<?> valueType = javaTypeAttribute(reader, VALUE_TYPE_ATTRIBUTE);

        List<ValueDefinition> values = new ArrayList<>();

        final QName elementName = reader.getElementName();

        while (reader.nextWhileNotEnd(elementName)) {
            if (reader.isStartElement()) {
                if (reader.isStartElement(DESCRIPTION_ELEMENT)) {
                    continue;
                }

                ValueDefinition vd = readValueElement(container, reader, context);
                if (null != vd) {
                    values.add(vd);
                }
            }
        }

        return new ValueDefinition(values, merge, null, null, valueType);
    }

    protected ValueDefinition readSet(BeanContainer container, XmlReader reader, LoaderContext context) {
        boolean merge = boolAttribute(reader, MERGE_ATTRIBUTE, false);
        Class<?> valueType = javaTypeAttribute(reader, VALUE_TYPE_ATTRIBUTE);

        Set<ValueDefinition> values = new LinkedHashSet<>();

        final QName elementName = reader.getElementName();

        while (reader.nextWhileNotEnd(elementName)) {
            if (reader.isStartElement()) {
                if (reader.isStartElement(DESCRIPTION_ELEMENT)) {
                    continue;
                }

                ValueDefinition vd = readValueElement(container, reader, context);
                if (null != vd) {
                    values.add(vd);
                }
            }
        }

        return new ValueDefinition(values, merge, null, null, valueType);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected ValueDefinition readMap(BeanContainer container, XmlReader reader, LoaderContext context) {
        boolean merge = boolAttribute(reader, MERGE_ATTRIBUTE, false);
        Class<?> keyType = javaTypeAttribute(reader, KEY_TYPE_ATTRIBUTE);
        Class<?> valueType = javaTypeAttribute(reader, VALUE_TYPE_ATTRIBUTE);

        Map map = new LinkedHashMap();

        final QName elementName = reader.getElementName();

        while (reader.nextWhileNotEnd(elementName)) {
            if (reader.isStartElement()) {
                if (reader.isStartElement(DESCRIPTION_ELEMENT)) {
                    continue;
                }
                if (reader.isStartElement(ENTRY_ELEMENT)) {
                    ValueDefinition key = readEntryKey(container, reader, context);
                    ValueDefinition val = readEntryValue(container, reader, context);

                    map.put(key, val);

                    continue;
                }
                throw new BeanDefinitionException("unsupported child element '" + reader.getElementLocalName() + "' in element 'map', source : " + reader.getSource());
            }
        }

        //Skip END event
        reader.next();

        return new ValueDefinition(map, merge, null, keyType, valueType);
    }

    protected ValueDefinition readEntryKey(BeanContainer container, XmlReader reader, LoaderContext context) {
        String key = reader.getAttribute(KEY_ATTRIBUTE);
        String keyRef = reader.getAttribute(KEY_REF_ATTRIBUTE);

        if (Strings.isAllEmpty(key, keyRef)) {
            throw new BeanDefinitionException("'key' or 'key-ref' must be defined in element '" + reader.getElementLocalName() + "', source : " + reader.getSource());
        }

        if (!Strings.isEmpty(key)) {
            return resolvedString(key);
        } else {
            return beanReference(context, keyRef);
        }
    }

    protected ValueDefinition readEntryValue(BeanContainer container, XmlReader reader, LoaderContext context) {
        Class<?> valueType = javaTypeAttribute(reader, VALUE_TYPE_ATTRIBUTE);

        String value = reader.getAttribute(VALUE_ATTRIBUTE);
        if (!Strings.isEmpty(value)) {
            return resolvedString(valueType, value);
        }

        String valueRef = reader.getAttribute(VALUE_REF_ATTRIBUTE);
        if (!Strings.isEmpty(valueRef)) {
            return beanReference(context, valueRef);
        }

        while (reader.nextWhileNotEnd(ENTRY_ELEMENT)) {
            if (reader.isStartElement()) {
                if (reader.isStartElement(DESCRIPTION_ELEMENT)) {
                    continue;
                }

                if (reader.isStartElement()) {
                    return readValueElement(container, reader, context);
                }
                throw new BeanDefinitionException("unsupported child element '" + reader.getElementLocalName() + "' in element 'entry', source : " + reader.getSource());
            }
        }

        return null;
    }

    protected ValueDefinition readProps(BeanContainer container, XmlReader reader, LoaderContext context) {
        boolean merge = boolAttribute(reader, MERGE_ATTRIBUTE, false);
        Properties props = new Properties();

        while (reader.nextWhileNotEnd(PROPS_ELEMENT)) {

            if (reader.isStartElement()) {
                if (reader.isStartElement(PROP_ELEMENT)) {
                    props.put(reader.getRequiredAttribute(KEY_ATTRIBUTE), reader.getElementTextAndEnd());
                } else {
                    throw new BeanDefinitionException("unsupported child element '" + reader.getElementLocalName() + "' in 'props' element, source : " + reader.getSource());
                }
            }
        }

        return new ValueDefinition(props, merge);
    }

    protected ValueDefinition resolvedString(String string) {
        return new ValueDefinition(string);
    }

    protected ValueDefinition resolvedString(Class<?> type, String string) {
        return new ValueDefinition(string, false, type);
    }

    protected ValueDefinition resolvedValue(Object value) {
        return new ValueDefinition(value);
    }

    protected ValueDefinition beanReference(LoaderContext context, String ref) {
        return new ValueDefinition(new BeanReference(ref));
    }

    protected ValueDefinition beanReference(LoaderContext context, Class<?> type, String name) {
        return new ValueDefinition(new BeanReference(type, name));
    }

    protected Class<?> javaTypeAttribute(XmlReader reader, String name) {
        String className = reader.getAttribute(name);
        if (Strings.isEmpty(className)) {
            return null;
        }
        try {
            return Classes.forName(className);
        } catch (ObjectNotFoundException e) {
            throw new BeanDefinitionException("invalid java type '" + className + "', must be a fully qualified class name, source : " + reader.getSource());
        }
    }

    protected Class<?> classAttribute(XmlReader reader, String name, boolean required) {
        String value = required ? reader.getRequiredAttribute(name) : reader.getAttribute(name);

        if (Strings.isEmpty(value)) {
            return null;
        }

        Class<?> clazz = tryForName(value);
        if (null == clazz) {
            throw new BeanDefinitionException("invalid class name '" + value + "' in source : " + reader.getSource() + ", line number : " + reader.getLineNumber());
        }
        return clazz;
    }

    protected static boolean boolAttribute(XmlReader reader, String name, boolean defaultValue) {
        String value = reader.getAttribute(name);

        if (Strings.isEmpty(value) || value.equals("default")) {
            return defaultValue;
        }

        return Converts.toBoolean(value);
    }

    protected static boolean testIfElement(BeanContainer container, XmlReader e, boolean not) {
        AppConfig config = container.getAppConfig();

        /*
        <xsd:attribute name="profile"               type="xsd:string"  use="optional"/>
        <xsd:attribute name="property"              type="xsd:string"  use="optional"/>
        <xsd:attribute name="class"                 type="xsd:string"  use="optional"/>
        <xsd:attribute name="expr"                  type="xsd:string"  use="optional"/>
        <xsd:attribute name="servlet-environment"   type="xsd:boolean" use="optional"/>
         */

        String profile = e.getAttribute(PROFILE);
        if(!Strings.isEmpty(profile)) {
            if(not == Strings.equalsIgnoreCase(config.getProfile(), profile)) {
                return not;
            }
        }

        String property = e.getAttribute(PROPERTY);
        if(!Strings.isEmpty(property)) {
            if(not == Converts.toBoolean(config.getProperty(property))){
                return not;
            }
        }

        String className = e.getAttribute(CLASS_PRESENT);
        if(!Strings.isEmpty(className)) {
            if(not == Classes.isPresent(className)) {
                return not;
            }
        }

        String exprText = e.getAttribute(EXPR);
        if(!Strings.isEmpty(exprText)) {
            Expression expr = SPEL.createExpression(parseContext, exprText);

            if(not == EL.test(expr.getValue(newExprVars(config)), true)) {
                return not;
            }
        }

        Boolean isServletEnvironment = e.getBooleanAttribute(SERVLET_ENVIRONMENT);
        if(null != isServletEnvironment) {
            return not == isServletEnvironment ? not : !not;
        }

        return !not;
    }

    protected static final class IfAttr {
        protected boolean not;
        protected String  value;

        protected boolean test(boolean condition) {
            return not ? !condition : condition;
        }
    }

    protected static IfAttr readIfAttr(XmlReader e, String name) {
        String v = e.getAttribute(name);
        if(Strings.isEmpty(v)) {
            return null;
        }

        IfAttr attr = new IfAttr();
        if(v.startsWith("!")) {
            attr.not   = true;
            attr.value = v.substring(1).trim();
        }else{
            attr.not   = false;
            attr.value = v;
        }
        return attr;
    }

    protected static boolean testIfAttributes(BeanContainer container, XmlReader e) {
        AppConfig config = container.getAppConfig();

        /*
        <xsd:attribute name="if-profile"               type="xsd:string"  use="optional"/>
        <xsd:attribute name="if-property"              type="xsd:string"  use="optional"/>
        <xsd:attribute name="if-class"                 type="xsd:string"  use="optional"/>
        <xsd:attribute name="if-expr"                  type="xsd:string"  use="optional"/>
        <xsd:attribute name="if-servlet-environment"   type="xsd:boolean" use="optional"/>
         */

        IfAttr profile = readIfAttr(e, IF_PROFILE);
        if(null != profile && !profile.test(Strings.equalsIgnoreCase(config.getProfile(), profile.value))) {
            return false;
        }

        IfAttr property = readIfAttr(e, IF_PROPERTY);
        if(null != property && !property.test(Converts.toBoolean(config.getProperty(property.value)))) {
            return false;
        }

        IfAttr className = readIfAttr(e, IF_CLASS_PRESENT);
        if(null != className && !className.test(Classes.isPresent(className.value))) {
            return false;
        }

        IfAttr exprText = readIfAttr(e, IF_EXPR);
        if(null == exprText) {
            exprText = readIfAttr(e, IF);
        }
        if(null != exprText) {
            Expression expr = SPEL.createExpression(parseContext, exprText.value);

            if(!exprText.test(EL.test(expr.getValue(newExprVars(config)), true))) {
                return false;
            }
        }

        IfAttr isServletEnvironment = readIfAttr(e, IF_SERVLET_ENVIRONMENT);
        if(null != isServletEnvironment) {
            if(!isServletEnvironment.test(container.getAppContext().isServletEnvironment())) {
                return false;
            }
        }

        return true;
    }

    protected static Map<String,Object> newExprVars(AppConfig config) {
        Map<String, Object> vars = New.hashMap();
        vars.put("config", config);
        vars.put("properties",  config.getProperties());
        return vars;
    }

    protected static boolean matchProfile(BeanContainer container, XmlReader element) {
        String profile = element.getAttribute(IF_PROFILE);
        if (!Strings.isEmpty(profile)) {
            return Strings.equalsIgnoreCase(container.getAppConfig().getProfile(), profile);
        } else {
            return true;
        }
    }

    public class LoaderContext {
        public boolean defaultLazyInit = true;
        public boolean defaultOverride = false;

        public LoaderContext(boolean defaultOverride) {
            this.defaultOverride = defaultOverride;
        }
    }
}