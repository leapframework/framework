/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.orm;

import leap.core.annotation.Inject;
import leap.orm.dao.Dao;
import org.junit.Test;

public class OrmContextTest extends OrmTestCase {

    @Inject("h2_alias")
    protected OrmContext h2AliasContext;

    @Inject
    protected OrmContext h2Context;

    @Test
    public void testAliasContext() {
        assertNotNull(h2AliasContext);
        assertNotNull(h2Context);
        assertSame(h2Context, h2AliasContext);

        assertSame(h2Context, Dao.get("h2_alias").getOrmContext());
    }

}
