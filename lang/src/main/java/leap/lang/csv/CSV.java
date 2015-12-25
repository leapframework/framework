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
package leap.lang.csv;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import leap.lang.Args;
import leap.lang.Exceptions;
import leap.lang.exception.NestedIOException;
import leap.lang.io.IO;


//Most codes in this package from apache commons-csv
public class CSV {
	
	private static final CSVFormat DEFAULT_FORMAT             = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
	private static final CSVFormat DEFAULT_FORMAT_SKIP_HEADER = DEFAULT_FORMAT.withSkipHeaderRecord(true);
	
	public static List<String[]> read(Reader reader) throws NestedIOException{
		return readList(reader, DEFAULT_FORMAT);
	}
	
	public static List<String[]> readSkipHeader(Reader reader) {
		return readList(reader, DEFAULT_FORMAT_SKIP_HEADER);
	}
	
	protected static List<String[]> readList(Reader reader,CSVFormat format) throws NestedIOException {
		Args.notNull(reader,"reader");
		CSVParser parser = null;
		try{
			parser = new CSVParser(reader, format);
			return parser.getRecords1();
		}catch(IOException e){
			throw Exceptions.wrap(e);
		}finally{
			IO.close(reader);
			IO.close(parser);
		}
	}
	
	public static void read(Reader reader,CsvProcessor processor) throws NestedIOException {
		CSVParser parser = null;
		try{
			parser = new CSVParser(reader, DEFAULT_FORMAT);
			
			String[] row;
			int rownum = 0;
			while((row = parser.nextRecord1()) != null){
				rownum++;
				processor.process(rownum,row);
			}
		}catch(IOException e){
			throw Exceptions.wrap(e);
		}catch(Exception e){
			throw Exceptions.uncheck(e);
		}finally{
			IO.close(reader);
			IO.close(parser);
		}
	}
	
	public static void write(Writer writer,List<Object[]> rows) throws NestedIOException {
		CSVPrinter printer = null;
		try{
			printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
			
			for(Object[] row : rows){
				printer.printRecord(row);
			}
		}catch(IOException e){
			throw Exceptions.wrap(e);
		}finally{
			IO.close(writer);
			IO.close(printer);
		}
	}
	
	public static List<String[]> decode(String csv){
		return read(new StringReader(csv));
	}
	
	public static String encode(List<Object[]> rows) {
		StringWriter sw = new StringWriter();
		write(sw,rows);
		return sw.toString();
	}
	
	protected CSV(){
		
	}

}
