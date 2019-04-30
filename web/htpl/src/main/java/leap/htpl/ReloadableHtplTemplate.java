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

import leap.htpl.resolver.StringHtplResource;
import leap.lang.Args;
import leap.lang.Exceptions;
import leap.lang.Out;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;

public class ReloadableHtplTemplate extends AbstractHtplTemplate implements HtplTemplate {
	private static final Log log = LogFactory.get(ReloadableHtplTemplate.class);

    protected final HtplEngine           engine;
    protected final HtplTemplateListener reloadListener;

    protected HtplResource resource;
	protected HtplTemplate template;
	
	public ReloadableHtplTemplate(HtplEngine engine,HtplResource resource, String templateName) {
		Args.notNull(resource,"resource");
		Args.assertTrue(resource.reloadable(),"The given resource must be reloadable");
		this.engine   = engine;
		this.resource = resource;
        this.name = templateName;
		this.reloadListener = template -> reloadTemplate();
		this.template = createWrappedTemplate(engine, resource);
	}
	
	@Override
    public boolean reloadable() {
	    return true;
    }
	
	@Override
    public HtplResource getResource() {
	    return resource;
    }

	@Override
	public Object getSource() {
		return resource.getSource();
	}
	
	@Override
    public Locale getLocale() {
	    return template.getLocale();
    }

	@Override
	public HtplEngine getEngine() {
		return engine;
	}

	@Override
    public HtplDocument getDocument() {
	    return template.getDocument();
    }

	@Override
    public HtplPage createPage() {
	    return template.createPage();
    }

	@Override
	public void render(HtplContext context, Writer writer) {
		template.render(context, writer);
	}
	
	@Override
    public void render(HtplContext context, HtplWriter writer) {
		template.render(context, writer);
    }

	@Override
    public void render(HtplTemplate parent, HtplContext context, HtplWriter writer) {
		template.render(parent, context, writer);
	}

	public boolean reload(){
		Out<Reader> out = new Out<Reader>();
		
		try {
	        if(resource.reload(out)){
	        	try(Reader r = out.getValue() ){
		        	//TODO : handle null reader, resource not exits ?
		        	if(null != r){
			        	onTemplateReloaded(createWrappedTemplate(engine, resource, r));
			        	return true;
		        	}
	        	}
	        }
        } catch (Exception e) {
        	log.warn("Error reloading resource '" + resource.getSource() + "' : " + e.getMessage(), e);
        }
		
		return false;
	}
	
	protected void reloadTemplate() {
		onTemplateReloaded(createWrappedTemplate(engine, resource));
	}
	
	protected void onTemplateReloaded(HtplTemplate reloadedTemplate) {
		this.template = reloadedTemplate;
		notifyTemplateReloaded();
	}
	
	protected HtplTemplate createWrappedTemplate(HtplEngine engine,HtplResource resource){
		try {
	        return createWrappedTemplate(engine, resource, resource.getReader());
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }
	}
	
	protected HtplTemplate createWrappedTemplate(HtplEngine engine,HtplResource resource, Reader r){
		try{
			r = resource.getReader();
			
			HtplTemplate template = engine.createTemplate(new WrappedHtplResource(resource,IO.readString(r)), name);
			
			for(HtplTemplate tpl : template.getDocument().getIncludedTemplates().values()) {
				if(!tpl.containsListener(reloadListener)){
					tpl.addListener(reloadListener);
				}
			}
			
			return template;
			
		}catch(IOException e){
			throw Exceptions.wrap(e);
		}finally{
			IO.close(r);
		}
	}

	@Override
    public String toString() {
	    return template.toString();
    }
	
	protected static class WrappedHtplResource extends StringHtplResource {

		private final HtplResource wrapped;
		
		public WrappedHtplResource(HtplResource wrapped, String string) {
	        super(wrapped.getSource(), string);
	        this.wrapped = wrapped;
        }
		
		@Override
        public String getFileName() {
			return wrapped.getFileName();
		}

		@Override
        public HtplResource tryGetRelative(String relativePath, Locale locale) {
			return wrapped.tryGetRelative(relativePath, locale);
		}

		@Override
        public HtplResource tryGetAbsolute(String absolutePath, Locale locale) {
			return wrapped.tryGetAbsolute(absolutePath, locale);
		}
		@Override
        public HtplResource tryGetResource(String path, Locale locale) {
	        return wrapped.tryGetResource(path, locale);
        }

		@Override
        public HtplResource tryGetResource(String path, Locale locale, boolean ensureTemplate) {
	        return wrapped.tryGetResource(path, locale, ensureTemplate);
        }

		@Override
        public HtplResource tryGetRelative(String relativePath, Locale locale, boolean ensureTemplate) {
	        return wrapped.tryGetRelative(relativePath, locale, ensureTemplate);
        }

		@Override
        public HtplResource tryGetAbsolute(String absolutePath, Locale locale, boolean ensureTempalte) {
	        return wrapped.tryGetAbsolute(absolutePath, locale, ensureTempalte);
        }

		@Override
        public Resource getResource() {
			return wrapped.getResource();
		}
	}
}