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

import java.util.Collection;
import java.util.TreeSet;

import leap.core.BeanFactory;
import leap.core.ioc.PostCreateBean;
import leap.htpl.ast.Attr;
import leap.htpl.ast.Element;
import leap.htpl.processor.AttrProcessor;
import leap.htpl.processor.ElementProcessor;
import leap.htpl.processor.Processor;
import leap.htpl.processor.ProcessorRegistration;
import leap.lang.Comparators;
import leap.lang.Strings;

public class DefaultHtplProcessors implements HtplProcessors,PostCreateBean {

	protected String 			  		prefix;
	protected TreeSet<AttrProcessor>	attrProcessors    = new TreeSet<>(Comparators.ORDERED_COMPARATOR);
	protected TreeSet<ElementProcessor> elementProcessors = new TreeSet<>(Comparators.ORDERED_COMPARATOR);
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
    public String getPrefix() {
	    return prefix;
    }
	
	@Override
    public ElementProcessor lookupElementProcessor(Element e) {
		for(ElementProcessor p : elementProcessors){
			if(p.supports(e)){
				return p;
			}
		}
	    return null;
    }

	@Override
    public AttrProcessor lookupAttrProcessor(Element e,Attr a) {
		for(AttrProcessor p : attrProcessors){
			if(p.supports(e, a)){
				return p;
			}
		}
		return null;
    }

	@Override
    public void postCreate(BeanFactory beanFactory) throws Exception {
		for(ProcessorRegistration tr : beanFactory.getBeans(ProcessorRegistration.class)){
			if(Strings.equals(prefix, tr.getPrefix())){
				addAll(tr.getProcessors());
			}
		}
    }
	
	public void addAll(Collection<Processor> c){
		if(null != c){
			for(Processor p : c){
				add(p);
			}
		}
	}
	
	public void add(Processor p){
		if(p instanceof AttrProcessor){
			addAttrProcessor((AttrProcessor)p);
			return;
		}

		if(p instanceof ElementProcessor){
			addElementProcessor((ElementProcessor)p);
			return;
		}
		
		throw new IllegalStateException("The given processor must be '" + 
										AttrProcessor.class.getSimpleName() + "' or '" + ElementProcessor.class.getSimpleName() + "'");
	}
	
	public void addAttrProcessor(AttrProcessor p){
		attrProcessors.add(p);
	}
	
	public void addElementProcessor(ElementProcessor p){
		elementProcessors.add(p);
	}
}