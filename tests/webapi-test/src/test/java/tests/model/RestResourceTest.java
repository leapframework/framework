package tests.model;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import app.models.api.Category;
import app.models.api.RestApi;
import app.models.api.RestCategory;
import app.models.api.RestModel;
import app.models.api.RestOperation;
import app.models.api.RestPath;
import leap.lang.Assert;
import leap.lang.New;
import leap.lang.http.HTTP;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.remote.RestQueryListResult;
import leap.web.api.remote.RestResource;
import leap.web.api.remote.RestResourceBuilder;
import leap.webunit.WebTestBase;

public class RestResourceTest extends WebTestBase {

	private static Category c1 = null;
	private static Category c2 = null;
	private static RestApi api1 = null;
	private static RestApi api2 = null;

	@Test
	public void testCUD() {
		RestApi apiToCreate = new RestApi();
		apiToCreate.setName("api5");
		apiToCreate.setTitle("Api1");

		String baseUrl=client().getBaseUrl()+"/api/restapi";

		RestResource resource=RestResourceBuilder.newBuilder()
				.setEndpoint(baseUrl)
				.build();

		RestApi created=resource.insert(RestApi.class, apiToCreate);
		Assert.notNull(created);

		Map<String,Object> partial=new HashMap<>();
		partial.put("title", "Api5");

		resource.update(created.id(), partial);
		RestApi updated=resource.find(RestApi.class, created.id(), null);
		Assert.notNull(updated);
		assertEquals("Api5", updated.getTitle());

		boolean deleted=resource.delete(created.id(), null);
		Assert.isTrue(deleted);
	}

	@Test
	public void testQuery() {
		String baseUrl=client().getBaseUrl()+"/api/restapi";

		RestResource resource=RestResourceBuilder.newBuilder()
				.setEndpoint(baseUrl)
				.build();

		QueryOptions queryOptions=new QueryOptions();
		queryOptions.setSelect("id,name,title");
		queryOptions.setPageSize(2);
		queryOptions.setPageIndex(1);
		queryOptions.setTotal(true);

		RestQueryListResult<RestApi> list=resource.queryList(RestApi.class, queryOptions);
		assertNotEmpty(list.getList());
	}


	@BeforeClass
	public static void initData() {
		RestCategory.deleteAll();
		RestOperation.deleteAll();
		RestPath.query().update(New.hashMap("parentId", null));
		RestPath.deleteAll();
		RestModel.deleteAll();
		RestApi.deleteAll();
		Category.deleteAll();

		c1 = new Category();
		c1.setTitle("Cate1");
		c1.create();

		c2 = new Category();
		c2.setTitle("Cate2");
		c2.create();

		api1 = new RestApi();
		api1.setName("api1");
		api1.setTitle("Api1");
		api1.create();

		RestCategory rc1 = new RestCategory();
		rc1.setApiId(api1.getId());
		rc1.setCategoryId(c1.getId());
		rc1.create();

		RestPath path = new RestPath();
		path.setFullPath("/");
		path.setApiId(api1.getId());
		path.create();

		RestPath subPath = new RestPath();
		subPath.setApiId(api1.getId());
		subPath.setParentId(path.getId());
		subPath.setFullPath("/t");
		subPath.create();

		RestOperation op = new RestOperation();
		op.setApiId(api1.getId());
		op.setPathId(path.getId());
		op.setTitle("Get");
		op.setHttpMethod(HTTP.Method.GET);
		op.create();

		api2 = new RestApi();
		api2.setName("api2");
		api2.setTitle("Api2");
		api2.create();

		RestCategory rc2 = new RestCategory();
		rc1.setApiId(api2.getId());
		rc1.setCategoryId(c2.getId());
		rc1.create();
	}
}
