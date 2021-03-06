/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.info.selection;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.caleydo.core.data.selection.EventBasedSelectionManager;
import org.caleydo.core.data.selection.IEventBasedSelectionManagerUser;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.view.CaleydoRCPViewPart;
import org.caleydo.view.info.selection.external.OpenExternalAction;
import org.caleydo.view.info.selection.external.OpenExternalWrappingAction;
import org.caleydo.view.info.selection.model.CategoryItem;
import org.caleydo.view.info.selection.model.ElementItem;
import org.caleydo.view.info.selection.model.SelectionTypeItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;

/**
 * Search view contains gene and pathway search.
 *
 * @author Marc Streit
 * @author Alexander Lex
 */
public class RcpSelectionInfoView extends CaleydoRCPViewPart implements IEventBasedSelectionManagerUser {
	public static final String VIEW_TYPE = "org.caleydo.view.info.selection";

	private final NavigableSet<CategoryItem> categories = new TreeSet<>();
	private TreeViewer selectionTree;

	private OpenExternalWrappingAction openExternalWrapper;
	private CopySelectionToClipBoardAction copyToClipBoard;

	/**
	 * Constructor.
	 */
	public RcpSelectionInfoView() {
		super(SerializedSelectionInfoView.class);

		for (IDCategory idCategory : IDCategory.getAllRegisteredIDCategories()) {
			if (idCategory.isInternaltCategory() || idCategory.getPrimaryMappingType() == null)
				continue;
			CategoryItem item = new CategoryItem(idCategory, this);
			categories.add(item);
			item.getManager().registerEventListeners();
		}
	}

	@Override
	public boolean isSupportView() {
		return true;
	}

	@Override
	public void dispose() {
		for (CategoryItem item : categories)
			item.getManager().unregisterEventListeners();
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {

		parentComposite = new Composite(parent, SWT.NULL);
		parentComposite.setLayout(new FillLayout());

		selectionTree = new TreeViewer(parentComposite, SWT.NONE);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessVerticalSpace = true;
		gridData.minimumHeight = 150;
		selectionTree.getTree().setLayoutData(gridData);

		selectionTree.setContentProvider(new SelectionContentProvider());
		selectionTree.setLabelProvider(new SelectionLabelProvider());
		selectionTree.setAutoExpandLevel(2);
		selectionTree.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof CategoryItem && !isSelected(element, false)) {
					return ((CategoryItem) element).getTotal() > 0;
				}
				// if selected or parent selected and a default one
				if (element instanceof SelectionTypeItem
						&& !(isSelected(element, true) || (isSelected(parentElement, true) && ((SelectionTypeItem) element)
								.isDefaultOne())))
					return ((SelectionTypeItem) element).size() > 0;
				return true;
			}
		});

		this.injectContextMenu();

		selectionTree.setInput(this.categories);

		fillToolBar();
	}

	/**
	 * @param element
	 * @param checkParents
	 * @return
	 */
	protected boolean isSelected(Object element, boolean leafesOnly) {
		ITreeSelection selection = (ITreeSelection)selectionTree.getSelection();
		for (TreePath path : selection.getPaths()) {
			if (leafesOnly) {
				if (path.getLastSegment() == element)
					return true;
			} else {
				for (int i = 0; i < path.getSegmentCount(); ++i) {
					if (path.getSegment(i) == element)
						return true;
				}
			}
		}
		return false;
	}

	private static class SelectionContentProvider implements ITreeContentProvider {

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		@Override
		public Object[] getElements(Object inputElement) {
			@SuppressWarnings("unchecked")
			final NavigableSet<CategoryItem> items = (NavigableSet<CategoryItem>) inputElement;
			return items.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof CategoryItem)
				return ((CategoryItem) parentElement).getChildren().toArray();
			if (parentElement instanceof SelectionTypeItem)
				return ((SelectionTypeItem) parentElement).getChildren().toArray();
			if (parentElement instanceof ElementItem)
				return Collections.emptyList().toArray();
			return null;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof CategoryItem)
				return null;
			if (element instanceof SelectionTypeItem)
				return ((SelectionTypeItem) element).getParent();
			if (element instanceof ElementItem)
				return ((ElementItem) element).getParent();
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof CategoryItem)
				return !((CategoryItem) element).getChildren().isEmpty();
			if (element instanceof SelectionTypeItem)
				return !((SelectionTypeItem) element).isEmpty();
			if (element instanceof ElementItem)
				return false;
			return false;
		}

	}

	private static class SelectionLabelProvider extends StyledCellLabelProvider {
		@Override
		protected void measure(Event event, Object element) {
			super.measure(event, element);
			// if (element instanceof SelectionTypeItem) {
			// Point size = event.gc.textExtent("a\nb");
			// event.height = Math.max(event.height, size.y);
			// }
		}

		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			StyledString text = new StyledString();

			if (element instanceof CategoryItem) {
				CategoryItem item = (CategoryItem) element;
				text.append(item.getLabel());
				if (item.size() > 0)
					text.append(String.format(" (%d)", item.size()), StyledString.COUNTER_STYLER);
				cell.setText(text.toString());
				cell.setStyleRanges(text.getStyleRanges());
			} else if (element instanceof SelectionTypeItem) {
				SelectionTypeItem item = (SelectionTypeItem) element;
				cell.setBackground(item.getColor());
				text.append(item.getLabel());
				int base = item.getParent().size();
				if (base > 0) {
					text.append(
							String.format(" %d (%.1f%%) ", item.size(), item.size() * 100.f / item.getParent().size()),
							StyledString.COUNTER_STYLER);
				} else {
					text.append(String.format(" %d ", item.size()), StyledString.COUNTER_STYLER);
				}
				text.append(item.getPreview(), StyledString.QUALIFIER_STYLER);
				cell.setText(text.toString());
				cell.setStyleRanges(text.getStyleRanges());
			} else if (element instanceof ElementItem) {
				ElementItem item = (ElementItem) element;
				cell.setText(item.getLabel());
			}
			super.update(cell);
		}
	}

	private void injectContextMenu() {
		// initalize the context menu
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				Object element = ((StructuredSelection) selectionTree.getSelection()).getFirstElement();
				if (element == null)
					return;
				manager.add(copyToClipBoard);
				if (element instanceof CategoryItem) {
					// CategoryItem item = (CategoryItem)element;
				} else if (element instanceof SelectionTypeItem) {
					SelectionTypeItem item = (SelectionTypeItem) element;
					if (!SelectionType.isDefaultType(item.getSelectionType())) {
						manager.add(new RemoveSelectionTypeAction(item.getSelectionType()));
					}
				} else if (element instanceof ElementItem) {
					IAction action = openExternalWrapper.getWrappee();
					if (action == null)
						return;
					manager.add(action);
				}

			}
		});
		Menu menu = menuMgr.createContextMenu(selectionTree.getTree());
		selectionTree.getTree().setMenu(menu);

		selectionTree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object element = ((StructuredSelection) event.getSelection()).getFirstElement();
				if (element instanceof ElementItem) {
					ElementItem item = (ElementItem) element;
					IAction action = OpenExternalAction.create(item.getIDType(), item.getId());
					openExternalWrapper.setWrappee(action);
				}
				copyToClipBoard.setSelection((ITreeSelection) event.getSelection());
			}
		});
	}

	@Override
	public void addToolBarContent(IToolBarManager toolBarManager) {
		this.openExternalWrapper = new OpenExternalWrappingAction();
		toolBarManager.add(openExternalWrapper);
		this.copyToClipBoard = new CopySelectionToClipBoardAction();
		toolBarManager.add(copyToClipBoard);
		super.addToolBarContent(toolBarManager);
	}

	@Override
	public void createDefaultSerializedView() {
		serializedView = new SerializedSelectionInfoView();
		determineDataConfiguration(serializedView, false);
	}

	@Override
	public synchronized void notifyOfSelectionChange(final EventBasedSelectionManager selectionManager) {
		if (parentComposite.isDisposed()) {
			return;
		}
		parentComposite.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IDCategory target = selectionManager.getIDType().getIDCategory();
				for (CategoryItem item : categories)
					if (item.getCategory() == target) {
						item.update();
						for (SelectionTypeItem s : item.getChildren()) {
							if (s.size() > 0 && s.size() <= 4 && !selectionTree.getExpandedState(s))
								selectionTree.setExpandedState(s, true);
						}
						// selectionTree.setHasChildren(item, item.getTotal() > 0);
						break;
					}
				// selectionTree.setInput(categories);
				selectionTree.refresh();
				selectionTree.expandToLevel(2);
			}
		});
	}
}
