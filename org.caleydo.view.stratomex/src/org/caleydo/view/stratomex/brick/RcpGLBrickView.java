/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.stratomex.brick;

import org.caleydo.core.view.ARcpGLViewPart;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.eclipse.swt.widgets.Composite;

/**
 * RCP View for a single Brick. Mainly intended for development purposes.
 *
 * @author Alexander Lex
 */
public class RcpGLBrickView extends ARcpGLViewPart {

	/**
	 * Constructor.
	 */
	public RcpGLBrickView() {
		super(SerializedBrickView.class);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		view = new GLBrick(glCanvas, ViewFrustum.createDefault());
		initializeView();
		createPartControlGL();
	}

	@Override
	public void createDefaultSerializedView() {
		serializedView = new SerializedBrickView();
		determineDataConfiguration(serializedView);
	}

}
