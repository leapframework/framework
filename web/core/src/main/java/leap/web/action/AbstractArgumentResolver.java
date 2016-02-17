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
package leap.web.action;

import leap.lang.Charsets;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.http.MimeType;
import leap.lang.http.MimeTypes;
import leap.lang.io.IO;
import leap.lang.naming.NamingStyles;
import leap.web.App;
import leap.web.Request;
import leap.web.action.Argument.Location;
import leap.web.route.RouteBase;

import javax.servlet.http.Part;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public abstract class AbstractArgumentResolver implements ArgumentResolver {

    protected static final BiFunction<ActionContext, Argument, Object> query    = (c,a) -> c.getRequest().getQueryParameters().get(a.getName());
    protected static final BiFunction<ActionContext, Argument, Object> request  = (c,a) -> c.getRequest().getParameters().get(a.getName());
    protected static final BiFunction<ActionContext, Argument, Object> part     = (c,a) -> c.getRequest().getPart(a.getName());
    protected static final BiFunction<ActionContext, Argument, Object> _default = (c,a) -> {
        String             name    = a.getName();
        Request            request = c.getRequest();
        Map<String,Object> params  = request.getParameters();

        if(params.containsKey(name)) {
            return params.get(name);
        }

        if(request.isMultipart()) {
            return request.getPart(name);
        }

        return null;
    };

	protected final BiFunction<ActionContext, Argument, Object> func;

	protected AbstractArgumentResolver(App app,RouteBase route,Argument arg) {
        this.func = func(route, arg);
	}
	
	protected Object getParameter(ActionContext ac,Argument arg) throws Throwable{
        return func.apply(ac, arg);
	}

    protected BiFunction<ActionContext, Argument, Object> func(RouteBase route, Argument arg) {
        Location loc = arg.getLocation();

        if(Location.QUERY_PARAM == loc) {
            return query;
        }

        if(Location.PATH_PARAM == loc) {
            return (c,a) -> c.getPathParameters().get(resolvePathVar(route, arg.getName()));
        }

        if(Location.REQUEST_PARAM == loc) {
            return request;
        }

        if(Location.PART_PARAM == loc) {
            return part;
        }

        if(arg.getTypeInfo().isSimpleType()) {
            String var = tryResolvePathVar(route, arg.getName());
            if(null != var) {
                return (c,a) -> c.getPathParameters().get(var);
            }
        }

        return _default;
    }

    protected String resolvePathVar(RouteBase route, String name) {
        String var = tryResolvePathVar(route, name);

        if(null != var) {
            return var;
        }

        throw new IllegalStateException("No path param '" + name + "' exists in action : " + route.getAction());
    }

    protected String tryResolvePathVar(RouteBase route, String name) {
        List<String> vars = route.getPathTemplate().getTemplateVariables();
        for(String var : vars) {
            if(Strings.equalsIgnoreCase(var, name)) {
                return var;
            }
        }

        for(String var : vars) {
            if(NamingStyles.LOWER_UNDERSCORE.of(var).equals(NamingStyles.LOWER_UNDERSCORE.of(name))) {
                return var;
            }
        }

        return null;
    }

	protected Object convertFromPart(Part part,Argument arg) throws Throwable {
		if(part.getSize() == 0) {
			return null;
		}
		
		try(InputStream in = part.getInputStream()) {
			if(arg.getType().equals(byte[].class)) {
				return IO.readByteArray(in);
			}
			
			if(!Strings.isEmpty(part.getContentType())) {
				MimeType mimeType = MimeTypes.parse(part.getContentType());
				
				if(!Strings.isEmpty(mimeType.getCharset())) {
					return Converts.convert(IO.readString(in, Charsets.forName(mimeType.getCharset())),arg.getType(),arg.getGenericType());
				}
			}
			
			return Converts.convert(IO.readString(in, Charsets.UTF_8), arg.getType(), arg.getGenericType());
		}
	}
}