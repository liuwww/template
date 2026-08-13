package io.github.liuwww.db.condition;

/**
 * 比较操作符枚举
 * <p>
 * 定义查询条件中支持的比较操作符，包括等值、范围、模糊匹配等。
 * </p>
 * @author lww
 */
public enum CompareOpe {

    /** 小于 (&lt;) */
    lt("<", false),

    /** 大于 (&gt;) */
    gt(">", false),

    /** 不等于 (!=) */
    ne("!=", false),

    /** 等于 (=) */
    eq("=", false),

    /** 小于等于 (&lt;=) */
    le("<=", false),

    /** 大于等于 (&gt;=) */
    ge(">=", false),

    /** 模糊匹配 (LIKE) */
    like("like", false),

    /** 不为空 (NOT NULL)，此操作符始终有效，不需要参数值 */
    notNull("not null", true),

    /** 为空 (IS NULL)，此操作符始终有效，不需要参数值 */
    isNull("is null", true),

    /** 空字符串 (= '')，此操作符始终有效，不需要参数值 */
    emptyStr("=''", true),

    /** IN 查询 */
    in("in", false);

    /** SQL 操作符字符串 */
    private String val;

    /** 是否默认有效（不需要参数值也有效） */
    private boolean defaultEffective;

    /**
     * 创建比较操作符
     *
     * @param val              SQL 操作符字符串
     * @param defaultEffective 是否默认有效
     */
    private CompareOpe(String val, boolean defaultEffective) {
        this.val = val;
        this.defaultEffective = defaultEffective;
    }

    /**
     * 获取 SQL 操作符字符串
     *
     * @return SQL 操作符字符串
     */
    public String getVal() {
        return val;
    }

    /**
     * 判断是否默认有效
     * <p>
     * 如果返回 true，表示此操作符不需要参数值也会出现在 SQL 中。
     * 如 isNull、notNull、emptyStr 等操作符。
     * </p>
     *
     * @return 如果默认有效返回 true，否则返回 false
     */
    public boolean defaultEffective() {
        return defaultEffective;
    }
}