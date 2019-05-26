package org.liuwww.db.condition;

import java.util.List;
import java.util.Map;

import org.liuwww.db.condition.Condition.ConditionRel;

public interface Compare<T extends Compare<T>> extends Iterable<Condition>
{

    T addCondition(Condition condition);

    /**
     * @Desc:==
     * @Date 2017年1月10日下午5:49:35
     * @author liuwww
     * @param field
     * @param val
     */
    T eq(String field, Object val);

    /**
     * @Desc:==
     * @Date 2017年1月10日下午5:49:35
     * @author liuwww
     * @param field
     * @param val
     * @param rel and或者or
     */
    T eq(String field, Object val, ConditionRel rel);

    /**
     * @Desc:!=
     * @Date 2017年1月10日下午5:49:45
     * @author liuwww
     * @param field
     * @param val
     */
    T ne(String field, Object val);

    /**
     * @Desc:!=
     * @Date 2017年1月10日下午5:49:45
     * @author liuwww
     * @param field
     * @param val
     * @param rel and或者or
     */
    T ne(String field, Object val, ConditionRel rel);

    /**
     * @Desc:<=
     * @Date 2017年1月10日下午5:49:57
     * @author liuwww
     * @param field
     * @param val
     */
    T le(String field, Object val);

    /**
     * @Desc:<=
     * @Date 2017年1月10日下午5:49:57
     * @author liuwww
     * @param field
     * @param val
     * @param rel and或者or
     */
    T le(String field, Object val, ConditionRel rel);

    /**
     * @Desc:<
     * @Date 2017年1月10日下午5:50:07
     * @author liuwww
     * @param field
     * @param val
     */
    T lt(String field, Object val);

    /**
     * @Desc:<
     * @Date 2017年1月10日下午5:50:07
     * @author liuwww
     * @param field
     * @param val
     * @param rel and或者or
     */
    T lt(String field, Object val, ConditionRel rel);

    /**
     * @Desc:>
     * @Date 2017年1月10日下午5:50:59
     * @author liuwww
     * @param field
     * @param val
     */
    T gt(String field, Object val);

    /**
     * @Desc:>
     * @Date 2017年1月10日下午5:50:59
     * @author liuwww
     * @param field
     * @param val
     * @param rel and或者or
     */
    T gt(String field, Object val, ConditionRel rel);

    /**
     * @Desc:>=
     * @Date 2017年1月10日下午5:50:59
     * @author liuwww
     * @param field
     * @param val
     */
    T ge(String field, Object val);

    /**
     * @Desc:>=
     * @Date 2017年1月10日下午5:50:59
     * @author liuwww
     * @param field
     * @param val
     * @param rel and或者or
     */
    T ge(String field, Object val, ConditionRel rel);

    /**
     * @Desc: like
     * @Date 2017年7月7日上午11:10:20
     * @author liuwww
     * @param field
     * @param val
     * @return
     */
    T like(String field, Object val);

    /**
     * @Desc: like
     * @Date 2017年7月7日上午11:10:20
     * @author liuwww
     * @param field
     * @param val
     * @return
     * @param rel and或者or
     */
    T like(String field, Object val, ConditionRel rel);

    /**
     * @Desc:返回condition的个数
     * @Date 2017年8月11日下午12:46:03
     * @author liuwww
     * @return
     */
    int size();

    /**
     * @Desc:返回参数列表
     * @Date 2017年8月11日下午12:54:52
     * @author liuwww
     * @return
     */
    List<Object> getParamList();

    /**
     * @Desc:相当于多个eq方法，每个key都作为参数（单个表、视图会做过滤）
     * @Date 2017年8月11日下午3:02:07
     * @author liuwww
     * @param paramMap
     * @return
     */
    T addParamMap(Map<String, Object> tttt);

    /**
     * @Desc:字段不为null
     * @Date 2018年9月13日下午3:50:08
     * @author liuwww
     * @param field
     * @return
     */
    T notNull(String field);

    /**
     * @Desc:字段不为null
     * @Date 2018年9月13日下午3:50:08
     * @author liuwww
     * @param field
     * @return
     */
    T notNull(String field, ConditionRel rel);

}