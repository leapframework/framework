/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.doc;

import leap.lang.Exceptions;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.web.api.meta.ApiMetadataContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

//todo : extracts doc resolver to core module ?
public class DefaultDocResolver implements DocResolver {

    private static final Log log = LogFactory.get(DefaultDocResolver.class);

    private static final String EXTERNAL_DOC_PREFIX = "doc:";
    private static final String EXTERNAL_DOC_CLASSPATH = "classpath:doc/";

    @Override
    public String resolveDescription(ApiMetadataContext context, String desc) {
        //todo : message key ?

        //todo : external doc
        if(desc.startsWith(EXTERNAL_DOC_PREFIX)) {
            return resolveExternalDoc(context, Strings.removeStart(desc, EXTERNAL_DOC_PREFIX));
        }

        return desc;
    }

    protected String resolveExternalDoc(ApiMetadataContext context, String doc) {
        if(Strings.isEmpty(doc)) {
            return "!!!err!! : doc file can't be empty!";
        }

        String fragment      = null;
        int    fragmentIndex = doc.lastIndexOf('#');

        if(fragmentIndex > 0) {
            fragment = doc.substring(fragmentIndex + 1);
            doc = doc.substring(0, fragmentIndex);
        }

        String cp = EXTERNAL_DOC_CLASSPATH + Paths.prefixWithoutSlash(doc);
        Resource resource = Resources.getResource(cp);
        if(!resource.exists()) {
            return "!!!err!!! : doc file '" + cp + "' not exists!";
        }

        log.debug("Read doc '{}'", cp);

        String content = resource.getContent();
        if(Strings.isEmpty(fragment)) {
            return content;
        }else{
            //todo : optimize performance
            return readFragment(cp, content, fragment);
        }
    }

    protected String readFragment(String cp, String content, String fragment) {
        //#fragment
        BufferedReader reader = new BufferedReader(new StringReader(content));

        String line;
        try{
            StringBuilder buf = null;

            while((line = reader.readLine()) != null) {
                line = line.trim();

                if(line.startsWith("#")) {

                    if(null != buf) {
                        //fragment end
                        break;
                    }

                    if(Strings.equalsIgnoreCase(fragment, line.substring(1).trim())) {
                        //fragment start
                        buf = new StringBuilder();
                    }

                }else {
                    if(buf.length() > 0) {
                        buf.append('\n');
                    }
                    buf.append(line);
                }
            }

            if(null != buf) {
                return buf.toString().trim();
            }

            return "!!!err!!! : fragment '" + fragment + "' not exists in '" + cp + "'";
        }catch (IOException e) {
            throw Exceptions.wrap(e);
        }
    }
}
