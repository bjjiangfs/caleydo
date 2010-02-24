package org.caleydo.view.scatterplot.listener;

import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;
import org.caleydo.core.manager.event.view.storagebased.ToggleColorModeEvent;
import org.caleydo.view.scatterplot.GLScatterPlot;

/**
 * Listener for
 * 
 * @author Juergen Pillhofer
 */
public class ToggleColorModeListener extends AEventListener<GLScatterPlot> {

	@Override
	public void handleEvent(AEvent event) {
		if (event instanceof ToggleColorModeEvent) {

			handler.toggleColorMode();
		}
	}

}
