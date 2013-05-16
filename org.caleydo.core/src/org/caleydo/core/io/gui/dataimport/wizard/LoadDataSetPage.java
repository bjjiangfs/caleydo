/**
 *
 */
package org.caleydo.core.io.gui.dataimport.wizard;

import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.io.DataSetDescription;
import org.caleydo.core.io.IDTypeParsingRules;
import org.caleydo.core.io.gui.dataimport.widget.BooleanCallback;
import org.caleydo.core.io.gui.dataimport.widget.DelimiterWidget;
import org.caleydo.core.io.gui.dataimport.widget.ICallback;
import org.caleydo.core.io.gui.dataimport.widget.LabelWidget;
import org.caleydo.core.io.gui.dataimport.widget.LoadFileWidget;
import org.caleydo.core.io.gui.dataimport.widget.PreviewTableWidget;
import org.caleydo.core.io.gui.dataimport.widget.SelectAllNoneWidget;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

/**
 * Page for loading the dataset from file.
 *
 * @author Christian Partl
 *
 */
public class LoadDataSetPage extends AImportDataPage implements Listener {

	public static final String PAGE_NAME = "Load Dataset";

	public static final String PAGE_DESCRIPTION = "Specify the dataset you want to load.";

	/**
	 * Button to specify whether the dataset is homogeneous, i.e. all columns have the same scale.
	 */
	protected Button buttonHomogeneous;

	/**
	 * Combo box to specify the {@link IDCategory} for the columns of the dataset.
	 */
	protected Combo columnIDCategoryCombo;
	/**
	 * Combo box to specify the {@link IDCategory} for the rows of the dataset.
	 */
	protected Combo rowIDCategoryCombo;

	/**
	 * Composite that is the parent of all gui elements of this dialog.
	 */
	protected Composite parentComposite;

	/**
	 * Combo box to specify the row ID Type.
	 */
	protected Combo rowIDCombo;

	/**
	 * Combo box to specify the column ID Type.
	 */
	protected Combo columnIDCombo;

	/**
	 * Manager for {@link #previewTable} that extends its features.
	 */
	protected PreviewTableWidget previewTable;

	/**
	 * Spinner used to define the index of the row that contains the column ids.
	 */
	protected Spinner rowOfColumnIDSpinner;

	/**
	 * Spinner used to define the index of the column that contains the row ids.
	 */
	protected Spinner columnOfRowIDSpinner;

	/**
	 * Spinner used to define the index of the row from where on data is contained.
	 */
	protected Spinner numHeaderRowsSpinner;

	/**
	 * Spinner used to define the index of the column from where on data is contained.
	 */
	protected Spinner dataStartColumnSpinner;

	/**
	 * Button to create a new {@link IDCategory}.
	 */
	protected Button createRowIDCategoryButton;

	/**
	 * Button to create a new {@link IDCategory}.
	 */
	protected Button createColumnIDCategoryButton;

	/**
	 * Button to create a new {@link IDType}.
	 */
	protected Button createRowIDTypeButton;

	/**
	 * Button to create a new {@link IDType}.
	 */
	protected Button createColumnIDTypeButton;

	/**
	 * Button that opens a dialog to define the {@link IDTypeParsingRules} for the row id type.
	 */
	protected Button defineRowIDParsingButton;

	/**
	 * Button that opens a dialog to define the {@link IDTypeParsingRules} for the column id type.
	 */
	protected Button defineColumnIDParsingButton;

	/**
	 * Group of widgets for file delimiter specification.
	 */
	protected DelimiterWidget delimiterRadioGroup;

	protected SelectAllNoneWidget selectAllNone;

	protected LoadFileWidget loadFile;

	protected LabelWidget label;

	/**
	 * Mediator for this page.
	 */
	private LoadDataSetPageMediator mediator;

	public LoadDataSetPage(DataSetDescription dataSetDescription) {
		super(PAGE_NAME, dataSetDescription);
		setDescription(PAGE_DESCRIPTION);
		mediator = new LoadDataSetPageMediator(this, dataSetDescription);
	}

	@Override
	public void createControl(Composite parent) {

		int numGridCols = 2;

		parentComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(numGridCols, true);
		parentComposite.setLayout(layout);

		// File Selection
		loadFile = new LoadFileWidget(parentComposite, "Open Data File", new ICallback<String>() {
			@Override
			public void on(String data) {
				mediator.onSelectFile(data);
			}
		});

		// label
		label = new LabelWidget(parentComposite, "Dataset Name");

		// Row Config
		createRowConfigPart(parentComposite);

		// Column Config
		createColumnConfigPart(parentComposite);

		// Delimiters
		delimiterRadioGroup = new DelimiterWidget(parentComposite, new ICallback<String>() {
			@Override
			public void on(String data) {
				mediator.onDelimiterChanged(data);
			}
		});

		selectAllNone = new SelectAllNoneWidget(parentComposite, new BooleanCallback() {
			@Override
			public void on(boolean selectAll) {
				mediator.onSelectAllNone(selectAll);
			}
		});

		previewTable = new PreviewTableWidget(parentComposite);
		// , new BooleanCallback() {
		// @Override
		// public void on(boolean showAllColumns) {
		// mediator.onShowAllColumns(showAllColumns);
		// }
		// });

		mediator.guiCreated();

		setControl(parentComposite);
	}

	private void createRowConfigPart(Composite parent) {

		Group rowConfigGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		rowConfigGroup.setText("Row Configuration");
		rowConfigGroup.setLayout(new GridLayout(2, false));
		rowConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite leftConfigGroupPart = new Composite(rowConfigGroup, SWT.NONE);
		leftConfigGroupPart.setLayout(new GridLayout(2, false));
		leftConfigGroupPart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createIDCategoryGroup(leftConfigGroupPart, "Row ID Class", false);
		createIDTypeGroup(leftConfigGroupPart, false);

		Label startParseAtLineLabel = new Label(leftConfigGroupPart, SWT.NONE);
		startParseAtLineLabel.setText("Number of Header Rows");

		numHeaderRowsSpinner = new Spinner(leftConfigGroupPart, SWT.BORDER);
		numHeaderRowsSpinner.setMinimum(1);
		numHeaderRowsSpinner.setMaximum(Integer.MAX_VALUE);
		numHeaderRowsSpinner.setIncrement(1);
		GridData gridData = new GridData(SWT.LEFT, SWT.FILL, false, true);
		gridData.widthHint = 70;
		numHeaderRowsSpinner.setLayoutData(gridData);
		numHeaderRowsSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				mediator.numHeaderRowsSpinnerModified();
			}
		});

		Label columnOfRowIDlabel = new Label(leftConfigGroupPart, SWT.NONE);
		columnOfRowIDlabel.setText("Column with Row IDs");

		columnOfRowIDSpinner = new Spinner(leftConfigGroupPart, SWT.BORDER);
		columnOfRowIDSpinner.setMinimum(1);
		columnOfRowIDSpinner.setMaximum(Integer.MAX_VALUE);
		columnOfRowIDSpinner.setIncrement(1);
		gridData = new GridData(SWT.LEFT, SWT.FILL, false, true);
		gridData.widthHint = 70;
		columnOfRowIDSpinner.setLayoutData(gridData);
		columnOfRowIDSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				mediator.columnOfRowIDSpinnerModified();
			}
		});

		Composite rightConfigGroupPart = new Composite(rowConfigGroup, SWT.NONE);
		rightConfigGroupPart.setLayout(new GridLayout(2, false));
		rightConfigGroupPart.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));

		createRowIDCategoryButton = createNewIDCategoryButton(rightConfigGroupPart);
		createRowIDCategoryButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				mediator.createRowIDCategoryButtonSelected();
			}

		});
		createRowIDTypeButton = createNewIDTypeButton(rightConfigGroupPart);
		createRowIDTypeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				mediator.createRowIDTypeButtonSelected();
			}
		});

		defineRowIDParsingButton = new Button(rightConfigGroupPart, SWT.PUSH);
		defineRowIDParsingButton.setText("Define Parsing");
		defineRowIDParsingButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				mediator.onDefineRowIDParsing();
			}
		});
	}

	private Button createNewIDCategoryButton(Composite parent) {
		Button createIDCategoryButton = new Button(parent, SWT.PUSH);
		createIDCategoryButton.setText("New");
		createIDCategoryButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));

		return createIDCategoryButton;
	}

	private Button createNewIDTypeButton(Composite parent) {
		Button createIDTypeButton = new Button(parent, SWT.PUSH);
		createIDTypeButton.setText("New");

		return createIDTypeButton;
	}

	private void createColumnConfigPart(Composite parent) {

		Group columnConfigGroup = new Group(parent, SWT.NONE);
		columnConfigGroup.setText("Column Configuration");
		columnConfigGroup.setLayout(new GridLayout(2, false));
		columnConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite leftConfigGroupPart = new Composite(columnConfigGroup, SWT.NONE);
		leftConfigGroupPart.setLayout(new GridLayout(2, false));
		leftConfigGroupPart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createIDCategoryGroup(leftConfigGroupPart, "Column ID Class", true);
		createIDTypeGroup(leftConfigGroupPart, true);

		Label rowOfColumnIDLabel = new Label(leftConfigGroupPart, SWT.NONE);
		rowOfColumnIDLabel.setText("Row with Column IDs");

		rowOfColumnIDSpinner = new Spinner(leftConfigGroupPart, SWT.BORDER);
		rowOfColumnIDSpinner.setMinimum(1);
		rowOfColumnIDSpinner.setMaximum(Integer.MAX_VALUE);
		rowOfColumnIDSpinner.setIncrement(1);
		GridData gridData = new GridData(SWT.LEFT, SWT.FILL, false, true);
		gridData.widthHint = 70;
		rowOfColumnIDSpinner.setLayoutData(gridData);
		rowOfColumnIDSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				mediator.rowOfColumnIDSpinnerModified();
			}
		});

		createDataPropertiesGroup(leftConfigGroupPart);

		Composite rightConfigGroupPart = new Composite(columnConfigGroup, SWT.NONE);
		rightConfigGroupPart.setLayout(new GridLayout(2, false));
		rightConfigGroupPart.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));

		createColumnIDCategoryButton = createNewIDCategoryButton(rightConfigGroupPart);
		createColumnIDCategoryButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				mediator.createColumnIDCategoryButtonSelected();
			}
		});
		createColumnIDTypeButton = createNewIDTypeButton(rightConfigGroupPart);
		createColumnIDTypeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				mediator.createColumnIDTypeButtonSelected();
			}
		});

		defineColumnIDParsingButton = new Button(rightConfigGroupPart, SWT.PUSH);
		defineColumnIDParsingButton.setText("Define Parsing");
		defineColumnIDParsingButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				mediator.onDefineColumnIDParsing();
			}
		});
	}

	private void createIDTypeGroup(Composite parent, final boolean isColumnIDTypeGroup) {
		Label idTypeLabel = new Label(parent, SWT.SHADOW_ETCHED_IN);
		idTypeLabel.setText(isColumnIDTypeGroup ? "Column ID Type" : "Row ID Type");
		idTypeLabel.setLayoutData(new GridData(SWT.LEFT));
		Combo idCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		idCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		if (isColumnIDTypeGroup) {
			columnIDCombo = idCombo;
		} else {
			rowIDCombo = idCombo;
		}

		idCombo.addListener(SWT.Modify, this);
		idCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				mediator.idTypeComboModified(isColumnIDTypeGroup);
			}
		});
	}

	private void createIDCategoryGroup(Composite parent, String groupLabel, final boolean isColumnCategory) {
		Label recordIDCategoryGroup = new Label(parent, SWT.SHADOW_ETCHED_IN);
		recordIDCategoryGroup.setText(groupLabel);
		recordIDCategoryGroup.setLayoutData(new GridData(SWT.LEFT));
		Combo idCategoryCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		idCategoryCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		idCategoryCombo.setText("<Please Select>");

		if (isColumnCategory) {
			columnIDCategoryCombo = idCategoryCombo;
		} else {
			rowIDCategoryCombo = idCategoryCombo;
		}

		idCategoryCombo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				mediator.idCategoryComboModified(isColumnCategory);
			}
		});
	}

	private void createDataPropertiesGroup(Composite parent) {

		buttonHomogeneous = new Button(parent, SWT.CHECK);
		buttonHomogeneous.setText("Columns use same Scale");
		buttonHomogeneous.setEnabled(true);
		buttonHomogeneous.setSelection(true);
	}

	/**
	 * Reads the min and max values (if set) from the dialog
	 */
	@Override
	public void fillDataSetDescription() {

		mediator.fillDataSetDescription();
	}

	public DataSetDescription getDataSetDescription() {
		return mediator.getDataSetDescription();
	}

	@Override
	public boolean isPageComplete() {
		if (loadFile.getFileName().isEmpty()) {
			((DataImportWizard) getWizard()).setRequiredDataSpecified(false);
			return false;
		}

		if (rowIDCombo.getSelectionIndex() == -1) {
			((DataImportWizard) getWizard()).setRequiredDataSpecified(false);
			return false;
		}

		if (columnIDCombo.getSelectionIndex() == -1) {
			((DataImportWizard) getWizard()).setRequiredDataSpecified(false);
			return false;
		}
		((DataImportWizard) getWizard()).setRequiredDataSpecified(true);

		return super.isPageComplete();
	}

	@Override
	public IWizardPage getNextPage() {

		return super.getNextPage();
	}

	@Override
	public void handleEvent(Event event) {
		if (getWizard().getContainer().getCurrentPage() != null)
			getWizard().getContainer().updateButtons();
	}

	@Override
	public void pageActivated() {
	}

}
