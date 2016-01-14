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
package leap.htpl.ast;

import java.io.IOException;
import java.util.Enumeration;

import leap.htpl.HtplCompiler;
import leap.htpl.HtplContext;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.HtplRenderable;
import leap.htpl.HtplResource;
import leap.htpl.HtplTemplate;
import leap.htpl.HtplWriter;
import leap.htpl.exception.HtplCompileException;
import leap.htpl.exception.HtplParseException;
import leap.htpl.exception.HtplRenderException;
import leap.lang.Strings;
import leap.lang.expression.Expression;
import leap.lang.servlet.ServletResource;

public class Include extends Node implements HtplRenderable {

	private final String     templateName;
	private final String     fragmentName;
	private final boolean    required;

    private Expression      expression;
	private HtplTemplate    template; //included template
	private Fragment 	    fragment;
	private ServletResource resource; //included servlet resource
	
	public Include(String templateName) {
		this(templateName,null,true);
	}

	public Include(String templateName,String fragmentName) {
		this(templateName,fragmentName,true);
	}
	
	public Include(String templateName,String fragmentName, boolean required) {
		this.templateName = templateName;
		this.fragmentName = fragmentName;
		this.required     = required;
	}

	public String getTemplateName() {
		return templateName;
	}
	
	public String getFragmentName() {
		return fragmentName;
	}

	protected Expression tryParseTemplateExpression() {
		return null;
	}

	@Override
	protected Node doDeepClone(Node parent) {
		Include clone = new Include(templateName,fragmentName,required);
		clone.template = template;
		clone.expression = expression;
		clone.fragment = null == fragment ? null : fragment.deepClone(clone);
		clone.resource = resource;
		return clone;
	}

	@Override
	protected void doWriteTemplate(Appendable out) throws IOException {
		out.append("<!--#include \"").append(templateName).append("\"-->");
	}
	
	@Override
    protected Node doProcess(HtplEngine engine,HtplDocument doc, ProcessCallback callback) {
        expression = engine.getExpressionManager().tryParseCompositeExpression(engine, templateName);

        if(null == expression){
            template = engine.resolveTemplate(doc.getResource(), templateName, doc.getLocale());
            if(null == template){
                processTemplateNotFound(engine, doc);
                return this;
            }

            if(!Strings.isEmpty(fragmentName)){
                fragment = template.getDocument().getFragment(fragmentName);
                if(null == fragment && required){
                    throw new HtplCompileException("No fragment named '" + fragmentName + "' in the included template '" + templateName + "'");
                }
                fragment = fragment.deepClone(this);
            }

            doc.addIncludedTemplate(templateName, template);
        }

        return this;
	}
	
	@Override
    public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		if(null != fragment){
			fragment.compileSelf(engine, doc);
		}
		compiler.renderable(this);
    }
	
	@Override
    public void render(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException {
        if(null != expression) {

            String result = context.evalString(expression);
            if(Strings.isEmpty(result)) {
                if(required) {
                    throw new HtplRenderException("The included template expression '" + this.templateName + "' returns empty string");
                }
                return;
            }

            String templateName = result;
            String fragmentName = null;
            int index = templateName.indexOf('#');
            if(index > 0) {
                fragmentName = result.substring(index+1);
                templateName = result.substring(0, index);
            }

            HtplTemplate template = context.getEngine().resolveTemplate(tpl.getDocument().getResource(), templateName, context.getLocale());
            if(null == template) {

                if(required) {
                    throw new HtplRenderException("The included template '" + templateName + "' not found");
                }

                return;
            }

            Fragment fragment = null;
            if(null != fragmentName) {
                fragment = template.getDocument().getFragment(fragmentName);
                if(null == fragment) {
                    if(required) {
                        throw new HtplRenderException("The included fragment '" + result + "' not found");
                    }
                    return;
                }
            }

            if(context.isDebug()) {
                writer.append("<!--#include \"" + result + "\"-->\n");
            }

            if(null == fragment){
                template.render(tpl, context, writer);
            }else{
                fragment.render(tpl, context, writer);
            }

            if(context.isDebug()) {
                writer.append("\n<!--#endinclude-->");
            }

        }else if(null != template) {
			if(context.isDebug()) {
				writer.append("<!--#include \"" + templateName + "\"-->\n");
			}
			
			if(null == fragment){
				//render the include templte
				template.render(tpl, context, writer);
			}else{
				//render the include fragment
				fragment.render(tpl, context, writer);
			}
			
			if(context.isDebug()) {
				writer.append("\n<!--#endinclude-->");
			}
			
		}else if(null != resource){
			if(context.isDebug()) {
				writer.append("<!--#include \"" + resource.getPathWithinContext() + "\"-->\n");
			}
			includeServletResource(tpl, context, writer, resource);
			if(context.isDebug()) {
				writer.append("\n<!--#endinclude-->");
			}
		}
    }
	
	protected void processTemplateNotFound(HtplEngine engine, HtplDocument doc) {
		if(doc.getResource().isServletResource()) {
			if(processServletResourceInclude(engine,doc)){
				return;
			}
		}
		if(required){
			throw new HtplParseException("The include template '" + templateName + "' can not be resolved");	
		}
	}
	
	protected boolean processServletResourceInclude(HtplEngine engine, HtplDocument doc) {
		HtplResource jsp = doc.getResource().tryGetResource(templateName, doc.getLocale());
		if(jsp == null){
			return false;
		}
		this.resource = jsp.getServletResource();
		return true;
	}
	
	protected void includeServletResource(HtplTemplate tpl, HtplContext context, HtplWriter writer, ServletResource sr) throws IOException {
		leap.web.Request r = null;
		
		if(context.getRequest() instanceof leap.web.Request) {
			r = (leap.web.Request)context.getRequest();
		}else{
			r = leap.web.Request.current();
		}

		javax.servlet.http.HttpServletRequest  req  = r.getServletRequest();
		javax.servlet.http.HttpServletResponse resp = r.response().getServletResponse();
		
		try {
	        req.getRequestDispatcher(sr.getPathWithinContext()).include(req, resp);

	        //TODO : optimize
	        Enumeration<String> vars = req.getAttributeNames();
	        while(vars.hasMoreElements()) {
	        	String var = vars.nextElement();
	        	context.setLocalVariable(var, req.getAttribute(var));
	        }
        } catch (javax.servlet.ServletException e) {
        	throw new HtplRenderException("Error including resource '" + resource.getPathWithinContext() + "', " + e.getMessage(), e);
        }
	}
}