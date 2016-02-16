/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package leap.core.web.path;

import leap.lang.Strings;
import leap.lang.annotation.Internal;

import java.util.Map;


//This class is a modified copy from <a href="http://jersey.java.net">jersey</a>.

/**
 * A URI template for a URI path.
 *
 * @author Paul Sandoz
 * @author Yegor Bugayenko (yegor256 at java.net)
 */
@Internal
final class JerseyPathTemplate extends JerseyUriTemplate implements PathTemplate{

    /**
     * Create a URI path template and encode (percent escape) any characters of
     * the template that are not valid URI characters. Paths that don't start with
     * a slash ({@code '/'}) will be automatically prefixed with one.
     *
     * @param path the URI path template.
     */
    public JerseyPathTemplate(final String path) {
        super(new JerseyPathTemplateParser(JerseyPathTemplate.prefixWithSlash(path)));
    }

    /**
     * Converts the path provided to a slash-leading form, no matter what is provided.
     *
     * @param path the URI path template.
     * @return slash-prefixed path.
     */
    private static String prefixWithSlash(final String path) {
        return !path.isEmpty() && path.charAt(0) == '/' ? path : "/" + path;
    }
    
    private String pattern;

	@Override
    public String getPattern() {
		if(null == pattern){
			pattern = getTemplate().replaceAll(TEMPLATE_NAMES_PATTERN.pattern(), "*");
		}
		
	    return pattern;
    }

	@Override
    public String getTemplateBeforeVariables() {
		if(!hasVariables()){
			return getTemplate();
		}else{
			StringBuilder path = new StringBuilder();

			String[] parts = Strings.split(getTemplate(),"/");
			
			for(String part : parts) {
				if(TEMPLATE_NAMES_PATTERN.matcher(part).matches()) {
					break;
				}
				path.append(part).append('/');
			}
			
			return path.toString();
		}
    }

	@Override
    public int compareTo(PathTemplate o) {
	    return COMPARATOR.compare(this,(JerseyUriTemplate)o);
    }

	@Override
    public boolean hasVariables() {
	    return getNumberOfTemplateVariables() > 0;
    }

	@Override
    public boolean match(String path, Map<String, String> params) {
	    return match((CharSequence)path,params);
    }
	
	@Override
    public boolean matches(String path) {
	    return match((CharSequence)path);
    }
}