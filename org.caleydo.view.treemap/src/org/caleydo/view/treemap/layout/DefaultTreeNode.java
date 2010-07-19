package org.caleydo.view.treemap.layout;

import java.awt.Color;
import java.util.List;

public class DefaultTreeNode implements AbstractTreeNode {

	float size;
	Color color;
	List<AbstractTreeNode> children;
	String label="";
	
	
	public DefaultTreeNode(double size, Color color, List<AbstractTreeNode> children, String label){
		this.size=(float) size;
		this.color=color;
		this.children=children;
		this.label=label;
	}
	
	public DefaultTreeNode(double size, Color color, List<AbstractTreeNode> children){
		this(size, color, children, "");
	}
	
	@Override
	public float getAreaSize() {
		// TODO Auto-generated method stub
		return size;
	}

	@Override
	public Color getAreaColor() {
		// TODO Auto-generated method stub
		return color;
	}

	@Override
	public List<AbstractTreeNode> getChildren() {
		// TODO Auto-generated method stub
		return children;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return label;
	}

}
