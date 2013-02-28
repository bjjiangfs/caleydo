/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander Lex, Christian Partl, Johannes Kepler
 * University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.datadomain.pathway.toolbar;

import java.util.ArrayList;

import org.caleydo.core.gui.SimpleAction;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.datadomain.pathway.graph.PathwayPath;
import org.caleydo.datadomain.pathway.listener.PathwayPathSelectionEvent;

/**
 * Button to clear a selected path in the pathway view.
 *
 * @author Christian Partl
 *
 */
public class ClearPathAction extends SimpleAction {

	public static final String LABEL = "Clear path";
	public static final String ICON = "resources/icons/view/pathway/clear_path.png";

	private String eventSpace;

	/**
	 * Constructor.
	 */
	public ClearPathAction(String eventSpace) {
		super(LABEL, ICON);
		setChecked(false);
		this.eventSpace = eventSpace;
	}

	@Override
	public void run() {
		super.run();
		setChecked(false);
		PathwayPathSelectionEvent pathEvent = new PathwayPathSelectionEvent();

		// for (PathwayPath pathSegment : pathSegmentList) {
		// pathSegments.add(pathSegment);
		// }
		// if (selectedPath != null && pathSegments!=null && pathSegments.size()>0) {
		// //pathSegments.get(pathSegments.size()-1).setPathway(selectedPath);
		// //pathSegments.set(pathSeg, element)
		// }

		pathEvent.setPathSegments(new ArrayList<PathwayPath>());
		pathEvent.setSender(this);
		pathEvent.setEventSpace(eventSpace);
		GeneralManager.get().getEventPublisher().triggerEvent(pathEvent);
	}
}