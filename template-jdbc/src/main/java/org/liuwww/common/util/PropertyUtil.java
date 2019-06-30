package org.liuwww.common.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.liuwww.common.execption.SysException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyUtil
{
    private static final Logger logger = LoggerFactory.getLogger(PropertyUtil.class);

    private Map<String, PropertiesConfiguration> propMap;

    private static PropertyUtil instance = new PropertyUtil();

    private Map<String, Long> lastLoadMap;

    private PropertyUtil()
    {
        propMap = new HashMap<String, PropertiesConfiguration>();
        lastLoadMap = new HashMap<String, Long>();
    }

    private PropertiesConfiguration getTheConfig(String path)
    {
        return instance.propMap.get(path);
    }

    private void setTheConfig(String path, PropertiesConfiguration config)
    {
        instance.propMap.put(path, config);
    }

    private static Configuration getConfig(String fileName, boolean istry)
    {
        PropertiesConfiguration config = instance.getTheConfig(fileName);
        if (config == null)
        {
            Long last = instance.lastLoadMap.get(fileName);
            if (istry)
            {
                if (last != null && System.currentTimeMillis() - last > 1000 * 60 * 5)
                {
                    return null;
                }
                else
                {
                    instance.lastLoadMap.put(fileName, System.currentTimeMillis());
                }
            }
            synchronized (PropertyUtil.class)
            {
                if (config == null)
                {
                    String file = fileName + ".properties";
                    try
                    {
                        config = new PropertiesConfiguration();
                        config.setEncoding("utf-8");
                        config.load(file);
                        config.setReloadingStrategy(new FileChangedReloadingStrategy());
                        instance.setTheConfig(fileName, config);
                    }
                    catch (ConfigurationException e)
                    {
                        if (!istry)
                        {
                            if (logger.isErrorEnabled())
                            {
                                logger.error("file load error", e);
                            }
                            throw new SysException("", e);
                        }

                    }
                }

            }
        }
        return config;
    }

    public static String getProp(String fileName, String tag)
    {
        return getProp(fileName, tag, false);
    }

    private static String getProp(String fileName, String tag, boolean istry)
    {
        Configuration config = getConfig(fileName, istry);
        if (config != null)
        {
            List<Object> list = config.getList(tag);
            if (list.size() > 1)
            {
                return StringUtil.join(list, ",");
            }
            else
            {
                return config.getString(tag);
            }

        }
        return null;
    }

    public static String tryGetProp(String fileName, String tag)
    {
        return getProp(fileName, tag, true);
    }

    public static Map<String, String> getPropMap(String fileName)
    {
        return getPropMap(fileName, false);
    }

    private static Map<String, String> getPropMap(String fileName, boolean isTry)
    {
        Configuration config = getConfig(fileName, isTry);
        if (config != null)
        {
            Map<String, String> map = new HashMap<String, String>();
            Iterator<String> it = config.getKeys();
            while (it.hasNext())
            {
                String key = it.next();
                List<Object> list = config.getList(key);
                if (list.size() > 0)
                {
                    map.put(key, StringUtil.join(list, ","));
                }
                else
                {
                    map.put(key, config.getString(key));
                }

            }
            return map;
        }
        else
        {
            return null;
        }
    }

    public static Map<String, String> tryGetPropMap(String fileName)
    {
        return getPropMap(fileName, true);
    }
}
