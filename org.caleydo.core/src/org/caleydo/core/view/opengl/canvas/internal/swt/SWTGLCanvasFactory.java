/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.core.view.opengl.canvas.internal.swt;


import java.util.List;

import javax.media.opengl.GLCapabilitiesImmutable;

import org.caleydo.core.view.contextmenu.AContextMenuItem;
import org.caleydo.core.view.contextmenu.item.SeparatorMenuItem;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.internal.IGLCanvasFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.jogamp.opengl.swt.GLCanvas;

/**
 * @author Samuel Gratzl
 *
 */
public class SWTGLCanvasFactory implements IGLCanvasFactory {

	@Override
	public SWTGLCanvas create(GLCapabilitiesImmutable caps, Composite parent) {
		GLCanvas canvas = new GLCanvas(parent, SWT.NO_BACKGROUND, caps, null, null);
		return new SWTGLCanvas(canvas);
	}

	@Override
	public void showPopupMenu(final AGLView view, final Iterable<AContextMenuItem> items) {
		final Composite parent = view.getParentComposite();
		parent.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				Menu m = new Menu(parent);
				for (AContextMenuItem menuItem : items) {
					create(m, menuItem);
				}
				m.setLocation(parent.getDisplay().getCursorLocation());
				m.setVisible(true);
			}
		});

	}

	private void create(Menu parent, final AContextMenuItem item) {
		if (item instanceof SeparatorMenuItem) {
			new MenuItem(parent, SWT.SEPARATOR);
			return;
		}

		List<AContextMenuItem> subItems = item.getSubMenuItems();
		if (!subItems.isEmpty()) {
			MenuItem menuItem = new MenuItem(parent, SWT.CASCADE);
			menuItem.setText(item.getLabel());

			Menu submenu = new Menu(parent);
			for (AContextMenuItem subMenuItem : subItems) {
				create(submenu, subMenuItem);
			}
			menuItem.setMenu(submenu);
		} else {
			MenuItem menuItem = new MenuItem(parent, SWT.PUSH);
			menuItem.setText(item.getLabel());
			menuItem.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					item.triggerEvent();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub

				}
			});
		}
	}
}