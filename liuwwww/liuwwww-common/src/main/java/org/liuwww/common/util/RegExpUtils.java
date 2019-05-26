/**
 *
 */
package org.liuwww.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Title: RegExpUtils <br/>
 * Description: 正则工具<br/>
 *
 * @author liuwww
 * @date 2015年12月24日下午10:38:22
 */
public class RegExpUtils
{
    /**
     *
     * @Desc:获取字符串中第一串字符，若无则返回null
     * @Date 2015年12月24日下午10:41:42
     * @author liuwww
     * @param str
     */
    public static String getIntegerStr(String str)
    {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find())
        {
            return matcher.group();
        }
        return null;
    }

    /**
     *
     * @Desc:获取字符串中第一串字符，若无则返回null
     * @Date 2015年12月24日下午10:41:42
     * @author liuwww
     * @param str
     */
    public static String[] getIntegerStrs(String str)
    {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(str);
        List<String> list = new ArrayList<String>();
        while (matcher.find())
        {
            list.add(matcher.group());
        }
        return list.toArray(new String[0]);
    }

    /**
     * @Desc:校验给定的字符是否是整数或者小数（1234，-123,121.122，-1212.1212 等）
     * @Date 2017年6月5日上午10:45:47
     * @author liuwww
     * @param str
     * @return
     */
    public static boolean isNumber(String str)
    {
        return Pattern.compile("^(\\-)?\\d+(\\.\\d+)?$").matcher(str).matches();
    }

    /**
     * @Desc:使用给定的正则匹配给定的输入
     * @Date 2017年6月5日上午11:33:10
     * @author liuwww
     * @param reg 正则
     * @param str 待验收的字符串
     * @return
     */
    public static boolean matcher(String reg, String str)
    {
        if (StringUtil.isBlank(reg) || StringUtil.isBlank(str))
        {
            return false;
        }
        return Pattern.compile(reg).matcher(str).matches();
    }

    public static void main(String[] args)
    {
        System.out.println(Arrays.asList(getIntegerStrs("223dfkja923;dfas")));
        System.out.println(Arrays.asList(getIntegerStrs("dfkja923;dfas")));
    }

    /**
     * @Desc:是否匹配给定正则中的一个
     * @Date 2018年3月2日下午5:53:49
     * @author liuwww
     * @param uri
     * @param excludedPatterns
     * @return
     */
    public static boolean hasMatch(String uri, List<Pattern> excludedPatterns)
    {
        if (excludedPatterns != null)
        {
            for (Pattern pattern : excludedPatterns)
            {
                if (pattern.matcher(uri).matches())
                {
                    return true;
                }
            }
        }
        return false;
    }

}
