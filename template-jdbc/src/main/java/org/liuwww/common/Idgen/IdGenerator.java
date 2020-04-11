package org.liuwww.common.Idgen;

import org.liuwww.db.context.TableMetaData;

public interface IdGenerator
{
    /**
     * @desc:返回null时使用auto-generated方式生成
     * @Date:2019年7月28日下午9:29:08
     * @author liuwww
     * @param tableName
     * @param tmd
     * @return
     */
    public String nextKey(String tableName, TableMetaData tmd);

    /**
     * @desc:返回true表明主键有数据库自动生成
     * @Date:2019年8月8日下午8:28:20
     * @author liuwww
     * @param tableName
     * @param tmd
     * @return
     */
    public boolean isCreatedByDatabase(String tableName, TableMetaData tmd);
}
