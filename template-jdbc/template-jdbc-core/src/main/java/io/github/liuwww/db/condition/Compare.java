package io.github.liuwww.db.condition;

import java.util.List;
import java.util.Map;

import io.github.liuwww.db.condition.Condition.ConditionRel;

/**
 * 条件比较接口
 * <p>
 * 提供链式的条件构建方法，支持等值、范围、模糊匹配、IN 等多种查询条件。
 * 所有方法都返回泛型 T，支持链式调用。
 * </p>
 * @author lww
 * @param <T> 实现类类型，用于支持链式调用
 */
public interface Compare<T extends Compare<T>> extends Iterable<Condition> {

    /**
     * 添加条件
     *
     * @param condition 条件对象
     * @return 当前对象实例
     */
    T addCondition(Condition condition);

    /**
     * 添加条件，可指定连接关系
     *
     * @param condition 条件对象
     * @param rel       与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T addCondition(Condition condition, ConditionRel rel);

    /**
     * 添加等于条件（=）
     *
     * @param field 字段名
     * @param val   字段值
     * @return 当前对象实例
     */
    T eq(String field, Object val);

    /**
     * 添加等于条件（=），可指定连接关系
     *
     * @param field 字段名
     * @param val   字段值
     * @param rel   与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T eq(String field, Object val, ConditionRel rel);

    /**
     * 添加不等于条件（!=）
     *
     * @param field 字段名
     * @param val   字段值
     * @return 当前对象实例
     */
    T ne(String field, Object val);

    /**
     * 添加不等于条件（!=），可指定连接关系
     *
     * @param field 字段名
     * @param val   字段值
     * @param rel   与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T ne(String field, Object val, ConditionRel rel);

    /**
     * 添加小于等于条件（&lt;=）
     *
     * @param field 字段名
     * @param val   字段值
     * @return 当前对象实例
     */
    T le(String field, Object val);

    /**
     * 添加小于等于条件（&lt;=），可指定连接关系
     *
     * @param field 字段名
     * @param val   字段值
     * @param rel   与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T le(String field, Object val, ConditionRel rel);

    /**
     * 添加小于条件（&lt;）
     *
     * @param field 字段名
     * @param val   字段值
     * @return 当前对象实例
     */
    T lt(String field, Object val);

    /**
     * 添加小于条件（&lt;），可指定连接关系
     *
     * @param field 字段名
     * @param val   字段值
     * @param rel   与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T lt(String field, Object val, ConditionRel rel);

    /**
     * 添加大于条件（&gt;）
     *
     * @param field 字段名
     * @param val   字段值
     * @return 当前对象实例
     */
    T gt(String field, Object val);

    /**
     * 添加大于条件（&gt;），可指定连接关系
     *
     * @param field 字段名
     * @param val   字段值
     * @param rel   与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T gt(String field, Object val, ConditionRel rel);

    /**
     * 添加大于等于条件（&gt;=）
     *
     * @param field 字段名
     * @param val   字段值
     * @return 当前对象实例
     */
    T ge(String field, Object val);

    /**
     * 添加大于等于条件（&gt;=），可指定连接关系
     *
     * @param field 字段名
     * @param val   字段值
     * @param rel   与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T ge(String field, Object val, ConditionRel rel);

    /**
     * 添加模糊匹配条件（LIKE）
     * <p>
     * 不会自动添加 % 通配符，需要手动在值中包含 %。
     * </p>
     *
     * @param field 字段名
     * @param val   字段值
     * @return 当前对象实例
     */
    T like(String field, Object val);

    /**
     * 添加模糊匹配条件（LIKE），可指定连接关系
     * <p>
     * 不会自动添加 % 通配符，需要手动在值中包含 %。
     * </p>
     *
     * @param field 字段名
     * @param val   字段值
     * @param rel   与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T like(String field, Object val, ConditionRel rel);

    /**
     * 添加模糊匹配条件，自动在值两边添加 %
     * <p>
     * 等效于 LIKE '%value%'
     * </p>
     *
     * @param field 字段名
     * @param val   字段值
     * @return 当前对象实例
     */
    T likeAuto(String field, Object val);

    /**
     * 添加模糊匹配条件，自动在值两边添加 %，可指定连接关系
     * <p>
     * 等效于 LIKE '%value%'
     * </p>
     *
     * @param field 字段名
     * @param val   字段值
     * @param rel   与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T likeAuto(String field, Object val, ConditionRel rel);

    /**
     * 添加右模糊匹配条件，自动在值右边添加 %
     * <p>
     * 等效于 LIKE 'value%'
     * </p>
     *
     * @param field 字段名
     * @param val   字段值
     * @return 当前对象实例
     */
    T likeR(String field, Object val);

    /**
     * 添加右模糊匹配条件，自动在值右边添加 %，可指定连接关系
     * <p>
     * 等效于 LIKE 'value%'
     * </p>
     *
     * @param field 字段名
     * @param val   字段值
     * @param rel   与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T likeR(String field, Object val, ConditionRel rel);

    /**
     * 添加左模糊匹配条件，自动在值左边添加 %
     * <p>
     * 等效于 LIKE '%value'
     * </p>
     *
     * @param field 字段名
     * @param val   字段值
     * @return 当前对象实例
     */
    T likeL(String field, Object val);

    /**
     * 添加左模糊匹配条件，自动在值左边添加 %，可指定连接关系
     * <p>
     * 等效于 LIKE '%value'
     * </p>
     *
     * @param field 字段名
     * @param val   字段值
     * @param rel   与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T likeL(String field, Object val, ConditionRel rel);

    /**
     * 获取条件数量
     *
     * @return 条件个数
     */
    int size();

    /**
     * 获取所有有效条件的参数列表
     * <p>
     * 只返回有效条件的参数，用于 SQL 预编译执行。
     * </p>
     *
     * @return 参数值列表
     */
    List<Object> getParamList();

    /**
     * 批量添加等于条件
     * <p>
     * 将 Map 中的每个键值对作为一个等于条件。
     * 对于单表查询，会过滤掉不在表结构中的字段。
     * </p>
     *
     * @param paramMap 字段名与值的映射
     * @return 当前对象实例
     */
    T addParamMap(Map<String, Object> paramMap);

    /**
     * 添加不为空条件（IS NOT NULL）
     *
     * @param field 字段名
     * @return 当前对象实例
     */
    T notNull(String field);

    /**
     * 添加不为空条件（IS NOT NULL），可指定连接关系
     *
     * @param field 字段名
     * @param rel   与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T notNull(String field, ConditionRel rel);

    /**
     * 添加为空条件（IS NULL）
     *
     * @param field 字段名
     * @return 当前对象实例
     */
    T isNull(String field);

    /**
     * 添加为空条件（IS NULL），可指定连接关系
     *
     * @param field 字段名
     * @param rel   与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T isNull(String field, ConditionRel rel);

    /**
     * 添加空字符串条件（= ''）
     *
     * @param field 字段名
     * @return 当前对象实例
     */
    T emptyStr(String field);

    /**
     * 添加空字符串条件（= ''），可指定连接关系
     *
     * @param field 字段名
     * @param rel   与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T emptyStr(String field, ConditionRel rel);

    /**
     * 添加 IN 条件
     *
     * @param field  字段名
     * @param params 参数值列表
     * @return 当前对象实例
     */
    T in(String field, List<Object> params);

    /**
     * 添加 IN 条件，可指定连接关系
     *
     * @param field  字段名
     * @param params 参数值列表
     * @param rel    与前一个条件的连接关系（AND 或 OR）
     * @return 当前对象实例
     */
    T in(String field, List<Object> params, ConditionRel rel);
}