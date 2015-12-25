/*
 * Copyright 2014 the original author or authors.
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
package leap.orm.linq.jaque;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import leap.lang.asm.ASM;
import leap.lang.asm.ClassReader;
import leap.lang.asm.tree.ClassNode;
import leap.lang.asm.tree.MethodNode;
import leap.lang.params.Params;
import leap.orm.linq.Condition;
import leap.orm.linq.ConditionParser;

@SuppressWarnings("rawtypes")
public class JaqueConditionParser implements ConditionParser {
	
	private final Map<Object, SqlCondition> objectCache = Collections.synchronizedMap(new WeakHashMap<Object, SqlCondition>());
    private final Map<Class,  SqlCondition> classCache  = Collections.synchronizedMap(new WeakHashMap<Class, SqlCondition>());
	
	@Override
    public String parse(Condition condition, Params params, AtomicInteger paramsCounter) {
		SerializedLambda sl = null;
		SqlCondition     sc = getSqlConditionFromCache(condition);
		
		if(null == sc) {
			sl = getSerializedLambda(condition);
			sc = parseCondition(sl, condition);
			
			if(sl.getCapturedArgCount() == 0) {
				objectCache.put(condition, sc);
			}else{
				classCache.put(condition.getClass(), sc);
			}
		}
		
		if(sc.hasCapturedParameters() && null == sl) {
			sl = getSerializedLambda(condition);
		}
		
		return sc.toSql(sl, params, paramsCounter);
    }
	
	protected SqlCondition getSqlConditionFromCache(Condition condition) {
		SqlCondition c = objectCache.get(condition);
		if(null == c){
			c = classCache.get(condition.getClass());
		}
		return c;
	}
	
	protected static String sql(Condition condition) {
		return parseCondition(getSerializedLambda(condition),condition).toSql();
	}
	
	protected static SqlCondition parseCondition(SerializedLambda sl, Condition condition) {
		Expression e = parseExpression(sl, condition);
		return new SqlExpressionVisitor(e).result();
	}
	
	protected static Expression parseExpression(SerializedLambda sl, Condition condition) {
		MethodNode mn = resolveImplMethod(sl);
		
		JaqueMethodVisitor mv = new JaqueMethodVisitor(sl, mn);
		
		mn.accept(mv);
		
		return TypeConverter.convert(mv.getResult(),Boolean.TYPE);
	}
	
	protected static MethodNode resolveImplMethod(SerializedLambda sl) {
		//Find lambda method
		ClassNode cn = null;
		MethodNode m = null;
		try {
			ClassReader cr = new ClassReader(sl.getImplClass());

			cn = new ClassNode(ASM.API);

			cr.accept(cn, 0);

			for (MethodNode mn : cn.methods) {
				if (!mn.name.equals(sl.getImplMethodName())) {
					continue;
				}

				if (!mn.desc.equals(sl.getImplMethodSignature())) {
					continue;
				}

				m = mn;
				break;
			}

		} catch (Exception e) {
			throw new IllegalStateException("Error resolving lambda byte codes, " + e.getMessage(), e);
		}

		if (null == m) {
			throw new IllegalStateException("Illegal state, cannot resolve lambda implMethod");
		}
		
		return m;
	}

	private static SerializedLambda getSerializedLambda(Object lambda) {
		try {
			//The lambda interface must implements java.io.Serializable
			Method m = lambda.getClass().getDeclaredMethod("writeReplace");
			m.setAccessible(true);

			return (SerializedLambda) m.invoke(lambda);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("The lambda class did not defines 'writeReplace' method");
		} catch (Exception e) {
			throw new IllegalStateException("Error resolve 'SerializedLambda' from the lambda oject, " + e.getMessage(), e);
		}
	}
}
