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
package leap.htpl;

import leap.htpl.ast.*;
import leap.htpl.ast.Node.ProcessCallback;
import leap.htpl.exception.HtplProcessException;
import leap.htpl.interceptor.ProcessInterceptor;
import leap.lang.Args;
import leap.lang.Exceptions;
import leap.lang.accessor.AttributeAccessor;
import leap.lang.accessor.PropertyGetter;
import leap.lang.collection.SimpleCaseInsensitiveMap;
import leap.lang.exception.ObjectExistsException;
import leap.lang.path.Paths;

import java.util.*;
import java.util.Map.Entry;

public class DefaultHtplDocument extends AbstractHtplObject implements PropertyGetter,AttributeAccessor, HtplDocument {
	
	protected final HtplEngine   engine;
	protected final HtplResource resource;

    protected Locale               locale;
    protected boolean              processed;
    protected String               layout;
    protected String               title;
    protected NodeContainer        nodes;
    protected Node[]               requiredHeaders;
    protected ProcessInterceptor[] processInterceptors;

    protected Map<String, String>       properties        = new HashMap<>();
    protected Map<String, Object>       attributes        = new HashMap<>();
    protected Map<String, Fragment>     fragments         = new SimpleCaseInsensitiveMap<>();
    protected Map<String, HtplTemplate> includedTemplates = new SimpleCaseInsensitiveMap<>();

	public DefaultHtplDocument(HtplEngine engine, HtplResource resource, Node... nodes){
		this(engine, resource, new NodeParent(nodes));
	}
	
	public DefaultHtplDocument(HtplEngine engine, HtplResource resource, List<Node> nodes){
		this(engine, resource, new NodeParent(nodes));
	}
	
	public DefaultHtplDocument(HtplEngine engine, HtplResource resource, List<Node> nodes, boolean autoIncludeJsp){
		this(engine, resource, new NodeParent(nodes),autoIncludeJsp);
	}
	
	public DefaultHtplDocument(HtplEngine engine, HtplResource resource, NodeContainer nodes){
		this(engine,resource,nodes,true);
	}
	
	public DefaultHtplDocument(HtplEngine engine, HtplResource resource, NodeContainer nodes, boolean autoIncludeJsp){
		Args.notNull(engine,"engine");
		Args.notNull(resource,"resource");
		Args.notNull(nodes,"nodes");
		
		this.engine  	  = engine;
		this.resource	  = resource;
		this.nodes        = nodes;
		this.processInterceptors = engine.factory().getBeans(ProcessInterceptor.class).toArray(new ProcessInterceptor[]{});
		
		if(autoIncludeJsp){
			this.autoIncludeJsp();	
		}
	}
	
	@Override
    public HtplResource getResource() {
	    return resource;
    }

	public HtplDocument process(){
		checkLocked();
		
		if(processed){
			throw new IllegalStateException("This document is processed");
		}

        try{
            for(ProcessInterceptor interceptor : processInterceptors) {
                interceptor.preProcessDocument(engine, this);
            }

            this.processProperties(engine);
            this.processNodes(engine);

            for(ProcessInterceptor interceptor : processInterceptors) {
                interceptor.postProcessDocument(engine, this);
            }

        }catch(Throwable e) {
            throw Exceptions.uncheck(e);
        }

        this.processed = true;
		this.lock();
		return this;
	}
	
	public Locale getLocale() {
		return resource.getLocale();
	}

	@Override
    public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		checkLocked();
		this.layout = layout;
	}
	
	@Override
    public String getTitle() {
	    return title;
    }

	@Override
    public void setTitle(String title) {
		checkLocked();
		this.title = title;
    }

	@Override
	public NodeContainer nodes(){
		return nodes;
	}
	
	@Override
    public Map<String, String> getProperties() {
		return properties;
	}
	
    @Override
    public boolean hasProperty(String name) {
	    return properties.containsKey(name);
    }

	@Override
    public String removeProperty(String name) {
		checkLocked();
	    return properties.remove(name);
    }

	@Override
    public String getProperty(String name) {
	    return properties.get(name);
    }

	public void setProperty(String name,String value){
		checkLocked();
		properties.put(name, value);
	}
	
	public void putProperties(Map<String, String> properties){
		checkLocked();
		if(null != properties){
			this.properties.putAll(properties);
		}
	}
	
	@Override
    public Map<String, Object> getAttributes(){
		return attributes;
	}
	
    @Override
    public Object getAttribute(String name) {
	    return attributes.get(name);
    }

	@Override
    public void setAttribute(String name, Object value) {
		checkLocked();
		attributes.put(name, value);
    }

	@Override
    public void removeAttribute(String name) {
		checkLocked();
		attributes.remove(name);
    }
	
	public void putAttributes(Map<String, Object> attributes){
		checkLocked();
		if(null != attributes){
			this.attributes.putAll(attributes);
		}
	}
	
	@Override
    public Map<String, Fragment> getFragments(){
		return fragments;
	}
	
	@Override
    public Fragment getFragment(String name){
		return fragments.get(name);
	}
	
	public void addFragment(String name,Fragment fragment) throws ObjectExistsException{
		checkLocked();
		
		/* TODO : check duplicated fragments
		if(fragments.containsKey(name)){
			throw new ObjectExistsException("Fragment '" + name + "' aleady exists");
		}
		*/
		fragments.put(name, fragment);
	}
	
	@Override
    public void addFragments(Map<String, Fragment> m) {
		checkLocked();
		
		if(null != m){
			fragments.putAll(m);
		}
    }

	public Fragment removeFragment(String name){
		checkLocked();
		
		return fragments.remove(name);
	}
	
	public void putFragments(Map<String, Fragment> fragments){
		checkLocked();
		
		if(null != fragments){
			this.fragments.putAll(fragments);
		}
	}
	
	@Override
    public void addIncludedTemplate(String name, HtplTemplate tpl) {
		checkLocked();
		includedTemplates.put(name, tpl);
    }

	@Override
    public HtplTemplate removeIncludedTemplate(String name) {
		checkLocked();
		return includedTemplates.remove(name);
    }
	
	@Override
    public void putIncludedTemplates(Map<String, HtplTemplate> m) {
		checkLocked();
		if(null != m) {
			this.includedTemplates.putAll(m);
		}
    }

	@Override
    public Map<String, HtplTemplate> getIncludedTemplates() {
	    return includedTemplates;
    }

	@Override
    public Node[] getRequiredHeaders() {
	    return requiredHeaders;
    }

	public void doLock(){
		//lock this document
		this.properties = Collections.unmodifiableMap(this.properties);
		this.attributes = Collections.unmodifiableMap(this.attributes);
		this.fragments  = Collections.unmodifiableMap(this.fragments);
		this.includedTemplates = Collections.unmodifiableMap(this.includedTemplates);
		this.nodes.lock();
	}
	
	/**
	 * Deep clone this document.
	 */
	public DefaultHtplDocument deepClone(){
		DefaultHtplDocument clone = new DefaultHtplDocument(engine, resource, nodes.deepCloneChildNodes(),false);
		
		//TODO : optimize
		
		clone.setLayout(layout);
		clone.setTitle(title);
		clone.putProperties(properties);
		clone.putAttributes(attributes);
		clone.putFragments(fragments);
		clone.putIncludedTemplates(includedTemplates);
		clone.requiredHeaders = this.requiredHeaders;
		clone.processed = this.processed;
		clone.processInterceptors = this.processInterceptors;
		
		return clone;
	}
	
	@Override
    public HtplCompiled compile() {
	    return compile(engine.createCompiler());
    }

	@Override
    public HtplCompiled compile(HtplCompiler compiler) {
		if(!processed){
			throw new IllegalStateException("This document must be processed before compile");
		}
		
		nodes.compile(engine, this, compiler);
		
		return new DefaultHtplCompiled(compiler.compile());
    }
	
	protected void processProperties(HtplEngine engine){
		for(Entry<String,String> prop : properties.entrySet()){
			if(prop.getKey().equalsIgnoreCase(HtplConstants.LAYOUT_PROPERTY)){
				this.layout = prop.getValue();
			}else if(prop.getKey().equalsIgnoreCase(HtplConstants.TITLE_PROPERTY)){
				this.title = prop.getValue();
			}
		}
	}
	
	protected void processNodes(final HtplEngine engine){
		this.nodes.process(engine, this, new ProcessCallback() {
			@Override
			public void preProcess(Node node) {
				try {
	                for(ProcessInterceptor pi : processInterceptors) {
	                	pi.preProcessNode(engine, DefaultHtplDocument.this, node);
	                }
                } catch (Throwable e) {
                	Exceptions.uncheckAndThrow(e, () -> new HtplProcessException("Error executing 'preProcsss'" + e.getMessage(), e) );
                }
			}
			
			@Override
			public Node postProcess(Node node, Node result) {
				try {
	                for(ProcessInterceptor pi : processInterceptors) {
	                	result = pi.postProcessNode(engine, DefaultHtplDocument.this, node, result);
	                }
                } catch (Throwable e) {
                	Exceptions.uncheckAndThrow(e, () -> new HtplProcessException("Error executing 'postProcess'" + e.getMessage(), e) );
                }
                return result;
			}
		});
	}
	
	protected void autoIncludeJsp() {
		if(resource.isServletResource()) {
			String jspFileName = null;

			String fileNameNoExt = Paths.getFileNameWithoutExtension(resource.getFileName());
			
			int lastSlashIndex = fileNameNoExt.lastIndexOf('/');
			if(lastSlashIndex >= 0) {
				jspFileName = fileNameNoExt.substring(0,lastSlashIndex) + "/~" + fileNameNoExt.substring(lastSlashIndex + 1);
			}else{
				jspFileName = "~" + fileNameNoExt;
			}
			
			jspFileName = jspFileName + ".jsp";

			Include include = new Include(jspFileName, null, false);
			nodes.childNodes().add(0, include);
			
			this.requiredHeaders = new Node[]{include};
		}else{
			this.requiredHeaders = new Node[]{};
		}
	}
	
	@Override
    public String toString() {
		return nodes.toString();
    }
}