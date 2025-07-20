/**
 *
 */
package org.liuwww.common.util;

import java.math.BigDecimal;

/**
 * Title: NumberUtils <br/>
 * Description: <br/>
 *
 * @author liuwww
 * @date 2015年12月19日下午1:56:04
 */
public class NumberUtils extends org.apache.commons.lang.math.NumberUtils {

    /**
     * @Desc:格式化double
     * @Date 2015年12月19日下午1:57:00
     * @author liuwww
     * @param d
     * @param scale
     *            保留小数位
     * @return
     */
    public static String formatDouble(Double d, int scale) {
        BigDecimal bigDecimal = new BigDecimal(d);
        Double r = bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        return r.toString();
    }

    /**
     * @Desc:类型转换
     * @Date 2018年8月14日上午10:57:53
     * @author liuwww
     * @param param
     * @return
     */
    public static int getInt(Object param) {
        if (param == null) {
            return 0;
        }
        else if (param instanceof String && "".equals(param.toString())) {
            return 0;
        }
        if (param instanceof Number) {
            return ((Number) param).intValue();
        }
        else if (param instanceof String && isDigits(param.toString())) {
            return Integer.parseInt(param.toString());
        }
        throw new RuntimeException("该类型无法转换：" + param.toString());
    }

    public static String getIntString(Object param) {
        return String.valueOf(getInt(param));
    }

    public static String getNumberStr(Object param) {
        if (param == null) {
            return null;
        }
        return param.toString();
    }
}
