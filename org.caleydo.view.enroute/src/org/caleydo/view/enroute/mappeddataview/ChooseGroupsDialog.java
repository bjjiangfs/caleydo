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
package org.caleydo.view.enroute.mappeddataview;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.virtualarray.group.Group;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Dialog where the user can specify the ids out of a perspective
 *
 * @author Alexander Lex
 * @author Marc Streit
 *
 */
public class ChooseGroupsDialog extends TitleAreaDialog {

	private List<Perspective> perspectives;
	// private IDType contextIDType;
	private Set<String> selectedItems = new HashSet<>();

	private Composite parent;

	private Table candidateCompoundsTable;

	public ChooseGroupsDialog(Shell parentShell, List<Perspective> perspectives) {
		super(parentShell);
		this.perspectives = perspectives;
		// this.contextIDType = perspective.getIdType();
	}

	@Override
	public void create() {
		super.create();
		setTitle("Select Groups");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		this.parent = parent;

		parent.setLayout(new GridLayout());

		GridData data = new GridData();
		GridLayout layout = new GridLayout(1, true);

		parent.setLayout(layout);

		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		Label descriptionLabel = new Label(parent, SWT.NONE);
		descriptionLabel.setText("Select compound to show.");
		descriptionLabel.setLayoutData(data);

		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		candidateCompoundsTable = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		candidateCompoundsTable.setHeaderVisible(true);
		TableColumn column1 = new TableColumn(candidateCompoundsTable, SWT.CHECK);
		column1.setText("Data vector");

		candidateCompoundsTable.setLayoutData(data);
		candidateCompoundsTable.setSortColumn(column1);
		candidateCompoundsTable.setSortDirection(SWT.UP);
		candidateCompoundsTable.setEnabled(true);

		setTableContent();

		return parent;
	}

	private void setTableContent() {

		for (Perspective perspective : perspectives) {
			for (Group group : perspective.getVirtualArray().getGroupList()) {
				String label = null;
				if (perspective.getVirtualArray().getGroupList().size() <= 1) {
					label = perspective.getLabel();
				} else {
					label = group.getLabel();
				}
				// IDMappingManagerRegistry.get().getIDMappingManager(contextIDType)
				// .getID(contextIDType, contextIDType.getIDCategory().getHumanReadableIDType(), id);

				TableItem item = new TableItem(candidateCompoundsTable, SWT.NONE);
				item.setText(0, label + " " + group.getSize());

				item.setData(label);

			}
		}

		for (TableColumn column : candidateCompoundsTable.getColumns()) {
			column.pack();
		}

		candidateCompoundsTable.pack();
		parent.layout();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {

		for (TableItem item : candidateCompoundsTable.getItems()) {
			if (item.getChecked()) {
				String label = (String) item.getData();
				if (label != null)
					selectedItems.add(label);

			}
		}

		super.okPressed();

	}

	/**
	 * @return the selectedItems, see {@link #selectedItems}
	 */
	public Set<String> getSelectedItems() {
		return selectedItems;
	}
}
