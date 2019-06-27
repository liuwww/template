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

    protected Param val;

    protected Boolean isValid;

    protected ConditionRel rel;

    public OneCondition(String field, CompareOpe ope, Object val)
    {
        super();
        this.field = field;
        this.ope = ope;
        this.rel = ConditionRel.AND;
        this.val = new Param(val);
    }

    public static OneCondition getTheCondition(String field, CompareOpe ope, Object val)
    {
        OneCondition c = new OneCondition(field, ope, val);
        c.val.getValList().set(0, val);
        return c;
    }

    public static OneCondition getTheCondition(String field, CompareOpe ope, Object val, ConditionRel rel)
    {
        OneCondition c = new OneCondition(field, ope, val, rel);
        c.val.getValList().set(0, val);
        return c;
    }

    public OneCondition(String field, CompareOpe ope, Object val, ConditionRel rel)
    {
        this(field, ope, val);
        if (rel != null)
        {
            this.rel = rel;
        }
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
        return val;
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
        return val.getValList();
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
                if (this.val == null)
                {
                    this.isValid = false;
                }
                else
                {
                    this.isValid = this.val.isValid();
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

}
