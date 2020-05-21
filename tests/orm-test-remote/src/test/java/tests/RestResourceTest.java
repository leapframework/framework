package tests;

import leap.core.annotation.Inject;
import leap.web.api.remote.RestResourceFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import app.models.Entity1;
import app.models.Entity2;
import app.models.Entity4;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.remote.RestQueryListResult;
import leap.web.api.remote.RestResource;
import leap.webunit.WebTestBase;

public class RestResourceTest extends WebTestBase {

	private static Entity1 c1 = null;
	private static Entity2 c2 = null;
	private static Entity4 c4 = null;

	private @Inject RestResourceFactory rsf;

	@Test
	public void testRemoteExpand() {
		String baseUrl=client().getBaseUrl()+"/api/entity1";

		RestResource resource = rsf.createResource(Entity1.class, baseUrl);

		QueryOptions queryOptions=new QueryOptions();
		queryOptions.setSelect("id,name,title,entity2Id,remoteEntity1");
		queryOptions.setPageSize(10);
		queryOptions.setPageIndex(1);
		queryOptions.setExpand("remoteEntity(id,title),entity2(id,title)");
		queryOptions.setTotal(false);

		RestQueryListResult<Entity1> list=resource.queryList(Entity1.class, queryOptions);
		assertNotEmpty(list.getList());

		Entity1 e1=list.getList().get(0);
		assertEquals(c1.getRemoteEntity1(), e1.getRemoteEntity().getId());
		assertEquals(c1.getEntity2Id(), e1.getEntity2().getId());
	}

	@BeforeClass
	public static void initData() {
		Entity1.deleteAll();
		Entity2.deleteAll();

		c4 = new Entity4();
		c4.setTitle("e4");
		c4.create();

		c2 = new Entity2();
		c2.setTitle("e2");
		c2.create();


		c1 = new Entity1();
		c1.setTitle("Cate1");
		c1.setRemoteEntity1(c4.getId());
		c1.setEntity2Id(c2.getId());
		c1.create();
	}
}
