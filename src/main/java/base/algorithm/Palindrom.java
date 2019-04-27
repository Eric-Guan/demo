package base.algorithm;

import org.junit.Test;

public class Palindrom {

    /**
     * 判断是否回文数
     * @param x
     * @return
     */
    public static boolean isPalindrom(int x) {
        if (x < 0 || (x > 0 && x % 10 == 0)) return false;
        int half = 0;
        while (x > half) {
            half = half * 10 + x % 10;
            x = x / 10;
        }
        return (x == half || x == half / 10);
    }

    @Test
    public void isPalindrom_test(){
        System.out.println(isPalindrom(123321));
    }


}
