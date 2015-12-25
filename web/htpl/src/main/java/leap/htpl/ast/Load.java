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
import java.io.Reader;
import java.util.Collections;
import java.util.Map;

import leap.htpl.HtplCompiler;
import leap.htpl.HtplContext;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.HtplRenderable;
import leap.htpl.HtplResource;
import leap.htpl.HtplTemplate;
import leap.htpl.HtplWriter;
import leap.htpl.exception.HtplProcessException;
import leap.lang.Strings;
import leap.lang.json.JSON;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.yaml.YAML;

/**
 * Loading data file
 */
public class Load extends Node implements HtplRenderable {

	private static final Log log = LogFactory.get(Load.class);
	
	private String 			    fileName;
	private String 			    variable;
	private Map<String, String> params;

	private Object data;
	
	public Load(String fileName,String variable,Map<String, String> params) {
		this.fileName = fileName;
		this.variable = variable;
		this.params   = null == params ? Collections.emptyMap() : Collections.unmodifiableMap(params);
	}
	
	public String getFileName() {
		return fileName;
	}

	@Override
	protected Node doDeepClone(Node parent) {
		Load clone = new Load(fileName,variable,params);
		
		clone.data = this.data;
		
		return clone;
	}

	@Override
	protected void doWriteTemplate(Appendable out) throws IOException {
		out.append("<!--#load \"").append(fileName).append("\"-->");
	}
	
	@Override
    protected Node doProcess(HtplEngine engine,HtplDocument doc, ProcessCallback callback) {
		//resolve resource.
		HtplResource resource = doc.getResource().tryGetResource(fileName, doc.getLocale());
		
		if(null == resource) {
			throw new HtplProcessException("Cannot load data file '" + fileName + "' in '" + doc.getResource() + "', file not exists");
		}
		
		if(Strings.isEmpty(variable)){
			variable = Paths.getFileNameWithoutExtension(Paths.getFileName(fileName));	
		}
		
		//load data.
		if(fileName.endsWith(".json")){
			try {
	            try(Reader r = resource.getReader()) {
	            	data = JSON.decode(r);
	            }
            } catch (Exception e) {
            	throw new HtplProcessException("Error loading json data file '" + fileName + "', " + e.getMessage(), e);
            }
		}else if(fileName.endsWith(".yaml")){
			try {
	            try(Reader r = resource.getReader()) {
	            	data = YAML.parse(r).raw();
	            }
            } catch (Exception e) {
            	throw new HtplProcessException("Error loading yaml data file '" + fileName + "', " + e.getMessage(), e);
            }
		}else{
			throw new HtplProcessException("Cannot load data file '" + fileName + "' in '" + doc.getResource() + "', file format not supported");
		}
		
		return this;
	}
	
	@Override
    public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		compiler.renderable(this);
    }
	
	@Override
    public void render(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException {
		log.debug("Set data as variable : {}", variable);
		context.setLocalVariable(variable, data);
	}
}