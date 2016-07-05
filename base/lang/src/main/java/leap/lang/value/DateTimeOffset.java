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
package leap.lang.value;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public final class DateTimeOffset implements Serializable, Comparable<DateTimeOffset> {
	private static final long serialVersionUID = 541973748553014280L;
	private final long utcMillis;
	private final int nanos;
	private final int minutesOffset;
	private static final int NANOS_MIN = 0;
	private static final int NANOS_MAX = 999999999;
	private static final int MINUTES_OFFSET_MIN = -840;
	private static final int MINUTES_OFFSET_MAX = 840;
	private static final int HUNDRED_NANOS_PER_SECOND = 10000000;
	private String formattedValue = null;

	private DateTimeOffset(Timestamp paramTimestamp, int paramInt) {
		if ((paramInt < MINUTES_OFFSET_MIN) || (paramInt > MINUTES_OFFSET_MAX))
			throw new IllegalArgumentException();
		this.minutesOffset = paramInt;

		int i = paramTimestamp.getNanos();
		if ((i < NANOS_MIN) || (i > NANOS_MAX)) {
			throw new IllegalArgumentException();
		}

		int j = (i + 50) / 100;
		this.nanos = (100 * (j % HUNDRED_NANOS_PER_SECOND));
		this.utcMillis = (paramTimestamp.getTime() - paramTimestamp.getNanos() / 1000000 + 1000 * (j / 10000000));

		assert ((this.minutesOffset >= MINUTES_OFFSET_MIN) && (this.minutesOffset <= MINUTES_OFFSET_MAX)) : ("minutesOffset: " + this.minutesOffset);
		assert ((this.nanos >= 0) && (this.nanos <= NANOS_MAX)) : ("nanos: " + this.nanos);
		assert (0 == this.nanos % 100) : ("nanos: " + this.nanos);
		assert (0L == this.utcMillis % 1000L) : ("utcMillis: " + this.utcMillis);
	}

	public static DateTimeOffset valueOf(Timestamp paramTimestamp, int paramInt) {
		return new DateTimeOffset(paramTimestamp, paramInt);
	}

	public static DateTimeOffset valueOf(Timestamp paramTimestamp, Calendar paramCalendar) {
		paramCalendar.setTimeInMillis(paramTimestamp.getTime());

		return new DateTimeOffset(paramTimestamp, (paramCalendar.get(15) + paramCalendar.get(16)) / 60000);
	}

	public String toString() {
		String str1 = this.formattedValue;
		if (null == str1) {
			String str2 = this.minutesOffset < 0 ? String.format(Locale.US, "-%1$02d:%2$02d", new Object[] {
			        Integer.valueOf(-this.minutesOffset / 60), Integer.valueOf(-this.minutesOffset % 60) }) : String.format(Locale.US,
			        "+%1$02d:%2$02d", new Object[] { Integer.valueOf(this.minutesOffset / 60), Integer.valueOf(this.minutesOffset % 60) });

			Calendar localCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT" + str2), Locale.US);

			localCalendar.setTimeInMillis(this.utcMillis);

			assert ((this.nanos >= 0) && (this.nanos <= NANOS_MAX));

			this.formattedValue = (str1 = 0 == this.nanos ? String.format(Locale.US, "%1$tF %1$tT %2$s", new Object[] { localCalendar, str2 })
			        : String.format(Locale.US, "%1$tF %1$tT.%2$s %3$s", new Object[] { localCalendar,
			                BigDecimal.valueOf(this.nanos, 9).stripTrailingZeros().toPlainString().substring(2), str2 }));
		}

		return str1;
	}

	public boolean equals(Object paramObject) {
		if (this == paramObject) {
			return true;
		}

		if (!(paramObject instanceof DateTimeOffset)) {
			return false;
		}

		DateTimeOffset localEdmDateTimeOffset = (DateTimeOffset) paramObject;
		return (this.utcMillis == localEdmDateTimeOffset.utcMillis) && (this.nanos == localEdmDateTimeOffset.nanos)
		        && (this.minutesOffset == localEdmDateTimeOffset.minutesOffset);
	}

	public int hashCode() {
		assert (0L == this.utcMillis % 1000L);
		long l = this.utcMillis / 1000L;

		int i = 571;
		i = 2011 * i + (int) l;
		i = 3217 * i + (int) (l / 60L * 60L * 24L * 365L);

		i = 3919 * i + this.nanos / 100000;
		i = 4463 * i + this.nanos / 1000;
		i = 5227 * i + this.nanos;

		i = 6689 * i + this.minutesOffset;
		i = 7577 * i + this.minutesOffset / 60;

		return i;
	}

	public Timestamp getTimestamp() {
		Timestamp localTimestamp = new Timestamp(this.utcMillis);
		localTimestamp.setNanos(this.nanos);
		return localTimestamp;
	}

	public int getMinutesOffset() {
		return this.minutesOffset;
	}

	public int compareTo(DateTimeOffset paramEdmDateTimeOffset) {
		assert (this.nanos >= 0);
		assert (paramEdmDateTimeOffset.nanos >= 0);

		return this.utcMillis < paramEdmDateTimeOffset.utcMillis ? -1 : this.utcMillis > paramEdmDateTimeOffset.utcMillis ? 1 : this.nanos
		        - paramEdmDateTimeOffset.nanos;
	}

	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	private void readObject(ObjectInputStream paramObjectInputStream) throws InvalidObjectException {
		throw new InvalidObjectException("");
	}

	private static class SerializationProxy implements Serializable {
		private final long utcMillis;
		private final int nanos;
		private final int minutesOffset;
		private static final long serialVersionUID = 664661379547314226L;

		SerializationProxy(DateTimeOffset paramEdmDateTimeOffset) {
			this.utcMillis = paramEdmDateTimeOffset.utcMillis;
			this.nanos = paramEdmDateTimeOffset.nanos;
			this.minutesOffset = paramEdmDateTimeOffset.minutesOffset;
		}

		private Object readResolve() {
			Timestamp localTimestamp = new Timestamp(this.utcMillis);
			localTimestamp.setNanos(this.nanos);
			return new DateTimeOffset(localTimestamp, this.minutesOffset);
		}
	}
}
