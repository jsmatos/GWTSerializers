package com.jsmatos.gwt.client.serialization.json;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.jsmatos.gwt.client.serialization.json.Serializer;
import com.jsmatos.gwt.client.serialization.json.tst.Beanery;
import com.jsmatos.gwt.client.serialization.json.tst.MyBean;
import com.jsmatos.gwt.client.serialization.json.tst.SomeEnum;
import com.jsmatos.gwt.client.serialization.json.tst.SomeImpl;


public class SerializerTest extends GWTTestCase {
    public String getModuleName() {
        return "com.jsmatos.gwt.GWTSerializers";
    }

    public void test_t1(){

        Beanery beanFactory = Beanery.beanFactory;
        MyBean bean = beanFactory.makeBean().as();
        bean.setFoo("Hello, beans");
        bean.setSomeEnum(SomeEnum.enum1);

        SomeImpl si = new SomeImpl();
        bean.setSi(si);

        AutoBean<MyBean> autoBean = AutoBeanUtils.getAutoBean(bean);
        String asJson = AutoBeanCodex.encode(autoBean).getPayload();



//        List<String> list = beanFactory.makeSomeListOfString().as();
//
//        list.add("aaa");
//        list.add("bbb");

//        AutoBean<List<String>> listAutoBean = AutoBeanUtils.getAutoBean(list);
//        asJson = AutoBeanCodex.encode(listAutoBean).getPayload();


//        System.out.println(asJson);
//
//        AutoBean<MyBean> autoBeanCloneAB = AutoBeanCodex.decode(beanFactory, MyBean.class, asJson);
//
//        MyBean autoBeanClone = autoBeanCloneAB.as();
//
//        System.out.println(autoBeanClone.getFoo());
//        System.out.println(autoBeanClone.getSomeEnum());
//        System.out.println(autoBeanClone.getSi());
//
//        Window.alert("pqp!");

        Serializer serializer = GWT.create(Serializer.class);

        String json = serializer.serialize(new SomeInterfaceImpl());
//
//
        System.out.println(json);
//        System.out.println("test_t1");
//        assertTrue(true);



//        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, "/jsongwtproject/products.json");
//
//
//        rb.setCallback(new RequestCallback() {
//            @Override
//            public void onResponseReceived(Request request, Response response) {
//                request.cancel();
//            }
//            @Override
//            public void onError(Request request, Throwable exception) {
//                Window.alert("Error occurred" + exception.getMessage());
//            }
//        });
//        try {
//            rb.send();
//        } catch (RequestException e) {
//
//
//        }
    }



//    public static <T extends RequestFactory> T create( Class<T> requestFactoryClass ) {
//
//
//        ServiceLayer serviceLayer = ServiceLayer.create();
//        SimpleRequestProcessor processor = new SimpleRequestProcessor( serviceLayer );
//        T factory = RequestFactorySource.create(requestFactoryClass);
//        factory.initialize( new SimpleEventBus(), new  InProcessRequestTransport( processor ) );
//        return factory;
//    }

}
