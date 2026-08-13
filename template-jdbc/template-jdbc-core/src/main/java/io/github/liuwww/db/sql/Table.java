package io.github.liuwww.db.sql;

import java.util.ArrayList;
import java.util.List;

import io.github.liuwww.db.config.DatabaseConfigCenter;
import io.github.liuwww.db.condition.AbstractCompare;
import io.github.liuwww.db.condition.Condition;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.executor.DatabaseExecutor;

import io.github.liuwww.common.execption.DbException;
import io.github.liuwww.common.util.StringUtil;

public class Table extends AbstractCompare<Table> {
    private String name;

    private List<Field> fieldList;

    private TableMetaData tmd;

    private DatabaseExecutor databaseExecutor;

    private DatabaseConfigCenter configCenter;

    public DatabaseExecutor getDatabaseExecutor() {
        return databaseExecutor;
    }

    public void setDatabaseExecutor(DatabaseExecutor databaseExecutor) {
        if (databaseExecutor != null) {
            this.databaseExecutor = databaseExecutor;
        } else if (configCenter != null) {
            // 使用配置中心的执行器
            this.databaseExecutor = configCenter.getDatabaseExecutor();
        }
        setTmd();
    }

    public void setConfigCenter(DatabaseConfigCenter configCenter) {
        this.configCenter = configCenter;
    }

    public List<Condition> getConditionList() {
        return conditionList;
    }

    public void setConditionList(List<Condition> conditionList) {
        this.conditionList = conditionList;
    }

    public Table() {
        fieldList = new ArrayList<Field>();
    }

    public Table(String tableName) {
        this(tableName, null);

    }

    public Table(String tableName, DatabaseExecutor databaseExecutor) {
        this();
        this.name = tableName;
        setDatabaseExecutor(databaseExecutor);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setTmd();
    }

    private void setTmd() {
        if (StringUtil.isBlank(this.name)) {
            throw new DbException("table name 不可为空！");
        }
        // 从配置中心获取表元数据
        if (configCenter != null) {
            this.tmd = configCenter.getTableMetaData(this.name);
        }
        if (this.tmd == null) {
            throw new DbException("表 (视图)【" + this.name + "】不存在或没加载，请确保已通过 setConfigCenter 注入配置中心");
        }
    }

    public Table addField(String field, String alias) {
        fieldList.add(new Field(field, alias));
        return this;
    }

    public Table addField(String field) {
        fieldList.add(new Field(field));
        return this;
    }

    public Table addField(Field field) {
        fieldList.add(field);
        return this;
    }

    public List<Field> getFieldList() {
        return fieldList;
    }

    @Override
    protected String getColumn(String field) {
        if (tmd == null) {
            throw new DbException("请先设置正确的 tableName 值！");
        }
        Column column = tmd.getColumn(field);
        if (column == null) {
            return null;
        }
        return super.getColumn(column.getColumnName());
    }

    public TableMetaData getTableMetaData() {
        return tmd;
    }

}