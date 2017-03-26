package org.vaslabs.vserializer;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by vnicolaou on 03/05/16.
 */
public class SerializationUtils {
    static Map<Class, Integer> sizes = new HashMap<>();
    static Map<Class, PrimitiveType> enumTypes = new HashMap<Class, PrimitiveType>();
    static Map<Class, PrimitiveType> primitiveWrappersDictionary = new HashMap<Class, PrimitiveType>();
    static Map<Class, Method> primitiveWrappers = new HashMap<Class, Method>();
    static Map<Class, Integer> primitiveWrappersSizes = new HashMap<>();
    static {
        try {
            primitiveWrappers.put(Integer.class, Integer.class.getMethod("valueOf", Integer.TYPE));
            primitiveWrappers.put(Boolean.class, Boolean.class.getMethod("valueOf", Boolean.TYPE));
            primitiveWrappers.put(Short.class, Short.class.getMethod("valueOf", Short.TYPE));
            primitiveWrappers.put(Long.class, Long.class.getMethod("valueOf", Long.TYPE));
            primitiveWrappers.put(Double.class, Double.class.getMethod("valueOf", Double.TYPE));
            primitiveWrappers.put(Float.class, Float.class.getMethod("valueOf", Float.TYPE));
            primitiveWrappers.put(Character.class, Character.class.getMethod("valueOf", Character.TYPE));
            primitiveWrappers.put(Byte.class, Byte.class.getMethod("valueOf", Byte.TYPE));
            primitiveWrappers = Collections.unmodifiableMap(primitiveWrappers);
        } catch (Exception e) {
            System.exit(1);
        }
    }

    static {
        try {
            primitiveWrappersDictionary.put(Integer.class, PrimitiveType.INT);
            primitiveWrappersDictionary.put(Boolean.class, PrimitiveType.BOOLEAN);
            primitiveWrappersDictionary.put(Short.class, PrimitiveType.SHORT);
            primitiveWrappersDictionary.put(Long.class, PrimitiveType.LONG);
            primitiveWrappersDictionary.put(Double.class, PrimitiveType.DOUBLE);
            primitiveWrappersDictionary.put(Float.class, PrimitiveType.FLOAT);
            primitiveWrappersDictionary.put(Character.class, PrimitiveType.CHAR);
            primitiveWrappersDictionary.put(Byte.class, PrimitiveType.BYTE);
            primitiveWrappersDictionary = Collections.unmodifiableMap(primitiveWrappersDictionary);
        } catch (Exception e) {
            System.exit(1);
        }
    }

    static {
        try {
            primitiveWrappersSizes.put(Integer.class, 4);
            primitiveWrappersSizes.put(Boolean.class, 1);
            primitiveWrappersSizes.put(Short.class, 2);
            primitiveWrappersSizes.put(Long.class, 8);
            primitiveWrappersSizes.put(Double.class, 8);
            primitiveWrappersSizes.put(Float.class, 4);
            primitiveWrappersSizes.put(Character.class, 2);
            primitiveWrappersSizes.put(Byte.class, 1);
            primitiveWrappersSizes = Collections.unmodifiableMap(primitiveWrappersSizes);
        } catch (Exception e) {
            System.exit(1);
        }
    }

    static {
        SerializationUtils.sizes.put(Integer.TYPE, Integer.SIZE/8);
        SerializationUtils.sizes.put(Float.TYPE, Float.SIZE/8);
        SerializationUtils.sizes.put(Long.TYPE, Long.SIZE/8);
        SerializationUtils.sizes.put(Boolean.TYPE, 1);
        SerializationUtils.sizes.put(Byte.TYPE, Byte.SIZE/8);
        SerializationUtils.sizes.put(Short.TYPE, Short.SIZE/8);
        SerializationUtils.sizes.put(Character.TYPE, Character.SIZE/8);
        SerializationUtils.sizes.put(Double.TYPE, Double.SIZE/8);
        SerializationUtils.sizes.put(int[].class, Integer.SIZE/8);
        SerializationUtils.sizes.put(long[].class, Long.SIZE/8);
        SerializationUtils.sizes.put(boolean[].class, 1);
        SerializationUtils.sizes.put(byte[].class, Byte.SIZE/8);
        SerializationUtils.sizes.put(short[].class, Short.SIZE/8);
        SerializationUtils.sizes.put(char[].class, Character.SIZE/8);
        SerializationUtils.sizes.put(float[].class, Float.SIZE/8);
        SerializationUtils.sizes.put(double[].class, Double.SIZE/8);
        sizes = Collections.unmodifiableMap(sizes);
    }

    static {
        SerializationUtils.enumTypes.put(Integer.TYPE, PrimitiveType.INT);
        SerializationUtils.enumTypes.put(Long.TYPE, PrimitiveType.LONG);
        SerializationUtils.enumTypes.put(Boolean.TYPE, PrimitiveType.BOOLEAN);
        SerializationUtils.enumTypes.put(Byte.TYPE, PrimitiveType.BYTE);
        SerializationUtils.enumTypes.put(Short.TYPE, PrimitiveType.SHORT);
        SerializationUtils.enumTypes.put(Character.TYPE, PrimitiveType.CHAR);
        SerializationUtils.enumTypes.put(Float.TYPE, PrimitiveType.FLOAT);
        SerializationUtils.enumTypes.put(Double.TYPE, PrimitiveType.DOUBLE);
        SerializationUtils.enumTypes.put(int[].class, PrimitiveType.INT);
        SerializationUtils.enumTypes.put(long[].class, PrimitiveType.LONG);
        SerializationUtils.enumTypes.put(boolean[].class, PrimitiveType.BOOLEAN);
        SerializationUtils.enumTypes.put(byte[].class, PrimitiveType.BYTE);
        SerializationUtils.enumTypes.put(short[].class, PrimitiveType.SHORT);
        SerializationUtils.enumTypes.put(char[].class, PrimitiveType.CHAR);
        SerializationUtils.enumTypes.put(float[].class, PrimitiveType.FLOAT);
        SerializationUtils.enumTypes.put(double[].class, PrimitiveType.DOUBLE);
        enumTypes = Collections.unmodifiableMap(enumTypes);
    }

    public static <T> T instantiate(Class<T> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance(null);
    }

    public static boolean isEnum(Class clazz) {
        Class enclosingClass = clazz.getEnclosingClass();
        return enclosingClass != null && enclosingClass.isEnum();
    }

    public static int calculateSize(Field[] fields, Object obj) {
        int size = 0;
        for (Field field : fields) {
            if (skipField(field))
                continue;
            if (field.getType().isArray()) {
                size += 4 + sizeOfArray(field, obj);
                continue;
            }
            else if (!field.getType().isPrimitive()) {
                if (primitiveWrappers.containsKey(field.getType())) {
                    size += 1;
                    field.setAccessible(true);
                    try {
                        final Object newObj = field.get(obj);
                        if (newObj != null)
                            size += primitiveWrappersSizes.get(field.getType());
                        continue;
                    } catch (IllegalAccessException iae) {

                    }
                }
                if (field.getType().equals(String.class)) {
                    try {
                        try {
                            arrangeField(field, obj);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                        String string = (String) field.get(obj);
                        if (string == null)
                            size += 4;
                        else
                            size += 4 + string.length()*2;
                        continue;
                    } catch (IllegalAccessException e) {
                        return 0;
                    }
                }
                size += 1;
                try {
                    field.setAccessible(true);
                    final Object newObj = field.get(obj);
                    if (newObj != null && !field.getType().isEnum())
                        size += calculateSize(getAllFields(newObj), newObj);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {

                }
            }
            size += sizeOf(field);
        }
        return size;
    }

    protected static int sizeOfArray(Field field, Object obj) {
        try {

            field.setAccessible(true);
            Class type = field.getType();
            if (field.get(obj) == null)
                return 0;
            final int consistentTypeSize;
            if (!sizes.containsKey(type)) {
                return sizeOfArrayType(field, obj);
            }
            else {
                consistentTypeSize = sizes.get(type);
            }
            int arraySize = findArrayLength(field, obj);
            field.setAccessible(false);
            return arraySize*consistentTypeSize;
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    private static int sizeOfArrayType(Field field, Object obj) throws IllegalAccessException {
        Object[] objects = (Object[]) field.get(obj);
        if (objects == null || objects.length == 0) {
            return 0;
        }
        int sizeSum = 0;
        for (Object object : objects) {
            if (object == null) {
                continue;
            }
            Class type = object.getClass();
            if (isEnum(type))
                sizeSum += 1;
            else
                sizeSum += calculateSize(getAllFields(type), object);
        }
        return sizeSum + objects.length;
    }


    protected static int findArrayLength(Field field, Object obj) throws IllegalAccessException {
        if (!enumTypes.containsKey(field.getType())) {
            return findArrayLengthNonPrimitive(field, obj);
        }
        switch (enumTypes.get(field.getType())) {
            case INT: {
                int[] array = (int[]) field.get(obj);
                return array.length;
            }
            case LONG: {
                long[] array = (long[])field.get(obj);
                return array.length;
            }
            case BYTE: {
                byte[] array = (byte[])field.get(obj);
                return array.length;
            }
            case CHAR: {
                char[] array = (char[])field.get(obj);
                return array.length;
            }
            case BOOLEAN: {
                boolean[] array = (boolean[]) field.get(obj);
                return array.length;
            }
            case SHORT: {
                short[] array = (short[]) field.get(obj);
                return array.length;
            }
            case FLOAT: {
                float[] array = (float[]) field.get(obj);
                return array.length;
            }
            case DOUBLE: {
                double[] array = (double[]) field.get(obj);
                return array.length;
            }
            default:
                return 0;
        }
    }

    private static int findArrayLengthNonPrimitive(Field field, Object obj) throws IllegalAccessException {
        Object[] objects = (Object[]) field.get(obj);
        return objects.length;
    }

    public static int sizeOf(Field field) {
        Class type = field.getType();
        if (!sizes.containsKey(type))
            return 0;
        int size = sizes.get(type);
        return size;
    }

    public static <T> void arrangeField(Field field, T obj) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    public static <T> int calculateNonPrimitiveArraySize(T[] objects) {
        int totalSize = objects.length + 4;
        for (T object : objects) {
            if (object != null) {
                final Field[] fields = getAllFields(object);
                final int sizeOfSingleObject = SerializationUtils.calculateSize(fields, object);
                totalSize += sizeOfSingleObject;
            }
        }
        return totalSize;
    }

    protected static boolean skipField(Field field) {
        return field.isSynthetic() || Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers());
    }

    protected static <T> byte[] toBytes(T obj) {
        Class type = obj.getClass();
        if (!type.isArray())
            throw new IllegalArgumentException("Accepts only arrays");
        if (!SerializationUtils.enumTypes.containsKey(type)) {
            throw new IllegalArgumentException("Must be a primitive array");
        }
        final ByteBuffer byteBuffer;
        int size = sizes.get(type);
        switch (SerializationUtils.enumTypes.get(type)) {
            case INT: {
                int[] array = (int[]) obj;
                byteBuffer = ByteBuffer.allocate(array.length*size);
                for (int i : array) { byteBuffer.putInt(i); }
                return byteBuffer.array();
            }
            case LONG: {
                long[] array = (long[]) obj;
                byteBuffer = ByteBuffer.allocate(array.length*size);
                for (long l : array) {
                    byteBuffer.putLong(l);
                }
                return byteBuffer.array();
            }
            case SHORT: {
                short[] array = (short[]) obj;
                byteBuffer = ByteBuffer.allocate(array.length*size);
                for (short s : array) { byteBuffer.putShort(s); }
                return byteBuffer.array();
            }
            case BOOLEAN: {
                boolean[] array = (boolean[]) obj;
                byteBuffer = ByteBuffer.allocate(array.length*size);
                for (boolean b : array) {
                    byteBuffer.put((byte) (b ? 1 : 0));
                }
                return byteBuffer.array();
            }
            case BYTE: {
                byte[] array = (byte[])obj;
                return array;
            }
            case CHAR: {
                char[] array = (char[]) obj;
                byteBuffer = ByteBuffer.allocate(array.length*size);
                for (char c : array) {byteBuffer.putChar(c);}
                return byteBuffer.array();
            }
            case FLOAT: {
                float[] array = (float[])obj;
                byteBuffer = ByteBuffer.allocate(array.length*size);
                for (float s : array) { byteBuffer.putFloat(s); }
                return byteBuffer.array();
            }
            case DOUBLE: {
                double[] array = (double[])obj;
                byteBuffer = ByteBuffer.allocate(array.length*size);
                for (double d : array) { byteBuffer.putDouble(d); }
                return byteBuffer.array();
            }
        }
        return null;
    }

    protected static void fromBytes(byte[] data, int[] preAllocatedValues) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        for (int i = 0; i<preAllocatedValues.length; i++) { preAllocatedValues[i] = byteBuffer.getInt();}
    }

    protected static void fromBytes(byte[] data, long[] preAllocatedValues) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        for (int i = 0; i<preAllocatedValues.length; i++) { preAllocatedValues[i] = byteBuffer.getLong();}
    }

    protected static void fromBytes(byte[] data, short[] preAllocatedValues) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        for (int i = 0; i<preAllocatedValues.length; i++) { preAllocatedValues[i] = byteBuffer.getShort();}
    }

    protected static void fromBytes(byte[] data, char[] preAllocatedValues) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        for (int i = 0; i<preAllocatedValues.length; i++) { preAllocatedValues[i] = byteBuffer.getChar();}
    }

    protected static void fromBytes(byte[] data, boolean[] preAllocatedValues) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        for (int i = 0; i<preAllocatedValues.length; i++) { preAllocatedValues[i] = byteBuffer.get() == 1;}
    }

    protected static void fromBytes(byte[] data, byte[] preAllocatedValues) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        for (int i = 0; i<preAllocatedValues.length; i++) { preAllocatedValues[i] = byteBuffer.get(); }
    }

    protected static void fromBytes(byte[] data, double[] preAllocatedValues) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        for (int i = 0; i<preAllocatedValues.length; i++) { preAllocatedValues[i] = byteBuffer.getDouble(); }

    }

    public static void fromBytes(byte[] data, float[] preAllocatedValues) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        for (int i = 0; i<preAllocatedValues.length; i++) { preAllocatedValues[i] = byteBuffer.getFloat(); }
    }


    public static <T> Field[] getAllFields(T obj) {
        return getAllFields(obj.getClass().getDeclaredFields(), obj.getClass().getSuperclass());
    }

    public static Field[] getAllFields(Class clazz) {
        final Field[] fields = clazz.getDeclaredFields();
        return getAllFields(fields, clazz.getSuperclass());
    }

    private static Field[] getAllFields(Field[] fields, Class<?> superclass) {
        if (superclass == null || superclass.equals(Object.class))
            return fields;
        Field[] inheritedFields = superclass.getDeclaredFields();
        Field[] mergedFields = new Field[fields.length + inheritedFields.length];
        int index = 0;
        for (int i = 0; i < fields.length; i++) {
            mergedFields[index++] = fields[i];
        }
        for (int i = 0; i < inheritedFields.length; i++) {
            mergedFields[index++] = inheritedFields[i];
        }

        return getAllFields(mergedFields, superclass.getSuperclass());

    }

    protected static <T> T instantiatePrimitiveWrapper(Class<T> clazz, ByteBuffer byteBuffer) throws InvocationTargetException, IllegalAccessException {
        Method valueOfMethod = primitiveWrappers.get(clazz);
        Class parameterType = valueOfMethod.getParameterTypes()[0];
        PrimitiveType enumType = enumTypes.get(parameterType);
        switch (enumType) {
            case INT:
                return (T) valueOfMethod.invoke(null, byteBuffer.getInt());
            case SHORT:
                return (T) valueOfMethod.invoke(null, byteBuffer.getShort());
            case LONG:
                return (T) valueOfMethod.invoke(null, byteBuffer.getLong());
            case FLOAT:
                return (T) valueOfMethod.invoke(null, byteBuffer.getFloat());
            case BOOLEAN:
                return (T) valueOfMethod.invoke(null, byteBuffer.get() == 1);
            case BYTE:
                return (T) valueOfMethod.invoke(null, byteBuffer.get());
            case CHAR:
                return (T) valueOfMethod.invoke(null, byteBuffer.getChar());
            case DOUBLE:
                return (T)valueOfMethod.invoke(null, byteBuffer.getDouble());
        }
        return null;
    }

    protected static void writePrimitiveWrapperValue(Object obj, ByteBuffer byteBuffer) {
        Class wrapperClass = obj.getClass();
        PrimitiveType enumType = primitiveWrappersDictionary.get(wrapperClass);
        switch (enumType) {
            case INT:
                byteBuffer.putInt(((Integer) obj).intValue());
                break;
            case SHORT:
                byteBuffer.putShort(((Short) obj).shortValue());
                break;
            case LONG:
                byteBuffer.putLong(((Long) obj).longValue()); break;
            case FLOAT:
                byteBuffer.putFloat(((Float) obj).floatValue()); break;
            case BOOLEAN:
                byteBuffer.put((byte) (((Boolean) obj).booleanValue() ? 1 : 0)); break;
            case BYTE:
                byteBuffer.put(((Byte) obj).byteValue()); break;
            case CHAR:
                byteBuffer.putChar(((Character) obj).charValue()); break;
            case DOUBLE:
                byteBuffer.putDouble(((Double) obj).doubleValue()); break;

        }
    }

}
