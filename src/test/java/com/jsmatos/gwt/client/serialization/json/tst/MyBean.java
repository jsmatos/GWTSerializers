package com.jsmatos.gwt.client.serialization.json.tst;

public interface MyBean {

    SomeEnum getSomeEnum();
    void setSomeEnum(SomeEnum e);

    String getFoo();
    void setFoo(String foo);

    SomeImpl getSi();

    void setSi(SomeImpl si);
}