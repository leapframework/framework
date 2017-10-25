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
import app.models.BookTag;
import app.models.Tag;
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
        MApiModel     am = amd.getModel(ormModel);
        EntityMapping em = dao.getOrmContext().getMetadata().getEntityMapping(ormModel);

        ModelExecutorContext      context  = new SimpleModelExecutorContext(ac, amd, am, dao, em);
        DefaultModelQueryExecutor executor = new DefaultModelQueryExecutor(context);

        return executor;
    }

    protected void initData() {
        BookTag.deleteAll();
    	Tag.deleteAll();
        Book.deleteAll();
        Author.deleteAll();

        Tag tag1=new Tag();
        tag1.setTitle("tag1");
        tag1.create();

        Tag tag2=new Tag();
        tag2.setTitle("tag2");
        tag2.create();


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

        BookTag bt1=new BookTag();
        bt1.setBookId(book1.getId());
        bt1.setTagId(tag1.getId());
        bt1.create();

        BookTag bt2=new BookTag();
        bt2.setBookId(book1.getId());
        bt2.setTagId(tag2.getId());
        bt2.create();

        BookTag bt3=new BookTag();
        bt3.setBookId(book2.getId());
        bt3.setTagId(tag2.getId());
        bt3.create();
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

    @Test
    public void testExpand(){
    	DefaultModelQueryExecutor executor = newExecutor(Book.class);

    	// many-to-one
        QueryOptions options = new QueryOptions();
        options.setExpand("author");
        List<Record> records=executor.queryList(options).list;
        assertNotNull(records.get(0).get("author"));

        // many-to-manay
        QueryOptions options2 = new QueryOptions();
        options2.setExpand("tags");
        List<Record> records2=executor.queryList(options2).list;
        assertNotNull(records2.get(0).get("tags"));

    }

}
