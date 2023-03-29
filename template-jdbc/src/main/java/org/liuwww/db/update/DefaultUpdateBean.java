package org.liuwww.db.update;

import java.util.HashMap;
import java.util.Map;

import org.liuwww.db.service.IDataTemplate;

public class DefaultUpdateBean implements UpdateBean
{

    private Map<String, Object> valMap;

    private Map<String, Object> paramMap;

    private IDataTemplate dataTemplate;

    private String tableName;

    public DefaultUpdateBean(String tableName, IDataTemplate dataTemplate)
    {
        super();
        this.tableName = tableName;
        this.dataTemplate = dataTemplate;
        this.valMap = new HashMap<>();
        this.paramMap = new HashMap<>();
    }

    @Override
    public int update()
    {
        return dataTemplate.updateRows(tableName, valMap, paramMap);
    }

    @Override
    public UpdateBean addCondition(String name, Object val)
    {
        paramMap.put(name, val);
        return this;
    }

    @Override
    public UpdateBean addValue(String field, Object val)
    {
        valMap.put(field, val);
        return this;
    }

    // @Override
    // public UpdateBean addVal(String field, Object val)
    // {
    // // TODO Auto-generated method stub
    // return null;
    // }

}
