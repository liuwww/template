package org.liuwww.common.context;

import java.util.Map;

/**
 * 多层嵌套环境
 * @author lwww 2017年9月2日上午11:02:43
 */
public interface MutiContext extends Context
{
    /**
     * @Desc:创建子级环境，不会重复创建
     * @Date 2017年9月2日上午11:03:08
     * @author liuwww
     * @param contextName
     * @return
     */
    MutiContext createSubContext(String contextName);

    /**
     * @Desc:从指定子级环境中删除
     * @Date 2017年9月2日上午11:04:17
     * @author liuwww
     * @param contextName
     * @param name
     * @return
     */
    Object remove(String contextName, String name);

    /**
     * @Desc:从指定子级环境中获取
     * @Date 2017年9月2日上午11:04:53
     * @author liuwww
     * @param contextName
     * @param name
     * @return
     */
    Object getInSubContext(String contextName, String name);

    /**
     * @Desc:添加中指定子级环境中
     * @Date 2017年9月2日上午11:05:31
     * @author liuwww
     * @param contextName
     * @param name
     * @param val
     * @return
     */
    Object put(String contextName, String name, Object val);

    /**
     * 返回父亲
     * 
     * @return
     */
    MutiContext getParent();

    /**
     * 设置父亲
     * 
     * @param parent
     */
    void setParent(MutiContext parent);

    /**
     * 添加子环境
     * 
     * @param contextName 子环境
     * @param context
     * @return
     */
    MutiContext putSubContext(String contextName, MutiContext context);

    /**
     * 删除子上下文
     * 
     * @param contextName 子环境
     */
    MutiContext removeSubContext(String contextName);

    /**
     * 返回子环境
     * 
     * @param contextName 子环境
     * @return 子环境
     */
    MutiContext getSubContext(String contextName);

    /**
     * 删除所有子上下文
     */
    void clearSubContext();

    /**
     * 返回子环境MAP
     * 
     * @return 子环境MAP
     */
    Map<String, MutiContext> getSubContextMap();

    /**
     * 是否含有子级环境
     */
    MutiContext contain(String name);

}