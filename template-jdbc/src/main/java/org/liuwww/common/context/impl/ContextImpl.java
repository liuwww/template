package org.liuwww.common.context.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.liuwww.common.context.Context;

public class ContextImpl implements Context, Serializable
{
    private static final long serialVersionUID = 7948206583720581442L;

    protected Map<String, Object> itemMap;

    public ContextImpl()
    {
        itemMap = new HashMap<String, Object>();
    }

    public ContextImpl(Map<String, Object> map)
    {
        itemMap = new HashMap<String, Object>(map);
    }

    @Override
    public boolean exist(String name)
    {
        return itemMap.containsKey(name);
    }

    @Override
    public void clear()
    {
        itemMap.clear();
    }

    @Override
    public Map<String, Object> getItemMap()
    {
        return Collections.unmodifiableMap(itemMap);
    }

    @Override
    public int size()
    {
        return itemMap.size();
    }

    @Override
    public boolean renameKey(String key, String newKey)
    {
        if (itemMap.containsKey(key))
        {
            itemMap.put(newKey, itemMap.get(key));
            itemMap.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public Object remove(String name)
    {
        return itemMap.remove(name);
    }

    @Override
    public Object get(String name)
    {
        return itemMap.get(name);
    }

    @Override
    public Object get(String name, Object defaultValue)
    {
        Object val = itemMap.get(name);
        return val == null ? defaultValue : val;
    }

    @Override
    public Object put(String name, Object value)
    {
        return itemMap.put(name, value);
    }

    @Override
    public void putAll(Map<String, Object> map)
    {
        itemMap.putAll(map);
    }
}
