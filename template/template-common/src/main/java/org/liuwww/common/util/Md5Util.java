package org.liuwww.common.util;

import java.security.MessageDigest;

import org.liuwww.common.execption.SysException;

public class Md5Util
{
    public static String bytes2hex(byte[] bytes)
    {
        final String HEX = "0123456789ABCDEF";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
        {
            // 取出这个字节的高4位，然后与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(HEX.charAt((b >> 4) & 0x0f));
            // 取出这个字节的低位，与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(HEX.charAt(b & 0x0f));
        }

        return sb.toString();
    }

    public static String md5Encode(String data)
    {
        try
        {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return bytes2hex(md5.digest(data.getBytes("utf-8"))).toLowerCase();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SysException("", e);
        }

    }
}
