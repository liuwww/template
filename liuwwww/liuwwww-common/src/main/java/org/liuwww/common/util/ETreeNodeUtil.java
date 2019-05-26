package org.liuwww.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.liuwww.common.util.ETreeNode.STATE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * easyui ETreeNode 节点类的工具类,后续增加treeGird处理方法
 * <p>
 * 提供根据给定的类的list转换成TreeNode的list的方法；如TreeNode中parentId有效，则提供方法获取children处理的方法，
 * 返回页面需要的list
 *
 * @author lww 2014-2-10
 * @since 1.0
 */
public class ETreeNodeUtil
{

    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(ETreeNodeUtil.class);

    /**
     * 根据给定的list，list转换的mapObj获取children处理好的TreeNode list
     */
    public static <T> List<ETreeNode> getOrderedTreeNodes(List<T> objList, TreeNodeMapObj mapObj)
    {
        return getOrderedTreeNodes(getTreeNodes(objList, mapObj));
    }

    /**
     * 根据给定的ETreeNode list，获取children处理好的ETreeNode list,其中parentId须有效
     */
    public static List<ETreeNode> getOrderedTreeNodes(List<ETreeNode> nodeList)
    {
        Map<String, ETreeNode> nodeMap = new HashMap<String, ETreeNode>();
        for (ETreeNode node : nodeList)
        {
            nodeMap.put(node.getId(), node);
        }
        for (ETreeNode node : nodeList)
        {
            String parentId = node.getParentId();
            ETreeNode pNode = nodeMap.get(parentId);
            if (pNode != null)
            {
                List<ETreeNode> children = pNode.getChildren();
                if (children == null)
                {
                    children = new ArrayList<ETreeNode>();
                    pNode.setChildren(children);
                }
                children.add(node);
            }
        }
        List<ETreeNode> resultList = new ArrayList<ETreeNode>();
        for (ETreeNode node : nodeList)
        {
            String parentId = node.getParentId();
            if ((parentId == null) || "".equals(parentId))
            {
                resultList.add(node);
            }
            else
            {
                ETreeNode pNode = nodeMap.get(parentId);
                if (pNode == null)
                {
                    resultList.add(node);
                }
            }

        }
        return resultList;
    }

    /**
     * 根据给定的list，list转换的mapObj获取对应的TreeNode 的list
     */
    public static <T> List<ETreeNode> getTreeNodes(List<T> objList, TreeNodeMapObj mapObj)
    {
        List<ETreeNode> nodeList = new ArrayList<ETreeNode>();
        for (T t : objList)
        {
            ETreeNode node = getNodeByMapObj(t, mapObj);
            nodeList.add(node);
        }
        return nodeList;
    }

    /**
     * 设定叶子节点的state为closed 以异步加载该子节点的子节点
     */
    public static void setLeafNodeStateClosed(List<ETreeNode> nodeList)
    {
        for (ETreeNode node : nodeList)
        {
            List<ETreeNode> children = node.getChildren();
            if ((children == null) || (children.size() == 0))
            {
                node.setState(STATE.CLOSED);
            }
            else
            {
                setLeafNodeStateClosed(node.getChildren());
            }
        }
    }

    /**
     * @Desc:对也在节点做些处理
     * @Date 2018年9月14日下午4:53:43
     * @author liuwww
     * @param nodeList
     * @param filter
     */
    public static void leafNodeFilter(List<ETreeNode> nodeList, NoteFileter filter)
    {
        for (ETreeNode node : nodeList)
        {
            List<ETreeNode> children = node.getChildren();
            if ((children == null) || (children.size() == 0))
            {
                filter.execute(node);
            }
            else
            {
                leafNodeFilter(node.getChildren(), filter);
            }
        }
    }

    /**
     * @Desc：使节点呈现关闭状态
     * @Date 2018年6月25日下午4:47:09
     * @author liuwww
     * @param nodeList
     */
    public static void setPNodeStateClosed(List<ETreeNode> nodeList)
    {
        if (nodeList == null)
        {
            return;
        }
        for (ETreeNode node : nodeList)
        {
            List<ETreeNode> children = node.getChildren();
            if ((children != null) && (children.size() > 0))
            {
                node.setState(STATE.CLOSED);
            }
            else
            {
                setPNodeStateClosed(node.getChildren());
            }
        }
    }

    /**
     * 排序
     */
    public static void sort(List<ETreeNode> nodeList)
    {
        Collections.sort(nodeList, new Comparator<ETreeNode>()
        {
            @Override
            public int compare(ETreeNode o1, ETreeNode o2)
            {
                return o1.getPosition() - o2.getPosition();
            }
        });
        for (ETreeNode node : nodeList)
        {
            List<ETreeNode> children = node.getChildren();
            if (children != null && children.size() > 1)
            {
                sort(children);
            }
        }
    }

    /**
     * 获取有序的treegrid数据，T对象需包含其自身id 父id，children字段，children为其自身的list类型
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getOrderTreeGridData(List<T> objList, TreeNodeMapObj mapObj)
    {
        String childrenName = mapObj.getChildrenAttrName();
        if (childrenName == null)
        {
            childrenName = "children";
        }
        Map<String, T> nodeMap = new HashMap<String, T>();
        for (T node : objList)
        {

            String val = getStrFieldValue(node, mapObj.getIdAttrName());
            nodeMap.put(val, node);
        }
        for (T node : objList)
        {
            String parentId = getStrFieldValue(node, mapObj.getParentIdAttrName());
            T pNode = nodeMap.get(parentId);
            if (pNode != null)
            {
                List<T> children = (List<T>) getFieldValue(pNode, childrenName);
                if (children == null)
                {
                    children = new ArrayList<T>();
                    setFieldValue(pNode, childrenName, children);
                }
                children.add(node);
            }
        }
        List<T> resultList = new ArrayList<T>();
        for (T node : objList)
        {
            String parentId = getStrFieldValue(node, mapObj.getParentIdAttrName());
            if ((parentId == null) || "".equals(parentId))
            {
                resultList.add(node);
            }
            else
            {
                T pNode = nodeMap.get(parentId);
                if (pNode == null)
                {
                    resultList.add(node);
                }
            }
        }
        return resultList;
    }

    private static <T> String getStrFieldValue(T obj, String fieldName)
    {
        return BeanUtils.getProperty(obj, fieldName);
    }

    private static <T> void setFieldValue(T obj, String fieldName, Object value)
    {
        BeanUtils.setProperty(obj, fieldName, value);
    }

    private static <T> Object getFieldValue(T obj, String fieldName)
    {
        return BeanUtils.getPropertyValue(obj, fieldName);
    }

    private static <T> ETreeNode getNodeByMapObj(T obj, TreeNodeMapObj mapObj)
    {
        ETreeNode node = new ETreeNode();
        List<String> fileds = EntryUtil.getFieldList(TreeNodeMapObj.class);
        for (String name : fileds)
        {
            try
            {
                String attrName = (String) EntryUtil.getFieldValue(mapObj, name);
                if (attrName != null)
                {
                    if (name.equals("attributesAttrName") && attrName.equals("this"))
                    {
                        node.setAttributes(obj);
                    }
                    else
                    {
                        Object param = EntryUtil.getFieldValue(obj, attrName);
                        String efildName = name.substring(0, name.indexOf("AttrName"));
                        if (StringUtil.equals(efildName, "position"))
                        {
                            EntryUtil.setFieldValue(node, efildName,
                                    param == null ? 0 : Short.parseShort(param.toString()));
                        }
                        else
                        {
                            EntryUtil.setFieldValue(node, efildName, param == null ? null : param.toString());
                        }

                    }
                }
            }
            catch (SecurityException e)
            {
                e.printStackTrace();
            }
        }
        return node;

    }

    /**
     * TreeNode与给定的类属性对应关系类
     */
    public static class TreeNodeMapObj
    {
        private String idAttrName;

        private String textAttrName;

        private String stateAttrName;

        private String checkedAttrName;

        private String attributesAttrName;

        private String childrenAttrName;

        private String parentIdAttrName;

        private String positionAttrName;

        public TreeNodeMapObj()
        {
        }

        public TreeNodeMapObj(String idAttrName, String textAttrName)
        {
            this.idAttrName = idAttrName;
            this.textAttrName = textAttrName;
        }

        public TreeNodeMapObj(String idAttrName, String textAttrName, String parentIdAttrName)
        {
            this.idAttrName = idAttrName;
            this.textAttrName = textAttrName;
            this.parentIdAttrName = parentIdAttrName;
        }

        public TreeNodeMapObj(String idAttrName, String textAttrName, String attributesAttrName,
                String childrenAttrName)
        {
            this.idAttrName = idAttrName;
            this.textAttrName = textAttrName;
            this.attributesAttrName = attributesAttrName;
            this.childrenAttrName = childrenAttrName;
        }

        public TreeNodeMapObj(String idAttrName, String textAttrName, String stateAttrName, String checkedAttrName,
                String attributesAttrName, String childrenAttrName)
        {
            this.idAttrName = idAttrName;
            this.textAttrName = textAttrName;
            this.stateAttrName = stateAttrName;
            this.checkedAttrName = checkedAttrName;
            this.attributesAttrName = attributesAttrName;
            this.childrenAttrName = childrenAttrName;
        }

        public String getIdAttrName()
        {
            return this.idAttrName;
        }

        public void setIdAttrName(String idAttrName)
        {
            this.idAttrName = idAttrName;
        }

        public String getTextAttrName()
        {
            return this.textAttrName;
        }

        public void setTextAttrName(String textAttrName)
        {
            this.textAttrName = textAttrName;
        }

        public String getStateAttrName()
        {
            return this.stateAttrName;
        }

        public void setStateAttrName(String stateAttrName)
        {
            this.stateAttrName = stateAttrName;
        }

        public String getCheckedAttrName()
        {
            return this.checkedAttrName;
        }

        public void setCheckedAttrName(String checkedAttrName)
        {
            this.checkedAttrName = checkedAttrName;
        }

        public String getAttributesAttrName()
        {
            return this.attributesAttrName;
        }

        public void setAttributesAttrName(String attributesAttrName)
        {
            this.attributesAttrName = attributesAttrName;
        }

        public String getChildrenAttrName()
        {
            return this.childrenAttrName;
        }

        public void setChildrenAttrName(String childrenAttrName)
        {
            this.childrenAttrName = childrenAttrName;
        }

        public String getParentIdAttrName()
        {
            return this.parentIdAttrName;
        }

        public void setParentIdAttrName(String parentIdAttrName)
        {
            this.parentIdAttrName = parentIdAttrName;
        }

        public String getPositionAttrName()
        {
            return positionAttrName;
        }

        public void setPositionAttrName(String positionAttrName)
        {
            this.positionAttrName = positionAttrName;
        }

    }

    public interface NoteFileter
    {
        public void execute(ETreeNode node);
    }

}
