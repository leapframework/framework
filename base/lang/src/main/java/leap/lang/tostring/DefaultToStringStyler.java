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

import leap.lang.Args;
import leap.lang.Classes;
import leap.lang.Objects2;

//from spring framework

/**
 * Spring's default {@code toString()} styler.
 *
 * <p>This class is used by {@link ToStringBuilder} to style {@code toString()}
 * output in a consistent manner according to Spring conventions.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 1.2.2
 */
class DefaultToStringStyler implements ToStringStyler {

	private final ValueStyler valueStyler;


	/**
	 * Create a new DefaultToStringStyler.
	 * @param valueStyler the ValueStyler to use
	 */
	public DefaultToStringStyler(ValueStyler valueStyler) {
		Args.notNull(valueStyler, "ValueStyler");
		this.valueStyler = valueStyler;
	}

	/**
	 * Return the ValueStyler used by this ToStringStyler.
	 */
	protected final ValueStyler getValueStyler() {
		return this.valueStyler;
	}

	@Override
    public void styleStart(StringBuilder buffer) {
		buffer.append('[');
    }

	@Override
    public void styleEnd(StringBuilder buffer) {
		buffer.append(']');
    }

	@Override
	public void styleStart(StringBuilder buffer, Object obj) {
		if (!obj.getClass().isArray()) {
			buffer.append('[').append(Classes.getShortName(obj.getClass()));
			styleIdentityHashCode(buffer, obj);
		}
		else {
			buffer.append('[');
			styleIdentityHashCode(buffer, obj);
			buffer.append(' ');
			styleValue(buffer, obj);
		}
	}

	private void styleIdentityHashCode(StringBuilder buffer, Object obj) {
		buffer.append('@');
		buffer.append(Objects2.getIdentityHexString(obj));
	}

	@Override
	public void styleEnd(StringBuilder buffer, Object o) {
		buffer.append(']');
	}

	@Override
    public void styleField(StringBuilder buffer, String fieldName, Object value, boolean first) {
        styleFieldStart(buffer, fieldName, first);
        styleValue(buffer, value);
        styleFieldEnd(buffer, fieldName);
    }

    protected void styleFieldStart(StringBuilder buffer, String fieldName, boolean first) {
        if(!first) {
            buffer.append(' ');
        }
        buffer.append(fieldName).append("=");
	}

	protected void styleFieldEnd(StringBuilder buffer, String fieldName) {
	}

	@Override
	public void styleValue(StringBuilder buffer, Object value) {
		buffer.append(this.valueStyler.style(value));
	}

	@Override
	public void styleFieldSeparator(StringBuilder buffer) {
		buffer.append(',');
	}

}
