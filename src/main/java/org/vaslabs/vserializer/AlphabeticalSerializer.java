package org.vaslabs.vserializer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.vaslabs.vserializer.SerializationUtils.*;

/**
 * Created by vnicolaou on 02/05/16.
 */
public class AlphabeticalSerializer extends StringSerializer {

    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null)
            return new byte[0];
        if (obj instanceof String)
            return super.serialize(obj);
        if (obj.getClass().isArray()) {
            boolean isPrimitive = SerializationUtils.enumTypes.containsKey(obj.getClass());
            if (isPrimitive) {
                return SerializationUtils.toBytes(obj);
            }
        }
        Class clazz = obj.getClass();
        if (clazz.isEnum() || SerializationUtils.isEnum(clazz)) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(4);
            int ordinal = 0;
            try {
                ordinal = getOrdinal(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
            byteBuffer.putInt(ordinal);
            return byteBuffer.array();
        }
        final Field[] fields = getAllFields(obj);
        final int size = computeSize(fields, obj);

        ByteBuffer byteBuffer = ByteBuffer.allocate(size);


        try {
            putIn(byteBuffer, fields, obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new byte[0];
        }

        return byteBuffer.array();
    }

    @Override
    public <T> byte[] serialize(List<T> list) {
        Object[] arrayFromList = list.toArray();
        return serialize(arrayFromList);
    }

    protected int computeSize(Field[] fields, Object obj) {
        return SerializationUtils.calculateSize(fields, obj);
    }

    @Override
    public <T> byte[] serialize(T[] objects) {
        if (objects.length == 0)
            return new byte[0];
        final int totalSize = SerializationUtils.calculateNonPrimitiveArraySize(objects);
        ByteBuffer byteBuffer = ByteBuffer.allocate(totalSize);
        byteBuffer.putInt(objects.length);
        for (T obj : objects) {
            try {
                if (obj != null) {
                    byteBuffer.put((byte) 1);
                    putIn(byteBuffer, getAllFields(obj), obj);
                } else {
                    byteBuffer.put((byte) -1);
                }
            } catch (IllegalAccessException e) {
                return new byte[0];
            }
        }
        return byteBuffer.array();
    }

    @Override
    public <T> T deserialise(byte[] data, Class<T> clazz) {
        if (clazz.equals(String.class))
            return super.deserialise(data, clazz);
        if (clazz.isArray()) {
            boolean isPrimitive = SerializationUtils.enumTypes.containsKey(clazz);
            if (isPrimitive) {
                return deserialisePrimitiveArray(data, clazz);
            } else {
                return deserialiseArray(data, clazz);
            }
        }
        if (clazz.isEnum()) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            try {
                return generateEnum(clazz, byteBuffer.getInt());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Field[] fields = getAllFields(clazz);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        T obj = null;
        try {
            obj = SerializationUtils.instantiate(clazz);
            obj = convert(byteBuffer, fields, obj);
        } catch (Exception e) {
            return obj;
        }
        return obj;
    }

    @Override
    public <T> List<T> deserialise(byte[] data, Class<List> listClass, Class<T> parametarizedClass) {
        T[] modelArray = (T[]) Array.newInstance(parametarizedClass, 0);
        T[] array = (T[]) deserialiseArray(data, modelArray.getClass());
        return Arrays.asList(array);
    }

    protected <T> T deserialisePrimitiveArray(byte[] data, Class<T> clazz) {
        int typeSize = SerializationUtils.sizes.get(clazz);
        switch (SerializationUtils.enumTypes.get(clazz)) {
            case INT: {
                int[] array = new int[data.length/typeSize];
                SerializationUtils.fromBytes(data, array);
                return (T) array;
            }
            case LONG:{
                long[] array = new long[data.length/typeSize];
                SerializationUtils.fromBytes(data, array);
                return (T) array;
            }
            case SHORT:{
                short[] array = new short[data.length/typeSize];
                SerializationUtils.fromBytes(data, array);
                return (T) array;
            }
            case CHAR:{
                char[] array = new char[data.length/typeSize];
                SerializationUtils.fromBytes(data, array);
                return (T) array;
            }
            case BOOLEAN:{
                boolean[] array = new boolean[data.length/typeSize];
                SerializationUtils.fromBytes(data, array);
                return (T) array;
            }
            case BYTE:{
                byte[] array = new byte[data.length/typeSize];
                SerializationUtils.fromBytes(data, array);
                return (T) array;
            } case FLOAT:{
                float[] array = new float[data.length/typeSize];
                SerializationUtils.fromBytes(data, array);
                return (T) array;
            } case DOUBLE: {
                double[] array = new double[data.length/typeSize];
                SerializationUtils.fromBytes(data, array);
                return (T) array;
            }

        }
        return null;
    }

    private <T> T deserialiseArray(byte[] data, Class<T> clazz) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        final int arraySize = data.length == 0 ? 0 : byteBuffer.getInt();
        Class type = clazz.getComponentType();
        T[] objects = (T[]) Array.newInstance(type, arraySize);
        final Field[] fields = getAllFields(type);
        for (int i = 0; i < objects.length; i++) {
            boolean objectIsNull = byteBuffer.get() == -1;
            if (objectIsNull) {
                objects[i] = null;
                continue;
            }
            T obj = null;
            try {
                if (SerializationUtils.primitiveWrappers.containsKey(type)) {
                    objects[i] = (T) SerializationUtils.instantiatePrimitiveWrapper(type, byteBuffer);
                    continue;
                }
                obj = (T) SerializationUtils.instantiate(type);
                obj = convert(byteBuffer, fields, obj);
                objects[i] = obj;
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        return (T) objects;
    }

    protected  <T> T convert(ByteBuffer byteBuffer, Field[] fields, T obj) throws NoSuchMethodException, InvocationTargetException, InstantiationException {
        Arrays.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field lhs, Field rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        for (Field field : fields) {
            if (skipField(field))
                continue;
            try {
                SerializationUtils.arrangeField(field, obj);
                convert(byteBuffer, field, obj);
            } catch (Exception e) {
                return null;
            }
        }
        return obj;
    }

    protected <T> void convert(ByteBuffer byteBuffer, Field field, T obj) throws IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException, NoSuchFieldException {
        Class fieldType = field.getType();
        if (skipField(field))
            return;
        if (fieldType.isArray()) {
            convertArray(byteBuffer, field, obj);
            return;
        }
        if (fieldType.isEnum()) {
            try {
                convertEnum(byteBuffer, field, obj);
            } catch (Exception e) {
                return;
            }
            return;
        }

        PrimitiveType primitiveType = SerializationUtils.enumTypes.get(fieldType);
        if (primitiveType == null) {
            if (String.class.equals(field.getType())) {
                this.convertString(byteBuffer, field, obj);
                return;
            }
            boolean isNull = -1 == byteBuffer.get();
            if (isNull) {
                field.set(obj, null);
                return;
            } else {

                final Object innerObject;
                if (primitiveWrappers.containsKey(field.getType()))  {
                    innerObject = SerializationUtils.instantiatePrimitiveWrapper(field.getType(), byteBuffer);
                    field.set(obj, innerObject);
                }
                else{
                    innerObject = SerializationUtils.instantiate(field.getType());
                    field.set(obj, innerObject);
                    convert(byteBuffer, getAllFields(innerObject), innerObject);
                }
                return;
            }
        }
        switch (primitiveType) {
            case INT: {
                int value = byteBuffer.getInt();
                field.setInt(obj, value);
                return;
            }
            case LONG: {
                long value = byteBuffer.getLong();
                field.setLong(obj, value);
                return;
            }
            case SHORT: {
                short value = byteBuffer.getShort();
                field.setShort(obj, value);
                return;
            }
            case CHAR: {
                char value = byteBuffer.getChar();
                field.setChar(obj, value);
                return;
            }
            case BYTE: {
                byte value = byteBuffer.get();
                field.setByte(obj, value);
                return;
            }
            case BOOLEAN: {
                byte value = byteBuffer.get();
                field.setBoolean(obj, value == 1);
                return;
            }
            case FLOAT: {
                float value = byteBuffer.getFloat();
                field.setFloat(obj, value);
                return;
            }
            case DOUBLE: {
                double value = byteBuffer.getDouble();
                field.setDouble(obj, value);
                return;
            }
            default:
                throw new IllegalArgumentException(field.getType().toString());
        }
    }

    private <T> void convertEnum(ByteBuffer byteBuffer, Field field, T obj) throws Exception {
        byte ordinal = byteBuffer.get();
        if (ordinal < 0) {
            field.set(obj, null);
        }
        Object enumObj = generateEnum(field.getType(), ordinal);
        field.set(obj, enumObj);
    }


    protected <T> void convertArray(ByteBuffer byteBuffer, Field field, T obj) throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        Class fieldType = field.getType();
        if (!SerializationUtils.enumTypes.containsKey(fieldType)) {
            convertNonPrimitiveArray(byteBuffer, field, obj);
            return;
        }
        final int arrayLength = byteBuffer.getInt();
        if (arrayLength == -1) {
            field.set(obj, null);
            return;
        }
        switch (SerializationUtils.enumTypes.get(fieldType)) {
            case INT: {
                int[] array = new int[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.getInt();}
                field.set(obj, array);
                return;
            }
            case LONG: {
                long[] array = new long[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.getLong();}
                field.set(obj, array);
                return;
            }
            case SHORT: {
                short[] array = new short[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.getShort();}
                field.set(obj, array);
                return;
            }
            case CHAR: {
                char[] array = new char[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.getChar();}
                field.set(obj, array);
                return;
            }case BOOLEAN: {
                boolean[] array = new boolean[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.get() == 1;}
                field.set(obj, array);
                return;
            }
            case BYTE: {
                byte[] array = new byte[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.get();}
                field.set(obj, array);
                return;
            } case FLOAT: {
                float[] array = new float[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.getFloat();}
                field.set(obj, array);
                return;
            } case DOUBLE: {
                double[] array = new double[arrayLength];
                for (int i = 0; i<arrayLength; i++) { array[i] = byteBuffer.getDouble();}
                field.set(obj, array);
                return;
            }
        }
    }

    private <T> void convertNonPrimitiveArray(ByteBuffer byteBuffer, final Field field, T object) throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        if (skipField(field))
            return;
        final int arraySize = byteBuffer.getInt();
        if (arraySize == -1)
            return;
        Class type = field.getType().getComponentType();
        T[] objects = (T[]) Array.newInstance(type, arraySize);
        final Field[] fields = getAllFields(type);
        for (int i = 0; i < objects.length; i++) {
            if (byteBuffer.get() == -1) {
                objects[i] = null;
                continue;
            }
            T obj = null;
            if (type.isEnum()) {
                obj = (T) generateEnum(type, byteBuffer.get());
            } else {
                obj = (T) SerializationUtils.instantiate(type);
                obj = convert(byteBuffer, fields, obj);

            }
            objects[i] = obj;
        }
        SerializationUtils.arrangeField(field, object);
        field.set(object, objects);
    }

    private <T> T generateEnum(Class<T> enumType, int ordinal) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        T[] enums =  enumType.getEnumConstants();
        return enums[ordinal];
    }

    protected void putIn(ByteBuffer byteBuffer, Field[] fields, Object obj) throws IllegalAccessException {
        Arrays.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field lhs, Field rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });

        for (Field field : fields) {
            if (skipField(field))
                continue;
            try {
                SerializationUtils.arrangeField(field, obj);
                putIn(byteBuffer, field, obj);
            } catch (NoSuchFieldException nsfe) {

            }
            field.setAccessible(false);
        }
    }

    protected void putIn(ByteBuffer byteBuffer, Field field, Object obj) throws IllegalAccessException {
        if (obj == null)
            return;
        Class type = field.getType();
        if (skipField(field))
            return;
        if (type.isArray()) {
            putArrayIn(byteBuffer, field, obj);
            return;
        }
        if (type.isEnum()) {
            try {
                int ordinal = getOrdinal(obj, field);
                byteBuffer.put((byte)ordinal);
                return;
            } catch (Exception e) {
                byteBuffer.put((byte) -1);
                return;
            }
        }
        PrimitiveType primitiveType = SerializationUtils.enumTypes.get(type);
        if (primitiveType == null) {
            if (String.class.equals(type)) {
                this.insertString(byteBuffer, field, obj);
                return;
            }
            Object fieldObject = field.get(obj);
            if (fieldObject == null) {
                byteBuffer.put((byte) -1);
                return;
            } else {
                byteBuffer.put((byte) 1);
                if (primitiveWrappersSizes.containsKey(type)) {
                    SerializationUtils.writePrimitiveWrapperValue(fieldObject, byteBuffer);
                } else
                    putIn(byteBuffer, getAllFields(fieldObject), fieldObject);
                return;
            }
        }
        switch (primitiveType) {
            case INT:
                byteBuffer.putInt(field.getInt(obj));
                return;
            case LONG:
                byteBuffer.putLong(field.getLong(obj));
                return;
            case SHORT:
                byteBuffer.putShort(field.getShort(obj));
                return;
            case CHAR:
                byteBuffer.putChar(field.getChar(obj));
                return;
            case BYTE:
                byteBuffer.put(field.getByte(obj));
                return;
            case BOOLEAN:
                byteBuffer.put((byte) (field.getBoolean(obj) ? 1 : 0));
                return;
            case FLOAT:
                byteBuffer.putFloat(field.getFloat(obj));
                return;
            case DOUBLE:
                byteBuffer.putDouble(field.getDouble(obj));
                return;
            default:
                throw new IllegalArgumentException(field.getType().toString());
        }
    }

    private int getOrdinal(Object obj, Field field) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        SerializationUtils.arrangeField(field, obj);
        Object enumObject = field.get(obj);
        if (enumObject == null)
            return -1;
        return getOrdinal(enumObject);
    }

    private int getOrdinal(Object enumObject) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method ordinalMethod = enumObject.getClass().getMethod("ordinal");
        return (int) ordinalMethod.invoke(enumObject);
    }

    private void putArrayIn(ByteBuffer byteBuffer, Field field, Object obj) {
        try {
            Object array = field.get(obj);
            if (array == null) {
                byteBuffer.putInt(-1);
                return;
            }
            int arrayLength = SerializationUtils.findArrayLength(field, obj);
            byteBuffer.putInt(arrayLength);
            insertArrayValues(byteBuffer, field, obj);
        } catch (Exception e) {
            return;
        }

    }

    private void insertArrayValues(ByteBuffer byteBuffer, Field field, Object obj) throws IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException {
        Class fieldType = field.getType();
        if (!SerializationUtils.enumTypes.containsKey(fieldType)) {
            insertArrayValuesNonPrimitive(byteBuffer, field, obj);
            return;
        }
        byte[] bytes = SerializationUtils.toBytes(field.get(obj));
        byteBuffer.put(bytes);
    }

    private void insertArrayValuesNonPrimitive(ByteBuffer byteBuffer, Field field, Object obj) throws IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException {
        Object[] objects = (Object[]) field.get(obj);
        if (objects == null || objects.length == 0)
            return;

        Field[] fields = getAllFields(objects[0]);
        for (Object object : objects) {
            if (object == null) {
                byteBuffer.put((byte) -1);
            } else {
                byteBuffer.put((byte) 1);
                final boolean isEnum = SerializationUtils.isEnum(object.getClass());
                if (isEnum) {
                    putEnum(byteBuffer, object);
                    continue;
                }
                putIn(byteBuffer, fields, object);
            }
        }
    }

    private void putEnum(ByteBuffer byteBuffer, Object obj) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        final int ordinal = getOrdinal(obj);
        byteBuffer.put((byte) ordinal);
    }

}

abstract class StringSerializer implements VSerializer {
    private static Field stringField = null;
    static {
        try {
            stringField = String.class.getDeclaredField("value");
            stringField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    @Override
    public <T> byte[] serialize(T myTestObject) {

        try {
            char[] chars = (char[]) stringField.get(myTestObject);
            return toBytes(chars);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return new byte[0];

    }

    private byte[] toBytes(char[] chars) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(chars.length*2);
        for (char c : chars) {
            byteBuffer.putChar(c);
        }
        return byteBuffer.array();
    }

    private char[] toChars(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        char[] chars = new char[bytes.length / 2];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = byteBuffer.getChar();
        }
        return chars;
    }

    @Override
    public <T> T deserialise(byte[] data, Class<T> clazz) {
        if (!clazz.equals(String.class)) {
            throw new IllegalArgumentException("Only Strings are supported");
        }
        return (T) new String(toChars(data));
    }

    protected <T> void convertString(ByteBuffer byteBuffer, Field field, T obj) throws IllegalAccessException {
        final int stringLength = byteBuffer.getInt();
        if (stringLength == -1) {
            field.set(obj, null);
            return;
        }
        final char[] stringChars = new char[stringLength];
        for (int i = 0; i < stringLength; i++) {
            stringChars[i] = byteBuffer.getChar();
        }
        String string = new String(stringChars);
        field.set(obj, string);
    }

    protected <T> void insertString(ByteBuffer byteBuffer, Field field, T obj) throws IllegalAccessException {
        String string = (String) field.get(obj);
        if (string == null) {
            byteBuffer.putInt(-1);
            return;
        }
        byteBuffer.putInt(string.length());
        char[] characters = string.toCharArray();
        byte[] bytes = toBytes(characters);
        byteBuffer.put(bytes);
    }
}