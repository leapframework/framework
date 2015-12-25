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
package test.perms;

import leap.core.sys.SysPermissionBase;

public class TestPermission extends SysPermissionBase {
	
	private static final long serialVersionUID = 3814085061173200278L;
	
	private static final String[] ALL_ACTIONS = new String[]{"create","update"};

	public TestPermission(String name) {
		super(name);
	}

	public TestPermission(String name, String actions) {
		super(name, actions);
	}

	@Override
    protected String[] getAllActionsAscendingSorted() {
	    return ALL_ACTIONS;
    }

	@Override
    public boolean isDefaultGranted() {
	    return false;
    }
}