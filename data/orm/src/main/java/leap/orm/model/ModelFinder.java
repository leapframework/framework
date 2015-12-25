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
package leap.orm.model;

import java.util.List;
import java.util.Map;

import leap.lang.Strings;
import leap.lang.collection.SimpleCaseInsensitiveMap;
import leap.lang.reflect.ReflectMethod;
import leap.orm.model.ModelRegistry.ModelContext;

@SuppressWarnings("rawtypes")
public class ModelFinder {
	
	public static final String FIND_BY     = "findBy";
	public static final String FIND_ALL_BY = "findAllBy";
	
	private static final Map<String, String> comparators   = new SimpleCaseInsensitiveMap<String>();
	private static final Map<String, String> boolOperators = new SimpleCaseInsensitiveMap<String>();
	
	static {
		comparators.put("Like",              "like");
		comparators.put("In",                "in");
		comparators.put("NotEquqls",         "<>");
		comparators.put("Between",           "between");
		comparators.put("LessThan",          "<");
		comparators.put("LessThanEquals",    "<=");
		comparators.put("GreaterThan",       ">");
		comparators.put("GreaterThanEquals", ">=");
		
		boolOperators.put("And", "and");
		boolOperators.put("Or",  "or");
	}
	
	private final ModelContext context;
	private final ReflectMethod	 method;
	private final String			 where;

	public ModelFinder(ModelContext context,ReflectMethod method) {
		this.context  = context;
		this.method   = method;
		this.where    = init();
	}
	
	public Object find(Object... args){
		return context.getDao().createCriteriaQuery(context.getEntityMapping(),context.getModelClass()).where(where,args).firstOrNull();
	}
	
    public List findAll(Object... args){
    	return context.getDao().createCriteriaQuery(context.getEntityMapping(),context.getModelClass()).where(where,args).list();
	}
    
    private String init(){
    	int prefixLength = method.getName().startsWith(FIND_BY) ? FIND_BY.length() : FIND_ALL_BY.length();
    	String query = method.getName().substring(prefixLength); 
    	return buildWhere(query);
    }
    
    private String buildWhere(String str){
    	StringBuilder where = new StringBuilder();

    	//[Field][Comparator][Boolean Operator])?[Field][Comparator]
    	//Boolean Operator : And , Or
    	String str1 = Strings.lowerFirst(str);
    	
    	int mark = 0;
    	
    	String field      = null;
    	String comparator = null;
    	
    	for(int i=1;i<str.length();i++){
    		if(i == str.length() - 1 || 
    			(Character.isLowerCase(str1.charAt(i - 1)) && 
				Character.isUpperCase(str1.charAt(i)) && 
				Character.isLowerCase(str1.charAt(i + 1)))) {

				String part = str.substring(mark,i == str.length() - 1 ? str.length() : i);
				mark = i;
				
				if(null == field){
					field = part;
					where.append(Strings.lowerFirst(field));
				}else if(comparators.containsKey(part)) {
					comparator = comparators.get(part);
					where.append(" ").append(comparator).append(" ? ");
				}else if(boolOperators.containsKey(part)){
					if(null == comparator){
						comparator = "=";
						where.append(" ").append(comparator).append(" ? ");
					}
					field   = null;
					comparator = null;
					where.append(boolOperators.get(part)).append(" ");
				}else{
					field = field + part;
					where.append(part);
				}
			}
    	}

    	if(null == comparator){
			comparator = "=";
			where.append(" ").append(comparator).append(" ? ");
    	}
    	
    	return where.toString();
    }
}