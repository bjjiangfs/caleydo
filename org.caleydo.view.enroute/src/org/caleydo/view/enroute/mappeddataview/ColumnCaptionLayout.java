/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.enroute.mappeddataview;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.Column;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.util.button.Button;
import org.caleydo.core.view.opengl.util.button.ButtonRenderer;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import org.caleydo.view.enroute.EPickingType;

/**
 * @author alexsb
 *
 */
public class ColumnCaptionLayout extends Column {

	AGLView parentView;
	MappedDataRenderer parent;
	Group group;

	// private static final int abstractModePixelWidth = 40;
	//
	// private float dymanicWidth;

	/**
	 *
	 */
	public ColumnCaptionLayout(AGLView parentView, MappedDataRenderer parent) {
		this.parentView = parentView;
		this.parent = parent;
		super.setBottomUp(false);
		// dymanicWidth = ratioSizeX;

	}

	public void init(Group group, Perspective samplePerspective, ATableBasedDataDomain dataDomain) {
		this.group = group;
		ElementLayout caption = new ElementLayout();
		this.append(caption);
		Button button = new Button(EPickingType.SAMPLE_GROUP_VIEW_MODE.name(), group.getID(),
				EIconTextures.ABSTRACT_BAR_ICON);
		ColumnCaptionRenderer renderer = new ColumnCaptionRenderer(parentView, parent, group, samplePerspective,
				dataDomain, button);
		caption.setRenderer(renderer);


		// button.setVisible(false);
		ButtonRenderer buttonRender = new ButtonRenderer.Builder(parentView, button).build();
		ElementLayout spacing = new ElementLayout();
		spacing.setPixelSizeY(2);

		append(spacing);
		ElementLayout buttonLayout = new ElementLayout();
		buttonLayout.setPixelSizeX(20);
		buttonLayout.setPixelSizeY(20);
		buttonLayout.setRenderer(buttonRender);
		this.append(buttonLayout);
	}

}
