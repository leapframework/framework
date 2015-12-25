/*
 * Copyright 2014 the original author or authors.
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
package leap.web.assets;

import java.io.File;
import java.util.List;

import javax.servlet.ServletContext;

import leap.core.AppContext;
import leap.core.AppContextAware;
import leap.core.BeanFactory;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.servlet.Servlets;

public class DefaultAssetManager implements AssetManager,AppContextAware {
	
	private static final String SERVLET_CONTEXT_SERVICE_KEY  = AssetTask.class.getName();

	protected ServletContext		servletContext;
	protected BeanFactory		   	beanFactory;
	protected AssetConfig			config;
	protected AssetNamingStrategy   namingStrategy;
	protected List<AssetType> 		assetTypes;
	protected List<AssetCompiler>   assetCompilers;
	
	@Override
    public void setAppContext(AppContext appContext) {
		this.servletContext = appContext.tryGetServletContext();
		this.beanFactory    = appContext.getBeanFactory();
    }

	public AssetConfig getConfig() {
		return config;
	}

	public void setConfig(AssetConfig config) {
		this.config = config;
	}
	
	public AssetNamingStrategy getNamingStrategy() {
		return namingStrategy;
	}

	public void setNamingStrategy(AssetNamingStrategy namingStrategy) {
		this.namingStrategy = namingStrategy;
	}
	
	@Override
    public AssetTask getTask(ServletContext sc) {
		AssetTask task = (AssetTask)sc.getAttribute(SERVLET_CONTEXT_SERVICE_KEY);
		
		if(null == task){
			Resource wr = Servlets.getResource(sc,"");
			if(!wr.isFile()){
				return null;
			}
			
			task = createTask(wr.getFile());
			
			if(null != task){
				sc.setAttribute(SERVLET_CONTEXT_SERVICE_KEY, task);				
			}
		}
		
		return task;
    }

	@Override
    public AssetTask createTask(File webappDirectory) {
		File sourceDirectory = getSourceDirectory(webappDirectory);
		File outputDirectory = getOutputDirectory(webappDirectory);
		
		if(null == sourceDirectory || !sourceDirectory.exists()){
			return null;
		}
		
		return new DefaultAssetTask(this, sourceDirectory,outputDirectory);
    }

	@Override
    public AssetType resolveAssetTypeByFilename(String filename) {
		for(AssetType type : assetTypes){
			if(type.matches(filename)){
				return type;
			}
		}
		return null;
    }

	@Override
    public AssetCompiler resolveAssetCompilerByFilename(String filename) {
		for(AssetCompiler compiler : assetCompilers){
			if(compiler.supports(filename)){
				return compiler;
			}
		}
		return null;
	}

	@Override
    public AssetMinifier resolveAssetMinifier(AssetType type) {
	    return beanFactory.tryGetBean(AssetMinifier.class,type.getName());
    }

	@Override
    public List<AssetProcessor> resolveResourceProcessors(AssetType type) {
	    return beanFactory.getBeans(AssetProcessor.class,type.getName());
    }

	public void setAssetTypes(List<AssetType> assetTypes) {
		this.assetTypes = assetTypes;
	}

	public void setAssetCompilers(List<AssetCompiler> assetCompilers) {
		this.assetCompilers = assetCompilers;
	}
	
	protected File getSourceDirectory(File webappDirectory){
		String webappDirectoryPath = Paths.suffixWithSlash(webappDirectory.getAbsolutePath());
		String assetsDirectoryPath = Paths.prefixWithoutSlash(config.getSourceDirectory());
		return new File(Paths.applyRelative(webappDirectoryPath, assetsDirectoryPath));
	}
	
	protected File getOutputDirectory(File webappDirectory){
		String webappDirectoryPath = Paths.suffixWithSlash(webappDirectory.getAbsolutePath());
		String outputDirectoryPath = Paths.prefixWithoutSlash(config.getPublicDirectory());
		return new File(Paths.applyRelative(webappDirectoryPath, outputDirectoryPath));
	}
}