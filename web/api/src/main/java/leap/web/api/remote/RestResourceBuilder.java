package leap.web.api.remote;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import leap.core.AppContext;
import leap.lang.Assert;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.orm.Orm;
import leap.orm.OrmContext;
import leap.orm.enums.RemoteType;
import leap.orm.mapping.EntityMapping;
import leap.web.Request;
import leap.web.api.remote.ds.RestDataSource;
import leap.web.api.remote.ds.RestDatasourceManager;

public class RestResourceBuilder {
	private static Log logger=LogFactory.get(RestResourceBuilder.class);
	private static String localIP;
	private String endpoint;
	private EntityMapping entityMapping;

	public static RestResourceBuilder newBuilder(){
		return new RestResourceBuilder();
	}

	public RestResource build(){
		DefaultRestResource res=AppContext.factory().inject(new DefaultRestResource());
		if(entityMapping!=null){
			RestDatasourceManager manager=getDataSourceManager();
			RestDataSource ds=manager.tryGetDataSource(entityMapping.getRemoteSettings().getDataSource());
			RestOrmContext context=new RestOrmContext(ds,entityMapping);
			res.setOrmContext(context);
			String basePath=entityMapping.getRemoteSettings().getEndpoint();
			if(ds!=null && Strings.isNotEmpty(ds.getEndpoint())){
				basePath=ds.getEndpoint();
			}
			if(Strings.isNotEmpty(basePath)){
				String url=Paths.suffixWithSlash(basePath)+entityMapping.getRemoteSettings().getRelativePath();
				res.setEndpoint(url);
			}
		}

		if(Strings.isNotBlank(endpoint)){
			res.setEndpoint(endpoint);
		}

		if(Strings.isEmpty(res.getEndpoint())){
			throw new RuntimeException("can't build rest resource,when endpoint or entityMapping is empty!");
		}
		res.setEndpoint(formatApiEndPoint(res.getEndpoint()));
		return res;
	}

	private String formatApiEndPoint(String apiEndPoint){
		if(apiEndPoint.contains("{context}")){
			apiEndPoint=apiEndPoint.replace("{context}", Strings.trimStart(getContextPath(),'/'));
			//apiEndPoint=apiEndPoint.replace("//", "/");
		}
		if(apiEndPoint.contains("~")){
			HttpServletRequest request=Request.tryGetCurrent().getServletRequest();
			apiEndPoint=apiEndPoint.replace("~", Strings.format("{0}://{1}:{2}", request.getScheme(),curServerLocalIp(),request.getLocalPort()));
		}
		if(apiEndPoint.startsWith("/")){
			HttpServletRequest request=Request.tryGetCurrent().getServletRequest();
			apiEndPoint=Strings.format("{0}://{1}:{2}", request.getScheme(),"127.0.0.1",request.getLocalPort())+apiEndPoint;
		}

		if(Strings.endsWith(apiEndPoint,"/")){
			apiEndPoint=Strings.trimEnd(apiEndPoint,'/');
		}
		return apiEndPoint;
	}

	private static String getContextPath() {
		return Request.tryGetCurrent().getServletRequest().getContextPath();
	}

	public static String curServerLocalIp(){
		if(!Strings.isEmpty(localIP)){
			return localIP;
		}

		String serverIP = null;
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();

                if(!ni.getInetAddresses().hasMoreElements()){
                	continue;
                }
                //ip = (InetAddress) ni.getInetAddresses().nextElement();
                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                for (InetAddress ip : Collections.list(inetAddresses)) {
                	logger.info("ip info:{},,isLoop:{},hostAddr:{},isSiteLocal:{}",ip,ip.isLoopbackAddress(),ip.getHostAddress(), ip.isSiteLocalAddress());
                	if(ip instanceof Inet6Address || ip.isLoopbackAddress()){
                		continue;
                	}
                	serverIP = ip.getHostAddress();
                	if(ip.isSiteLocalAddress()){
	                    break;
                	}
                }
                if(Strings.isNotBlank(serverIP)){
                	break;
                }
            }
        } catch (SocketException ex) {
        	logger.error(ex.getMessage(),ex);
        }

        localIP=serverIP;

        return localIP;
	}

	private RestDatasourceManager getDataSourceManager(){
		return AppContext.getBean(RestDatasourceManager.class);
	}


	public RestResourceBuilder setEndpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}


	public RestResourceBuilder setEntityMapping(EntityMapping entityMapping) {
		Assert.notNull(entityMapping, "entity mapping can't be null.");
		Assert.isTrue(entityMapping.isRemote() && RemoteType.rest.equals(entityMapping.getRemoteSettings().getRemoteType())
				, "entity must be remote rest model.");
		this.entityMapping = entityMapping;
		return this;
	}

	public RestResourceBuilder setEntityClass(Class<?> cls){
		OrmContext orm= Orm.context(cls);
		if(orm==null){
			throw new RuntimeException("can't find entity mapping from cls:"+cls.getName());
		}
		EntityMapping em =orm.getMetadata().getEntityMapping(cls);
		setEntityMapping(em);
		return this;
	}

}
