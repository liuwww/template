package org.liuwww.db.sql;

import java.util.Map;

import org.liuwww.common.entity.Order;
import org.liuwww.common.entity.TableEntity;
import org.liuwww.db.condition.Conditions;
import org.liuwww.db.condition.GroupCondition;
import org.liuwww.db.condition.OneCondition;
import org.liuwww.db.context.TableMetaData;

public interface SqlBeanBuilder
{
    public SqlBean buildQuery(Table table);

    public <T> SqlBean buildQuery(TableEntity<T> entity);

    public <T> SqlBean buildQuery(TableEntity<T> entity, TableMetaData tmd, Order order);

    public SqlBean buildInsert(Row row);

    public SqlBean buildUpdate(Row row);

    public SqlBean buildDelete(Row row);

    public SqlBean buildQuery(SqlBean bean, Map<String, Object> paramMap);

    public SqlBean buildQuery(SqlBean bean, Conditions conditions);

    public String buildConditonSqlFragment(OneCondition oneCondition, DbType dbType);

    public String buildConditonSqlFragment(GroupCondition groupCondition, DbType dbType);

    public SqlBean buildQuery(String tableName, Map<String, Object> paramMap, Order order);

    public SqlBean buildUpdate(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap,
            TableMetaData tmd);

    public String getConditionField(String field, TableMetaData tmd);

    public String buildDeleteSql(TableMetaData tmd);

    public String buildInsertSql(TableMetaData tmd);

}
