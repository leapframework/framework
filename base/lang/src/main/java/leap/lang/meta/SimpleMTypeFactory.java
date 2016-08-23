/*
 * Copyright 2015 the original author or authors.
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
package leap.lang.meta;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import leap.lang.*;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.meta.annotation.ComplexType;
import leap.lang.meta.annotation.NonProperty;
import leap.lang.meta.annotation.TypeWrapper;
import leap.lang.meta.annotation.UserSortable;

public class SimpleMTypeFactory extends AbstractMTypeFactory implements MTypeFactory {
	
	protected static final Map<Class<?>, MComplexType> COMPLEX_TYPES = 
			Collections.synchronizedMap(new WeakHashMap<Class<?>, MComplexType>());
	
	protected static final MTypeFactory[] externalFactories;
	
	static {
		externalFactories = Factory.newInstances(MTypeFactory.class).toArray(new MTypeFactory[]{});
	}

    private MTypeContext context;

    public SimpleMTypeFactory() {
    }

    public SimpleMTypeFactory(MTypeContext context) {
        this.context = context;
    }

    @Override
    public MType getMType(Class<?> type) {
        return getMType(type, null, context);
    }

    @Override
    public MType getMType(Class<?> type, Type genericType) {
        return getMType(type, genericType, context);
    }

    @Override
    public MType getMType(Class<?> type, Type genericType, MTypeContext context) {
        Args.notNull(context, "context");

		return getMType(type, genericType, context, new Stack<>(), false);
    }

	protected MType getMType(Class<?> type, Type genericType, MTypeContext context, Stack<Class<?>> stack, boolean createComplexTypeRef) {
		Args.notNull(type, "type");

		for(MTypeFactory factory : externalFactories){
			MType mtype = factory.getMType(type, genericType, context);
			if(null != mtype) {
				return mtype;
			}
		}
		
		MSimpleType mtype = MSimpleTypes.tryForClass(type);
		if(null != mtype) {
			return mtype;
		}

		if(type.isArray()) {
			return new MCollectionType(getMType(type.getComponentType(), null));
		}
		
		if(Iterable.class.isAssignableFrom(type)) {
			MType elementType;

            if(null == genericType) {
                elementType = MUnresolvedType.TYPE;
            }else{
                Type typeArgument = Types.getTypeArgument(genericType);

                Class<?> elementClass = Types.getActualType(typeArgument);
                genericType = typeArgument;

                elementType = getMType(elementClass, genericType, context, stack, true);
            }

			return new MCollectionType(elementType);
		}

        boolean isComplexTypeAnnotated = type.isAnnotationPresent(ComplexType.class);
        if(!isComplexTypeAnnotated) {
            if(Map.class.isAssignableFrom(type)) {
                return getDictionaryType(context, type, genericType, stack, createComplexTypeRef);
            }

            if(Object.class.equals(type)) {
                return MObjectType.TYPE;
            }
        }

		return getComplexTypeOrRef(context, type, stack, createComplexTypeRef);
	}

    protected MType getDictionaryType(MTypeContext context, Class<?> type, Type genericType, Stack<Class<?>> stack, boolean createComplexTypeRef) {
        if(null == genericType) {
            return MDictionaryType.INSTANCE;
        }

        Type[] types = getDictionaryTypes(type, genericType);

        Type keyType = types[0];
        Type valType = types[1];

        MType keyMType = getMType(Types.getActualType(keyType), keyType, context, stack, createComplexTypeRef);
        MType valMType = getMType(Types.getActualType(valType), valType, context, stack, createComplexTypeRef);

        return new MDictionaryType(keyMType, valMType);
    }

    protected Type[] getDictionaryTypes(Class<?> type, Type genericType) {
        Type[] types = Types.getTypeArguments(genericType);
        if(types.length != 2) {
            for(Type genericInterface : type.getGenericInterfaces()) {
                Class<?> c = Types.getActualType(genericInterface);
                if(Map.class.isAssignableFrom(c)) {
                    return getDictionaryTypes(c, genericInterface);
                }
            }

            Type genericSuperClass = type.getGenericSuperclass();
            if(null != genericSuperClass) {
                Class<?> c = Types.getActualType(genericSuperClass);
                if(Map.class.isAssignableFrom(c)) {
                    return getDictionaryTypes(c, genericSuperClass);
                }
            }
        }
        return types;
    }
	
	protected MType getComplexTypeOrRef(MTypeContext context, Class<?> type, Stack<Class<?>> stack, boolean createComplexTypeRef) {
		MComplexType ct = getComplexType(context, type, stack);
		
		if(!createComplexTypeRef) {
			return ct;
		}
		
        String fqName = context.strategy().getComplexTypeFqName(ct, type);
        if(!Strings.isEmpty(fqName)) {
            return new MComplexTypeRef(ct.getName(), fqName);
        }

		return ct.createTypeRef();
	}
	
	protected MComplexType getComplexType(MTypeContext context, Class<?> type, Stack<Class<?>> stack) {
		MComplexType ct = COMPLEX_TYPES.get(type);
		if(null == ct) {
			ct = createComplexType(context, type, stack);
			COMPLEX_TYPES.put(type, ct);
            context.listener().onComplexTypeResolved(type, ct);
		}
		
		return ct;
	}
	
	protected MComplexType createComplexType(MTypeContext context, Class<?> type, Stack<Class<?>> stack) {
		if(stack.contains(type)) {
			throw new IllegalStateException("Cannot create complex type for '" + type + "', found cyclic reference");
		}

		stack.add(type);
		
		MComplexTypeBuilder ct = new MComplexTypeBuilder(type);
		
		ct.setName(type.getSimpleName());
		ct.setAbstract(Modifier.isAbstract(type.getModifiers()));

        String name = context.strategy().getComplexTypeName(type);
        if(!Strings.isEmpty(name)) {
            ct.setName(name);
        }

		BeanType bt = BeanType.of(type);
		for(BeanProperty bp : bt.getProperties()) {

            if(!bp.isField()) {
                continue;
            }

            if(bp.isAnnotationPresent(NonProperty.class)) {
                continue;
            }

			MPropertyBuilder mp = new MPropertyBuilder();

            mp.setName(bp.getName());
			mp.setType(getMType(bp.getType(), bp.getGenericType(), context, stack, true));

            configureProperty(bp, mp);

			ct.addProperty(mp.build());
		}
		
		return ct.build();
	}
}