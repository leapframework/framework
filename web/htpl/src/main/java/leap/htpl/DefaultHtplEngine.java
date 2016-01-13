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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.annotation.N;
import leap.core.ioc.BeanDefinition;
import leap.core.ioc.BeanDefinitionException;
import leap.core.ioc.PostCreateBean;
import leap.core.schedule.Scheduler;
import leap.core.schedule.SchedulerManager;
import leap.web.assets.AssetManager;
import leap.web.assets.AssetSource;
import leap.htpl.ast.Attr;
import leap.htpl.ast.Element;
import leap.htpl.escaping.EscapeType;
import leap.htpl.escaping.HtplEscaper;
import leap.htpl.exception.HtplParseException;
import leap.htpl.processor.AttrProcessor;
import leap.htpl.processor.ElementProcessor;
import leap.htpl.resolver.SimpleHtplResource;
import leap.htpl.resolver.StringHtplResource;
import leap.lang.Args;
import leap.lang.Enums;
import leap.lang.Strings;
import leap.lang.collection.SimpleCaseInsensitiveMap;
import leap.lang.exception.NestedIOException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;

public class DefaultHtplEngine implements HtplEngine, PostCreateBean {
	
	private static final Log log = LogFactory.get(DefaultHtplEngine.class);
	
    protected @Inject @M BeanFactory           factory;
    protected @Inject @M HtplConfig            config;
    protected @Inject @M HtplParser            parser;
    protected @Inject @M HtplExpressionManager expressionManager;
    protected @Inject @M HtplTemplateSource    templateSource;
    protected @Inject @M SchedulerManager      schedulerManager;

    protected @Inject @N AssetSource           assetSource;
    protected @Inject @N AssetManager          assetManager;
    
    protected Locale                           defaultLocale;
    protected Scheduler                        reloadScheduler;
    protected int                              reloadInterval      = HtplConstants.DEFAULT_RELOAD_INTERVAL;
    protected List<ReloadableHtplTemplate>     reloadableTemplates = new CopyOnWriteArrayList<ReloadableHtplTemplate>();
    protected Map<String, HtplProcessors>      processorsMap       = new SimpleCaseInsensitiveMap<>();
    protected Map<EscapeType, HtplEscaper>     escapersMap         = new HashMap<EscapeType, HtplEscaper>();
    protected Map<String, HtplTemplate>        managedTemplates    = new ConcurrentHashMap<String, HtplTemplate>();
	
	private boolean reloadScheduled = false;
	
	@Override
    public BeanFactory factory() {
	    return factory;
    }
	
	public HtplConfig getConfig() {
		return config;
	}
	
	public void setConfig(HtplConfig config) {
		this.config = config;
	}

	@Override
	public HtplExpressionManager getExpressionManager() {
		return expressionManager;
	}
	
	public void setExpressionManager(HtplExpressionManager expressionParser) {
		this.expressionManager = expressionParser;
	}
	
	@Override
    public HtplEscaper getEscaper(EscapeType mode) {
		Args.notNull(mode,"mode");
	    return escapersMap.get(mode);
    }

	@Override
	public AssetSource getAssetSource() {
		return assetSource;
	}
	
	@Override
    public AssetManager getAssetManager() {
        return assetManager;
    }
	
	public void setParser(HtplParser documentParser) {
		this.parser = documentParser;
	}

	public void setReloadScheduler(Scheduler reloadScheduler) {
		this.reloadScheduler = reloadScheduler;
	}
	
	public void setReloadInterval(int reloadInterval) {
		this.reloadInterval = reloadInterval;
	}

	public HtplTemplateSource getTemplateSource() {
		return templateSource;
	}

	public void setTemplateSource(HtplTemplateSource templateSource) {
		this.templateSource = templateSource;
	}

	@Override
    public HtplDocument parseDocument(HtplResource resource) throws HtplParseException, NestedIOException {
	    return parser.parseDocument(this, resource);
    }

	@Override
    public HtplCompiler createCompiler() {
	    return new DefaultHtplCompiler(this);
    }
	
	@Override
    public boolean resolveElementProcessor(Element e) {
		String prefix = Strings.trim(e.getPrefix());
		String name   = e.getLocalName();

		if(Strings.isEmpty(prefix)){
			prefix = Strings.substringBefore(name, "-");
			if(!Strings.isEmpty(prefix)){
				name = name.substring(prefix.length() + 1);
			}
		}
		
		HtplProcessors processors = processorsMap.get(prefix);
		if(null == processors){
			return false;
		}
		
		String originalPrefix    = e.getPrefix();
		String originalLocalName = e.getLocalName();
		
		//Set prefix and local name to prefix and name.
		e.setPrefix(prefix);
		e.setLocalName(name);

		ElementProcessor processor = processors.lookupElementProcessor(e);
		if(null != processor){
			e.setProcessor(processor);
			return true;
		}else{
			//No processor, restore original prefix and local name
			e.setPrefix(originalPrefix);
			e.setLocalName(originalLocalName);
			return false;
		}
	}

	@Override
    public boolean resolveAttrProcessor(Element e, Attr a) {
		String prefix = Strings.trim(a.getPrefix());
		String name   = a.getLocalName();
		
		if(Strings.isEmpty(prefix)){
			/*
			if(name.startsWith(HtplConstants.HTML5_DATA_PREFIX)){
				name = name.substring(HtplConstants.HTML5_DATA_PREFIX.length());
			}
			*/

			prefix = Strings.substringBefore(name, "-");
			if(!Strings.isEmpty(prefix)){
				name = name.substring(prefix.length() + 1);
			}
		}
		
		HtplProcessors processors = processorsMap.get(prefix);
		if(null == processors){
			return false;
		}

		String originalPrefix    = a.getPrefix();
		String originalLocalName = a.getLocalName();
		
		//Set prefix and local name to prefix and name.
		a.setPrefix(prefix);
		a.setLocalName(name);

		AttrProcessor processor = processors.lookupAttrProcessor(e, a);
		if(null != processor){
			a.setProcessor(processor);
			return true;
		}else{
			//No processor, restore original prefix and local name
			a.setPrefix(originalPrefix);
			a.setLocalName(originalLocalName);
			return false;
		}
    }

	@Override
    public HtplTemplate resolveTemplate(String templateName) throws HtplParseException, NestedIOException {
	    return resolveTemplate(templateName,(Locale)null);
    }

	@Override
    public HtplTemplate resolveTemplate(String templateName, Locale locale) throws HtplParseException, NestedIOException {
		Args.notEmpty(templateName,"template name");
		try {
			HtplTemplate template = managedTemplates.get(getTemplateKey(templateName, locale));
			if(null != template){
				return template;
			}else{
				return templateSource.getTemplate(templateName, locale);	
			}
		} catch (HtplException e){
			throw (HtplException)e;
        } catch (Throwable e) {
        	throw new HtplException("Error resolving template '" + templateName + "', " + e.getMessage(), e);
        }
    }
	
	@Override
    public HtplTemplate resolveTemplate(HtplResource current, String templateName, Locale locale) throws HtplParseException, NestedIOException {
		if(null != current){
			HtplResource lr = null;
			
			if(Paths.isExplicitRelativePath(templateName)){
				lr = current.tryGetRelative(templateName, locale, true);
			}else if(templateName.startsWith("/")){
				lr = current.tryGetAbsolute(templateName, locale, true);
			}else {
				lr = current.tryGetRelative(templateName, locale, true);
				if(null == lr){
					lr = current.tryGetAbsolute(templateName, locale, true);
				}
			}
			
			if(null != lr){
				return createTemplate(lr);
			}
		}
		
		return resolveTemplate(templateName, locale);
    }

	@Override
    public HtplTemplate createTemplate(HtplResource resource) {
		return createTemplate(resource, null);
    }
	
	public HtplTemplate createTemplate(HtplResource resource,Locale locale){
		Args.notNull(resource,"resource");
		
		if(resource.reloadable()){
			ReloadableHtplTemplate tpl = null;
			File file = resource.getFile();
			
			if(null != file) {
				for(ReloadableHtplTemplate rt : reloadableTemplates) {
					File f = rt.getResource().getFile();
					if(null != f && f.getAbsolutePath().equals(file.getAbsolutePath())) {
						tpl = rt;
						break;
					}
				}
			}
			
			if(null == tpl) {
				tpl = new ReloadableHtplTemplate(this, resource);
				reloadableTemplates.add(tpl);
			}
			
			if(!reloadScheduled){
				synchronized (this) {
					if(!reloadScheduled){
					    if(reloadScheduler == null) {
					        reloadScheduler = schedulerManager.newFixedThreadPoolScheduler("htpl-reload");
					    }
						//Start reload task
						reloadScheduler.scheduleAtFixedRate(new ReloadTask(), reloadInterval);
					}
                }
			}
			
			return tpl;
		}else{
			return new DefaultHtplTemplate(this, resource, locale);	
		}
	}
	
	@Override
    public HtplTemplate createTemplate(Resource resource) {
		Args.notNull(resource,"resource");
		return createTemplate(new SimpleHtplResource(resource,null));
    }
	
	@Override
    public HtplTemplate createTemplate(Resource resource, Locale locale) throws HtplParseException, NestedIOException {
		Args.notNull(resource,"resource");
	    return createTemplate(new SimpleHtplResource(resource, locale));
    }
	
	@Override
    public HtplTemplate createTemplate(String html) {
		Args.notEmpty("html",html);
		return createTemplate(new StringHtplResource(html));
    }

	@Override
    public HtplTemplate createTemplate(String html, Locale locale) throws HtplParseException {
		Args.notEmpty("html",html);
		return createTemplate(new StringHtplResource(html,locale));
    }

	@Override
    public void addTemplate(String name, HtplTemplate template) {
		Args.notEmpty(name,"name");
		Args.notNull(template,"template");
		managedTemplates.put(getTemplateKey(name, template.getLocale()), template);
    }

	@Override
    public HtplTemplate removeTempalte(String name, Locale locale) {
		Args.notEmpty(name,"name");
		return managedTemplates.remove(getTemplateKey(name, locale));
    }
	
	protected String getTemplateKey(String name, Locale locale) {
		if(null == locale) {
			locale = defaultLocale;
		}
		return name + "_" + locale;
	}

	@Override
    public void postCreate(BeanFactory beanFactory) throws Exception {
		this.factory	   = beanFactory;
		this.defaultLocale = beanFactory.getAppConfig().getDefaultLocale();
		
		//Register processors
		for(Entry<HtplProcessors, BeanDefinition> bean : beanFactory.getBeansWithDefinition(HtplProcessors.class).entrySet()){
			addProcessors(bean);
		}
		
		//Register escaper
		for(Entry<String, HtplEscaper> entry : beanFactory.getNamedBeans(HtplEscaper.class).entrySet()){
			EscapeType mode = Enums.nameOf(EscapeType.class, entry.getKey(), true);
			if(null == mode) {
				throw new BeanDefinitionException("Invalid escape mode '" + entry.getKey() + "'");
			}
			escapersMap.put(mode, entry.getValue());
		}
    }
	
	protected void addProcessors(Entry<HtplProcessors, BeanDefinition> bean){
		HtplProcessors processors = bean.getKey();
		
		String prefix = Strings.trim(processors.getPrefix());
		
		if(processorsMap.containsKey(prefix)){
			throw new HtplException("Processors with prefix '" + prefix + "' aleady registered, check the bean '" + bean.getValue() + "'");
		}
		
		processorsMap.put(prefix, processors);
	}

	protected class ReloadTask implements Runnable {
		@Override
        public void run() {
	        if(!reloadableTemplates.isEmpty()){
	        	for(ReloadableHtplTemplate tpl : reloadableTemplates){
	        		if(tpl.reload()){
	        			log.debug("Template '" + tpl + "' was reloaded");
	        		}
	        	}
	        }
        }
	}
}