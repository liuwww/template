package org.liuwww.db.sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.liuwww.db.condition.Conditions;
import org.liuwww.db.condition.GroupCondition;
import org.liuwww.db.condition.OneCondition;
import org.liuwww.db.context.DbContext;
import org.liuwww.db.context.TableMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.liuwww.common.entity.Order;
import org.liuwww.common.entity.TableEntity;
import org.liuwww.common.execption.DbException;
import org.liuwww.common.util.BeanUtil;
import org.liuwww.common.util.StringUtil;
import org.liuwww.common.util.XmlReaderUtil;

public class SqlBeanUtil
{
    private static Logger logger = LoggerFactory.getLogger(SqlBeanUtil.class);

    private static Map<DbType, SqlBeanBuilder> sqlBuilderMap;
    static
    {
        sqlBuilderMap = new HashMap<DbType, SqlBeanBuilder>();
        sqlBuilderMap.put(DbType.MYSQL, new MySQLSqlBeanBuilder());
        sqlBuilderMap.put(DbType.ORACLE, new OracleSqlBeanBuilder());
        sqlBuilderMap.put(DbType.OTHER, new DefaultSqlBeanBuilder());
    }

    public static void registerBuilder(DbType dbType, SqlBeanBuilder builder)
    {
        sqlBuilderMap.put(dbType, builder);
    }

    public static SqlBean getInsertSqlBean(Row row)
    {
        SqlBeanBuilder builder = getSqlBuilder(row.getDbType());
        return builder.buildInsert(row);
    }

    public static SqlBean getSqlBean(Table table)
    {
        return getSqlBuilder(table.getTableMetaData().getDbType()).buildQuery(table);
    }

    public static JdbcTemplate getDefaultJdbcTemplate()
    {
        List<JdbcTemplate> list = BeanUtil.getBeanList(JdbcTemplate.class);
        if (list.size() > 0)
        {
            return list.get(0);
        }
        return null;
    }

    private static JdbcTemplate getJdbcTemplate(String fileName, String tag)
    {
        JdbcTemplate jdbcTemplate = null;
        String tempName = XmlReaderUtil.getProp("sql/" + fileName, tag + ".jdbcTemplate");
        if (tempName != null)
        {
            jdbcTemplate = (JdbcTemplate) BeanUtil.getBean(tempName);
        }
        if (jdbcTemplate == null)
        {
            jdbcTemplate = getDefaultJdbcTemplate();
        }
        return jdbcTemplate;
    }

    public static SqlBean getOriginalSqlBean(String fileName, String tag)
    {
        String sql = XmlReaderUtil.getProp("sql/" + fileName, tag + ".sql");
        if (sql == null)
        {
            throw new DbException("文件：+" + fileName + "的sql:" + tag + "不存在！");
        }
        String orderBy = XmlReaderUtil.getProp("sql/" + fileName, tag + ".order-by");
        String jdbcTemplateStr = XmlReaderUtil.getProp("sql/" + fileName, tag + ".jdbcTemplate");
        JdbcTemplate jdbcTemplate = null;
        if (StringUtils.isNotBlank(jdbcTemplateStr))
        {
            jdbcTemplate = (JdbcTemplate) BeanUtil.getBean(jdbcTemplateStr);
        }
        if (jdbcTemplate == null)
        {
            jdbcTemplate = getDefaultJdbcTemplate();
        }
        return new DefaultSqlBean(sql, orderBy, jdbcTemplate);
    }

    public static SqlBean getSqlBean(String fileName, String tag, Object[] params)
    {
        SqlBean bean = getOriginalSqlBean(fileName, tag);
        if (bean.getJdbcTemplate() == null)
        {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(fileName, tag);
            bean.setJdbcTemplate(jdbcTemplate);
        }
        bean.setParams(params);
        return bean;
    }

    public static SqlBean getSqlBean(String fileName, String tag, Map<String, Object> paramMap)
    {
        SqlBean bean = getOriginalSqlBean(fileName, tag);
        if (bean.getJdbcTemplate() == null)
        {
            bean.setJdbcTemplate(getDefaultJdbcTemplate());
        }
        DbType dbType = DbContext.getDbTypeForJdbcTemlate(bean.getJdbcTemplate());
        SqlBeanBuilder builder = getSqlBuilder(dbType);
        bean = builder.buildQuery(bean, paramMap);
        return bean;
    }

    public static SqlBean getSqlBean(String fileName, String tag, Conditions conditions)
    {
        SqlBean bean = getOriginalSqlBean(fileName, tag);
        if (bean.getJdbcTemplate() == null)
        {
            bean.setJdbcTemplate(getDefaultJdbcTemplate());
        }
        DbType dbType = DbContext.getDbTypeForJdbcTemlate(bean.getJdbcTemplate());
        SqlBeanBuilder builder = getSqlBuilder(dbType);
        bean = builder.buildQuery(bean, conditions);
        return bean;
    }

    public static <T> SqlBean getSqlBean(TableEntity<T> entity, JdbcTemplate jdbcTemplate, Order order)
    {
        DbType dbtype = DbContext.getDbType(entity.tableName(), jdbcTemplate);
        SqlBeanBuilder builder = getSqlBuilder(dbtype);
        TableMetaData tmd = DbContext.getTableMetaData(entity.tableName(), jdbcTemplate);
        return builder.buildQuery(entity, tmd, order);
    }

    public static SqlBean getSqlBean(String tableName, Map<String, Object> paramMap, Order order)
    {

        DbType dbType = DbContext.getDbType(tableName, null);
        return getSqlBuilder(dbType).buildQuery(tableName, paramMap, order);
    }

    public static SqlBeanBuilder getSqlBuilder(DbType dbType)
    {
        SqlBeanBuilder builder = sqlBuilderMap.get(dbType);
        if (builder == null)
        {
            builder = sqlBuilderMap.get(DbType.OTHER);
        }
        return builder;
    }

    public static SqlBean getUpdateSqlBean(Row row)
    {
        return getSqlBuilder(row.getDbType()).buildUpdate(row);
    }

    public static SqlBean getDeleteSqlBean(Row row)
    {
        return getSqlBuilder(row.getDbType()).buildDelete(row);
    }

    public static String getDeleteSql(TableMetaData tmd)
    {
        return getSqlBuilder(tmd.getDbType()).buildDeleteSql(tmd);
    }

    public static SqlBean getUpdateRowsSqlBean(String tableName, Map<String, Object> valMap,
            Map<String, Object> paramMap, TableMetaData tmd)
    {
        return getSqlBuilder(tmd.getDbType()).buildUpdate(tableName, valMap, paramMap, tmd);
    }

    public static String getConditonSqlFragment(OneCondition oneCondition, DbType dbType)
    {
        return getSqlBuilder(dbType).buildConditonSqlFragment(oneCondition, dbType);
    }

    public static String getConditonSqlFragment(GroupCondition groupCondition, DbType dbType)
    {
        return getSqlBuilder(dbType).buildConditonSqlFragment(groupCondition, dbType);
    }

    public static void checkSqlBeanJdbcTemplate(String tableName, JdbcTemplate jdbcTemplate, SqlBean bean)
    {
        if (jdbcTemplate != null)
        {
            bean.setJdbcTemplate(jdbcTemplate);
        }
        if (bean.getJdbcTemplate() == null)
        {
            bean.setJdbcTemplate(DbContext.getJdbcTemplateForTable(tableName));
        }
    }

    public static String getInsertSql(TableMetaData tmd)
    {
        return getSqlBuilder(tmd.getDbType()).buildInsertSql(tmd);
    }

    public static void addSqlOrderBy(StringBuilder sql, Order order, TableMetaData tmd)
    {
        addSqlOrderBy(sql, order, tmd, false);
    }

    public static void addSqlOrderBy(StringBuilder sql, Order order, TableMetaData tmd, boolean hasOrder)
    {
        if (order != null)
        {
            String field = order.getField();
            String sort = order.getSort();
            if (StringUtil.isNotBlank(field))
            {
                if (sort == null)
                {
                    sort = "";
                }
                if (tmd != null)
                {
                    Column c = tmd.getColumn(field);
                    if (c != null)
                    {
                        if (!hasOrder)
                        {
                            sql.append(" order by");
                            sql.append(" ").append(c.getColumnName()).append(" ").append(sort);
                        }
                        else
                        {
                            sql.append(" ,").append(c.getColumnName()).append(" ").append(sort);
                        }
                    }
                    else
                    {
                        if (logger.isWarnEnabled())
                        {
                            logger.warn("排序字段：{}，在表或视图{}中不存在", field, tmd.getTableName());
                        }
                    }
                }
                else
                {
                    if (!hasOrder)
                    {
                        sql.append(" order by");
                        sql.append(" ").append(field).append(" ").append(sort);
                    }
                    else
                    {
                        sql.append(" ,").append(field).append(" ").append(sort);
                    }
                }
            }
        }
    }

}
