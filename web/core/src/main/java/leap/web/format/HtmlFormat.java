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
package leap.web.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.http.MimeTypes;
import leap.web.Content;
import leap.web.Contents;
import leap.web.action.ActionContext;

public class HtmlFormat extends AbstractResponseFormat {
	
	public HtmlFormat() {
		super(MimeTypes.TEXT_HTML_TYPE);
	}

    @Override
    public Content getContent(ActionContext context, Object value) throws Exception {
        String html;
        if(null == value){
            html = Strings.EMPTY;
        }else{
            html = Converts.toString(value);
        }
        return Contents.html(html);
    }

}