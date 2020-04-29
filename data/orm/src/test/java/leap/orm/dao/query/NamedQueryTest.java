/*
 * Copyright 2014 the original author or authors.
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
package leap.orm.dao.query;

import leap.core.annotation.Inject;
import leap.core.value.Record;
import leap.junit.contexual.Contextual;
import leap.orm.OrmTestCase;
import leap.orm.dao.DaoCommand;
import leap.orm.query.PageResult;
import leap.orm.tested.model.file.Directory;
import leap.orm.tested.model.file.File;
import leap.orm.tested.model.petclinic.Owner;
import leap.orm.tested.model.petclinic.Owner1;
import leap.orm.tested.model.product.Product;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NamedQueryTest extends OrmTestCase {

    protected @Inject DaoCommand findOwnerByLastNameWithAlias;

    @Test
    public void testFindOwnersByLastName() {
        deleteAll(Owner.class);

        assertTrue(dao.createNamedQuery(Owner.class, "findOwnerByLastName").param("lastName", "test").result().isEmpty());

        Owner older = dmo.getDataFactory().generate(Owner.class);
        older.setLastName("test");

        dao.insert(older);

        Owner newer = dao.createNamedQuery(Owner.class, "findOwnerByLastName").params(older).single();
        newer.getPets();//force to create pets set

        compareFields(older, newer.fields());
    }

    @Test
    public void testFindOwner1WithNotMatchedColumnName() {
        deleteAll(Owner1.class);

        Owner1 owner = dmo.getDataFactory().generate(Owner1.class);
        dao.insert(owner);

        Record record = dao.createNamedQuery("findOwner1ByName").param("name", owner.getName()).first();
        assertNotEmpty(record.getString("id"));
    }

    @Test
    public void testFindOwnerByLastNameWithAlias() {
        deleteAll(Owner.class);

        new Owner().setFullName("a", "b").create();

        Owner found = findOwnerByLastNameWithAlias.createQuery(Owner.class, new Object[]{"b"}).first();
        assertEquals("a", found.getFirstName());
    }

    @Test
    public void testFindOwnersByLastNameForMap() {
        deleteAll(Owner.class);

        assertTrue(dao.createNamedQuery("findOwnerByLastName").param("lastName", "test").result().isEmpty());
        assertTrue(dao.createNamedQuery("findOwnerByLastNameSimple").param("lastName", "test").result().isEmpty());

        Owner older = dmo.getDataFactory().generate(Owner.class);
        older.setLastName("test");

        dao.insert(older);

        Map<String, Object> newer = dao.createNamedQuery("findOwnerByLastName").params(older).single();
        compareFields(older, newer);

        newer = dao.createNamedQuery("findOwnerByLastNameSimple").params(older).single();
        compareFields(older, newer);
    }

    @Test
    public void testQueryWithResultClass() {
        Product.deleteAll();
        Product p = new Product();
        p.setId("id");
        p.setTypeId(1);
        p.create();
        List<Product> prdts = dao.createNamedQuery("queryProductWithResultClass", Product.class).list();
        prdts.forEach((prdt) -> {
            assertNotNull(prdt.getId());
        });
        prdts = dao.createSqlQuery(Product.class, "select * from product").list();
        prdts.forEach((prdt) -> {
            assertNotNull(prdt.getId());
        });

        List<Product> prdts1 = dao.createSqlQuery(Product.class, "select id from product").executeQuery((rs) -> {
            final List<Product> list = new ArrayList<>();
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getString(1));
                list.add(product);
            }
            return list;
        });

        assertEquals(prdts.size(), prdts1.size());
        prdts1.forEach((prdt) -> {
            assertNotNull(prdt.getId());
        });
    }

    @Test
    public void testQueryDirSqlWithResultFile() {
        dao.deleteAll(Directory.class);
        dao.deleteAll(File.class);
        Directory dir = new Directory();
        dir.setName("name");
        dir.setScopeId("scopeId");
        dao.cmdInsert(Directory.class).from(dir).execute();
        List<File> files = dao.createNamedQuery("queryDirSqlWithResultFile", File.class).list();
        assertEquals(1, files.size());
        assertEquals(dir.getScopeId(), files.get(0).getDirectoryId());
        assertEquals(dir.getName(), files.get(0).getScopeId());
        assertEquals(dir.getId(), files.get(0).getId());
    }

    @Test
    @Contextual("mysql")
    public void testBracketsExpression() {
        dao.createNamedQuery("testBracketsExpression").list();
    }

    @Test
    public void testUpperCaseSqlWithLowerCaseField() {
        Product.deleteAll();
        Product product = new Product();
        product.create();
        List<Product> products = Product.<Product>query("testUpperCaseSqlWithLowerCaseField").list();
        assertEquals(1, products.size());
        assertEquals(product.getId(), products.get(0).getId());
    }

    protected void compareFields(Owner older, Map<String, Object> newer) {
        Map<String, Object> olderFields = older.fields();
        Map<String, Object> newerFields = newer;

        if (db.isMySql() || db.isSqlServer()) {
            //TODO : the mill-seconds problem of mysql and sql server
            olderFields.remove("createdAt");
            newerFields.remove("createdAt");
            olderFields.remove("updatedAt");
            newerFields.remove("updatedAt");
        }

        assertMapEquals(olderFields, newerFields);
    }

    @Test
    public void testBug() {
        Owner.query("testBug").list();
    }

    @Test
    public void testQueryWithSlash() {
        Owner.query("testQueryWithSlash").list();
    }

    @Test
    @Contextual("mysql")
    public void testGetTotalCountWithUnion() {
        Owner.deleteAll();
        new Owner().setFullName("f1", "l1").create();
        new Owner().setFullName("f2", "l2").create();
        PageResult<Owner> pageResult = Owner.<Owner>query("testGetTotalCountWithUnion").pageResult(1, 10);
        List<Owner>       owners     = pageResult.list();
        long              count      = pageResult.getTotalCount();
        assertEquals(owners.size(), count);
    }
}
