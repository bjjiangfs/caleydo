package org.caleydo.core.manager.event.view.group;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.manager.event.AEvent;

/**
 * Event that signals that two groups should be interchanged. Depending on a boolean gene or experiment group
 * info has to be used.
 * 
 * @author Bernhard Schlegl
 */
@XmlRootElement
@XmlType
public class InterchangeGroupsEvent
	extends AEvent {

	private boolean bGeneGroup;

	public void setGeneExperimentFlag(boolean bGeneGroup) {
		this.bGeneGroup = bGeneGroup;
	}

	public boolean isGeneGroup() {
		return bGeneGroup;
	}

	@Override
	public boolean checkIntegrity() {
		// nothing to check
		return true;
	}

}
