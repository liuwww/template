package org.liuwww.common.util;

import java.util.List;

public class FancyTreeNode
{
    private String title;

    private String key;

    private Boolean folder;

    private Boolean lazy;

    private List<FancyTreeNode> children;

    private Boolean checkbox;

    private Object data;

    private Boolean expanded;

    private Boolean focus;

    private String extraClasses;

    private String icon;

    private Boolean selected;

    private String statusNodeType;

    private String tooltip;

    private String pKey;

    public FancyTreeNode()
    {

    }

    public FancyTreeNode(String key, String title, Boolean folder, Boolean lazy)
    {
        super();
        this.title = title;
        this.key = key;
        this.folder = folder;
        this.lazy = lazy;
    }

    public FancyTreeNode(String pKey, String key, String title, Boolean folder, Boolean lazy)
    {
        super();
        this.pKey = pKey;
        this.title = title;
        this.key = key;
        this.folder = folder;
        this.lazy = lazy;
    }

    public String getpKey()
    {
        return pKey;
    }

    public void setpKey(String pKey)
    {
        this.pKey = pKey;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public Boolean isFolder()
    {
        return folder;
    }

    public void setFolder(Boolean folder)
    {
        this.folder = folder;
    }

    public Boolean isLazy()
    {
        return lazy;
    }

    public void setLazy(Boolean lazy)
    {
        this.lazy = lazy;
    }

    public List<FancyTreeNode> getChildren()
    {
        return children;
    }

    public void setChildren(List<FancyTreeNode> children)
    {
        this.children = children;
    }

    public Boolean isCheckbox()
    {
        return checkbox;
    }

    public void setCheckbox(Boolean checkbox)
    {
        this.checkbox = checkbox;
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }

    public Boolean isExpanded()
    {
        return expanded;
    }

    public void setExpanded(Boolean expanded)
    {
        this.expanded = expanded;
    }

    public Boolean isFocus()
    {
        return focus;
    }

    public void setFocus(Boolean focus)
    {
        this.focus = focus;
    }

    public String getExtraClasses()
    {
        return extraClasses;
    }

    public void setExtraClasses(String extraClasses)
    {
        this.extraClasses = extraClasses;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public Boolean isSelected()
    {
        return selected;
    }

    public void setSelected(Boolean selected)
    {
        this.selected = selected;
    }

    public String getStatusNodeType()
    {
        return statusNodeType;
    }

    public void setStatusNodeType(String statusNodeType)
    {
        this.statusNodeType = statusNodeType;
    }

    public String getTooltip()
    {
        return tooltip;
    }

    public void setTooltip(String tooltip)
    {
        this.tooltip = tooltip;
    }

}
