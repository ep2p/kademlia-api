package example;

import java.util.Random;

public class BoundedHashExample {
    public static void main(String[] args) {
        int i = Integer.MAX_VALUE - new Random().nextInt(Integer.MAX_VALUE / 2);
        System.out.println(i);
        int hashed = i << -8 >>> -8;
        System.out.println(hashed);
        hashed = i << -256 >>> -256;
        System.out.println(hashed);

        int j = 5;
        hashed = j << -8 >>> -8;
        System.out.println(hashed);
    }
}
