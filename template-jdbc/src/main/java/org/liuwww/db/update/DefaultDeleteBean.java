package org.liuwww.db.update;

import java.util.HashMap;
import java.util.Map;

import org.liuwww.db.service.IDataTemplate;

public class DefaultDeleteBean implements DeleteBean
{

    private Map<String, Object> paramMap;

    private IDataTemplate dataTemplate;

    private String tableName;

    public DefaultDeleteBean(String tableName, IDataTemplate dataTemplate)
    {
        super();
        this.dataTemplate = dataTemplate;
        this.tableName = tableName;
        this.paramMap = new HashMap<>();
    }

    @Override
    public int delete()
    {
        return dataTemplate.deleteRows(tableName, paramMap);
    }

    @Override
    public DeleteBean addCondition(String name, Object val)
    {
        paramMap.put(name, val);
        return this;
    }

}
