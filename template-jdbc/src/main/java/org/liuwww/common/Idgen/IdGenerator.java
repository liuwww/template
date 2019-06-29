package org.liuwww.common.Idgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Snowflake ID
 * @author lwww 2017年6月20日下午5:06:27
 */
public class IdGenerator
{
    protected static final Logger LOG = LoggerFactory.getLogger(IdGenerator.class);

    // 节点id
    private long workerId;

    // 数据中心id
    private long datacenterId;

    private long sequence = 0L;

    // 2016-01-01 00:00:00 时间偏移值
    private long twepoch = 1451577600000L;

    // 节点偏移数
    private long workerIdBits = 5L;

    // 数据中心偏移数
    private long datacenterIdBits = 5L;

    // 31 最大节点Id
    private long maxWorkerId = -1L ^ (-1L << workerIdBits);

    // 31 最大数据中心Id
    private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    // 序列位数
    private long sequenceBits = 12L;

    // 节点偏移量
    private long workerIdShift = sequenceBits;

    // 数据中心偏移量
    private long datacenterIdShift = sequenceBits + workerIdBits;

    // 时间戳偏移量
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    // 最大序列4095，从0计，共4096个，每毫秒的序列数
    private long sequenceMask = -1L ^ (-1L << sequenceBits);

    // 最后计数的毫秒数
    private long lastTimestamp = -1L;

    // id值偏移值
    // 最后id=id-idShift，该值为workerId=0,datacenterId=0,timestamp=1497948346923,sequence=1,date=2017-06-20
    // 16:45:46.923时的值
    private long idShift = 194493009302126592L;

    public IdGenerator(long workerId, long datacenterId)
    {
        // sanity check for workerId
        if ((workerId > maxWorkerId) || (workerId < 0))
        {
            throw new IllegalArgumentException(String.format("worker Id 不可大于  %d 或小于 0", maxWorkerId));
        }
        if ((datacenterId > maxDatacenterId) || (datacenterId < 0))
        {
            throw new IllegalArgumentException(String.format("数据中心 Id 不能大于 %d 或小于 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        LOG.info(String.format("ID生成器初始化. 时间戳 左偏移数 ： %d, 数据中心id位数 ：%d,  节点位数：%d, 每毫秒序列位数： %d, 节点： %d",
                timestampLeftShift, datacenterIdBits, workerIdBits, sequenceBits, workerId));
    }

    public synchronized long nextId()
    {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp)
        {
            LOG.error(String.format("时间不可回流，最新的毫秒数： %d.", lastTimestamp));
            throw new RuntimeException(String.format("系统时钟回流了毫秒数： %d milliseconds", lastTimestamp - timestamp));
        }
        if (lastTimestamp == timestamp)
        {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0)
            {
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        else
        {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        long id = ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift) | sequence;
        return (id - idShift);
    }

    protected long tilNextMillis(long lastTimestamp)
    {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp)
        {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen()
    {
        return System.currentTimeMillis();
    }

    public IdInfo getIdInfo(Long id)
    {
        Long seq = (id + idShift) & 0xfff;
        Long workId = (id >> 12) & 0x1f;
        Long dataId = (id >> 17) & 0x1f;
        Long timestamp = (id >> 22) + twepoch;
        return new IdInfo(workId, dataId, seq, timestamp);
    }

}
