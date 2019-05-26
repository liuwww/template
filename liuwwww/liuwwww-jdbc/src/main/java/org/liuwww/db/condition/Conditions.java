package org.liuwww.db.condition;

import org.liuwww.db.context.TableMetaData;
import org.liuwww.db.sql.Column;

public class Conditions extends AbstractCompare<Conditions>
{
    protected TableMetaData tmd;

    protected boolean isOne = false;

    public Conditions()
    {
    }

    public Conditions(TableMetaData tmd, boolean isOne)
    {
        this.tmd = tmd;
        this.isOne = isOne;
    }

    @Override
    protected String getColumn(String field)
    {
        if (!isOne || tmd == null)
        {
            return field;
        }
        Column column = tmd.getColumn(field);
        if (column == null)
        {
            return null;
        }
        return super.getColumn(column.getColumnName());
    }

    public TableMetaData getTmd()
    {
        return tmd;
    }

    public void setTmd(TableMetaData tmd)
    {
        this.tmd = tmd;
    }

    public boolean isOne()
    {
        return isOne;
    }

    public void setOne(boolean isOne)
    {
        this.isOne = isOne;
    }
}