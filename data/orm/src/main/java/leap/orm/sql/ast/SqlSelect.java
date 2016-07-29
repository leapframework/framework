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

import java.util.HashMap;


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
		return null != selectItemAliases && selectItemAliases.containsKey(name.toLowerCase());
	}

	public void addSelectItemAlias(String alias) {
		if(null == selectItemAliases) {
			selectItemAliases = new HashMap<>();
		}
		selectItemAliases.put(alias.toLowerCase(),alias);
	}

	public String getSelectItemAlias(String name){
		if(isSelectItemAlias(name)){
			return selectItemAliases.get(name.toLowerCase());
		}
		return name;
	}

	public boolean isUnion() {
		return union;
	}

	public void setUnion(boolean union) {
		this.union = union;
	}
}