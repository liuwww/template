package io.github.liuwww.db.condition;

import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.sql.Column;

/**
 * 条件集合类
 * <p>
 * 继承 AbstractCompare，提供基于表元数据的字段过滤功能。
 * 如果字段不在表结构定义中，则不会作为查询条件。
 * </p>
 * 
 * @author lww
 */
public class Conditions extends AbstractCompare<Conditions> {

    /** 表元数据 */
    protected TableMetaData tmd;

    /** 是否只返回一条记录（用于单条查询） */
    protected boolean isOne = false;

    /**
     * 创建空的条件集合
     */
    public Conditions() {
    }

    /**
     * 创建基于表元数据的条件集合
     *
     * @param tmd   表元数据，用于字段过滤
     * @param isOne 是否只返回一条记录
     */
    public Conditions(TableMetaData tmd, boolean isOne) {
        this.tmd = tmd;
        this.isOne = isOne;
    }

    /**
     * 获取数据库列名
     * <p>
     * 如果 isOne 为 true 且 tmd 不为 null，则会进行字段有效性检查，
     * 只有表结构中存在的字段才会被作为查询条件。
     * </p>
     *
     * @param field Java 字段名或数据库列名
     * @return 数据库列名，如果字段无效则返回 null
     */
    @Override
    protected String getColumn(String field) {
        if (!isOne || tmd == null) {
            return field;
        }
        Column column = tmd.getColumn(field);
        if (column == null) {
            return null;
        }
        return super.getColumn(column.getColumnName());
    }

    @Override
    public TableMetaData getTableMetaData() {
        return tmd;
    }

    /**
     * 设置表元数据
     *
     * @param tmd 表元数据
     */
    public void setTmd(TableMetaData tmd) {
        this.tmd = tmd;
    }

    /**
     * 判断是否只返回一条记录
     *
     * @return 如果只返回一条记录返回 true，否则返回 false
     */
    public boolean isOne() {
        return isOne;
    }

    /**
     * 设置是否只返回一条记录
     *
     * @param isOne 是否只返回一条记录
     */
    public void setOne(boolean isOne) {
        this.isOne = isOne;
    }

    /**
     * 判断条件集合是否有效
     * <p>
     * 如果条件列表中存在有效的条件，则返回 true。
     * </p>
     *
     * @return 如果存在有效条件返回 true，否则返回 false
     */
    public boolean isValid() {
        if (conditionList.isEmpty()) {
            return false;
        }
        for (Condition c : conditionList) {
            if (c.isValid()) {
                return true;
            }
        }
        return false;
    }
}