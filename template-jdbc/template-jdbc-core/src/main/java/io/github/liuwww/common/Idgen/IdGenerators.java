package io.github.liuwww.common.Idgen;

/**
 * 中立的 ID 生成器全局工具类。
 * <p>
 * 持有一个 {@code volatile} 静态实例作为"全局默认 ID 生成器"，
 * 默认使用 {@link SnowflakeIdGenerator}（workerId=0, datacenterId=0）。
 * 应用启动时可通过 {@link #setDefault(IdGenerator)} 替换为其他实现
 * （如 {@link SequentialUUIDGenerator}、{@link NanoIdGenerator} 等）。
 * </p>
 * <p>
 * <b>层级关系：</b>
 * <ul>
 * <li><b>全局级</b>：本类持有的静态实例，所有未显式注入 IdGenerator 的
 * {@code DataTemplate} 均回退到此实例。</li>
 * <li><b>Template 级</b>：通过 {@code DataTemplate} 构造函数或
 * {@code CoreDbContext.setIdGenerator(...)} 注入的实例，
 * 优先于全局级，与具体数据库/数据源绑定。</li>
 * </ul>
 * </p>
 * <p>
 * <b>线程安全说明：</b>
 * <ul>
 * <li>{@code defaultGenerator} 字段为 {@code volatile}，引用更新对所有线程立即可见。</li>
 * <li>具体实现的线程安全由各 {@link IdGenerator} 自行保证
 * （如 {@link SnowflakeIdGenerator#nextId()} 内部 {@code synchronized}）。</li>
 * <li>{@code setDefault} 替换引用是原子操作，正在执行的 {@code nextKey} 会持有旧引用完成本次调用。</li>
 * <li><b>使用约定：</b>{@code setDefault} 应在应用启动阶段（生成 ID 之前）调用一次。
 * 运行时反复替换可能导致旧实例的序列状态不延续到新实例。</li>
 * </ul>
 * </p>
 *
 * @author liuwww
 */
public final class IdGenerators {

    private static volatile IdGenerator defaultGenerator = new SnowflakeIdGenerator(0, 0);

    private IdGenerators() {
    }

    /**
     * 设置全局默认 ID 生成器。
     * <p>
     * 应在应用启动时（生成 ID 之前）调用，替换默认的 {@link SnowflakeIdGenerator}。
     * </p>
     *
     * @param generator 新的全局默认 ID 生成器，不允许为 null
     * @throws NullPointerException 当传入 null 时
     */
    public static void setDefault(IdGenerator generator) {
        if (generator == null) {
            throw new NullPointerException("default IdGenerator cannot be null");
        }
        defaultGenerator = generator;
    }

    /**
     * 获取全局默认 ID 生成器。
     *
     * @return 全局默认 ID 生成器实例（永不为 null）
     */
    public static IdGenerator getDefault() {
        return defaultGenerator;
    }

    /**
     * 通过全局默认生成器生成下一个字符串 ID。
     *
     * @return 下一个 ID
     */
    public static String nextKey() {
        return defaultGenerator.nextKey(null);
    }

    /**
     * 通过全局默认生成器生成下一个 long ID。
     * <p>
     * 注意：调用此方法要求全局默认实现支持 long 类型 ID（如 {@link SnowflakeIdGenerator}）。
     * 若实现为 {@link UUIDGenerator} 等仅支持字符串的实现，将抛 {@link UnsupportedOperationException}。
     * </p>
     *
     * @return 下一个 long ID
     */
    public static long nextId() {
        return defaultGenerator.nextLongKey(null);
    }
}
