package io.ep2p.kademlia.util;

import io.ep2p.kademlia.exception.UnsupportedBoundingException;
import lombok.Getter;

import java.math.BigInteger;

/**
 * bounds a hash into certain number size
 */
@Getter
public class BoundedHashUtil {
    private final int maxSize;

    public BoundedHashUtil(int maxSizeInBits) {
        this.maxSize = maxSizeInBits;
    }

    @SuppressWarnings("unchecked")
    public <I extends Number, O extends Number> O hash(I input, Class<O> oClass) throws UnsupportedBoundingException {
        if(oClass.equals(Long.class)){
            long i = ((Long) input) << -maxSize >>> -maxSize;
            return (O) Long.valueOf(i);
        }

        if(oClass.equals(Integer.class)){
            int i = ((Integer) input) << -maxSize >>> -maxSize;
            return (O) Integer.valueOf(i);
        }

        if(oClass.equals(BigInteger.class)){
            if(input.getClass() == BigInteger.class)
                return (O) input;
            else if(input.getClass() == Integer.class) {
                return (O) BigInteger.valueOf((Integer) input);
            }else if (input.getClass() == Long.class){
                return (O) BigInteger.valueOf((Long) input);
            }
            return (O) input;
        }

        throw new UnsupportedBoundingException(oClass);
    }
}
