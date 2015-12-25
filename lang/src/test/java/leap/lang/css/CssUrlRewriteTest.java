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

import leap.junit.TestBase;

import org.junit.Test;

public class CssUrlRewriteTest extends TestBase {
    
    protected String path = "/t";
    
    @Test
    public void testImport() {
        //assertEquals("@import /t/a b", rewrite("@import a b"));
        assertEquals("@import '/t/a' b", rewrite("@import 'a' b"));
        assertEquals("@import \"/t/a\" b", rewrite("@import \"a\" b"));
        
        assertEquals("@import url(/t/a) b", rewrite("@import url(a) b"));
        assertEquals("@import url(/t/a) b", rewrite("@import url( a ) b"));
        assertEquals("@import url('/t/a') b", rewrite("@import url('a') b"));
        assertEquals("@import url(\"/t/a\") b", rewrite("@import url(\"a\") b"));
        assertEquals("@import url('/a') b", rewrite("@import url('../a') b"));
    }
    
    @Test
    public void testProperty() {
        assertEquals("p : url ('/t/a') b", rewrite("p : url ('a') b"));
        assertEquals("p : url (/t/a) b", rewrite("p : url (a) b"));
        assertEquals("p : url (\"/t/a\") b", rewrite("p : url (\"a\") b"));
        assertEquals("p : url (\"/t/a\") b", rewrite("p : url (\"./a\") b"));
        
        assertEquals("p : url (data:aa) b", rewrite("p : url (data:aa) b"));
    }
    
    @Test
    public void testCombine() {
        assertEquals("@import '/t/a' b\na{p:url(/t/a)}", rewrite("@import 'a' b\na{p:url(a)}"));
        assertEquals("@import '/t/a' b\na{p:url(/t/a)}\n@import '/t/a'", rewrite("@import 'a' b\na{p:url(a)}\n@import 'a'"));
    }
    
    private String rewrite(String css) {
        return new CssUrlRewriter(path, css).rewrite();
    }
}
