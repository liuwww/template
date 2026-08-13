package io.github.liuwww.common.Idgen;

import java.security.SecureRandom;

import io.github.liuwww.db.context.TableMetaData;

/**
 * 时序 UUID 生成器（COMB GUID 风格）。
 * <p>
 * 生成 32 位十六进制字符串，结构如下：
 * </p>
 * <pre>
 *   12 hex (毫秒时间戳)  |  20 hex (随机)
 *   tttttttttttt          rrrrrrrrrrrrrrrrrrrr
 * </pre>
 * <p>
 * 与 {@link UUIDGenerator} 的 32 位 hex 长度完全兼容，可直接替换。
 * 区别在于前 12 位为时间戳，使相邻插入的 ID 在 B-tree 索引中趋于连续分布，
 * 显著降低 InnoDB 聚簇索引的页分裂与碎片化，写入性能优于完全随机的 UUID。
 * </p>
 * <p>
 * 随机部分 80 bit（10 字节），由 {@link SecureRandom} 生成。
 * 单毫秒内生成 100 万个 ID 的碰撞概率约 4×10⁻¹³，可忽略，
 * 无需额外计数器。
 * </p>
 * <p>
 * 线程安全：{@link SecureRandom} 线程安全，多线程并发调用安全。
 * </p>
 *
 * @author liuwww
 * @since 2026-07
 */
public class SequentialUUIDGenerator implements IdGenerator {

    /** 时间戳部分长度（hex 字符数） */
    private static final int TIMESTAMP_HEX_LENGTH = 12;

    /** 随机部分长度（hex 字符数） */
    private static final int RANDOM_HEX_LENGTH = 20;

    /** 总长度：32 hex（与 UUID 去横线格式一致） */
    private static final int TOTAL_LENGTH = TIMESTAMP_HEX_LENGTH + RANDOM_HEX_LENGTH;

    /** 随机部分字节数（20 hex = 10 bytes） */
    private static final int RANDOM_BYTES = RANDOM_HEX_LENGTH / 2;

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private final SecureRandom random = new SecureRandom();

    @Override
    public String nextKey(TableMetaData tableMetaData) {
        long timestamp = System.currentTimeMillis();
        byte[] randBytes = new byte[RANDOM_BYTES];
        random.nextBytes(randBytes);

        char[] buf = new char[TOTAL_LENGTH];

        // 前 12 hex：毫秒时间戳（48 bit）
        long tsPart = timestamp;
        for (int i = TIMESTAMP_HEX_LENGTH - 1; i >= 0; i--) {
            buf[i] = HEX_DIGITS[(int) (tsPart & 0xF)];
            tsPart >>>= 4;
        }

        // 后 20 hex：随机字节（每字节高低 nibble 各 1 个 hex）
        for (int i = 0; i < RANDOM_HEX_LENGTH; i++) {
            int byteIdx = i / 2;
            int nibble = (i % 2 == 0)
                    ? (randBytes[byteIdx] >> 4) & 0xF
                    : randBytes[byteIdx] & 0xF;
            buf[TIMESTAMP_HEX_LENGTH + i] = HEX_DIGITS[nibble];
        }

        return new String(buf);
    }

    @Override
    public int nextIntKey(TableMetaData tableMetaData) {
        throw new UnsupportedOperationException("SequentialUUIDGenerator 仅支持字符串类型的 key");
    }

    @Override
    public long nextLongKey(TableMetaData tableMetaData) {
        throw new UnsupportedOperationException("SequentialUUIDGenerator 仅支持字符串类型的 key");
    }

    @Override
    public boolean isCreatedByDatabase(TableMetaData tableMetaData) {
        return false;
    }
}
