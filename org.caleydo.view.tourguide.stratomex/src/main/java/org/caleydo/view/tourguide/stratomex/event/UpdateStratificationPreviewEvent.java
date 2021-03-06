/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.tourguide.stratomex.event;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.ADirectedEvent;

/**
 * @author Samuel Gratzl
 *
 */
public class UpdateStratificationPreviewEvent extends ADirectedEvent {
	private final TablePerspective tablePerspective;

	public UpdateStratificationPreviewEvent(TablePerspective tablePerspective) {
		this.tablePerspective = tablePerspective;
	}

	/**
	 * @return the tablePerspective, see {@link #tablePerspective}
	 */
	public TablePerspective getTablePerspective() {
		return tablePerspective;
	}


	@Override
	public boolean checkIntegrity() {
		return tablePerspective != null;
	}
}
