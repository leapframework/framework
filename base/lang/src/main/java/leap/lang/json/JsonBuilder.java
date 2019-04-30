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

package leap.lang.json;

import leap.lang.Beans;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 
 * build json in set properties way:
 * 
 * <code>
 *     JsonBuilder builder = JsonBuilder.create()
 *      .setProperty("query.bool.filter[0].range.timestamp.gte","2017-12-29 00:00:00.000")
 *      .setProperty("query.bool.filter[0].range.timestamp.lt", "2017-12-30 00:00:00.000")
 *      .setProperty("query.bool.filter[1].term.span\\.kind","server")
 *      .setProperty("query.bool.filter[2].term.component","apigateway")
 *      .setProperty("size",0)
 *      .setProperty("aggs.count.terms.field","tags.service.name");
 *     builder.setProperty("query.bool.filter[0].range.timestamp.lt","2017-12-31 00:00:00.000");
 *     JsonObject json = builder.build();
 * </code>
 * 
 * @author kael.
 */
public class JsonBuilder {
    private static final Log log = LogFactory.get(JsonBuilder.class);

    protected Pattern pattern = Pattern.compile("\\[\\d+\\]$");

    private class KV{

        public KV(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        String key;
        Object value;
    }

    public static class KeyParser{
        private final StringBuilder origin;
        private int pos = 0;
        public KeyParser(String origin) {
            this.origin = new StringBuilder(origin);
        }

        public String next(){
            int start = pos;
            boolean escape = false;
            while (pos < origin.length()){
                if(origin.charAt(pos) == '\\'){
                    escape = true;
                    pos ++;
                    continue;
                }
                if(origin.charAt(pos) == '.'){
                    if(pos == start){
                        pos++;
                    }else if(!escape){
                        pos ++;
                        break;
                    }else {
                        origin.deleteCharAt(pos-1);
                    }
                }
                if(escape){
                    escape = false;
                }
                pos ++;
            }
            if(isEnd()){
                return origin.substring(start,pos);
            }
            return origin.substring(start,pos-1);
        }
        public boolean isEnd(){
            return pos >= origin.length();
        }
    }

    private List<KV> list = new ArrayList<>();

    public static JsonBuilder create(){
        return new JsonBuilder();
    }

    public JsonBuilder setProperty(String key, Object value){
        list = list.stream().filter(kv -> !kv.key.equals(key)).collect(Collectors.toList());
        list.add(new KV(key,value));
        return this;
    }

    public JsonObject build(){
        List<KV> flat = flat();
        check(flat);
        Map<String, Object> map = new HashMap<>();
        flat.forEach(kv -> {
            String key = kv.key;
            Object value = kv.value;
            parseKeyValue(new KeyParser(key),value,map);
        });
        return JsonObject.of(map);
    }

    private void parseKeyValue(KeyParser parser, Object value, Map<String, Object> map){
        if(parser.isEnd()){
            return;
        }
        String key = parser.next();
        if(parser.isEnd()){
            Optional<Integer> optional = parseArrayIndex(key);
            if(optional.isPresent()){
                key = key.replaceAll(pattern.pattern(),"");
                ensureListNotNullAndSize(key,map,optional.get()).set(optional.get(),value);
            }else {
                map.put(key,value);
            }
        }else {
            Optional<Integer> optional = parseArrayIndex(key);
            if(optional.isPresent()){
                key = key.replaceAll(pattern.pattern(),"");
                List<Object> list = ensureListNotNullAndSize(key,map,optional.get());
                if(list.get(optional.get()) == null){
                    list.set(optional.get(),new HashMap<String, Object>());
                }
                Map<String, Object> m = (Map<String, Object>) list.get(optional.get());
                parseKeyValue(parser,value,m);
            }else {
                if(null == map.get(key)){
                    map.put(key,new HashMap<>());
                }
                Map<String, Object> m = (Map<String, Object>) map.get(key);
                parseKeyValue(parser,value,m);
            }
        }
    }


    private void check(List<KV> flat){
        Set<String> repeatKey = new HashSet<>();
        Set<String> distinct = new HashSet<>();
        flat.stream().sorted((o1, o2) -> {
            if(o1.key.startsWith(o2.key) || o2.key.startsWith(o1.key)){
                throw new JsonException("conflict key: " + o1.key +" <-> " + o2.key);
            }
            return o1.key.compareTo(o2.key);
        }).map(kv -> kv.key).forEach(s -> {
            if(distinct.contains(s)){
                repeatKey.add(s);
            }else {
                distinct.add(s);
            }
        });
        if(repeatKey.size() > 0){
            flat.stream().sorted(Comparator.comparing(kv -> kv.key))
                    .forEach(kv -> log.error("{} -> {}", kv.key,kv.value));
            throw new JsonException("duplicate key: " + Arrays.toString(repeatKey.toArray()));
        }

    }

    private List<Object> ensureListNotNullAndSize(String key, Map<String, Object> m, int lastIdx){
        if(m.get(key) == null){
            m.put(key,new ArrayList<>());
        }
        List list = (List) m.get(key);
        if(list.size() <= lastIdx){
            do {
                list.add(null);
            }while (list.size() <= lastIdx);
        }
        return list;
    }

    private Optional<Integer> parseArrayIndex(String key){
        Matcher m = pattern.matcher(key);
        if(!m.find()){
            return Optional.empty();
        }
        String idx = m.group();
        return Optional.of(Integer.parseInt(idx.substring(1,idx.length()-1)));
    }

    private List<KV> flat(){
        List<KV> flat = new ArrayList<>();
        list.forEach(kv -> {
            String key = kv.key;
            Object value = kv.value;
            if(value == null || Beans.isSimpleProperty(value.getClass())){
                flat.add(new KV(key,value));
            }else {
                JsonObject json = JsonObject.of(JSON.decodeMap(JSON.encode(value)));
                addFlatList(key,json,flat);
            }
        });
        return flat;
    }

    private void addFlatList(String key, JsonObject json, List<KV> flat){
        json.forEachProperty((s,o) ->{
            if(null == o){
                flat.add(new KV(key+"."+s,null));
                return;
            }
            if(Beans.isSimpleProperty(o.getClass())){
                flat.add(new KV(key+"."+s,o));
                return;
            }
            if(o.getClass().isArray()){
                Object[] arrays = (Object[])o;
                for (int i = 0; i < arrays.length; i ++){
                    addFlatList(key+"." + s + "["+i+"]",json.getObject(s),flat);
                }
                return;
            }
            if(o instanceof List){
                List arrays = (List) o;
                for (int i = 0; i < arrays.size(); i ++){
                    addFlatList(key+"." + s + "["+i+"]",json.getObject(s),flat);
                }
                return;
            }
            addFlatList(key+"."+s,json.getObject(s),flat);
        });
    }
}
