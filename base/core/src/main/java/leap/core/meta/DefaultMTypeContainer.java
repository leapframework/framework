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

package leap.core.meta;

import leap.lang.Types;
import leap.lang.meta.*;
import leap.lang.meta.annotation.TypeWrapper;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class DefaultMTypeContainer extends AbstractMTypeContainerCreator implements MTypeContainer, MTypeContext {

    protected static final Map<Class<?>, MComplexType> CACHED_COMPLEX_TYPES = new ConcurrentHashMap<>();

    private final MTypeFactory[]              externalFactories;
    private final Map<Class<?>, MComplexType> complexTypes         = new HashMap<>();
    private final Map<String, Object>         attrs                = new HashMap<>();
    private final Map<Class<?>, String>       creatingComplexTypes = new HashMap<>();

    private MTypeFactory defaultFactory;

    public DefaultMTypeContainer(MTypeFactory[] externalFactories) {
        this.externalFactories = externalFactories;
    }

    @Override
    public MTypeContainer create() {
        defaultFactory = new SimpleMTypeFactory(this);
        return this;
    }

    @Override
    public Object getAttribute(String name) {
        return attrs.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attrs.put(name, value);
    }

    @Override
    public void runInContext(Consumer<MTypeContext> func) {
        func.accept(this);
    }

    @Override
    public <T> T runInContextWithResult(Function<MTypeContext, T> func) {
        return func.apply(this);
    }

    @Override
    public MTypeFactory root() {
        return this;
    }

    @Override
    public MTypeStrategy strategy() {
        return strategy;
    }

    @Override
    public Map<Class<?>, MComplexType> getComplexTypes() {
        return complexTypes;
    }

    @Override
    public void onComplexTypeCreating(Class<?> type, String name) {
        creatingComplexTypes.put(type, name);
    }

    @Override
    public void onComplexTypeCreated(Class<?> type) {
        creatingComplexTypes.remove(type);
    }

    @Override
    public String getCreatingComplexType(Class type) {
        return creatingComplexTypes.get(type);
    }

    @Override
    public MType getMType(Class<?> type) {
        return getMType(type, null, this);
    }

    @Override
    public MType getMType(Class<?> type, Type genericType) {
        return getMType(type, genericType, this);
    }

    @Override
    public MType getMType(Class<?> declaringClass, Class<?> type, Type genericType) {
        return getMType(declaringClass, type, genericType, this);
    }

    @Override
    public MType getMType(Class<?> declaringClass, Class<?> type, Type genericType, MTypeContext context) {
        MType mtype;

        TypeWrapper tw = type.getAnnotation(TypeWrapper.class);
        if (null != tw) {
            Class<?> wrappedType = tw.value();
            if (!wrappedType.equals(Void.class)) {
                type = wrappedType;
            } else {
                if (null == genericType || genericType.equals(type)) {
                    return MVoidType.TYPE;
                } else {
                    Type typeArgument = Types.getTypeArgument(genericType);

                    type = Types.getActualType(declaringClass, typeArgument);
                    genericType = typeArgument;
                }
            }
        }

        //Resolve from cached complex types in context.
        MComplexType ct = complexTypes.get(type);
        if (null != ct) {
            return ct.createTypeRef();
        }

        //Resolve from creating complex types.
        String complexTypeName = getCreatingComplexType(type);
        if (null != complexTypeName) {
            return new MComplexTypeRef(complexTypeName);
        }

        //Resolve from global cached complex types.
        mtype = CACHED_COMPLEX_TYPES.get(type);

        if (null == mtype) {
            for (MTypeFactory f : externalFactories) {
                mtype = f.getMType(declaringClass, type, genericType, context);

                if (null != mtype) {
                    break;
                }
            }
        }

        if (null == mtype) {
            mtype = defaultFactory.getMType(declaringClass, type, genericType, context);

            //Cache the complex type from default factory in global scope.
            //Don't cache the complex type from external factories.
            if (mtype.isComplexType()) {
                CACHED_COMPLEX_TYPES.put(type, mtype.asComplexType());
            }
        }

        if (null != mtype) {
            onTypeResolved(mtype);

            if(alwaysReturnComplexTypeRef && mtype.isComplexType()) {
                return mtype.asComplexType().createTypeRef();
            }
        }

        return mtype;
    }

    private void onTypeResolved(MType mtype) {
        if (mtype.isComplexType()) {
            MComplexType ct = mtype.asComplexType();

            if (!complexTypes.containsKey(ct.getJavaType())) {
                listener.onComplexTypeResolved(ct.getJavaType(), mtype.asComplexType());
                complexTypes.put(ct.getJavaType(), ct);
            }

            for (MProperty p : ct.getProperties()) {
                onTypeResolved(p.getType());
            }
        } else if (mtype.isCollectionType()) {
            MType elementType = mtype.asCollectionType().getElementType();
            onTypeResolved(elementType);
        }
    }
}
