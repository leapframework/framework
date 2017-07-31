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
import java.util.Map;

import leap.lang.*;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.meta.annotation.ComplexType;
import leap.lang.meta.annotation.NonProperty;

public class SimpleMTypeFactory extends AbstractMTypeFactory implements MTypeFactory {

	protected static final MTypeFactory[] externalFactories;
	
	static {
		externalFactories = Factory.newInstances(MTypeFactory.class).toArray(new MTypeFactory[]{});
	}

    private MTypeContext context;

    public SimpleMTypeFactory() {
        this(MTypeContext.DEFAULT);
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
    public MType getMType(Class<?> declaringClass, Class<?> type, Type genericType, MTypeContext context) {
        Args.notNull(context, "context");

        MTypeFactory root = context.root();
        if(null == root) {
            root = this;
        }

		return getMType(context, declaringClass, type, genericType, root);
    }

	protected MType getMType(MTypeContext context, Class<?> declaringType, Class<?> type, Type genericType, MTypeFactory root) {
		Args.notNull(type, "type");

		for(MTypeFactory factory : externalFactories){
			MType mtype = factory.getMType(declaringType, type, genericType, context);
			if(null != mtype) {
				return mtype;
			}
		}
		
		MSimpleType mtype = MSimpleTypes.tryForClass(type);
		if(null != mtype) {
			return mtype;
		}

		if(type.isArray()) {
			return new MCollectionType(root.getMType(declaringType, type.getComponentType(), null, context));
		}
		
		if(Iterable.class.isAssignableFrom(type)) {
			MType elementType;

            if(null == genericType) {
                elementType = MUnresolvedType.TYPE;
            }else{
                Type typeArgument = Types.getTypeArgument(genericType);

                Class<?> elementClass = Types.getActualType(declaringType, typeArgument);
                genericType = typeArgument;

                elementType = root.getMType(elementClass, genericType, context);
            }

			return new MCollectionType(elementType);
		}

        boolean isComplexTypeAnnotated = type.isAnnotationPresent(ComplexType.class);
        if(!isComplexTypeAnnotated) {
            if(Map.class.isAssignableFrom(type)) {
                return getDictionaryType(context, declaringType, type, genericType, root);
            }

            if(Object.class.equals(type)) {
                return MObjectType.TYPE;
            }
        }

		return createComplexType(context, type, root);
	}

    protected MType getDictionaryType(MTypeContext context, Class<?> declaringType, Class<?> type, Type genericType, MTypeFactory root) {
        if(null == genericType) {
            return MDictionaryType.INSTANCE;
        }

        Type[] types = getDictionaryTypes(type, genericType);

        Type keyType = types[0];
        Type valType = types[1];

        MType keyMType = getMType(context, declaringType, Types.getActualType(keyType), keyType, root);
        MType valMType = getMType(context, declaringType, Types.getActualType(valType), valType, root);

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
	
	protected MComplexType createComplexType(MTypeContext context, Class<?> type, MTypeFactory root) {
		MComplexTypeBuilder ct = new MComplexTypeBuilder(type);
		
		ct.setName(type.getSimpleName());
		ct.setAbstract(Modifier.isAbstract(type.getModifiers()));

        String name = "";
        ComplexType a = type.getAnnotation(ComplexType.class);
        if(null != a) {
            name = a.name();
        }

        if(Strings.isEmpty(name)) {
            name = context.strategy().getComplexTypeName(type);
        }

        if(!Strings.isEmpty(name)) {
            ct.setName(name);
        }

        context.onComplexTypeCreating(type, name);

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
			mp.setType(root.getMType(bp.getType(), bp.getGenericType(), context));
            mp.setBeanProperty(bp);

            configureProperty(bp, mp);

			ct.addProperty(mp.build());
		}

        MComplexType ret = ct.build();

        context.onComplexTypeCreated(type, ret);
		
		return ret;
	}
}