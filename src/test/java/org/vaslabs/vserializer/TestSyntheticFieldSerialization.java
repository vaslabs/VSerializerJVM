package org.vaslabs.vserializer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by vnicolaou on 11/11/16.
 */

public class TestSyntheticFieldSerialization {

    VSerializer vSerializer = new AlphabeticalSerializer();

    @Test
    public void test_anonymous_class_serialization_with_primitive_field() {
        final int somePrimitiveField = 5;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int something = somePrimitiveField  + 6;
            }
        };
        byte[] data = vSerializer.serialize(runnable);
        assertEquals(0, data.length);
        Runnable recoveredRunnable = vSerializer.deserialise(data, Runnable.class);
        assertNull(recoveredRunnable);
    }

}
