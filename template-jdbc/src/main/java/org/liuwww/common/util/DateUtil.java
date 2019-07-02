package org.liuwww.common.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.liuwww.common.execption.DbException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtil
{
    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

    public static String defaultDatePattern = "yyyy-MM-dd";

    public static String datetimePattern = "yyyy-MM-dd HH:mm:ss";

    public static String[] datetimePatterns =
    { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm" };

    /**
     * 获得默认的 date pattern
     */
    public static String getDatePattern()
    {
        return defaultDatePattern;
    }

    /**
     * 返回预设Format的当前日期字符串
     */
    public static String getToday()
    {
        Date today = new Date();
        return format(today);
    }

    public static String nowString()
    {
        return format(new Date(), datetimePattern);
    }

    /**
     * 使用预设Format格式化Date成字符串
     */
    public static String format(Date date)
    {
        return format(date, getDatePattern());
    }

    /**
     * 使用参数Format格式化Date成字符串
     */
    public static String format(Date date, String pattern)
    {
        String returnValue = "";

        if (date != null)
        {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            returnValue = df.format(date);
        }

        return (returnValue);
    }

    /**
     * 使用预设格式将字符串转为Date
     */
    public static Date parse(String strDate)
    {
        try
        {
            return DateUtils.parseDate(strDate, datetimePatterns);
        }
        catch (ParseException e)
        {
            throw new DbException("", e);
        }
    }

    /**
     * 使用参数Format将字符串转为Date
     */
    public static Date parse(String strDate, String pattern)
    {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        try
        {
            return df.parse(strDate);
        }
        catch (ParseException e)
        {
            if (logger.isInfoEnabled())
            {

                logger.info("字符串转日期异常：" + pattern, e);
            }
        }
        return null;
    }

    /**
     * 在日期上增加数个整月
     */
    public static Date addMonth(Date date, int n)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, n);
        return cal.getTime();
    }

    /**
     * 在日期上增加数个整日(n为负数则是减少数日)
     */
    public static Date addDay(Date date, int n)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, n);
        return cal.getTime();
    }

    /**
     * 在日期上增加数个小时(n为负数则是减少数小时)
     */
    public static Date addHour(Date date, int n)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, n);
        return cal.getTime();
    }

    public static Date parseDate(String str, String[] parsePatterns) throws ParseException
    {
        return DateUtils.parseDate(str, parsePatterns);
    }

    /**
     * 字符串转化为日期,通用性相对较强
     *
     * @param dateString 具有日期格式的字符串
     * @param DataFormat 日期格式
     * @return Date
     */
    public static Date stringToDate(String dateString, String DataFormat)
    {
        Date date = null;
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(DataFormat);
            date = sdf.parse(dateString);
        }
        catch (ParseException ex)
        {
            return null;
        }
        return date;
    }

    /**
     * 求出两个时间段的时间差（精确到天/小时/分）
     */
    public static String timeLeft(Date timeNow, Date timeLimit)
    {
        if (timeNow == null || timeLimit == null)
        {
            return "0";
        }
        long now = timeNow.getTime();
        long limit = timeLimit.getTime();
        int day = (int) (Math.abs(now - limit) / (3600000 * 24));
        int hour = (int) (Math.abs(now - limit) % (3600000 * 24)) / 3600000;
        int minute = (int) ((Math.abs(now - limit) % (3600000 * 24)) % 3600000) / 60000;
        String timeLeft = "0";
        StringBuffer sb = new StringBuffer();
        if (now < limit)
        {
            sb.append("剩余").append(day).append("天").append(hour).append("小时").append(minute).append("分");
        }
        if (now > limit)
        {
            sb.append("超过").append(day).append("天").append(hour).append("小时").append(minute).append("分");
        }
        timeLeft = sb.toString();
        return timeLeft;
    }

    /**
     * 时间一是否超过时间二
     */
    public static String isExceed(Date timeNow, Date timeLimit)
    {
        if (timeNow == null || timeLimit == null)
        {
            return "false";
        }
        long now = timeNow.getTime();
        long limit = timeLimit.getTime();
        if (now > limit)
        {
            return "true";
        }
        return "false";
    }

    /**
     * 求出两个时间段的时间差(精确到小时)
     */
    public static int timeInterval(Date timeNow, Date timeLimit)
    {
        if (timeNow == null || timeLimit == null)
        {
            return 0;
        }
        long now = timeNow.getTime();
        long limit = timeLimit.getTime();
        int interval = (int) ((now - limit) / 3600000);
        return interval;
    }

    /**
     * 求出两个时间段的时间差(精确到秒)
     */
    public static int timeIntervalSecond(Date timeNow, Date timeLimit)
    {
        if (timeNow == null || timeLimit == null)
        {
            return 0;
        }
        long now = timeNow.getTime();
        long limit = timeLimit.getTime();
        int interval = (int) ((now - limit) / 1000);
        return interval;
    }

    /**
     * 按照小时添加时间(减去下班时间)
     */
    public static Date addHours(Date currentDate, int num, String amStart, String amEnd, String pmStart, String pmEnd)
            throws Exception
    {
        if (currentDate == null)
        {
            return null;
        }
        // 求出下班时间的时间差，包括中午和晚上
        long midDay = Timestamp.valueOf("2007-04-09 " + pmStart + ":00").getTime()
                - Timestamp.valueOf("2007-04-09 " + amEnd + ":00").getTime();
        long midNight = Timestamp.valueOf("2007-04-10 " + amStart + ":00").getTime()
                - Timestamp.valueOf("2007-04-09 " + pmEnd + ":00").getTime();
        Calendar calendar = Calendar.getInstance();
        // 判断是否是工作时间，如果不是的话currentDate变为下次上班时间，并过一秒
        if (!isWorkTime(currentDate, amStart, amEnd, pmStart, pmEnd))
        {
            // 是否是中午休息
            if (isDay(currentDate, amEnd, pmStart))
            {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String now = df.format(currentDate);
                String[] nowDay = now.split(" ");
                // 组织成date型
                currentDate = df.parse(nowDay[0] + " " + pmStart + ":01");
            }
            // 是否是夜间休息凌晨之后上班之前
            else if (isAfterMidNight(currentDate, amStart))
            {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String now = df.format(currentDate);
                String[] nowDay = now.split(" ");
                // 组织成date型
                currentDate = df.parse(nowDay[0] + " " + amStart + ":01");
            }
            // 下午下班后，晚上凌晨前
            else
            {
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                day = day + 1;
                calendar.set(Calendar.DAY_OF_MONTH, day);
                Date dateTemp = calendar.getTime();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String now = df.format(dateTemp);
                String[] nowDay = now.split(" ");
                // 组织成date型
                currentDate = df.parse(nowDay[0] + " " + amStart + ":01");
            }
        }
        // 对时间间隔进行for循环，发现下班时间跳过，并加上相应的时间间隔
        calendar.setTimeInMillis(currentDate.getTime());
        for (int i = 0; i < num; i++)
        {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            hour = hour + 1;
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            currentDate = calendar.getTime();
            while (!isWorkTime(currentDate, amStart, amEnd, pmStart, pmEnd))
            {
                if (isDay(currentDate, amEnd, pmStart))
                {
                    currentDate = new Date((currentDate.getTime() + midDay));
                    calendar.setTimeInMillis(currentDate.getTime());
                }
                else
                {
                    currentDate = new Date((currentDate.getTime() + midNight));
                    calendar.setTimeInMillis(currentDate.getTime());
                }
            }
        }
        long time = currentDate.getTime();
        return new Date(time);
    }

    // 是否是上班时间
    private static boolean isWorkTime(Date date, String amStart, String amEnd, String pmStart, String pmEnd)
            throws ParseException
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = df.format(date);
        String[] nowDay = now.split(" ");
        // 组织成date型
        Date amstart = df.parse(nowDay[0] + " " + amStart + ":00");
        Date amend = df.parse(nowDay[0] + " " + amEnd + ":00");
        Date pmstart = df.parse(nowDay[0] + " " + pmStart + ":00");
        Date pmend = df.parse(nowDay[0] + " " + pmEnd + ":00");
        if ((date.after(amstart) && date.before(amend)) || (date.after(pmstart) && date.before(pmend)))
        {
            return true;
        }
        return false;
    }

    // 非工作时间---中午还是晚上，只能在!isWorkTime嵌套下使用
    private static boolean isDay(Date date, String amEnd, String pmStart) throws ParseException
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = df.format(date);
        String[] nowDay = now.split(" ");
        // 组织成date型
        Date amend = df.parse(nowDay[0] + " " + amEnd + ":00");
        Date pmstart = df.parse(nowDay[0] + " " + pmStart + ":00");
        if (date.after(amend) && date.before(pmstart))
        {
            return true;
        }
        return false;
    }

    // 是否在午夜之后
    private static boolean isAfterMidNight(Date date, String amStart) throws ParseException
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = df.format(date);
        String[] nowDay = now.split(" ");
        // 组织成date型
        Date amstart = df.parse(nowDay[0] + " " + amStart + ":00");
        if (date.before(amstart))
        {
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println(DateUtil.addHours(new Date(), 6, "8:00", "12:00", "14:00", "18:00"));
    }

    public static String format2(Date date)
    {

        return format(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date parseToTimestamp(String strDate, String pattern)
    {
        Date date = parse(strDate, pattern);
        return new Timestamp(date.getTime());

    }

    /**
     * @desc:获取季度
     * @Date:2018年9月2日下午10:10:49
     * @author liuwww
     * @param date
     * @return
     */
    public static int getQuarter(Date date)
    {

        int season = 0;

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int month = c.get(Calendar.MONTH);
        switch (month)
        {
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.MARCH:
                season = 1;
                break;
            case Calendar.APRIL:
            case Calendar.MAY:
            case Calendar.JUNE:
                season = 2;
                break;
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.SEPTEMBER:
                season = 3;
                break;
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
            case Calendar.DECEMBER:
                season = 4;
                break;
            default:
                break;
        }
        return season;
    }
}
