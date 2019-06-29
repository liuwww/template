package org.liuwww.common.entity;

import java.util.HashMap;
import java.util.Map;

import org.liuwww.common.util.StringUtil;

/**
 * 对request的parameterMap值做下处理，如果该值是字符数组且长度为1，则返回该字符串
 * @author lwww 2017年7月4日上午11:07:18
 */
public class ReqMap extends HashMap<String, Object>
{
    private static final long serialVersionUID = 7234308921934010811L;

    /**
     * value 长度为1返回字符串，大于一返回且isJonsArr=false，返回String[],否则返回用逗号拼接的字符串
     * @param map
     * @param isJonsArr
     */
    public ReqMap(Map<String, String[]> map, boolean isJonsArr)
    {
        super(map.size());
        if (isJonsArr)
        {
            initJoin(map);
        }
        else
        {
            init(map);
        }
    }

    private void init(Map<String, String[]> map)
    {
        for (String key : map.keySet())
        {
            String[] val = map.get(key);
            if (val.length == 1)
            {
                put(key, val[0]);
            }
            else
            {
                put(key, val);
            }
        }
    }

    private void initJoin(Map<String, String[]> map)
    {
        for (String key : map.keySet())
        {
            String[] val = map.get(key);
            if (val.length == 1)
            {
                put(key, val[0]);
            }
            else
            {
                put(key, StringUtil.join(val, ","));
            }
        }
    }

    /**
     * value 长度为1返回字符串，大于一返回String[]
     * @param map
     */
    public ReqMap(Map<String, String[]> map)
    {
        super(map.size());
        init(map);
    }

}
