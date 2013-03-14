/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.vis.rank.ui;

import java.awt.Color;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.basic.RadioController;
import org.caleydo.core.view.opengl.layout2.basic.ScrollBar;
import org.caleydo.core.view.opengl.layout2.basic.ScrolledGLElement;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.vis.rank.config.IRankTableUIConfig;
import org.caleydo.vis.rank.internal.ui.ButtonBar;
import org.caleydo.vis.rank.layout.IRowHeightLayout;
import org.caleydo.vis.rank.model.RankTableModel;

/**
 * @author Samuel Gratzl
 *
 */
public class RankTableUI extends GLElementContainer implements ISelectionCallback {

	public RankTableUI() {
	}

	public void init(RankTableModel table, IRankTableUIConfig config, IRowHeightLayout... layouts) {
		setLayout(GLLayouts.flowVertical(0));
		if (layouts.length > 1) {
			ButtonBar buttons = new ButtonBar();
			buttons.setzDelta(0.5f);
			RadioController radio = new RadioController(this);
			for(int i = 0; i < layouts.length; ++i) {
				GLButton b = new GLButton();
				b.setLayoutData(layouts[i]);
				b.setRenderer(renderer);
				radio.add(b);
				buttons.addButton(b);
			}
			this.add(buttons);
		}
		TableUI tableui = new TableUI(table, config, layouts);
		ScrolledGLElement sc = new ScrolledGLElement(tableui, new ScrollBar(true), null, RenderStyle.SCROLLBAR_WIDTH);
		this.add(sc);
		if (config.isInteractive() && !table.getConfig().isDestroyOnHide())
			this.add(new ColumnPoolUI(table, config));
	}

	private static final IGLRenderer renderer = new  IGLRenderer() {
		@Override
		public void render(GLGraphics g, float w, float h, GLElement parent) {
			IRowHeightLayout l = parent.getLayoutDataAs(IRowHeightLayout.class, null);
			boolean selected = ((GLButton)parent).isSelected();
			g.fillImage(l.getIcon(), 0, 0, w, h);
			if (selected)
				g.color(Color.RED).drawRect(0, 0, w, h);
		}
	};


	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		IRowHeightLayout l = button.getLayoutDataAs(IRowHeightLayout.class, null);
		ScrolledGLElement scbody = (ScrolledGLElement) get(1);
		TableUI table = (TableUI) scbody.getContent();
		TableBodyUI body = (TableBodyUI) table.get(1);
		body.setRowLayout(l);
	}
}