package org.liuwww.db.update;

public interface DeleteBean
{
    int delete();

    /**
     * 添加删除的条件
     * @param name
     * @param val
     * @return UpdateBean 返回自身
     */
    DeleteBean addCondition(String name, Object val);
}
