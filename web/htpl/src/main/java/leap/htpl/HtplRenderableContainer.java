/*
 * Copyright 2014 the original author or authors.
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
package leap.htpl;

import java.io.IOException;
import java.util.List;


public class HtplRenderableContainer implements HtplRenderable {
	
	protected final HtplRenderable[] nodes;

	public HtplRenderableContainer(List<HtplRenderable> nodes) {
		this.nodes = nodes.toArray(new HtplRenderable[]{});
	}

	@Override
    public void render(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException {
		for(int i=0;i<nodes.length;i++){
			nodes[i].render(tpl, context, writer);
		}
    }

	@Override
    public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(int i=0;i<nodes.length;i++){
			sb.append(nodes[i].toString());
		}

		return sb.toString();
    }
}