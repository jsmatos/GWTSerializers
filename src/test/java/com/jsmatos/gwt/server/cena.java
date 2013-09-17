package com.jsmatos.gwt.server;


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import com.jsmatos.gwt.client.serialization.json.SomeInterface;

public class cena {

    public static void main(String ... args){

        System.out.println(buildImpl("aaa", List.class));



    }



    private static String buildImpl(String name, Class<?> clazz){
        if(!clazz.isInterface()){
            throw new RuntimeException(clazz+" is not an interface");
        }



        StringBuilder sb = new StringBuilder(" public ").append(clazz.getName()).append(" ").append(name).append(" = new ");
        sb.append(clazz.getName()).append("(){\n\n");

        for(Method method:clazz.getMethods()){

            sb.append(toString(method)).append("{\n");
            Class<?> returnType = method.getReturnType();
            if(!Void.TYPE.equals(returnType)){

                if(returnType.isPrimitive()){
                    if(method.getParameterTypes().length>0){
                        sb.append("\tthrow new RuntimeException(\"this method can't be invoked\");\n");
                    }else{

                    }
                }else {

                    if(method.getParameterTypes().length>0){
                        sb.append("\tthrow new RuntimeException(\"this method can't be invoked\");\n");
                    }else{

                    }
                }
            }

            sb.append("}\n\n");
        }

        sb.append("}\n");


        return sb.toString();
    }


    private static String defaultValue(Class clazz){
        if(float.class.equals(clazz)){
            return String.valueOf((float)0);
        }

        if(boolean.class.equals(clazz)){
            return String.valueOf((boolean)false);
        }

        if(byte.class.equals(clazz)){
            return String.valueOf((byte)0);
        }
        if(short.class.equals(clazz)){
            return String.valueOf((short)0);
        }
        if(int.class.equals(clazz)){
            return String.valueOf((int)0);
        }
        if(long.class.equals(clazz)){
            return String.valueOf((long)0);
        }
        if(char.class.equals(clazz)){
            return String.valueOf((char)0);
        }
        if(double.class.equals(clazz)){
            return String.valueOf((double)0);
        }


        throw new RuntimeException("unable to find default value for type "+clazz);
    }


    public static String toString(Method method) {
        try {
            StringBuffer sb = new StringBuffer();
            int mod = method.getModifiers() ^ Modifier.ABSTRACT;
            if (mod != 0) {
                sb.append(Modifier.toString(mod) + " ");
            }
            sb.append(getTypeName(method.getReturnType()) + " ");
//            sb.append(getTypeName(method.getDeclaringClass()) + ".");
            sb.append(method.getName() + "(");
            Class[] params = method.getParameterTypes();
            for (int j = 0; j < params.length; j++) {
                sb.append(getTypeName(params[j])).append(" p").append(j);
                if (j < (params.length - 1))
                    sb.append(",");
            }
            sb.append(")");
            Class[] exceptions = method.getExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append(exceptions[k].getName());
                    if (k < (exceptions.length - 1))
                        sb.append(",");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }

    static String getTypeName(Class type) {
        if (type.isArray()) {
            try {
                Class cl = type;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuffer sb = new StringBuffer();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) { /*FALLTHRU*/ }
        }
        return type.getName();
    }

    SomeInterface s = new SomeInterface(){

        @Override
        public String getA() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void setA(String a) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getB() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void setB(String b) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getInt1() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void setInt1(int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public long getLong1() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void setLong1() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public float getFloat1() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }
    };


}
