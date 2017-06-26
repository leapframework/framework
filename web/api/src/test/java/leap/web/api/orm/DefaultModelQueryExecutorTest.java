/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.orm;

import app.models.Author;
import app.models.Book;
import leap.core.value.Record;
import leap.orm.mapping.EntityMapping;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.params.QueryOptions;
import org.junit.Test;

import java.util.List;

public class DefaultModelQueryExecutorTest extends ModelExecutorTestBase {

    /**
     * Creates the {@link DefaultModelQueryExecutor} of the orm model.
     */
    protected DefaultModelQueryExecutor newExecutor(Class<?> ormModel) {
        MApiModel     am = am(ormModel);
        EntityMapping em = dao.getOrmContext().getMetadata().getEntityMapping(ormModel);

        DefaultModelQueryExecutor executor = new DefaultModelQueryExecutor(this, am, dao, em);

        executor.setJoinModelsLookup((name) -> {
            EntityMapping joinedEm = dao.getOrmContext().getMetadata().getEntityMapping(name);
            MApiModel     joinedAm = am(joinedEm.getEntityClass());

            return new ModelAndMapping(joinedAm, joinedEm);
        });

        return executor;
    }

    protected void initData() {
        Book.deleteAll();
        Author.deleteAll();

        Author author1 = new Author();
        author1.setName("Author1");
        author1.create();

        Author author2 = new Author();
        author2.setName("Author2");
        author2.create();

        Book book1 = new Book();
        book1.setTitle("book1");
        book1.setAuthorId(author1.getId());
        book1.create();

        Book book2 = new Book();
        book2.setTitle("book2");
        book2.setAuthorId(author2.getId());
        book2.create();
    }

    @Override
    protected void setUp() throws Exception {
        initData();
    }

    @Test
    public void testManyToOneJoinAndFilter() {
        DefaultModelQueryExecutor executor = newExecutor(Book.class);

        QueryOptions options = new QueryOptions();
        assertEquals(2, executor.queryList(options).list.size());

        options.setJoins("author a");
        options.setFilters("a.name eq 'not exists'");
        assertEquals(0, executor.queryList(options).list.size());

        options.setFilters("a.name eq 'Author1'");
        List<Record> records = executor.queryList(options).list;
        assertEquals(1, records.size());
        assertEquals("book1", records.get(0).getString("title"));

        options.setFilters("a.name eq 'Author2'");
        records = executor.queryList(options).list;
        assertEquals(1, records.size());
        assertEquals("book2", records.get(0).getString("title"));

    }

}
