package org.liuwww.db.condition;

import java.util.ArrayList;
import java.util.List;

import org.liuwww.db.context.TableMetaData;
import org.liuwww.db.sql.DbType;
import org.liuwww.db.sql.SqlBeanUtil;

public class GroupCondition extends AbstractCompare<GroupCondition> implements Condition
{

    private ConditionRel rel;

    private Boolean isValid;

    public GroupCondition()
    {
        this.rel = ConditionRel.AND;
    }

    public GroupCondition(ConditionRel rel)
    {
        conditionList = new ArrayList<Condition>();
        this.rel = rel;
    }

    @Override
    public String getSqlFragment(DbType dbType)
    {
        return SqlBeanUtil.getConditonSqlFragment(this, dbType);
    }

    @Override
    public List<Object> getParamList()
    {
        List<Object> list = new ArrayList<Object>(conditionList.size());
        for (Condition c : conditionList)
        {
            if (c.isValid())
            {
                list.addAll(c.getParamList());
            }
        }
        return list;
    }

    @Override
    public boolean isValid()
    {
        if (this.isValid == null)
        {
            for (Condition c : conditionList)
            {
                if (c.isValid())
                {
                    this.isValid = true;
                    break;
                }
            }
        }
        if (this.isValid == null)
        {
            this.isValid = Boolean.valueOf(false);
        }
        return this.isValid.booleanValue();
    }

    @Override
    public ConditionRel getCondtionRel()
    {
        return this.rel;
    }

    @Override
    public TableMetaData getTableMetaData()
    {
        return null;
    }

}
