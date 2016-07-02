/*
 * Copyright 2002-2012 the original author or authors.
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

package leap.lang.tostring;


//from spring framework

/**
 * Utility class that builds pretty-printing {@code toString()} methods
 * with pluggable styling conventions. By default, ToStringCreator adheres
 * to Spring's {@code toString()} styling conventions.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 1.2.2
 */
public class ToStringBuilder {

	/**
	 * Default ToStringStyler instance used by this ToStringCreator.
	 */
	private static final ToStringStyler DEFAULT_TO_STRING_STYLER =
			new DefaultToStringStyler(StylerUtils.DEFAULT_VALUE_STYLER);


	private StringBuilder buffer = new StringBuilder(512);

	private ToStringStyler styler;

	private Object object;

	private boolean styledFirstField;

	public ToStringBuilder(){
		this(null,(ToStringStyler)null);
	}

	/**
	 * Create a ToStringCreator for the given object.
	 * @param obj the object to be stringified
	 */
	public ToStringBuilder(Object obj) {
		this(obj, (ToStringStyler) null);
	}

	/**
	 * Create a ToStringCreator for the given object, using the provided style.
	 * @param obj the object to be stringified
	 * @param styler the ValueStyler encapsulating pretty-print instructions
	 */
	ToStringBuilder(Object obj, ValueStyler styler) {
		this(obj, new DefaultToStringStyler(styler != null ? styler : StylerUtils.DEFAULT_VALUE_STYLER));
	}

	/**
	 * Create a ToStringCreator for the given object, using the provided style.
	 * @param obj the object to be stringified
	 * @param styler the ToStringStyler encapsulating pretty-print instructions
	 */
	ToStringBuilder(Object obj, ToStringStyler styler) {
		this.object = obj;
		this.styler = (styler != null ? styler : DEFAULT_TO_STRING_STYLER);
		
		if(null != obj){
			this.styler.styleStart(this.buffer, this.object);
		}else{
			this.styler.styleStart(buffer);
		}
	}


	/**
	 * Append a byte field value.
	 * @param fieldName the name of the field, usually the member variable name
	 * @param value the field value
	 * @return this, to support call-chaining
	 */
	public ToStringBuilder append(String fieldName, byte value) {
		return append(fieldName, new Byte(value));
	}

	/**
	 * Append a short field value.
	 * @param fieldName the name of the field, usually the member variable name
	 * @param value the field value
	 * @return this, to support call-chaining
	 */
	public ToStringBuilder append(String fieldName, short value) {
		return append(fieldName, new Short(value));
	}

	/**
	 * Append a integer field value.
	 * @param fieldName the name of the field, usually the member variable name
	 * @param value the field value
	 * @return this, to support call-chaining
	 */
	public ToStringBuilder append(String fieldName, int value) {
		return append(fieldName, new Integer(value));
	}

	/**
	 * Append a long field value.
	 * @param fieldName the name of the field, usually the member variable name
	 * @param value the field value
	 * @return this, to support call-chaining
	 */
	public ToStringBuilder append(String fieldName, long value) {
		return append(fieldName, new Long(value));
	}

	/**
	 * Append a float field value.
	 * @param fieldName the name of the field, usually the member variable name
	 * @param value the field value
	 * @return this, to support call-chaining
	 */
	public ToStringBuilder append(String fieldName, float value) {
		return append(fieldName, new Float(value));
	}

	/**
	 * Append a double field value.
	 * @param fieldName the name of the field, usually the member variable name
	 * @param value the field value
	 * @return this, to support call-chaining
	 */
	public ToStringBuilder append(String fieldName, double value) {
		return append(fieldName, new Double(value));
	}

	/**
	 * Append a boolean field value.
	 * @param fieldName the name of the field, usually the member variable name
	 * @param value the field value
	 * @return this, to support call-chaining
	 */
	public ToStringBuilder append(String fieldName, boolean value) {
		return append(fieldName, Boolean.valueOf(value));
	}

	/**
	 * Append a field value.
	 * @param fieldName the name of the field, usually the member variable name
	 * @param value the field value
	 * @return this, to support call-chaining
	 */
	public ToStringBuilder append(String fieldName, Object value) {
	    if(styledFirstField) {
	        this.styler.styleFieldSeparator(this.buffer);
	        this.styler.styleField(this.buffer, fieldName, value, false);
	    }else{
	        this.styler.styleField(this.buffer, fieldName, value, true);
	        this.styledFirstField = true;
	    }
		return this;
	}

	/**
	 * Append the provided value.
	 * @param value The value to append
	 * @return this, to support call-chaining.
	 */
	public ToStringBuilder append(Object value) {
		this.styler.styleValue(this.buffer, value);
		return this;
	}

	/**
	 * Return the String representation that this ToStringCreator built.
	 */
	@Override
	public String toString() {
		if(null != object){
			this.styler.styleEnd(this.buffer, this.object);
		}else{
			this.styler.styleEnd(buffer);
		}
		return this.buffer.toString();
	}

}
