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
package leap.orm;

import leap.core.AppContext;
import leap.junit.contexual.ContextualIgnore;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;

import org.junit.Test;

public class OrmTestCaseTest extends OrmTestCase {
	
	private Dao previousDao;
	private Dmo previousDmo;

	@Test
	@ContextualIgnore
	public void testOrmTestCaseNonContextual(){
		assertNotNull(dao);
		assertSame(dao, defaultDao);
		
		assertNotNull(dmo);
		assertSame(dmo, defaultDmo);
		assertSame(db, context.getDb());
	}

	@Test
	public void testOrmTestCaseContextual(){
		assertNotNull(dao);
		assertNotNull(dmo);
		assertNotSame(previousDao, dao);
		assertNotSame(previousDmo, dmo);
		previousDao = dao;
		previousDmo = dmo;
	}
	
	@Test
	public void testOrmContext(){
		assertNotNull(context);
		assertNotNull(metadata);
		assertNotNull(domains);
	}
	
	@Test
	public void testDomainConfig(){
		assertNotNull(domains.getDomain("Code"));
		assertNotNull(domains.getDomain("code"));
		assertNotNull(domains.getDomain("Name"));
		assertNotNull(domains.getDomain("test"));
		assertNotNull(domains.getDomain("string1"));
	}
	
	@Test
	public void testServletEnvironment(){
		assertFalse(AppContext.current().isServletEnvironment());
	}
}