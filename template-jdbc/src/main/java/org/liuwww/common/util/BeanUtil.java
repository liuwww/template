package org.liuwww.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class BeanUtil implements ApplicationContextAware
{
    private static ApplicationContext context;

    private static Logger logger = LoggerFactory.getLogger(BeanUtil.class);

    public static ApplicationContext getContext()
    {
        return context;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        context = applicationContext;
    }

    public static Object getBean(String beanName)
    {
        try
        {
            return context.getBean(beanName);
        }
        catch (BeansException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("there is not has bean:{}", beanName);
            }
            return null;
        }
    }

    public static <T> T getBean(String beanName, Class<T> tClass)
    {
        try
        {
            return context.getBean(beanName, tClass);
        }
        catch (BeansException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("there is not has bean:{}", beanName);
            }
            return null;
        }
    }

    public static <T> T getBean(Class<T> tClass)
    {
        try
        {
            return context.getBean(tClass);
        }
        catch (BeansException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("there is not has bean:{}", tClass);
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getBeanList(Class<T> tClass)
    {

        Map<String, T> map = context.getBeansOfType(tClass);
        List<T> list = new ArrayList<T>();
        for (String key : map.keySet())
        {
            T bean = (T) getBean(key);
            list.add(bean);
        }
        return list;
    }
}
