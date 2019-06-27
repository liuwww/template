package org.liuwww.db.sql;

import java.util.ArrayList;
import java.util.List;

import org.liuwww.db.condition.AbstractCompare;
import org.liuwww.db.condition.Condition;
import org.liuwww.db.context.DbContext;
import org.liuwww.db.context.TableMetaData;
import org.springframework.jdbc.core.JdbcTemplate;

import org.liuwww.common.execption.SysException;
import org.liuwww.common.util.StringUtil;

public class Table extends AbstractCompare<Table>
{
    private String name;

    private List<Field> fieldList;

    private TableMetaData tmd;

    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate()
    {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate)
    {
        if (jdbcTemplate != null)
        {
            this.jdbcTemplate = jdbcTemplate;
        }
        else
        {
            this.jdbcTemplate = DbContext.getJdbcTemplateForTable(name);
        }
        setTmd();
    }

    public List<Condition> getConditionList()
    {
        return conditionList;
    }

    public void setConditionList(List<Condition> conditionList)
    {
        this.conditionList = conditionList;
    }

    public Table()
    {
        fieldList = new ArrayList<Field>();
    }

    public Table(String tableName)
    {
        this(tableName, null);

    }

    public Table(String tableName, JdbcTemplate jdbcTemplate)
    {
        this();
        this.name = tableName;
        setJdbcTemplate(jdbcTemplate);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
        setTmd();
    }

    private void setTmd()
    {
        if (StringUtil.isBlank(this.name))
        {
            throw new SysException("table name 不可为空！");
        }
        tmd = DbContext.getTableMetaData(this.name, this.jdbcTemplate);
    }

    public Table addField(String field, String alias)
    {
        fieldList.add(new Field(field, alias));
        return this;
    }

    public Table addField(String field)
    {
        fieldList.add(new Field(field));
        return this;
    }

    public Table addField(Field field)
    {
        fieldList.add(field);
        return this;
    }

    public List<Field> getFieldList()
    {
        return fieldList;
    }

    @Override
    protected String getColumn(String field)
    {
        if (tmd == null)
        {
            throw new SysException("请先设置正确的tableName值！");
        }
        Column column = tmd.getColumn(field);
        if (column == null)
        {
            return null;
        }
        return super.getColumn(column.getColumnName());
    }

    public TableMetaData getTableMetaData()
    {
        return tmd;
    }

}
