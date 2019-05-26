package org.liuwww.db.condition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.liuwww.db.condition.Condition.ConditionRel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCompare<T extends Compare<T>> implements Compare<T>
{
    protected List<Condition> conditionList;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public AbstractCompare()
    {
        conditionList = new ArrayList<Condition>();
    }

    @Override
    public int size()
    {
        return conditionList.size();
    }

    @Override
    public T addCondition(Condition condition)
    {
        conditionList.add(condition);
        return getTarget();
    }

    @Override
    public Iterator<Condition> iterator()
    {
        return conditionList.iterator();
    }

    @Override
    public List<Object> getParamList()
    {
        List<Object> params = new ArrayList<Object>();
        for (Condition c : conditionList)
        {
            if (c.isValid())
            {
                params.addAll(c.getParamList());
            }
        }
        return params;
    }

    /**
     * @see org.liuwww.db.condition.Compare#eq(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public T eq(String field, Object val)
    {
        addConditon(field, CompareOpe.eq, val, null);
        return getTarget();
    }

    private void addConditon(String field, CompareOpe ope, Object val, ConditionRel rel)
    {
        String column = getColumn(field);
        if (column != null)
        {
            addCondition(new OneCondition(column, ope, val, rel));
        }
        else
        {
            logger.debug("无匹配的查询字段：{}", field);
        }
    }

    /**
     * @see org.liuwww.db.condition.Compare#eq(java.lang.String,
     *      java.lang.Object,
     *      org.liuwww.db.condition.Condition.ConditionRel)
     */
    @Override
    public T eq(String field, Object val, ConditionRel rel)
    {
        addConditon(field, CompareOpe.eq, val, rel);
        return getTarget();
    }

    /**
     * @see org.liuwww.db.condition.Compare#ne(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public T ne(String field, Object val)
    {
        addConditon(field, CompareOpe.ne, val, null);
        return getTarget();
    }

    /**
     * @see org.liuwww.db.condition.Compare#ne(java.lang.String,
     *      java.lang.Object,
     *      org.liuwww.db.condition.Condition.ConditionRel)
     */
    @Override
    public T ne(String field, Object val, ConditionRel rel)
    {
        addConditon(field, CompareOpe.ne, val, rel);
        return getTarget();
    }

    /**
     * @see org.liuwww.db.condition.Compare#le(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public T le(String field, Object val)
    {
        addConditon(field, CompareOpe.le, val, null);
        return getTarget();
    }

    /**
     * @see org.liuwww.db.condition.Compare#le(java.lang.String,
     *      java.lang.Object,
     *      org.liuwww.db.condition.Condition.ConditionRel)
     */
    @Override
    public T le(String field, Object val, ConditionRel rel)
    {
        addConditon(field, CompareOpe.le, val, rel);
        return getTarget();
    }

    /**
     * @see org.liuwww.db.condition.Compare#lt(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public T lt(String field, Object val)
    {
        addConditon(field, CompareOpe.lt, val, null);
        return getTarget();
    }

    /**
     * @see org.liuwww.db.condition.Compare#lt(java.lang.String,
     *      java.lang.Object,
     *      org.liuwww.db.condition.Condition.ConditionRel)
     */
    @Override
    public T lt(String field, Object val, ConditionRel rel)
    {
        addConditon(field, CompareOpe.lt, val, rel);
        return getTarget();
    }

    /**
     * @see org.liuwww.db.condition.Compare#gt(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public T gt(String field, Object val)
    {
        addConditon(field, CompareOpe.gt, val, null);
        return getTarget();
    }

    /**
     * @see org.liuwww.db.condition.Compare#gt(java.lang.String,
     *      java.lang.Object,
     *      org.liuwww.db.condition.Condition.ConditionRel)
     */
    @Override
    public T gt(String field, Object val, ConditionRel rel)
    {
        addConditon(field, CompareOpe.gt, val, rel);
        return getTarget();
    }

    /**
     * @see org.liuwww.db.condition.Compare#ge(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public T ge(String field, Object val)
    {
        addConditon(field, CompareOpe.ge, val, null);
        return getTarget();
    }

    /**
     * @see org.liuwww.db.condition.Compare#ge(java.lang.String,
     *      java.lang.Object,
     *      org.liuwww.db.condition.Condition.ConditionRel)
     */
    @Override
    public T ge(String field, Object val, ConditionRel rel)
    {
        addConditon(field, CompareOpe.ge, val, rel);
        return getTarget();
    }

    /**
     * @see org.liuwww.db.condition.Compare#like(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public T like(String field, Object val)
    {
        addConditon(field, CompareOpe.like, val, null);
        return getTarget();
    }

    /**
     * @see org.liuwww.db.condition.Compare#like(java.lang.String,
     *      java.lang.Object,
     *      org.liuwww.db.condition.Condition.ConditionRel)
     */
    @Override
    public T like(String field, Object val, ConditionRel rel)
    {
        addConditon(field, CompareOpe.like, val, rel);
        return getTarget();
    }

    /**
     *
     * @see org.liuwww.db.condition.Compare#addParamMap(java.util.Map)
     */
    @Override
    public T addParamMap(Map paramMap)
    {
        for (Object key : paramMap.keySet())
        {
            if (key != null)
            {
                addConditon(key.toString(), CompareOpe.eq, paramMap.get(key), null);
            }
        }
        return getTarget();
    }

    @Override
    public T notNull(String field)
    {
        addConditon(field, CompareOpe.notNull, null, null);
        return getTarget();
    }

    @Override
    public T notNull(String field, ConditionRel rel)
    {
        addConditon(field, CompareOpe.notNull, null, rel);
        return getTarget();
    }

    protected String getColumn(String field)
    {
        return field;
    }

    @SuppressWarnings("unchecked")
    protected T getTarget()
    {
        return (T) this;
    }
}
