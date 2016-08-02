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
package leap.orm.tested.model;

import leap.lang.time.StopWatch;
import leap.orm.OrmTestCase;
import leap.orm.model.Model;
import org.junit.Ignore;

@Ignore
public class ModelPerfMain extends OrmTestCase {

	public static void main(String[] args) throws Exception {
		Thread.sleep(100);
		
		int count=100000;
		
		for(int i=0;i<10;i++){
			System.out.println("============new perf============");
			perfNewNormalPerson(count);
			perfNewModelPerson(count);
			System.out.println("");
			System.out.println("");			
		}
		
		for(int i=0;i<10;i++){
			System.out.println("============setter perf============");
			perfSetter(new NormalPerson(), count);
			perfSetter(new ModelPerson(), count);
			perfSet(new ModelPerson(), count);
			System.out.println("");
			System.out.println("");
		}
		
		for(int i=0;i<10;i++){
			System.out.println("============getter perf============");
			perfGetter(new NormalPerson(), count);
			perfGetter(new ModelPerson(), count);
			perfGet(new ModelPerson(), count);
			System.out.println("");
			System.out.println("");
		}
    }
	
	private static void perfNewNormalPerson(int count){
		StopWatch sw = StopWatch.startNew();
		for(int i=0;i<count;i++){
			new NormalPerson();
		}
		System.out.println("new(NormalPerson) : " + sw.getElapsedMilliseconds() + "ms");
	}
	
	
	private static void perfNewModelPerson(int count){
		StopWatch sw = StopWatch.startNew();
		for(int i=0;i<count;i++){
			new ModelPerson();
		}
		System.out.println("new(ModelPerson) : " + sw.getElapsedMilliseconds() + "ms");
	}
	
	private static void perfSetter(PersonInterface person,int count){
		StopWatch sw = StopWatch.startNew();
		for(int i=0;i<count;i++){
			person.setName("name");
		}
		System.out.println("setter(" + person.getClass().getSimpleName() + ") : " + sw.getElapsedMilliseconds() + "ms");
	}
	
	private static void perfSet(ModelPerson person,int count){
		StopWatch sw = StopWatch.startNew();
		for(int i=0;i<count;i++){
			person.set("name", "name");
		}
		System.out.println("set(" + person.getClass().getSimpleName() + ") : " + sw.getElapsedMilliseconds() + "ms");
	}
	
	private static void perfGetter(PersonInterface person,int count){
		StopWatch sw = StopWatch.startNew();
		for(int i=0;i<count;i++){
			person.getName();
		}
		System.out.println("getter(" + person.getClass().getSimpleName() + ") : " + sw.getElapsedMilliseconds() + "ms");
	}
	
	private static void perfGet(ModelPerson person,int count){
		StopWatch sw = StopWatch.startNew();
		for(int i=0;i<count;i++){
			person.get("name");
		}
		System.out.println("get(" + person.getClass().getSimpleName() + ") : " + sw.getElapsedMilliseconds() + "ms");
	}
	
	public interface PersonInterface {
		String getName();
		void setName(String name);
	}
	
	public static class NormalPerson implements PersonInterface {
		private String  name;
		private boolean eanbled;
		private String  field1;
		private String  field2;
		private String  field3;
		private String  field4;
		private String  field5;
		private String  field6;
		private String  field7;
		private String  field8;
		private String  field9;
		private String  field10;
		private String  field11;
		private String  field12;
		private String  field13;
		private String  field14;
		private String  field15;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isEanbled() {
			return eanbled;
		}

		public void setEanbled(boolean eanbled) {
			this.eanbled = eanbled;
		}

		public String getField1() {
			return field1;
		}

		public void setField1(String field1) {
			this.field1 = field1;
		}

		public String getField2() {
			return field2;
		}

		public void setField2(String field2) {
			this.field2 = field2;
		}

		public String getField3() {
			return field3;
		}

		public void setField3(String field3) {
			this.field3 = field3;
		}

		public String getField4() {
			return field4;
		}

		public void setField4(String field4) {
			this.field4 = field4;
		}

		public String getField5() {
			return field5;
		}

		public void setField5(String field5) {
			this.field5 = field5;
		}

		public String getField6() {
			return field6;
		}

		public void setField6(String field6) {
			this.field6 = field6;
		}

		public String getField7() {
			return field7;
		}

		public void setField7(String field7) {
			this.field7 = field7;
		}

		public String getField8() {
			return field8;
		}

		public void setField8(String field8) {
			this.field8 = field8;
		}

		public String getField9() {
			return field9;
		}

		public void setField9(String field9) {
			this.field9 = field9;
		}

		public String getField10() {
			return field10;
		}

		public void setField10(String field10) {
			this.field10 = field10;
		}

		public String getField11() {
			return field11;
		}

		public void setField11(String field11) {
			this.field11 = field11;
		}

		public String getField12() {
			return field12;
		}

		public void setField12(String field12) {
			this.field12 = field12;
		}

		public String getField13() {
			return field13;
		}

		public void setField13(String field13) {
			this.field13 = field13;
		}

		public String getField14() {
			return field14;
		}

		public void setField14(String field14) {
			this.field14 = field14;
		}

		public String getField15() {
			return field15;
		}

		public void setField15(String field15) {
			this.field15 = field15;
		}
	}
	
	public static class ModelPerson extends Model implements PersonInterface {
		
		private String  name;
		private boolean eanbled;
		private String  field1;
		private String  field2;
		private String  field3;
		private String  field4;
		private String  field5;
		private String  field6;
		private String  field7;
		private String  field8;
		private String  field9;
		private String  field10;
		private String  field11;
		private String  field12;
		private String  field13;
		private String  field14;
		private String  field15;		

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isEanbled() {
			return eanbled;
		}

		public void setEanbled(boolean eanbled) {
			this.eanbled = eanbled;
		}

		public String getField1() {
			return field1;
		}

		public void setField1(String field1) {
			this.field1 = field1;
		}

		public String getField2() {
			return field2;
		}

		public void setField2(String field2) {
			this.field2 = field2;
		}

		public String getField3() {
			return field3;
		}

		public void setField3(String field3) {
			this.field3 = field3;
		}

		public String getField4() {
			return field4;
		}

		public void setField4(String field4) {
			this.field4 = field4;
		}

		public String getField5() {
			return field5;
		}

		public void setField5(String field5) {
			this.field5 = field5;
		}

		public String getField6() {
			return field6;
		}

		public void setField6(String field6) {
			this.field6 = field6;
		}

		public String getField7() {
			return field7;
		}

		public void setField7(String field7) {
			this.field7 = field7;
		}

		public String getField8() {
			return field8;
		}

		public void setField8(String field8) {
			this.field8 = field8;
		}

		public String getField9() {
			return field9;
		}

		public void setField9(String field9) {
			this.field9 = field9;
		}

		public String getField10() {
			return field10;
		}

		public void setField10(String field10) {
			this.field10 = field10;
		}

		public String getField11() {
			return field11;
		}

		public void setField11(String field11) {
			this.field11 = field11;
		}

		public String getField12() {
			return field12;
		}

		public void setField12(String field12) {
			this.field12 = field12;
		}

		public String getField13() {
			return field13;
		}

		public void setField13(String field13) {
			this.field13 = field13;
		}

		public String getField14() {
			return field14;
		}

		public void setField14(String field14) {
			this.field14 = field14;
		}

		public String getField15() {
			return field15;
		}

		public void setField15(String field15) {
			this.field15 = field15;
		}
	}
}
