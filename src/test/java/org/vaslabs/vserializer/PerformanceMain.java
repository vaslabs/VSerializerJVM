package org.vaslabs.vserializer;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by vnicolaou on 12/01/17.
 */

public class PerformanceMain {

    static byte[] memory;
    static TestUtils.AllEncapsulatedData[] allEncapsulatedData = new TestUtils.AllEncapsulatedData[1000];
    static Object[] reference = new Object[1000];
    static VSerializer vSerializer = new AlphabeticalSerializer();
    public static void main(String[] args) throws InterruptedException {


        for (int i = 0; i < allEncapsulatedData.length; i++) {
            allEncapsulatedData[i] =  new TestUtils.AllEncapsulatedData();
            reference[i] = new PerformanceMain();
        }
        memory = vSerializer.serialize(TimeUnit.DAYS);
        //memory = TestUtils.serializeObject(TimeUnit.DAYS);

        //TestUtils.AllEncapsulatedData[] allEncapsulatedDatas = vSerializer.deserialise(memory, TestUtils.AllEncapsulatedData[].class);


        //for (int i = 0; i < allEncapsulatedData.length; i++) {
        //    System.out.println(allEncapsulatedDatas[i].toString());
        //}

        System.out.println(memory.length);

        System.out.println(Arrays.toString(memory));

        System.out.println(new String(memory));
        System.out.println(HexBin.encode(memory));
        System.out.println(vSerializer.deserialise(memory, TimeUnit.class));

        while (true) Thread.sleep(100);

    }

}
