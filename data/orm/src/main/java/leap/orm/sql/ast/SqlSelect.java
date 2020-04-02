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
package leap.orm.sql.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SqlSelect extends SqlQuery implements SqlTableSource {

	private boolean                 distinct;
	private SqlTop                  top;
	private HashMap<String, String> selectItemAliases;
	private SqlSelectList           selectList;
	private boolean                 union;

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public SqlTop getTop() {
		return top;
	}

	public void setTop(SqlTop top) {
		this.top = top;
	}
	
	public SqlSelectList getSelectList() {
		return selectList;
	}

	public void setSelectList(SqlSelectList selectList) {
		this.selectList = selectList;
	}

	public boolean isSelectItemAlias(String name) {
		if(null != selectItemAliases && selectItemAliases.containsKey(name.toLowerCase())) {
		    return true;
        }
        if(null != from && from instanceof SqlSelect) {
            return ((SqlSelect)from).isSelectItemAlias(name);
        }
        return false;
	}

	public void addSelectItemAlias(String alias) {
		if(null == selectItemAliases) {
			selectItemAliases = new HashMap<>();
		}
		if(null == alias) {
			return;
		}
		if(alias.startsWith("`") && alias.endsWith("`")) {
			alias = alias.substring(1, alias.length() - 1);
		}else if(alias.startsWith("\"") && alias.endsWith("\"")) {
			alias = alias.substring(1, alias.length() - 1);
		}else if(alias.startsWith("'") && alias.endsWith("'")) {
			alias = alias.substring(1, alias.length() - 1);
		}
		selectItemAliases.put(alias.toLowerCase(),alias);
	}

    public void addSelectItemAliases(Map<String,String> m) {
        if(null != m) {
            if(null == selectItemAliases) {
                selectItemAliases = new HashMap<>(m.size());
            }
            selectItemAliases.putAll(m);
        }
    }

	public String getSelectItemAlias(String name){
	    if(null != selectItemAliases) {
	        String alias = selectItemAliases.get(name.toLowerCase());
	        if(null != alias) {
	            return alias;
            }
        }
        if(null != from && from instanceof SqlSelect) {
            return ((SqlSelect)from).getSelectItemAlias(name);
        }
        return name;
	}

    public Map<String,String> getSelectItemAliases() {
        return selectItemAliases;
    }

	public boolean isUnion() {
		return union;
	}

	public void setUnion(boolean union) {
		this.union = union;
	}

	public List<SqlObjectName> getSqlObjectNames() {
		List<SqlObjectName> objectNames = new ArrayList<>();
		for (AstNode node : selectList.getNodes()) {
			if (node instanceof SqlObjectName) {
				objectNames.add((SqlObjectName) node);
			}
		}
		return objectNames;
	}
}