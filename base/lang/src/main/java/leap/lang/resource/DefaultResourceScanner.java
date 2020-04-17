/*
 * Copyright 2002-2012 the original author or authors.
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

package leap.lang.resource;

import leap.lang.Args;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.exception.NestedIOException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.net.NET;
import leap.lang.net.Urls;
import leap.lang.path.AntPathMatcher;
import leap.lang.path.PathMatcher;
import leap.lang.path.Paths;
import leap.lang.reflect.Reflection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A {@link ResourceScanner} implementation that is able to resolve a specified resource location path into one
 * or more matching Resources. The source path may be a simple path which has a one-to-one mapping to a target
 * {@link Resource}, or alternatively may contain the special "<code>classpath*:</code>"
 * prefix and/or internal Ant-style regular expressions (matched using 
 * {@link AntPathMatcher} utility). Both of the latter are effectively wildcards.
 * 
 * <p>
 * <b>No Wildcards:</b>
 * 
 * <p>
 * In the simple case, if the specified location path does not start with the <code>"classpath*:</code>" prefix, and
 * does not contain a PathMatcher pattern, this resolver will simply return a single resource via a
 * <code>getResource()</code> call on the underlying <code>ResourceLoader</code>. Examples are real URLs such as "
 * <code>file:C:/context.xml</code>", pseudo-URLs such as "<code>classpath:/context.xml</code>", and simple unprefixed
 * paths such as "<code>/WEB-INF/context.xml</code>". The latter will resolve in a fashion specific to the underlying
 * <code>ResourceLoader</code> (e.g. <code>ServletContextResource</code> for a <code>WebApplicationContext</code>).
 * 
 * <p>
 * <b>Ant-style Patterns:</b>
 * 
 * <p>
 * When the path location contains an Ant-style pattern, e.g.:
 * 
 * <pre>
 * /WEB-INF/*-context.xml
 * com/mycompany/**&#47;applicationContext.xml
 * file:C:/some/path/*-context.xml
 * classpath:com/mycompany/**&#47;applicationContext.xml
 * </pre>
 * 
 * the resolver follows a more complex but defined procedure to try to resolve the wildcard. It produces a
 * <code>Resource</code> for the path up to the last non-wildcard segment and obtains a <code>URL</code> from it. If
 * this URL is not a "<code>jar:</code>" URL or container-specific variant (e.g. "<code>zip:</code>" in WebLogic, "
 * <code>wsjar</code>" in WebSphere", etc.), then a <code>java.io.File</code> is obtained from it, and used to resolve
 * the wildcard by walking the filesystem. In the case of a jar URL, the resolver either gets a
 * <code>java.net.JarURLConnection</code> from it, or manually parses the jar URL, and then traverses the contents of
 * the jar file, to resolve the wildcards.
 * 
 * <p>
 * <b>Implications on portability:</b>
 * 
 * <p>
 * If the specified path is already a file URL (either explicitly, or implicitly because the base
 * <code>ResourceLoader</code> is a filesystem one, then wildcarding is guaranteed to work in a completely portable
 * fashion.
 * 
 * <p>
 * If the specified path is a classpath location, then the resolver must obtain the last non-wildcard path segment URL
 * via a <code>Classloader.getResource()</code> call. Since this is just a node of the path (not the file at the end) it
 * is actually undefined (in the ClassLoader Javadocs) exactly what sort of a URL is returned in this case. In practice,
 * it is usually a <code>java.io.File</code> representing the directory, where the classpath resource resolves to a
 * filesystem location, or a jar URL of some sort, where the classpath resource resolves to a jar location. Still, there
 * is a portability concern on this operation.
 * 
 * <p>
 * If a jar URL is obtained for the last non-wildcard segment, the resolver must be able to get a
 * <code>java.net.JarURLConnection</code> from it, or manually parse the jar URL, to be able to walk the contents of the
 * jar, and resolve the wildcard. This will work in most environments, but will fail in others, and it is strongly
 * recommended that the wildcard resolution of resources coming from jars be thoroughly tested in your specific
 * environment before you rely on it.
 * 
 * <p>
 * <b><code>classpath*:</code> Prefix:</b>
 * 
 * <p>
 * There is special support for retrieving multiple class path resources with the same name, via the "
 * <code>classpath*:</code>" prefix. For example, "<code>classpath*:META-INF/beans.xml</code>" will find all "beans.xml"
 * files in the class path, be it in "classes" directories or in JAR files. This is particularly useful for
 * autodetecting config files of the same name at the same location within each jar file. Internally, this happens via a
 * <code>ClassLoader.getResources()</code> call, and is completely portable.
 * 
 * <p>
 * The "classpath*:" prefix can also be combined with a PathMatcher pattern in the rest of the location path, for
 * example "classpath*:META-INF/*-beans.xml". In this case, the resolution strategy is fairly simple: a
 * <code>ClassLoader.getResources()</code> call is used on the last non-wildcard path segment to get all the matching
 * resources in the class loader hierarchy, and then off each resource the same PathMatcher resolution strategy
 * described above is used for the wildcard subpath.
 * 
 * <p>
 * <b>Other notes:</b>
 * 
 * <p>
 * <b>WARNING:</b> Note that "<code>classpath*:</code>" when combined with Ant-style patterns will only work reliably
 * with at least one root directory before the pattern starts, unless the actual target files reside in the file system.
 * This means that a pattern like "<code>classpath*:*.xml</code>" will <i>not</i> retrieve files from the root of jar
 * files but rather only from the root of expanded directories. This originates from a limitation in the JDK's
 * <code>ClassLoader.getResources()</code> method which only returns file system locations for a passed-in empty String
 * (indicating potential roots to search).
 * 
 * <p>
 * <b>WARNING:</b> Ant-style patterns with "classpath:" resources are not guaranteed to find matching resources if the
 * root package to search is available in multiple class path locations. This is because a resource such as
 * 
 * <pre>
 * com / mycompany / package1 / service - context.xml
 * </pre>
 * 
 * may be in only one location, but when a path such as
 * 
 * <pre>
 *     classpath:com/mycompany/**&#47;service-context.xml
 * </pre>
 * 
 * is used to try to resolve it, the resolver will work off the (first) URL returned by
 * <code>getResource("com/mycompany");</code>. If this base package node exists in multiple classloader locations, the
 * actual end resource may not be underneath. Therefore, preferably, use "<code>classpath*:<code>" with the same
 * Ant-style pattern in such a case, which will search <i>all</i> class path
 * locations that contain the root package.
 * 
 * @author Juergen Hoeller
 * @author Colin Sampaleanu
 * @author Marius Bogoevici
 * @author Costin Leau
 * @since 1.0.2
 * @see #CLASSPATH_ALL_URL_PREFIX
 * @see leap.lang.path.springframework.util.AntPathMatcher
 * @see org.springframework.core.io.ResourceLoader#getResource(String)
 * @see java.lang.ClassLoader#getResources(String)
 */
class DefaultResourceScanner implements ResourceScanner {

	private static final Log logger = LogFactory.get(DefaultResourceScanner.class);

	private static Method equinoxResolveMethod;

	static {
		// Detect Equinox OSGi (e.g. on WebSphere 6.1)
		try {
			Class<?> fileLocatorClass = DefaultResourceScanner.class.getClassLoader().loadClass("org.eclipse.core.runtime.FileLocator");
			equinoxResolveMethod = fileLocatorClass.getMethod("resolve", URL.class);
			logger.debug("Found Equinox FileLocator for OSGi bundle URL resolution");
		} catch (Throwable ex) {
			equinoxResolveMethod = null;
		}
	}

	private final ResourceLoader resourceLoader;
	
	private boolean         quietly     = true;
	private PathMatcher     pathMatcher = Resources.matcher;
	private ExecutorService executorService;

	public DefaultResourceScanner() {
		this.resourceLoader = new DefaultResourceLoader();
	}
	

	public DefaultResourceScanner(ClassLoader classLoader) {
		this.resourceLoader = new DefaultResourceLoader(classLoader);
	}

	public DefaultResourceScanner(ResourceLoader resourceLoader) {
		Args.notNull(resourceLoader, "ResourceLoader");
		this.resourceLoader = resourceLoader;
	}
	
	public boolean isQuietly() {
		return quietly;
	}

	public void setQuietly(boolean quietly) {
		this.quietly = quietly;
	}

	public ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	public ClassLoader getClassLoader() {
		return getResourceLoader().getClassLoader();
	}

	public void setPathMatcher(PathMatcher pathMatcher) {
		Args.notNull(pathMatcher, "PathMatcher must not be null");
		this.pathMatcher = pathMatcher;
	}

	public PathMatcher getPathMatcher() {
		return this.pathMatcher;
	}
	
	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public Resource getResource(String location) {
		return getResourceLoader().getResource(location);
	}
	
	@Override
    public String extractRootDirPath(String location) {
		int prefixEnd = location.indexOf(":") + 1;
		int rootDirEnd = location.length();
		while (rootDirEnd > prefixEnd && getPathMatcher().isPattern(location.substring(prefixEnd, rootDirEnd))) {
			rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1;
		}
		if (rootDirEnd == 0) {
			rootDirEnd = prefixEnd;
		}
		return location.substring(0, rootDirEnd);
    }

	public Resource[] scan(String locationPattern) throws IOException {
		Args.notNull(locationPattern, "Location pattern");
		if (locationPattern.startsWith(Urls.CLASSPATH_ALL_URL_PREFIX)) {
			String locationPatternWithPrefix = locationPattern.substring(Urls.CLASSPATH_ALL_URL_PREFIX.length());
			
			// a class path resource (multiple resources for same name possible)
			if (getPathMatcher().isPattern(locationPatternWithPrefix)) {
				// a class path resource pattern
				return findPathMatchingResources(locationPattern);
			} else {
				// all class path resources with the given name
				return findAllClassPathResources(locationPatternWithPrefix);
			}
		} else {
			// Only look for a pattern after a prefix here
			// (to not get fooled by a pattern symbol in a strange prefix).
			int prefixEnd = locationPattern.indexOf(":") + 1;
			if (getPathMatcher().isPattern(locationPattern.substring(prefixEnd))) {
				// a file pattern
				return findPathMatchingResources(locationPattern);
			} else {
				// a single resource with the given name
				return new Resource[] {getResourceLoader().getResource(locationPattern)};
			}
		}
	}

	/**
	 * Find all class location resources with the given location via the ClassLoader.
	 */
	protected Resource[] findAllClassPathResources(String location) throws IOException {
		String path = location;
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		Enumeration<URL> resourceUrls = getClassLoader().getResources(path);
		Set<Resource> result = new LinkedHashSet<>(16);
		while (resourceUrls.hasMoreElements()) {
			URL url = resourceUrls.nextElement();
			result.add(convertClassLoaderURL(url,location));
		}
		return result.toArray(new Resource[result.size()]);
	}

	protected Resource convertClassLoaderURL(URL url, String classPathPrefix) {
		return new UrlResource(url, classPathPrefix);
	}
	
	protected String getResourceLocation(Resource resource) throws IOException{
		if(!Strings.isEmpty(resource.getClasspath())){
			return Urls.CLASSPATH_ONE_URL_PREFIX + resource.getClasspath();
		}
		
		return Urls.FILE_URL_PREFIX + resource.getFile().getAbsolutePath();
	}
	
	public Resource[] scan(Resource rootDirResource,String subPattern) throws IOException {
		final String rootDirPath = getResourceLocation(rootDirResource);
		return findPathMatchingResources(rootDirPath, subPattern,new Resource[]{rootDirResource});
	}
	
	protected Resource[] findPathMatchingResources(String locationPattern) throws IOException {
		final String rootDirPath = extractRootDirPath(locationPattern);
		final String subPattern  = locationPattern.substring(rootDirPath.length());
		
		return findPathMatchingResources(rootDirPath, subPattern, scan(rootDirPath));
	}
		
	protected Resource[] findPathMatchingResources(final String rootDirPath,final String subPattern,Resource[] rootDirResources) throws IOException {
		
		final Set<Resource> result = null == executorService ? new LinkedHashSet<Resource>() : new CopyOnWriteArraySet<Resource>();
		
		List<Future<?>> futures = New.arrayList();
		
		for (Resource rootDirResource : rootDirResources) {
			
			if(isQuietly() && !rootDirResource.exists()){
				continue;
			}
			
			rootDirResource = resolveRootDirResource(rootDirResource);
			
			final Resource rootResourceToScan = rootDirResource;
			
			if(null != executorService){
				futures.add(executorService.submit(new Runnable() {
					@Override
					public void run() {
						try {
	                        scan(result,rootResourceToScan,rootDirPath,subPattern);
                        } catch (IOException e) {
                        	throw new NestedIOException(e);
                        }
					}
				}));
			}else{
				scan(result,rootDirResource,rootDirPath,subPattern);	
			}
		}
		
		if(null != executorService){
			for(Future<?> future : futures){
				try {
	                future.get();
                } catch (RuntimeException e) {
                	throw (RuntimeException)e;
                } catch (Exception e){
                	throw new RuntimeException(e);
                }
			}
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace("Resolved location pattern [" + subPattern + "] to resources " + result);
		}
		
		return result.toArray(new Resource[result.size()]);
	}
	
	protected void scan(Set<Resource> result,Resource rootDirResource,String rootDirPath,String subPattern) throws IOException{
		if (isJarResource(rootDirResource)) {
			result.addAll(doFindPathMatchingJarResources(rootDirResource, subPattern));
		} else if (rootDirResource.getURL().getProtocol().startsWith(Urls.PROTOCOL_VFS)) {
			result.addAll(VfsResourceMatchingDelegate.findMatchingResources(rootDirResource, subPattern, getPathMatcher()));
		} else {
			result.addAll(doFindPathMatchingFileResources(rootDirResource,rootDirPath,subPattern));
		}		
	}
	
	protected Resource resolveRootDirResource(Resource original) throws IOException {
		if (equinoxResolveMethod != null) {
			URL url = original.getURL();
			if (url.getProtocol().startsWith("bundle")) {
				return new UrlResource((URL)Reflection.invokeMethod(equinoxResolveMethod, null, url));
			}
		}
		return original;
	}

	protected boolean isJarResource(Resource resource) throws IOException {
		return Urls.isJarUrl(resource.getURL());
	}

	protected Set<Resource> doFindPathMatchingJarResources(Resource rootDirResource, String subPattern) throws IOException {

		URLConnection con = rootDirResource.getURL().openConnection();
		JarFile jarFile;
		String jarFileUrl;
		String rootEntryPath;
		boolean newJarFile = false;

		if (con instanceof JarURLConnection) {
			// Should usually be the case for traditional JAR files.
			JarURLConnection jarCon = (JarURLConnection) con;
			NET.useCachesIfNecessary(jarCon);
			jarFile = jarCon.getJarFile();
			jarFileUrl = jarCon.getJarFileURL().toExternalForm();
			JarEntry jarEntry = jarCon.getJarEntry();
			rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
		} else {
			// No JarURLConnection -> need to resort to URL file parsing.
			// We'll assume URLs of the format "jar:path!/entry", with the protocol
			// being arbitrary as long as following the entry format.
			// We'll also handle paths with and without leading "file:" prefix.
			String urlFile = rootDirResource.getURL().getFile();
			int separatorIndex = urlFile.indexOf(Urls.JAR_URL_SEPARATOR);
			if (separatorIndex != -1) {
				jarFileUrl = urlFile.substring(0, separatorIndex);
				rootEntryPath = urlFile.substring(separatorIndex + Urls.JAR_URL_SEPARATOR.length());
				jarFile = getJarFile(jarFileUrl);
			} else {
				jarFile = new JarFile(urlFile);
				jarFileUrl = urlFile;
				rootEntryPath = "";
			}
			newJarFile = true;
		}

		try {
			if (logger.isTraceEnabled()) {
				logger.trace("Looking for matching resources in jar file [" + jarFileUrl + "]");
			}
			if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
				// Root entry path must end with slash to allow for proper matching.
				// The Sun JRE does not return a slash here, but BEA JRockit does.
				rootEntryPath = rootEntryPath + "/";
			}

			if(!rootDirResource.getURLString().endsWith("/")) {
				// Root dir resource must end with slash to createRelative correctly.
				final String dirName = Paths.getDirNameByDirPath(rootEntryPath);
				rootDirResource = rootDirResource.createRelative(dirName + "/");
			}

			Set<Resource> result = new LinkedHashSet<Resource>(8);
			for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
				JarEntry entry = entries.nextElement();
				String entryPath = entry.getName();
				if (entryPath.startsWith(rootEntryPath)) {
					String relativePath = entryPath.substring(rootEntryPath.length());
					if (getPathMatcher().match(subPattern, relativePath)) {
						result.add(rootDirResource.createRelative(relativePath));
					}
				}
			}
			return result;
		}
		finally {
			// Close jar file, but only if freshly obtained -
			// not from JarURLConnection, which might cache the file reference.
			if (newJarFile) {
				jarFile.close();
			}
		}
	}

	/**
	 * Resolve the given jar file URL into a JarFile object.
	 */
	protected JarFile getJarFile(String jarFileUrl) throws IOException {
		if (jarFileUrl.startsWith(Urls.FILE_URL_PREFIX)) {
			try {
				return new JarFile(Urls.toURI(jarFileUrl).getSchemeSpecificPart());
			} catch (URISyntaxException ex) {
				// Fallback for URLs that are not valid URIs (should hardly ever happen).
				return new JarFile(jarFileUrl.substring(Urls.FILE_URL_PREFIX.length()));
			}
		} else {
			return new JarFile(jarFileUrl);
		}
	}

	/**
	 * Find all resources in the file system that match the given location pattern via the Ant-style PathMatcher.
	 */
	protected Set<Resource> doFindPathMatchingFileResources(Resource rootDirResource,String rootDirPath, String subPattern) throws IOException {

		File rootDir;
		try {
			rootDir = rootDirResource.getFile().getAbsoluteFile();
		} catch (IllegalStateException|NestedIOException ex) {
			if (logger.isWarnEnabled()) {
				logger.warn("Cannot search for matching files underneath " + rootDirResource
				        + " because it does not correspond to a directory in the file system", ex);
			}
			return Collections.emptySet();
		}
		return doFindMatchingFileSystemResources(rootDir, rootDirPath, subPattern);
	}

	/**
	 * Find all resources in the file system that match the given location pattern via the Ant-style PathMatcher.
	 */
	protected Set<Resource> doFindMatchingFileSystemResources(File rootDir, String rootDirPath, String subPattern) throws IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("Looking for matching resources in directory tree [" + rootDir.getPath() + "]");
		}
		Set<File> matchingFiles = retrieveMatchingFiles(rootDir, subPattern);
		Set<Resource> result = new LinkedHashSet<Resource>(matchingFiles.size());
		
		boolean isClasspath   = rootDirPath.startsWith(Urls.CLASSPATH_ALL_URL_PREFIX) || rootDirPath.startsWith(Urls.CLASSPATH_ONE_URL_PREFIX);
		int rootDirPathLength = rootDir.getAbsolutePath().length();
		String classpathRoot  = isClasspath ? rootDirPath.substring(rootDirPath.indexOf(":") + 1) : null; 
		
		if(null != classpathRoot && classpathRoot.startsWith("/")){
			classpathRoot = classpathRoot.substring(1);
		}
		
		for (File file : matchingFiles) {
			if(isClasspath){
				String classpath = classpathRoot + Paths.normalize(file.getAbsolutePath().substring(rootDirPathLength+1));
				result.add(new SimpleFileResource(file, classpath));
			}else{
				result.add(new SimpleFileResource(file));	
			}
		}
		return result;
	}

	/**
	 * Retrieve files that match the given path pattern, checking the given directory and its subdirectories.
	 */
	protected Set<File> retrieveMatchingFiles(File rootDir, String pattern) throws IOException {
		if (!rootDir.exists()) {
			// Silently skip non-existing directories.
			if (logger.isTraceEnabled()) {
				logger.trace("Skipping [" + rootDir.getAbsolutePath() + "] because it does not exist");
			}
			return Collections.emptySet();
		}
		if (!rootDir.isDirectory()) {
			// Complain louder if it exists but is no directory.
			if (logger.isWarnEnabled()) {
				logger.warn("Skipping [" + rootDir.getAbsolutePath() + "] because it does not denote a directory");
			}
			return Collections.emptySet();
		}
		if (!rootDir.canRead()) {
			if (logger.isWarnEnabled()) {
				logger.warn("Cannot search for matching files underneath directory [" + rootDir.getAbsolutePath()
				        + "] because the application is not allowed to read the directory");
			}
			return Collections.emptySet();
		}
		String fullPattern = Strings.replace(rootDir.getAbsolutePath(), File.separator, "/");
		if (!pattern.startsWith("/")) {
			fullPattern += "/";
		}
		fullPattern = fullPattern + Strings.replace(pattern, File.separator, "/");
		Set<File> result = new LinkedHashSet<File>(8);
		doRetrieveMatchingFiles(fullPattern, rootDir, result);
		return result;
	}

	/**
	 * Recursively retrieve files that match the given pattern, adding them to the given result list.
	 */
	protected void doRetrieveMatchingFiles(String fullPattern, File dir, Set<File> result) throws IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("Searching directory [" + dir.getAbsolutePath() +
					"] for files matching pattern [" + fullPattern + "]");
		}
		File[] dirContents = dir.listFiles();
		if (dirContents == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("Could not retrieve contents of directory [" + dir.getAbsolutePath() + "]");
			}
			return;
		}
		for (File content : dirContents) {
			String currPath = Strings.replace(content.getAbsolutePath(), File.separator, "/");
			if (content.isDirectory() && getPathMatcher().matchStart(fullPattern, currPath + "/")) {
				if (!content.canRead()) {
					if (logger.isTraceEnabled()) {
						logger.trace("Skipping subdirectory [" + dir.getAbsolutePath() +
								"] because the application is not allowed to read the directory");
					}
				}
				else {
					doRetrieveMatchingFiles(fullPattern, content, result);
				}
			}
			if (getPathMatcher().match(fullPattern, currPath)) {
				result.add(content);
			}
		}
	}

	/**
	 * Inner delegate class, avoiding a hard JBoss VFS API dependency at runtime.
	 */
	private static class VfsResourceMatchingDelegate {
		public static Set<Resource> findMatchingResources(Resource rootResource, String locationPattern, PathMatcher pathMatcher) throws IOException {
			Object root = VfsUtils.findRoot(rootResource.getURL());
			PatternVirtualFileVisitor visitor = new PatternVirtualFileVisitor(VfsUtils.getPath(root), locationPattern, pathMatcher);
			VfsUtils.visit(root, visitor);
			return visitor.getResources();
		}
	}

	/**
	 * VFS visitor for path matching purposes.
	 */
	private static class PatternVirtualFileVisitor implements InvocationHandler {
		private final String        subPattern;
		private final PathMatcher   pathMatcher;
		private final String        rootPath;
		private final Set<Resource> resources = new LinkedHashSet<Resource>();

		public PatternVirtualFileVisitor(String rootPath, String subPattern, PathMatcher pathMatcher) {
			this.subPattern = subPattern;
			this.pathMatcher = pathMatcher;
			this.rootPath = (rootPath.length() == 0 || rootPath.endsWith("/") ? rootPath : rootPath + "/");
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();
			if (Object.class.equals(method.getDeclaringClass())) {
				if (methodName.equals("equals")) {
					// Only consider equal when proxies are identical.
					return (proxy == args[0]);
				}
				else if (methodName.equals("hashCode")) {
					return System.identityHashCode(proxy);
				}
			}
			else if ("getAttributes".equals(methodName)) {
				return getAttributes();
			}
			else if ("visit".equals(methodName)) {
				visit(args[0]);
				return null;
			}
			else if ("toString".equals(methodName)) {
				return toString();
			}
			
			throw new IllegalStateException("Unexpected method invocation: " + method);
		}

		public void visit(Object vfsResource) {
			String relativePath = VfsUtils.getPath(vfsResource).substring(this.rootPath.length());
			if (this.pathMatcher.match(this.subPattern,relativePath)) {
				this.resources.add(new VfsResource(vfsResource));
			}
		}

		public Object getAttributes() {
			return VfsUtils.getVisitorAttribute();
		}

		public Set<Resource> getResources() {
			return this.resources;
		}

		@SuppressWarnings("unused")
		public int size() {
			return this.resources.size();
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("sub-pattern: ").append(this.subPattern);
			sb.append(", resources: ").append(this.resources);
			return sb.toString();
		}
	}
}
