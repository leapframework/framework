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
package leap.lang.st.stpl;

import java.util.Map;

import leap.lang.params.MapParams;
import leap.lang.params.Params;

/**
 * A simple string template.
 */
public class StplTemplate {
	
	/**
	 * Parse a given text to {@link StplTemplate} object.
	 */
	public static StplTemplate parse(String text) {
		return StplParser.parse(text);
	}
	
	/**
	 * Parse the given template text and render it using the given params.
	 */
	public static String render(String template,Map<String, Object> params){
		return parse(template).render(params);
	}
	
	/**
	 * Parse the given template text and render it using the given params.
	 */
	public static String render(String template,Params params){
		return parse(template).render(params);
	}
	
	private final String     text;
	private final StplNode[] nodes;

	StplTemplate(final String text,final StplNode[] nodes) {
		this.text  = text;
		this.nodes = nodes;
	}

	/**
	 * Returns the template text.
	 */
	public String getText() {
		return text;
	}
	
	protected StplNode[] getNodes() {
		return nodes;
	}

	public String render(Params params) {
		StringBuilder out = new StringBuilder(text.length());

		render(out,params);
		
		return out.toString();
	}
	
	public String render(Map<String, Object> params){
		return render(new MapParams(params));
	}
	
	public void render(StringBuilder out,Params params) {
		for(int i=0;i<nodes.length;i++){
			nodes[i].render(out, params);
		}
	}
	
	public void render(StringBuilder out,Map<String, Object> params) {
		render(out,new MapParams(params));
	}
}
