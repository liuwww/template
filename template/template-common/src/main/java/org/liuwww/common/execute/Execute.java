package org.liuwww.common.execute;

import org.liuwww.common.context.Context;

public interface Execute
{
    public void init();

    public void destroy();

    public <T> T execute(Context context, ExecuteChain chain) throws Exception;
}
