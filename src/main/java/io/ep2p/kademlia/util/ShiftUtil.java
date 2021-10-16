package io.ep2p.kademlia.util;

import java.math.BigInteger;

public class ShiftUtil {
    @SuppressWarnings("unchecked")
    public static <I extends Number> I shift(I input, int size){
        if (input instanceof Long){
            return (I) Long.valueOf(((Long) input) << -size >>> -size);
        }

        if (input instanceof Integer){
            return (I) Integer.valueOf(((Integer) input) << -size >>> -size);
        }

        if (input instanceof BigInteger){
            return (I) ((BigInteger) input).shiftLeft(-size).shiftRight(-size);
        }

        throw new IllegalArgumentException();
    }
}
