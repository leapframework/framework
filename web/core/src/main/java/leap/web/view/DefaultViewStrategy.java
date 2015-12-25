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
package leap.web.view;

import leap.lang.Arrays2;
import leap.lang.Strings;

public class DefaultViewStrategy implements ViewStrategy {

	@Override
    public String[] getCandidateViewPaths(String viewPath) {
		if(viewPath.indexOf('_') < 0){
			return Arrays2.EMPTY_STRING_ARRAY;
		}
		
		int    lastSlashIndex = viewPath.lastIndexOf('/');
		String prefixPart;
		String viewName;
		
		if(lastSlashIndex >= 0){
			prefixPart = viewPath.substring(0,lastSlashIndex + 1);
			viewName   = viewPath.substring(lastSlashIndex + 1);
		}else{
			prefixPart = "";
			viewName   = viewPath;
		}
		
		String[] parts = Strings.split(viewName,"_");
		
		String candidateViewName = Strings.lowerCamel(parts);
		if(!candidateViewName.equals(viewName)){
			return new String[]{prefixPart + candidateViewName};
		}else{
			return Arrays2.EMPTY_STRING_ARRAY;
		}
    }

}
