package org.caleydo.view.compare.toolbar;

import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.data.loader.ResourceLoader;
import org.caleydo.rcp.view.toolbar.IToolBarItem;
import org.caleydo.rcp.view.toolbar.action.AToolBarAction;
import org.caleydo.view.compare.event.UseSortingEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

/**
 * @author Alexander Lex
 * 
 */
public class UseSortingAction extends AToolBarAction implements IToolBarItem {
	public static final String TEXT = "Use sorting to reduce clutter";
	public static final String ICON = "resources/icons/view/storagebased/parcoords/angular_brush.png";

	private boolean useSorting = true;

	/**
	 * Constructor.
	 */
	public UseSortingAction(int iViewID) {
		super(iViewID);

		setText(TEXT);
		setToolTipText(TEXT);
		setImageDescriptor(ImageDescriptor.createFromImage(new ResourceLoader()
				.getImage(PlatformUI.getWorkbench().getDisplay(), ICON)));
		super.setChecked(useSorting);
	}

	public void setSortingEnabled(boolean sortingEnabled) {
		this.useSorting = sortingEnabled;
	}

	@Override
	public void run() {
		super.run();
		if (useSorting)
			useSorting = false;
		else
			useSorting = true;

		GeneralManager.get().getEventPublisher().triggerEvent(
				new UseSortingEvent(useSorting));
	};
}
