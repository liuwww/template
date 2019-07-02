package org.liuwww.common.execption;

public class DbException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private Integer errId;

    public DbException()
    {

    }

    public DbException(String msg)
    {
        super(msg);
    }

    public DbException(Throwable e)
    {
        super(e);
    }

    public DbException(Integer errId, String msg)
    {
        super(msg);
        this.errId = errId;
    }

    public DbException(String msg, Throwable e)
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
