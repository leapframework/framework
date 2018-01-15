package leap.web.api.remote;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import leap.lang.Out;
import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.HTTP.Method;
import leap.lang.http.client.HttpRequest;
import leap.lang.json.JSON;
import leap.lang.json.JsonSettings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.api.mvc.params.CountOptions;
import leap.web.api.mvc.params.DeleteOptions;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.remote.json.TypeReference;

public class DefaultRestResource extends AbstractRestResource {
	private final Log log=LogFactory.get(DefaultRestResource.class);

	private String endpoint;

	private RestOrmContext ormContext;

	@Override
	public <T> T insert(Class<T> entityClass, Object obj) {
		String op="";
		HttpRequest request=httpClient.request(buildOperationPath(op))
				.ajax()
				.setJson(JSON.encode(obj, JsonSettings.MIN))
				.setMethod(Method.POST);
		T val= send(entityClass, request, getAccessToken());
		return val;
	}

	@Override
	public boolean delete(Object id, DeleteOptions options) {
		String op=Strings.format("/{0}", id);

		HttpRequest request=httpClient.request(buildOperationPath(op))
				.ajax()
				.setMethod(Method.DELETE);

		if(options!=null && options.isCascadeDelete()){
			request.addQueryParam("cascade_delete", "true");
		}
		send(null, request, getAccessToken());
		return true;
	}

	@Override
	public void update(Object id, Object partial) {
		String op=Strings.format("/{0}", id);

		HttpRequest request=httpClient.request(buildOperationPath(op))
				.ajax()
				.setJson(JSON.encode(partial, JsonSettings.MIN))
				.setMethod(Method.PATCH);
		send(null, request, getAccessToken());
	}

	@Override
	public <T> T find(Class<T> entityClass, Object id, QueryOptionsBase options) {
		String op=Strings.format("/{0}", id);

		HttpRequest request=httpClient.request(buildOperationPath(op))
				.ajax()
				.setMethod(Method.GET);

		buildQueryOption(request,options);

		T val= send(entityClass, request, getAccessToken());
		return val;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> RestQueryListResult<T> queryList(Class<T> entityClass, final QueryOptions options, Map<String, Object> filters) {
		String op="";

		HttpRequest request=httpClient.request(buildOperationPath(op))
				.ajax()
				.setMethod(Method.GET);

		buildQueryOption(request,options);

		final Out<RestQueryListResult<T>> out=new Out<>();
		final Type targetType=new TypeReference<List<T>>(entityClass){}.getType();

		send(request,getAccessToken(),(response)->{
			String content = response.getString();
	        log.debug("status:{},Received response : {}",response.getStatus(), content);
	        if(response.is2xx()){
	        	List<T> list=new ArrayList<>();
	        	if(Strings.isNotEmpty(content) && ContentTypes.APPLICATION_JSON_TYPE.isCompatible(response.getContentType())){
	        		list=(List<T>)decode(targetType,content);
	        	}
	        	int count=list.size();
	        	String countStr=response.getHeader("X-Total-Count");
	        	if(options.isTotal() && Strings.isNotBlank(countStr)){
	        		count=Integer.parseInt(countStr);
	        	}
	        	out.accept(new RestQueryListResult<T>(list, count));
	        	return;
	        }
	        throw new RuntimeException("REMOTE_SERVICE_INVOKE_FAILED");
		});
		if(out.isEmpty()){
			return null;
		}
		return out.get();
	}

	@Override
	public int count(CountOptions options) {
		String op="/count";

		HttpRequest request=httpClient.request(buildOperationPath(op))
				.ajax()
				.setMethod(Method.GET);

		if(options!=null && Strings.isNotEmpty(options.getFilters())){
			request.addQueryParam("filters", options.getFilters());
		}

		Integer val= send(Integer.class, request, getAccessToken());
		return val==null?0:val.intValue();
	}

	@Override
	public void setEndpoint(String endpoint) {
		this.endpoint=endpoint;
	}

	@Override
	public String getEndpoint() {
		return endpoint;
	}

	public RestOrmContext getOrmContext() {
		return ormContext;
	}

	public void setOrmContext(RestOrmContext ormContext) {
		this.ormContext = ormContext;
	}

}
