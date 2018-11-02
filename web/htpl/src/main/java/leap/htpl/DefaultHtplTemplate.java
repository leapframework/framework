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

import leap.htpl.ast.Fragment;
import leap.htpl.ast.Node;
import leap.htpl.exception.HtplCompileException;
import leap.htpl.exception.HtplParseException;
import leap.htpl.exception.HtplRenderException;
import leap.lang.Args;
import leap.lang.Strings;

import java.io.Writer;
import java.util.Locale;
import java.util.Map.Entry;

public class DefaultHtplTemplate extends AbstractHtplTemplate implements HtplTemplate {
	
	protected final HtplEngine	 engine;
	protected final HtplResource resource;
	protected final Locale		 locale;

	protected HtplDocument documentWithoutLayout;
	protected HtplCompiled compiledWithoutLayout;
	
	protected TemplateWithLayout templateWithLayout;
	
	public DefaultHtplTemplate(HtplEngine engine,HtplResource resource,Locale locale){
		Args.notNull(engine,"engine");
		Args.notNull(resource,"resource");
		this.engine   = engine;
		this.locale   = locale;
		this.resource = resource;
		parseAndCompileDocument();
	}
	
	@Override
    public boolean reloadable() {
	    return false;
    }

	@Override
    public HtplEngine getEngine() {
	    return engine;
    }
	
	@Override
    public HtplResource getResource() {
	    return resource;
    }

	@Override
    public Locale getLocale() {
	    return locale;
    }

	@Override
    public Object getSource() {
	    return resource.getSource();
    }
	
	@Override
    public HtplDocument getDocument() {
	    return documentWithoutLayout;
    }
	
	@Override
    public HtplPage createPage() {
		DefaultHtplPage page = new DefaultHtplPage(this);

		if(null != templateWithLayout){
			page.putProperties(templateWithLayout.document.getProperties());
		}else{
			page.putProperties(documentWithoutLayout.getProperties());
		}
		
	    return page;
    }

	@Override
    public void render(HtplContext context, Writer writer) {
		render(context,createHtplWriter(writer));
    }
	
	@Override
    public void render(HtplContext context, HtplWriter writer) {
		try {
            if(!preRenderTemplate(context, writer)) {
                return;
            }

			if(context.isDebug() && null != resource) {
				String path = resource.isResource() ? resource.getServletResource().getPath() : resource.getFileName();
				if(null != path) {
					writer.append("<!--file: " + path + "-->\n");
				}
			}
			if(null != templateWithLayout && context.isRenderLayout()){
				templateWithLayout.compiled.render(this, context, writer);
			}else{
				compiledWithoutLayout.render(this, context, writer);
			}

            postRenderTemplate(context, writer);

        } catch (HtplException e) {
        	throw e;
        } catch (Throwable e){
        	throw new HtplRenderException("Error render template '" + getSource() + "' : " + e.getMessage(),e);
        }
    }

	@Override
    public void render(HtplTemplate parent, HtplContext context, HtplWriter writer) {
		try {
			if(null != templateWithLayout && context.isRenderLayout()){
				templateWithLayout.compiled.render(parent, context, writer);
			}else{
				compiledWithoutLayout.render(parent, context, writer);
			}
        } catch (HtplException e) {
        	throw e;
        } catch (Throwable e){
        	throw new HtplRenderException("Error render the included template '" + getSource() + 
        								  "' in parent template '" + parent.getSource() + "' : " + e.getMessage(),e);
        }
    }

	protected void parseAndCompileDocument(){
		try{
			//Parse and compile document with out layout.
			this.documentWithoutLayout = engine.parseDocument(resource).process();
			this.compiledWithoutLayout = documentWithoutLayout.compile();
			
			//Resolve and compiles document with default layout
			resolveAndCompileLayoutDocument();
			
		}catch(Throwable e){
			throw new HtplParseException("Template '" + resource.getSource() + "' parse error : " + e.getMessage(), e);
		}
	}
	
	protected void resolveAndCompileLayoutDocument(){
		String layoutName = documentWithoutLayout.getLayout();
		if(!Strings.isEmpty(layoutName)){
			HtplTemplate layoutTemplate = engine.resolveTemplate(resource, layoutName, locale);
			
			if(null == layoutTemplate){
				throw new HtplCompileException("Layout '" + layoutName + "' can not be resolved");
			}
			
			//add listener to handle reloading of layout template.
			layoutTemplate.addListener(template -> compileLayoutDocument(template));

			//compile it now
			compileLayoutDocument(layoutTemplate);
		}
	}
	
	protected void compileLayoutDocument(HtplTemplate layoutTemplate) {
		HtplDocument layoutDocument = layoutTemplate.getDocument().deepClone();
		
		//Merge properties
		layoutDocument.putProperties(getDocument().getProperties());
		
		//Merge fragments
		for(Entry<String, Fragment> entry : this.documentWithoutLayout.getFragments().entrySet()) {
			layoutDocument.addFragment(entry.getKey(), entry.getValue().deepClone(null));
		}
		
		//Merge required headers
		Node[] requiredHeaders = this.documentWithoutLayout.getRequiredHeaders();
		for(int i=requiredHeaders.length-1;i>=0;i--) {
			layoutDocument.nodes().childNodes().add(0,requiredHeaders[i].deepClone(null));
		}
		
		//Replate placeholders
		//replaceLayoutPlaceholders(layoutDocument, layoutDocument.nodes());
		
		layoutDocument.lock();
		
		TemplateWithLayout twl = new TemplateWithLayout();
		twl.document = layoutDocument;
		twl.compiled = layoutDocument.compile();

		this.templateWithLayout = twl;
	}
	
	protected HtplWriter createHtplWriter(Writer out){
		return new DefaultHtplWriter(engine,out);
	}
	
	/*
	protected void replaceLayoutPlaceholders(HtplDocument layoutDocument, NodeContainer parent){
		replaceLayoutPlaceholders(layoutDocument, parent, new AtomicBoolean(false));
	}
	
	protected void replaceLayoutPlaceholders(HtplDocument layoutDocument, NodeContainer parent, AtomicBoolean requiredHeadersAdded){
		for(Node node : parent.childNodes()){
			if(node instanceof RenderFragment){
				RenderFragment render = (RenderFragment)node;
				Fragment fragment = documentWithoutLayout.getFragment(render.getName());
				if(null == fragment && render.isRequired()){
					throw new HtplCompileException("The fragment '" + render.getName() + "' must be defined in template '" + getSource() + "'");
				}
				
				if(null != fragment) {
					List<Node> fragmentNodes = fragment.deepCloneChildNodes();
					
					if(!requiredHeadersAdded.get()) {
						
						Node[] nodes = getDocument().getRequiredHeaders();
						if(null != nodes) {
							for(int i=nodes.length-1;i>=0;i--) {
								fragmentNodes.add(0, nodes[i].deepClone(null));
							}
						}
						
						requiredHeadersAdded.set(true);
					}
					
					render.setChildNodes(fragmentNodes);	
				}else{
					parent.removeChildNode(node);
				}
				
				continue;
			}
			
			if(node instanceof NodeContainer){
				replaceLayoutPlaceholders(layoutDocument, (NodeContainer)node, requiredHeadersAdded);
			}
		}
	}
	*/
	
	@Override
    public String toString() {
		Object source = getSource();
		
		if(null == source){
			return super.toString();
		}

		return source.toString();
	}
	
	protected final class TemplateWithLayout {
		protected HtplDocument document;
		protected HtplCompiled compiled;
	}
}