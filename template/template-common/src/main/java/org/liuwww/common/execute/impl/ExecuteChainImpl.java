package org.liuwww.common.execute.impl;

import java.util.List;

import org.liuwww.common.context.Context;
import org.liuwww.common.execute.Execute;
import org.liuwww.common.execute.ExecuteChain;

public class ExecuteChainImpl implements ExecuteChain
{

    private List<Execute> executelist;

    private int currentPosition = 0;

    public ExecuteChainImpl(List<Execute> executelist)
    {
        this.executelist = executelist;
    }

    @Override
    public <T> T execute(Context context) throws Exception
    {
        if (currentPosition == executelist.size() - 1)
        {
            return null;
        }
        return executelist.get(this.currentPosition++).execute(context, this);
    }

}
