package io.github.liuwww.db.condition;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.github.liuwww.common.util.StringUtil;
import io.github.liuwww.db.condition.Condition.ConditionRel;
import io.github.liuwww.db.context.TableMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 条件比较抽象基类
 * <p>
 * 实现了 Compare 接口，提供条件构建的基础功能。
 * 子类需要实现 getTableMetaData() 方法以支持字段过滤。
 * </p>
 * @author lww
 * @param <T> 实现类类型，用于支持链式调用
 */
public abstract class AbstractCompare<T extends Compare<T>> implements Compare<T> {

    /** 条件列表 */
    protected List<Condition> conditionList;

    /** 日志记录器 */
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 创建空的比较器实例
     */
    public AbstractCompare() {
        conditionList = new ArrayList<Condition>();
    }

    @Override
    public int size() {
        return conditionList.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T addCondition(Condition condition) {
        return addCondition(condition, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T addCondition(Condition condition, ConditionRel rel) {
        if (condition instanceof OneCondition) {
            OneCondition one = (OneCondition) condition;
            if (rel != null) {
                one.setRel(rel);
            }
            String column = getColumn(one.getField());
            one.setField(column);
            if (one.getOpe() == CompareOpe.in) {
                List<Object> valList = one.param.getValList();
                if (valList.size() == 1 && valList.get(0) != null) {
                    Object para = valList.get(0);
                    if (para instanceof List) {
                        valList.clear();
                        valList.addAll((List) para);
                    }
                    else if (para.getClass().isArray()) {
                        valList.clear();
                        int length = Array.getLength(para);
                        for (int i = 0; i < length; i++) {
                            valList.add(Array.get(para, i));
                        }
                    }
                }
            }
        }
        else if (condition instanceof GroupCondition) {
            if (rel != null) {
                GroupCondition group = (GroupCondition) condition;
                group.setRel(rel);
            }
        }
        addTheCondition(condition);
        return getTarget();
    }

    @Override
    public Iterator<Condition> iterator() {
        return conditionList.iterator();
    }

    @Override
    public List<Object> getParamList() {
        List<Object> params = new ArrayList<Object>();
        for (Condition c : conditionList) {
            if (c.isValid()) {
                params.addAll(c.getParamList());
            }
        }
        return params;
    }

    @Override
    public T eq(String field, Object val) {
        addCondition(field, CompareOpe.eq, val, null);
        return getTarget();
    }

    @SuppressWarnings({ "unchecked", "rawtypes"
    })
    protected void addCondition(String field, CompareOpe ope, Object val, ConditionRel rel) {
        if (CompareOpe.in == ope) {
            String column = getColumn(field);
            if (column != null) {
                OneCondition oneCondition = null;
                if (val != null) {
                    if (val instanceof List) {
                        List list = (List) val;
                        oneCondition = new OneCondition(column, ope, list, rel);
                    }
                    else if (val.getClass().isArray()) {
                        List list = new ArrayList<>();
                        int len = Array.getLength(val);
                        for (int i = 0; i < len; i++) {
                            list.add(Array.get(val, i));
                        }
                        oneCondition = new OneCondition(column, ope, list, rel);
                    }
                    else {
                        oneCondition = new OneCondition(column, ope, val, rel);
                    }
                }
                else {
                    oneCondition = new OneCondition(column, ope, val, rel);
                }
                conditionList.add(oneCondition);
            }
            else {
                if (logger.isWarnEnabled()) {
                    logger.warn("无匹配的查询字段：{}", field);
                }
            }
        }
        else {
            Condition condition = getCondition(field, ope, val, rel);
            if (condition != null) {
                conditionList.add(condition);
            }
            else {
                if (logger.isWarnEnabled()) {
                    logger.warn("无匹配的查询字段：{}", field);
                }
            }
        }
    }

    protected void addTheCondition(Condition condition) {
        if (condition instanceof OneCondition) {
            OneCondition one = (OneCondition) condition;
            OneCondition validCondition = getValidFieldCondition(one);
            if (validCondition != null) {
                conditionList.add(condition);
            }
        }
        else if (condition instanceof GroupCondition) {
            GroupCondition group = (GroupCondition) condition;
            checkGroupCondition(group);
            conditionList.add(group);
        }
        else {
            conditionList.add(condition);
        }
    }

    protected OneCondition getValidFieldCondition(OneCondition one) {
        if (!one.isColumnField()) {
            return one;
        }
        else {
            String column = getColumn(one.getField());
            if (column != null) {
                one.setField(column);
                return one;
            }
            else {
                if (logger.isWarnEnabled()) {
                    logger.warn("无匹配的查询字段：{}", one.field);
                }
                return null;
            }
        }
    }

    protected void checkGroupCondition(GroupCondition group) {
        Iterator<Condition> it = group.iterator();
        while (it.hasNext()) {
            Condition c = it.next();
            if (c instanceof OneCondition) {
                OneCondition one = (OneCondition) c;
                String column = getColumn(one.field);
                if (column != null) {
                    one.field = column;
                }
                else {
                    it.remove();
                    if (logger.isWarnEnabled()) {
                        logger.warn("无匹配的查询字段：{}", one.field);
                    }
                }
            }
            else if (c instanceof GroupCondition) {
                checkGroupCondition((GroupCondition) c);
            }
            else {
                if (logger.isWarnEnabled()) {
                    logger.warn("未处理的Condition类型{}", c.getClass().getName());
                }
            }
        }
    }

    @Override
    public T eq(String field, Object val, ConditionRel rel) {
        addCondition(field, CompareOpe.eq, val, rel);
        return getTarget();
    }

    @Override
    public T ne(String field, Object val) {
        addCondition(field, CompareOpe.ne, val, null);
        return getTarget();
    }

    @Override
    public T ne(String field, Object val, ConditionRel rel) {
        addCondition(field, CompareOpe.ne, val, rel);
        return getTarget();
    }

    @Override
    public T le(String field, Object val) {
        addCondition(field, CompareOpe.le, val, null);
        return getTarget();
    }

    @Override
    public T le(String field, Object val, ConditionRel rel) {
        addCondition(field, CompareOpe.le, val, rel);
        return getTarget();
    }

    @Override
    public T lt(String field, Object val) {
        addCondition(field, CompareOpe.lt, val, null);
        return getTarget();
    }

    @Override
    public T lt(String field, Object val, ConditionRel rel) {
        addCondition(field, CompareOpe.lt, val, rel);
        return getTarget();
    }

    @Override
    public T gt(String field, Object val) {
        addCondition(field, CompareOpe.gt, val, null);
        return getTarget();
    }

    @Override
    public T gt(String field, Object val, ConditionRel rel) {
        addCondition(field, CompareOpe.gt, val, rel);
        return getTarget();
    }

    @Override
    public T ge(String field, Object val) {
        addCondition(field, CompareOpe.ge, val, null);
        return getTarget();
    }

    @Override
    public T ge(String field, Object val, ConditionRel rel) {
        addCondition(field, CompareOpe.ge, val, rel);
        return getTarget();
    }

    @Override
    public T like(String field, Object val) {
        addCondition(field, CompareOpe.like, val, null);
        return getTarget();
    }

    @Override
    public T like(String field, Object val, ConditionRel rel) {
        addCondition(field, CompareOpe.like, val, rel);
        return getTarget();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public T addParamMap(Map paramMap) {
        for (Object key : paramMap.keySet()) {
            if (key != null) {
                addCondition(key.toString(), CompareOpe.eq, paramMap.get(key), null);
            }
        }
        return getTarget();
    }

    @Override
    public T notNull(String field) {
        addCondition(field, CompareOpe.notNull, null, null);
        return getTarget();
    }

    @Override
    public T notNull(String field, ConditionRel rel) {
        addCondition(field, CompareOpe.notNull, null, rel);
        return getTarget();
    }

    @Override
    public T isNull(String filed) {
        addCondition(filed, CompareOpe.isNull, null, null);
        return getTarget();
    }

    @Override
    public T isNull(String filed, ConditionRel rel) {
        addCondition(filed, CompareOpe.isNull, null, rel);
        return getTarget();
    }

    /**
     * 获取数据库列名
     * <p>
     * 默认直接返回字段名，子类可以重写以实现字段映射。
     * </p>
     *
     * @param field Java 字段名或数据库列名
     * @return 数据库列名
     */
    protected String getColumn(String field) {
        return field;
    }

    /**
     * 创建条件对象
     *
     * @param field 字段名
     * @param ope   比较操作符
     * @param val   参数值
     * @param rel   连接关系
     * @return 条件对象，如果字段无效或值为空则返回 null
     */
    protected Condition getCondition(String field, CompareOpe ope, Object val, ConditionRel rel) {
        String column = getColumn(field);
        if (column != null) {
            // 如果操作符不是默认有效的类型（如 isNull, notNull, emptyStr），检查值是否为空
            if (!ope.defaultEffective()) {
                if (val == null || (val instanceof String && StringUtil.isBlank((String) val))) {
                    return null;
                }
            }
            OneCondition oneCondition = new OneCondition(column, ope, val, rel);
            return oneCondition;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected T getTarget() {
        return (T) this;
    }

    @Override
    public T likeAuto(String field, Object val) {
        return likeAuto(field, val, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T likeAuto(String field, Object val, ConditionRel rel) {
        if (val != null && StringUtil.isNotBlank(val.toString())) {
            addCondition(field, CompareOpe.like, "%" + val + "%", rel);
        }
        return (T) this;
    }

    @Override
    public T likeR(String field, Object val) {
        return likeR(field, val, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T likeR(String field, Object val, ConditionRel rel) {
        if (val != null && StringUtil.isNotBlank(val.toString())) {
            addCondition(field, CompareOpe.like, val + "%", rel);
        }
        return (T) this;
    }

    @Override
    public T likeL(String field, Object val) {
        return likeL(field, val, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T likeL(String field, Object val, ConditionRel rel) {
        if (val != null && StringUtil.isNotBlank(val.toString())) {
            addCondition(field, CompareOpe.like, "%" + val, rel);
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T emptyStr(String field) {
        addCondition(field, CompareOpe.emptyStr, null, null);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T emptyStr(String field, ConditionRel rel) {
        addCondition(field, CompareOpe.emptyStr, null, rel);
        return (T) this;
    }

    @Override
    public T in(String field, List<Object> params) {
        return in(field, params, ConditionRel.AND);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T in(String field, List<Object> params, ConditionRel rel) {
        addCondition(field, CompareOpe.in, params, rel);
        return (T) this;
    }

    /**
     * 获取表元数据
     *
     * @return 表元数据
     */
    public abstract TableMetaData getTableMetaData();
}
