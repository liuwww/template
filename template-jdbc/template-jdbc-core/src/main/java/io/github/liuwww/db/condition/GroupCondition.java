package io.github.liuwww.db.condition;

import java.util.ArrayList;
import java.util.List;

import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.sql.DbType;
import io.github.liuwww.db.sql.SqlBeanUtil;

/**
 * 条件组
 * <p>
 * 将多个条件组合在一起，支持 AND/OR 逻辑关系。
 * 可以嵌套使用，实现复杂的条件组合。
 * </p>
 * 
 * @author lww
 */
public class GroupCondition extends AbstractCompare<GroupCondition> implements Condition {

    /** 条件连接关系 */
    private ConditionRel rel;

    /** 是否有效的缓存 */
    private Boolean isValid;

    /**
     * 创建默认 AND 连接的条件组
     */
    public GroupCondition() {
        this.rel = ConditionRel.AND;
    }

    /**
     * 创建指定连接关系的条件组
     *
     * @param rel 条件连接关系（AND 或 OR）
     */
    public GroupCondition(ConditionRel rel) {
        conditionList = new ArrayList<Condition>();
        this.rel = rel;
    }

    @Override
    public String getSqlFragment(DbType dbType) {
        return SqlBeanUtil.getConditonSqlFragment(this, dbType);
    }

    @Override
    public List<Object> getParamList() {
        List<Object> list = new ArrayList<Object>(conditionList.size());
        for (Condition c : conditionList) {
            if (c.isValid()) {
                list.addAll(c.getParamList());
            }
        }
        return list;
    }

    @Override
    public boolean isValid() {
        if (this.isValid == null) {
            for (Condition c : conditionList) {
                if (c.isValid()) {
                    this.isValid = true;
                    break;
                }
            }
        }
        if (this.isValid == null) {
            this.isValid = Boolean.valueOf(false);
        }
        return this.isValid.booleanValue();
    }

    @Override
    public ConditionRel getConditionRel() {
        return this.rel;
    }

    /**
     * 设置与前一个条件的连接关系
     *
     * @param rel 连接关系（AND 或 OR）
     */
    public void setRel(ConditionRel rel) {
        this.rel = rel;
    }

    @Override
    public TableMetaData getTableMetaData() {
        return null;
    }
}