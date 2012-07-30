/**
 * 
 */
package org.caleydo.core.io.gui.dataimport;

import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog that allows the creation of ID categories and ID types.
 * 
 * @author Christian Partl
 * 
 */
public class CreateIDTypeDialog extends Dialog {

	/**
	 * Combo box that allows the specification of the {@link EDataType} the
	 * {@link IDType} should be associated with.
	 */
	protected Combo dataTypeCombo;

	/**
	 * Text field where the user is supposed to specify the name of the
	 * {@link IDType} to be created.
	 */
	protected Text typeNameTextField;

	/**
	 * Text field where the user can specify a string that shall be replaced
	 * using regular expressions. This regular expression is applied when
	 * parsing ids of the {@link IDType} created using this dialog.
	 */
	protected Text replacementRegExTextField;

	/**
	 * Text field where the user can specify a string that shall replace the
	 * string specified by {@link #replacementRegExTextField}. This regular
	 * expression is applied when parsing ids of the {@link IDType} created
	 * using this dialog.
	 */
	protected Text replacementStringTextField;

	/**
	 * Text field where the user can specify a regular expression to define a
	 * substring. This regular expression is applied when parsing ids of the
	 * {@link IDType} created using this dialog.
	 */
	protected Text substringRegExTextField;

	/**
	 * Button to specify whether to use regular expressions shall be used to
	 * parse ids for the {@link IDType} created in this dialog.
	 */
	protected Button useRegExButton;

	/**
	 * Text field where the user is supposed to specify the name of the
	 * {@link IDCategory} to be created.
	 */
	protected Text categoryNameTextField;

	protected Label replacementRegExLabel;
	protected Label replacementStringLabel;
	protected Label substringRegExLabel;

	/**
	 * Mediator of this dialog.
	 */
	private CreateIDTypeDialogMediator mediator;

	/**
	 * @param parentShell
	 */
	public CreateIDTypeDialog(Shell parentShell) {
		super(parentShell);
		mediator = new CreateIDTypeDialogMediator(this);
	}

	/**
	 * @param parentShell
	 * @param idCategory
	 *            {@link IDCategory} the created {@link IDType} will be
	 *            restricted to.
	 */
	public CreateIDTypeDialog(Shell parentShell, IDCategory idCategory) {
		super(parentShell);
		mediator = new CreateIDTypeDialogMediator(this, idCategory);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(mediator.isCreateIDCategory() ? "Create ID Class"
				: "Create ID Type");
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite parentComposite = new Composite(parent, SWT.NONE);
		parentComposite.setLayout(new GridLayout(2, false));

		Label categoryIDLabel = new Label(parentComposite, SWT.NONE);
		categoryIDLabel.setText("ID Class name");

		categoryNameTextField = new Text(parentComposite, SWT.BORDER);
		categoryNameTextField.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				mediator.categoryNameTextFieldModified();
			}
		});

		Label dataTypeLabel = new Label(parentComposite, SWT.NONE);
		dataTypeLabel.setText("Data type");

		dataTypeCombo = new Combo(parentComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		dataTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label idTypeLabel = new Label(parentComposite, SWT.NONE);
		idTypeLabel.setText("ID type name");

		typeNameTextField = new Text(parentComposite, SWT.BORDER);
		typeNameTextField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Group regExGroup = new Group(parentComposite, SWT.SHADOW_ETCHED_IN);
		regExGroup.setText("Regular expressions");
		regExGroup.setLayout(new GridLayout(4, false));
		regExGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		Composite useRegExComposite = new Composite(regExGroup, SWT.NONE);
		useRegExComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		useRegExComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 4,
				1));
		useRegExButton = new Button(useRegExComposite, SWT.CHECK);
		useRegExButton.setText("Use regular expressions to convert IDs");

		replacementRegExLabel = new Label(regExGroup, SWT.NONE);
		replacementRegExLabel.setText("Replace");
		replacementRegExLabel
				.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true));

		replacementRegExTextField = new Text(regExGroup, SWT.BORDER);

		GridData replacementTextFieldsGridData = new GridData(SWT.FILL, SWT.FILL, true,
				true);
		replacementTextFieldsGridData.widthHint = 150;
		replacementRegExTextField.setLayoutData(replacementTextFieldsGridData);

		replacementStringLabel = new Label(regExGroup, SWT.NONE);
		replacementStringLabel.setText("with");

		replacementStringTextField = new Text(regExGroup, SWT.BORDER);
		replacementStringTextField.setLayoutData(replacementTextFieldsGridData);

		substringRegExLabel = new Label(regExGroup, SWT.NONE);
		substringRegExLabel.setText("Substring specification");

		substringRegExTextField = new Text(regExGroup, SWT.BORDER);
		substringRegExTextField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 3, 1));

		useRegExButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				mediator.useRegExButtonSelected();
			}
		});

		mediator.guiCreated();

		return parent;
	}

	@Override
	protected void okPressed() {
		if (!mediator.okPressed())
			return;
		super.okPressed();
	}

	/**
	 * @return the idCategory, see {@link #idCategory}
	 */
	public IDCategory getIdCategory() {
		return mediator.getIdCategory();
	}

	/**
	 * @return the idType, see {@link #idType}
	 */
	public IDType getIdType() {
		return mediator.getIdType();
	}
}