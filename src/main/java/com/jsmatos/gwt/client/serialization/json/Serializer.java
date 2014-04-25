package com.jsmatos.gwt.client.serialization.json;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Serializer extends SerializerHelper{
    private static final Logger logger = Logger.getLogger(Serializer.class.getName());
    private static final Map<String,ObjectSerializer> SERIALIZABLE_TYPES = new HashMap<String,ObjectSerializer>();

    protected Serializer() {
    }

    static protected String getTypeName(Object obj) {
        return obj.getClass().getName();
    }

    public static String JSON_Beautify(String input){
        JavaScriptObject javaScriptObject = JsonUtils.safeEval(input);
        return JSON_Beautify(javaScriptObject);
    }

    private static native String JSON_Beautify(JavaScriptObject input)/*-{
        return JSON.stringify(input, null, 4);
    }-*/;

    protected void addObjectSerializer(String name, ObjectSerializer obj) {
        SERIALIZABLE_TYPES.put(name, obj);
    }

    protected ObjectSerializer getObjectSerializer(String name) {
        return SERIALIZABLE_TYPES.get(name);
    }

    public String serialize(Object pojo, Class<?> clazz) {
        if(pojo==null){
            return JSONNull.getInstance().toString();
        }else {

            String name = clazz.getName();
            ObjectSerializer serializer = getObjectSerializer(name);
            if (serializer == null) {
                throw new SerializationException("Can't find object serializer for " + name);
            }
            return serializer.serialize(pojo);
        }

    }

    public String serialize(Object pojo) {
        String json = serialize(pojo,pojo.getClass());

        try{
            json = JSON_Beautify(json);
        }catch (Exception e){
            logger.log(Level.SEVERE,e.getMessage(),e);
        }

        return json;
    }

    public JSONValue serializeToJson(Map<String, ?> pojo) {
        return serializeMapToJson(pojo);
    }

    public JSONValue serializeToJson(Object pojo) {
        if(pojo==null){
            return JSONNull.getInstance();
        }

        if(pojo instanceof String){
            return new JSONString((String) pojo);
        }

        if(pojo instanceof Character){
            return getChar((Character) pojo);
        }

        if(pojo instanceof Date){
            return getDate((Date) pojo);
        }

        if(pojo instanceof Enum){
            return getEnum((Enum) pojo);
        }

        if(pojo instanceof Boolean){
            return getBoolean((Boolean)pojo);
        }

        if (pojo instanceof Number){
            return getNumber(((Number)pojo).doubleValue());
        }

        if (pojo instanceof Map){
            return serializeToJson((Map<String, ?>) pojo);
        }

        if (pojo instanceof Collection){
            return serializeCollectionToJson((Collection) pojo);
        }

        String name = getTypeName(pojo);
        ObjectSerializer serializer = getObjectSerializer(name);
        if (serializer == null) {
            throw new SerializationException("Can't find object serializer for " + name);
        }
        return serializer.serializeToJson(pojo);


    }

    public <T> T deSerialize(JSONValue jsonValue, Class<T> clazz) throws JSONException {
        return (T) deSerialize(jsonValue,clazz.getName());
    }

    public Object deSerialize(JSONValue jsonValue, String className) throws JSONException {
        ObjectSerializer serializer = getObjectSerializer(className);
        if (serializer == null) {
            System.out.println(SERIALIZABLE_TYPES);
            throw new SerializationException("Can't find object serializer for " + className);
        }
        return serializer.deSerialize(jsonValue, className);
    }

    public Object deSerialize(String jsonString, String className) throws JSONException {
        if(jsonString==null){
            return null;
        }else {
            ObjectSerializer serializer = getObjectSerializer(className);
            if (serializer == null) {
                throw new SerializationException("Can't find object serializer for " + className);
            }
            return serializer.deSerialize(jsonString, className);
        }
    }

    public <T> T deSerialize(String jsonString, Class<T> clazz){
        return (T) deSerialize(jsonString,clazz.getName());
    }

    public <T> void fillMapWith(Map<String,T> map, JSONValue value, Class<T> clazz){
        JSONObject object = value.isObject();
        if(object!=null){
            for (String key:object.keySet()){
                map.put(key, deSerialize(object.get(key),clazz));
            }
        }

    }

    public <T> Map<String,T> fromMap(JSONValue value, Class<T> clazz){
        if(value==null){
            return null;
        }
        Map<String,T> result = new HashMap<String, T>();
        fillMapWith(result,value,clazz);
        return result;
    }

    public <T> void fillCollectionWith(Collection<T> collection, JSONValue value, Class<T> clazz){
        JSONArray array = value.isArray();
        if(array!=null) {
            int size = array.size();
            for (int i = 0; i < size; i++) {
                collection.add(deSerialize(array.get(i), clazz));
            }
        }
    }

    public <T> Collection<T> fromCollection(JSONValue value, Class<T> clazz){
        if(value==null){
            return null;
        }
        Collection<T> result = new ArrayList<T>();
        fillCollectionWith(result,value,clazz);
        return result;
    }

    public JSONValue serializeMapToJson(Map<String,?> map){
        if(map==null){
            return JSONNull.getInstance();
        }else {
            JSONObject object = new JSONObject();
            for (Map.Entry<String,?> entry:map.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();
                if(key!=null){
                    object.put(key,serializeToJson(value));
                }
            }

            return object;
        }
    }

    public <T> JSONValue serializeCollectionToJson(Collection<T> collection){
        if(collection==null){
            return JSONNull.getInstance();
        }else {
            JSONArray array = new JSONArray();
            int i=0;
            for (T t:collection){
                JSONValue value = serializeToJson(t);
                array.set(i,value);
            }

            return array;
        }
    }
}
