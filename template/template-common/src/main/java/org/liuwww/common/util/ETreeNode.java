package org.liuwww.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ETreeNode implements Comparable<ETreeNode> {
	private String id; // 节点id，对载入远程数据很重要

	private String text; // 显示在节点的文本

	private String parentId; // 父节点id，在界面显示时没有使用

	private String state = STATE.OPEN.toString(); // 节点状态，'open' or
													// 'closed'，默认为'open'。当设置为'closed'时，拥有子节点的节点将会从远程站点载入它们

	private boolean checked = false; // 表明节点是否被选择

	private short position;
	private Object attributes; // 可以为节点添加的自定义属性

	private List<ETreeNode> children; // 子节点，必须用数组定义

	public ETreeNode() {
	}

	public ETreeNode(String id, String text, String parentId) {
		this.id = id;
		this.text = text;
		this.parentId = parentId;
	}

	public ETreeNode(String id, String text, String parentId, STATE state,
			boolean checked) {
		this.id = id;
		this.text = text;
		this.parentId = parentId;
		this.state = state.toString();
		this.checked = checked;
	}

	public ETreeNode(String id, String text, String parentId, STATE state,
			boolean checked, Map<String, Object> attributes) {
		this.id = id;
		this.text = text;
		this.parentId = parentId;
		this.state = state.toString();
		this.checked = checked;
		this.attributes = attributes;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getState() {
		return state;
	}

	public void setState(STATE state) {
		this.state = state.toString();
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public Object getAttributes() {
		return attributes;
	}

	public void setAttributes(Object attributes) {
		this.attributes = attributes;
	}

	public List<ETreeNode> getChildren() {
		return children;
	}

	public void setChildren(List<ETreeNode> children) {
		this.children = children;
	}

	public void addChild(ETreeNode child) {
		if (children == null) {
			children = new ArrayList<ETreeNode>();
		}
		children.add(child);
	}

	public enum STATE {
		OPEN {
			@Override
			public String toString() {
				return "open";
			}
		},
		CLOSED {
			@Override
			public String toString() {
				return "closed";
			}
		}
	}

	public short getPosition() {
		return position;
	}

	public void setPosition(short position) {
		this.position = position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ETreeNode o) {
		return this.position - o.position;
	}
}
