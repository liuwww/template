package org.liuwww.db.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.liuwww.db.condition.CompareOpe;
import org.liuwww.db.condition.Condition;
import org.liuwww.db.condition.OneCondition;
import org.liuwww.db.context.DbContext;
import org.liuwww.db.context.TableMetaData;
import org.liuwww.common.entity.Order;
import org.liuwww.common.entity.TableEntity;
import org.liuwww.common.execption.DbException;
import org.liuwww.common.util.EntryUtil;
import org.liuwww.common.util.StringUtil;

public class MySQLSqlBeanBuilder extends AbstractSqlBeanBuilder implements SqlBeanBuilder
{

    @Override
    public SqlBean buildQuery(Table table)
    {
        List<Object> params = table.getParamList();
        TableMetaData tmd = table.getTableMetaData();
        if (tmd == null)
        {
            throw new DbException("表【" + table.getName() + "】不存在或没加载！");
        }
        DbType dbType = tmd.getDbType();
        StringBuffer sql = new StringBuffer("select ");
        if (table.getFieldList().size() == 0)
        {
            for (Column c : tmd.getColumnList())
            {
                sql.append("`").append(c.getColumnName()).append("`,");
            }
            sql.deleteCharAt(sql.length() - 1);
        }
        else
        {
            for (Field f : table.getFieldList())
            {
                boolean valid = false;
                Column c = tmd.getColumn(f.getField());
                if (c != null)
                {
                    sql.append('`').append(c.getColumnName()).append('`');
                    valid = true;
                }
                else
                {
                    // sql.append('`').append(f.getField()).append('`');
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("table or view {} 没有字段：{}", tmd.getTableName(), f.getField());
                    }
                }

                if (StringUtils.isNotBlank(f.getAlias()))
                {
                    sql.append(" as ").append('`').append(f.getAlias()).append('`');
                }
                if (valid)
                {
                    sql.append(",");
                }
            }
            sql = sql.deleteCharAt(sql.length() - 1);
        }
        sql.append(" from ").append(tmd.getTableName());
        boolean hasConditons = false;
        if (table.getConditionList().size() > 0)
        {
            sql.append(" where ");
            boolean frist = true;
            for (Condition c : table.getConditionList())
            {
                if (c.isValid())
                {
                    hasConditons = true;
                    if (frist)
                    {
                        sql.append(c.getSqlFragment(dbType).substring(4));
                        frist = false;
                    }
                    else
                    {
                        sql.append(c.getSqlFragment(dbType));
                    }
                }

            }
        }
        if (table.getConditionList().size() == 0)
        {
            sql.append(" where 1=1 ");
        }
        else if (hasConditons)
        {
            sql.append(" ");
        }

        SqlBean sqlBean = new DefaultSqlBean(sql.toString(), params.toArray(), null, tmd.getDbType(),
                table.getJdbcTemplate());
        sqlBean.setTableMetaData(table.getTableMetaData());
        sqlBean.setTables(new String[]
        { table.getName() });
        return sqlBean;
    }

    @Override
    public <T> SqlBean buildQuery(TableEntity<T> entity)
    {
        return buildQuery(entity, null, null);
    }

    @Override
    public SqlBean buildInsert(Row row)
    {
        StringBuilder sql = new StringBuilder("insert into ");
        StringBuilder subsql = new StringBuilder();
        sql.append(row.getTableName());
        sql.append("(");
        Map<String, Object> map = row.getRowValueMap();
        List<Object> params = new ArrayList<Object>(map.size());
        for (String key : map.keySet())
        {
            sql.append('`').append(key).append("`,");
            params.add(map.get(key));
            subsql.append("?,");
        }

        sql = sql.delete(sql.length() - 1, sql.length());
        sql.append(") ");
        sql.append("values(");
        subsql = subsql.delete(subsql.length() - 1, subsql.length());
        sql.append(subsql);
        sql.append(")");
        return new DefaultSqlBean(sql, params.toArray(), null, DbType.MYSQL, null);
    }

    @Override
    public String buildInsertSql(TableMetaData tmd)
    {
        StringBuilder sql = new StringBuilder("insert into ");
        StringBuilder subsql = new StringBuilder();
        sql.append('`').append(tmd.getTableName()).append('`');
        sql.append('(');
        List<Column> list = tmd.getColumnList();
        for (Column c : list)
        {
            sql.append('`').append(c.getColumnName()).append("`,");
            subsql.append("?,");
        }
        sql = sql.delete(sql.length() - 1, sql.length());
        sql.append(") ");
        sql.append("values(");
        subsql = subsql.delete(subsql.length() - 1, subsql.length());
        sql.append(subsql);
        sql.append(")");
        return sql.toString();
    }

    @Override
    public SqlBean buildUpdate(Row row)
    {
        StringBuilder sql = new StringBuilder("update ");
        sql.append(row.getTableName()).append(" set ");

        Map<String, Object> map = row.getRowValueMap();
        List<Object> params = new ArrayList<Object>();
        for (String key : map.keySet())
        {
            sql.append('`').append(key).append('`').append("=?,");
            params.add(map.get(key));
        }
        sql = sql.delete(sql.length() - 1, sql.length());
        sql.append(" where ");
        if (StringUtil.isNotBlank(row.getIdValue()))
        {
            sql.append(row.getIdName()).append("=?");
            params.add(row.getIdValue());
        }
        else if (row.getIdValues() != null && row.getIdValues().length > 0)
        {
            String[] idValues = row.getIdValues();
            String[] idNames = row.getIdNames();
            for (int i = 0; i < idNames.length; i++)
            {
                sql.append(idNames[i]).append("=? and ");
                params.add(idValues[i]);
            }
            sql.delete(sql.length() - 4, sql.length());
        }
        else
        {
            sql.append("1=2");
        }

        return new DefaultSqlBean(sql.toString(), params.toArray(), null, DbType.MYSQL, null);
    }

    @Override
    public SqlBean buildQuery(SqlBean bean, Map<String, Object> paramMap)
    {
        TableMetaData tmd = bean.getTableMetaData();
        DefaultSqlBean dsb = (DefaultSqlBean) bean;
        StringBuilder sql = dsb.getOriginalSql();
        for (Entry<String, Object> e : paramMap.entrySet())
        {
            String field = e.getKey();
            Object val = e.getValue();
            Column column = tmd.getColumn(field);
            if (column != null && val != null && StringUtils.isNotBlank(val.toString()))
            {
                sql.append("and `").append(column.getColumnName()).append('`');
                CompareOpe ope = null;
                joinCondition(sql, dsb.getParamsList(), val, ope);
            }
        }
        return dsb;
    }

    @Override
    public String buildConditonSqlFragment(OneCondition c, DbType dbType)
    {
        if (!c.isColumnField())
        {
            return super.buildConditonSqlFragment(c, dbType);
        }
        CompareOpe ope = c.getOpe();
        if (ope == CompareOpe.notNull)
        {
            return new StringBuilder(c.getCondtionRel().getVal()).append("  `").append(c.getField())
                    .append("` is not null ").toString();
        }
        else if (ope == CompareOpe.emptyStr)
        {
            return new StringBuilder(c.getCondtionRel().getVal()).append("  `").append(c.getField()).append("`='' ")
                    .toString();
        }
        else if (ope == CompareOpe.isNull)
        {
            return new StringBuilder(c.getCondtionRel().getVal()).append("  `").append(c.getField())
                    .append("` is null ").toString();
        }
        else
        {
            return new StringBuilder(c.getCondtionRel().getVal()).append("  `").append(c.getField()).append("` ")
                    .append(c.getOpe().getVal()).append(" ? ").toString();
        }

    }

    @Override
    public <T> SqlBean buildQuery(TableEntity<T> entity, TableMetaData tmd, Order order)
    {
        if (tmd == null)
        {
            tmd = DbContext.getTableMetaData(entity.tableName(), null);
        }

        if (tmd == null)
        {
            throw new RuntimeException("表" + entity.tableName() + "不存在或配置数据未刷新");
        }
        StringBuilder sql = new StringBuilder("select ");
        List<Object> paramList = new ArrayList<Object>();
        for (Column column : tmd.getColumnList())
        {
            sql.append('`').append(column.getColumnName()).append('`').append(",");
        }
        sql = sql.delete(sql.length() - 1, sql.length());
        sql.append(" from ").append(entity.tableName());
        sql.append(" where 1=1 ");
        for (String field : EntryUtil.getFieldList(entity.getClass()))
        {
            Column column = tmd.getColumn(field);
            if (column != null)
            {
                Object val = EntryUtil.getFieldValue(entity, field);
                if (val != null && StringUtils.isNotBlank(val.toString()))
                {
                    sql.append("and `").append(column.getColumnName()).append('`');
                    joinCondition(sql, paramList, val, CompareOpe.eq);
                }
            }
        }
        SqlBeanUtil.addSqlOrderBy(sql, order, tmd);
        return new DefaultSqlBean(sql, paramList, null, DbType.MYSQL, new String[]
        { entity.tableName() }, null);

    }

    @Override
    public SqlBean buildQuery(String tableName, Map<String, Object> paramMap, Order order)
    {
        TableMetaData tmd = DbContext.getTableMetaData(tableName, null);
        StringBuilder sql = null;
        List<Object> paramList = new ArrayList<Object>(tmd.getColumnList().size());
        if (tmd != null)
        {
            sql = new StringBuilder("select ");

            for (Column column : tmd.getColumnList())
            {
                sql.append('`').append(column.getColumnName()).append('`').append(",");
            }
            sql = sql.delete(sql.length() - 1, sql.length());
            sql.append(" from ").append(tableName);
            sql.append(" where 1=1 ");

            for (Entry<String, Object> entry : paramMap.entrySet())
            {
                String field = entry.getKey();
                Object val = entry.getValue();
                Column column = tmd.getColumn(field);
                if (column != null && val != null && StringUtil.isNotBlank(val.toString()))
                {
                    sql.append("and ").append('`').append(column.getColumnName()).append('`');
                    joinCondition(sql, paramList, val, CompareOpe.eq);
                }
            }
        }
        SqlBeanUtil.addSqlOrderBy(sql, order, tmd);
        SqlBean bean = new DefaultSqlBean(sql, paramList.toArray(), null, tmd.getDbType(), null);
        return bean;
    }

    @Override
    public SqlBean buildUpdate(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap,
            TableMetaData tmd)
    {
        if (tmd == null)
        {
            tmd = DbContext.getTableMetaData(tableName, null);
        }

        StringBuilder sql = new StringBuilder("update ");
        sql.append(tmd.getTableName()).append(" set ");
        List<Object> params = new ArrayList<Object>(valMap.size());
        for (Entry<String, Object> entry : valMap.entrySet())
        {
            Column column = tmd.getColumn(entry.getKey());
            if (column != null)
            {
                sql.append('`').append(column.getColumnName()).append("`=?,");
                params.add(entry.getValue());
            }
        }
        if (params.size() == 0)
        {
            throw new DbException("更新数据时无有效更新字段！");
        }
        int len = params.size();
        sql = sql.delete(sql.length() - 1, sql.length());

        sql.append(" where ");
        for (Entry<String, Object> entry : paramMap.entrySet())
        {
            Column column = tmd.getColumn(entry.getKey());
            if (column != null)
            {
                sql.append('`').append(column.getColumnName()).append("`=? and ");
                params.add(entry.getValue());
            }
        }
        if (len == params.size())
        {
            throw new DbException("更新数据时无有效条件字段！");
        }
        sql = sql.delete(sql.length() - 4, sql.length());
        return new DefaultSqlBean(sql, params.toArray(), null, tmd.getDbType(), null);
    }

    @Override
    public String getConditionField(String field, TableMetaData tmd)
    {
        Column c = tmd.getColumn(field);
        if (c != null)
        {
            return '`' + c.getColumnName() + '`';
        }
        return null;
    }

    @Override
    public String buildDeleteSql(TableMetaData tmd)
    {
        boolean unionKey = tmd.isUnionKey();
        if (unionKey)
        {
            Column[] idColumns = tmd.getIdColumns();
            StringBuilder sql = new StringBuilder("delete from ");
            sql.append('`').append(tmd.getTableName()).append('`').append(" where ");
            for (int i = 0; i < idColumns.length; i++)
            {
                sql.append('`').append(idColumns[i].getColumnName()).append('`').append("=? ");

                if (i != idColumns.length - 1)
                {
                    sql.append("and ");
                }
            }
            return sql.toString();
        }
        else
        {
            Column idColumn = tmd.getIdColumn();
            if (idColumn == null)
            {
                throw new DbException("表[" + tmd.getTableName() + "]没有主键");
            }
            StringBuilder sql = new StringBuilder("delete from ");
            sql.append('`').append(tmd.getTableName()).append('`').append(" where ");
            sql.append('`').append(idColumn.getColumnName()).append('`').append("=? ");
            return sql.toString();
        }
    }

    @Override
    public String buildInsertSql(TableMetaData tmd, boolean withId)
    {
        StringBuilder sql = new StringBuilder("insert into ");
        StringBuilder subsql = new StringBuilder();
        sql.append(tmd.getTableName());
        sql.append('(');
        List<Column> list = tmd.getColumnList();
        Column idColumn = tmd.getIdColumn();
        for (Column c : list)
        {
            if (!withId && c.equals(idColumn))
            {
                continue;
            }
            sql.append("`").append(c.getColumnName()).append("`,");
            subsql.append("?,");
        }
        sql = sql.delete(sql.length() - 1, sql.length());
        sql.append(") ");
        sql.append("values(");
        subsql = subsql.delete(subsql.length() - 1, subsql.length());
        sql.append(subsql);
        sql.append(")");
        return sql.toString();
    }

}
