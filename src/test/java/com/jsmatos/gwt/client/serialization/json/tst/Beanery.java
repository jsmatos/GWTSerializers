package com.jsmatos.gwt.client.serialization.json.tst;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Beanery extends AutoBeanFactory {
    Beanery beanFactory = GWT.create(Beanery.class);


    AutoBean<MyBean> makeBean();
    AutoBean<MyBiggerBean> makeBigBean();

    class builder{

        MyBean makeBean(){
            return beanFactory.makeBean().as();
        }

    }

    class Serializer{
        public String enconde(MyBean myBean){
            AutoBean<MyBean> autoBean = AutoBeanUtils.getAutoBean(myBean);
            return AutoBeanCodex.encode(autoBean).getPayload();
        }

        public <T> T decode(String asJson, Class<T> clazz){
            AutoBean<T> autoBean = AutoBeanCodex.decode(Beanery.beanFactory, clazz, asJson);

            return autoBean.as();
        }



    }

    builder builder = new builder();
}


class Main{

    public static void main(String ... args){

        Set<Class> classes = new HashSet<Class>();
        classes.add(List.class);
        classes.add(Map.class);

        System.out.println(generateBeanery("aahaha",classes));

    }

    private static String generateBeanery(String beaneryName, Set<Class> classes){
        StringBuilder sb = new StringBuilder("interface ");
        sb.append(beaneryName);
        sb.append(" extends ").append(AutoBeanFactory.class.getName()).append(" {\n\n");

        sb.append("\t").append(beaneryName).append(" beanFactory = ").append(GWT.class.getName()).append(".create(").append(beaneryName).append(".class);\n\n");

        for(Class clazz:classes){
           if(clazz.isInterface()){

               sb.append("\t").append(AutoBean.class.getName()).append("<").append(clazz.getName()).append("> ");
               sb.append(replaceDots(clazz)).append("();\n");


           }
        }


        sb.append("\n}");





        return sb.toString();
    }

    private static String replaceDots(Class clazz){
        return clazz.getName().replaceAll("\\.","_");
    }


    interface aahaha extends com.google.web.bindery.autobean.shared.AutoBeanFactory {

        aahaha beanFactory = com.google.gwt.core.client.GWT.create(aahaha.class);

        com.google.web.bindery.autobean.shared.AutoBean<java.util.Map> java_util_Map();
        com.google.web.bindery.autobean.shared.AutoBean<java.util.List> java_util_List();

    }

}