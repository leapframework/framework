/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app;

import leap.core.web.RequestBase;
import leap.web.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestFilter implements FilterMapping, Filter {
    public static Map<String, Object> state = new HashMap<>();

    @Override
    public void doFilter(Request request, Response response, FilterChain chain) throws ServletException, IOException {
        state.clear();
        state.putAll(request.getParameters());
        chain.doFilter(request, response);
    }

    @Override
    public Filter getFilter() {
        return this;
    }

    @Override
    public boolean matches(RequestBase request) {
        return true;
    }
}
