package com.jsmatos.gwt.client.serialization.json;

public class SerializationException extends RuntimeException {

    /**
	 *
	 */
	private static final long serialVersionUID = 9069457742212754344L;

	public SerializationException() {
    }

    public SerializationException(String arg0) {
        super(arg0);
    }

    public SerializationException(Throwable arg0) {
        super(arg0);
    }

    public SerializationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
