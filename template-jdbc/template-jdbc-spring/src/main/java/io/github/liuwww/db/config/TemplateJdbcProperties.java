package io.github.liuwww.db.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Template JDBC Spring 配置属性。
 * <p>
 * 通过 {@code application.properties} 或 {@code application.yml} 配置，
 * 前缀为 {@code template-jdbc}。
 * </p>
 *
 * <h3>配置示例</h3>
 * <pre>
 * # 表结构加载方式：datasource（默认）、json、composite
 * template-jdbc.loader-type=datasource
 *
 * # JSON 表结构配置（loader-type=json 时生效）
 * template-jdbc.json.classpath=table-structure.json
 * template-jdbc.json.file=/etc/app/table-structure.json
 *
 * # ID 生成器配置
 * template-jdbc.id-generator.type=snowflake
 * template-jdbc.id-generator.snowflake.worker-id=0
 * template-jdbc.id-generator.snowflake.datacenter-id=0
 * template-jdbc.id-generator.snowflake.id-shift=0
 * template-jdbc.id-generator.snowflake.twepoch=1767225600000
 *
 * # NanoId 配置（id-generator.type=nano-id 时生效）
 * template-jdbc.id-generator.nano-id.size=21
 * </pre>
 *
 * @author liuwww
 * @since 2026-07
 */
@ConfigurationProperties(prefix = "template-jdbc")
public class TemplateJdbcProperties {

    /** 表结构加载方式：datasource、json、composite */
    private String loaderType = "datasource";

    /** JSON 表结构配置 */
    private JsonConfig json = new JsonConfig();

    /** ID 生成器配置 */
    private IdGeneratorConfig idGenerator = new IdGeneratorConfig();

    public String getLoaderType() {
        return loaderType;
    }

    public void setLoaderType(String loaderType) {
        this.loaderType = loaderType;
    }

    public JsonConfig getJson() {
        return json;
    }

    public void setJson(JsonConfig json) {
        this.json = json;
    }

    public IdGeneratorConfig getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGeneratorConfig idGenerator) {
        this.idGenerator = idGenerator;
    }

    /** JSON 表结构加载配置 */
    public static class JsonConfig {

        /** classpath 资源路径（单数据源模式） */
        private String classpath;

        /** 文件系统路径（单数据源模式） */
        private String file;

        /**
         * 多数据源 JSON 路径映射（beanName -> JSON 路径）。
         * <p>
         * 配置后按 beanName 逐个加载对应 JSON 文件并绑定到该数据源。
         * 路径支持 classpath:、http://、https:// 前缀，无前缀按文件路径处理。
         * </p>
         * <p>
         * 示例：
         * <pre>
         * template-jdbc.json.datasources.dataSource=classpath:tables/dataSource.json
         * template-jdbc.json.datasources.dsA=classpath:tables/dsA.json
         * </pre>
         * </p>
         */
        private Map<String, String> datasources = new LinkedHashMap<>();

        public String getClasspath() {
            return classpath;
        }

        public void setClasspath(String classpath) {
            this.classpath = classpath;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public Map<String, String> getDatasources() {
            return datasources;
        }

        public void setDatasources(Map<String, String> datasources) {
            this.datasources = datasources;
        }
    }

    /** ID 生成器配置 */
    public static class IdGeneratorConfig {

        /**
         * ID 生成器类型。
         * <ul>
         * <li>snowflake - 雪花算法（默认）</li>
         * <li>uuid - 随机 UUID</li>
         * <li>sequential-uuid - 时序 UUID（COMB GUID）</li>
         * <li>nano-id - 短随机字符串</li>
         * <li>database-auto-increment - 数据库自增</li>
         * </ul>
         */
        private String type = "snowflake";

        /** Snowflake 配置 */
        private SnowflakeConfig snowflake = new SnowflakeConfig();

        /** NanoId 配置 */
        private NanoIdConfig nanoId = new NanoIdConfig();

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public SnowflakeConfig getSnowflake() {
            return snowflake;
        }

        public void setSnowflake(SnowflakeConfig snowflake) {
            this.snowflake = snowflake;
        }

        public NanoIdConfig getNanoId() {
            return nanoId;
        }

        public void setNanoId(NanoIdConfig nanoId) {
            this.nanoId = nanoId;
        }
    }

    /** Snowflake ID 生成器配置 */
    public static class SnowflakeConfig {

        /** 工作节点 ID (0-7) */
        private long workerId = 0;

        /** 数据中心 ID (0-3) */
        private long datacenterId = 0;

        /** ID 基准值偏移量 */
        private long idShift = 0;

        /** 时间偏移值（毫秒），默认 2026-01-01 00:00:00 UTC */
        private long twepoch = 1767225600000L;

        public long getWorkerId() {
            return workerId;
        }

        public void setWorkerId(long workerId) {
            this.workerId = workerId;
        }

        public long getDatacenterId() {
            return datacenterId;
        }

        public void setDatacenterId(long datacenterId) {
            this.datacenterId = datacenterId;
        }

        public long getIdShift() {
            return idShift;
        }

        public void setIdShift(long idShift) {
            this.idShift = idShift;
        }

        public long getTwepoch() {
            return twepoch;
        }

        public void setTwepoch(long twepoch) {
            this.twepoch = twepoch;
        }
    }

    /** NanoId 生成器配置 */
    public static class NanoIdConfig {

        /** ID 字符长度，默认 21 */
        private int size = 21;

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
