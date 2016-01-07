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
package leap.webunit.server;

import leap.lang.Charsets;
import leap.lang.Classes;
import leap.lang.resource.Resources;
import leap.lang.tools.DEV;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Embedded Web Server for Testing.
 */
public class TWebServer {
	
	public static final String ROOT_CONTEXT_PATH   = "";
	public static final String ROOT_WEB_APP_NAME   = "root";
	public static final int    DEFAULT_HTTP_PORT   = 8080;
	public static final int    DEFAULT_HTTPS_PORT  = 8443;
	
	static {
		//Lets 'Resources' can scan project's classes and test-classes which does not deploy to the WEB-INF/classes folder.
		Resources.setClassLoader(TWebServer.class.getClassLoader());
	}
	
	public static void main(String[] args) {
		new TWebServer(DEFAULT_HTTP_PORT,DEFAULT_HTTPS_PORT).start().join();
    }
	
	private final int						httpPort;
	private final int                       httpsPort;
	private final Server                    server;
	private final HttpConfiguration         httpConfig;
	private final ErrorHandler              errorHandler;
	private final Map<String,WebAppContext> contexts = new ConcurrentHashMap<String, WebAppContext>();
	
	private leap.lang.resource.FileResource currentDir;
	
	public TWebServer(int httpPort){
		this(httpPort, DEFAULT_HTTPS_PORT, true);
	}
	
    public TWebServer(int httpPort, int httpsPort) {
        this(httpPort, httpsPort, true);
    }
	
	public TWebServer(int httpPort, int httpsPort, boolean autoScanWebapps){
		this.httpPort     = httpPort;
		this.httpsPort    = httpsPort;
		this.httpConfig   = newHttpConfiguration();
		this.server       = newServer();
		this.errorHandler = new Utf8ErrorHandler();
		
		configureSSL();
		
		init();
		
		if(autoScanWebapps){
			autoScanWebapps();
		}
	}
	
	protected HttpConfiguration newHttpConfiguration() {
	    HttpConfiguration c = new HttpConfiguration();
	    
	    c.setSecureScheme("https");
	    c.setSecurePort(httpsPort);
	    
	    return c;
	}
	
	protected Server newServer() {
	    Server server = new Server();
	    
	    ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
	    connector.setPort(httpPort);
	    
	    server.addConnector(connector);
	    
	    return server;
	}
	
	public int getHttpPort(){
		return httpPort;
	}
	
	public int getHttpsPort() {
	    return httpsPort;
	}
	
	public boolean isStarted(){
		return server.isStarted();
	}

	public String[] getContextPaths() {
		return contexts.keySet().toArray(new String[]{});
	}
	
	/**
	 * Starts this server.
	 */
	public synchronized TWebServer start() {
        try {
        	if(contexts.isEmpty()){
        		throw new IllegalStateException("No webapp(s) exists in project directory '" +
												currentDir.getFilepath() +
												"', if running in IntelliJ IDEA, set Working Directory to $MODULE_DIR$");
        	}
        	
        	ContextHandlerCollection handlers = new ContextHandlerCollection();
        	handlers.setHandlers(contexts.values().toArray(new Handler[]{}));
        	server.setHandler(handlers);
        	
	        server.setStopAtShutdown(true);
	        server.start();
        } catch (Exception e) {
        	stop();
        	throw new RuntimeException("Failed startup : " + e.getMessage(), e);
        }
        
        if(!isAllWebAppsAvailable()){
        	stop();
        }
        
        return this;				
	}
	
	public synchronized TWebServer join(){
        try {
        	server.join();
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
        return this;		
	}
	
	/**
	 * Stops this server.
	 * 
	 * @throws RuntimeException if error occurred.
	 */
	public synchronized TWebServer stop(){
		try {
	        server.stop();
        } catch (Exception e) {
        	System.out.println("Error stopping server : " + e.getMessage());
        	e.printStackTrace();
        }
		return this;		
	}
	
	public boolean isAllWebAppsAvailable(){
		if(contexts.isEmpty()){
			return false;
		}
		
		for(WebAppContext context : contexts.values()){
			if(!context.isAvailable()){
				return false;
			}
		}
		
		return true;		
	}
	
	/**
	 * Returns the {@link ServletContext} of {@link #ROOT_CONTEXT_PATH}
	 */
	public ServletContext getServletContext() throws IllegalStateException {
		return getServletContext(ROOT_CONTEXT_PATH);
	}
	
	/**
	 * @throws IllegalStateException if the given context path not exists in this server.
	 */
	public ServletContext getServletContext(String contextPath) throws IllegalStateException{
		WebAppContext context = contexts.get(contextPath);
		
		if(null == context){
			throw new IllegalStateException("Context path '" + contextPath + "' not exists in this server");
		}
		
		return context.getServletContext();
	}
	
	public Map<String,ServletContext> getServletContexts(){
		Map<String, ServletContext> servletContexts = new LinkedHashMap<String, ServletContext>();

		for(Entry<String, WebAppContext> entry : contexts.entrySet()){
			servletContexts.put(entry.getKey(), entry.getValue().getServletContext());
		}
		
		return servletContexts;
	}
	
	public synchronized TWebServer addContext(String contextPath,String webappDirectory) {
		if(null == contextPath){
			throw new IllegalArgumentException("Argument 'context path' must not be null");
		}
		
		if(null == webappDirectory || "".equals(webappDirectory)){
			throw new IllegalArgumentException("Argument 'webapp directory' must nobe be null or empty");
		}
		
		File dir = new File(webappDirectory);
		if(!dir.exists()){
			throw new IllegalArgumentException("The given webapp dir '" + webappDirectory + "' not exists");
		}
		
		if(!dir.isDirectory()){
			throw new IllegalArgumentException("The given webapp dir '" + webappDirectory + "' is not a directory");
		}
		
		if(!"".equals(contextPath)){
			if(contextPath.equals("/")){
				contextPath = "";
			}else{
				if(!contextPath.startsWith("/")){
					contextPath = "/" + contextPath;
				}
				
				if(contextPath.endsWith("/")){
					contextPath = contextPath.substring(0,contextPath.length() - 2);
				}
			}
		}
		
		if(contexts.containsKey(contextPath)){
			throw new IllegalArgumentException("Context path '" + contextPath + "' aleady exists in this server");
		}
		
		WebAppContext context = new WebAppContext();
		context.setContextPath(contextPath);
		context.setClassLoader(Classes.getClassLoader());
		context.setParentLoaderPriority(true);
		context.setServer(server);
		context.setErrorHandler(errorHandler);
		context.setThrowUnavailableOnStartupException(true);
		
        try {
    		//set base resources
    		List<Resource> resources = new ArrayList<Resource>();

        	//web app dir
	        resources.add(new FileResource(dir.toURI().toURL()));
	        
	        //webapp classpath resources
	        Enumeration<URL> urls = context.getClassLoader().getResources("META-INF/resources/");
	        while(urls.hasMoreElements()){
	        	URL url = urls.nextElement();
	        	resources.add(Resource.newResource(url));
	        }
	        
	        context.setBaseResource(new ResourceCollection(resources.toArray(new Resource[0])));
	        
	        //config ServletContainerInitializer in the context.
	        AnnotationConfiguration ac = new AnnotationConfiguration();
	        ac.createServletContainerInitializerAnnotationHandlers(context, ac.getNonExcludedInitializers(context));
        } catch (Exception e) {
        	throw new RuntimeException("Error adding context '" + contextPath + "' : " + e.getMessage(),e);
        }
        
		contexts.put(contextPath, context);
		return this;
	}
	
	/**
	 * Removes a context from this server of the given context path.
	 */
	public boolean removeContext(String contextPath){
		return null != contexts.remove(contextPath);
	}
	
	public TWebServer duplicateContext(String existsContextPath,String duplicateContextPath) throws IllegalStateException{
		WebAppContext context = contexts.get(existsContextPath);
		
		if(null == context){
			throw new IllegalStateException("The given argument [existsContextPath] '" + existsContextPath + "' not exists");
		}
		
		if(contexts.containsKey(duplicateContextPath)){
			throw new IllegalStateException("The given argument [duplicateContextPath] '" + duplicateContextPath + "' aleady exists");
		}
		
		WebAppContext duplicateContext = new WebAppContext();
		duplicateContext.setContextPath(duplicateContextPath);
		duplicateContext.setBaseResource(context.getBaseResource());
		duplicateContext.setClassLoader(duplicateContext.getClassLoader());
		duplicateContext.setErrorHandler(duplicateContext.getErrorHandler());
		
		contexts.put(duplicateContextPath, duplicateContext);
		
		return this;
	}
	
    private void configureSSL() {
        String keyStoreResourcePath = Classes.getPackageResourcePath(TWebServer.class) + "/keystore.jks";
        Resource keyStoreResource   = Resource.newClassPathResource(keyStoreResourcePath);
        
        SslContextFactory sslContextFactory = new SslContextFactory(true);
        sslContextFactory.setKeyStoreResource(keyStoreResource);
        sslContextFactory.setKeyStorePassword("123456");
        
        HttpConfiguration c = new HttpConfiguration(httpConfig);
        c.addCustomizer(new SecureRequestCustomizer());
        
        ServerConnector sslConnector = 
                new ServerConnector(server, 
                                    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                                    new HttpConnectionFactory(c));
        
        sslConnector.setPort(httpConfig.getSecurePort());
        server.addConnector(sslConnector);
    }
	
	private void init() {
		currentDir = Resources.getUserDir();

        if(!currentDir.createRelative("./src/main").exists()) {
            Class<?> testClass = DEV.getCurrentTestClass();
            if(null != testClass) {
                String path = testClass.getResource("").getFile();
                int index = path.indexOf("/target/test-classes");
                if(index > 0) {
                    String dir = path.substring(0, index);
                    currentDir = Resources.createFileResource(dir);
                }
            }
        }
    }
	
	private void autoScanWebapps(){
        String projectHomePath = currentDir.getFile().getAbsolutePath();

		//root webapp
		File webappDir = new File(projectHomePath + "/src/main/webapp");
		if(!webappDir.exists()){
			webappDir = new File(projectHomePath + "/src/test/webapp");
		}
		if(webappDir.exists()){
			tryAddWebApp(ROOT_CONTEXT_PATH,webappDir);
		}
		
		//webapps
		tryAddWebapps(new File(projectHomePath + "/src/main/webapps"));
		tryAddWebapps(new File(projectHomePath + "/src/test/webapps"));
	}
	
	private void tryAddWebapps(File webappsDir){
		if(webappsDir.exists()){
			for(File file : webappsDir.listFiles()){
				if(file.isDirectory()){
					String name        = file.getName();
					String contextPath = ROOT_WEB_APP_NAME.equals(name) ? ROOT_CONTEXT_PATH : "/" + name;
					tryAddWebApp(contextPath, file);
				}
			}
		}
	}
	
	private void tryAddWebApp(String contextPath,File dir) {
		File webInfDir = new File(dir.getAbsolutePath() + "/WEB-INF");
		
		if(webInfDir.exists()){
			addContext(contextPath, dir.getAbsolutePath());
		}
	}
	
	protected static class Utf8ErrorHandler extends ErrorHandler {

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            String method = request.getMethod();
            if (!HttpMethod.GET.is(method) && !HttpMethod.POST.is(method) && !HttpMethod.HEAD.is(method))
            {
                baseRequest.setHandled(true);
                return;
            }
            
            baseRequest.setHandled(true);
            response.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());    
            response.setHeader(HttpHeader.CACHE_CONTROL.asString(), "must-revalidate,no-cache,no-store");
            
            ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
            OutputStreamWriter writer = new OutputStreamWriter(out, Charsets.UTF_8);
            String reason=(response instanceof Response)?((Response)response).getReason():null;
            handleErrorPage(request, writer, response.getStatus(), reason);
            writer.flush();
            response.setContentLength(out.size());
            response.getOutputStream().write(out.toByteArray(), 0, out.size());
            writer.close();
        }
	}
}