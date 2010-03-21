package org.caleydo.view.compare.event;

import org.caleydo.core.manager.event.AEvent;

/**
 * @author Alexander Lex
 * 
 */
public class UseSortingEvent extends AEvent {

	private boolean useSorting;

	public UseSortingEvent() {
	}

	public UseSortingEvent(boolean useSorting) {
		this.useSorting = useSorting;
	}

	public void setUseSorting(boolean useSorting) {
		this.useSorting = useSorting;
	}

	public boolean isUseSorting() {
		return useSorting;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
