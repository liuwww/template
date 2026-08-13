package io.github.liuwww.db.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.github.liuwww.common.util.StringUtil;
import io.github.liuwww.db.condition.CompareOpe;
import io.github.liuwww.db.condition.Condition;
import io.github.liuwww.db.condition.Conditions;
import io.github.liuwww.db.condition.GroupCondition;
import io.github.liuwww.db.condition.OneCondition;
import io.github.liuwww.db.context.DbContext;
import io.github.liuwww.db.context.TableMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.liuwww.common.entity.Order;
import io.github.liuwww.common.entity.TableEntity;
import io.github.liuwww.common.execption.DbException;
import io.github.liuwww.common.util.EntryUtil;
import io.github.liuwww.common.util.StringUtil;

/**
 * SQL 构建器抽象基类
 * <p>
 * 实现了 SqlBeanBuilder 接口的基础方法，提供通用的 SQL 构建逻辑。
 * 不同数据库的特殊语法由子类（如 MySQLSqlBeanBuilder）重写实现。
 * </p>
 * 
 * @author liuwww
 * @since 2017-05-11
 */
public abstract class AbstractSqlBeanBuilder implements SqlBeanBuilder {

    /** 日志记录器 */
    protected static Logger logger = LoggerFactory.getLogger(AbstractSqlBeanBuilder.class);

    /**
     * 创建默认的 SQL 构建器实例
     */
    public AbstractSqlBeanBuilder() {
    }

    @Override
    public SqlBean buildQuery(Table table) {
        List<Object> params = table.getParamList();
        TableMetaData tmd = table.getTableMetaData();
        if (tmd == null) {
            throw new DbException("表【" + table.getName() + "】不存在或没加载！");
        }
        DbType dbType = tmd.getDbType();
        StringBuffer sql = new StringBuffer("select ");
        if (table.getFieldList().size() == 0) {
            for (Column c : tmd.getColumnList()) {
                sql.append(c.getColumnName()).append(",");
            }
            sql.deleteCharAt(sql.length() - 1);
        } else {
            for (Field f : table.getFieldList()) {
                Column c = tmd.getColumn(f.getField());
                if (c != null) {
                    sql.append(c.getColumnName());
                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn("table or view {} 没有字段：{}", tmd.getTableName(), f.getField());
                    }
                }
                if (StringUtil.isNotBlank(f.getAlias())) {
                    sql.append(" as ").append(f.getAlias());
                }
                sql.append(",");
            }
            sql = sql.deleteCharAt(sql.length() - 1);
        }
        sql.append(" from ").append(tmd.getTableName());
        boolean hasConditons = false;
        if (table.getConditionList().size() > 0) {
            sql.append(" where ");
            boolean frist = true;
            for (Condition c : table.getConditionList()) {
                if (c.isValid()) {
                    hasConditons = true;
                    if (frist) {
                        sql.append(c.getSqlFragment(dbType).substring(4));
                        frist = false;
                    } else {
                        sql.append(c.getSqlFragment(dbType));
                    }
                }
            }
        }
        if (table.getConditionList().size() == 0) {
            sql.append(" where 1=1 ");
        } else if (hasConditons) {
            sql.append(" ");
        }

        SqlBean sqlBean = new DefaultSqlBean(sql.toString(), params.toArray(new Object[0]), null, tmd.getDbType(),
                table.getDatabaseExecutor());
        sqlBean.setTableMetaData(table.getTableMetaData());
        sqlBean.setTables(new String[] { table.getName() });

        return sqlBean;
    }

    @Override
    public <T> SqlBean buildQuery(TableEntity<T> entity, TableMetaData tmd, Order order) {
        if (tmd == null) {
            throw new DbException("表" + entity.tableName() + "不存在或配置数据未刷新，且未提供 TableMetaData");
        }
        StringBuilder sql = new StringBuilder("select ");
        List<Object> paramList = new ArrayList<Object>();
        for (Column column : tmd.getColumnList()) {
            sql.append(column.getColumnName()).append(",");
        }
        sql = sql.delete(sql.length() - 1, sql.length());
        sql.append(" from ").append(entity.tableName());
        sql.append(" where 1=1 ");
        for (String field : EntryUtil.getFieldList(entity.getClass())) {
            Column column = tmd.getColumn(field);
            if (column != null) {
                Object val = EntryUtil.getFieldValue(entity, field);
                if (val != null && StringUtil.isNotBlank(val.toString())) {
                    sql.append("and ").append(column.getColumnName());
                    joinCondition(sql, paramList, val, CompareOpe.eq);
                }
            }
        }
        SqlBeanUtil.addSqlOrderBy(sql, order, tmd);
        return new DefaultSqlBean(sql, paramList.toArray(new Object[0]), null, tmd.getDbType(), null);
    }

    @Override
    public <T> SqlBean buildQuery(TableEntity<T> entity) {
        return buildQuery(entity, null, null);
    }

    @Override
    public SqlBean buildInsert(Row row) {
        return buildInsert(row, true);
    }

    @Override
    public SqlBean buildInsert(Row row, boolean withId) {
        StringBuilder sql = new StringBuilder("insert into ");
        StringBuilder subsql = new StringBuilder();
        sql.append(row.getTableName());
        sql.append('(');
        Map<String, Object> map = row.getRowValueMap();
        List<Object> params = new ArrayList<Object>(map.size());
        for (Entry<String, Object> entry : map.entrySet()) {
            sql.append(entry.getKey()).append(',');
            params.add(entry.getValue());
            subsql.append("?,");
        }
        if (withId && StringUtil.isNotBlank(row.getIdName()) && map.get(row.getIdName()) == null) {
            sql.append(row.getIdName()).append(",");
            params.add(row.getIdValue());
            subsql.append("?,");
        }
        sql = sql.delete(sql.length() - 1, sql.length());
        sql.append(") ");
        sql.append("values(");
        subsql = subsql.delete(subsql.length() - 1, subsql.length());
        sql.append(subsql);
        sql.append(")");
        return new DefaultSqlBean(sql.toString(), params.toArray(), null, row.getDbType(), null);
    }

    @Override
    public String buildInsertSql(TableMetaData tmd) {
        return buildInsertSql(tmd, true);
    }

    @Override
    public String buildInsertSql(TableMetaData tmd, boolean withId) {
        StringBuilder sql = new StringBuilder("insert into ");
        StringBuilder subsql = new StringBuilder();
        sql.append(tmd.getTableName());
        sql.append('(');
        List<Column> list = tmd.getColumnList();
        Column idColumn = tmd.getIdColumn();
        for (Column c : list) {
            if (!withId && c.equals(idColumn)) {
                continue;
            }
            sql.append(c.getColumnName()).append(',');
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
    public SqlBean buildUpdate(Row row) {
        StringBuilder sql = new StringBuilder("update ");
        sql.append(row.getTableName()).append(" set ");

        Map<String, Object> map = row.getRowValueMap();
        List<Object> params = new ArrayList<Object>();
        for (String key : map.keySet()) {
            sql.append(key).append("=?,");
            params.add(map.get(key));
        }
        sql = sql.delete(sql.length() - 1, sql.length());
        sql.append(" where ").append(row.getIdName()).append("=?");
        params.add(row.getIdValue());
        return new DefaultSqlBean(sql.toString(), params.toArray(), null, row.getDbType(), null);
    }

    @Override
    public SqlBean buildDelete(Row row) {
        StringBuilder sql = new StringBuilder("delete from ");
        sql.append(row.getTableName()).append(" where ");
        Map<String, Object> map = row.getRowValueMap();
        List<Object> params = new ArrayList<Object>(map.size());
        for (Entry<String, Object> entry : map.entrySet()) {
            sql.append(entry.getKey()).append("=? and ");
            params.add(entry.getValue());
        }
        sql = sql.delete(sql.length() - 4, sql.length());
        return new DefaultSqlBean(sql, params.toArray(), null, row.getDbType(), null);
    }

    @Override
    public String buildDeleteSql(TableMetaData tmd) {
        Column idColumn = tmd.getIdColumn();
        if (idColumn == null) {
            throw new DbException("表[" + tmd.getTableName() + "]没有主键");
        }
        StringBuilder sql = new StringBuilder("delete from ");
        sql.append(tmd.getTableName()).append(" where ");
        sql.append(idColumn.getColumnName()).append("=? ");
        return sql.toString();
    }

    @Override
    public SqlBean buildQuery(SqlBean bean, Map<String, Object> paramMap) {
        TableMetaData tmd = bean.getTableMetaData();
        DefaultSqlBean dsb = (DefaultSqlBean) bean;
        StringBuilder sql = dsb.getOriginalSql();
        for (Entry<String, Object> e : paramMap.entrySet()) {
            String field = e.getKey();
            Object val = e.getValue();
            Column column = tmd.getColumn(field);
            if (column != null && val != null && StringUtil.isNotBlank(val.toString())) {
                sql.append("and ").append(column.getColumnName());
                CompareOpe ope = null;
                joinCondition(sql, dsb.getParamsList(), val, ope);
            }
        }
        return dsb;
    }

    @Override
    public SqlBean buildQuery(SqlBean bean, Conditions conditions) {
        DefaultSqlBean dsb = (DefaultSqlBean) bean;
        StringBuilder sql = dsb.getOriginalSql();
        for (Condition c : conditions) {
            if (c.isValid()) {
                sql.append(" ").append(c.getSqlFragment(bean.getDbType()));
            }
        }
        return dsb;
    }

    /**
     * 拼接条件 SQL 片段
     *
     * @param sql       SQL 构建器
     * @param paramList 参数列表
     * @param val       参数值
     * @param ope       比较操作符
     */
    protected static void joinCondition(StringBuilder sql, List<Object> paramList, Object val, CompareOpe ope) {
        if (ope == null) {
            ope = CompareOpe.eq;
        }
        switch (ope) {
            case eq:
                sql.append("=? ");
                paramList.add(val);
                break;
            case lt:
                sql.append("<? ");
                paramList.add(val);
                break;
            case gt:
                sql.append(">? ");
                paramList.add(val);
                break;
            case ne:
                sql.append("!=? ");
                paramList.add(val);
                break;
            case le:
                sql.append("<=? ");
                paramList.add(val);
                break;
            case ge:
                sql.append(">=? ");
                paramList.add(val);
                break;
            case like:
                sql.append(" like ? ");
                paramList.add("%" + val + "%");
                break;
            default:
                sql.append("=? ");
                paramList.add(val);
        }
    }

    @Override
    public String buildConditonSqlFragment(GroupCondition group, DbType dbType) {
        if (group.size() > 0) {
            StringBuilder sql = new StringBuilder(group.getConditionRel().getVal());
            sql.append(" (");

            boolean first = true;
            for (Condition condition : group) {
                if (condition.isValid()) {
                    if (first) {
                        sql.append(condition.getSqlFragment(dbType).substring(4));
                        first = false;
                    } else {
                        sql.append(condition.getSqlFragment(dbType));
                    }
                }
            }
            sql.append(")");
            return sql.toString();
        }
        return "";
    }

    @Override
    public SqlBean buildQuery(String tableName, Map<String, Object> paramMap, Order order) {
        TableMetaData tmd = DbContext.getTableMetaData(tableName, null);
        if (tmd == null) {
            throw new DbException("表【" + tableName + "】不存在或没加载！");
        }
        return buildQuery(tableName, paramMap, order, tmd);
    }

    @Override
    public SqlBean buildQuery(String tableName, Map<String, Object> paramMap, Order order, TableMetaData tmd) {
        if (tmd == null) {
            throw new DbException("表【" + tableName + "】不存在或没加载！");
        }
        StringBuilder sql = new StringBuilder("select ");
        List<Object> paramList = new ArrayList<Object>(tmd.getColumnList().size());

        for (Column column : tmd.getColumnList()) {
            sql.append(column.getColumnName()).append(",");
        }
        sql = sql.delete(sql.length() - 1, sql.length());
        sql.append(" from ").append(tableName);
        sql.append(" where 1=1 ");

        if (paramMap != null) {
            for (Entry<String, Object> entry : paramMap.entrySet()) {
                String field = entry.getKey();
                Object val = entry.getValue();
                Column column = tmd.getColumn(field);
                if (column != null && val != null && StringUtil.isNotBlank(val.toString())) {
                    sql.append("and ").append(column.getColumnName());
                    joinCondition(sql, paramList, val, CompareOpe.eq);
                }
            }
        }
        SqlBeanUtil.addSqlOrderBy(sql, order, tmd);
        SqlBean bean = new DefaultSqlBean(sql, paramList.toArray(), null, tmd.getDbType(), null);
        return bean;
    }

    /**
     * 构建查询字段列表对应的 SELECT 列片段（不含 SELECT 关键字）。
     * <p>
     * 子类可重写此方法以支持特定数据库的列名转义（如 MySQL 使用反引号）。
     * 当 fieldList 为 null 或空时，返回表的全部列。
     * </p>
     *
     * @param tmd       表元数据
     * @param fieldList 字段列表
     * @return SELECT 后的列片段，如 "id, name as user_name"
     */
    protected String buildSelectColumns(TableMetaData tmd, List<Field> fieldList) {
        StringBuilder columns = new StringBuilder();
        if (fieldList == null || fieldList.isEmpty()) {
            for (Column c : tmd.getColumnList()) {
                columns.append(c.getColumnName()).append(",");
            }
        } else {
            for (Field f : fieldList) {
                Column c = tmd.getColumn(f.getField());
                if (c != null) {
                    columns.append(c.getColumnName());
                    if (StringUtil.isNotBlank(f.getAlias())) {
                        columns.append(" as ").append(f.getAlias());
                    }
                    columns.append(",");
                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn("table or view {} 没有字段：{}", tmd.getTableName(), f.getField());
                    }
                }
            }
        }
        if (columns.length() > 0 && columns.charAt(columns.length() - 1) == ',') {
            columns.deleteCharAt(columns.length() - 1);
        }
        return columns.toString();
    }

    @Override
    public SqlBean buildQuery(String tableName, List<Field> fieldList) {
        TableMetaData tmd = DbContext.getTableMetaData(tableName, null);
        return buildQuery(tableName, fieldList, tmd);
    }

    @Override
    public SqlBean buildQuery(String tableName, List<Field> fieldList, TableMetaData tmd) {
        if (tmd == null) {
            throw new DbException("表【" + tableName + "】不存在或没加载！");
        }
        StringBuilder sql = new StringBuilder("select ");
        sql.append(buildSelectColumns(tmd, fieldList));
        sql.append(" from ").append(tmd.getTableName());
        sql.append(" where 1=1 ");

        SqlBean bean = new DefaultSqlBean(sql.toString(), new Object[0], null, tmd.getDbType(), null);
        bean.setTableMetaData(tmd);
        bean.setTables(new String[] { tableName });
        return bean;
    }

    @Override
    public SqlBean buildUpdate(String tableName, Map<String, Object> valMap, Map<String, Object> paramMap,
            TableMetaData tmd) {
        if (tmd == null) {
            throw new DbException("表【" + tableName + "】不存在或没加载！");
        }

        StringBuilder sql = new StringBuilder("update ");
        sql.append(tmd.getTableName()).append(" set ");
        List<Object> params = new ArrayList<Object>(valMap.size());
        for (Entry<String, Object> entry : valMap.entrySet()) {
            Column column = tmd.getColumn(entry.getKey());
            if (column != null) {
                sql.append(column.getColumnName()).append("=?,");
                params.add(entry.getValue());
            }
        }
        if (params.size() == 0) {
            throw new DbException("更新数据时无有效更新字段！");
        }
        int len = params.size();
        sql = sql.delete(sql.length() - 1, sql.length());

        sql.append(" where ");
        for (Entry<String, Object> entry : paramMap.entrySet()) {
            Column column = tmd.getColumn(entry.getKey());
            if (column != null) {
                sql.append(column.getColumnName()).append("=? and ");
                params.add(entry.getValue());
            }
        }
        if (len == params.size()) {
            throw new DbException("更新数据时无有效条件字段！");
        }
        sql = sql.delete(sql.length() - 4, sql.length());
        return new DefaultSqlBean(sql, params.toArray(), null, tmd.getDbType(), null);
    }

    @Override
    public String buildConditonSqlFragment(OneCondition c, DbType dbType) {
        CompareOpe ope = c.getOpe();
        if (ope == CompareOpe.notNull) {
            return new StringBuilder(c.getConditionRel().getVal()).append("  ").append(c.getField())
                    .append(" is not null ").toString();
        } else if (ope == CompareOpe.emptyStr) {
            return new StringBuilder(c.getConditionRel().getVal()).append("  ").append(c.getField()).append("='' ")
                    .toString();
        } else if (ope == CompareOpe.isNull) {
            return new StringBuilder(c.getConditionRel().getVal()).append("  ").append(c.getField()).append(" is null ")
                    .toString();
        } else if (ope == CompareOpe.in) {
            StringBuilder builder = new StringBuilder(c.getConditionRel().getVal()).append("  ").append(c.getField())
                    .append(" in(");
            int len = c.getParamList().size();
            for (int i = 0; i < len; i++) {
                builder.append("?,");
            }
            builder.delete(builder.length() - 1, builder.length());
            builder.append(")");
            return builder.toString();
        } else {
            return new StringBuilder(c.getConditionRel().getVal()).append("  ").append(c.getField()).append(" ")
                    .append(c.getOpe().getVal()).append(" ? ").toString();
        }
    }

    @Override
    public String getConditionField(String field, TableMetaData tmd) {
        Column c = tmd.getColumn(field);
        if (c != null) {
            return c.getColumnName();
        }
        return null;
    }
}