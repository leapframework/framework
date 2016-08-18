/*
 * Copyright 2010 the original author or authors.
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
package leap.lang.json;

import leap.lang.naming.NamingStyle;
import leap.lang.naming.NamingStyles;

public class JsonSettings {
	
	public static JsonSettings MAX = new Builder().build();
	
	public static JsonSettings MIN = new Builder().setKeyQuoted(false)
												  .setIgnoreEmpty(true)
												  .setIgnoreNull(true).build();

    private final boolean     keyQuoted;
    private final boolean     ignoreNull;
    private final boolean     ignoreEmpty;
    private final NamingStyle namingStyle;
    
	public JsonSettings(boolean keyQuoted,boolean ignoreNull,boolean ignoreEmpty,NamingStyle namingStyle) {
		this.keyQuoted   = keyQuoted;
		this.ignoreNull  = ignoreNull;
		this.ignoreEmpty = ignoreEmpty;
		this.namingStyle = namingStyle;
	}
	public boolean isKeyQuoted() {
		return keyQuoted;
	}

	public boolean isIgnoreNull() {
		return ignoreNull;
	}

	public boolean isIgnoreEmpty() {
		return ignoreEmpty;
	}

	public NamingStyle getNamingStyle(){
		return this.namingStyle;
	}
	public static final class Builder {
		
		private boolean keyQuoted  = true;
		private boolean ignoreNull  = false;
		private boolean ignoreEmpty = false;
		private NamingStyle namingStyle = NamingStyles.RAW;
		
		public Builder() {
	        super();
        }

		public boolean isKeyQuoted() {
			return keyQuoted;
		}

		public Builder setKeyQuoted(boolean keyQuoted) {
			this.keyQuoted = keyQuoted;
			return this;
		}

		public boolean isIgnoreNull() {
			return ignoreNull;
		}

		public Builder setIgnoreNull(boolean ignoreNull) {
			this.ignoreNull = ignoreNull;
			return this;
		}

		public boolean isIgnoreEmpty() {
			return ignoreEmpty;
		}

		public Builder setIgnoreEmpty(boolean ignoreEmpty) {
			this.ignoreEmpty = ignoreEmpty;
			return this;
		}

        public Builder ignoreEmpty() {
            return setIgnoreEmpty(true);
        }

        public Builder ignoreNull() {
            return setIgnoreNull(true);
        }

		public NamingStyle getNamingStyle() {
			return namingStyle;
		}

		public Builder setNamingStyle(NamingStyle namingStyle) {
			this.namingStyle = namingStyle;
			return this;
		}

		public JsonSettings build(){
			return new JsonSettings(keyQuoted, ignoreNull, ignoreEmpty,namingStyle);
		}
	}
}