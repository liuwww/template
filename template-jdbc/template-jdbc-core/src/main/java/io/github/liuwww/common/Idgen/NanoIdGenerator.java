package io.github.liuwww.common.Idgen;

import java.security.SecureRandom;

import io.github.liuwww.db.context.TableMetaData;

/**
 * NanoID 风格的短随机字符串 ID 生成器。
 * <p>
 * 默认生成 21 字符的 URL-safe ID，字符集为 {@code A-Za-z0-9_-}（共 64 字符），
 * 每字符 6 bit，21 字符共 126 bit 随机熵，碰撞概率极低。
 * </p>
 * <p>
 * 特性：
 * <ul>
 * <li>短：21 字符，比 UUID（32 hex）短约 1/3。</li>
 * <li>URL 安全：使用 {@code -} 和 {@code _} 替代 {@code +} 和 {@code /}。</li>
 * <li>不可枚举：纯随机，无时序信息，适合对外暴露的 ID（token、订单号、短链等）。</li>
 * <li>长度可配置：通过构造方法可自定义长度。</li>
 * </ul>
 * </p>
 * <p>
 * 线程安全：内部 {@link SecureRandom} 线程安全，多线程并发调用安全。
 * </p>
 *
 * @author liuwww
 */
public class NanoIdGenerator implements IdGenerator {

    /** 默认长度：21 字符（126 bit 熵，与 NanoID 原始实现一致） */
    public static final int DEFAULT_SIZE = 21;

    /** URL-safe 字符集（64 字符，每字符 6 bit） */
    private static final char[] ALPHABET =
            "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private static final int MASK = 63; // 2^6 - 1

    private final SecureRandom random = new SecureRandom();

    private final int size;

    /**
     * 使用默认长度 21 创建。
     */
    public NanoIdGenerator() {
        this(DEFAULT_SIZE);
    }

    /**
     * 指定长度创建。
     *
     * @param size ID 字符长度，必须 &gt; 0
     */
    public NanoIdGenerator(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive: " + size);
        }
        this.size = size;
    }

    @Override
    public String nextKey(TableMetaData tableMetaData) {
        return fastId();
    }

    /**
     * 生成 ID。
     * <p>
     * 采用批量取随机字节 + 拒绝采样方式，比逐字符调用 random 效率更高，
     * 且避免模运算引入的分布偏差。
     * </p>
     *
     * @return NanoID 字符串
     */
    private String fastId() {
        char[] buf = new char[size];
        // 每个字节提供 8 bit，但每个字符需要 6 bit 的索引。
        // 简单实现：取 size 个字节，每个字节用 6 bit（& 0x3F）；
        // 但 0x3F 对应 0-63，恰好覆盖 64 字符表，无拒绝采样需求。
        // 注意：256 个字节值映射到 64 字符，64 的倍数 = 256 不整除，
        // 但因 256/64 = 4 整除，分布完全均匀，无需拒绝采样。
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        for (int i = 0; i < size; i++) {
            buf[i] = ALPHABET[bytes[i] & MASK];
        }
        return new String(buf);
    }

    @Override
    public int nextIntKey(TableMetaData tableMetaData) {
        throw new UnsupportedOperationException("NanoIdGenerator 仅支持字符串类型的 key");
    }

    @Override
    public long nextLongKey(TableMetaData tableMetaData) {
        throw new UnsupportedOperationException("NanoIdGenerator 仅支持字符串类型的 key");
    }

    @Override
    public boolean isCreatedByDatabase(TableMetaData tableMetaData) {
        return false;
    }

    /**
     * 返回配置的 ID 长度。
     *
     * @return ID 长度
     */
    public int getSize() {
        return size;
    }
}
