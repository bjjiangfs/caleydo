package org.caleydo.core.view.opengl.canvas.listener;

import org.caleydo.core.manager.event.AEvent;
import org.caleydo.core.manager.event.AEventListener;
import org.caleydo.core.manager.event.view.ResetAllViewsEvent;

/**
 * Events that signals that all view that are of the type IResettableView should be resetted
 * 
 * @author Alexander Lex
 */
public class ResetViewListener
	extends AEventListener<IResettableView> {

	@Override
	public void handleEvent(AEvent event) {
		if (event instanceof ResetAllViewsEvent)
			handler.resetView();
	}

}
