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
package leap.orm.df;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import leap.lang.Args;

public class DomainData {
	
	private String[] cols;
	private final List<Object[]>              allRows    = new ArrayList<Object[]>();
	private final Map<Locale, List<Object[]>> localeRows = new HashMap<Locale, List<Object[]>>();
	
	public boolean isMultiColumns() {
		return null != cols && cols.length > 1;
	}
	
	public String[] cols(){
		return cols;
	}
	
	public synchronized void setCols(String[] cols) throws IllegalStateException {
		Args.notEmpty(cols,"cols");
		if(null != this.cols){
			throw new IllegalStateException("'cols' aleady exists, cannot change it");
		}
		
		this.cols = cols;
	}
	
	public synchronized void addRow(Locale locale,Object[] row){
		rows(locale).add(row);
		allRows.add(row);
	}
	
	public Set<Locale> locales() {
		return localeRows.keySet();
	}
	
	public List<Object[]> rows(Locale locale) {
		List<Object[]> rows = localeRows.get(locale);
		
		if(null == rows){
			rows = new ArrayList<Object[]>();
			localeRows.put(locale, rows);
		}
		
		return rows;
	}
	
	public List<Object[]> rows() {
		return allRows;
	}
}