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
package leap.htpl.processor.html;

import java.util.HashSet;
import java.util.Set;

import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Attr;
import leap.htpl.ast.Element;
import leap.htpl.ast.Node;
import leap.htpl.expression.UrlExpression;
import leap.htpl.processor.AbstractAttrProcessor;
import leap.lang.Strings;
import leap.lang.expression.Expression;
import leap.lang.path.Paths;

public class UrlAttrProcessor extends AbstractAttrProcessor {
	
	private static final Set<String> urlAttrs = new HashSet<>();
	
	static {
		//see https://developer.mozilla.org/en-US/docs/Web/HTML/Attributes
		
		//href : <a>, <area>, <base>, <link>
		urlAttrs.add("a.href");
		urlAttrs.add("base.href");
		urlAttrs.add("link.href");
		
		//src : <audio>, <embed>, <iframe>, <img>, <input>, <script>, <source>, <track>, <video>
		urlAttrs.add("script.src");
		urlAttrs.add("img.src");
		urlAttrs.add("iframe.src");
		urlAttrs.add("audio.src");
		urlAttrs.add("video.src");
		
		//action : <form>
		urlAttrs.add("form.action");
	}
	
	@Override
    public boolean required() {
		return false;
	}

	@Override
	public boolean supports(Element e, Attr a) {
		String qAttrName =  (e.getQualifiedName() + "." + a.getLocalName()).toLowerCase();
		return urlAttrs.contains(qAttrName);
	}

	@Override
	public Node processStartElement(HtplEngine engine, HtplDocument doc, Element e, Attr a) throws Throwable {
		String url = Strings.trim(a.getString());

		//Protocol relative url
		if(Strings.startsWith(url, "//")){
			return e;
		}
		
		//Context relative url
		if(Strings.startsWith(url, "/")){
			a.setValue("@{/}" + url);
			return e;
		}
		
		//Context relative url
		if(Strings.startsWith(url, "~")){
			a.setValue("@{~}" + Paths.prefixWithSlash(url.substring(1)));
			return e;
		}
		
		//Server relative url
		if(Strings.startsWith(url, "^")){
			a.setValue("@{^}" + Paths.prefixWithoutSlash(url.substring(1)));
			return e;
		}
		
		if(Strings.startsWith(url, "@{")) {
			return e;
		}
		
		Expression expression = engine.getExpressionManager().tryParseAttributeExpression(engine, url);
		if(null != expression) {
			a.setValue(new UrlExpression(engine, url, expression));	
		}
		
		return e;
	}
}
