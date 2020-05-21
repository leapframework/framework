package tests;

import app.models.Entity1;
import app.models.Entity2;
import leap.core.annotation.Inject;
import leap.lang.Assert;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.remote.RestQueryListResult;
import leap.web.api.remote.RestResource;
import leap.web.api.remote.RestResourceFactory;
import leap.webunit.WebTestBase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class RemoteRelationTest extends WebTestBase {

    private static Entity1 c1   = null;
    private static Entity1 c2   = null;
    private static Entity2 api1 = null;
    private static Entity2 api2 = null;

    private @Inject RestResourceFactory rsf;

    @Test
    public void testCUD() {
        Entity1 apiToCreate = new Entity1();
        apiToCreate.setName("api5");
        apiToCreate.setTitle("Api1");

        String baseUrl = client().getBaseUrl() + "/api/entity1";

        RestResource resource = rsf.createResource(Entity1.class, baseUrl);

        Entity1 created = resource.insert(Entity1.class, apiToCreate);
        Assert.notNull(created);

        Map<String, Object> partial = new HashMap<>();
        partial.put("title", "Api5");

        resource.update(created.getId(), partial);
        Entity1 updated = resource.find(Entity1.class, created.getId(), null);
        Assert.notNull(updated);
        assertEquals("Api5", updated.getTitle());

        boolean deleted = resource.delete(created.getId(), null);
        Assert.isTrue(deleted);
    }

    @Test
    public void testQuery() {
        String baseUrl = client().getBaseUrl() + "/api/entity1";

        RestResource resource = rsf.createResource(Entity1.class, baseUrl);

        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setSelect("id,name,title");
        queryOptions.setPageSize(2);
        queryOptions.setPageIndex(1);
        queryOptions.setTotal(true);

        RestQueryListResult<Entity1> list = resource.queryList(Entity1.class, queryOptions);
        assertNotEmpty(list.getList());
    }

    @BeforeClass
    public static void initData() {
        Entity1.deleteAll();
        Entity2.deleteAll();

        c1 = new Entity1();
        c1.setTitle("Cate1");
        c1.create();

        c2 = new Entity1();
        c2.setTitle("Cate2");
        c2.create();

        api1 = new Entity2();
        api1.setName("api1");
        api1.setTitle("Api1");
        api1.create();

        api2 = new Entity2();
        api2.setName("api2");
        api2.setTitle("Api2");
        api2.create();
    }
}
