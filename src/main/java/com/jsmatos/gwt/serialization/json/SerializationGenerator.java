package com.jsmatos.gwt.serialization.json;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.*;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.jsmatos.gwt.client.serialization.json.*;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

public class SerializationGenerator extends Generator {

    private JClassType serializeInterface;
    private JClassType stringClass;
    //    private SourceWriter srcWriter;
    private String className;
    private TypeOracle typeOracle;
    private Set<String> importsList = new HashSet<String>();
    private ClassSourceFileComposerFactory composerFactory;

    private final Set<String> TYPES = new HashSet<String>();

    private final Map<Class,Class> replaceables = new HashMap<Class, Class>();

    public String generate(TreeLogger logger, GeneratorContext ctx, final String requestedClass) throws UnableToCompleteException {
        //get the type oracle
        typeOracle = ctx.getTypeOracle();
        assert (typeOracle != null);
        serializeInterface = typeOracle.findType(JsonSerializableItems.class.getName());
        assert (serializeInterface != null);
        stringClass = typeOracle.findType(String.class.getName());
        assert (stringClass != null);

        //get class from type oracle
        JClassType serializeClass = typeOracle.findType(requestedClass);

        if (serializeClass == null) {
            logger.log(TreeLogger.ERROR, "Unable to find metadata for type '"
                    + requestedClass + "'", null);
            throw new UnableToCompleteException();
        }

        //create source writer
        String packageName = serializeClass.getPackage().getName();
        className = serializeClass.getSimpleSourceName() + "_TypeSerializer";
        PrintWriter printWriter = ctx.tryCreate(logger, packageName, className);
        if (printWriter == null) {
            return packageName + "." + className;
        }
        composerFactory = new ClassSourceFileComposerFactory(packageName, className);
//        composerFactory.setSuperclass("com.kfuntak.gwt.json.serialization.client.Serializer");
        composerFactory.setSuperclass(requestedClass);

//		// Java imports
        composerFactory.addImport(java.util.Collection.class.getName());
        composerFactory.addImport(List.class.getName());
        composerFactory.addImport(ArrayList.class.getName());
        composerFactory.addImport(java.util.LinkedList.class.getName());
        composerFactory.addImport(java.util.Stack.class.getName());
        composerFactory.addImport(java.util.Vector.class.getName());
        composerFactory.addImport(Set.class.getName());
        composerFactory.addImport(java.util.TreeSet.class.getName());
        composerFactory.addImport(HashSet.class.getName());
        composerFactory.addImport(java.util.LinkedHashSet.class.getName());
        composerFactory.addImport(java.util.SortedSet.class.getName());
        composerFactory.addImport(java.util.Date.class.getName());
//		// GWT imports
        composerFactory.addImport(com.google.gwt.core.client.GWT.class.getName());
        composerFactory.addImport(com.google.gwt.json.client.JSONNull.class.getName());
        composerFactory.addImport(com.google.gwt.json.client.JSONNumber.class.getName());
        composerFactory.addImport(com.google.gwt.json.client.JSONString.class.getName());
        composerFactory.addImport(com.google.gwt.json.client.JSONValue.class.getName());
        composerFactory.addImport(com.google.gwt.json.client.JSONObject.class.getName());
        composerFactory.addImport(com.google.gwt.json.client.JSONArray.class.getName());
        composerFactory.addImport(com.google.gwt.json.client.JSONBoolean.class.getName());
        composerFactory.addImport(com.google.gwt.json.client.JSONParser.class.getName());
        composerFactory.addImport(com.google.gwt.json.client.JSONException.class.getName());
//		// Module imports
        composerFactory.addImport(ObjectSerializer.class.getName());
        composerFactory.addImport(JsonSerializableItems.class.getName());
        composerFactory.addImport(IncompatibleObjectException.class.getName());
        composerFactory.addImport(SerializerHelper.class.getName());
        composerFactory.addImport(DeserializerHelper.class.getName());


        Set<Class> serializableTypes = new HashSet<Class>();

        JClassType[] subTypes = serializeInterface.getSubtypes();
        for (int i = 0; i < subTypes.length; ++i) {
            String clazzName = subTypes[i].getQualifiedSourceName();
            try {
                Class<?> clz = Class.forName(clazzName);

                JsonSerializableItems items = (JsonSerializableItems) clz.newInstance();
                Map<Class, Class> replaceables = items.replaceables();
                if(replaceables!=null){
                    this.replaceables.putAll(replaceables);
                }

                for(Class clazz:items.serializableClasses()){
                    serializableTypes.add(clazz);
                    composerFactory.addImport(clazz.getName().replaceAll("\\$.*",""));
                    TYPES.add(clazz.getName());
                    Method[] methods = clazz.getMethods();
                    for (Method method:methods){
                        clz = method.getReturnType();
                        if(!clz.isPrimitive()){

                            if(clz.isArray()){
                                clz.getComponentType();
                            }else{
                                composerFactory.addImport(clz.getName().replaceAll("\\$.*",""));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }


        StringBuffer buffer = new StringBuffer();

        //create a serializer for each interface that supports Serializable

        subTypes = new JClassType[serializableTypes.size()];
        int i=0;
        for(Class clazz:serializableTypes){
            subTypes[i] = typeOracle.findType(clazz.getName());
            i++;

        }

        for (i = 0; i < subTypes.length; ++i) {
            if (subTypes[i].isAbstract()) {
                continue;
            }
            buffer.append("public class " + subTypes[i].getName() + "_SerializableImpl implements ObjectSerializer{\n");
            buffer.append("public " + subTypes[i].getName() + "_SerializableImpl(){}\n");

            try {
                String defaultSerializationString = generateDefaultSerialization();
                String typeSerializationString = generateTypeSerialization(subTypes[i].getQualifiedSourceName());
                String defaultDeserializationString = generateDefaultDeserialization(subTypes[i].getQualifiedSourceName());
                String tyepDeserializationString = generateTypeDeserialization(subTypes[i].getQualifiedSourceName());

                buffer.append(defaultSerializationString);
                buffer.append("\n");
                buffer.append(typeSerializationString);
                buffer.append("\n");
                buffer.append(defaultDeserializationString);
                buffer.append("\n");
                buffer.append(tyepDeserializationString);
                buffer.append("\n");
                buffer.append("}");
                buffer.append("\n");
                //System.out.println(buffer.toString());
            } catch (NotFoundException e) {
                e.printStackTrace();
            }

            buffer.append("\n");
//					System.out.println(buffer.toString());

        }


        //in the class constructor, add each serializer
        buffer.append("public ").append(className).append("(){\n");
        for (i = 0; i < subTypes.length; ++i) {
            if (subTypes[i].isAbstract()) {
                continue;
            }
            buffer.append("addObjectSerializer(\"").append(subTypes[i].getQualifiedSourceName()).append("\", new ").append(subTypes[i].getName()).append("_SerializableImpl() );\n");
        }

        for(Map.Entry<Class,Class> entry:this.replaceables.entrySet()){
            buffer.append("addObjectSerializer(\"").append(entry.getKey().getName()).append("\", getObjectSerializer(\"").append(entry.getValue().getName()).append("\") );\n");
        }


        buffer.append("\n}");

        String code = buffer.toString();
        logger.log(TreeLogger.Type.ERROR,"\nSOURCE CODE:\n"+code);

        SourceWriter srcWriter = composerFactory.createSourceWriter(ctx, printWriter);
        if (srcWriter == null) {
            return packageName + "." + className;
        }

        srcWriter.println(code);
        srcWriter.commit(logger);


        return packageName + "." + className;
    }

    private String generateTypeDeserialization(String typeName) throws NotFoundException {

        JClassType baseType = typeOracle.getType(typeName);
        String packageName = baseType.getPackage().getName();

        StringBuffer buffer = new StringBuffer();
        buffer.append("public Object deSerialize(JSONValue jsonValue, String className) throws JSONException{");
        buffer.append("\n");

        // Return null if the given object is null
        buffer.append("if((jsonValue == null) || (jsonValue instanceof JSONNull)){");
        buffer.append("\n");
        buffer.append("return null;");
        buffer.append("\n");
        buffer.append("}");
        buffer.append("\n");

        // Throw Incompatible exception is JsonValue is not an instance of
        // JsonObject
        buffer.append("if(!(jsonValue instanceof JSONObject)){");
        buffer.append("\n");
        buffer.append("throw new IncompatibleObjectException(jsonValue+\" --- not an instance of JSONObject\");");
        buffer.append("\n");
        buffer.append("}");
        buffer.append("\n");

        // Initialise JsonObject then
        String baseTypeName = baseType.getSimpleSourceName();
        buffer.append("JSONObject jsonObject=(JSONObject)jsonValue;");
        buffer.append("\n");

        JEnumType enumType = baseType.isEnum();

        if(enumType!=null){
            buffer.append(baseTypeName + " mainResult;");
        }else{
            buffer.append(baseTypeName + " mainResult=new " + baseTypeName + "();");
        }

        buffer.append("\n");
        buffer.append("Serializer serializer;");
        buffer.append("\n");
        buffer.append("JSONArray inputJsonArray=null;");
        buffer.append("\n");
        buffer.append("int inpJsonArSize=0;");
        buffer.append("\n");
        buffer.append("JSONValue fieldJsonValue=null;");
        buffer.append("\n");

        // Start deSerialisation
        List<JField> allFields = new ArrayList<JField>();
        JField[] fields = baseType.getFields();
        for (JField field : fields) {
            if (!field.isStatic() && !field.isTransient()) {
                allFields.add(field);
            }
        }
        if (TYPES.contains(baseType.getQualifiedSourceName())){
//        if (baseType.isAssignableTo(typeOracle.getType(JsonSerializableItems.class.getName()))) {
            boolean flag = true;
            JClassType superClassType = baseType;
            while (flag) {
                superClassType = superClassType.getSuperclass();
//                if (superClassType.isAssignableTo(typeOracle.getType(JsonSerializableItems.class.getName()))) {
                if(TYPES.contains(superClassType.getQualifiedSourceName())){
                    JField[] subClassFields = superClassType.getFields();
                    for (JField subClassField : subClassFields) {
                        if (!subClassField.isStatic() && !subClassField.isTransient()) {
                            allFields.add(subClassField);
                        }
                    }
                } else {
                    flag = false;
                }
            }
        }
        fields = new JField[allFields.size()];
        allFields.toArray(fields);

        for (JField field : fields) {
            JType fieldType = field.getType();
            String fieldName = field.getName();
            String fieldNameForGS = getNameForGS(fieldName);
            buffer.append("\nfieldJsonValue=jsonObject.get(\"" + fieldName + "\");");
            buffer.append("\n");

            enumType = fieldType.isEnum();
            if(enumType!=null){

                buffer.append("mainResult.set" + fieldNameForGS + "(Enum.valueOf("+enumType.getName()+".class, DeserializerHelper.getString(fieldJsonValue)));");
                buffer.append("\n");
            }else if (fieldType.isPrimitive() != null) {
                JPrimitiveType fieldPrimitiveType = (JPrimitiveType) fieldType;
                JClassType fieldBoxedType = typeOracle.getType(fieldPrimitiveType.getQualifiedBoxedSourceName());
                if (fieldBoxedType.getQualifiedSourceName().equals("java.lang.Short")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getShort(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldBoxedType.getQualifiedSourceName().equals("java.lang.Byte")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getByte(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldBoxedType.getQualifiedSourceName().equals("java.lang.Long")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getLong(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldBoxedType.getQualifiedSourceName().equals("java.lang.Integer")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getInt(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldBoxedType.getQualifiedSourceName().equals("java.lang.Float")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getFloat(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldBoxedType.getQualifiedSourceName().equals("java.lang.Double")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getDouble(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldBoxedType.getQualifiedSourceName().equals("java.lang.Boolean")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getBoolean(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldBoxedType.getQualifiedSourceName().equals("java.lang.Character")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getShort(fieldJsonValue));");
                    buffer.append("\n");
                }
            } else {
                JClassType fieldClassType = (JClassType) fieldType;
                if (fieldClassType.getQualifiedSourceName().equals("java.lang.Short")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getShort(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Byte")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getByte(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Long")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getLong(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Integer")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getInt(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Float")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getFloat(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Double")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getDouble(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Boolean")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getBoolean(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Character")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getShort(fieldJsonValue));");
                    buffer.append("\n");
                } else if (fieldClassType.getQualifiedSourceName().equals("java.util.Date")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getDate(fieldJsonValue));");
                    buffer.append("\n");
                } else if (TYPES.contains((fieldClassType.getQualifiedSourceName()))) {
                    importsList.add(fieldClassType.getQualifiedSourceName());
//                    buffer.append("serializer = GWT.create(Serializer.class);");
                    buffer.append("serializer = "+className+".this;");
                    buffer.append("\n");
                    buffer.append("mainResult.set" + fieldNameForGS + "((" + fieldClassType.getSimpleSourceName() + ")serializer.deSerialize(fieldJsonValue, \"" + fieldClassType.getQualifiedSourceName() + "\"));");
                    buffer.append("\n");
                } else if (fieldClassType.isAssignableTo(typeOracle.getType("java.util.Collection"))) {
                    deserializeCollection(buffer, fieldClassType, fieldNameForGS, fieldName);
                } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.String")) {
                    buffer.append("mainResult.set" + fieldNameForGS + "(DeserializerHelper.getString(fieldJsonValue));");
                    buffer.append("\n");
                }
            }
        }

        buffer.append("return mainResult;");
        buffer.append("\n");
        buffer.append("}");
        buffer.append("\n");

        return buffer.toString();
    }

    private void deserializeCollection(StringBuffer buffer, JClassType fieldClassType, String fieldNameForGS, String fieldName) throws NotFoundException {
        // Return null if JSON object is null
        buffer.append("if(fieldJsonValue!=null && !(fieldJsonValue instanceof JSONNull)){");

        buffer.append("\n");
        buffer.append("mainResult.set" + fieldNameForGS + "(null);");
        buffer.append("\n");

        // Throw Incompatible exception if the JSON object is not a collection
        buffer.append("if(!(fieldJsonValue instanceof JSONArray)){");
        buffer.append("\n");
        buffer.append("throw new IncompatibleObjectException(fieldJsonValue+\" --- no an instance of JSONArray\");");
        buffer.append("\n");
        buffer.append("}");
        buffer.append("\n");

        // Start deSerilisation
        buffer.append("inputJsonArray=(JSONArray)fieldJsonValue;");
        buffer.append("\n");
        buffer.append("inpJsonArSize=inputJsonArray.size();");
        buffer.append("\n");

        String fieldTypeQualifiedName = fieldClassType.getQualifiedSourceName();
        JParameterizedType parameterizedType = (JParameterizedType) fieldClassType;
        fieldClassType = parameterizedType.getTypeArgs()[0];
        String parameterSimpleName = fieldClassType.getSimpleSourceName();
        String fieldColName = fieldName + "Col";// Field Collection Result
        // Object Name
        importsList.add(fieldClassType.getQualifiedSourceName());
        if (fieldTypeQualifiedName.equals("java.util.List") || fieldTypeQualifiedName.equals("java.util.ArrayList")) {
            buffer.append("ArrayList<" + parameterSimpleName + "> " + fieldColName + " = new ArrayList<" + parameterSimpleName + ">();");
            buffer.append("\n");
        } else if (fieldTypeQualifiedName.equals("java.util.Set") || fieldTypeQualifiedName.equals("java.util.HashSet")) {
            buffer.append("HashSet<" + parameterSimpleName + "> " + fieldColName + " = new HashSet<" + parameterSimpleName + ">();");
            buffer.append("\n");
        } else if (fieldTypeQualifiedName.equals("java.util.SortedSet") || fieldTypeQualifiedName.equals("java.util.TreeSet")) {
            buffer.append("TreeSet<" + parameterSimpleName + "> " + fieldColName + " = new TreeSet<" + parameterSimpleName + ">();");
            buffer.append("\n");
        } else if (fieldTypeQualifiedName.equals("java.util.LinkedList")) {
            buffer.append("LinkedList<" + parameterSimpleName + "> " + fieldColName + " = new LinkedList<" + parameterSimpleName + ">();");
            buffer.append("\n");
            buffer.append("mainResult.set" + fieldNameForGS + "(" + fieldColName + ");");
            buffer.append("\n");
        } else if (fieldTypeQualifiedName.equals("java.util.Stack")) {
            buffer.append("Stack<" + parameterSimpleName + "> " + fieldColName + " = new Stack<" + parameterSimpleName + ">();");
            buffer.append("\n");
        } else if (fieldTypeQualifiedName.equals("java.util.Vector")) {
            buffer.append("Vector<" + parameterSimpleName + "> " + fieldColName + " = new Vector<" + parameterSimpleName + ">();");
            buffer.append("\n");
        } else if (fieldTypeQualifiedName.equals("java.util.LinkedHashSet")) {
            buffer.append("LinkedHashSet<" + parameterSimpleName + "> " + fieldColName + "=new LinkedHashSet<" + parameterSimpleName + ">();");
            buffer.append("\n");
        }
        buffer.append("for(int ij=0;ij<inpJsonArSize;ij++){");
        // DeSerialise individual elements
        buffer.append("fieldJsonValue=inputJsonArray.get(ij);");
        if (fieldClassType.getQualifiedSourceName().equals("java.lang.Short")) {
            buffer.append(fieldColName + ".add(DeserializerHelper.getShort(fieldJsonValue));");
            buffer.append("\n");
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Byte")) {
            buffer.append(fieldColName + ".add(DeserializerHelper.getByte(fieldJsonValue));");
            buffer.append("\n");
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Long")) {
            buffer.append(fieldColName + ".add(DeserializerHelper.getLong(fieldJsonValue));");
            buffer.append("\n");
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Integer")) {
            buffer.append(fieldColName + ".add(DeserializerHelper.getInt(fieldJsonValue));");
            buffer.append("\n");
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Float")) {
            buffer.append(fieldColName + ".add(DeserializerHelper.getFloat(fieldJsonValue));");
            buffer.append("\n");
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Double")) {
            buffer.append(fieldColName + ".add(DeserializerHelper.getDouble(fieldJsonValue));");
            buffer.append("\n");
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Boolean")) {
            buffer.append(fieldColName + ".add(DeserializerHelper.getBoolean(fieldJsonValue));");
            buffer.append("\n");
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Character")) {
            buffer.append(fieldColName + ".add(DeserializerHelper.getShort(fieldJsonValue));");
            buffer.append("\n");
        } else if (fieldClassType.getQualifiedSourceName().equals("java.util.Date")) {
            buffer.append(fieldColName + ".add(DeserializerHelper.getDate(fieldJsonValue));");
            buffer.append("\n");
//        } else if (fieldClassType.isAssignableTo(typeOracle.getType(JsonSerializableItems.class.getName()))) {
        } else if (TYPES.contains(fieldClassType.getQualifiedSourceName())) {
            importsList.add(fieldClassType.getQualifiedSourceName());
            buffer.append("\n");
            buffer.append("serializer = "+className+".this;");
            buffer.append("\n");
            buffer.append(fieldColName + ".add((" + fieldClassType.getSimpleSourceName() + ")serializer.deSerialize(fieldJsonValue, \"" + fieldClassType.getQualifiedSourceName() + "\"));");
            buffer.append("\n");
        } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.String")) {
            buffer.append(fieldColName + ".add(DeserializerHelper.getString(fieldJsonValue));");
            buffer.append("\n");
        }
        buffer.append("}");
        buffer.append("\n");
        buffer.append("mainResult.set" + fieldNameForGS + "(" + fieldColName + ");");
        buffer.append("\n");

        buffer.append("\n");
        buffer.append("}");

    }

    private String generateDefaultDeserialization(String className) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("public Object deSerialize(String jsonString, String className) throws JSONException{");
        buffer.append("\n");
        buffer.append("return deSerialize(JSONParser.parseStrict(jsonString), \"" + className + "\");");
        buffer.append("\n");
        buffer.append("}");
        buffer.append("\n");
        return buffer.toString();
    }

    private String generateTypeSerialization(String typeName) throws NotFoundException {

        JClassType baseType = typeOracle.getType(typeName);
        String packageName = baseType.getPackage().getName();

        StringBuffer buffer = new StringBuffer();
        buffer.append("public JSONValue serializeToJson(Object object){");
        buffer.append("\n");

        // Return JSONNull instance if object is null
        buffer.append("if(object==null){");
        buffer.append("\n");
        buffer.append("return JSONNull.getInstance();");
        buffer.append("\n");
        buffer.append("}");
        buffer.append("\n");

        // Throw Incompatible Exception if object is not of the type it claims
        // to be
        buffer.append("if(!(object instanceof " + baseType.getSimpleSourceName() + ")){");
        buffer.append("\n");
        buffer.append("throw new IncompatibleObjectException(object+\" --- not an instance of "+baseType.getQualifiedSourceName()+"\");");
        buffer.append("\n");
        buffer.append("}");
        buffer.append("\n");

        // Initialise result object
        buffer.append("JSONObject mainResult=new JSONObject();");
        buffer.append("\n");
        buffer.append("JSONValue jsonValue=null;");
        buffer.append("\n");
        buffer.append("JSONArray jsonResultArray=null;");
        buffer.append("\n");
        buffer.append("int index=0;");
        buffer.append("\n");
        buffer.append("Serializer serializer=null;");
        buffer.append("\n");
        buffer.append("Object fieldValue=null;");
        buffer.append("\n");
        buffer.append(baseType.getSimpleSourceName() + " mainVariable=(" + baseType.getSimpleSourceName() + ")object;");
        buffer.append("\n");

        // Serialise fields
        List<JField> allFields = new ArrayList<JField>();
        JField[] fields = baseType.getFields();
        for (JField field : fields) {
            if (!field.isStatic() && !field.isTransient()) {
                allFields.add(field);
            }
        }

        if(TYPES.contains(baseType.getQualifiedSourceName())){
//        if (baseType.isAssignableTo(typeOracle.getType(JsonSerializableItems.class.getName()))) {
            boolean flag = true;
            JClassType superClassType = baseType;
            while (flag) {
                superClassType = superClassType.getSuperclass();
                if(TYPES.contains(superClassType.getQualifiedSourceName())){
//                if (superClassType.isAssignableTo(typeOracle.getType(JsonSerializableItems.class.getName()))) {
                    JField[] subClassFields = superClassType.getFields();
                    for (JField subClassField : subClassFields) {
                        if (!subClassField.isStatic() && !subClassField.isTransient()) {
                            allFields.add(subClassField);
                        }
                    }
                } else {
                    flag = false;
                }
            }
        }
        fields = new JField[allFields.size()];
        allFields.toArray(fields);
        buffer.append("if(mainVariable!=null){\n");
        for (JField field : fields) {
            JType fieldType = field.getType();
            String fieldName = field.getName();
            String fieldNameForGS = getNameForGS(fieldName);
            // Get field value for object
            buffer.append("fieldValue=mainVariable.get" + fieldNameForGS + "();\n");
            buffer.append("\n");

            JEnumType enumType = fieldType.isEnum();

            if(enumType!=null){
                buffer.append("if(fieldValue!=null){\n");
                buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getEnum((Enum)fieldValue);");
                buffer.append("\n");
                buffer.append("mainResult.put(\"" + fieldName + "\",jsonValue);");
                buffer.append("\n");
                buffer.append("}\n");

            }else if (fieldType.isPrimitive() != null) {
                JPrimitiveType fieldPrimitiveType = (JPrimitiveType) fieldType;
                JClassType fieldBoxedType = typeOracle.getType(fieldPrimitiveType.getQualifiedBoxedSourceName());
                if (fieldBoxedType.getQualifiedSourceName().equals("java.lang.Boolean")) {
                    buffer.append("if(fieldValue!=null){\n");
                    buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getBoolean((Boolean)fieldValue);");
                    buffer.append("\n");
                    buffer.append("mainResult.put(\"" + fieldName + "\",jsonValue);");
                    buffer.append("\n");
                    buffer.append("}\n");
                } else if (fieldBoxedType.getQualifiedSourceName().equals("java.lang.Character")) {
                    buffer.append("if(fieldValue!=null){\n");
                    buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getChar((Character)fieldValue);");
                    buffer.append("\n");
                    buffer.append("mainResult.put(\"" + fieldName + "\",jsonValue);");
                    buffer.append("\n");
                    buffer.append("}\n");
                } else if (fieldBoxedType.isAssignableTo(typeOracle.getType("java.lang.Number"))) {
                    buffer.append("if(fieldValue!=null){\n");
                    buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getNumber((Number)fieldValue);");
                    buffer.append("\n");
                    buffer.append("mainResult.put(\"" + fieldName + "\",jsonValue);");
                    buffer.append("\n");
                    buffer.append("}\n");
                }
            } else {
                JClassType fieldClassType = (JClassType) fieldType;
                if (fieldClassType.isAssignableTo(typeOracle.getType("java.util.Collection"))) {
                    // Serialise collection
                    JParameterizedType parameterizedType = (JParameterizedType) fieldClassType;
                    fieldClassType = parameterizedType.getTypeArgs()[0];
                    importsList.add(fieldClassType.getQualifiedSourceName());
                    String fieldSimpleName = fieldClassType.getSimpleSourceName();
                    buffer.append("\n");
                    buffer.append("if(fieldValue != null){");
                    buffer.append("\n");
                    buffer.append("Collection<" + fieldSimpleName + "> " + fieldSimpleName.toLowerCase() + "ColValue=(Collection<" + fieldSimpleName + ">)fieldValue;");
                    buffer.append("\n");
                    buffer.append("jsonResultArray=new JSONArray();");
                    buffer.append("\n");
                    buffer.append("index=0;");
                    buffer.append("\n");
                    buffer.append("for(" + fieldSimpleName + " dummy : " + fieldSimpleName.toLowerCase() + "ColValue){");
                    buffer.append("\n");
                    if (fieldClassType.getQualifiedSourceName().equals("java.lang.String")) {
                        buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getString((String)dummy);");
                        buffer.append("\n");
                        buffer.append("jsonResultArray.set(index++,jsonValue);");
                        buffer.append("\n");
                    } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Boolean")) {
                        buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getBoolean((Boolean)dummy);");
                        buffer.append("\n");
                        buffer.append("jsonResultArray.set(index++,jsonValue);");
                        buffer.append("\n");
                    } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Character")) {
                        buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getChar((Character)dummy);");
                        buffer.append("\n");
                        buffer.append("jsonResultArray.set(index++,jsonValue);");
                        buffer.append("\n");
                    } else if (fieldClassType.isAssignableTo(typeOracle.getType("java.lang.Number"))) {
                        buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getNumber((Number)dummy);");
                        buffer.append("\n");
                        buffer.append("jsonResultArray.set(index++,jsonValue);");
                        buffer.append("\n");
                    } else if (fieldClassType.getQualifiedSourceName().equals("java.util.Date")) {
                        buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getDate((Date)dummy);");
                        buffer.append("\n");
                        buffer.append("jsonResultArray.set(index++,jsonValue);");
                        buffer.append("\n");
                    } else if (TYPES.contains(fieldClassType.getQualifiedSourceName())) {
                        // TODO: Put alternalive to importsList
                        //importsList.add(fieldClassType.getQualifiedSourceName());
//                        buffer.append("serializer = GWT.create("+Serializer.class.getName()+".class);");
                        buffer.append("serializer = "+className+".this;");
                        buffer.append("\n");
                        buffer.append("jsonResultArray.set(index++,serializer.serializeToJson(dummy));");
                        buffer.append("\n");
                    }
                    buffer.append("}");
                    buffer.append("\n");
                    buffer.append("mainResult.put(\"" + fieldName + "\",jsonResultArray);");
                    buffer.append("\n");
                    buffer.append("}");
                    buffer.append("\n");
                } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.String")) {
                    buffer.append("if(fieldValue!=null){\n");
                    buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getString((String)fieldValue);");
                    buffer.append("\n");
                    buffer.append("mainResult.put(\"" + fieldName + "\",jsonValue);");
                    buffer.append("\n");
                    buffer.append("}\n");
                } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Boolean")) {
                    buffer.append("if(fieldValue!=null){\n");
                    buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getBoolean((Boolean)fieldValue);");
                    buffer.append("\n");
                    buffer.append("mainResult.put(\"" + fieldName + "\",jsonValue);");
                    buffer.append("\n");
                    buffer.append("}\n");
                } else if (fieldClassType.getQualifiedSourceName().equals("java.lang.Character")) {
                    buffer.append("if(fieldValue!=null){\n");
                    buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getChar((Character)fieldValue);");
                    buffer.append("\n");
                    buffer.append("mainResult.put(\"" + fieldName + "\",jsonValue);");
                    buffer.append("\n");
                    buffer.append("}\n");
                } else if (fieldClassType.isAssignableTo(typeOracle.getType("java.lang.Number"))) {
                    buffer.append("if(fieldValue!=null){\n");
                    buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getNumber((Number)fieldValue);");
                    buffer.append("\n");
                    buffer.append("mainResult.put(\"" + fieldName + "\",jsonValue);");
                    buffer.append("\n");
                    buffer.append("}\n");
                } else if (fieldClassType.getQualifiedSourceName().equals("java.util.Date")) {
                    buffer.append("if(fieldValue!=null){\n");
                    buffer.append("jsonValue="+SerializerHelper.class.getSimpleName()+".getDate((Date)fieldValue);");
                    buffer.append("\n");
                    buffer.append("mainResult.put(\"" + fieldName + "\",jsonValue);");
                    buffer.append("\n");
                    buffer.append("}\n");
//                } else if (fieldClassType.isAssignableTo(typeOracle.getType(JsonSerializableItems.class.getName()))) {
                } else if (TYPES.contains(fieldClassType.getQualifiedSourceName())) {
                    importsList.add(fieldClassType.getQualifiedSourceName());
//                    buffer.append("serializer = GWT.create("+Serializer.class.getName()+".class);");
                    buffer.append("serializer = "+className+".this;");
                    buffer.append("\n");
                    buffer.append("if(fieldValue!=null){\n");
                    buffer.append("mainResult.put(\"" + fieldName + "\",serializer.serializeToJson(fieldValue));");
                    buffer.append("\n");
                    buffer.append("}\n");
                }

            }
        }

        // Put class type for compatibility with flex JSON [de]serialisation
//        buffer.append("mainResult.put(\"class\",new JSONString(\"" + baseType.getQualifiedSourceName() + "\"));");
        buffer.append("}\n");

        // Return statement
        buffer.append("return mainResult;");
        buffer.append("\n");
        buffer.append("}");
        buffer.append("\n");
        return buffer.toString();
    }

    private String generateDefaultSerialization() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("public String serialize(Object pojo){");
        buffer.append("\n");
        buffer.append("return serializeToJson(pojo).toString();");
        buffer.append("\n");
        buffer.append("}");
        buffer.append("\n");
        return buffer.toString();
    }

    private static String getNameForGS(String name) {
        StringBuffer buffer = new StringBuffer(name);
        buffer.setCharAt(0, new String(new char[]{name.charAt(0)}).toUpperCase().charAt(0));
        return buffer.toString();
    }
}
