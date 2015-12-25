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
package leap.lang.http;

import java.util.Collections;
import java.util.Map;

import leap.lang.Strings;
import leap.lang.http.Header.HeaderElement;

public class MimeType {
	public static final String WILDCARD_TYPE = "*";
	public static final String PARAM_CHARSET = "charset";
	
	private final String 			  type;
	private final String 			  subtype;
	private final Map<String, String> parameters;
	
    public MimeType() {
    	this(WILDCARD_TYPE,WILDCARD_TYPE);
    }
	
    public MimeType(String type, String subtype) {
        this(type, subtype, null, null);
    }
    
    public MimeType(String type, String subtype, String charset) {
        this(type, subtype, charset, null);
    }
	
    public MimeType(String type, String subtype, Map<String, String> parameters) {
        this(type, subtype, null, parameters);
    }

    public MimeType(String type, String subtype,String charset, Map<String, String> parameters) {
        this.type    = type == null ? WILDCARD_TYPE : type;
        this.subtype = subtype == null ? WILDCARD_TYPE : subtype;
        
        Map<String, String> parametersMap = HeaderElement.createParametersMap(parameters);
        if(!Strings.isEmpty(charset)){
        	parametersMap.put(PARAM_CHARSET, charset);
        }
        this.parameters = Collections.unmodifiableMap(parametersMap);
    }
    
    /**
     * Getter for primary type.
     *
     * @return value of primary type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Checks if the primary type is a wildcard.
     *
     * @return true if the primary type is a wildcard.
     */
    public boolean isWildcardType() {
        return this.getType().equals(WILDCARD_TYPE);
    }

    /**
     * Getter for subtype.
     *
     * @return value of subtype.
     */
    public String getSubtype() {
        return this.subtype;
    }
    
    /**
     * Returns a String that contains the media type and subtype value. 
     * 
     * This value does not include the semicolon (;) separator that follows the subtype.
     */
    public String getMediaType() {
    	return type + "/" + subtype;
    }
    
	/**
	 * Indicates whether this mime type is concrete, i.e. whether neither the type or
	 * subtype is a wildcard character {@code &#42;}.
	 * @return whether this mime type is concrete
	 */
	public boolean isConcreteType() {
		return !isWildcardType() && !isWildcardSubtype();
	}

    /**
     * Checks if the subtype is a wildcard.
     *
     * @return true if the subtype is a wildcard.
     */
    public boolean isWildcardSubtype() {
        return this.getSubtype().equals(WILDCARD_TYPE);
    }    
    
    /**
     * Returns the charset parameter or <code>null</code> if not charset parameter.
     */
    public String getCharset(){
    	return parameters.get(PARAM_CHARSET);
    }
    
    public String getParameter(String name){
    	return parameters.get(name);
    }
    
    /**
     * Getter for a read-only parameter map. Keys are case-insensitive.
     *
     * @return an immutable map of parameters.
     */
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    /**
     * Create a new {@code MimeType} instance with the same type, subtype and parameters
     * copied from the original instance and the supplied "{@value #CHARSET_PARAMETER}" parameter.
     *
     * @param charset the "{@value #PARAM_CHARSET}" parameter value. If {@code null} or empty
     *                the "{@value #PARAM_CHARSET}" parameter will not be set or updated.
     * @return copy of the current {@code MimeType} instance with the "{@value #PARAM_CHARSET}"
     *         parameter set to the supplied value.
     */
    public MimeType withCharset(String charset) {
        return new MimeType(this.type, this.subtype, charset, HeaderElement.createParametersMap(this.parameters));
    }
    
    /**
     * Check if this mime type is compatible with another mime type. E.g.
     * image/* is compatible with image/jpeg, image/png, etc. Media type
     * parameters are ignored. The function is commutative.
     *
     * @param other the mime type to compare with.
     * @return true if the types are compatible, false otherwise.
     */
    public boolean isCompatible(MimeType other) {
        return other != null && // return false if other is null, else
                (type.equals(WILDCARD_TYPE) || other.type.equals(WILDCARD_TYPE) || // both are wildcard types, or
                        (type.equalsIgnoreCase(other.type) && (subtype.equals(WILDCARD_TYPE)
                                || other.subtype.equals(WILDCARD_TYPE))) || // same types, wildcard sub-types, or
                        (type.equalsIgnoreCase(other.type) && this.subtype.equalsIgnoreCase(other.subtype))); // same types & sub-types
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MimeType)) {
            return false;
        }

        MimeType other = (MimeType) obj;
        return (this.type.equalsIgnoreCase(other.type)
                && this.subtype.equalsIgnoreCase(other.subtype)
                && this.parameters.equals(other.parameters));
    }

	@Override
	public int hashCode() {
		int result = this.type.hashCode();
		result = 31 * result + this.subtype.hashCode();
		result = 31 * result + this.parameters.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		appendTo(sb);
		return sb.toString();
	}

	protected void appendTo(StringBuilder sb) {
		sb.append(this.type);
		sb.append('/');
		sb.append(this.subtype);
		appendTo(this.parameters, sb);
	}

	private void appendTo(Map<String, String> map, StringBuilder sb) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append(';');
			sb.append(entry.getKey());
			sb.append('=');
			sb.append(entry.getValue());
		}
	}
}
