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
package leap.db.platform;

import leap.db.DbMetadataReader;

public abstract class GenericDbMetadataReaderBase implements DbMetadataReader {
	
	public static final String SCHEMA_CATALOG   = "TABLE_CATALOG";
	public static final String SCHEMA_NAME      = "TABLE_SCHEM";
	
	public static final String TABlE_CATALOG    = "TABLE_CAT";
	public static final String TABLE_SCHEMA     = "TABLE_SCHEM";
	public static final String TABLE_NAME       = "TABLE_NAME";
	public static final String TABLE_TYPE       = "TABLE_TYPE";
	public static final String REMARKS          = "REMARKS";
	
	public static final String FKTABLE_CATALOG  = "PKTABLE_CAT";
	public static final String FKTABLE_SCHEMA   = "FKTABLE_SCHEM";
	public static final String FKTABLE_NAME     = "FKTABLE_NAME";
	public static final String FK_NAME          = "FK_NAME";
	
	
	public static final String PKTABLE_CATALOG  = "PKTABLE_CAT";
	public static final String PKTABLE_SCHEMA   = "PKTABLE_SCHEM";
	public static final String PKTABLE_NAME     = "PKTABLE_NAME";
	public static final String PK_NAME          = "PK_NAME";
	public static final String PKCOLUMN_NAME    = "PKCOLUMN_NAME";
	public static final String FKCOLUMN_NAME    = "FKCOLUMN_NAME";
	public static final String KEY_SEQ          = "KEY_SEQ";
	
	public static final String COLUMN_NAME      = "COLUMN_NAME";
	public static final String COLUMN_DEFAULT   = "COLUMN_DEF";
	public static final String COLUMN_TYPE      = "DATA_TYPE";
	public static final String COLUMN_SIZE      = "COLUMN_SIZE";
	public static final String COLUMN_PRECISION = "NUM_PREC_RADIX";
	public static final String COLUMN_SCALE     = "DECIMAL_DIGITS";
	public static final String COLUMN_NULLABLE  = "IS_NULLABLE";
	
	public static final String INDEX_NAME       = "INDEX_NAME";
	public static final String ORDINAL_POSITION = "ORDINAL_POSITION";
	public static final String NON_UNIQUE       = "NON_UNIQUE";
	
	public static final String SEQUENCE_CATALOG   = "SEQ_CAT";
	public static final String SEQUENCE_SCHEMA    = "SEQ_SCHEM";
	public static final String SEQUENCE_NAME      = "SEQ_NAME";
	public static final String SEQUENCE_MINVALUE  = "SEQ_MINVALUE";
	public static final String SEQUENCE_MAXVALUE  = "SEQ_MAXVALUE";
	public static final String SEQUENCE_INCREMENT = "SEQ_INCREMENT";
	public static final String SEQUENCE_START     = "SEQ_START";
	public static final String SEQUENCE_CACHE     = "SEQ_CACHE";
	public static final String SEQUENCE_CYCLE     = "SEQ_CYCLE";

}
