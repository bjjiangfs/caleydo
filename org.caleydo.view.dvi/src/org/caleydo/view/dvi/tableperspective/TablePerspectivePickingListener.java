/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dvi.tableperspective;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.IMultiTablePerspectiveBasedView;
import org.caleydo.core.view.contextmenu.GenericContextMenuItem;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.view.dvi.GLDataViewIntegrator;
import org.caleydo.view.dvi.contextmenu.AddTablePerspectiveToViewsItemContainer;
import org.caleydo.view.dvi.contextmenu.RemoveTablePerspectiveFromViewItem;
import org.caleydo.view.dvi.contextmenu.RenameLabelHolderItem;
import org.caleydo.view.dvi.contextmenu.ShowTablePerspectiveInViewsItemContainer;
import org.caleydo.view.dvi.event.CreateAndRenameTablePerspectiveEvent;
import org.caleydo.view.dvi.node.MultiTablePerspectiveViewNode;
import org.caleydo.view.dvi.node.ViewNode;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class TablePerspectivePickingListener extends APickingListener {

	private GLDataViewIntegrator view;
	private DragAndDropController dragAndDropController;
	private AMultiTablePerspectiveRenderer tablePerspectiveRenderer;

	public TablePerspectivePickingListener(GLDataViewIntegrator view, DragAndDropController dragAndDropController,
			AMultiTablePerspectiveRenderer tablePerspectiveRenderer) {
		this.view = view;
		this.dragAndDropController = dragAndDropController;
		this.tablePerspectiveRenderer = tablePerspectiveRenderer;
	}

	private TablePerspectiveRenderer getTablePerspectiveRenderer(int id) {

		Collection<TablePerspectiveRenderer> dimensionGroupRenderers = tablePerspectiveRenderer
				.getDimensionGroupRenderers();

		for (TablePerspectiveRenderer dimensionGroupRenderer : dimensionGroupRenderers) {
			if (dimensionGroupRenderer.hashCode() == id) {
				return dimensionGroupRenderer;
			}
		}
		return null;
	}

	@Override
	public void clicked(Pick pick) {

		TablePerspectiveRenderer draggedComparisonGroupRenderer = getTablePerspectiveRenderer(pick.getObjectID());
		if (draggedComparisonGroupRenderer == null)
			return;
		//
		// draggedComparisonGroupRenderer
		// .setSelectionType(SelectionType.SELECTION);
		Vec2f point = pick.getPickedPoint();
		dragAndDropController.clearDraggables();
		dragAndDropController.setDraggingProperties(new Vec2f(point.x(), point.y()), "DimensionGroupDrag");
		// dragAndDropController.setDraggingStartPosition(new Point(point.x,
		// point.y));
		dragAndDropController.addDraggable(draggedComparisonGroupRenderer);
		// dragAndDropController.setDraggingMode("DimensionGroupDrag");
		view.setDisplayListDirty();

	}

	@Override
	public void mouseOver(Pick pick) {
		TablePerspectiveRenderer dimensionGroupRenderer = getTablePerspectiveRenderer(pick.getObjectID());
		if (dimensionGroupRenderer == null)
			return;

		dimensionGroupRenderer.setColor(dimensionGroupRenderer.getBorderColor());
		view.setDisplayListDirty();
	}

	@Override
	public void mouseOut(Pick pick) {
		TablePerspectiveRenderer tablePerspectiveRenderer = getTablePerspectiveRenderer(pick.getObjectID());
		if (tablePerspectiveRenderer == null)
			return;

		if (tablePerspectiveRenderer.isActive()) {
			tablePerspectiveRenderer.setColor(tablePerspectiveRenderer.getActiveColor());
		} else {
			tablePerspectiveRenderer.setColor(TablePerspectiveRenderer.INACTIVE_COLOR);
		}

		view.setDisplayListDirty();
	}

	// @Override
	// public void dragged(Pick pick) {
	// String draggingMode = dragAndDropController.getDraggingMode();
	//
	// if (!dragAndDropController.isDragging() &&
	// dragAndDropController.hasDraggables()
	// && draggingMode != null && draggingMode.equals("DimensionGroupDrag")) {
	// dragAndDropController.startDragging();
	// }
	// }

	@Override
	public void rightClicked(Pick pick) {

		TablePerspectiveRenderer renderer = getTablePerspectiveRenderer(pick.getObjectID());
		if (renderer == null)
			return;

		IExtensionRegistry registry = Platform.getExtensionRegistry();

		List<Pair<String, String>> viewTypes = new ArrayList<Pair<String, String>>();

		IConfigurationElement[] viewElements = registry.getConfigurationElementsFor("org.eclipse.ui.views");

		IConfigurationElement[] categoryElements = registry
				.getConfigurationElementsFor("org.caleydo.view.ViewCategory");

		for (IConfigurationElement element : viewElements) {
			try {
				String bundleID = element.getAttribute("id");
				if (bundleID.startsWith("org.caleydo.view.")) {

					for (IConfigurationElement category : categoryElements) {

						if (category.getAttribute("viewID").equals(bundleID)
								&& Boolean.valueOf(category.getAttribute("isDataView"))) {

							int indexOfLastDot = -1;
							for (int i = 0; i < 4; i++) {
								indexOfLastDot = bundleID.indexOf('.', indexOfLastDot + 1);
							}

							bundleID = (indexOfLastDot == -1) ? (bundleID) : (bundleID.substring(0, indexOfLastDot));

							Bundle bundle = Platform.getBundle(bundleID);
							if (bundle != null) {
								bundle.start();
								viewTypes.add(new Pair<String, String>(element.getAttribute("name"), element
										.getAttribute("id")));
							}
						}
					}
				}
			} catch (BundleException e) {
				e.printStackTrace();
			}
		}

		Set<String> validViewIDs = DataDomainManager.get().getAssociationManager()
				.getViewTypesForDataDomain(renderer.getDataDomain().getDataDomainType());

		List<Pair<String, String>> finalViewTypes = new ArrayList<Pair<String, String>>();

		for (String viewID : validViewIDs) {
			for (Pair<String, String> viewType : viewTypes) {
				if (viewID.equals(viewType.getSecond())) {
					finalViewTypes.add(viewType);
				}
			}
		}

		Collections.sort(finalViewTypes, Pair.<String> compareFirst());

		Set<ViewNode> viewNodes = view.getViewNodes();

		List<IMultiTablePerspectiveBasedView> multiTablePerspectiveViews = new ArrayList<IMultiTablePerspectiveBasedView>();

		if (viewNodes != null) {
			for (ViewNode node : viewNodes) {
				if (node.getRepresentedView() instanceof IMultiTablePerspectiveBasedView) {
					multiTablePerspectiveViews.add((IMultiTablePerspectiveBasedView) node.getRepresentedView());
				}
			}
		}

		if (finalViewTypes.size() > 0) {
			if (renderer.hasTablePerspective()) {
				view.getContextMenuCreator().addContextMenuItem(
						new ShowTablePerspectiveInViewsItemContainer(renderer.getTablePerspective(), finalViewTypes));
				view.getContextMenuCreator().addContextMenuItem(
						new AddTablePerspectiveToViewsItemContainer(multiTablePerspectiveViews, renderer
								.getTablePerspective()));
				view.getContextMenuCreator().addContextMenuItem(
						new RenameLabelHolderItem(renderer.getTablePerspective()));

			} else {
				view.getContextMenuCreator().addContextMenuItem(
						new ShowTablePerspectiveInViewsItemContainer(renderer.getCreator(), finalViewTypes));
				view.getContextMenuCreator().addContextMenuItem(
						new AddTablePerspectiveToViewsItemContainer(multiTablePerspectiveViews, renderer.getCreator()));
				CreateAndRenameTablePerspectiveEvent event = new CreateAndRenameTablePerspectiveEvent(
						renderer.getCreator());
				view.getContextMenuCreator().addContextMenuItem(
						new GenericContextMenuItem("Rename Table Perspective", event));
			}
		}

		if (tablePerspectiveRenderer.node instanceof MultiTablePerspectiveViewNode) {
			view.getContextMenuCreator()
					.addContextMenuItem(
							new RemoveTablePerspectiveFromViewItem(
									renderer.getTablePerspective(),
									(IMultiTablePerspectiveBasedView) ((MultiTablePerspectiveViewNode) tablePerspectiveRenderer.node)
											.getRepresentedView()));
		}
	}
}
