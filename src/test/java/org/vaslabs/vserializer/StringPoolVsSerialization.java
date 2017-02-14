package org.vaslabs.vserializer;

/**
 * Created by vnicolaou on 12/02/17.
 */
public class StringPoolVsSerialization {

    private static String[] stringPool = new String[10000];
    private static byte[][] stringPoolRawMemory = new byte[stringPool.length][];
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < stringPool.length; i++) {
            stringPool[i] = randomString(1000).intern();
            stringPoolRawMemory[i] = TestUtils.serializeObject(stringPool[i]);
        }

        while (true) Thread.sleep(100);
    }

    private static String randomString(int range) {
        int rnd = (int)(Math.random()*range);
        return String.valueOf(rnd);
    }
}
