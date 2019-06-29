package org.liuwww.db.test.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.liuwww.common.util.DbNameConverter;

public class DataUtil
{
    public static Map<String, Object> convertMapKey(Map<String, Object> map)
    {
        Map<String, Object> resMap = new HashMap<String, Object>(map.size());
        for (Entry<String, Object> en : map.entrySet())
        {
            String key = DbNameConverter.convert(en.getKey());
            resMap.put(key, en.getValue());
        }
        return resMap;
    }
}
