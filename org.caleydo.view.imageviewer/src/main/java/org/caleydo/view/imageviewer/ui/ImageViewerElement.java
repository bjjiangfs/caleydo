/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.imageviewer.ui;

import java.util.Map.Entry;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLImageElement;
import org.caleydo.core.view.opengl.layout2.GLImageViewer;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.datadomain.image.LayeredImage;
import org.caleydo.datadomain.image.LayeredImage.Image;
import org.caleydo.view.imageviewer.internal.ImageViewerViewPart.SelectImageEvent;

/**
 * element of this view holding a {@link TablePerspective}
 *
 * @author Thomas Geymayer
 *
 */
public class ImageViewerElement extends GLImageViewer {

	protected APickingListener layerPickListener;

	public ImageViewerElement() {
		layerPickListener = new APickingListener() {
			@Override
			protected void mouseOver(Pick pick) {
				((GLImageElement) pick.getObject()).setColor(Color.RED);
			}

			@Override
			protected void mouseOut(Pick pick) {
				((GLImageElement) pick.getObject()).setColor(Color.WHITE);
			}
		};
	}

	public void setImage(LayeredImage img) {
		clear();
		setBaseImage(img.getBaseImage().image.getPath());
		System.out.println("URL = "
				+ img.getConfig().getProperty("URL", "http://caleydo.org"));

		for (Entry<String, LayeredImage.Layer> layer : img.getLayers().entrySet()) {
			Image highlight = layer.getValue().border;
			Image mask = layer.getValue().area;

			String highlightPath = "", maskPath = "";

			if (highlight == null) {
				if (mask == null)
					continue;
				highlightPath = mask.image.getPath();
			} else {
				highlightPath = highlight.image.getPath();

				if (mask != null)
					maskPath = mask.image.getPath();
			}

			addLayer(highlightPath, maskPath).onPick(layerPickListener);
		}

		elementStack.scaleToFit();
	}

	@ListenTo(sendToMe = true)
	public void onSelectImage(SelectImageEvent e) {
		setImage(e.image);
	}

}
