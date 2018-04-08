/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package app.models;

import java.util.List;
import java.util.Map;

/**
 * Created by kael on 2016/12/5.
 */
public class PartialModel {
    private int integer;
    private double doubleNum;
    private Map<String, Object> map;
    private List<String> list;

    public int getInteger() {
        return integer;
    }

    public void setInteger(int integer) {
        this.integer = integer;
    }

    public double getDoubleNum() {
        return doubleNum;
    }

    public void setDoubleNum(double doubleNum) {
        this.doubleNum = doubleNum;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }
}
