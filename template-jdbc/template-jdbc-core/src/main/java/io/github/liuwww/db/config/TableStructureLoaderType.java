package io.github.liuwww.db.config;

/**
 * 表结构加载器类型枚举
 *
 * @author liuwww
 */
public enum TableStructureLoaderType {

    DATASOURCE("datasource", "从数据源加载表结构"),

    JSON("json", "从JSON加载表结构"),

    COMPOSITE("composite", "组合多种加载器");

    private final String code;

    private final String description;

    TableStructureLoaderType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TableStructureLoaderType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (TableStructureLoaderType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}