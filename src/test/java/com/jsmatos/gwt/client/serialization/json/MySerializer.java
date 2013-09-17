package com.jsmatos.gwt.client.serialization.json;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jsmatos.gwt.client.serialization.json.JsonSerializableItems;

public class MySerializer implements JsonSerializableItems{
    @Override
    public Set<Class> serializableClasses() {
        return new HashSet<Class>(){{

            add(SomeInterfaceImpl.class);

        }};
    }

	@Override
	public Map<Class, Class> replaceables() {
		// TODO Auto-generated method stub
		return null;
	}


}
