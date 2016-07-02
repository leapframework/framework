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
package leap.core.sys;

import java.security.Permission;
import java.util.Arrays;

import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Strings;

public abstract class SysPermissionBase extends SysPermission {

	private static final long serialVersionUID = 1777486234774787540L;
	
	public static final String ALL  = "*";
	public static final String NONE = "none";
	
	protected final String[] actions;
	
	private String actionsString;
	
	public SysPermissionBase(String name) {
		this(name,ALL);
	}
	
	public SysPermissionBase(String name,String actions) {
		super(Strings.isEmpty(name) ? ALL : name);
		Args.notEmpty(actions);
		this.actions = parse(actions.toLowerCase());
	}
	
	@Override
    public boolean implies(Permission permission) {
		if(null == permission){
			return false;
		}
		
		if(!this.getClass().isAssignableFrom(permission.getClass())){
			return false;
		}
		
		return impliesName(permission) && impliesActions(permission);
    }
	
	protected boolean impliesName(Permission permission){
		return ALL.equals(this.getName()) || this.getName().equals(permission.getName());
	}
	
	protected boolean impliesActions(Permission permission){
		return this.getActions().indexOf(permission.getActions()) >= 0;
	}

	@Override
    public boolean equals(Object obj) {
		if(null == obj){
			return false;
		}
		
		if(!this.getClass().isAssignableFrom(obj.getClass())){
			return false;
		}
		
		SysPermissionBase that = (SysPermissionBase)obj;
	    return that.getName().equals(this.getName()) && that.getActions().equals(this.getActions());
    }

	@Override
    public int hashCode() {
	    return getName().hashCode() * 37 + getActions().hashCode();
    }
	
	@Override
    public String getActions() {
		if(null == actionsString){
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<actions.length;i++){
				if(i > 0){
					sb.append(",");
				}
				sb.append(actions[i]);
			}
			actionsString = sb.toString();
		}
	    return actionsString;
    }

	protected String[] parse(String actions) {
		if(actions.equals(ALL)){
			return getAllActionsAscendingSorted();
		}
		
		if(actions.equals(NONE)){
			return Arrays2.EMPTY_STRING_ARRAY;
		}
		
		String[] values = Strings.split(actions,",",true,true);
		
		for(int i=0;i<values.length;i++){
			if(values[i].endsWith(ALL) || values[i].equals(NONE)){
				throw new IllegalArgumentException("invalid actions '" + actions + "', '*' or 'none' can not used with other actions");
			}
			
			if(!Arrays2.contains(getAllActionsAscendingSorted(), values[i])){
				throw new IllegalArgumentException("invalid action '" + values[i] + "'");
			}
		}
		
		Arrays.sort(values);
		
		return values;
	}
	
	/**
	 * lowercase, sorted in ascending order.
	 */
	protected abstract String[] getAllActionsAscendingSorted();
}