/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.edm;

import leap.lang.Valued;

/**
 * @see <a href="http://msdn.microsoft.com/en-us/library/ee373839.aspx">Feed Customization (WCF Data Services)</a>.
 */
public class EdmFeedCustomization {

	public enum SyndicationTextContentKind implements Valued<String>  {
		
		Text("text"),
		
		Html("html"),
		
		Xhtml("xhtml");
		
		private final String value;
		
		SyndicationTextContentKind(String value){
			this.value = value;
		}

		public String getValue() {
	        return value;
        }

		@Override
        public String toString() {
			return value;
        }
	}
	
	public enum SyndicationItemProperty implements Valued<String>  {
		
		AuthorEmail("SyndicationAuthorEmail"),
		
		AuthorName("SyndicationAuthorName"),
		
		AuthorUri("SyndicationAuthorUri"),
		
		ContributorEmail("SyndicationContributorEmail"),
		
		ContributorName("SyndicationContributorName"),
		
		ContributorUri("SyndicationContributorUri"),

		Published("SyndicationPublished"),
		
		Rights("SyndicationRights"),
		
		Summary("SyndicationSummary"),
		
		Title("SyndicationTitle"),
		
		Updated("SyndicationUpdated"),
		
		CustomProperty("SyndicationCustomProperty");
		
		private final String value;
		
		SyndicationItemProperty(String value){
			this.value = value;
		}

		public String getValue() {
	        return value;
        }

		@Override
        public String toString() {
			return value;
        }
		
		public boolean equalsValue(String value){
			return this.value.equals(value);
		}
	}	
	
	protected EdmFeedCustomization(){
		
	}
}
