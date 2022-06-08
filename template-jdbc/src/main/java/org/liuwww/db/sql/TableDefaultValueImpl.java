package org.liuwww.db.sql;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.liuwww.db.context.TableMetaData;

/**
 * 表新增数据或者更新数据时 一些默认值的设置,默认实现，可以由实现ITableDefaultValue接口的bean覆盖此实现<br>
 * bean的name规则为tableName_tableDefaultValue,其中tableName大写
 * @author lwww 2017年5月25日下午4:44:25
 */
// @Component(TableDefaultValue.BEAN_NAME_SUFFIX)
public class TableDefaultValueImpl implements TableDefaultValue
{

    @Override
    public Map<String, Object> getNewDefaultValue(TableMetaData tmd)
    {
        Map<String, Object> valueMap = new HashMap<String, Object>(3);
        Column column = tmd.getColumn("sts");
        if (column != null)
        {
            valueMap.put(column.getColumnName(), "A");
        }
        column = tmd.getColumn("stsDate");
        if (column != null)
        {
            valueMap.put(column.getColumnName(), new Timestamp(System.currentTimeMillis()));
        }
        column = tmd.getColumn("createDate");
        if (column != null)
        {
            valueMap.put(column.getColumnName(), new Timestamp(System.currentTimeMillis()));
        }
        return valueMap;
    }

    @Override
    public Map<String, Object> getUpdateDefaultValue(TableMetaData tmd)
    {
        Map<String, Object> valueMap = new HashMap<String, Object>(1);
        Column column = tmd.getColumn("stsDate");
        if (column != null)
        {
            valueMap.put(column.getColumnName(), new Timestamp(System.currentTimeMillis()));
        }
        return valueMap;
    }

}
