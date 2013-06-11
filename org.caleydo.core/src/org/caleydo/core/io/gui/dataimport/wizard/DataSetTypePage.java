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
package org.caleydo.core.io.gui.dataimport.wizard;

import org.caleydo.core.gui.util.FontUtil;
import org.caleydo.core.io.DataDescriptionUtil;
import org.caleydo.core.io.DataSetDescription;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * @author Christian
 *
 */
public class DataSetTypePage extends AImportDataPage {

	public static final String PAGE_NAME = "Select Dataset Type";

	public static final String PAGE_DESCRIPTION = "Specify the type of the dataset.";

	protected Button numericalDatasetButton;
	protected Button categoricalDatasetButton;
	// protected Button inhomogeneousDatasetButton;

	/**
	 * determines whether a new dataset has been loaded prior visiting this page.
	 */
	protected boolean datasetChanged = true;

	/**
	 * @param pageName
	 * @param dataSetDescription
	 */
	protected DataSetTypePage(DataSetDescription dataSetDescription) {
		super("Data Type Selection", dataSetDescription);
		setDescription(PAGE_DESCRIPTION);
	}

	@Override
	protected void createGuiElements(Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText("Select the data type of your homogeneous dataset.");
		group.setLayout(new GridLayout(1, true));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		numericalDatasetButton = new Button(group, SWT.RADIO);
		numericalDatasetButton.setText("Numerical");
		numericalDatasetButton.setSelection(true);
		FontUtil.makeBold(numericalDatasetButton);
		Label numericalDatasetLabel = new Label(group, SWT.WRAP);
		numericalDatasetLabel
				.setText("Choose if all columns in your dataset are numerical (integer or real numbers) and homogeneous (all columns have the same meaning and bounds).");
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.widthHint = 200;
		numericalDatasetLabel.setLayoutData(gridData);

		categoricalDatasetButton = new Button(group, SWT.RADIO);
		categoricalDatasetButton.setText("Categorical");
		FontUtil.makeBold(categoricalDatasetButton);
		Label categoricalDatasetLabel = new Label(group, SWT.WRAP);
		categoricalDatasetLabel
				.setText("Choose if your dataset is categorical and contains the same categories in all columns.");
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.widthHint = 200;
		categoricalDatasetLabel.setLayoutData(gridData);
	}

	@Override
	public void fillDataSetDescription() {
		if (numericalDatasetButton.getSelection()) {
			if (dataSetDescription.getDataDescription() == null
					|| dataSetDescription.getDataDescription().getNumericalProperties() == null || datasetChanged) {

				dataSetDescription.setDataDescription(DataDescriptionUtil.createNumericalDataDescription(getWizard()
						.getFilteredDataMatrix()));
				// the page needs to init from the newly created data description
				getWizard().getNumericalDataPage().setInitFromDataDescription(true);
			}

		} else if (categoricalDatasetButton.getSelection()) {
			if (dataSetDescription.getDataDescription() == null
					|| dataSetDescription.getDataDescription().getCategoricalClassDescription() == null
					|| datasetChanged) {
				dataSetDescription.setDataDescription(DataDescriptionUtil.createCategoricalDataDescription(getWizard()
						.getFilteredDataMatrix()));
				// the page needs to init from the newly created data description
				getWizard().getCategoricalDataPage().setInitFromDataDescription(true);
			}
		}
		// else {
		// if (datasetChanged || dataSetDescription.getDataDescription() != null) {
		// getWizard().getInhomogeneousDataPropertiesPage().setInitColumnDescriptions(true);
		// // No global data description for inhomogeneous
		// dataSetDescription.setDataDescription(null);
		// }
		// }
		datasetChanged = false;
	}

	// private void updateWidgets() {
	// if (datasetChanged) {
	// if (isNumericalDataset()) {
	// numericalDatasetButton.setSelection(true);
	// categoricalDatasetButton.setSelection(false);
	// } else {
	// numericalDatasetButton.setSelection(false);
	// categoricalDatasetButton.setSelection(true);
	// }
	//
	// }
	// }

	// private boolean isNumericalDataset() {
	// int numFloatsParsed = 0;
	// int valuesConsidered = 0;
	// for (List<String> row : getWizard().getFilteredDataMatrix()) {
	// for (String value : row) {
	// try {
	// valuesConsidered++;
	// Float.parseFloat(value);
	// numFloatsParsed++;
	// // at least half of the dataset must consist of readable floats
	// if (numFloatsParsed >= row.size() * getWizard().getFilteredDataMatrix().size() * 0.5f) {
	// return true;
	// }
	// } catch (NumberFormatException e) {
	// continue;
	// }
	// if (valuesConsidered > row.size() * getWizard().getFilteredDataMatrix().size() * 0.5f) {
	// return false;
	// }
	// }
	// }
	// return false;
	// }

	@Override
	public void pageActivated() {
		// The user must always visit the next page before he can finish
		// updateWidgets();
		getWizard().setChosenDataTypePage(null);
		getWizard().getContainer().updateButtons();

	}

	@Override
	public IWizardPage getNextPage() {
		DataImportWizard wizard = getWizard();
		if (numericalDatasetButton.getSelection()) {
			return wizard.getNumericalDataPage();
		}
		return wizard.getCategoricalDataPage();
	}

	/**
	 * @param datasetChanged
	 *            setter, see {@link datasetChanged}
	 */
	public void setDatasetChanged(boolean datasetChanged) {
		this.datasetChanged = datasetChanged;
	}

	/**
	 * @return the datasetChanged, see {@link #datasetChanged}
	 */
	public boolean isDatasetChanged() {
		return datasetChanged;
	}
}
