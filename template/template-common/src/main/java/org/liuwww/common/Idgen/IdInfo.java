package org.liuwww.common.Idgen;

import java.util.Date;

import org.liuwww.common.util.DateUtil;

/**
 * Title: IdInfo <br/>
 * Description: Id组成信息<br/>
 * @author liuwww
 * @date 2016-5-18下午5:20:30
 */
public class IdInfo
{
    private long workerId;

    private long datacenterId;

    private long sequence = 0L;

    private long timestamp;

    public long getWorkerId()
    {
        return workerId;
    }

    public IdInfo(long workerId, long datacenterId, long sequence, long timestamp)
    {
        super();
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.sequence = sequence;
        this.timestamp = timestamp;
    }

    public void setWorkerId(long workerId)
    {
        this.workerId = workerId;
    }

    public long getDatacenterId()
    {
        return datacenterId;
    }

    public void setDatacenterId(long datacenterId)
    {
        this.datacenterId = datacenterId;
    }

    public long getSequence()
    {
        return sequence;
    }

    public void setSequence(long sequence)
    {
        this.sequence = sequence;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder(super.toString());
        str.append("{workerId=").append(this.workerId);
        str.append(",datacenterId=").append(this.datacenterId);
        str.append(",timestamp=").append(this.timestamp);
        str.append(",sequence=").append(this.sequence);
        str.append(",date=").append(DateUtil.format(new Date(this.timestamp), "yyyy-MM-dd HH:mm:ss.SSS"));
        str.append("}");
        return str.toString();
    }
}
