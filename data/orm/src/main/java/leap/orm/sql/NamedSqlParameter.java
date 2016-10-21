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
package leap.orm.sql;

import leap.lang.Args;
import leap.lang.expression.Expression;
import leap.lang.params.Params;
import leap.orm.mapping.FieldMapping;
import leap.orm.serialize.FieldSerializer;

public class NamedSqlParameter implements SqlParameter {

    private final int             index;
    private final String          name;
    private final Expression      defaultValue;
    private final FieldMapping    fm;
    private final FieldSerializer serializer;

    public NamedSqlParameter(int index,String name) {
		this(index,name,null, null);
	}
	
	public NamedSqlParameter(int index, String name, Expression defaultValue, FieldMapping fm) {
		Args.notEmpty(name,"name");
		this.index 		  = index;
		this.name  		  = name;
		this.defaultValue = defaultValue;
        this.fm           = fm;
        this.serializer   = null == fm ? null : fm.getSerializer();
	}

    public String getName() {
        return name;
    }

    @Override
	public SqlValue getValue(SqlContext context, Params parameters) {
		Object v;
		if(parameters.isIndexed()){
			v = parameters.get(index);
		}else{
			v = parameters.get(name);	
		}
		
		if(null == v && null != defaultValue) {
			v = defaultValue.getValue(context, parameters.map());
            return SqlValue.generated(trySerialize(v));
		}else{
            return SqlValue.of(trySerialize(v));
        }
	}

    protected Object trySerialize(Object value) {
        if(null != serializer) {
            return serializer.trySerialize(fm, value);
        }else{
            return value;
        }
    }

	@Override
    public String toString() {
		return "param[name=" + name + ",index=" + index + "]";
    }

}