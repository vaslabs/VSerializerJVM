package org.vaslabs.vserializer;

import java.io.*;

/**
 * Created by vnicolaou on 05/05/16.
 */
public class TestUtils {

    protected static void initWithData(EncapsulatedData myTestObject) {
        myTestObject.a = 0xff121212;
        myTestObject.b = 0x1111;
        myTestObject.c = 0xf;
        myTestObject.d = 0xff;
    }

    protected static byte[] serializeObject(Serializable cds) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data = null;
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(cds);
            data = baos.toByteArray();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;

    }

    protected static <T> T deserializeObject(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (T) objectInputStream.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    public static EncapsulatedData[] initEncapsulatedDataArray() {
        EncapsulatedData[] encapsulatedDatas = new TestUtils.EncapsulatedData[10];
        for (int i = 0; i < encapsulatedDatas.length; i++) {
            encapsulatedDatas[i] = new TestUtils.EncapsulatedData();
            initWithData(encapsulatedDatas[i]);
            encapsulatedDatas[i].b = i;
        }
        return encapsulatedDatas;
    }

    public static class EncapsulatedData implements Serializable {
        protected long a;// = 0xff121212;
        protected int b;// = 0x1111;
        protected short d;// = 0xff;
        protected byte c;// = 0xf;
    }

    public static class FinalEncapsulatedData {
        protected final long a;// = 0xff121212;
        protected final int b;// = 0x1111;
        protected final short d;// = 0xff;
        protected final byte c;// = 0xf;

        public FinalEncapsulatedData() {
            a = 0;
            b = 0;
            d = 0;
            c = 0;
        }
        public FinalEncapsulatedData(long a, int b, short d, byte c) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }
    }

    public static class ComplexDataStructure implements Serializable {
        protected long a;
        protected int b;
        protected ComplexDataStructure somethingElse;
    }

    public static class DataStructureWithArray implements Serializable{
        protected int[] numbers;
        protected long value;
        protected boolean somethingElse;
    }

    public static class DataStructureWithObjectArray implements Serializable{
        protected EncapsulatedData[] encapsulatedDatas;
        protected long value;
        protected boolean somethingElse;
    }

    public static class InternalStrings implements Serializable{
        protected String myMessage;
        protected int myNumber;
        protected String myOtherMessage;
    }

    public static class AllEncapsulatedData implements Externalizable {

        protected long a;// = 0xff121212;
        protected int b;// = 0x1111;
        protected short d;// = 0xff;
        protected byte c;// = 0xf;
        protected boolean e;
        protected char f;
        protected float aFloat;
        protected double aDouble;

        private void readObject(ObjectInputStream objectInputStream) throws IOException {
            AllEncapsulatedData allEncapsulatedData = new AllEncapsulatedData();
            allEncapsulatedData.a = objectInputStream.readLong();
            allEncapsulatedData.aDouble = objectInputStream.readDouble();
            allEncapsulatedData.aFloat = objectInputStream.readFloat();
            allEncapsulatedData.b = objectInputStream.readInt();
            allEncapsulatedData.c = objectInputStream.readByte();
            allEncapsulatedData.d = objectInputStream.readShort();
            allEncapsulatedData.e = objectInputStream.readBoolean();
            allEncapsulatedData.f = objectInputStream.readChar();
            objectInputStream.close();
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(256);
            stringBuilder.append(a).append(':').append(b).append(':')
                    .append(':').append(c)
                    .append(':').append(d)
                    .append(':').append(e)
                    .append(':').append(f)
                    .append(':').append(aFloat);
            return stringBuilder.toString();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeLong(a);
            out.writeDouble(aDouble);
            out.writeFloat(aFloat);
            out.writeInt(b);
            out.writeByte(c);
            out.writeShort(d);
            out.writeBoolean(e);
            out.writeChar(f);
            out.close();
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            AllEncapsulatedData allEncapsulatedData = new AllEncapsulatedData();
            allEncapsulatedData.a = in.readLong();
            allEncapsulatedData.aDouble = in.readDouble();
            allEncapsulatedData.aFloat = in.readFloat();
            allEncapsulatedData.b = in.readInt();
            allEncapsulatedData.c = in.readByte();
            allEncapsulatedData.d = in.readShort();
            allEncapsulatedData.e = in.readBoolean();
            allEncapsulatedData.f = in.readChar();
            in.close();
        }
    }

    public static class AllEncapsulatedArrayData implements Serializable {
        protected long[] a;// = 0xff121212;
        protected int[] b;// = 0x1111;
        protected short[] d;// = 0xff;
        protected byte[] c;// = 0xf;
        protected boolean[] e;
        protected char[] f;
        protected float[] floats;
        protected double[] doubles;
    }

    public static class TransientData implements Serializable{
        protected int myNumber;
        protected transient int transientNumber;
    }

    public static class StaticData implements Serializable{
        protected int myNumber;
        protected static int staticNumber;
    }

    public static class InconsistenceSizedCollection {
        protected AllEncapsulatedArrayData[] allEncapsulatedData;
    }

    public static class PrimitiveWrapperClass {
        protected Boolean aBoolean;
        protected Double aDouble;
    }
}
