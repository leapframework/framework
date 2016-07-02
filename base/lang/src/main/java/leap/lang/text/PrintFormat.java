/*
 * Copyright 2013 the original author or authors.
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
package leap.lang.text;

public final class PrintFormat {

	/* Constant for left justification. */
	public static final int	JUST_LEFT	= 'l';

	/* Constant for centering. */
	public static final int	JUST_CENTER	= 'c';

	/** Constant for right-justified Strings. */
	public static final int	JUST_RIGHT	= 'r';

	/** Current justification */
	private int	just;

	/** Current max length */
	private int	maxChars;
	
	public PrintFormat(int maxChars) {
		this(maxChars,JUST_LEFT);
	}

	public PrintFormat(int maxChars, int justn) {
		switch (justn) {
			case JUST_LEFT:
			case JUST_CENTER:
			case JUST_RIGHT:
				this.just = justn;
				break;
			default:
				throw new IllegalArgumentException("invalid justification arg.");
		}
		if (maxChars < 1) {
			throw new IllegalArgumentException("maxChars must be positive.");
		}
		this.maxChars = maxChars;
	}
	
	public int maxChars(){
		return maxChars;
	}

	/** Format a String */
	public StringBuffer format(String s, StringBuffer where) {
		if(null == s){
			s = "";
		}
		
		String wanted = s.substring(0, Math.min(s.length(), maxChars));

		// If no space left for justification, return maxChars' worth */
		if (wanted.length() > maxChars) {
			where.append(wanted);
		}
		// Else get the spaces in the right place.
		else
			switch (just) {
				case JUST_RIGHT:
					pad(where, maxChars - wanted.length());
					where.append(wanted);
					break;
				case JUST_CENTER:
					int startPos = where.length();
					pad(where, (maxChars - wanted.length()) / 2);
					where.append(wanted);
					pad(where, (maxChars - wanted.length()) / 2);
					// Adjust for "rounding error"
					pad(where, maxChars - (where.length() - startPos));
					break;
				case JUST_LEFT:
					where.append(wanted);
					pad(where, maxChars - wanted.length());
					break;
			}
		return where;
	}

	public String format(String s) {
		return format(s, new StringBuffer()).toString();
	}
	
	protected final void pad(StringBuffer to, int howMany) {
		for (int i = 0; i < howMany; i++){
			to.append(' ');
		}
	}
}