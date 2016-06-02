/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.lang.logging;

import leap.lang.net.Urls;
import leap.lang.resource.Resource;

public class LogUtils {

    private static final String USER_DIR             = System.getProperty("user.dir");
    private static final String USER_DIR_WITH_PREFIX = Urls.FILE_URL_PREFIX + System.getProperty("user.dir");

    public static String getFilePath(String path) {
        if(null == path) {
            return null;
        }

        if(path.startsWith(USER_DIR)) {
            return "." + path.substring(USER_DIR.length());
        }else{
            return path;
        }
    }

    /**
     * Returns the url for logging.
     */
    public static String getUrl(Resource r) {
        if(null == r) {
            return null;
        }

        String url = r.getURLString();

        if(url.startsWith(USER_DIR_WITH_PREFIX)) {
            url = "." + url.substring(USER_DIR_WITH_PREFIX.length());
        }else if(url.startsWith(Urls.PROTOCOL_JAR + ":")) {
            int index = url.indexOf(".jar!/");
            if(index > 0) {
                int start = url.lastIndexOf('/', index);
                if(start > 0) {
                    return url.substring(start + 1);
                }
            }
        }

        return url;
    }

    protected LogUtils() {

    }
}
