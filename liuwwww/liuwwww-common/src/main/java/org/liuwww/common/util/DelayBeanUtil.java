package org.liuwww.common.util;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

@Component
public class DelayBeanUtil
{
    private static void delay()
    {
        while (BeanUtil.getContext() == null)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static <T> T getService(Class<T> clazz)
    {
        delay();
        return BeanUtil.getBean(clazz);
    }

    public static List<DataSource> getBeanList(Class<DataSource> clazz)
    {
        delay();
        return BeanUtil.getBeanList(clazz);
    }
}
