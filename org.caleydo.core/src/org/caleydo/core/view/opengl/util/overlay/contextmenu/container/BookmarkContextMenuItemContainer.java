package org.caleydo.core.view.opengl.util.overlay.contextmenu.container;

import org.caleydo.core.data.mapping.IDType;
import org.caleydo.core.view.opengl.util.overlay.contextmenu.AItemContainer;
import org.caleydo.core.view.opengl.util.overlay.contextmenu.item.RemoveBookmarkItem;
import org.caleydo.datadomain.genetic.contextmenu.container.ContentContextMenuItemContainer;

/**
 * Implementation of AItemContainer for elements in the bookmark menue. You need to pass an ID and an ID type.
 * 
 * @author Alexander Lex
 */
public class BookmarkContextMenuItemContainer
	extends AItemContainer {

	/**
	 * Constructor.
	 */
	public BookmarkContextMenuItemContainer() {
		super();
	}

	/**
	 * Set the experiment index
	 */
	public void setID(IDType idType, int id) {
		createMenuContent(idType, id);
	}

	private void createMenuContent(IDType idType, int id) {

		if (idType.getCategory() == EIDCategory.GENE) {
			ContentContextMenuItemContainer geneContainer = new ContentContextMenuItemContainer();
			geneContainer.setID(EIDType.DAVID, id);
			addItemContainer(geneContainer);
			addSeparator();
		}

		RemoveBookmarkItem removeBookmarkItem = new RemoveBookmarkItem(idType, id);
		addContextMenuItem(removeBookmarkItem);

	}
}
