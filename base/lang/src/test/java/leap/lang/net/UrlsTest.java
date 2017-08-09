/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package leap.lang.net;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

/**
 * @author kael.
 */
public class UrlsTest {
    @Test
    public void testResolveUrlExpr(){
        URI uri1 = URI.create("http://127.0.0.1:8080/ctx");
        URI uri2 = URI.create("http://127.0.0.1/ctx/");
        String str1 = Urls.resolveUrlExpr("@{~/}path1",uri1);
        String str2 = Urls.resolveUrlExpr("@{~}path1",uri1);
        String str3 = Urls.resolveUrlExpr("@{/}path1",uri1);
        String str4 = Urls.resolveUrlExpr("@{^/}path1",uri1);
        String str5 = Urls.resolveUrlExpr("@{^}path1",uri1);
        Assert.assertEquals("http://127.0.0.1:8080/ctx/path1",str1);
        Assert.assertEquals("http://127.0.0.1:8080/ctxpath1",str2);
        Assert.assertEquals("/ctx/path1",str3);
        Assert.assertEquals("/path1",str4);
        Assert.assertEquals("path1",str5);

        str1 = Urls.resolveUrlExpr("@{~/}path1",uri2);
        str2 = Urls.resolveUrlExpr("@{~}path1",uri2);
        str3 = Urls.resolveUrlExpr("@{/}path1",uri2);
        str4 = Urls.resolveUrlExpr("@{^/}path1",uri2);
        str5 = Urls.resolveUrlExpr("@{^}path1",uri2);
        Assert.assertEquals("http://127.0.0.1/ctx/path1",str1);
        Assert.assertEquals("http://127.0.0.1/ctxpath1",str2);
        Assert.assertEquals("/ctx/path1",str3);
        Assert.assertEquals("/path1",str4);
        Assert.assertEquals("path1",str5);
    }
    
}
