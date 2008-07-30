package org.caleydo.core.manager.command;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import org.caleydo.core.command.CommandQueueSaxType;
import org.caleydo.core.command.ICommand;
import org.caleydo.core.command.ICommandListener;
import org.caleydo.core.command.base.ACommand;
import org.caleydo.core.command.queue.ICommandQueue;
import org.caleydo.core.manager.AManager;
import org.caleydo.core.manager.ICommandManager;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.command.factory.CommandFactory;
import org.caleydo.core.manager.command.factory.ICommandFactory;
import org.caleydo.core.manager.type.EManagerObjectType;
import org.caleydo.core.manager.type.EManagerType;
import org.caleydo.core.parser.parameter.IParameterHandler;
import org.caleydo.core.view.swt.undoredo.UndoRedoViewRep;

/**
 * Manager for creating and exporting commands.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 */
public class CommandManager
	extends AManager
	implements ICommandManager
{

	private ICommandFactory commandFactory;

	/**
	 * List of all Commands to be executed as soon as possible
	 */
	private Vector<ICommand> vecCmdHandle;

	/**
	 * List of All Commands to be executed when sooner or later.
	 */
	private Vector<ICommand> vecCmdSchedule;

	protected Hashtable<Integer, ICommandQueue> hashCommandQueueId;

	protected Hashtable<Integer, ICommand> hashCommandId;

	protected Vector<ICommand> vecUndo;

	protected Vector<ICommand> vecRedo;

	protected ArrayList<UndoRedoViewRep> arUndoRedoViews;

	private int iCountRedoCommand = 0;

	/**
	 * Constructor.
	 */
	public CommandManager(IGeneralManager setGeneralManager)
	{

		super(setGeneralManager, IGeneralManager.iUniqueId_TypeOffset_Command,
				EManagerType.COMMAND);

		commandFactory = new CommandFactory(setGeneralManager, this);

		vecCmdHandle = new Vector<ICommand>();

		vecCmdSchedule = new Vector<ICommand>();

		hashCommandQueueId = new Hashtable<Integer, ICommandQueue>();

		hashCommandId = new Hashtable<Integer, ICommand>();

		vecUndo = new Vector<ICommand>(100);

		vecRedo = new Vector<ICommand>(100);

		arUndoRedoViews = new ArrayList<UndoRedoViewRep>();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.data.manager.CommandManager#addCommandListener(org.caleydo
	 * .core.command.ICommandListener)
	 */
	public void addCommandListener(ICommandListener addCommandListener)
	{

		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.data.manager.CommandManager#removeCommandListener(org
	 * .caleydo.core.command.ICommandListener)
	 */
	public boolean removeCommandListener(ICommandListener removeCommandListener)
	{

		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.data.manager.CommandManager#hasCommandListener(org.caleydo
	 * .core.command.ICommandListener)
	 */
	public boolean hasCommandListener(ICommandListener hasCommandListener)
	{

		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.command.ICommandActionListener#handleCommand(org.caleydo
	 * .core.command.ICommand)
	 */
	public void handleCommand(ICommand addCommand)
	{

		addCommand.doCommand();
		vecCmdHandle.addElement(addCommand);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.command.ICommandActionListener#scheduleCommand(org.caleydo
	 * .core.command.ICommand)
	 */
	public void scheduleCommand(ICommand addCommand)
	{

		vecCmdSchedule.addElement(addCommand);
		addCommand.doCommand();

	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.data.manager.GeneralManager#hasItem(int)
	 */
	public boolean hasItem(int iItemId)
	{

		return hashCommandId.containsKey(iItemId);
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.data.manager.GeneralManager#getItem(int)
	 */
	public Object getItem(int iItemId)
	{

		return hashCommandId.get(iItemId);
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.data.manager.GeneralManager#size()
	 */
	public int size()
	{

		return hashCommandId.size();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.data.manager.GeneralManager#registerItem(java.lang.Object
	 * , int, org.caleydo.core.data.manager.BaseManagerType)
	 */
	public boolean registerItem(Object registerItem, int iItemId)
	{

		ICommand registerCommand = (ICommand) registerItem;

		if (registerCommand.getClass().equals(ICommandQueue.class))
		{
			hashCommandQueueId.put(iItemId, (ICommandQueue) registerItem);
		}
		else
		{

		}

		vecCmdHandle.addElement(registerCommand);
		hashCommandId.put(iItemId, registerCommand);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.data.manager.GeneralManager#unregisterItem(int,
	 * org.caleydo.core.data.manager.BaseManagerType)
	 */
	public boolean unregisterItem(int iItemId)
	{

		// if ( type == ManagerObjectType.CMD_QUEUE ) {
		// hashCommandQueueId.remove( iItemId );
		// }
		if (hashCommandId.containsKey(iItemId))
		{

			ICommand unregisterCommand = hashCommandId.remove(iItemId);

			return vecCmdHandle.remove(unregisterCommand);
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.manager.ICommandManager#createCommandByType(org.caleydo
	 * .core.command.CommandQueueSaxType)
	 */
	public ICommand createCommandByType(final CommandQueueSaxType cmdType)
	{

		ICommand createdCommand = commandFactory.createCommandByType(cmdType);

		// BUG! creating command is not executing command!
		// //FIXME: should iterate over all undo/redo views.
		// if ( ! arUndoRedoViews.isEmpty() )
		// {
		// arUndoRedoViews.get(0).addCommand(createdCommand);
		// }

		return createdCommand;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.manager.ICommandManager#createCommand(org.caleydo.core
	 * .parser.parameter.IParameterHandler)
	 */
	public ICommand createCommand(final IParameterHandler phAttributes)
	{

		CommandQueueSaxType cmdType = CommandQueueSaxType.valueOf(phAttributes
				.getValueString(CommandQueueSaxType.TAG_TYPE.getXmlKey()));

		ICommand createdCommand = createCommandByType(cmdType);

		if (phAttributes != null)
		{
			createdCommand.setParameterHandler(phAttributes);
		}

		// BUG! creating command is not executing command!
		// //FIXME: should iterate over all undo/redo views.
		// if (arUndoRedoViews.isEmpty() == false)
		// {
		// arUndoRedoViews.get(0).addCommand(createdCommand);
		// }

		return createdCommand;
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.manager.ICommandManager#hasCommandQueueId(int)
	 */
	public boolean hasCommandQueueId(final int iCmdQueueId)
	{

		return hashCommandQueueId.containsKey(iCmdQueueId);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.manager.ICommandManager#getCommandQueueByCmdQueueId(int)
	 */
	public ICommandQueue getCommandQueueByCmdQueueId(final int iCmdQueueId)
	{

		return hashCommandQueueId.get(iCmdQueueId);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.manager.ICommandManager#createCommandQueue(java.lang
	 * .String, java.lang.String, int, int, int, int)
	 */
	public ICommand createCommandQueue(final String sCmdType, final String sProcessType,
			final int iCmdId, final int iCmdQueueId, final int sQueueThread,
			final int sQueueThreadWait)
	{

		ICommand newCmd = commandFactory.createCommandQueue(sCmdType, sProcessType, iCmdId,
				iCmdQueueId, sQueueThread, sQueueThreadWait);

		int iNewCmdId = createId(EManagerObjectType.COMMAND);
		newCmd.setId(iNewCmdId);

		registerItem(newCmd, iNewCmdId);

		return newCmd;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.manager.ICommandManager#runDoCommand(org.caleydo.core
	 * .command.ICommand)
	 */
	public synchronized void runDoCommand(ICommand runCmd)
	{

		vecUndo.addElement(runCmd);

		if (iCountRedoCommand > 0)
		{
			vecRedo.remove(runCmd);
			iCountRedoCommand--;
		}

		// FIXME: think of multiple tread support! current Version is not thread
		// safe!
		Iterator<UndoRedoViewRep> iter = arUndoRedoViews.iterator();

		assert iter != null : "arUndoRedoViews was not inizalized! Iterator ist null-pointer";

		while (iter.hasNext())
		{
			iter.next().addCommand(runCmd);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.manager.ICommandManager#runUndoCommand(org.caleydo.core
	 * .command.ICommand)
	 */
	public synchronized void runUndoCommand(ICommand runCmd)
	{

		iCountRedoCommand++;
		vecUndo.remove(runCmd);

		vecRedo.addElement(runCmd);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.manager.ICommandManager#addUndoRedoViewRep(org.caleydo
	 * .core.view.swt.undoredo.UndoRedoViewRep)
	 */
	public void addUndoRedoViewRep(UndoRedoViewRep undoRedoViewRep)
	{

		arUndoRedoViews.add(undoRedoViewRep);
		arUndoRedoViews.get(0).updateCommandList(vecUndo);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.manager.ICommandManager#writeSerializedObjects(java.
	 * lang.String)
	 */
	public void writeSerializedObjects(final String sFileName)
	{

		try
		{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(sFileName));

			// First write number of command objects
			out.writeInt(vecUndo.size());

			// Iterate over commands
			for (ICommand tmpCmd : vecUndo)
			{
				out.writeObject(tmpCmd);

				generalManager.getLogger().log(Level.INFO,
						"Serialize command: [" + tmpCmd.getInfoText() + "]");
			}

			out.close();

		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.caleydo.core.manager.ICommandManager#readSerializedObjects(java.lang
	 * .String)
	 */
	public void readSerializedObjects(final String sFileName)
	{

		try
		{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(sFileName));

			int iCmdCount = in.readInt();
			for (int iCmdIndex = 0; iCmdIndex < iCmdCount; iCmdIndex++)
			{
				// vecCmdHandle.add((ICommand)in.readObject());
				ACommand tmpCmd = ((ACommand) in.readObject());
				tmpCmd.setGeneralManager(generalManager);
				tmpCmd.doCommand();
			}

			in.close();

		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
