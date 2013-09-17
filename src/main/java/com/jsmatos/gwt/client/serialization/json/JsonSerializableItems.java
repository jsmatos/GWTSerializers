package com.jsmatos.gwt.client.serialization.json;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface JsonSerializableItems extends Serializable {

    public Set<Class> serializableClasses();

    public Map<Class,Class> replaceables();

}
