package io.github.liuwww.common.Idgen;

import io.github.liuwww.db.context.TableMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.LockSupport;

/**
 * Snowflake ID 生成器
 *
 * 分布式ID生成算法，生成64位的long型ID，包含时间戳、数据中心ID、工作节点ID和序列号
 *
 * ID结构:
 * 1 bit(符号位) | 44 bits(时间戳) | 2 bits(数据中心ID) | 3 bits(工作节点ID) | 12 bits(序列号)
 *
 * @author lwww
 */
public class SnowflakeIdGenerator implements IdGenerator {
    protected static final Logger LOG = LoggerFactory.getLogger(SnowflakeIdGenerator.class);

    // 节点id
    private final long workerId;

    // 数据中心id
    private final long datacenterId;

    // 默认时间偏移值：2026-01-01 00:00:00 UTC
    private static final long DEFAULT_TWPOCH = 1767225600000L;

    // 时间偏移值（可通过构造方法配置）
    private final long twepoch;

    /**
     * ID 基准值偏移量。
     * <p>
     * 最终生成的 ID = 原始雪花 ID - idShift。
     * 通过设置基准值可以让 ID 从一个较小的值开始增长，避免因时间戳过大导致 ID 数值过长。
     * 默认为 0（不偏移），可通过构造方法自定义。
     * </p>
     */
    private final long idShift;

    // 节点偏移数
    private final long workerIdBits = 3L;

    // 数据中心偏移数
    private final long datacenterIdBits = 2L;

    // 最大节点Id (最多8个节点)
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    // 最大数据中心Id (最多4个数据中心)
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    // 序列位数
    private final long sequenceBits = 12L;

    // 节点偏移量
    private final long workerIdShift = sequenceBits;

    // 数据中心偏移量
    private final long datacenterIdShift = sequenceBits + workerIdBits;

    // 时间戳偏移量 (增加到44位)
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    // 最大序列4095，从0计，共4096个，每毫秒的序列数
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    // 序列号
    private long sequence = 0L;

    // 最后计数的毫秒数
    private long lastTimestamp = -1L;

    /**
     * 构造函数（基准值默认为 0，不偏移）。
     *
     * @param workerId     工作节点ID (0-7)
     * @param datacenterId 数据中心ID (0-3)
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        this(workerId, datacenterId, 0L, DEFAULT_TWPOCH);
    }

    /**
     * 构造函数（可自定义基准值）。
     *
     * @param workerId     工作节点ID (0-7)
     * @param datacenterId 数据中心ID (0-3)
     * @param idShift      ID 基准值偏移量，最终 ID = 原始雪花 ID - idShift
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId, long idShift) {
        this(workerId, datacenterId, idShift, DEFAULT_TWPOCH);
    }

    /**
     * 构造函数（可自定义基准值和时间偏移值）。
     *
     * @param workerId     工作节点ID (0-7)
     * @param datacenterId 数据中心ID (0-3)
     * @param idShift      ID 基准值偏移量，最终 ID = 原始雪花 ID - idShift
     * @param twepoch      时间偏移值（毫秒），ID 时间戳以此为起点计算
     */
    public SnowflakeIdGenerator(long workerId, long datacenterId, long idShift, long twepoch) {
        // sanity check for workerId
        if ((workerId > maxWorkerId) || (workerId < 0)) {
            throw new IllegalArgumentException(String.format("worker Id 不可大于  %d 或小于 0", maxWorkerId));
        }
        if ((datacenterId > maxDatacenterId) || (datacenterId < 0)) {
            throw new IllegalArgumentException(String.format("数据中心 Id 不能大于 %d 或小于 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.idShift = idShift;
        this.twepoch = twepoch;
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("ID生成器初始化. 时间戳 左偏移数 ： %d, 数据中心id位数 ：%d,  节点位数：%d, 每毫秒序列位数： %d, 节点： %d",
                    timestampLeftShift, datacenterIdBits, workerIdBits, sequenceBits, workerId));
        }
    }

    /**
     * 生成下一个唯一ID。
     * <p>
     * 使用 synchronized 保证 sequence 与 lastTimestamp 的一致性，
     * 与原版 {@link SnowflakeIdGenerator2} 实现风格一致。
     * </p>
     *
     * @return 唯一的long型ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            if (LOG.isErrorEnabled()) {
                LOG.error(String.format("时间不可回流，最新的毫秒数： %d.", lastTimestamp));
            }
            throw new RuntimeException(String.format("系统时钟回流了毫秒数： %d milliseconds", lastTimestamp - timestamp));
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                // 序列号回绕，等待到下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 新毫秒，重置序列号
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        long id = ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift) | sequence;
        return (id - idShift);
    }

    /**
     * 等待到下一毫秒
     *
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 下一毫秒的时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            // 序列号耗尽时距下一毫秒可能仅剩亚毫秒，固定挂起 1ms 会造成无谓等待。
            // 采用更细的挂起粒度（0.01ms）并在循环中重新检测，减少跨越时间边界后的多余等待。
            LockSupport.parkNanos(10000L);
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳（毫秒）
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 解析ID信息
     * <p>
     * 注意：传入的 id 应为 {@link #nextId()} 返回的值（已减去 idShift），
     * 此方法会先加回 idShift 还原为原始雪花 ID 再解析。
     * </p>
     *
     * @param id 需要解析的ID
     * @return 包含ID各部分信息的对象
     */
    public IdInfo getIdInfo(long id) {
        id += idShift;
        Long seq = id & sequenceMask;
        Long workId = (id >> workerIdShift) & ((1L << workerIdBits) - 1);
        Long dataId = (id >> datacenterIdShift) & ((1L << datacenterIdBits) - 1);
        Long timestamp = (id >> timestampLeftShift) + twepoch;
        return new IdInfo(workId, dataId, seq, timestamp);
    }

    @Override
    public String nextKey(TableMetaData tmd) {
        return String.valueOf(this.nextId());
    }

    @Override
    public boolean isCreatedByDatabase(TableMetaData tmd) {
        return false;
    }

    @Override
    public int nextIntKey(TableMetaData tableMetaData) {
        long id = nextId();
        if (id > Integer.MAX_VALUE || id < Integer.MIN_VALUE) {
            throw new ArithmeticException("Generated ID is out of integer range: " + id);
        }
        return (int) id;
    }

    @Override
    public long nextLongKey(TableMetaData tableMetaData) {
        return this.nextId();
    }

    // Getter methods for configuration values
    public long getTwepoch() {
        return twepoch;
    }

    public long getIdShift() {
        return idShift;
    }

    public long getWorkerId() {
        return workerId;
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public long getWorkerIdBits() {
        return workerIdBits;
    }

    public long getDatacenterIdBits() {
        return datacenterIdBits;
    }

    public long getSequenceBits() {
        return sequenceBits;
    }
}
