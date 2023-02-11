package org.liuwww.db.condition;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.liuwww.common.util.StringUtil;
import org.liuwww.db.condition.Condition.ConditionRel;
import org.liuwww.db.context.TableMetaData;
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

    @SuppressWarnings("unchecked")
    @Override
    public T addCondition(Condition condition)
    {
        if (condition instanceof OneCondition)
        {
            OneCondition one = (OneCondition) condition;
            String column = getColumn(one.getField());
            one.setField(column);
            if (one.getOpe() == CompareOpe.in)
            {
                List<Object> valList = one.param.getValList();
                if (valList.size() == 1 && valList.get(0) != null)
                {
                    Object para = valList.get(0);
                    if (para instanceof List)
                    {
                        valList.clear();
                        valList.addAll((List) para);
                    }
                    else if (para.getClass().isArray())
                    {
                        valList.clear();
                        int length = Array.getLength(para);
                        for (int i = 0; i < length; i++)
                        {
                            valList.add(Array.get(para, i));
                        }
                    }
                }
            }
        }
        addTheCondition(condition);
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

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    protected void addConditon(String field, CompareOpe ope, Object val, ConditionRel rel)
    {
        if (CompareOpe.in == ope)
        {
            String column = getColumn(field);
            if (column != null)
            {
                OneCondition oneCondition = null;
                if (val != null)
                {
                    if (val instanceof List)
                    {
                        List list = (List) val;
                        oneCondition = new OneCondition(column, ope, list, rel);
                    }
                    else if (val.getClass().isArray())
                    {
                        List list = new ArrayList<>();
                        int len = Array.getLength(val);
                        for (int i = 0; i < len; i++)
                        {
                            list.add(Array.get(val, i));
                        }
                        oneCondition = new OneCondition(column, ope, list, rel);
                    }
                    else
                    {
                        oneCondition = new OneCondition(column, ope, val, rel);
                    }
                }
                else
                {
                    oneCondition = new OneCondition(column, ope, val, rel);
                }
                conditionList.add(oneCondition);
            }
            else
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("无匹配的查询字段：{}", field);
                }
            }
        }
        else
        {
            Condition condition = getCondition(field, ope, val, rel);
            if (condition != null)
            {
                conditionList.add(condition);
            }
            else
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("无匹配的查询字段：{}", field);
                }
            }
        }
    }

    protected void addTheCondition(Condition condition)
    {
        if (condition instanceof OneCondition)
        {
            OneCondition one = (OneCondition) condition;
            OneCondition validCondition = getValidFieldCondition(one);
            if (validCondition != null)
            {
                conditionList.add(condition);
            }
        }
        else if (condition instanceof GroupCondition)
        {
            GroupCondition group = (GroupCondition) condition;
            checkGroupCondition(group);
            conditionList.add(group);
        }
        else
        {
            conditionList.add(condition);
            // throw new SysException("未做处理:" + condition.getClass().getName());
        }
    }

    protected OneCondition getValidFieldCondition(OneCondition one)
    {
        if (!one.isColumnField())
        {
            return one;
        }
        else
        {
            String column = getColumn(one.getField());
            if (column != null)
            {
                one.setField(column);
                return one;
            }
            else
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("无匹配的查询字段：{}", one.field);
                }
                return null;
            }
        }
    }

    protected void checkGroupCondition(GroupCondition group)
    {
        Iterator<Condition> it = group.iterator();
        while (it.hasNext())
        {
            Condition c = it.next();
            if (c instanceof OneCondition)
            {
                OneCondition one = (OneCondition) c;
                String column = getColumn(one.field);
                if (column != null)
                {
                    one.field = column;
                }
                else
                {
                    it.remove();
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("无匹配的查询字段：{}", one.field);
                    }
                }
            }
            else if (c instanceof GroupCondition)
            {
                checkGroupCondition((GroupCondition) c);
            }
            else
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("未处理的Condition类型{}", c.getClass().getName());
                }
            }
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
    @SuppressWarnings("rawtypes")
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

    @Override
    public T isNull(String filed)
    {
        addConditon(filed, CompareOpe.isNull, null, null);
        return getTarget();
    }

    @Override
    public T isNull(String filed, ConditionRel rel)
    {
        addConditon(filed, CompareOpe.isNull, null, rel);
        return getTarget();
    }

    protected String getColumn(String field)
    {
        return field;
    }

    protected Condition getCondition(String field, CompareOpe ope, Object val, ConditionRel rel)
    {
        String column = getColumn(field);
        if (column != null)
        {
            OneCondition oneCondition = new OneCondition(column, ope, val, rel);
            return oneCondition;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected T getTarget()
    {
        return (T) this;
    }

    @Override
    public T likeAuto(String field, Object val)
    {
        return likeAuto(field, val, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T likeAuto(String field, Object val, ConditionRel rel)
    {
        if (val != null && StringUtil.isNotBlank(val.toString()))
        {
            addConditon(field, CompareOpe.like, "%" + val + "%", rel);
        }
        return (T) this;
    }

    @Override
    public T likeR(String field, Object val)
    {
        return likeR(field, val, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T likeR(String field, Object val, ConditionRel rel)
    {
        if (val != null && StringUtil.isNotBlank(val.toString()))
        {
            addConditon(field, CompareOpe.like, val + "%", rel);
        }
        return (T) this;
    }

    @Override
    public T likeL(String field, Object val)
    {
        return likeL(field, val, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T likeL(String field, Object val, ConditionRel rel)
    {
        if (val != null && StringUtil.isNotBlank(val.toString()))
        {
            addConditon(field, CompareOpe.like, "%" + val, rel);
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T emptyStr(String field)
    {
        addConditon(field, CompareOpe.emptyStr, null, null);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T emptyStr(String field, ConditionRel rel)
    {
        addConditon(field, CompareOpe.emptyStr, null, rel);
        return (T) this;
    }

    @Override
    public T in(String field, List<Object> params)
    {
        return in(field, params, ConditionRel.AND);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T in(String field, List<Object> params, ConditionRel rel)
    {
        addConditon(field, CompareOpe.in, params, rel);
        return (T) this;
    }

    public abstract TableMetaData getTableMetaData();

}
