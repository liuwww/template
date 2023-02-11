package org.liuwww.db.condition;

import java.util.Collections;
import java.util.List;

import org.liuwww.db.sql.DbType;
import org.liuwww.db.sql.SqlBeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneCondition implements Condition
{
    protected static Logger logger = LoggerFactory.getLogger(OneCondition.class);

    protected String field;

    protected CompareOpe ope;

    protected Param param;

    protected Boolean isValid;

    protected ConditionRel rel;

    protected boolean isColumnField = true;

    public OneCondition(String field, CompareOpe ope, Object val)
    {
        this(field, ope, val, true);
    }

    public OneCondition(String field, CompareOpe ope, List val)
    {
        this(field, ope, val, true);
    }

    /**
     * @param field 条件字段
     * @param ope 比较类型
     * @param val 条件参数值
     * @param isColumnField field是否是数据库字段，如果true则会判断字段是否有效，非有效字段将不会拼接到sql中，如果是false则会默认是有效字段
     */
    public OneCondition(String field, CompareOpe ope, Object val, boolean isColumnField)
    {
        super();
        this.field = field;
        this.ope = ope;
        this.rel = ConditionRel.AND;
        this.param = new Param(val);
        this.isColumnField = isColumnField;
    }

    /**
     * @param field 条件字段
     * @param ope 比较类型
     * @param val 条件参数值
     * @param isColumnField field是否是数据库字段，如果true则会判断字段是否有效，非有效字段将不会拼接到sql中，如果是false则会默认是有效字段
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public OneCondition(String field, CompareOpe ope, List val, boolean isColumnField)
    {
        super();
        this.field = field;
        this.ope = ope;
        this.rel = ConditionRel.AND;
        this.param = new Param(val);
        this.isColumnField = isColumnField;
    }

    public OneCondition(String field, CompareOpe ope, Object val, ConditionRel rel)
    {
        this(field, ope, val);
        if (rel != null)
        {
            this.rel = rel;
        }
    }

    public OneCondition(String field, CompareOpe ope, List val, ConditionRel rel)
    {
        this(field, ope, val);
        if (rel != null)
        {
            this.rel = rel;
        }
    }

    public static OneCondition getTheCondition(String field, CompareOpe ope, Object val)
    {
        OneCondition c = new OneCondition(field, ope, val);
        c.param.getValList().set(0, val);
        return c;
    }

    public static OneCondition getTheCondition(String field, CompareOpe ope, Object val, ConditionRel rel)
    {
        OneCondition c = new OneCondition(field, ope, val, rel);
        c.param.getValList().set(0, val);
        return c;
    }

    public String getField()
    {
        return field;
    }

    protected void setField(String field)
    {
        this.field = field;
    }

    public CompareOpe getOpe()
    {
        return ope;
    }

    public Object getVal()
    {
        List<Object> valList = param.getValList();
        if (valList != null && !valList.isEmpty())
        {
            return valList.get(0);
        }
        return null;
    }

    @Override
    public String getSqlFragment(DbType dbType)
    {
        return SqlBeanUtil.getConditonSqlFragment(this, dbType);
    }

    @Override
    public List<Object> getParamList()
    {
        if (ope.defaultEffective())
        {
            return Collections.emptyList();
        }
        return param.getValList();
    }

    @Override
    public boolean isValid()
    {
        if (ope.defaultEffective())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("conditon：{}:{} 是否有效：{}", this.field, this.ope.getVal(), true);
            }
            return true;
        }
        else
        {
            if (this.isValid == null)
            {
                if (this.param == null)
                {
                    this.isValid = false;
                }
                else
                {
                    this.isValid = this.param.isValid();
                }
            }
            if (logger.isDebugEnabled())
            {
                logger.debug("conditon：{}:{},值：{},是否有效：{}", this.field, this.ope.getVal(), this.getVal(),
                        this.isValid.booleanValue());
            }
            return this.isValid.booleanValue();
        }
    }

    @Override
    public ConditionRel getCondtionRel()
    {
        return this.rel;
    }

    public boolean isColumnField()
    {
        return isColumnField;
    }

    public void setColumnField(boolean isColumnField)
    {
        this.isColumnField = isColumnField;
    }

}
