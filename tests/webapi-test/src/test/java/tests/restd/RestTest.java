/*
 * Copyright 2017 the original author or authors.
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
 *
 */

package tests.restd;

import app.models.restd.Company;
import leap.lang.New;
import org.junit.Test;
import tests.ApiTestCase;

/**
 * Created by calvin on 2017/6/23.
 */
public class RestTest extends ApiTestCase {

    @Test
    public void testBasicCrud() {
        Company.deleteAll();

        String companyPath = "/restd/company";
        String companyName = "leap name";

        // test get all
        get(companyPath).assertSuccess().assertContentContains("[]");

        // test create
        usePost(companyPath).setJson(New.hashMap("name", companyName)).send().assertSuccess();
        Company[] companies = get(companyPath).assertSuccess().decodeJsonArray(Company.class);
        assertEquals(companies.length, 1);
        assertEquals(companies[0].getName(), companyName);

        // test get one
        String id = companies[0].getId();
        assertNotNull(id);
        Company company = get(companyPath + "/" + id).assertSuccess().decodeJson(Company.class);
        assertEquals(company.getName(), companyName);

        // test update
        String newName = "leap new name";
        usePatch(companyPath + "/" + id).setJson(New.hashMap("name", newName)).send().assertSuccess();
        company = get(companyPath + "/" + id).assertSuccess().decodeJson(Company.class);
        assertEquals(company.getName(), newName);

        // test delete
        delete(companyPath + "/" + id).assertSuccess();
        get(companyPath + "/" + id).assertNotFound();
    }
}
