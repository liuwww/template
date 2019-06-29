package org.liuwww.common.util;

public class MoneyUtil
{

    private static final char[] data = new char[]
    { '零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖' };

    private static final char[] units = new char[]
    { '元', '拾', '佰', '仟', '万', '拾', '佰', '仟', '亿' };

    public static String convert(int money)
    {
        if (money > 0)
        {
            StringBuffer sbf = new StringBuffer();
            int unit = 0;
            while (money != 0)
            {
                sbf.insert(0, units[unit++]);
                int number = money % 10;
                sbf.insert(0, data[number]);
                money /= 10;
            }
            return sbf.toString();
        }
        else
        {
            return "负" + convert(0 - money);
        }

    }

    /** 
     * @param args 
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub
        System.out.println(convert(-0122424241));
    }
}