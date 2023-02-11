package org.liuwww.db.condition;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.liuwww.db.sql.DbType;

import org.liuwww.common.util.StringUtil;

public interface Condition
{
    public static enum ConditionRel {
        AND("and"), OR(" or");

        private String val;

        ConditionRel(String val)
        {
            this.val = val;
        }

        public String getVal()
        {
            return val;
        }
    }

    public class Param implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private List<Object> valList;

        public List<Object> getValList()
        {
            return valList;
        }

        public void setValList(List<Object> valList)
        {
            this.valList = valList;
        }

        public Param(List<Object> valList)
        {
            this.valList = valList;
        }

        public Param(Object val)
        {
            this.valList = new ArrayList<Object>(1);
            this.valList.add(val);
        }

        public boolean isValid()
        {
            if (valList == null)
            {
                return false;
            }
            for (Object val : valList)
            {
                if (val != null)
                {
                    if (!(val instanceof String && StringUtil.isBlank(val.toString())))
                    {
                        return true;
                    }
                    if (val instanceof List && !((List) val).isEmpty())
                    {
                        return true;
                    }
                    if (val.getClass().isArray() && Array.getLength(val) > 0)
                    {
                        return true;
                    }
                    return false;
                }
            }
            return false;
        }

        @Override
        public String toString()
        {
            if (valList == null)
            {
                return null;
            }
            else
            {
                return valList.toString();
            }
        }

    }

    public String getSqlFragment(DbType dbType);

    public List<Object> getParamList();

    public boolean isValid();

    public ConditionRel getCondtionRel();

}
