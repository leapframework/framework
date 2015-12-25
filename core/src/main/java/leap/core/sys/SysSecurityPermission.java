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

import java.util.Arrays;


/**
 * represents the permission used by {@link SysSecurity}.
 */
public class SysSecurityPermission extends SysPermissionBase {

	private static final long serialVersionUID = 3966303601169520037L;
	
	public static final String GRANT   = "grant";
	public static final String DENY    = "deny";
	public static final String ENABLE  = "enable";
	public static final String DISABLE = "disable";
	
	private static final String[] ALL_ACTIONS = new String[]{GRANT,DENY,ENABLE,DISABLE};
	static {
		Arrays.sort(ALL_ACTIONS);
	}
	
	public SysSecurityPermission(String name) {
	    super(name);
    }
	
	public SysSecurityPermission(String name, String actions) {
	    super(name, actions);
    }
	
	@Override
    public boolean isDefaultGranted() {
	    return false;
    }

	@Override
    protected String[] getAllActionsAscendingSorted() {
	    return ALL_ACTIONS;
    }
}