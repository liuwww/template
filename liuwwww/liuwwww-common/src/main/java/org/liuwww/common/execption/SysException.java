package org.liuwww.common.execption;

public class SysException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private Integer errId;

    public SysException()
    {

    }

    public SysException(String msg)
    {
        super(msg);
    }

    public SysException(Throwable e)
    {
        super(e);
    }

    public SysException(Integer errId, String msg)
    {
        super(msg);
        this.errId = errId;
    }

    public SysException(String msg, Throwable e)
    {
        super(msg, e);
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
