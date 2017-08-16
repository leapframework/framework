/*
 * Copyright 2017 the original author or authors.
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
package leap.webunit.client;

import leap.junit.TestBase;
import leap.lang.Charsets;
import leap.lang.Strings;
import leap.lang.exception.NestedIOException;
import leap.lang.http.HTTP;
import leap.lang.http.Headers;
import leap.lang.http.MimeType;
import leap.lang.http.MimeTypes;
import leap.lang.io.IO;
import leap.lang.jsoup.Jsoup;
import leap.lang.jsoup.nodes.Document;

import java.io.InputStream;
import java.nio.charset.Charset;

abstract class THttpResponseBase<C extends THttpClient> implements THttpResponse {

    protected static final leap.lang.http.Header[] EMPTY_HEADER_ARRAY = new leap.lang.http.Header[]{};

    protected final THttpRequestBase<C> request;

    protected MimeType contentType;
    protected String   content;
    protected Document htmlDocument;

    protected THttpResponseBase(THttpRequestBase<C> request) {
        this.request = request;
    }

    @Override
    public THttpRequest request() {
        return request;
    }

    @Override
    public boolean isOk(){
        return getStatus() == HTTP.SC_OK;
    }

    @Override
    public boolean isSuccess(){
        return 200 <= getStatus() && getStatus() < 300;
    }

    @Override
    public boolean isFailure() {
        return getStatus() >= 300;
    }

    @Override
    public String getLocation(){
        return getHeader(Headers.LOCATION);
    }

    @Override
    public String getMediaType(){
        MimeType contentType = getContentType();
        return null == contentType ? null : contentType.getMediaType();
    }

    @Override
    public String getCharset(){
        MimeType contentType = getContentType();
        return null == contentType ? null : contentType.getCharset();
    }

    @Override
    public String getContent() throws NestedIOException {
        if(null == content){
            InputStream is = getInputStream();
            content = null == is ? null : IO.readStringAndClose(is, charset());
        }
        return content;
    }

    @Override
    public Document getDocument() throws NestedIOException {
        if(null == htmlDocument){
            String html = getContent();
            htmlDocument = Jsoup.parse(html);
        }
        return htmlDocument;
    }

    @Override
    public THttpResponse assertStatusEquals(int status){
        TestBase.assertEquals((Integer)status, getStatus());
        return this;
    }

    @Override
    public THttpResponse assertContentEquals(String expectedContent){
        TestBase.assertEquals(expectedContent, getContent());
        return this;
    }

    @Override
    public THttpResponse assertContentEmpty() {
        TestBase.assertEmpty(getContent());
        return this;
    }

    @Override
    public THttpResponse assertContentNotEmpty() {
        TestBase.assertNotEmpty(getContent());
        return this;
    }

    @Override
    public THttpResponse assertContentContains(String containsContent){
        TestBase.assertContains(getContent(),containsContent);
        return this;
    }

    @Override
    public THttpResponse assertSuccess(){
        TestBase.assertTrue(isSuccess());
        return this;
    }

    @Override
    public THttpResponse assertFailure() {
        TestBase.assertFalse(isSuccess());
        return this;
    }

    @Override
    public THttpResponse assertOk(){
        TestBase.assertTrue("The response status should be 200, but is " + getStatus(),isOk());
        return this;
    }

    @Override
    public THttpResponse assertNotOk() {
        TestBase.assertFalse("The response status should not be 200", isOk());
        return this;
    }

    @Override
    public THttpResponse assertContentTypeHtml(){
        TestBase.assertEquals(MimeTypes.TEXT_HTML,getMediaType());
        return this;
    }

    @Override
    public THttpResponse assertContentTypeText(){
        TestBase.assertEquals(MimeTypes.TEXT_PLAIN,getMediaType());
        return this;
    }

    @Override
    public THttpResponse assertContentTypeEquals(String mediaType){
        TestBase.assertEquals(mediaType,getMediaType());
        return this;
    }

    @Override
    public THttpResponse assertContentTypeEquals(String mediaType,String charset){
        TestBase.assertEquals(mediaType,getMediaType());
        TestBase.assertEquals(Strings.upperCase(charset),Strings.upperCase(getCharset()));
        return this;
    }

    @Override
    public THttpResponse assertContentTypePresent(){
        TestBase.assertNotNull(getContentType());
        return this;
    }

    @Override
    public THttpResponse assertHeaderEquals(String headerName,String expectedValue){
        TestBase.assertEquals(expectedValue, getHeader(headerName));
        return this;
    }

    @Override
    public THttpResponse assertCharsetEquals(String expectedCharset){
        TestBase.assertEquals(expectedCharset,getCharset());
        return this;
    }

    protected Charset charset(){
        MimeType contentType = getContentType();
        String   charset     = null == contentType ? null : contentType.getCharset();

        return null == charset ? Charsets.UTF_8 : Charsets.forName(charset);
    }
}
