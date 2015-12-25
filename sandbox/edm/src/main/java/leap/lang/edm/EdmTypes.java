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
package leap.lang.edm;

public class EdmTypes {

//	private static final Map<Class<?>,EdmType> types = Collections.synchronizedMap(new WeakHashMap<Class<?>, EdmType>());
//	
//	public static EdmType of(EdmManager manager,Class<?> javaType){
//		return of(manager,javaType,null);
//	}
	
	/*
	public static EdmType of(EdmManager manager,Class<?> javaType,Type genericType){
		EdmType edmType = types.get(javaType);
		
		if(null != edmType){
			return edmType;
		}
		
		//simple type
		EdmSimpleType simpleType = EdmSimpleType.of(javaType);
		if(null != simpleType){
			return simpleType;
		}
		
		//entity type
		EdmEntityTypeRef entityTypeRef = manager.entityTypeRef(javaType);
		if(null != entityTypeRef){
			return entityTypeRef;
		}
		
		//collection type
		if(Iterable.class.isAssignableFrom(javaType)){
			if(null == genericType){
				throw new IllegalArgumentException("generic type must not be null of collection type '" + javaType.getClass().getName() + "'");
			}
			Class<?> elementJavaType = Types.getActualTypeArgument(genericType);
			EdmType  elementEdmType  = of(manager,elementJavaType);
			
			return new EdmCollectionType(elementEdmType);
		}
		
		//collection type
		if(javaType.isArray()){
			return new EdmCollectionType(of(manager,javaType.getComponentType()));
		}
		
		//enum type
		if(javaType.isEnum()){
			EdmEnumType enumType = manager.enumType(javaType);
			if(null != enumType){
				return enumType;
			}
			
			edmType = createEnumType(manager,javaType);
			types.put(javaType, edmType);
			return edmType;
		}
		
		//complex type
		EdmComplexTypeRef complexTypeRef = manager.complexTypeRef(javaType);
		if(null != complexTypeRef){
			return complexTypeRef;
		}
		
		if(javaType.isAnnotationPresent(ComplexType.class)){
			edmType = createComplexType(manager,javaType);
			types.put(javaType, edmType);
			return edmType;
		}
		
		throw new UnsupportedOperationException("cannot convert to edm type for java type '" + javaType.getName() + "'");
	}
	*/
	
	/*
	public static EdmEnumType createEnumType(EdmManager manager,Class<?> enumClass){
		Args.checkIsTrue(enumClass.isEnum(),"must be enum class");
		
		EdmEnumTypeBuilder type = new EdmEnumTypeBuilder(enumClass.getSimpleName());
		
		ReflectEnum reflect = ReflectEnum.get(enumClass);
		
		EdmSimpleType underlyingType = reflect.isValued() ? EdmSimpleType.of(reflect.getValueType()) : EdmSimpleType.STRING;
		if(null == underlyingType){
			throw new UnsupportedOperationException("enum's underlying type must be simple type,'" + reflect.getValueType().getName() + "' not supported");
		}
		
		type.setUnderlyingType(underlyingType);
		
		for(Enum<?> enumObject : reflect.getEnumConstants()){
			if(reflect.isValued()){
				type.addMember(enumObject.name(),reflect.getValue(enumObject));
			}else{
				type.addMember(enumObject.name());
			}
		}
		
		return type.build();
	}
	
	public static EdmComplexType createComplexType(EdmManager manager,Class<?> javaType){
		EdmComplexTypeBuilder type = new EdmComplexTypeBuilder(javaType.getSimpleName());

		BeanModel<?> model = BeanModel.get(javaType);
		
		for(BeanProperty bp : model.getProperties()){
			EdmType propertyType = of(manager,bp.getType(),bp.getGenericType());
			type.addProperty(bp.getName(), propertyType, bp.getType().isPrimitive() ? false : true);
		}
		
		return type.build();
	}
	*/
	
	protected EdmTypes(){
		
	}	
}
