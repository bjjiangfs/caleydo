/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.tourguide.stratomex.t;

import java.util.Collection;

import org.caleydo.view.stratomex.GLStratomex;
import org.caleydo.view.tourguide.api.adapter.TourGuideDataModes;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.view.tourguide.api.model.PathwayPerspectiveRow;
import org.caleydo.view.tourguide.spi.score.IScore;
import org.caleydo.view.tourguide.stratomex.event.UpdatePathwayPreviewEvent;

/**
 * facade / adapter to {@link GLStratomex} to hide the communication details
 *
 * @author Samuel Gratzl
 *
 */
public class PathwayStratomexAdapter extends AStratomexAdapter {
	public static final String SECONDARY_ID = "PATHWAYS";

	public PathwayStratomexAdapter() {
		super(TourGuideDataModes.PATHWAYS);
	}

	@Override
	public String getPartName() {
		return "Dependent Pathways";
	}

	@Override
	public String getSecondaryID() {
		return SECONDARY_ID;
	}

	@Override
	protected void updatePreview(AScoreRow old, AScoreRow new_, Collection<IScore> visibleColumns, IScore sortedBy) {
		updatePathway((PathwayPerspectiveRow) old, (PathwayPerspectiveRow) new_, visibleColumns, sortedBy);
	}

	private void updatePathway(PathwayPerspectiveRow old, PathwayPerspectiveRow new_,
			Collection<IScore> visibleColumns, IScore sortedBy) {
		if (new_ != null && hasOne()) {
			UpdatePathwayPreviewEvent event = new UpdatePathwayPreviewEvent(new_.getPathway());
			event.to(getAddin());
			triggerEvent(event);
		}
	}
}
