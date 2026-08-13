package io.github.liuwww.common.util;

/**
 * 字符串工具类
 * 
 * @author liuwww
 * @date 2015-11-27下午8:16:50
 */
public class StringUtil {

    /**
     * 判断字符串是否为空白（null、空字符串或仅包含空白字符）
     * 
     * @param str 要检查的字符串
     * @return 如果为空白返回true，否则返回false
     */
    public static boolean isBlank(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否不为空白
     * 
     * @param str 要检查的字符串
     * @return 如果不为空白返回true，否则返回false
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断字符串是否为空（null或空字符串）
     * 
     * @param str 要检查的字符串
     * @return 如果为空返回true，否则返回false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否不为空
     * 
     * @param str 要检查的字符串
     * @return 如果不为空返回true，否则返回false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 首字母大写
     * 
     * @param str 输入字符串
     * @return 首字母大写后的字符串
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        char firstChar = str.charAt(0);
        if (Character.isTitleCase(firstChar)) {
            return str;
        }
        return new StringBuilder(str.length())
                .append(Character.toTitleCase(firstChar))
                .append(str.substring(1))
                .toString();
    }

    /**
     * 去除字符串两端的空白字符
     * 
     * @param str 输入字符串
     * @return 去除两端空白后的字符串，如果输入为null则返回null
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 如果字符串为空白，返回默认值
     * 
     * @param str        输入字符串
     * @param defaultStr 默认值
     * @return 如果输入为空白返回默认值，否则返回原字符串
     */
    public static String defaultIfBlank(String str, String defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }

    /**
     * 如果字符串为空，返回默认值
     * 
     * @param str        输入字符串
     * @param defaultStr 默认值
     * @return 如果输入为空返回默认值，否则返回原字符串
     */
    public static String defaultIfEmpty(String str, String defaultStr) {
        return isEmpty(str) ? defaultStr : str;
    }

    /**
     * 获取文件后缀名
     * 
     * @param fileName 文件名
     * @return 文件后缀名，如果没有后缀返回空字符串，如果文件名为空返回null
     */
    public static String getFileSuffix(String fileName) {
        if (isBlank(fileName)) {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index > 0) {
            return fileName.substring(index + 1);
        } else {
            return "";
        }
    }

    /**
     * 比较两个字符串是否相等（忽略大小写）
     * 
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 如果两个字符串相等（忽略大小写）返回true，否则返回false
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }

    /**
     * 比较两个字符串是否相等
     * 
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 如果两个字符串相等返回true，否则返回false
     */
    public static boolean equals(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }
}
