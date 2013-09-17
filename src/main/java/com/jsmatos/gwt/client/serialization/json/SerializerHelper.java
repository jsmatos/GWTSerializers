package com.jsmatos.gwt.client.serialization.json;

import com.google.gwt.json.client.*;

import java.util.Date;


public class SerializerHelper {

    public static JSONValue getEnum(Enum e){
        if(e==null){
            return JSONNull.getInstance();
        }
        return getString(e.name());
    }

    public static JSONValue getString(String string) {
        if (string == null) {
            return JSONNull.getInstance();
        }
        return new JSONString(string);
    }

    public static JSONValue getBoolean(Boolean boolValue) {
        if (boolValue == null) {
            return JSONNull.getInstance();
        }
        return JSONBoolean.getInstance(boolValue);
    }

    public static JSONValue getNumber(Number number) {
        if (number == null) {
            return JSONNull.getInstance();
        }
        return new JSONNumber(number.doubleValue());
    }

    public static JSONValue getChar(Character character) {
        if (character == null) {
            return JSONNull.getInstance();
        }
        return new JSONString(new String(new char[]{character}));
    }

    public static JSONValue getDate(Date date) {
        if (date == null) {
            return JSONNull.getInstance();
        }
        return new JSONNumber(date.getTime());
    }
}
