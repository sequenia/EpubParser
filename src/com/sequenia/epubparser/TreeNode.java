package com.sequenia.epubparser;

import java.util.HashMap;

public class TreeNode<T> {
	private TreeNode<T> parent;
	private HashMap<String, TreeNode<T>> children;
	private T data;
	
	public TreeNode() {
		children = new HashMap<String, TreeNode<T>>();
	}
	
	public void setParent(TreeNode<T> _parent) {
		parent = _parent;
	}
	
	public TreeNode<T> getParent() {
		return parent;
	}
	
	public void addChild(String name, TreeNode<T> child) {
		children.put(name, child);
	}
	
	public TreeNode<T> getChild(String name) {
		return children.get(name);
	}
	
	public void setData(T _data) {
		data = _data;
	}
	
	public T getData() {
		return data;
	}
}