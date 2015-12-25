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
package leap.core.web;

import java.util.LinkedHashSet;
import java.util.Set;

import leap.lang.Strings;
import leap.lang.path.AntPathMatcher;
import leap.lang.path.PathMatcher;
import leap.lang.tostring.ToStringBuilder;

public class DefaultRequestMatcher implements RequestMatcher{
	
	protected boolean     ignoreCase = true;
	protected String[]    prefixes;
	protected String[]    suffixes;
	protected String[]    patterns;
	protected PathMatcher patternMatcher = new AntPathMatcher();
	
	public DefaultRequestMatcher() {

	}

	public DefaultRequestMatcher(boolean ignoreCase){
		this.ignoreCase = ignoreCase;
	}
	
	public void setPrefixes(String prefixes){
		this.parsePrefixes(prefixes);
	}

	public void setSuffixes(String suffixes){
		this.parseSuffixes(suffixes);
	}
	
	public void setPatterns(String patterns){
		this.parsePatterns(patterns);
	}
	
	@Override
	public boolean matches(RequestBase request) {
		String path = request.getPath(ignoreCase);
		
		if(null != prefixes){
			if(matchPrefixes(path)){
				return true;
			}
		}
		
		if(null != suffixes){
			if(matchSuffixes(path)){
				return true;
			}
		}
		
		if(null != patterns){
			if(matchPatterns(path)){
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean matchPrefixes(String path){
		for(int i=0;i<prefixes.length;i++){
			if(path.startsWith(prefixes[i])){
				return true;
			}
		}
		return false;
	}
	
	protected boolean matchSuffixes(String path){
		for(int i=0;i<suffixes.length;i++){
			if(path.endsWith(suffixes[i])){
				return true;
			}
		}
		return false;
	}
	
	protected boolean matchPatterns(String path){
		for(int i=0;i<patterns.length;i++){
			if(patternMatcher.match(patterns[i], path)){
				return true;
			}
		}
		return false;
	}
	
	protected void parseSuffixes(String suffixes){
		if(!Strings.isEmpty(suffixes)){
			if(ignoreCase){
				suffixes = suffixes.toLowerCase();
			}
			
			Set<String> set = new LinkedHashSet<String>();
			
			String[] lines = Strings.splitMultiLines(suffixes);
			
			for(String line : lines){
				String[] parts = Strings.split(line,'|');
				
				for(String part : parts){
					if(!Strings.isEmpty((part = part.trim()))){
						if(ignoreCase){
							part = part.toLowerCase();
						}
						set.add(part);
					}
				}
			}
			
			if(!set.isEmpty()){
				this.suffixes = set.toArray(new String[set.size()]);
			}
		}
	}
	
	protected void parsePrefixes(String prefixes){
		if(!Strings.isEmpty(prefixes)){
			if(ignoreCase){
				prefixes = prefixes.toLowerCase();
			}
			
			Set<String> set = new LinkedHashSet<String>();

			String[] lines = Strings.splitMultiLines(prefixes);
			for(String line : lines){
				if(!Strings.isEmpty(line = line.trim())){
					if(!line.startsWith("/")){
						line = "/" + line;
					}
					set.add(line);
				}
			}
			if(!set.isEmpty()){
				this.prefixes = set.toArray(new String[set.size()]);
			}
		}
	}
	
	protected void parsePatterns(String patterns){
		if(!Strings.isEmpty(patterns)){
			if(ignoreCase){
				patterns = patterns.toLowerCase();
			}
			
			Set<String> set = new LinkedHashSet<String>();
			
			String[] lines = Strings.splitMultiLines(patterns);
			
			for(String line : lines){
				if(!Strings.isEmpty(line = line.trim())){
					if(!line.startsWith("/")){
						line = "/" + line;
					}
					
					if(!patternMatcher.isPattern(line)){
						throw new IllegalArgumentException("'" + line + "' is not a valid ant path pattern");
					}
					
					set.add(line);
				}
			}
			
			if(!set.isEmpty()){
				this.patterns = set.toArray(new String[set.size()]);
			}
		}
	}

	@Override
    public String toString() {
		return new ToStringBuilder(this)
					.append("prefixes",Strings.join(prefixes,','))
					.append("suffixes",Strings.join(suffixes,','))
					.append("patterns",Strings.join(patterns,'|'))
					.toString();
    }
	
	
}