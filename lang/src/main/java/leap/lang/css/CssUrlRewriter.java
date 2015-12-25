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
package leap.lang.css;

import leap.lang.path.Paths;

public class CssUrlRewriter {
    
    private final static byte EOI = 0x1A;
    
    protected final String        path;
    protected final String        css;
    protected final StringBuilder chars;
    
    private int                   mark;
    private int                   end;
    private int                   pos;
    private char                  ch;
    private boolean               rewrited;
    
    public CssUrlRewriter(String path, String css) {
        this.path  = Paths.suffixWithSlash(path);
        this.css   = css;
        this.chars = new StringBuilder(css);
        this.end   = chars.length() - 1;
        this.pos   = -1;
    }
    
    public boolean tryRewrite() {
        while(next()) {
            if(ch == '@') {
                if(scanLetters("import")) {
                    rewriteImportUrl();
                }
                continue;
            }
            
            if(ch == ':') {
                if(scanUrlProperty()) {
                    rewritePropertyUrl();
                }
            }
        }
        
        return rewrited;
    }
    
    public String rewrite(){
        if(tryRewrite()) {
            return chars.toString();
        }else{
            return css;
        }
    }
    
    private boolean scanUrlProperty() {
        while(next()) {
            if(Character.isWhitespace(ch)) {
                continue;
            }
            
            if(ch == 'u' || ch == 'U') {
                if(scanUrlStart()) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }
    
    private void rewriteImportUrl() {
        do{
            if(Character.isWhitespace(ch)) {
                continue;
            }
            
            if(ch == '\'' || ch == '\"') {
                rewriteQuotedUri(ch);
                return;
            }
            
            if(ch == 'u' || ch == 'U') {
                if(scanUrlStart()) {
                    rewritePropertyUrl();
                    return;
                }
            }
            
            rewriteUnQuotedUri();
            return;
        }while(next());
    }
    
    private boolean scanUrlStart() {
        if(next() && (ch == 'r' || ch == 'R')) {
            if(next() && (ch == 'l' || ch == 'L')) {
                while(next()) {
                    if(Character.isWhitespace(ch)) {
                        continue;
                    }
                    if(ch == '(') {
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }
    
    private void rewriteUnQuotedUri() {
        mark();
        while(next()) {
            if(Character.isWhitespace(ch)) {
                rewriteUrl(marked());
                break;
            }
        }
    }
    
    private void rewriteQuotedUri(char quoteChar) {
        if(next()){
            mark();
            do{
                if(ch == quoteChar) {
                    rewriteUrl(marked());
                    break;
                }
            }while(next());
        }
    }
    
    private void rewritePropertyUrl() {
        if(next()) {
            mark();
            do{
                if(ch == ')') {
                    rewriteUrl(marked());
                    return;
                }
            }while(next());
        }
    }
    
    private void rewriteUrl(String url) {
        if(url.length() == 0) {
            return;
        }
        
        char c = url.charAt(0);
        if(c == '/') {
            return;
        }
        
        for(int i=1;i<url.length();i++) {
            char ch = url.charAt(i);
            if(ch=='/'){
                break;
            }
            if(ch == ':'){
                return;
            }
        }
        
        String quoteChar = "";
        String urlPath = url.trim();
        if(c == '"') {
            quoteChar = "\"";
            if(url.endsWith("\"")) {
                urlPath = url.substring(1, url.length() - 1);
            }else{
                return;
            }
        }
        if(c == '\''){
            quoteChar = "'";
            if(url.endsWith("'")) {
                urlPath = url.substring(1, url.length() - 1);
            }else{
                return;
            }
        }
        
        String newUrl = quoteChar + path(Paths.normalize(Paths.applyRelative(path, urlPath))) + quoteChar;
        chars.replace(mark, pos, newUrl);
        
        int increse = newUrl.length() - url.length(); 
        pos += increse;
        end += increse;
        
        rewrited = true;
    }
    
    protected String path(String rewritedPath) {
        return rewritedPath;
    }
    
    private boolean scanLetters(String word) {
        if(next()) {
            mark();
            do{
                if(!Character.isLetter(ch)) {
                    break;
                }
            }while(next());
        }
        if(mark == pos || markedLen() > word.length()) {
            return false;
        }else if(word.equalsIgnoreCase(marked())){
            return true;
        }
        return false;
    }
    
    private void mark() {
        mark = pos;
    }
    
    private int markedLen() {
        return pos - mark;
    }
    
    private String marked() {
        return chars.substring(mark, pos);
    }
    
    private boolean next() {
        if(pos == end) {
            ch = EOI;
            return false;
        }
        pos++;
        ch = chars.charAt(pos);
        return true;
    }
    
}
