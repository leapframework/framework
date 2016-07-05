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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import leap.lang.Args;
import leap.lang.Factory;
import leap.lang.Strings;
import leap.lang.Types;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;

public class SimpleMTypeFactory implements MTypeFactory {
	
	protected static final Map<Class<?>, MComplexType> COMPLEX_TYPES = 
			Collections.synchronizedMap(new WeakHashMap<Class<?>, MComplexType>());
	
	protected static final MTypeFactory[] externalFactories;
	
	static {
		externalFactories = Factory.newInstances(MTypeFactory.class).toArray(new MTypeFactory[]{});
	}
	
	protected final BiConsumer<Class<?>, MComplexType>         complextTypeCreatedListener;
	protected final Function<Class<?>, String> 				   complextTypeLocalNamer;
	protected final BiFunction<Class<?>, MComplexType, String> complextTypeFqNamer;
	
	public SimpleMTypeFactory(BiConsumer<Class<?>, MComplexType> complextTypeCreatedListener, 
							Function<Class<?>, String> complextTypeLocalNamer,
							BiFunction<Class<?>, MComplexType, String> complextTypeFqNamer) {
	    super();
	    this.complextTypeCreatedListener = complextTypeCreatedListener;
	    this.complextTypeLocalNamer      = complextTypeLocalNamer;
	    this.complextTypeFqNamer         = complextTypeFqNamer;
    }

	@Override
    public MType getMType(Class<?> type, Type genericType, MTypeFactory root) {
		return getMType(type, genericType, root, new Stack<Class<?>>(), false);
    }

	protected MType getMType(Class<?> type, Type genericType, MTypeFactory root, Stack<Class<?>> stack, boolean createComplexTypeRef) {
		Args.notNull(type, "type");
		
		for(MTypeFactory factory : externalFactories){
			MType mtype = factory.getMType(type, genericType, root);
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
			MType elementType = null == genericType ? MUnresolvedType.TYPE : getMType(Types.getActualTypeArgument(genericType));
			
			return new MCollectionType(elementType);
		}
		
		if(Object.class.equals(type)) {
			return MObjectType.TYPE;
		}
		
		return getComplexTypeOrRef(root, type, stack, createComplexTypeRef);
	}
	
	protected MType getComplexTypeOrRef(MTypeFactory root, Class<?> type, Stack<Class<?>> stack, boolean createComplexTypeRef) {
		MComplexType ct = getComplexType(root, type, stack);
		
		if(!createComplexTypeRef) {
			return ct;
		}
		
		if(null != complextTypeFqNamer) {
			String fqName = complextTypeFqNamer.apply(type, ct);
			if(!Strings.isEmpty(fqName)) {
				return new MComplexTypeRef(ct.getName(), fqName);
			}
		}
		
		return ct.createTypeRef();
	}
	
	protected MComplexType getComplexType(MTypeFactory root, Class<?> type, Stack<Class<?>> stack) {
		MComplexType ct = COMPLEX_TYPES.get(type);
		if(null == ct) {
			ct = createComplexType(root, type, stack);
			
			if(null != complextTypeCreatedListener) {
				complextTypeCreatedListener.accept(type, ct);
			}
			
			COMPLEX_TYPES.put(type, ct);
		}
		
		return ct;
	}
	
	protected MComplexType createComplexType(MTypeFactory root, Class<?> type) {
		return createComplexType(root, type, new Stack<Class<?>>());
	}
	
	protected MComplexType createComplexType(MTypeFactory root, Class<?> type, Stack<Class<?>> stack) {
		if(stack.contains(type)) {
			throw new IllegalStateException("Cannot create complext type for '" + type + "', found cyclic reference");
		}
		
		stack.add(type);
		
		MComplexTypeBuilder ct = new MComplexTypeBuilder();
		
		ct.setName(type.getSimpleName());
		ct.setAbstract(Modifier.isAbstract(type.getModifiers()));
		
		if(null != complextTypeLocalNamer) {
			String name = complextTypeLocalNamer.apply(type);
			if(!Strings.isEmpty(name)) {
				ct.setName(name);
			}
		}
		
		BeanType bt = BeanType.of(type);
		for(BeanProperty bp : bt.getProperties()) {
			MPropertyBuilder mp = new MPropertyBuilder();
			mp.setName(bp.getName());
			mp.setType(getMType(bp.getType(), bp.getGenericType(), root, stack, true)); 
			ct.addProperty(mp.build());
		}
		
		return ct.build();
	}
}