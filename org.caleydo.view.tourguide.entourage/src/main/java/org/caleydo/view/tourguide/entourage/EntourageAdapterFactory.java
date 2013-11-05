/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.tourguide.entourage;

import org.caleydo.view.entourage.GLEntourage;
import org.caleydo.view.entourage.RcpGLSubGraphView;
import org.caleydo.view.tourguide.api.query.EDataDomainQueryMode;
import org.caleydo.view.tourguide.api.vis.ITourGuideView;
import org.caleydo.view.tourguide.spi.adapter.IViewAdapter;
import org.caleydo.view.tourguide.spi.adapter.IViewAdapterFactory;
import org.eclipse.ui.IViewPart;

/**
 * @author Samuel Gratzl
 *
 */
public class EntourageAdapterFactory implements IViewAdapterFactory {

	@Override
	public IViewAdapter createFor(IViewPart view, EDataDomainQueryMode mode, ITourGuideView vis) {
		if (!(view instanceof RcpGLSubGraphView))
			return null;
		GLEntourage entourage = ((RcpGLSubGraphView) view).getView();
		switch (mode) {
		case STRATIFICATIONS:
			return new EntourageAdapter(entourage, vis);
		case OTHER:
			return new EntourageOtherAdapter(entourage, vis);
		default:
			return null;
		}
	}

}