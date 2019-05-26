package org.liuwww.common.context.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.liuwww.common.context.MutiContext;

public class MutiContextImpl extends ContextImpl implements MutiContext, Serializable
{
    private static final long serialVersionUID = -3860938416137025102L;

    private Map<String, MutiContext> subContextMap = new HashMap<String, MutiContext>();

    private MutiContext parent = null;

    public MutiContextImpl()
    {

    }

    public MutiContextImpl(Map<String, Object> map)
    {
        super(map);
    }

    @Override
    public MutiContext getParent()
    {
        return parent;
    }

    @Override
    public void setParent(MutiContext parent)
    {
        this.parent = parent;
    }

    @Override
    public MutiContext createSubContext(String contextName)
    {
        MutiContext context = new MutiContextImpl();
        putSubContextAndSetParent(contextName, context);
        return context;
    }

    @Override
    public MutiContext putSubContext(String contextName, MutiContext context)
    {
        putSubContextAndSetParent(contextName, context);
        return context;
    }

    /**
     * 设置儿子环境，但儿子的不设置当前为父亲
     */
    public void putContext(String contextName, MutiContext context)
    {
        subContextMap.put(contextName, context);
    }

    private void putSubContextAndSetParent(String contextName, MutiContext context)
    {
        context.setParent(this);
        subContextMap.put(contextName, context);
    }

    @Override
    public MutiContext removeSubContext(String contextName)
    {
        return subContextMap.remove(contextName);
    }

    @Override
    public MutiContext getSubContext(String contextName)
    {
        return subContextMap.get(contextName);
    }

    @Override
    public void clearSubContext()
    {
        subContextMap.clear();

    }

    @Override
    public void clear()
    {
        super.clear();
        clearSubContext();
    }

    @Override
    public Map<String, MutiContext> getSubContextMap()
    {
        return subContextMap;
    }

    @Override
    public Object remove(String contextName, String name)
    {
        MutiContext subContext = subContextMap.get(contextName);
        if (subContext != null)
        {
            return subContext.remove(name);
        }
        return null;
    }

    @Override
    public Object getInSubContext(String contextName, String name)
    {
        MutiContext subContext = subContextMap.get(contextName);
        if (subContext != null)
        {
            return subContext.get(name);
        }
        return null;
    }

    /**
     * 改写get方法，使得可以从父环境中查找，同时，也可以从子环境中查找 先找自己，再找子，再找父
     */
    @Override
    public Object get(String name)
    {
        return findNodeMap(name, this, new HashMap<MutiContext, String>());
    }

    /**
     * @Desc:
     * @Date 2017年9月2日上午11:26:22
     * @author liuwww
     * @param name
     * @param contextNode
     * @param up 为null 查找当前，子级，上级；true 不查找子级；false:不查找上级
     * @return
     */
    protected Object findNodeMap(String name, MutiContext contextNode, Map<MutiContext, String> nodeMap)
    {
        Map<String, Object> itemMap = contextNode.getItemMap();

        if (itemMap.containsKey(name))
        {
            return itemMap.get(name);
        }
        else
        {
            nodeMap.put(contextNode, "");
        }
        // 查找子级
        Map<String, MutiContext> cMap = contextNode.getSubContextMap();
        if (!cMap.isEmpty())
        {
            for (MutiContext context : cMap.values())
            {
                if (nodeMap.get(context) == null)
                {
                    Object subResult = findNodeMap(name, context, nodeMap);
                    if (subResult != null)
                    {
                        return subResult;
                    }
                }
            }
        }

        // 查询父级
        MutiContext theParent = contextNode.getParent();
        if (theParent != null && nodeMap.get(theParent) == null)
        {
            return findNodeMap(name, theParent, nodeMap);
        }
        return null;
    }

    protected boolean existNodeMap(String name, MutiContext contextNode, Map<MutiContext, String> nodeMap)
    {

        // 如果当前不存在，则查找父亲中有没有
        // 如果已经存在，则返回之
        if (contextNode.getItemMap().containsKey(name))
        {
            return true;
        }
        else
        {
            nodeMap.put(contextNode, "");
        }

        if (!contextNode.getSubContextMap().isEmpty())
        {
            for (MutiContext context : contextNode.getSubContextMap().values())
            {
                if (nodeMap.get(context) == null)
                {
                    boolean exist = existNodeMap(name, context, nodeMap);
                    if (exist)
                    {
                        return true;
                    }
                }
            }
        }
        MutiContext theParent = contextNode.getParent();
        if ((theParent != null) && (nodeMap.get(theParent) == null))
        {
            return existNodeMap(name, theParent, nodeMap);
        }
        return false;
    }

    protected MutiContext containNodeMap(String name, MutiContext contextNode, Map<MutiContext, String> nodeMap)
    {
        // 如果当前不存在，则查找父亲中有没有
        // 如果已经存在，则返回之
        if (contextNode.getItemMap().containsKey(name))
        {
            return contextNode;
        }
        else
        {
            nodeMap.put(contextNode, "");
        }

        if (!contextNode.getSubContextMap().isEmpty())
        {
            for (MutiContext context : contextNode.getSubContextMap().values())
            {
                if (nodeMap.get(context) == null)
                {
                    MutiContext con = containNodeMap(name, context, nodeMap);
                    if (con != null)
                    {
                        return con;
                    }
                }
            }
        }
        MutiContext theParent = contextNode.getParent();
        if ((theParent != null) && (nodeMap.get(theParent) == null))
        {
            return containNodeMap(name, theParent, nodeMap);
        }
        return null;
    }

    @Override
    public boolean renameKey(String key, String newKey)
    {
        Map<MutiContext, String> nodeMap = new HashMap<MutiContext, String>();
        if (nodeMap.get(this) == null)
        {
            return renameKeyNodeMap(key, newKey, this, nodeMap);
        }
        return false;
    }

    protected boolean renameKeyNodeMap(String key, String newKey, MutiContext contextNode,
            Map<MutiContext, String> nodeMap)
    {
        boolean renamed = renameKeyOfSuper(key, newKey, contextNode);
        MutiContext theParent = contextNode.getParent();
        if (renamed)
        {
            return true;
        }
        else
        {
            nodeMap.put(contextNode, "");
        }
        if (!contextNode.getSubContextMap().isEmpty())
        {
            for (MutiContext context : contextNode.getSubContextMap().values())
            {
                if (nodeMap.get(context) == null)
                {
                    renamed = renameKeyNodeMap(key, newKey, context, nodeMap);
                    if (renamed)
                    {
                        return true;
                    }
                }
            }
        }
        if ((theParent != null) && (nodeMap.get(theParent) == null))
        {
            renamed = renameKeyNodeMap(key, newKey, theParent, nodeMap);
            if (renamed)
            {
                return true;
            }
        }
        return false;
    }

    private boolean renameKeyOfSuper(String key, String newKey, MutiContext context)
    {
        Map<String, Object> itemMap = context.getItemMap();
        if (itemMap.containsKey(key))
        {
            itemMap.put(newKey, itemMap.get(key));
            itemMap.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public boolean exist(String name)
    {
        Map<MutiContext, String> nodeMap = new HashMap<MutiContext, String>();
        return existNodeMap(name, this, nodeMap);

    }

    @Override
    public MutiContext contain(String name)
    {
        Map<MutiContext, String> nodeMap = new HashMap<MutiContext, String>();
        return containNodeMap(name, this, nodeMap);

    }

    @Override
    public Object put(String contextName, String name, Object val)
    {
        MutiContext subContext = subContextMap.get(contextName);
        if (subContext == null)
        {
            subContext = createSubContext(contextName);
        }
        subContext.put(name, val);
        return val;
    }

}
