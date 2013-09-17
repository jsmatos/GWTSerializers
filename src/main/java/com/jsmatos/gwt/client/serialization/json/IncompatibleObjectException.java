package com.jsmatos.gwt.client.serialization.json;

import com.google.gwt.json.client.JSONException;

public class IncompatibleObjectException extends JSONException {

    private static final long serialVersionUID = 1L;

    public IncompatibleObjectException() {
    }

    public IncompatibleObjectException(String message) {
        super(message);
    }

}
