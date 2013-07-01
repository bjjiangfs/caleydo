/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.enroute.path;

import java.util.List;

import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.view.MinSizeUpdateEvent;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.listener.EnablePathSelectionEvent;
import org.caleydo.datadomain.pathway.listener.PathwayPathSelectionEvent;

/**
 * Strategy for {@link APathwayPathRenderer}s that shall always be in sync with the currently selected path.
 *
 * @author Christian Partl
 *
 */
public class SelectedPathUpdateStrategy extends APathUpdateStrategy {

	/**
	 * @param renderer
	 */
	public SelectedPathUpdateStrategy(APathwayPathRenderer renderer, String pathwayPathEventSpace) {
		super(renderer, pathwayPathEventSpace);
	}

	@Override
	public void onEnablePathSelection(EnablePathSelectionEvent event) {
		// path selection is done by manipulating the displayed path itself
	}

	@Override
	public void onSelectedPathChanged(PathwayPathSelectionEvent event) {
		renderer.setPath(event.getPathSegmentsAsVertexList());
	}

	@Override
	public void triggerPathUpdate() {
		triggerPathUpdate(renderer.pathSegments);
		MinSizeUpdateEvent event = new MinSizeUpdateEvent(renderer, renderer.minHeightPixels, renderer.minWidthPixels);
		event.setEventSpace(pathwayPathEventSpace);
		EventPublisher.INSTANCE.triggerEvent(event);
	}

	@Override
	public void nodesCreated() {
		// nothing to do
	}

	@Override
	public boolean isPathChangePermitted(List<List<PathwayVertexRep>> newPath) {
		return true;
	}

}
