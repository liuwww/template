package org.liuwww.common.serializer;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Constants
{
    public static long MAX_JS_LONG = 2L << 53;

    public static long MIX_JS_LONG = -2L << 53;

    private static BigInteger maxBigInteger = new BigInteger(String.valueOf(MAX_JS_LONG));

    private static BigInteger mixBigInteger = new BigInteger(String.valueOf(MIX_JS_LONG));

    private static BigDecimal maxBigDecimal = new BigDecimal(MAX_JS_LONG);

    private static BigDecimal mixBigDecimal = new BigDecimal(MIX_JS_LONG);

    public static boolean isBigger(Long val)
    {
        return val > MAX_JS_LONG || val < MIX_JS_LONG;
    }

    public static boolean isBigger(BigInteger val)
    {
        return maxBigInteger.compareTo(val) < 0 || mixBigInteger.compareTo(val) > 0;
    }

    public static boolean isBigger(BigDecimal val)
    {
        return maxBigDecimal.compareTo(val) < 0 || mixBigDecimal.compareTo(val) > 0;
    }

}
