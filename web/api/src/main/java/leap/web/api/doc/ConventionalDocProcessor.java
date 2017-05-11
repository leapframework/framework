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

import leap.lang.Classes;
import leap.lang.Exceptions;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.lang.reflect.ReflectMethod;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.web.action.Argument;
import leap.web.api.config.ApiConfigException;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.ApiMetadataContext;
import leap.web.api.meta.ApiMetadataProcessor;
import leap.web.api.meta.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.*;

public class ConventionalDocProcessor implements ApiMetadataProcessor {

    private static final String CLASSPATH_PREFIX = "classpath:doc/";

    @Override
    public void postProcess(ApiMetadataContext context, ApiMetadataBuilder m) {
        Map<Class<?>,ClassDoc> docs = new HashMap<>();

        //operations
        m.getPaths().forEach((k, p) -> {
            p.getOperations().forEach(o -> {
                processOperation(context, docs, o);
            });
        });

        //models
        m.getModels().forEach((k, model) -> {
            processModel(context, docs, model);
        });
    }

    protected void processOperation(ApiMetadataContext context, Map<Class<?>, ClassDoc> docs, MApiOperationBuilder o) {
        ReflectMethod method = o.getRoute().getAction().getMethod();

        //operation
        if(null != method) {
            ClassDoc  cdoc = resolveClassDoc(docs, method.getDeclaringClass(), false);
            MethodDoc mdoc = cdoc.methods.get(method.getName());

            if(null != mdoc) {
                if(!Strings.isEmpty(mdoc.description)) {
                    o.setDescription(mdoc.description);
                }

                if(!Strings.isEmpty(mdoc.response)) {
                    for(MApiResponseBuilder resp : o.getResponses()) {
                        if(resp.getStatus() >= 200 && resp.getStatus() < 300) {
                            resp.setDescription(mdoc.response);
                            break;
                        }
                    }
                }

                //parameters
                o.getParameters().forEach((param) -> processParameter(context, docs, mdoc, param, method));
            }
        }
    }

    protected void processParameter(ApiMetadataContext context, Map<Class<?>, ClassDoc> docs, MethodDoc doc, MApiParameterBuilder param, ReflectMethod method) {
        Argument a = param.getArgument();
        if (null != a) {

            if(null != param.getWrapperArgument()) {
                ClassDoc wrapperDoc = resolveClassDoc(docs, param.getWrapperArgument().getType(), true);

                String desc = wrapperDoc.properties.get(a.getDeclaredName());
                if(!Strings.isEmpty(desc)) {
                    param.setDescription(desc);
                }
            }else{
                String desc = doc.params.get(a.getDeclaredName());
                if(!Strings.isEmpty(desc)) {
                    param.setDescription(desc);
                }
            }

        }
    }

    protected void processModel(ApiMetadataContext context, Map<Class<?>,ClassDoc> docs, MApiModelBuilder model) {
        Class<?> c = model.getJavaType();
        if(null != c) {
            ClassDoc doc = resolveClassDoc(docs, c, true);

            if(!Strings.isEmpty(doc.description)) {
                model.setDescription(doc.description);
            }

            model.getProperties().forEach((k,p) -> {
                processProperty(context, doc, p);
            });
        }

    }

    protected void processProperty(ApiMetadataContext context, ClassDoc doc, MApiPropertyBuilder p) {
        BeanProperty bp = p.getBeanProperty();
        if(null != bp) {
            String desc = doc.properties.get(bp.getName());
            if(!Strings.isEmpty(desc)) {
                p.setDescription(desc);
            }
        }
    }

    protected ClassDoc resolveClassDoc(Map<Class<?>,ClassDoc> docs, Class<?> cls, boolean properties) {
        ClassDoc doc = docs.get(cls);
        if(null == doc) {

            for(String file : getCandidateDocFiles(cls)) {
                String loc = CLASSPATH_PREFIX + file;

                Resource resource = Resources.getResource(loc);
                if(resource.exists()) {
                    doc = readClassDoc(cls, resource, properties);
                    break;
                }
            }

            if(null == doc) {
                doc = ClassDoc.EMPTY;
            }

            docs.put(cls, doc);
        }
        return doc;
    }

    protected ClassDoc readClassDoc(Class<?> cls, Resource resource, boolean properties) {
        ClassDoc doc = new ClassDoc(cls);
        doc.parse(resource.getURLString(), resource.getContent(),properties);
        return doc;
    }

    protected Set<String> getCandidateDocFiles(Class<?> cls) {
        Set<String> files = new LinkedHashSet<>();

        String pkg = Classes.getPackageName(cls);
        if(!Strings.isEmpty(pkg)) {
            String[] parts = Strings.split(pkg, '.');
            for(int i=0;i<parts.length;i++) {
                StringBuilder dir = new StringBuilder();
                for(int j=i;j<parts.length;j++) {
                    if(j > i) {
                        dir.append('/');
                    }
                    dir.append(parts[j]);
                }
                files.add(dir.toString() + "/" + cls.getSimpleName() + ".md");
            }
        }

        files.add(cls.getSimpleName() + ".md");

        return files;
    }

    protected static boolean isHead1(String line) {
        for(int i=0;i<line.length();i++) {
            char c = line.charAt(i);
            if(Character.isWhitespace(c)) {
                continue;
            }
            if(c == '#') {
                i++;
                if(i<line.length() && line.charAt(i) != '#'){
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    protected static boolean isHead2(String line) {
        for(int i=0;i<line.length();i++) {
            char c = line.charAt(i);
            if(Character.isWhitespace(c)) {
                continue;
            }
            if(c == '#') {
                i++;
                if(i<line.length() && line.charAt(i) == '#'){
                    i++;
                    if(i==line.length()-1 || line.charAt(i) != '#') {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    protected static final class ClassDoc {
        static final ClassDoc EMPTY = new ClassDoc(null);

        protected Class<?>               cls;
        protected String                 description;
        protected Map<String, MethodDoc> methods    = new HashMap<>();
        protected Map<String, String>    properties = new HashMap<>();

        public ClassDoc(Class<?> cls) {
            this.cls = cls;
        }

        protected void parse(String url, String content, boolean forProperties) {
            String[] lines = readLines(content);

            boolean       classEnd  = false;
            StringBuilder classDesc = new StringBuilder();

            for(int i=0;i<lines.length;i++) {
                if(isHead1(lines[i])) {

                    classEnd = true;

                    if(forProperties) {
                        //found property
                        String prop = lines[i].trim().substring(1).trim();
                        i++;
                        StringBuilder s = new StringBuilder();
                        for(;i<lines.length;i++) {
                            //until EOF or '#' found
                            if(isHead1(lines[i])) {
                                break;
                            }else{
                                s.append(lines[i]).append('\n');
                            }
                        }
                        properties.put(prop, s.toString().trim());
                    }else{
                        //found method.
                        String method = lines[i].trim().substring(1).trim();

                        boolean exists = false;
                        for(Method m : cls.getMethods()) {
                            if(m.getName().equals(method)) {
                                exists = true;
                                break;
                            }
                        }
                        if(!exists) {
                            throw new ApiConfigException("Method '" + method + "' not exists in class '" + cls.getName() + ", check file : " + url);
                        }

                        MethodDoc doc = new MethodDoc(url, i);

                        i = doc.parse(lines) - 1;

                        methods.put(method, doc);
                    }
                }else if(!classEnd){
                    classDesc.append(lines[i]).append('\n');
                }
            }

            this.description = classDesc.toString().trim();
        }

        protected String[] readLines(String content) {
            BufferedReader reader = new BufferedReader(new StringReader(content));

            List<String> lines = new ArrayList<>();

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                throw Exceptions.uncheck(e);
            }

            return lines.toArray(new String[lines.size()]);
        }
    }

    protected static final class MethodDoc {

        protected String              description;
        protected Map<String, String> params = new HashMap<>();
        protected String              response;

        private String url;
        private int curr;

        public MethodDoc(String url, int curr) {
            this.url  = url;
            this.curr = curr;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, String> getParams() {
            return params;
        }

        public void setParams(Map<String, String> params) {
            this.params = params;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        protected int parse(String[] lines) {
            readDescription(lines);
            readParams(lines);
            readResponse(lines);
            return this.curr;
        }

        protected void readDescription(String[] lines) {
            this.description = readContent(lines);
        }

        protected void readParams(String[] lines) {
            if(isEOF(lines) || !isHead2(lines[curr])) {
                return;
            }

            int i;
            for(i=curr;i<lines.length;i++) {
                if(!readParam(lines)){
                    return;
                }
                if(curr >= lines.length - 1 || isHead1(lines[curr])) {
                    return;
                }else{
                    i = curr;
                }
            }

            curr = i;
        }

        protected boolean readParam(String[] lines) {
            String line = lines[curr].trim().substring(2).trim();
            if(Strings.startsWithIgnoreCase(line, "response")) {
                return false;
            }else if(Strings.startsWithIgnoreCase(line, "param")) {
                line = Strings.removeStartIgnoreCase(line,"param").trim();
                String name = line.substring(1).trim();
                String desc = readContent(lines);
                params.put(name, desc);
                return true;
            }else{
                throw new ApiConfigException("Invalid param line '" + lines[curr] + "' at file :" + url);
            }
        }

        protected void readResponse(String[] lines) {
            if(!isEOF(lines) && isHead2(lines[curr])) {
                String line = lines[curr].substring(2).trim();
                if(Strings.startsWithIgnoreCase(line, "response")) {
                    if(Strings.startsWithIgnoreCase(line, "response")) {
                        this.response = readContent(lines);
                    }
                }else{
                    throw new ApiConfigException("Invalid response line '" + lines[curr] + "' at file :" + url);
                }
            }
        }

        protected String readContent(String[] lines) {
            //read until EOF or '#' or '##', allows '###'

            StringBuilder s = new StringBuilder();
            int i;
            for(i=curr+1;i<lines.length;i++) {

                if(isHead1(lines[i]) || isHead2(lines[i])) {
                    break;
                }

                s.append(lines[i]).append('\n');
            }
            curr = i;

            return s.toString().trim();
        }

        protected boolean isEOF(String[] lines) {
            return curr >= lines.length;
        }
    }
}
