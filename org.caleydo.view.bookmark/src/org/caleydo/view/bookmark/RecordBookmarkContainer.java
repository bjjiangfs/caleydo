package org.caleydo.view.bookmark;

import java.util.Set;

import org.caleydo.core.data.id.IDCategory;
import org.caleydo.core.data.id.IDType;
import org.caleydo.core.data.selection.RecordSelectionManager;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.manager.event.data.BookmarkEvent;
import org.caleydo.core.util.collection.UniqueList;
import org.caleydo.core.util.mapping.color.ColorMapper;
import org.caleydo.core.util.mapping.color.ColorMappingManager;
import org.caleydo.core.util.mapping.color.EColorMappingType;

/**
 * A concrete implementation of ABookmarkContainer for the category
 * {@link EIDCategory#GENE}
 * 
 * @author Alexander Lex
 */
class RecordBookmarkContainer extends ABookmarkContainer<RecordSelectionManager> {

	ColorMapper colorMapping;
	IDCategory category;
	IDType idType;

	RecordBookmarkContainer(GLBookmarkView manager, IDCategory category, IDType idType) {
		super(manager, category, manager.getDataDomain().getPrimaryRecordMappingType());
		bookmarkItems = new UniqueList<ABookmark>();
		this.idType = idType;
		this.category = category;

		colorMapping = ColorMappingManager.get().getColorMapping(
				EColorMappingType.GENE_EXPRESSION);

		selectionManager = 	new RecordSelectionManager(idType);

	}

	@Override
	<IDDataType> void handleNewBookmarkEvent(BookmarkEvent<IDDataType> event) {
		// ArrayList<Integer> ids;
		Set<Integer> convertedIDs;

		for (IDDataType id : event.getBookmarks()) {

			if (event.getIDType().getIDCategory() == category) {
				convertedIDs = GeneralManager.get().getIDMappingManager()
						.getIDAsSet(event.getIDType(), idType, id);
				if (convertedIDs == null || convertedIDs.size() == 0)
					continue;
			} else
				throw new IllegalStateException("ID type: " + idType + " unhandled");

			RecordBookmark bookmark = new RecordBookmark(manager, this, idType,
					convertedIDs.iterator().next(), manager.getMinSizeTextRenderer());
			if (bookmarkItems.add(bookmark))
				containerLayout.append(bookmark.getLayout());
			// selectionManager.add(davidID);
		}
		updateContainerSize();

	}


}