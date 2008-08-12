package org.caleydo.core.command.data;

import java.util.logging.Level;
import org.caleydo.core.command.CommandType;
import org.caleydo.core.command.base.ACmdCreational;
import org.caleydo.core.command.base.ACmdExternalAttributes;
import org.caleydo.core.data.collection.IStorage;
import org.caleydo.core.manager.data.IStorageManager;
import org.caleydo.core.manager.id.EManagedObjectType;
import org.caleydo.core.parser.parameter.IParameterHandler;
import org.caleydo.core.util.exception.CaleydoRuntimeException;
import org.caleydo.core.util.exception.CaleydoRuntimeExceptionType;

/**
 * Command creates a new storage.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 * @author Alexander Lex
 */
public class CmdDataCreateStorage
	extends ACmdCreational<IStorage>
{
	private EManagedObjectType storageType;

	/**
	 * Constructor.
	 */
	public CmdDataCreateStorage(final CommandType cmdType)
	{
		super(cmdType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.command.ICommand#doCommand()
	 */
	public void doCommand() throws CaleydoRuntimeException
	{
		IStorageManager storageManager = generalManager.getStorageManager();
		createdObject = (IStorage) storageManager.createStorage(storageType);
		createdObject.setLabel(sLabel);

		generalManager.getIDManager().mapInternalToExternalID(createdObject.getID(), iExternalID);
		
		generalManager.getLogger().log(Level.INFO, "Created Storage with ID: " + createdObject.getID());
		commandManager.runDoCommand(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.command.ICommand#undoCommand()
	 */
	public void undoCommand() throws CaleydoRuntimeException
	{
		commandManager.runUndoCommand(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.command.base.ACmdCreate_IdTargetLabel#setParameterHandler(org.caleydo.core.parser.parameter.IParameterHandler)
	 */
	public void setParameterHandler(final IParameterHandler parameterHandler)
	{
		super.setParameterHandler(parameterHandler);

		if (sAttribute1.length() > 0)
		{
			if (sAttribute1.equalsIgnoreCase("NOMINAL"))
				storageType = EManagedObjectType.STORAGE_NOMINAL;
			else if (sAttribute1.equalsIgnoreCase("NUMERICAL"))
				storageType = EManagedObjectType.STORAGE_NUMERICAL;
			else
				throw new CaleydoRuntimeException(
						"attrib1 of CREATE_STORAGE must be either NUMERICAL or NOMINAL, but was neither",
						CaleydoRuntimeExceptionType.COMMAND);
		}

	}

	public void setAttributes(EManagedObjectType stroageType)
	{
		this.storageType = stroageType;
	}
}
