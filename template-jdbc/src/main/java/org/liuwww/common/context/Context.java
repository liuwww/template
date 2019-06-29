package org.liuwww.common.context;

import java.io.Serializable;
import java.util.Map;

/**
 * 上下文环境
 * @author lwww 2017年9月2日上午10:48:33
 */
public interface Context extends Serializable
{
    /**
     * @Desc：放入值，如果有旧值则返回，否则返回null
     * @Date 2017年9月2日上午10:48:58
     * @author liuwww
     * @param name
     * @param object
     * @return
     */
    Object put(String name, Object value);

    /**
     * @Desc:重命名key
     * @Date 2017年9月2日上午10:50:03
     * @author liuwww
     * @param key
     * @param newKey
     * @return
     */
    boolean renameKey(String key, String newKey);

    /**
     * @Desc:移除并返回
     * @Date 2017年9月2日上午10:50:19
     * @author liuwww
     * @param name
     * @return 有则返回，否则返回nul
     */
    Object remove(String name);

    /**
     * @Desc:获取值
     * @Date 2017年9月2日上午10:50:54
     * @author liuwww
     * @param name
     * @return
     */
    Object get(String name);

    /**
     * @Desc:全部放入环境
     * @Date 2017年9月2日上午10:51:12
     * @author liuwww
     * @param map
     */
    void putAll(Map<String, Object> map);

    /**
     * @Desc:获取相关值，无则返回给定的默认值
     * @Date 2017年9月2日上午10:51:58
     * @author liuwww
     * @param name
     * @param defaultValue
     * @return
     */
    Object get(String name, Object defaultValue);

    int size();

    /**
     * @Desc:是否存在
     * @Date 2017年9月2日上午10:52:28
     * @author liuwww
     * @param name
     * @return
     */
    boolean exist(String name);

    /**
     * @Desc:清空
     * @Date 2017年9月2日上午10:52:46
     * @author liuwww
     */
    void clear();

    /**
     * @Desc:返回所有
     * @Date 2017年9月2日上午10:53:01
     * @author liuwww
     * @return
     */
    Map<String, Object> getItemMap();

}