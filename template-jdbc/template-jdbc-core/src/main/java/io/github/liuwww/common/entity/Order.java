package io.github.liuwww.common.entity;

import java.util.regex.Pattern;

public class Order
{
    /** 字段名合法字符：字母、数字、下划线、点 */
    private static final Pattern FIELD_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]+$");

    private String field;

    private String sort;

    public Order(String field, String sort)
    {
        super();
        setField(field);
        setSort(sort);
    }

    /**
     * 校验排序字段名是否合法，防止 SQL 注入。
     *
     * @param field 待校验的字段名
     * @throws IllegalArgumentException 如果字段名包含非法字符
     */
    public static void validateField(String field) {
        if (field != null && !FIELD_PATTERN.matcher(field).matches()) {
            throw new IllegalArgumentException("非法排序字段名: " + field);
        }
    }

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        validateField(field);
        this.field = field;
    }

    public String getSort()
    {
        return sort;
    }

    public void setSort(String sort)
    {
        if (sort != null) {
            String upper = sort.trim().toUpperCase(java.util.Locale.ROOT);
            if (!"ASC".equals(upper) && !"DESC".equals(upper)) {
                throw new IllegalArgumentException("排序方向只能为 ASC 或 DESC: " + sort);
            }
            this.sort = upper;
        } else {
            this.sort = null;
        }
    }

}

