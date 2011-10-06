package org.caleydo.core.event.view;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.data.id.IDCategory;
import org.caleydo.core.data.selection.ESelectionCommandType;
import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.event.AEvent;

/**
 * A SelectionCommandEvent holds a {@link SelectionCommand} which is used to signal one of the actions defined
 * in {@link ESelectionCommandType} to a {@link VABasedSelectionManager}. Which particular selection manager
 * the command should be applied to is specified via the additional {@link EIDType}.
 * 
 * @author Werner Puff
 * @author Alexander Lex
 */
@XmlRootElement
@XmlType
public class SelectionCommandEvent
	extends AEvent {

	IDCategory idCategory;

	/** list of selection commands to handle by the receiver */
	SelectionCommand selectionCommand = null;

	public SelectionCommand getSelectionCommand() {
		return selectionCommand;
	}

	public void setSelectionCommand(SelectionCommand selectionCommand) {
		this.selectionCommand = selectionCommand;
	}

	public IDCategory getIdCategory() {
		return idCategory;
	}

	public void tableIDCategory(IDCategory idCategory) {
		this.idCategory = idCategory;
	}

	@Override
	public boolean checkIntegrity() {
		if (idCategory == null)
			throw new NullPointerException("category was null");
		if (selectionCommand == null)
			throw new NullPointerException("selectionCommands was null");
		return true;
	}
}