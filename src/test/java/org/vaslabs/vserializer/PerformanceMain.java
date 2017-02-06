package org.vaslabs.vserializer;

import java.util.Arrays;

/**
 * Created by vnicolaou on 12/01/17.
 */
public class PerformanceMain {

    static byte[] memory;
    static TestUtils.AllEncapsulatedData[] allEncapsulatedData = new TestUtils.AllEncapsulatedData[1000];
    static VSerializer vSerializer = new AlphabeticalSerializer();
    public static void main(String[] args) throws InterruptedException {


        for (int i = 0; i < allEncapsulatedData.length; i++) {
            allEncapsulatedData[i] =  new TestUtils.AllEncapsulatedData();
        }
        memory = vSerializer.serialize(allEncapsulatedData);

        TestUtils.AllEncapsulatedData[] allEncapsulatedDatas = vSerializer.deserialise(memory, TestUtils.AllEncapsulatedData[].class);

        for (int i = 0; i < allEncapsulatedData.length; i++) {
            System.out.println(allEncapsulatedDatas[i].toString());
        }

        System.out.println(memory.length);

        System.out.println(Arrays.toString(memory));

        while (true) Thread.sleep(100);

    }

}
