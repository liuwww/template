package io.github.liuwww.db.condition;

import java.util.Collections;
import java.util.List;

import io.github.liuwww.db.sql.DbType;
import io.github.liuwww.db.sql.SqlBeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 单个查询条件
 * <p>
 * 表示一个简单的查询条件，如 "name = ?" 或 "age > ?"。
 * 支持等值、范围、模糊匹配等多种比较操作。
 * </p>
 * 
 * @author lww
 */
public class OneCondition implements Condition {

    protected static Logger logger = LoggerFactory.getLogger(OneCondition.class);

    /** 字段名 */
    protected String field;

    /** 比较操作符 */
    protected CompareOpe ope;

    /** 条件参数 */
    protected Param param;

    /** 是否有效的缓存 */
    protected Boolean isValid;

    /** 与前一个条件的连接关系 */
    protected ConditionRel rel;

    /** 字段是否为数据库字段（如果是 false 则认为是有效字段） */
    protected boolean isColumnField = true;

    /**
     * 创建单条件
     *
     * @param field 字段名
     * @param ope   比较操作符
     * @param val   参数值
     */
    public OneCondition(String field, CompareOpe ope, Object val) {
        this(field, ope, val, true);
    }

    /**
     * 创建单条件
     *
     * @param field 字段名
     * @param ope   比较操作符
     * @param val   参数值（列表类型）
     */
    public OneCondition(String field, CompareOpe ope, List val) {
        this(field, ope, val, true);
    }

    /**
     * 创建单条件
     *
     * @param field         条件字段
     * @param ope           比较类型
     * @param val           条件参数值
     * @param isColumnField field 是否是数据库字段，如果 true 则会判断字段是否有效，
     *                      非有效字段将不会拼接到 SQL 中，
     *                      如果是 false 则会默认是有效字段
     */
    public OneCondition(String field, CompareOpe ope, Object val, boolean isColumnField) {
        super();
        this.field = field;
        this.ope = ope;
        this.rel = ConditionRel.AND;
        this.param = new Param(val);
        this.isColumnField = isColumnField;
    }

    /**
     * 创建单条件（列表参数）
     *
     * @param field         条件字段
     * @param ope           比较类型
     * @param val           条件参数值
     * @param isColumnField field 是否是数据库字段
     */
    @SuppressWarnings({ "unchecked", "rawtypes"
    })
    public OneCondition(String field, CompareOpe ope, List val, boolean isColumnField) {
        super();
        this.field = field;
        this.ope = ope;
        this.rel = ConditionRel.AND;
        this.param = new Param(val);
        this.isColumnField = isColumnField;
    }

    /**
     * 创建单条件（可指定连接关系）
     *
     * @param field 字段名
     * @param ope   比较操作符
     * @param val   参数值
     * @param rel   与前一个条件的连接关系
     */
    public OneCondition(String field, CompareOpe ope, Object val, ConditionRel rel) {
        this(field, ope, val);
        if (rel != null) {
            this.rel = rel;
        }
    }

    /**
     * 创建单条件（可指定连接关系，列表参数）
     *
     * @param field 字段名
     * @param ope   比较操作符
     * @param val   参数值
     * @param rel   与前一个条件的连接关系
     */
    public OneCondition(String field, CompareOpe ope, List val, ConditionRel rel) {
        this(field, ope, val);
        if (rel != null) {
            this.rel = rel;
        }
    }

    /**
     * 获取单条件实例
     *
     * @param field 字段名
     * @param ope   比较操作符
     * @param val   参数值
     * @return OneCondition 实例
     */
    public static OneCondition getTheCondition(String field, CompareOpe ope, Object val) {
        OneCondition c = new OneCondition(field, ope, val);
        c.param.getValList().set(0, val);
        return c;
    }

    /**
     * 获取单条件实例（可指定连接关系）
     *
     * @param field 字段名
     * @param ope   比较操作符
     * @param val   参数值
     * @param rel   与前一个条件的连接关系
     * @return OneCondition 实例
     */
    public static OneCondition getTheCondition(String field, CompareOpe ope, Object val, ConditionRel rel) {
        OneCondition c = new OneCondition(field, ope, val, rel);
        c.param.getValList().set(0, val);
        return c;
    }

    /**
     * 获取字段名
     *
     * @return 字段名
     */
    public String getField() {
        return field;
    }

    /**
     * 设置字段名
     *
     * @param field 字段名
     */
    protected void setField(String field) {
        this.field = field;
    }

    /**
     * 获取比较操作符
     *
     * @return 比较操作符
     */
    public CompareOpe getOpe() {
        return ope;
    }

    /**
     * 设置与前一个条件的连接关系
     *
     * @param rel 连接关系（AND 或 OR）
     */
    public void setRel(ConditionRel rel) {
        this.rel = rel;
    }

    /**
     * 获取参数值
     *
     * @return 参数值
     */
    public Object getVal() {
        List<Object> valList = param.getValList();
        if (valList != null && !valList.isEmpty()) {
            return valList.get(0);
        }
        return null;
    }

    @Override
    public String getSqlFragment(DbType dbType) {
        return SqlBeanUtil.getConditonSqlFragment(this, dbType);
    }

    @Override
    public List<Object> getParamList() {
        if (ope.defaultEffective()) {
            return Collections.emptyList();
        }
        return param.getValList();
    }

    @Override
    public boolean isValid() {
        if (ope.defaultEffective()) {
            if (logger.isDebugEnabled()) {
                logger.debug("condition：{}:{} 是否有效：{}", this.field, this.ope.getVal(), true);
            }
            return true;
        }
        else {
            if (this.isValid == null) {
                if (this.param == null) {
                    this.isValid = false;
                }
                else {
                    this.isValid = this.param.isValid();
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("condition：{}:{},值：{},是否有效：{}", this.field, this.ope.getVal(), this.getVal(),
                        this.isValid.booleanValue());
            }
            return this.isValid.booleanValue();
        }
    }

    @Override
    public ConditionRel getConditionRel() {
        return this.rel;
    }

    /**
     * 判断字段是否为数据库字段
     *
     * @return 如果是数据库字段返回 true，否则返回 false
     */
    public boolean isColumnField() {
        return isColumnField;
    }

    /**
     * 设置字段是否为数据库字段
     *
     * @param isColumnField 是否为数据库字段
     */
    public void setColumnField(boolean isColumnField) {
        this.isColumnField = isColumnField;
    }
}