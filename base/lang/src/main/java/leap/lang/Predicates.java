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
package leap.lang;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Map.Entry;
import java.util.function.Predicate;

import leap.lang.annotation.Nullable;

//some codes copy from google guava library 15.0-SNAPSHOT,under Apache License 2.0
public class Predicates {

	/**
	 * Returns a predicate that evaluates to {@code true} if the object reference being tested is null.
	 */
	public static <T> Predicate<T> isNull() {
		return ObjectPredicate.IS_NULL.withNarrowedType();
	}
	
	public static <T extends Named> Predicate<T> nameEquals(final String name){
		return new Predicate<T>() {
			public boolean test(T object) {
				return Strings.equals(object.getName(), name);
			}
		};
	}

	public static <T extends Named> Predicate<T> nameEqualsIgnoreCase(final String name){
		return new Predicate<T>() {
			public boolean test(T object) {
				return Strings.equalsIgnoreCase(object.getName(),name);
			}
		};
	}
	
	public static <K,V> Predicate<Entry<K,V>> entryKeyEquals(final K key){
		return new Predicate<Entry<K,V>>() {
			public boolean test(Entry<K, V> entry) {
	            return entry.getKey().equals(key);
            }
		};
	}
	
	public static <V> Predicate<Entry<String,V>> entryKeyEqualsIgnoreCase(final String key){
		return new Predicate<Entry<String,V>>() {
			public boolean test(Entry<String, V> entry) {
	            return entry.getKey().equalsIgnoreCase(key);
            }
		};
	}	
	
	/**
	 * Returns a predicate that evaluates to {@code true} if the object being tested is an instance of the given class.
	 * If the object being tested is {@code null} this predicate evaluates to {@code false}.
	 * 
	 * <p>
	 * If you want to filter an {@code Iterable} to narrow its type, consider using
	 * {@link com.google.common.collect.Iterables#filter(Iterable, Class)} in preference.
	 * 
	 * <p>
	 * <b>Warning:</b> contrary to the typical assumptions about predicates (as documented at {@link Predicate#apply}),
	 * the returned predicate may not be <i>consistent with equals</i>. For example, {@code instanceOf(ArrayList.class)}
	 * will yield different results for the two equal instances {@code Lists.newArrayList(1)} and
	 * {@code Arrays.asList(1)}.
	 */
	public static Predicate<Object> instanceOf(Class<?> clazz) {
		return new InstanceOfPredicate(clazz);
	}
	
	@SuppressWarnings("unchecked")
	public static Predicate<Class<?>> annotatedWith(final Class<? extends Annotation>... annotationClasses){
		return new Predicate<Class<?>>() {
			@Override
			public boolean test(Class<?> input) {
				for(int i=0;i<annotationClasses.length;i++){
					if(input.isAnnotationPresent(annotationClasses[i])){
						return true;
					}
				}
				return false;
			}
		};
	}
	
	/**
	 * Returns a predicate that evaluates to {@code true} if the object being tested {@code equals()} the given target
	 * or both are null.
	 */
	public static <T> Predicate<T> equalTo(@Nullable T target) {
		return (target == null) ? Predicates.<T> isNull() : new IsEqualToPredicate<T>(target);
	}	
	
	enum ObjectPredicate implements Predicate<Object> {
		ALWAYS_TRUE {
			@Override
			public boolean test(@Nullable Object o) {
				return true;
			}
		},
		ALWAYS_FALSE {
			@Override
			public boolean test(@Nullable Object o) {
				return false;
			}
		},
		IS_NULL {
			@Override
			public boolean test(@Nullable Object o) {
				return o == null;
			}
		},
		NOT_NULL {
			@Override
			public boolean test(@Nullable Object o) {
				return o != null;
			}
		};

		@SuppressWarnings("unchecked")
		// these Object predicates work for any T
		<T> Predicate<T> withNarrowedType() {
			return (Predicate<T>) this;
		}
	}
	
	private static class InstanceOfPredicate implements Predicate<Object>, Serializable {
		private final Class<?>	clazz;

		private InstanceOfPredicate(Class<?> clazz) {
			this.clazz = Args.notNull(clazz);
		}

		@Override
		public boolean test(@Nullable Object o) {
			return clazz.isInstance(o);
		}

		@Override
		public int hashCode() {
			return clazz.hashCode();
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (obj instanceof InstanceOfPredicate) {
				InstanceOfPredicate that = (InstanceOfPredicate) obj;
				return clazz == that.clazz;
			}
			return false;
		}

		@Override
		public String toString() {
			return "IsInstanceOf(" + clazz.getName() + ")";
		}

		private static final long	serialVersionUID	= 0;
	}
	
	/** @see Predicates#equalTo(Object) */
	private static class IsEqualToPredicate<T> implements Predicate<T>, Serializable {
		private final T	target;

		private IsEqualToPredicate(T target) {
			this.target = target;
		}

		@Override
		public boolean test(T t) {
			return target.equals(t);
		}

		@Override
		public int hashCode() {
			return target.hashCode();
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (obj instanceof IsEqualToPredicate) {
				IsEqualToPredicate<?> that = (IsEqualToPredicate<?>) obj;
				return target.equals(that.target);
			}
			return false;
		}

		@Override
		public String toString() {
			return "IsEqualTo(" + target + ")";
		}

		private static final long	serialVersionUID	= 0;
	}
	
	protected Predicates(){
		
	}
}
