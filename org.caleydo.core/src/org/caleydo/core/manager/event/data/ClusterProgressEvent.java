package org.caleydo.core.manager.event.data;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.manager.event.AEvent;

@XmlRootElement
@XmlType
public class ClusterProgressEvent
	extends AEvent {

	private int percentCompleted = -1;
	private boolean forSimilaritiesBar;

	public ClusterProgressEvent() {
		// nothing to initialize here
	}
	
	public ClusterProgressEvent(int percentCompleted, boolean forSimilaritiesBar) {
		this.percentCompleted = percentCompleted;
		this.forSimilaritiesBar = forSimilaritiesBar;
	}

	public boolean isForSimilaritiesBar() {
		return forSimilaritiesBar;
	}

	public int getPercentCompleted() {
		return percentCompleted;
	}

	@Override
	public boolean checkIntegrity() {
		if (percentCompleted < 0 || percentCompleted > 100)
			throw new IllegalStateException("percentCompleted was outside of the valid range (0 - 100): "
				+ percentCompleted);

		return true;
	}

	public void setPercentCompleted(int percentCompleted) {
		this.percentCompleted = percentCompleted;
	}

	public void setForSimilaritiesBar(boolean forSimilaritiesBar) {
		this.forSimilaritiesBar = forSimilaritiesBar;
	}

}
