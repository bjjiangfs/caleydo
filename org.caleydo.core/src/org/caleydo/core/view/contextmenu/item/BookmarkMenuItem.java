package org.caleydo.core.view.contextmenu.item;

import java.util.ArrayList;

import org.caleydo.core.data.id.IDType;
import org.caleydo.core.manager.event.data.BookmarkEvent;
import org.caleydo.core.view.contextmenu.ContextMenuItem;

/**
 * Item that adds a selected element to the bookmark container
 * 
 * @author Alexander Lex
 * @author Marc Streit
 */
public class BookmarkMenuItem
	extends ContextMenuItem {

	/**
	 * Constructor which takes a single dimension index.
	 */
	public BookmarkMenuItem(String label, IDType idType, int id) {
		setLabel(label);

		BookmarkEvent<Integer> event = new BookmarkEvent<Integer>(idType);
		event.addBookmark(id);
		event.setSender(this);
		registerEvent(event);
	}

	/**
	 * Constructor which takes an array of dimension indices.
	 */
	public BookmarkMenuItem(String label, IDType idType, ArrayList<Integer> ids) {
		setLabel(label);

		BookmarkEvent<Integer> event = new BookmarkEvent<Integer>(idType);
		event.setSender(this);

		for (Integer id : ids)
			event.addBookmark(id);
		registerEvent(event);
	}
}