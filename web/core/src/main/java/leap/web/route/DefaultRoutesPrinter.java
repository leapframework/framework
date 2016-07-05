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
package leap.web.route;

import java.io.PrintWriter;

import leap.lang.Iterables;
import leap.lang.Strings;
import leap.lang.net.Urls;
import leap.lang.text.PrintFormat;

public class DefaultRoutesPrinter implements RoutesPrinter {
	
	protected boolean reverseDisplayOrder = true;
	
	public boolean isReverseDisplayOrder() {
		return reverseDisplayOrder;
	}

	public void setReverseDisplayOrder(boolean reverseDisplayOrder) {
		this.reverseDisplayOrder = reverseDisplayOrder;
	}

	@Override
    public void print(Routes routes, PrintWriter writer) {
		int maxMethodLength = 6; //length of header 'METHOD' 
		int maxPathLength   = 4; //length of header 'PATH'
		int maxActionLength = 6; //length of header 'ACTION'
		
		for(Route route : routes){
			String handlerDescription = getActionDescription(route);
			String pathDescription    = getPathDescription(route);
			
			maxMethodLength = Math.max(maxMethodLength, route.getMethod().length());
			maxPathLength   = Math.max(maxPathLength,   pathDescription.length());
			maxActionLength = Math.max(maxActionLength, handlerDescription.length());
		}
		
		PrintFormat methodFormat = new PrintFormat(maxMethodLength, PrintFormat.JUST_LEFT);
		PrintFormat pathFormat   = new PrintFormat(maxPathLength,   PrintFormat.JUST_LEFT);
		PrintFormat actionFormat = new PrintFormat(maxActionLength, PrintFormat.JUST_LEFT);
		
		printHeader(writer, methodFormat, pathFormat, actionFormat);
		
		if(reverseDisplayOrder) {
			Route[] displayRoutes = Iterables.toArray(routes,Route.class);
			for(int i=displayRoutes.length-1;i>=0;i--) {
				printRoute(writer,displayRoutes[i],methodFormat,pathFormat,actionFormat);
			}
		}else{
			for(Route route : routes){
				printRoute(writer,route,methodFormat,pathFormat,actionFormat);
			}
		}
		
		writer.flush();
    }
	
	protected String getPathDescription(Route route){
		if(route.getRequiredParameters().isEmpty()){
			return route.getPathTemplate().getTemplate();
		}else{
			return route.getPathTemplate().getTemplate() + "(" + Urls.getQueryString(route.getRequiredParameters())  + ")";
		}
	}
	
	protected String getActionDescription(Route route){
		return route.getAction().toString();
	}
	
	protected void printHeader(PrintWriter writer,PrintFormat methodFormat,PrintFormat pathFormat,PrintFormat actionFormat){
		StringBuilder rule = new StringBuilder();
		
		rule.append(methodFormat.format("METHOD")).append("  ");
		rule.append(pathFormat.format("PATH")).append("   ");
		rule.append(actionFormat.format("ACTION")).append("   ");
		rule.append("DEFAULT VIEW");
		
		writer.println(rule.toString());
		
		StringBuilder line = new StringBuilder();
		line.append(Strings.repeat('-', methodFormat.maxChars())).append("  ")
		    .append(Strings.repeat('-', pathFormat.maxChars())).append("   ")
		    .append(Strings.repeat('-', actionFormat.maxChars())).append("   ")
		    .append(Strings.repeat('-', 30));
		
		writer.println(line.toString());
	}
	
	protected void printRoute(PrintWriter writer,Route route,PrintFormat methodFormat,PrintFormat pathFormat,PrintFormat actionFormat){
		StringBuilder rule = new StringBuilder();
		
		rule.append(methodFormat.format(route.getMethod())).append("  ");
		rule.append(pathFormat.format(getPathDescription(route))).append("   ");
		rule.append(actionFormat.format(getActionDescription(route))).append("   ");
		
		if(null != route.getDefaultViewName()){
			rule.append(route.getDefaultViewName());
		}else{
			rule.append("(none)");
		}
		
		writer.println(rule.toString());
	}

}
