/*
 * Copyright 2013 the original author or authors.
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
package leap.web;

import leap.core.AppContext;
import leap.core.AppException;
import leap.core.Session;
import leap.core.i18n.MessageSource;
import leap.core.security.Authentication;
import leap.core.security.UserPrincipal;
import leap.core.validation.Validation;
import leap.lang.Arrays2;
import leap.lang.Assert;
import leap.lang.Exceptions;
import leap.lang.Strings;
import leap.lang.exception.NestedIOException;
import leap.lang.exception.NestedServletException;
import leap.lang.http.HTTP.Method;
import leap.lang.http.Headers;
import leap.lang.http.MimeType;
import leap.lang.http.MimeTypes;
import leap.lang.http.QueryStringParser;
import leap.lang.tostring.ToStringBuilder;
import leap.web.action.ActionContext;
import leap.web.assets.AssetSource;
import leap.web.config.WebConfig;
import leap.web.format.FormatManager;
import leap.web.view.View;
import leap.web.view.ViewSource;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;

public class DefaultRequest extends Request {

    protected final App            app;
    protected final WebConfig      config;
    protected final AppHandler     handler;
    protected final Response       response;
    protected final String         overrideMethod;
    protected final String         method;
    protected final String         path;
    protected final RequestWrapper req;
    protected final Params         params = new SimpleParams();

    private String                     lowercaseRequestPath;
    private String                     serverUrl;
    private String                     reverseProxyServerUrl;
    private String                     contextUrl;
    private String                     reverseProxyContextUrl;
    private String                     servicePath;
    private String                     servicePathWithoutExtension;
    private String                     pathExtension;
    private MessageSource              messageSource;
    private AssetSource                assetSource;
    private ViewSource                 viewSource;
    private FormatManager              formatManager;
    private String                     themeName;
    private Locale                     locale;
    private MimeType                   contentType;
    private MimeType[]                 acceptableMediaTypes;
    private Map<String, Object>        parameters;
    private Validation                 validation;
    private ActionContext              actionContext;
    private Result                     result;
    private Boolean                   ajax;
    private Boolean                   pjax;
    private Boolean                   debug;
    private Boolean                   multipart;
    private Boolean                   gzipSupport;
    private UserPrincipal             user;
    private Authentication            authentication;
    private Session                   session;
    private Map<String, List<String>> queryParams;
    private Map<String, Object>       queryParamsMap;
    private Boolean                   acceptValidationError;
    private Router                    externalRouter;
	
	public DefaultRequest(App app, AppHandler handler, RequestWrapper servletRequest, Response response){
		this.app    		 = app;
		this.config			 = app.getWebConfig();
		this.handler		 = handler;
		this.response		 = response;
		this.req             = servletRequest;
		this.overrideMethod  = req.getHeader(Headers.X_HTTP_METHOD_OVERRIDE);
		this.method			 = Strings.upperCase(Strings.isEmpty(overrideMethod) ? req.getMethod() : overrideMethod);
		this.path    		 = extractRequestPath();
		this.locale          = req.getLocale();
		this.resolveServicePath(path);
	}
	
	@Override
    public void removeAttribute(String name) {
		req.removeAttribute(name);
    }

	@Override
    public void setAttribute(String name, Object value) {
	    req.setAttribute(name, value);
    }

	@Override
    public Object getAttribute(String name) {
	    return req.getAttribute(name);
    }

	@Override
    public AppContext getAppContext() {
	    return app.context();
    }

	@Override
    public App app() {
	    return app;
    }
	
	@Override
    public Response response() {
	    return response;
    }

	@Override
    public Params params() {
	    return params;
    }

	@Override
    public Session getSession() {
		if(null == session){
			session = new ServletSession(req.getSession());
		}
		return session;
    }
	
	@Override
    public Session getSession(boolean create) {
		HttpSession hs = req.getSession(create);
		
		if(null == hs){
			session = null;
		}else if(null == session || session.getServletSession() != hs) {
			session = new ServletSession(hs);
		}

		return session;
    }
	
	@Override
    public MessageSource getMessageSource() {
		return messageSource;
	}

	@Override
    public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
    public Locale getLocale() {
	    return locale;
    }
	
	@Override
    public void setLocale(Locale locale) {
		this.locale = locale;
    }
	
	@Override
    public AssetSource getAssetSource() {
	    return assetSource;
    }

	@Override
    public void setAssetSource(AssetSource assetSource) {
		this.assetSource = assetSource;
    }

	@Override
    public ViewSource getViewSource() {
	    return viewSource;
    }

	@Override
    public void setViewSource(ViewSource viewSource) {
		this.viewSource = viewSource;
    }

	@Override
    public String getThemeName() {
	    return themeName;
    }

	@Override
    public void setThemeName(String themeName) {
		this.themeName = themeName;
    }

	@Override
    public boolean isDebug() {
		if(null == debug){
			debug = app.config().isDebug();
		}
	    return debug;
    }

	@Override
    public void setDebug(boolean debug) {
		this.debug = debug;
    }

	@Override
    public String getContextPath() {
	    return req.getContextPath();
    }

	@Override
    public String getUri() {
	    return req.getRequestURI();
    }
	
	@Override
    public String getUriWithQueryString() {
	    String qs = getQueryString();
	    if(null == qs || qs.length() == 0) {
	        return req.getRequestURI();
	    }else{
	        return req.getRequestURI() + "?" + getQueryString();    
	    }
    }

    @Override
    public String getPath() {
	    return path;
    }
	
	@Override
    public String getPath(boolean lowercase) {
		if(lowercase){
			if(null == lowercaseRequestPath){
				lowercaseRequestPath = path.toLowerCase();
			}
			return lowercaseRequestPath;
		}
		return path;
    }
	
	@Override
    public int getContentLength() {
	    return req.getContentLength();
    }

	@Override
    public MimeType getContentType() {
		if(null == contentType){
			String ct = req.getContentType();
			if(!Strings.isEmpty(ct)){
				contentType = MimeTypes.parse(ct);
			}
		}
	    return contentType;
    }

	@Override
    public String getContentTypeValue() {
	    return req.getContentType();
    }

	@Override
    public MimeType[] getAcceptableMediaTypes() {
		if(null == acceptableMediaTypes){
			List<MimeType> mediaTypes = MimeTypes.parseList(getHeader(Headers.ACCEPT));
			if(mediaTypes.isEmpty()){
				acceptableMediaTypes = MimeTypes.EMPTY_ARRAY;
			}else{
				acceptableMediaTypes = mediaTypes.toArray(new MimeType[mediaTypes.size()]);
			}
		}
	    return acceptableMediaTypes;
    }

	@Override
    public String getQueryString() {
	    return req.getQueryString();
    }

	@Override
    public String getBasePath() {
	    return app.getBasePath();
    }
	
	@Override
    public String getServerUrl() {
	    if(null == serverUrl) {
	        URI uri;
	        try {
	            uri = new URI(req.getRequestURL().toString());
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Invalid syntax of request url '" + req.getRequestURL() + "' : " + e.getMessage(), e);
            }
	        
	        StringBuilder url = new StringBuilder();
	        
	        //{scheme}://{host}[:port]
	        url.append(uri.getScheme()).append("://")
	           .append(uri.getHost());
	        
	        int port = uri.getPort();
	        if( port != -1 && ( req.isSecure() ? port != 443 : port != 80 )) {
	            url.append(':').append(port);
	        }
	        
	        serverUrl = url.toString();
	    }
	    
        return serverUrl;
    }
    /**
     * get the serverUrl, if has reverse proxy，you need set：host and x-forwarded-proto in header，such as:
     *
     * host:localhost:8080
     * x-forwarded-proto:https
     *
     * otherwise, return <code>serverUrl()</code>
     *
     */
    @Override
    public String getReverseProxyServerUrl() {
	    if(null == reverseProxyServerUrl){
            String schema= getHeader("x-forwarded-proto");
            String host = getHeader("x-forwarded-host");
            if(Strings.isEmpty(host)){
                host = getHeader("host");
            }
            URI uri;
            try {
                uri = new URI(req.getRequestURL().toString());
                if(Strings.isEmpty(schema)){
                    schema = uri.getScheme();
                }
                if(Strings.isEmpty(host)){
                    host = uri.getHost();
                }
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Invalid syntax of request url '" + req.getRequestURL() + "' : " + e.getMessage(), e);
            }
            
            schema+="://";
            String url=schema+host;
            url=regularUrl(url);
            reverseProxyServerUrl = url;
        }
        return reverseProxyServerUrl;
    }
    
    protected String regularUrl(String url){
        //remove default port
        url += "/";
        if(url.startsWith("https") || url.startsWith("HTTPS")){
            url = url.replaceFirst(":443/", "/");
        }else{
            url = url.replaceFirst(":80/", "/");
        }
        return url.substring(0,url.length()-1);
    }
    
    @Override
    public String getContextUrl() {
        if(null == contextUrl) {
            contextUrl = getServerUrl() + getContextPath();
        }
        return contextUrl;
    }

    @Override
    public String getReverseProxyContextUrl() {
	    if(null == reverseProxyContextUrl){
            reverseProxyContextUrl = getReverseProxyServerUrl() + getContextPath();
        }
        return reverseProxyContextUrl;
    }

    @Override
    public String getRealRemoteHost() {
        String ipAddress = getHeader("x-forwarded-for");
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = getHeader("Proxy-Client-IP");
        }
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = getHeader("WL-Proxy-Client-IP");
        }
        if(ipAddress!=null && ipAddress.length()>15){  
            if(ipAddress.indexOf(",")>0){
                ipAddress = ipAddress.substring(0,ipAddress.indexOf(","));
            }
        }
        if(null == ipAddress || Strings.isEmpty(ipAddress)){
            ipAddress = req.getRemoteHost();
        }
        return ipAddress;
    }

    @Override
    public String getServicePath() {
	    return servicePath;
    }
	
	@Override
    public String getServicePathWithoutExtension() {
	    return servicePathWithoutExtension;
    }

	@Override
    public String getPathExtension() {
	    return pathExtension;
    }
	
	@Override
    public boolean hasPathExtension() {
	    return !"".equals(pathExtension);
    }

	@Override
    public String getMethod() {
	    return method;
    }

	@Override
    public String getOverrideMethod() {
	    return overrideMethod;
    }

	@Override
    public String getRawMethod() {
	    return req.getMethod();
    }
	
	@Override
    public String getParameter(String name) {
		if(config.isAutoTrimParameters()){
			//Auto trim request parameters
			String s = req.getParameter(name);
			return null == s ? null : s.trim();
		}else{
			return req.getParameter(name);
		}
    }

	@Override
    public String[] getParameterValues(String name) {
	    String[] values = req.getParameterValues(name);
	    
	    //Auto trim request parameters
	    if(null != values && config.isAutoTrimParameters()){
	    	for(int i=0;i<values.length;i++){
	    		values[i] = Strings.trim(values[i]);
	    	}
	    }
	    
	    return values;
    }
	
	@Override
    public boolean hasParameter(String name) {
	    return req.getParameterMap().containsKey(name);
    }
	
    @Override
    public Map<String, Object> getParameters() {
		if(null == parameters){
			parameters = initRequestParameters();
		}
		return parameters;
	}
    
    @Override
    public String getQueryParameter(String name) {
        List<String> values = queryParams().get(name);
        if(null == values) {
            return null;
        }else{
            return values.get(0);
        }
    }

    @Override
    public String[] getQueryParameterValues(String name) {
        Object v = getQueryParameters().get(name);
        if(null == v){
            return null;
        }else if(v instanceof String) {
            return new String[]{(String)v};
        }else{
            return (String[])v;
        }
    }

    @Override
    public boolean hasQueryParameter(String name) {
        return queryParams().containsKey(name);
    }
    
    @Override
    public Map<String, Object> getQueryParameters() {
        if(null == queryParamsMap) {
            
            queryParamsMap = new LinkedHashMap<String, Object>(queryParams().size());
            for(Entry<String, List<String>> entry : queryParams().entrySet()) {
                List<String> values = entry.getValue();
                if(values.size() == 1){
                    queryParamsMap.put(entry.getKey(), values.get(0));
                }else{
                    queryParamsMap.put(entry.getKey(), values.toArray(new String[values.size()]));
                }
            }
        }
        
        return queryParamsMap;
    }

    protected Map<String, List<String>> queryParams() {
        if(null == queryParams) {
            queryParams = QueryStringParser.parseMap(getQueryString());
        }
        return queryParams;
    }
	
	@Override
    public Part getPart(String name) {
	    try {
	        return req.getPart(name);
        } catch (IOException e) {
        	throw new NestedIOException("Error getting part '" + name + "', " + e.getMessage(), e);
        } catch (ServletException e) {
        	throw new NestedServletException("Error getting part '" + name + "', " + e.getMessage(), e);
        }
    }

	@Override
    public Collection<Part> getParts() {
	    try {
	        return req.getParts();
        } catch (IOException e) {
        	throw new NestedIOException("Error getting parts, " + e.getMessage(), e);
        } catch (ServletException e) {
        	throw new NestedServletException("Error getting parts, " + e.getMessage(), e);
        }
    }
	
	@Override
    public boolean hasHeader(String name) {
		Enumeration<String> names = req.getHeaderNames();
		while(names.hasMoreElements()) {
			if(names.nextElement().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
    }

	@Override
    public String getHeader(String name) {
	    return req.getHeader(name);
    }
	
	@Override
    public long getDateHeader(String name) {
	    return req.getDateHeader(name);
    }
	
	@Override
    public Cookie getCookie(String name) {
		Cookie[] cookies = req.getCookies();
		if(null == cookies || cookies.length == 0){
			return null;
		}
		
		for(Cookie cookie : cookies){
			if(cookie.getName().equals(name)){
				return cookie;
			}
		}
		
		return null;
    }
	
	@Override
    public Cookie[] getCookies() {
	    return req.getCookies();
    }

    @Override
    public BufferedInputStream peekInputStream() throws IOException {
        return req.peekInputStream();
    }

    @Override
    public byte[] peekInputStreamAsBytes() throws IOException {
        return req.peekBytes();
    }

    @Override
    public InputStream getInputStream() {
	    try {
	        return req.getInputStream();
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }
    }
	
	@Override
    public BufferedReader getReader() throws NestedIOException {
	    try {
	        return req.getReader();
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }
    }

	@Override
    public ServletContext getServletContext() {
	    return app.getServletContext();
    }

	@Override
    public HttpServletRequest getServletRequest() {
	    return req;
    }
	
	@Override
    public boolean isSecure() {
	    return req.isSecure();
    }

	@Override
    public String getCharacterEncoding() {
	    return req.getCharacterEncoding();
    }

	@Override
    public void setCharacterEncoding(String charset) {
		try {
	        req.setCharacterEncoding(charset);
        } catch (UnsupportedEncodingException e) {
        	Exceptions.wrapAndThrow("Unsupported charset '" + charset + "'", e);
        }
	}
	
	@Override
    public boolean isAjax() {
		if(null == ajax) {
			ajax = config.getAjaxDetector().detectAjaxRequest(this);
		}
	    return ajax;
    }
	
	@Override
	public void setAjax(boolean ajax) {
		this.ajax = ajax;
	}
	
	@Override
    public boolean isPjax() {
		if(null == pjax) {
			pjax = config.getPjaxDetector().detectPjaxRequest(this);
		}
	    return pjax;
    }

	@Override
    public void setPjax(boolean pjax) {
		this.pjax = pjax;
    }

	@Override
    public boolean isMultipart() {
		if(null == multipart) {
			MimeType contentType = getContentType();
			if(null == contentType){
				return false;
			}
			//multipart/form-data
			multipart = contentType.getType().equals("multipart") && contentType.getSubtype().equals("form-data");
		}
		return multipart;
	}
	
	@Override
    public boolean isGzipSupport() {
		if( null == gzipSupport) {
			String encoding = req.getHeader(Headers.ACCEPT_ENCODING);
			if(null != encoding && encoding.contains("gzip")) {
				gzipSupport = true;
			}else{
				gzipSupport = false;
			}
		}
		
	    return gzipSupport;
    }

	@Override
    public boolean isMethod(Method httpMethod) {
	    return null == httpMethod ? false : getMethod().equalsIgnoreCase(httpMethod.name());
    }
	
	@Override
    public void forward(String path) throws ServletException,IOException {
        req.getRequestDispatcher(path).forward(req, response.getServletResponse());
	}
	
	@Override
    public void forwardToView(String viewName) throws ServletException, IOException {
		try {
			View view = viewSource.getView(viewName, locale);
			if(null == view) {
				response.sendError(404,"View '" + viewName + "' not found");
				return;
			}
	        view.render(this, response);
		} catch (RuntimeException e){
			throw e;
        } catch (Throwable e) {
        	throw new ServletException("Error forwarding to view '" + viewName + ", " + e.getMessage(), e);
        }
    }
	
	public void forwardToAction(String actionPath) throws ServletException, IOException {
		try {
	        if(!handler.handleAction(this, response, actionPath) ){
	        	throw new AppException("Cannot forward to action '" + actionPath + "', not found");
	        }
		} catch (RuntimeException e){
			throw e;
        } catch (Throwable e) {
        	throw new ServletException("Error forwarding to action path '" + actionPath + ", " + e.getMessage(), e);
        }
	}

	@Override
	public ActionContext getActionContext() {
		return actionContext;
	}

	@Override
	public void setActionContext(ActionContext actionContext) {
		this.actionContext = actionContext;
	}

	@Override
    public Result getResult() {
	    return result;
    }

	@Override
    public void setResult(Result result) {
		Assert.notNull(result,"The 'result' argument must not be null");
		this.result = result;
	}
	
	@Override
    public UserPrincipal getUser() {
	    return user;
    }

	@Override
    public void setUser(UserPrincipal user) {
		this.user = user;
    }

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public FormatManager getFormatManager() {
	    return formatManager;
    }

	@Override
    public void setFormatManager(FormatManager formatManager) {
		this.formatManager = formatManager;
    }

	@Override
    public Validation getValidation() {
		return validation;
    }
	
	@Override
	public void setValidation(Validation validation) {
		Assert.notNull(validation);
		this.validation = validation;
	}
	
	@Override
    public Boolean getAcceptValidationError() {
        return acceptValidationError;
    }

    @Override
    public void setAcceptValidationError(Boolean accept) {
        this.acceptValidationError = accept;
    }

    @Override
    public Router getExternalRouter() {
        return externalRouter;
    }

    public void setExternalRouter(Router router) {
        this.externalRouter = router;
    }

    protected String extractRequestPath(){
		String path;
		if(Strings.isEmpty(req.getContextPath())){
			path = req.getRequestURI();
		}else{
			path = req.getRequestURI().substring(req.getContextPath().length());
		}
		
		//removes jsessionid
		int index;
		if(req.isRequestedSessionIdFromURL()){
			index = path.lastIndexOf(';');
			if( index > 0){
				path = path.substring(0,index);
			}
		}else if((index = path.lastIndexOf(app.getWebConfig().getJsessionidPrefix())) > 0) {
			path = path.substring(0,index);
		}
		
		//removes '/' if the path ends with '/'
		if(path.length() > 1){
			int lastIndex = path.length() - 1;
			if(path.charAt(lastIndex) == '/'){
				path = path.substring(0,lastIndex);
			}
		}

		// replace two or more slash with only one slash.
        path = path.replaceAll("\\/+", "/");
		
		return path;	
	}
	
	protected void resolveServicePath(String path){
		this.servicePath = extractServicePath(path);
		int lastDotIndex = this.servicePath.lastIndexOf('.');
		if(lastDotIndex >= 0 && lastDotIndex < servicePath.length() - 1){
			servicePathWithoutExtension = servicePath.substring(0,lastDotIndex);
			pathExtension        = servicePath.substring(lastDotIndex + 1);
		}else{
			servicePathWithoutExtension = servicePath;
			pathExtension        = "";
		}
	}
	
	protected String extractServicePath(String path){
		if(Strings.isEmpty(getBasePath())){
			return path;
		}else if(Strings.startsWith(path, getBasePath())){
			return path.substring(getBasePath().length());
		}else{
			return path;
		}
	}
	
	protected Map<String, Object> initRequestParameters(){
		Map<String, Object> map = new LinkedHashMap<String, Object>();

		boolean trim = config.isAutoTrimParameters();
		
		for(Entry<String, String[]> entry : req.getParameterMap().entrySet()){
			String   name  = entry.getKey();
			String[] value = entry.getValue();
			
			if(null == value || value.length == 0){
				map.put(name, Strings.EMPTY);
			}else if(value.length == 1) {
				map.put(name, trim ? Strings.trim(value[0]) : value[0]);
			}else{
				for(int i=0;i<value.length;i++){
					value[i] = trim ? Strings.trim(value[i]) : value[i];
				}
				map.put(name, value);
			}
		}
		
		return Collections.unmodifiableMap(map);
	}
	
	@Override
    public String toString() {
		return new ToStringBuilder(this).append("method", getMethod()).append("uri", req.getRequestURI()).toString();
	}

	protected final class ServletSession implements Session {
		
		protected final HttpSession hs;

		private boolean valid = true;
		
		public ServletSession(HttpSession session) {
			this.hs = session;
		}

		@Override
        public Object getAttribute(String name) {
	        return hs.getAttribute(name);
        }

		@Override
        public void removeAttribute(String name) {
			hs.removeAttribute(name);
        }

		@Override
        public void setAttribute(String name, Object value) {
	        hs.setAttribute(name, value);
        }

		@Override
        public void invalidate() {
			valid   = false;
			session = null;
			hs.invalidate();
        }

		@Override
        public boolean valid() {
	        return valid;
        }

		@Override
        public HttpSession getServletSession() throws IllegalStateException {
	        return hs;
        }
	}
	
	protected final class SimpleParams implements Params {

        @Override
        public Iterable<String> names() {
            final Enumeration<String> names = req.getParameterNames();
            return () -> new Iterator<String>() {
                @Override
                public boolean hasNext() {
                    return names.hasMoreElements();
                }

                @Override
                public String next() {
                    return names.nextElement();
                }
            };
        }

        @Override
        public String get(String name) {
	        return req.getParameter(name);
        }

		@Override
        public String[] getArray(String name) {
			String[] v = req.getParameterValues(name);
			if(null == v){
				return Arrays2.EMPTY_STRING_ARRAY;
			}else{
				return v;
			}
        }
		
	}
}
