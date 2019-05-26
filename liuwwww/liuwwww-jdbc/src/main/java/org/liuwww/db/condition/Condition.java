package org.liuwww.db.condition;

import java.util.ArrayList;
import java.util.List;

import org.liuwww.db.sql.DbType;

import org.liuwww.common.util.StringUtil;

public interface Condition
{
    public static enum ConditionRel {
        AND("and"), OR("or");

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

    public class Param
    {
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
            this.valList = new ArrayList<Object>(0);
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
                if (val != null && !(val instanceof String && StringUtil.isBlank(val.toString())))
                {
                    return true;
                }
            }
            return false;
        }

    }

    public String getSqlFragment(DbType dbType);

    public List<Object> getParamList();

    public boolean isValid();

    public ConditionRel getCondtionRel();

}
