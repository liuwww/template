package org.liuwww.common.context.util;

import org.liuwww.common.context.Context;
import org.liuwww.common.context.MutiContext;
import org.liuwww.common.context.impl.ContextImpl;
import org.liuwww.common.context.impl.MutiContextImpl;

public final class ContextFactory
{
    private ContextFactory()
    {

    }

    public static MutiContext getMutiContext()
    {
        return new MutiContextImpl();
    }

    public static Context getContext()
    {
        return new ContextImpl();
    }
}
