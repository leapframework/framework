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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.reflect.Reflection;

/**
 * Utility for detecting the JBoss VFS version available in the classpath.
 * JBoss AS 5+ uses VFS 2.x (package <code>org.jboss.virtual</code>) while
 * JBoss AS 6+ uses VFS 3.x (package <code>org.jboss.vfs</code>).
 *
 * <p>Thanks go to Marius Bogoevici for the initial patch.
 *
 * <b>Note:</b> This is an internal class and should not be used outside the framework.
 *
 * @author Costin Leau
 */
abstract class VfsUtils {

	private static final Log logger = LogFactory.get(VfsUtils.class);

	private static final String VFS2_PKG = "org.jboss.virtual.";
	private static final String VFS3_PKG = "org.jboss.vfs.";
	private static final String VFS_NAME = "VFS";

	private static enum VFS_VER { V2, V3 }

	private static VFS_VER version;

	private static Method VFS_METHOD_GET_ROOT_URL = null;
	private static Method VFS_METHOD_GET_ROOT_URI = null;

	private static Method VIRTUAL_FILE_METHOD_EXISTS = null;
	private static Method VIRTUAL_FILE_METHOD_GET_INPUT_STREAM;
	private static Method VIRTUAL_FILE_METHOD_GET_SIZE;
	private static Method VIRTUAL_FILE_METHOD_GET_LAST_MODIFIED;
	private static Method VIRTUAL_FILE_METHOD_TO_URL;
	private static Method VIRTUAL_FILE_METHOD_TO_URI;
	private static Method VIRTUAL_FILE_METHOD_GET_NAME;
	private static Method VIRTUAL_FILE_METHOD_GET_PATH_NAME;
	private static Method VIRTUAL_FILE_METHOD_GET_CHILD;

	protected static Class<?> VIRTUAL_FILE_VISITOR_INTERFACE;
	protected static Method VIRTUAL_FILE_METHOD_VISIT;

	private static Method VFS_UTILS_METHOD_IS_NESTED_FILE = null;
	private static Method VFS_UTILS_METHOD_GET_COMPATIBLE_URI = null;
	private static Field VISITOR_ATTRIBUTES_FIELD_RECURSE = null;
	private static Method GET_PHYSICAL_FILE = null;

	static {
		ClassLoader loader = VfsUtils.class.getClassLoader();
		String pkg;
		Class<?> vfsClass;

		// check for JBoss 6
		try {
			vfsClass = loader.loadClass(VFS3_PKG + VFS_NAME);
			version = VFS_VER.V3;
			pkg = VFS3_PKG;

			if (logger.isDebugEnabled()) {
				logger.debug("JBoss VFS packages for JBoss AS 6 found");
			}
		}
		catch (ClassNotFoundException ex) {
			// fallback to JBoss 5
			if (logger.isDebugEnabled())
				logger.debug("JBoss VFS packages for JBoss AS 6 not found; falling back to JBoss AS 5 packages");
			try {
				vfsClass = loader.loadClass(VFS2_PKG + VFS_NAME);

				version = VFS_VER.V2;
				pkg = VFS2_PKG;

				if (logger.isDebugEnabled())
					logger.debug("JBoss VFS packages for JBoss AS 5 found");
			}
			catch (ClassNotFoundException ex2) {
				logger.error("JBoss VFS packages (for both JBoss AS 5 and 6) were not found - JBoss VFS support disabled");
				throw new IllegalStateException("Cannot detect JBoss VFS packages", ex2);
			}
		}

		// cache reflective information
		try {
			String methodName = (VFS_VER.V3.equals(version) ? "getChild" : "getRoot");

			VFS_METHOD_GET_ROOT_URL = Reflection.findMethod(vfsClass, methodName, URL.class);
			VFS_METHOD_GET_ROOT_URI = Reflection.findMethod(vfsClass, methodName, URI.class);

			Class<?> virtualFile = loader.loadClass(pkg + "VirtualFile");

			VIRTUAL_FILE_METHOD_EXISTS = Reflection.findMethod(virtualFile, "exists");
			VIRTUAL_FILE_METHOD_GET_INPUT_STREAM = Reflection.findMethod(virtualFile, "openStream");
			VIRTUAL_FILE_METHOD_GET_SIZE = Reflection.findMethod(virtualFile, "getSize");
			VIRTUAL_FILE_METHOD_GET_LAST_MODIFIED = Reflection.findMethod(virtualFile, "getLastModified");
			VIRTUAL_FILE_METHOD_TO_URI = Reflection.findMethod(virtualFile, "toURI");
			VIRTUAL_FILE_METHOD_TO_URL = Reflection.findMethod(virtualFile, "toURL");
			VIRTUAL_FILE_METHOD_GET_NAME = Reflection.findMethod(virtualFile, "getName");
			VIRTUAL_FILE_METHOD_GET_PATH_NAME = Reflection.findMethod(virtualFile, "getPathName");
			GET_PHYSICAL_FILE = Reflection.findMethod(virtualFile, "getPhysicalFile");

			methodName = (VFS_VER.V3.equals(version) ? "getChild" : "findChild");

			VIRTUAL_FILE_METHOD_GET_CHILD = Reflection.findMethod(virtualFile, methodName, String.class);

			Class<?> utilsClass = loader.loadClass(pkg + "VFSUtils");

			VFS_UTILS_METHOD_GET_COMPATIBLE_URI = Reflection.findMethod(utilsClass, "getCompatibleURI",
					virtualFile);
			VFS_UTILS_METHOD_IS_NESTED_FILE = Reflection.findMethod(utilsClass, "isNestedFile", virtualFile);

			VIRTUAL_FILE_VISITOR_INTERFACE = loader.loadClass(pkg + "VirtualFileVisitor");
			VIRTUAL_FILE_METHOD_VISIT = Reflection.findMethod(virtualFile, "visit", VIRTUAL_FILE_VISITOR_INTERFACE);

			Class<?> visitorAttributesClass = loader.loadClass(pkg + "VisitorAttributes");
			VISITOR_ATTRIBUTES_FIELD_RECURSE = Reflection.findField(visitorAttributesClass, "RECURSE");
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalStateException("Could not detect the JBoss VFS infrastructure", ex);
		}
	}

	protected static Object invokeVfsMethod(Method method, Object target, Object... args) throws IOException {
		try {
			return method.invoke(target, args);
		}
		catch (InvocationTargetException ex) {
			Throwable targetEx = ex.getTargetException();
			if (targetEx instanceof IOException) {
				throw (IOException) targetEx;
			}
			Reflection.handleException(ex);
		}
		catch (Exception ex) {
			Reflection.handleException(ex);
		}
		
		throw new IllegalStateException("Invalid code path reached");
	}

	static boolean exists(Object vfsResource) {
		try {
			return (Boolean) invokeVfsMethod(VIRTUAL_FILE_METHOD_EXISTS, vfsResource);
		}
		catch (IOException ex) {
			return false;
		}
	}

	static boolean isReadable(Object vfsResource) {
		try {
			return ((Long) invokeVfsMethod(VIRTUAL_FILE_METHOD_GET_SIZE, vfsResource) > 0);
		}
		catch (IOException ex) {
			return false;
		}
	}

	static long getSize(Object vfsResource) throws IOException {
		return (Long) invokeVfsMethod(VIRTUAL_FILE_METHOD_GET_SIZE, vfsResource);
	}

	static long getLastModified(Object vfsResource) throws IOException {
		return (Long) invokeVfsMethod(VIRTUAL_FILE_METHOD_GET_LAST_MODIFIED, vfsResource);
	}

	static InputStream getInputStream(Object vfsResource) throws IOException {
		return (InputStream) invokeVfsMethod(VIRTUAL_FILE_METHOD_GET_INPUT_STREAM, vfsResource);
	}

	static URL getURL(Object vfsResource) throws IOException {
		return (URL) invokeVfsMethod(VIRTUAL_FILE_METHOD_TO_URL, vfsResource);
	}

	static URI getURI(Object vfsResource) throws IOException {
		return (URI) invokeVfsMethod(VIRTUAL_FILE_METHOD_TO_URI, vfsResource);
	}

	static String getName(Object vfsResource) {
		try {
			return (String) invokeVfsMethod(VIRTUAL_FILE_METHOD_GET_NAME, vfsResource);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Cannot get resource name", ex);
		}
	}

	static Object getRelative(URL url) throws IOException {
		return invokeVfsMethod(VFS_METHOD_GET_ROOT_URL, null, url);
	}

	static Object getChild(Object vfsResource, String path) throws IOException {
		return invokeVfsMethod(VIRTUAL_FILE_METHOD_GET_CHILD, vfsResource, path);
	}

	static File getFile(Object vfsResource) throws IOException {
		if (VFS_VER.V2.equals(version)) {
			if ((Boolean) invokeVfsMethod(VFS_UTILS_METHOD_IS_NESTED_FILE, null, vfsResource)) {
				throw new IOException("File resolution not supported for nested resource: " + vfsResource);
			}
			try {
				return new File((URI) invokeVfsMethod(VFS_UTILS_METHOD_GET_COMPATIBLE_URI, null, vfsResource));
			}
			catch (Exception ex) {
				throw new IOException("Failed to obtain File reference for " + vfsResource, ex);
			}
		}
		else {
			return (File) invokeVfsMethod(GET_PHYSICAL_FILE, vfsResource);
		}
	}

	static Object getRoot(URI url) throws IOException {
		return invokeVfsMethod(VFS_METHOD_GET_ROOT_URI, null, url);
	}
	
	static Object getVisitorAttribute() {
		return doGetVisitorAttribute();
	}

	static String getPath(Object resource) {
		return doGetPath(resource);
	}

	static Object findRoot(URL url) throws IOException {
		return getRoot(url);
	}

	static void visit(Object resource, InvocationHandler visitor) throws IOException {
		Object visitorProxy = Proxy.newProxyInstance(VIRTUAL_FILE_VISITOR_INTERFACE.getClassLoader(),
				new Class<?>[] { VIRTUAL_FILE_VISITOR_INTERFACE }, visitor);
		invokeVfsMethod(VIRTUAL_FILE_METHOD_VISIT, resource, visitorProxy);
	}

	// protected methods used by the support sub-package

	protected static Object getRoot(URL url) throws IOException {
		return invokeVfsMethod(VFS_METHOD_GET_ROOT_URL, null, url);
	}

	protected static Object doGetVisitorAttribute() {
		return Reflection.getFieldValue((Object)null,VISITOR_ATTRIBUTES_FIELD_RECURSE);
	}

	protected static String doGetPath(Object resource) {
		return (String) Reflection.invokeMethod(VIRTUAL_FILE_METHOD_GET_PATH_NAME, resource);
	}
}