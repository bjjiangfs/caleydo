/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.vis.rank.internal.ui;


import org.caleydo.core.event.EventPublisher;
import org.caleydo.vis.rank.internal.event.FilterEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 *
 *
 * @author Samuel Gratzl
 *
 */
public class FloatFilterDialog extends AFilterDialog {

	private Button filterNotMappedUI;
	private Button filterMissingUI;

	private final boolean filterMissing;
	private final boolean filterNotMapped;


	public FloatFilterDialog(Shell parentShell, String title, Object receiver, boolean filterNotMapped,
			boolean filterMissing, boolean filterGlobally, boolean hasSnapshots, Point loc) {
		super(parentShell, "Filter " + title, receiver, filterGlobally, hasSnapshots, loc);
		this.filterMissing = filterMissing;
		this.filterNotMapped = filterNotMapped;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Filter Settings for " + title);
	}

	@Override
	protected void createSpecificFilterUI(Composite composite) {
		filterNotMappedUI = new Button(composite, SWT.CHECK);
		filterNotMappedUI.setText("Filter not mapped entries?");
		filterNotMappedUI.setLayoutData(twoColumns(new GridData(SWT.LEFT, SWT.CENTER, true, true)));
		filterNotMappedUI.setSelection(filterNotMapped);

		filterMissingUI = new Button(composite, SWT.CHECK);
		filterMissingUI.setText("Filter missing value entries?");
		filterMissingUI.setLayoutData(twoColumns(new GridData(SWT.LEFT, SWT.CENTER, true, true)));
		filterMissingUI.setSelection(filterMissing);

		Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		SelectionAdapter adapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				triggerEvent(false);
			}
		};
		filterNotMappedUI.addSelectionListener(adapter);
		filterMissingUI.addSelectionListener(adapter);

		createApplyGlobally(composite);
		addOKButton(composite, true);
	}

	@Override
	protected void triggerEvent(boolean cancel) {
		boolean a, b;
		if (cancel) {
			a = filterNotMapped;
			b = filterMissing;
		} else {
			a = filterNotMappedUI.getSelection();
			b = filterMissingUI.getSelection();
		}
		EventPublisher.trigger(new FilterEvent(new FilterChecked(a, b, isFilterGlobally())).to(receiver));
	}

	public static class FilterChecked {
		private boolean filterNotMapped;
		private boolean filterMissing;
		private boolean globalFiltering;

		public FilterChecked(boolean filterNotMapped, boolean filterMissing, boolean globalFiltering) {
			this.filterNotMapped = filterNotMapped;
			this.filterMissing = filterMissing;
			this.globalFiltering = globalFiltering;
		}

		/**
		 * @return the filterMissing, see {@link #filterMissing}
		 */
		public boolean isFilterMissing() {
			return filterMissing;
		}

		/**
		 * @return the filterNotMapped, see {@link #filterNotMapped}
		 */
		public boolean isFilterNotMapped() {
			return filterNotMapped;
		}

		/**
		 * @return the globalFiltering, see {@link #globalFiltering}
		 */
		public boolean isGlobalFiltering() {
			return globalFiltering;
		}
	}
}

