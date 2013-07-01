/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.vis.rank.ui;

import gleem.linalg.Vec2f;

import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.data.loader.ResourceLocators;
import org.caleydo.vis.rank.config.IRankTableUIConfig;
import org.caleydo.vis.rank.layout.IRowHeightLayout;
import org.caleydo.vis.rank.layout.RowHeightLayouts;
import org.caleydo.vis.rank.model.RankTableModel;

/**
 * basic ui widget for a {@link RankTableModel}
 *
 * @author Samuel Gratzl
 *
 */
public class TableUI extends GLElementContainer implements IGLLayout {
	public TableUI(RankTableModel table, IRankTableUIConfig config, IRowHeightLayout... layouts) {
		setLayout(this);
		this.add(new TableHeaderUI(table, config));
		this.add(new TableBodyUI(table, layouts.length == 0 ? RowHeightLayouts.UNIFORM : layouts[0], config));
	}

	public TableBodyUI getBody() {
		return (TableBodyUI) get(1);
	}

	public TableHeaderUI getHeader() {
		return (TableHeaderUI) get(0);
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		IGLLayoutElement header = children.get(0);
		IGLLayoutElement body = children.get(1);
		float hi = header.getSetHeight();
		header.setBounds(0, 0, w, hi);
		body.setBounds(0, hi, w, h - hi);
		Vec2f old = getLayoutDataAs(Vec2f.class, new Vec2f(0, 0));
		Vec2f new_ = body.getLayoutDataAs(Vec2f.class, new Vec2f(0, 0));
		if (!old.equals(new_)) {
			setLayoutData(new_.copy());
			relayoutParent();
		}
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		// push my resource locator to find the icons
		g.pushResourceLocator(ResourceLocators.classLoader(this.getClass().getClassLoader()));

		super.renderImpl(g, w, h);

		g.popResourceLocator();
	}
}
