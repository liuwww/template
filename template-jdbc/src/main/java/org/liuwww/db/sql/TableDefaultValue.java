package org.liuwww.db.sql;

import java.util.Map;

import org.liuwww.db.context.TableMetaData;

/**
 * 表新增数据或者更新数据时 一些默认值的设置
 * @author lwww 2017年5月25日下午4:40:37
 */
public interface TableDefaultValue
{

    public static String BEAN_NAME_SUFFIX = "_dbTableDefaultValue";

    /**
     * @Desc:新增数据时，默认值的设置
     * @Date 2017年5月25日下午4:41:22
     * @author liuwww
     * @param tmd 
     * @param Row  
     * @return
     */
    public Map<String, Object> getNewDefaultValue(TableMetaData tmd);

    /**
     * @Desc:更新数据时，默认值的设置
     * @Date 2017年5月25日下午4:41:22
     * @author liuwww
     * @param row  
     * @param tmd 
     * @return
     */
    public Map<String, Object> getUpdateDefaultValue(TableMetaData tmd);
}
