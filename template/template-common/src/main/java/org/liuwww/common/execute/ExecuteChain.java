package org.liuwww.common.execute;

import org.liuwww.common.context.Context;

public interface ExecuteChain
{
    public <T> T execute(Context context) throws Exception;
}
