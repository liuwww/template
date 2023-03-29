package org.liuwww.db.update;

/**
 * 为灵活处理更新单条或多条数据的接口
 * @Date:2023年3月28日下午9:29:58
 * @author liuwww
 */
public interface UpdateBean
{
    int update();

    /**
     * 添加更新的条件
     * @param name
     * @param val
     * @return UpdateBean 返回自身
     */
    UpdateBean addCondition(String name, Object val);

    /**
     * 添加要更新的字段
     * @Date:2023年3月29日下午4:11:02
     * @author liuwww
     * @param field 更新的字段
     * @param val 字段的值
     * @return UpdateBean 返回自身
     */
    UpdateBean addValue(String field, Object val);

}
