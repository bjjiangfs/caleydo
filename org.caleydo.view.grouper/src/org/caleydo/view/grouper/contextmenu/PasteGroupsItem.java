package org.caleydo.view.grouper.contextmenu;

import org.caleydo.core.view.contextmenu.ContextMenuItem;
import org.caleydo.view.grouper.event.PasteGroupsEvent;

public class PasteGroupsItem extends ContextMenuItem {

	public PasteGroupsItem(int parentGroupID, String dataDomainID) {

		setLabel("Paste");
		
		PasteGroupsEvent event = new PasteGroupsEvent(parentGroupID);
		event.setSender(this);
		event.setDataDomainID(dataDomainID);
		registerEvent(event);
	}
}
