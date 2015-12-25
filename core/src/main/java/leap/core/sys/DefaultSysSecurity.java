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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import leap.core.DefaultAppConfig;
import leap.lang.Args;

public class DefaultSysSecurity implements SysSecurity {
	
	protected Map<SysPermission,Boolean> cachedResults    = new ConcurrentHashMap<SysPermission, Boolean>();
	protected Map<Class<?>,Permissions>  typedPermissions = new ConcurrentHashMap<Class<?>,Permissions>();
	
	public DefaultSysSecurity(DefaultAppConfig config){
		this.init(config.getPermissions());
	}

	@Override
    public void checkPermission(SysPermission permission) throws SecurityException {
		checkPermission(permission.getClass(),permission);
    }

	@Override
    public void checkPermission(Class<? extends SysPermission> type, SysPermission permission) throws SecurityException {
		Args.notNull(type);
		Args.notNull(permission);
		
		Boolean result = cachedResults.get(permission);
		if(null == result){
			Permissions permissions = typedPermissions.get(type);	
			
			if(null != permissions){
				
				for(SysPermission denied : permissions.denied){
					if(denied.implies(permission)){
						result = false;
						break;
					}
				}
				
				if(null == result){
					for(SysPermission granted : permissions.granted){
						if(granted.implies(permission)){
							result = true;
						}
					}
				}
			}
		}
		
		if(null == result){
			if(permission.isDefaultGranted()){
				result = true;
			}else{
				result = false;
			}
			cachedResults.put(permission, result);
		}
		
		if(!result){
			throw new SecurityException("permission " + permission.toString() + " not granted");
		}
    }

	protected void init(List<SysPermissionDefinition> permissionDefinitions){
		for(SysPermissionDefinition pd : permissionDefinitions){
			
			Permissions permissions = typedPermissions.get(pd.getPermType());
			if(null == permissions){
				permissions = new Permissions();
				typedPermissions.put(pd.getPermType(), permissions);
			}
			
			if(pd.isGranted()){
				permissions.granted.add(pd.getPermObject());
			}else{
				permissions.denied.add(pd.getPermObject());
			}
		}
	}
	
	protected static final class Permissions {
		private List<SysPermission> granted = new CopyOnWriteArrayList<SysPermission>();
		private List<SysPermission> denied  = new CopyOnWriteArrayList<SysPermission>();
	}
}