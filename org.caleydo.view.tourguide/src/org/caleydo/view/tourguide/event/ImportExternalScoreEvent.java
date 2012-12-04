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
package org.caleydo.view.tourguide.event;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.event.AEvent;
import org.caleydo.view.tourguide.data.serialize.ISerializeableScore;

/**
 * @author Samuel Gratzl
 *
 */
public class ImportExternalScoreEvent extends AEvent {
	private ATableBasedDataDomain dataDomain;
	private boolean inDimensionDirection;
	private Class<? extends ISerializeableScore> type;

	public ImportExternalScoreEvent() {

	}

	public ImportExternalScoreEvent(ATableBasedDataDomain dataDomain, boolean inDimensionDirection,
			Class<? extends ISerializeableScore> type) {
		super();
		this.dataDomain = dataDomain;
		this.inDimensionDirection = inDimensionDirection;
		this.type = type;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.caleydo.core.event.AEvent#checkIntegrity()
	 */
	@Override
	public boolean checkIntegrity() {
		return dataDomain != null && type != null;
	}

	/**
	 * @return the type, see {@link #type}
	 */
	public Class<? extends ISerializeableScore> getType() {
		return type;
	}

	/**
	 * @return the inDimensionDirection, see {@link #inDimensionDirection}
	 */
	public boolean isInDimensionDirection() {
		return inDimensionDirection;
	}

	/**
	 * @return the dataDomain, see {@link #dataDomain}
	 */
	public ATableBasedDataDomain getDataDomain() {
		return dataDomain;
	}

}

