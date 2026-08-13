package io.github.liuwww.db.condition;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import io.github.liuwww.common.util.StringUtil;
import io.github.liuwww.db.sql.DbType;

/**
 * 查询条件接口
 * <p>
 * 所有查询条件都需要实现此接口，用于生成 SQL 条件片段和参数列表。
 * 支持 AND/OR 条件组合。
 * </p>
 * @author lww
 */
public interface Condition {

    /**
     * 条件连接关系枚举
     * <p>
     * 用于指定多个条件之间的逻辑关系（AND 或 OR）。
     * </p>
     */
    public static enum ConditionRel {

        /** AND 连接 */
        AND("and"),

        /** OR 连接 */
        OR(" or");

        private String val;

        ConditionRel(String val) {
            this.val = val;
        }

        /**
         * 获取 SQL 连接关键字
         *
         * @return SQL 连接关键字
         */
        public String getVal() {
            return val;
        }
    }

    /**
     * 条件参数封装类
     * <p>
     * 封装条件参数的值列表，并提供有效性检查功能。
     * 支持单值、列表、数组等多种参数类型。
     * </p>
     */
    public class Param implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 参数值列表 */
        private List<Object> valList;

        /**
         * 获取参数值列表
         *
         * @return 参数值列表
         */
        public List<Object> getValList() {
            return valList;
        }

        /**
         * 设置参数值列表
         *
         * @param valList 参数值列表
         */
        public void setValList(List<Object> valList) {
            this.valList = valList;
        }

        /**
         * 使用值列表创建参数
         *
         * @param valList 参数值列表
         */
        public Param(List<Object> valList) {
            this.valList = valList;
        }

        /**
         * 使用单个值创建参数
         *
         * @param val 参数值
         */
        public Param(Object val) {
            this.valList = new ArrayList<Object>(1);
            this.valList.add(val);
        }

        /**
         * 检查参数是否有效
         * <p>
         * 参数有效的条件：
         * <ul>
         * <li>值列表不为 null</li>
         * <li>至少有一个值不为 null</li>
         * <li>如果是字符串，不能是空字符串</li>
         * <li>如果是列表，不能是空列表</li>
         * <li>如果是数组，长度不能为 0</li>
         * </ul>
         * </p>
         *
         * @return 如果参数有效返回 true，否则返回 false
         */
        public boolean isValid() {
            if (valList == null) {
                return false;
            }
            for (Object val : valList) {
                if (val == null) {
                    continue;
                }
                if (val instanceof String && StringUtil.isBlank(val.toString())) {
                    continue;
                }
                if (val instanceof List && ((List<?>) val).isEmpty()) {
                    continue;
                }
                if (val.getClass().isArray() && Array.getLength(val) == 0) {
                    continue;
                }
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            if (valList == null) {
                return null;
            }
            else {
                return valList.toString();
            }
        }
    }

    /**
     * 获取 SQL 条件片段
     * <p>
     * 根据数据库类型生成对应的 SQL 条件片段。
     * </p>
     *
     * @param dbType 数据库类型
     * @return SQL 条件片段，如 "AND name = ?"
     */
    String getSqlFragment(DbType dbType);

    /**
     * 获取参数列表
     * <p>
     * 返回条件中绑定的参数值，用于 SQL 预编译执行。
     * </p>
     *
     * @return 参数值列表
     */
    List<Object> getParamList();

    /**
     * 检查条件是否有效
     * <p>
     * 如果条件参数无效（如 null 或空字符串），则该条件不会出现在最终 SQL 中。
     * </p>
     *
     * @return 如果条件有效返回 true，否则返回 false
     */
    boolean isValid();

    /**
     * 获取条件连接关系
     * <p>
     * 返回此条件与前一个条件之间的逻辑关系（AND 或 OR）。
     * </p>
     *
     * @return 条件连接关系
     */
    ConditionRel getConditionRel();
}