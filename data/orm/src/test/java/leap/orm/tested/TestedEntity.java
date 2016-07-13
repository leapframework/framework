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
package leap.orm.tested;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import leap.orm.annotation.*;

@Entity
public class TestedEntity {
	@Id
	@Column(length = 50)
	private String    	  id;
	private char      	  char1;
	private Character 	  char2;
	private String	  	  string1;
	private String	  	  _string2;
	private boolean		  bool1;
	private Boolean		  bool2;
	private short		  short1;
	private Short		  short2;
	private int			  int1;
	private Integer		  int2;
	private long		  long1;
	private Long		  long2;
	private float		  float1;
	private Float		  float2;
	private double		  double1;
	private Double		  double2;
	private BigInteger	  bigint;
	private BigDecimal	  decimal;
	private byte		  byte1;
	private Byte		  byte2;
	private Date		  date1;
	private java.sql.Date date2;
	private Calendar	  date3;
	private Time		  time1;
	private Timestamp	  timestamp1;
	
	@Column(type=ColumnType.TIMESTAMP)
	private Long          timestamp2;
	
	@Column(type=ColumnType.CLOB)
	private String		  clob1;
	private byte[]        blob1;
	private Byte[]		  blob2;
	
	@NonColumn
	private String	nonColumn;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public char getChar1() {
		return char1;
	}

	public void setChar1(char char1) {
		this.char1 = char1;
	}

	public Character getChar2() {
		return char2;
	}

	public void setChar2(Character char2) {
		this.char2 = char2;
	}

	public String getString1() {
		return string1;
	}

	public void setString1(String string1) {
		this.string1 = string1;
	}

	public String getString2() {
		return _string2;
	}

	public void setString2(String _string2) {
		this._string2 = _string2;
	}
	
	public boolean isBool1() {
		return bool1;
	}

	public void setBool1(boolean bool1) {
		this.bool1 = bool1;
	}

	public Boolean getBool2() {
		return bool2;
	}

	public void setBool2(Boolean bool2) {
		this.bool2 = bool2;
	}

	public short getShort1() {
		return short1;
	}

	public void setShort1(short short1) {
		this.short1 = short1;
	}

	public Short getShort2() {
		return short2;
	}

	public void setShort2(Short short2) {
		this.short2 = short2;
	}

	public int getInt1() {
		return int1;
	}

	public void setInt1(int int1) {
		this.int1 = int1;
	}

	public Integer getInt2() {
		return int2;
	}

	public void setInt2(Integer int2) {
		this.int2 = int2;
	}

	public long getLong1() {
		return long1;
	}

	public void setLong1(long long1) {
		this.long1 = long1;
	}

	public Long getLong2() {
		return long2;
	}

	public void setLong2(Long long2) {
		this.long2 = long2;
	}

	public float getFloat1() {
		return float1;
	}

	public void setFloat1(float float1) {
		this.float1 = float1;
	}

	public Float getFloat2() {
		return float2;
	}

	public void setFloat2(Float float2) {
		this.float2 = float2;
	}

	public double getDouble1() {
		return double1;
	}

	public void setDouble1(double double1) {
		this.double1 = double1;
	}

	public Double getDouble2() {
		return double2;
	}

	public void setDouble2(Double double2) {
		this.double2 = double2;
	}

	public BigInteger getBigint() {
		return bigint;
	}

	public void setBigint(BigInteger bigint) {
		this.bigint = bigint;
	}

	public BigDecimal getDecimal() {
		return decimal;
	}

	public void setDecimal(BigDecimal decimal) {
		this.decimal = decimal;
	}

	public byte getByte1() {
		return byte1;
	}

	public void setByte1(byte byte1) {
		this.byte1 = byte1;
	}

	public Byte getByte2() {
		return byte2;
	}

	public void setByte2(Byte byte2) {
		this.byte2 = byte2;
	}
	
	public Date getDate1() {
		return date1;
	}

	public void setDate1(Date date1) {
		this.date1 = date1;
	}

	public java.sql.Date getDate2() {
		return date2;
	}

	public void setDate2(java.sql.Date date2) {
		this.date2 = date2;
	}
	
	public Calendar getDate3() {
		return date3;
	}

	public void setDate3(Calendar date3) {
		this.date3 = date3;
	}

	public Time getTime1() {
		return time1;
	}

	public void setTime1(Time time1) {
		this.time1 = time1;
	}

	public Timestamp getTimestamp1() {
		return timestamp1;
	}

	public void setTimestamp1(Timestamp timestamp1) {
		this.timestamp1 = timestamp1;
	}
	
	public Long getTimestamp2() {
        return timestamp2;
    }

    public void setTimestamp2(Long timestamp2) {
        this.timestamp2 = timestamp2;
    }

    public String getClob1() {
		return clob1;
	}

	public void setClob1(String clob1) {
		this.clob1 = clob1;
	}

	public byte[] getBlob1() {
		return blob1;
	}

	public void setBlob1(byte[] stream1) {
		this.blob1 = stream1;
	}

	public Byte[] getBlob2() {
		return blob2;
	}

	public void setBlob2(Byte[] stream2) {
		this.blob2 = stream2;
	}

	public String getNonColumn() {
		return nonColumn;
	}

	public void setNonColumn(String nonColumn) {
		this.nonColumn = nonColumn;
	}
}