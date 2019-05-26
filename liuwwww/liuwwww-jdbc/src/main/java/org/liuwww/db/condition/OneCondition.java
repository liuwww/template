package org.liuwww.db.condition;

import java.util.Collections;
import java.util.List;

import org.liuwww.db.sql.DbType;
import org.liuwww.db.sql.SqlBeanUtil;

public class OneCondition implements Condition
{
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
        if (CompareOpe.like.equals(ope) && this.val.isValid())
        {
            this.val.getValList().set(0, "%" + val + "%");
        }
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
        if (ope == CompareOpe.notNull)
        {
            return Collections.emptyList();
        }
        return val.getValList();
    }

    @Override
    public boolean isValid()
    {
        if (ope != CompareOpe.notNull)
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
            return this.isValid.booleanValue();
        }
        else
        {
            return true;
        }
    }

    @Override
    public ConditionRel getCondtionRel()
    {
        return this.rel;
    }

}
