package org.liuwww.common.execption;

/**
 * 业务逻辑的异常，用于中断逻辑等操作
 * @author lwww 2017年8月12日下午4:09:15
 */
public class BusinessExecption extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private Integer errId;

    public BusinessExecption()
    {

    }

    public BusinessExecption(String msg)
    {
        super(msg);
    }

    public BusinessExecption(Integer errId, String msg)
    {
        super(msg);
        this.errId = errId;
    }

    public Integer getErrId()
    {
        return errId;
    }

    public void setErrId(Integer errId)
    {
        this.errId = errId;
    }

}
