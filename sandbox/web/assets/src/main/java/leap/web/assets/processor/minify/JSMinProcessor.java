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
package leap.web.assets.processor.minify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import leap.core.BeanFactory;
import leap.core.BeanFactoryAware;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.assets.AssetManager;
import leap.web.assets.AssetMinifier;
import leap.web.assets.AssetMinifyExeption;
import leap.web.assets.AssetFile;
import leap.web.assets.AssetProcessor;

public class JSMinProcessor implements AssetProcessor,BeanFactoryAware {
	
	private static final Log log = LogFactory.get(JSMinProcessor.class);
	
	protected static final AssetMinifier DEFAULT_JS_MINIFIER = new DefaultJsMinifier();

	protected BeanFactory beanFactory;
	
	@Override
    public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
    }

	@Override
	public void process(AssetManager manager, AssetFile file) throws IOException {
		if(!file.isMinified()){
			AssetMinifier minifier = manager.resolveAssetMinifier(file.getAssetType());
			if(null == minifier){
				minifier = DEFAULT_JS_MINIFIER;
			}
			
			log.debug("Minify js by '{}'", minifier.getClass().getSimpleName());
			String minifiedContent = Strings.trim(minifier.minify(manager, file.getSourceContentAsString()));
			file.setMinifiedContent(minifiedContent.getBytes(manager.getConfig().getCharset()));
		}
	}

	protected static final class DefaultJsMinifier implements AssetMinifier {
		@Override
        public String minify(AssetManager manager, String source) throws AssetMinifyExeption {
			ByteArrayInputStream  in  = new ByteArrayInputStream(source.getBytes(manager.getConfig().getCharset()));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			try {
	            new JSMin(in, out).jsmin();
            } catch (Exception e) {
            	throw new AssetMinifyExeption("Minify js error , " + e.getMessage(), e);
            }
			 
			return Strings.newString(out.toByteArray(), manager.getConfig().getCharset().name());
        }
	}
}