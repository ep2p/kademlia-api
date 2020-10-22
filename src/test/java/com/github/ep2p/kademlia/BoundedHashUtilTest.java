package com.github.ep2p.kademlia;

import com.github.ep2p.kademlia.util.BoundedHashUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoundedHashUtilTest {
    @Test
    public void testBoundedHash(){
        BoundedHashUtil boundedHashUtil1 = new BoundedHashUtil(8);
        BoundedHashUtil boundedHashUtil2 = new BoundedHashUtil(256);
        int input = 1573985150;
        Assertions.assertEquals(126, boundedHashUtil1.hash(input));
        Assertions.assertEquals(input, boundedHashUtil2.hash(input));
    }
}
