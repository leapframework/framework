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
package leap.core;

import java.util.HashSet;
import java.util.Set;

import leap.core.junit.AppTestBase;
import leap.core.sys.SysContext;
import leap.core.sys.SysSecurityPermission;

import org.junit.Test;

import test.perms.TestPermission;

public class SysSecurityTest extends AppTestBase {

	@Test
	public void testSimpleAppPermissionPermission(){
		SysSecurityPermission pp = null;
		
		pp = new SysSecurityPermission("*");
		assertTrue(pp.equals(new SysSecurityPermission("*","grant,deny,disable,enable")));
		assertFalse(pp.equals(new SysSecurityPermission("test","grant,deny,disable,enable")));
		assertTrue(pp.implies(new SysSecurityPermission("test","grant")));
		assertTrue(pp.implies(new SysSecurityPermission("*","deny")));
		assertTrue(pp.implies(new SysSecurityPermission("","disable")));
		assertTrue(pp.implies(new SysSecurityPermission("test","enable")));
		
		pp = new SysSecurityPermission("test");
		assertTrue(pp.equals(new SysSecurityPermission("test","grant,deny,disable,enable")));
		assertFalse(pp.equals(new SysSecurityPermission("*","grant,deny,disable,enable")));
		assertTrue(pp.implies(new SysSecurityPermission("test","grant")));
		assertTrue(pp.implies(new SysSecurityPermission("test","deny")));
		assertTrue(pp.implies(new SysSecurityPermission("test","disable")));
		assertTrue(pp.implies(new SysSecurityPermission("test","enable")));	
		assertFalse(pp.implies(new SysSecurityPermission("*","deny")));
		assertFalse(pp.implies(new SysSecurityPermission("","disable")));		
		
		pp = new SysSecurityPermission("*","grant, disable");
		assertTrue(pp.equals(new SysSecurityPermission("*","disable,grant")));
		assertFalse(pp.equals(new SysSecurityPermission("*","grant,enable")));
		assertTrue(pp.implies(new SysSecurityPermission("test","grant")));
		assertTrue(pp.implies(new SysSecurityPermission("test","disable")));
		assertFalse(pp.implies(new SysSecurityPermission("test","enable")));
		assertFalse(pp.implies(new SysSecurityPermission("test","deny")));
		
		try {
	        pp = new SysSecurityPermission("*","xxxxx");
	        fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
		
		try {
	        pp = new SysSecurityPermission("*","*,grant");
	        fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
		
		try {
	        pp = new SysSecurityPermission("*","none,grant");
	        fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
		
		pp = new SysSecurityPermission("*","grant,enable");
		Set<SysSecurityPermission> set = new HashSet<SysSecurityPermission>();
		set.add(pp);
		assertTrue(set.contains(pp));
		assertTrue(set.contains(new SysSecurityPermission("*","enable,grant")));
	}
	
	@Test
	public void testSimpleCheckPermission(){
		try {
	        SysContext.security().checkPermission(new TestPermission("test","create"));
        } catch (SecurityException e) {
        	fail("should not throw SecurityException");
        }
		
		try {
			SysContext.security().checkPermission(new TestPermission("test","update"));
	        fail("should throw SecurityException");
        } catch (SecurityException e) {
        	
        }
	}
}