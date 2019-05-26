package org.liuwww.db.sql;

import org.liuwww.db.condition.CompareOpe;
import org.liuwww.db.condition.OneCondition;

public class OracleSqlBeanBuilder extends AbstractSqlBeanBuilder implements SqlBeanBuilder
{

    @Override
    public String buildConditonSqlFragment(OneCondition c, DbType dbType)
    {
        if (c.getOpe() != CompareOpe.notNull)
        {
            return new StringBuilder(c.getCondtionRel().getVal()).append(c.getField()).append(c.getOpe().getVal())
                    .append(" ? ").toString();
        }
        else
        {
            return new StringBuilder(c.getCondtionRel().getVal()).append("  ").append(c.getField())
                    .append(" is not null ").toString();
        }
    }

}
