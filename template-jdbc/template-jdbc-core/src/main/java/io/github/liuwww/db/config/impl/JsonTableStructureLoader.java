package io.github.liuwww.db.config.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import io.github.liuwww.db.config.TableStructureContainer;
import io.github.liuwww.db.config.TableStructureLoader;
import io.github.liuwww.db.context.TableMetaData;
import io.github.liuwww.db.sql.Column;
import io.github.liuwww.db.sql.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 从 JSON 加载表结构的加载器。
 * <p>
 * 适用于无法直连数据库反射元数据（如只读库、远程库、测试环境）的场景，
 * 通过 JSON 声明表结构，避免每次启动都发起 JDBC 连接。
 * </p>
 *
 * <h3>JSON 格式</h3>
 * <pre>{@code
 * {
 *   "tables": [
 *     {
 *       "tableName": "t_user",
 *       "dbType": "mysql",
 *       "tableType": "TABLE",
 *       "comment": "用户表",
 *       "idColumnName": "id",
 *       "columns": [
 *         {"columnName": "id", "dataTypeName": "BIGINT", "columnSize": 20, "nullable": false, "remarks": "主键"},
 *         {"columnName": "user_name", "dataTypeName": "VARCHAR", "columnSize": 100, "nullable": false, "remarks": "用户名"}
 *       ]
 *     }
 *   ]
 * }
 * }</pre>
 *
 * <h3>加载模式</h3>
 * <ul>
 * <li><b>单数据源模式（退化）</b>：通过 {@code setJsonTableStructure} / {@code setClasspathResource}
 *     / {@code setFilePath} 设置全局 JSON 源，所有表绑定到 {@code setDataSource} 设置的数据源（可选）。</li>
 * <li><b>多数据源模式</b>：通过 {@code setDataSourceJsonSources} 设置 beanName-&gt;JSON 路径映射，
 *     配合 {@code setDataSourceBindings} 设置 beanName-&gt;DataSource 映射，
 *     按 beanName 逐个加载 JSON 文件并绑定到对应数据源。</li>
 * </ul>
 *
 * <h3>路径格式</h3>
 * JSON 源支持以下前缀：
 * <ul>
 * <li>{@code classpath:xxx} - classpath 资源</li>
 * <li>{@code http://xxx} / {@code https://xxx} - 网络 URL</li>
 * <li>其他 - 文件系统路径</li>
 * </ul>
 *
 * <h3>导出</h3>
 * 通过 {@link #exportToFile} 和 {@link #exportByDataSource} 可将已加载的表结构导出为 JSON 文件，
 * 实现先 JDBC 加载再切换为 JSON 加载的加速场景。
 *
 * @author liuwww
 * @since 2026-07
 */
public class JsonTableStructureLoader implements TableStructureLoader {

    private static final Logger logger = LoggerFactory.getLogger(JsonTableStructureLoader.class);

    // === 单数据源模式字段 ===

    /** JSON 字符串输入源 */
    private String jsonTableStructure;

    /** classpath 资源路径 */
    private String classpathResource;

    /** 文件系统路径 */
    private String filePath;

    /** 可选的数据源绑定，加载的表元数据归属此 DataSource */
    private DataSource dataSource;

    // === 多数据源模式字段 ===

    /** beanName -> JSON 路径映射（多数据源模式） */
    private Map<String, String> dataSourceJsonSources;

    /** beanName -> DataSource 映射（由 configCenter 设置） */
    private Map<String, DataSource> dataSourceBindings;

    public JsonTableStructureLoader() {
    }

    public JsonTableStructureLoader(String jsonTableStructure) {
        this.jsonTableStructure = jsonTableStructure;
    }

    public JsonTableStructureLoader(String jsonTableStructure, DataSource dataSource) {
        this.jsonTableStructure = jsonTableStructure;
        this.dataSource = dataSource;
    }

    public void setJsonTableStructure(String jsonTableStructure) {
        this.jsonTableStructure = jsonTableStructure;
    }

    public void setClasspathResource(String classpathResource) {
        this.classpathResource = classpathResource;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 设置多数据源 JSON 路径映射。
     *
     * @param dataSourceJsonSources beanName -> JSON 路径（支持 classpath:/http:/文件路径前缀）
     */
    public void setDataSourceJsonSources(Map<String, String> dataSourceJsonSources) {
        this.dataSourceJsonSources = dataSourceJsonSources;
    }

    /**
     * 设置多数据源绑定（由 GenericDatabaseConfigCenter 在 init 前设置）。
     *
     * @param dataSourceBindings beanName -> DataSource
     */
    public void setDataSourceBindings(Map<String, DataSource> dataSourceBindings) {
        this.dataSourceBindings = dataSourceBindings;
    }

    @Override
    public TableStructureContainer loadStructureContainer() {
        TableStructureContainer container = new DataSourceContextTableStructureContainer();

        if (dataSourceJsonSources != null && !dataSourceJsonSources.isEmpty()) {
            // 多数据源模式：按 beanName 逐个加载
            for (Map.Entry<String, String> entry : dataSourceJsonSources.entrySet()) {
                String beanName = entry.getKey();
                String source = entry.getValue();
                DataSource ds = dataSourceBindings != null ? dataSourceBindings.get(beanName) : null;
                loadFromSource(container, source, ds, beanName);
            }
        } else {
            // 单数据源模式（退化处理）
            String json = resolveJson();
            if (json != null && !json.trim().isEmpty()) {
                parseAndAddTables(container, json, dataSource);
            } else if (logger.isWarnEnabled()) {
                logger.warn("JSON 表结构内容为空，跳过加载");
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("从JSON加载表结构完成，共 {} 个表", container.getTableNames().size());
        }
        return container;
    }

    /**
     * 多数据源模式：从指定路径加载 JSON 并绑定到对应 DataSource。
     */
    private void loadFromSource(TableStructureContainer container, String source, DataSource ds, String beanName) {
        String json = resolveFromSource(source);
        if (json == null || json.trim().isEmpty()) {
            if (logger.isWarnEnabled()) {
                logger.warn("数据源 [{}] 的 JSON 内容为空，源: {}", beanName, source);
            }
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("加载数据源 [{}] 的表结构，源: {}", beanName, source);
        }
        parseAndAddTables(container, json, ds);
    }

    /**
     * 解析 JSON 并将表添加到容器。
     */
    @SuppressWarnings("unchecked")
    private void parseAndAddTables(TableStructureContainer container, String json, DataSource ds) {
        try {
            Object root = new JsonParser(json).parse();
            if (!(root instanceof Map)) {
                throw new IllegalArgumentException("JSON 根节点必须是对象");
            }

            Map<String, Object> rootMap = (Map<String, Object>) root;
            Object tablesObj = rootMap.get("tables");
            if (!(tablesObj instanceof List)) {
                throw new IllegalArgumentException("JSON 缺少 tables 数组");
            }

            List<Object> tables = (List<Object>) tablesObj;
            for (Object tableObj : tables) {
                if (!(tableObj instanceof Map)) {
                    continue;
                }
                Map<String, Object> tableMap = (Map<String, Object>) tableObj;
                TableMetaData tmd = buildTableMetaData(tableMap, ds);
                container.addTableMetaData(tmd.getTableName(), tmd);

                if (logger.isDebugEnabled()) {
                    logger.debug("从JSON加载表结构: {}, 列数: {}", tmd.getTableName(),
                            tmd.getColumnList() != null ? tmd.getColumnList().size() : 0);
                }
            }
        } catch (Exception e) {
            logger.error("从JSON加载表结构失败", e);
            throw new RuntimeException("从JSON加载表结构失败", e);
        }
    }

    /**
     * 统一路径解析，支持 classpath:/http:/文件路径。
     *
     * @param source 路径，支持前缀：classpath:、http://、https://，无前缀按文件路径处理
     * @return JSON 字符串
     */
    private String resolveFromSource(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        String trimmed = source.trim();
        if (trimmed.startsWith("classpath:")) {
            String path = trimmed.substring("classpath:".length());
            return readStream(openClasspathResource(path));
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return readFromUrl(trimmed);
        }
        return readTextFile(trimmed);
    }

    /**
     * 单数据源模式：按优先级解析 JSON 内容：字符串 > classpath > 文件。
     */
    private String resolveJson() {
        if (jsonTableStructure != null && !jsonTableStructure.trim().isEmpty()) {
            return jsonTableStructure;
        }
        if (classpathResource != null && !classpathResource.trim().isEmpty()) {
            return readStream(openClasspathResource(classpathResource));
        }
        if (filePath != null && !filePath.trim().isEmpty()) {
            return readTextFile(filePath);
        }
        return null;
    }

    private InputStream openClasspathResource(String path) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is == null) {
            is = JsonTableStructureLoader.class.getClassLoader().getResourceAsStream(path);
        }
        if (is == null) {
            throw new RuntimeException("Classpath 资源未找到: " + path);
        }
        return is;
    }

    private String readTextFile(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("文件未找到: " + path);
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return readStream(fis);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + path, e);
        }
    }

    private String readFromUrl(String url) {
        try {
            URL u = new URL(url);
            try (InputStream is = u.openStream()) {
                return readStream(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("读取 URL 失败: " + url, e);
        }
    }

    private String readStream(InputStream is) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) >= 0) {
                sb.append(buf, 0, n);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("读取流失败", e);
        }
    }

    // ========== 导出方法 ==========

    /**
     * 将表结构列表导出为 JSON 文件。
     *
     * @param tables   表元数据列表
     * @param filePath 输出文件路径
     */
    public static void exportToFile(List<TableMetaData> tables, String filePath) {
        String json = toJson(tables);
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(json);
        } catch (IOException e) {
            throw new RuntimeException("导出 JSON 文件失败: " + filePath, e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("导出 {} 个表到文件: {}", tables.size(), filePath);
        }
    }

    /**
     * 按数据源 beanName 分组导出到目录，每个数据源一个文件（{beanName}.json）。
     *
     * @param tablesByBeanName beanName -> 表元数据列表
     * @param dirPath          输出目录路径
     */
    public static void exportByDataSource(Map<String, List<TableMetaData>> tablesByBeanName, String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        for (Map.Entry<String, List<TableMetaData>> entry : tablesByBeanName.entrySet()) {
            String beanName = entry.getKey();
            if (beanName == null || beanName.trim().isEmpty()) {
                beanName = "default";
            }
            String filePath = new File(dir, beanName + ".json").getPath();
            exportToFile(entry.getValue(), filePath);
        }
    }

    /**
     * 序列化表结构列表为 JSON 字符串。
     */
    static String toJson(List<TableMetaData> tables) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"tables\": [\n");
        for (int i = 0; i < tables.size(); i++) {
            sb.append("    ").append(serializeTable(tables.get(i)));
            if (i < tables.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ]\n}");
        return sb.toString();
    }

    private static String serializeTable(TableMetaData tmd) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"tableName\": \"").append(escape(tmd.getTableName())).append("\"");

        if (tmd.getDbType() != null) {
            sb.append(", \"dbType\": \"").append(tmd.getDbType().name()).append("\"");
        }
        if (tmd.getTableType() != null) {
            sb.append(", \"tableType\": \"").append(escape(tmd.getTableType())).append("\"");
        }
        if (tmd.getComment() != null) {
            sb.append(", \"comment\": \"").append(escape(tmd.getComment())).append("\"");
        }

        // 主键列
        Column[] idCols = tmd.getIdColumns();
        if (idCols != null && idCols.length > 0) {
            StringBuilder idNames = new StringBuilder();
            for (int i = 0; i < idCols.length; i++) {
                if (i > 0) {
                    idNames.append(",");
                }
                if (idCols[i] != null) {
                    idNames.append(idCols[i].getColumnName());
                }
            }
            sb.append(", \"idColumnName\": \"").append(escape(idNames.toString())).append("\"");
        }

        // 列
        List<Column> columns = tmd.getColumnList();
        sb.append(", \"columns\": [");
        if (columns != null) {
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(serializeColumn(columns.get(i)));
            }
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    private static String serializeColumn(Column col) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"columnName\": \"").append(escape(col.getColumnName())).append("\"");
        if (col.getSqlDataType() != null) {
            sb.append(", \"dataTypeName\": \"").append(escape(col.getSqlDataType().name())).append("\"");
        }
        sb.append(", \"columnSize\": ").append(col.getColumnSize());
        sb.append(", \"nullable\": ").append(col.isNullable());
        if (col.getRemarks() != null) {
            sb.append(", \"remarks\": \"").append(escape(col.getRemarks())).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ========== JSON 解析与构建 ==========

    @SuppressWarnings("unchecked")
    private TableMetaData buildTableMetaData(Map<String, Object> tableMap, DataSource ds) {
        TableMetaData tmd = new TableMetaData();

        String tableName = getString(tableMap, "tableName");
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("tableName 不能为空");
        }
        tmd.setTableName(tableName);

        String tableType = getString(tableMap, "tableType");
        if (tableType != null) {
            tmd.setTableType(tableType);
        }

        String dbTypeName = getString(tableMap, "dbType");
        if (dbTypeName != null) {
            tmd.setDbType(DbType.getByName(dbTypeName));
        }

        String comment = getString(tableMap, "comment");
        if (comment != null) {
            tmd.setComment(comment);
        }

        if (ds != null) {
            tmd.setDataSource(ds);
        }

        // 构建列
        Object columnsObj = tableMap.get("columns");
        if (columnsObj instanceof List) {
            for (Object colObj : (List<Object>) columnsObj) {
                if (!(colObj instanceof Map)) {
                    continue;
                }
                Map<String, Object> colMap = (Map<String, Object>) colObj;
                Column col = buildColumn(colMap);
                tmd.getColumnList().add(col);
                tmd.putNameMap(col.getName(), col);
                tmd.putColumnMap(col.getColumnName(), col);
            }
        }

        // 设置主键（支持逗号分隔的联合主键）
        String idColumnName = getString(tableMap, "idColumnName");
        if (idColumnName != null && !idColumnName.trim().isEmpty()) {
            String[] idNames = idColumnName.split(",");
            Column[] idCols = new Column[idNames.length];
            for (int i = 0; i < idNames.length; i++) {
                String name = idNames[i].trim();
                idCols[i] = tmd.getColumnByColumn(name);
                if (idCols[i] == null && logger.isWarnEnabled()) {
                    logger.warn("表 {} 的主键列 {} 在 columns 中未找到", tableName, name);
                }
            }
            tmd.setIdColumns(idCols);
        }

        return tmd;
    }

    private Column buildColumn(Map<String, Object> colMap) {
        Column col = new Column();

        String columnName = getString(colMap, "columnName");
        if (columnName == null || columnName.trim().isEmpty()) {
            throw new IllegalArgumentException("column 的 columnName 不能为空");
        }
        col.setColumnName(columnName);

        String dataTypeName = getString(colMap, "dataTypeName");
        if (dataTypeName != null) {
            col.setDataTypeName(dataTypeName);
        }

        col.setColumnSize(getInt(colMap, "columnSize", 0));
        col.setNullable(getBoolean(colMap, "nullable", true));

        String remarks = getString(colMap, "remarks");
        if (remarks != null) {
            col.setRemarks(remarks);
        }

        return col;
    }

    private static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

    private static int getInt(Map<String, Object> map, String key, int defaultVal) {
        Object v = map.get(key);
        if (v == null) {
            return defaultVal;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static boolean getBoolean(Map<String, Object> map, String key, boolean defaultVal) {
        Object v = map.get(key);
        if (v == null) {
            return defaultVal;
        }
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        return Boolean.parseBoolean(v.toString());
    }

    /**
     * 极简递归下降 JSON 解析器。
     * <p>
     * 支持标准 JSON 语法：对象、数组、字符串（含转义）、数字、true/false/null。
     * 不依赖任何外部库，输出 {@code Map<String,Object>} / {@code List<Object>} /
     * {@code String} / {@code Long} / {@code Double} / {@code Boolean} / {@code null}。
     * </p>
     */
    private static class JsonParser {

        private final String json;
        private int pos;

        JsonParser(String json) {
            this.json = json;
        }

        Object parse() {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            if (pos < json.length()) {
                throw error("意外的字符: " + peek());
            }
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (pos >= json.length()) {
                throw error("输入意外结束");
            }
            char c = peek();
            switch (c) {
                case '{':
                    return parseObject();
                case '[':
                    return parseArray();
                case '"':
                    return parseString();
                case 't':
                    return parseKeyword("true", Boolean.TRUE);
                case 'f':
                    return parseKeyword("false", Boolean.FALSE);
                case 'n':
                    return parseKeyword("null", null);
                default:
                    if (c == '-' || (c >= '0' && c <= '9')) {
                        return parseNumber();
                    }
                    throw error("意外字符: " + c);
            }
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{');
            skipWhitespace();
            if (pos < json.length() && peek() == '}') {
                next();
                return map;
            }
            do {
                skipWhitespace();
                if (pos >= json.length() || peek() != '"') {
                    throw error("期望字符串 key");
                }
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
            } while (consume(','));
            expect('}');
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            expect('[');
            skipWhitespace();
            if (pos < json.length() && peek() == ']') {
                next();
                return list;
            }
            do {
                list.add(parseValue());
                skipWhitespace();
            } while (consume(','));
            expect(']');
            return list;
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < json.length()) {
                char c = next();
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    if (pos >= json.length()) {
                        throw error("转义序列不完整");
                    }
                    char esc = next();
                    switch (esc) {
                        case '"':
                            sb.append('"');
                            break;
                        case '\\':
                            sb.append('\\');
                            break;
                        case '/':
                            sb.append('/');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'u':
                            if (pos + 4 > json.length()) {
                                throw error("\\u 转义序列不完整");
                            }
                            sb.append((char) Integer.parseInt(json.substring(pos, pos + 4), 16));
                            pos += 4;
                            break;
                        default:
                            throw error("无效转义: \\" + esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw error("字符串未闭合");
        }

        private Number parseNumber() {
            int start = pos;
            if (peek() == '-') {
                next();
            }
            while (pos < json.length() && Character.isDigit(peek())) {
                next();
            }
            boolean isFloat = false;
            if (pos < json.length() && peek() == '.') {
                isFloat = true;
                next();
                while (pos < json.length() && Character.isDigit(peek())) {
                    next();
                }
            }
            if (pos < json.length() && (peek() == 'e' || peek() == 'E')) {
                isFloat = true;
                next();
                if (pos < json.length() && (peek() == '+' || peek() == '-')) {
                    next();
                }
                while (pos < json.length() && Character.isDigit(peek())) {
                    next();
                }
            }
            String numStr = json.substring(start, pos);
            return isFloat ? Double.parseDouble(numStr) : Long.parseLong(numStr);
        }

        private Object parseKeyword(String keyword, Object value) {
            for (int i = 0; i < keyword.length(); i++) {
                if (pos >= json.length() || json.charAt(pos) != keyword.charAt(i)) {
                    throw error("无效关键字");
                }
                pos++;
            }
            return value;
        }

        private void skipWhitespace() {
            while (pos < json.length()) {
                char c = json.charAt(pos);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    pos++;
                } else {
                    break;
                }
            }
        }

        private char peek() {
            if (pos >= json.length()) {
                throw error("输入意外结束");
            }
            return json.charAt(pos);
        }

        private char next() {
            if (pos >= json.length()) {
                throw error("输入意外结束");
            }
            return json.charAt(pos++);
        }

        private void expect(char c) {
            if (pos >= json.length() || json.charAt(pos) != c) {
                throw error("期望 '" + c + "'");
            }
            pos++;
        }

        private boolean consume(char c) {
            if (pos < json.length() && json.charAt(pos) == c) {
                pos++;
                return true;
            }
            return false;
        }

        private IllegalArgumentException error(String msg) {
            return new IllegalArgumentException("JSON 解析错误 (位置 " + pos + "): " + msg);
        }
    }
}
