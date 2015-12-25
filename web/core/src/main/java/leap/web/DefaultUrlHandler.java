/*
 * Copyright 2015 the original author or authors.
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
package leap.web;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

import javax.servlet.ServletException;

import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;


public class DefaultUrlHandler implements UrlHandler {
    private static final Log log = LogFactory.get(DefaultUrlHandler.class);
    
    protected static final UrlInfo FORWARD_VIEW   = new DefaultUrlInfo(false, true, true, false);
    protected static final UrlInfo FORWARD_ACTION = new DefaultUrlInfo(true, false, true, false);
    protected static final UrlInfo FORWARD_PATH   = new DefaultUrlInfo(false, false, true, false);
    protected static final UrlInfo REDIRECT       = new DefaultUrlInfo(false, false, false, true);
    
    private static final Set<String> prefixes = new HashSet<String>();
    static {
        prefixes.add(Renderable.ACTION_PREFIX);
        prefixes.add(Renderable.VIEW_PREFIX);
        prefixes.add(Renderable.FORWARD_PREFIX);
        prefixes.add(Renderable.REDIRECT_PREFIX);
        prefixes.add(Renderable.DOWNLOAD_PREFIX);
    }
    
    @Override
    public String removePrefix(String url) {
        String[] parts = split(url);
        if(null == parts){
            return url;
        }
        return parts[1];
    }
    
    protected String[] split(String url) {
        if(null == url) {
            return null;
        }
        
        int index = url.indexOf(':');
        if(index < 0) {
            return null;
        }
        String[] parts = new String[2];
        parts[0] = url.substring(0, index+1).toLowerCase();
        parts[1] = url.substring(index+1);
        return parts;
    }

    @Override
    public void handleUrl(Request request, Response response, String url) throws IOException,ServletException {
        handleUrl(request, response, url, null);
    }

    @Override
    public void handleUrl(Request request, Response response, String url, BiFunction<String, UrlInfo, String> func) throws IOException,ServletException {
        if(null == url) {
            throw new IllegalStateException("Cannot handle null url");
        }
        
        String[] parts = split(url);
        if(null != parts){
            String prefix = parts[0];
            String path   = parts[1];
            
            if(prefix.equals(Renderable.ACTION_PREFIX)) {
                if(null != func) {
                    path = func.apply(path, FORWARD_ACTION);
                }
                log.debug("foward to action : {}", path);
                request.forwardToAction(path);
                return;
            }
            
            if(prefix.equals(Renderable.VIEW_PREFIX)) {
                if(null != func) {
                    path = func.apply(path, FORWARD_VIEW);
                }
                log.debug("forward to view : {}", path);
                request.forwardToView(path);
                return;
            }
            
            if(prefix.equals(Renderable.FORWARD_PREFIX)) {
                if(null != func) {
                    path = func.apply(path, FORWARD_PATH);
                }
                log.debug("forward to path : {}", path);
                request.forward(path);
                return;
            }

            if(prefix.equals(Renderable.REDIRECT_PREFIX)) {
                url = path;
            }
        }
        
        if(null != func) {
            url = func.apply(url, REDIRECT);
        }
        
        log.debug("send redirect to : {}", url);
        response.sendRedirect(url);
    }
    
    protected static final class DefaultUrlInfo implements UrlInfo {
        protected final boolean view;
        protected final boolean action;
        protected final boolean forward;
        protected final boolean redirect;
        
        protected DefaultUrlInfo(boolean action, boolean view, boolean forward, boolean redirect) {
            this.action = action;
            this.view = view;
            this.forward = forward;
            this.redirect = redirect;
        }
        
        @Override
        public boolean isAction() {
            return action;
        }

        @Override
        public boolean isView() {
            return view;
        }

        @Override
        public boolean isForward() {
            return forward;
        }

        @Override
        public boolean isRedirect() {
            return redirect;
        }
    }

}
