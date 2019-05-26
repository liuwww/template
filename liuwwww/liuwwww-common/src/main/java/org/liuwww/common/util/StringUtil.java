package org.liuwww.common.util;

import org.apache.commons.lang.StringUtils;

/**
 * @Desc:
 * @author liuwww
 * @date 2015-11-27下午8:16:50
 */
public class StringUtil extends StringUtils
{

    public static String getFileSuffix(String fileName)
    {
        if (isBlank(fileName))
        {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index > 0)
        {
            return fileName.substring(index + 1);
        }
        else
        {
            return "";
        }
    }
}
