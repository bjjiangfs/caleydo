package org.caleydo.core.parser.xml.sax.handler.command;

import org.caleydo.core.command.CommandType;
import org.caleydo.core.command.ICommand;
import org.caleydo.core.command.queue.ICommandQueue;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.IXmlParserManager;
import org.caleydo.core.parser.parameter.IParameterHandler;
import org.caleydo.core.parser.parameter.ParameterHandler;
import org.caleydo.core.parser.parameter.IParameterHandler.ParameterHandlerType;
import org.caleydo.core.parser.xml.sax.handler.AXmlParserHandler;
import org.caleydo.core.parser.xml.sax.handler.SXmlParserHandler;
import org.caleydo.core.util.exception.CaleydoRuntimeException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Create commands
 * 
 * @see org.caleydo.core.parser.xml.sax.handler.IXmlParserHandler
 * @author Michael Kalkusch
 */
public class CommandSaxHandler
	extends AXmlParserHandler
{

	/* XML Tags */
	private final String sTag_Command = CommandType.TAG_CMD.getXmlKey();

	private final String sTag_CommandQueue = CommandType.TAG_CMD_QUEUE.getXmlKey();

	/* END: XML Tags */

	/**
	 * Since the opening tag is handled by the external handler this fal is set
	 * to true by default.
	 */
	private boolean bCommandBuffer_isActive = false;

	private boolean bCommandQueue_isActive = false;

	protected ICommandQueue commandQueueIter = null;

	/**
	 * <Application > <CommandBuffer> <Cmd /> <Cmd /> </CommandBuffer>
	 * </Application>
	 */
	public CommandSaxHandler(final IGeneralManager generalManager,
			final IXmlParserManager xmlParserManager)
	{

		super(generalManager, xmlParserManager);

		setXmlActivationTag("CommandBuffer");
	}

	/**
	 * Read values of class: iCurrentFrameId
	 * 
	 * @param attrs
	 * @param bIsExternalFrame
	 */
	protected ICommand readCommandData(final Attributes attrs, boolean bIsExternalFrame)
	{

		ICommand lastCommand = null;

		IParameterHandler phAttributes = new ParameterHandler();

		/* create new Frame */
		phAttributes.setValueBySaxAttributes(attrs, CommandType.TAG_PROCESS
				.getXmlKey(), CommandType.TAG_PROCESS.getDefault(),
				ParameterHandlerType.STRING);

		phAttributes.setValueBySaxAttributes(attrs, CommandType.TAG_LABEL
				.getXmlKey(), CommandType.TAG_LABEL.getDefault(),
				ParameterHandlerType.STRING);

		phAttributes.setValueBySaxAttributes(attrs, CommandType.TAG_CMD_ID
				.getXmlKey(), CommandType.TAG_CMD_ID.getDefault(),
				ParameterHandlerType.INT);

		phAttributes.setValueBySaxAttributes(attrs, CommandType.TAG_UNIQUE_ID
				.getXmlKey(), CommandType.TAG_UNIQUE_ID.getDefault(),
				ParameterHandlerType.INT);

		phAttributes.setValueBySaxAttributes(attrs, CommandType.TAG_MEMENTO_ID
				.getXmlKey(), CommandType.TAG_MEMENTO_ID.getDefault(),
				ParameterHandlerType.INT);

		phAttributes.setValueBySaxAttributes(attrs, CommandType.TAG_TYPE
				.getXmlKey(), CommandType.TAG_TYPE.getDefault(),
				ParameterHandlerType.STRING);

		phAttributes.setValueBySaxAttributes(attrs, CommandType.TAG_PARENT
				.getXmlKey(), CommandType.TAG_PARENT.getDefault(),
				ParameterHandlerType.INT);

		phAttributes.setValueBySaxAttributes(attrs, CommandType.TAG_ATTRIBUTE1
				.getXmlKey(), CommandType.TAG_ATTRIBUTE1.getDefault(),
				ParameterHandlerType.STRING);

		phAttributes.setValueBySaxAttributes(attrs, CommandType.TAG_ATTRIBUTE2
				.getXmlKey(), CommandType.TAG_ATTRIBUTE2.getDefault(),
				ParameterHandlerType.STRING);

		phAttributes.setValueBySaxAttributes(attrs, CommandType.TAG_ATTRIBUTE3
				.getXmlKey(), CommandType.TAG_ATTRIBUTE3.getDefault(),
				ParameterHandlerType.STRING);

		phAttributes.setValueBySaxAttributes(attrs, CommandType.TAG_ATTRIBUTE4
				.getXmlKey(), CommandType.TAG_ATTRIBUTE4.getDefault(),
				ParameterHandlerType.STRING);

		phAttributes.setValueBySaxAttributes(attrs, CommandType.TAG_POS_GL_ORIGIN
				.getXmlKey(), CommandType.TAG_POS_GL_ORIGIN.getDefault(),
				ParameterHandlerType.VEC3F);

		phAttributes.setValueBySaxAttributes(attrs,
				CommandType.TAG_POS_GL_ROTATION.getXmlKey(),
				CommandType.TAG_POS_GL_ROTATION.getDefault(),
				ParameterHandlerType.VEC4F);

		phAttributes.setValueBySaxAttributes(attrs, CommandType.TAG_DETAIL
				.getXmlKey(), CommandType.TAG_DETAIL.getDefault(),
				ParameterHandlerType.STRING);

		// generalManager.logMsg(
		// "XML-TAG= " + phAttributes.getValueString(
		// CommandType.TAG_LABEL.getXmlKey() ),
		// LoggerType.FULL );

		lastCommand = generalManager.getCommandManager().createCommand(phAttributes);

		if (lastCommand != null)
		{
			String sData_Cmd_process = phAttributes
					.getValueString(CommandType.TAG_PROCESS.getXmlKey());

			if (sData_Cmd_process.equals(CommandType.RUN_CMD_NOW.toString()))
			{
				// generalManager.logMsg("do command: " +
				// lastCommand.toString(),
				// LoggerType.FULL );
				lastCommand.doCommand();
			}
		}
		else
		{
			// generalManager.logMsg("do command: command=null!",
			// LoggerType.VERBOSE );
		}

		return lastCommand;
	}

	/**
	 * Read values of class: iCurrentFrameId
	 * 
	 * @param attrs
	 * @param bIsExternalFrame
	 */
	protected void readCommandQueueData(final Attributes attrs, boolean bIsExternalFrame)
	{

		ICommand lastCommand = null;

		String sData_Queue_process = CommandType.RUN_QUEUE_ON_DEMAND.toString();
		String sData_Queue_type = CommandType.COMMAND_QUEUE_RUN.toString();

		int iData_Queue_CmdId;
		int iData_Queue_CmdQueueId;
		int iData_Queue_ThreadPool_Id = -1;
		int iData_Queue_ThreadPool_Wait_Id = -1;

		try
		{
			/* create new Frame */
			sData_Queue_process = SXmlParserHandler.assignStringValue(attrs,
					CommandType.RUN_QUEUE_ON_DEMAND.getXmlKey(),
					CommandType.RUN_QUEUE_ON_DEMAND.toString());

			iData_Queue_CmdId = SXmlParserHandler.assignIntValueIfValid(attrs,
					CommandType.CMD_ID.getXmlKey(), -1);

			iData_Queue_CmdQueueId = SXmlParserHandler.assignIntValueIfValid(attrs,
					CommandType.CMDQUEUE_ID.getXmlKey(), -1);

			sData_Queue_type = SXmlParserHandler.assignStringValue(attrs,
					CommandType.COMMAND_QUEUE_RUN.getXmlKey(),
					CommandType.COMMAND_QUEUE_RUN.toString());

			iData_Queue_ThreadPool_Id = SXmlParserHandler.assignIntValueIfValid(attrs,
					CommandType.CMD_THREAD_POOL_ID.getXmlKey(), -1);

			iData_Queue_ThreadPool_Wait_Id = SXmlParserHandler.assignIntValueIfValid(attrs,
					CommandType.CMD_THREAD_POOL_WAIT_ID.getXmlKey(), -1);

			lastCommand = generalManager.getCommandManager().createCommandQueue(
					sData_Queue_type, sData_Queue_process, iData_Queue_CmdId,
					iData_Queue_CmdQueueId, iData_Queue_ThreadPool_Id,
					-iData_Queue_ThreadPool_Wait_Id);

		}
		catch (Exception e)
		{
			System.err
					.println("CommandSaxHandler::readCommandQueueData() ERROR while parsing "
							+ e.toString());
		}

		CommandType currentType = CommandType.valueOf(sData_Queue_type);

		switch (currentType)
		// CommandType
		{

			case COMMAND_QUEUE_RUN:
				if (CommandType.valueOf(sData_Queue_process) == CommandType.RUN_QUEUE)
				{
					lastCommand.doCommand();
					commandQueueIter = null;
				}
				break;

			case COMMAND_QUEUE_OPEN:
				if (commandQueueIter != null)
				{
					assert false : "COMMAND_QUEUE_OPEN: already one queue is beeing processed!";
				}

				commandQueueIter = (ICommandQueue) lastCommand;
				break;

			// no default section!
			// default:
		} // switch (currentType) {

		// throw new CaleydoRuntimeException( "can not create command from [" +
		// attrs.toString() + "]");
	}

	/**
	 * @see org.xml.sax.ContentHandler#startElement(Stringt, Stringt, Stringt,
	 *      org.xml.sax.Attributes)
	 */
	public void startElement(String namespaceURI, String localName, String qName,
			Attributes attrs) throws SAXException
	{

		String eName = ("".equals(localName)) ? qName : localName;

		if (null != eName)
		{

			if (eName.equals(sOpeningTag))
			{
				/* <sFrameStateTag> */
				if (bCommandBuffer_isActive)
				{
					throw new SAXException("<" + sOpeningTag + "> already opened!");
				}
				else
				{
					bCommandBuffer_isActive = true;
					return;
				}

			} // end: if (eName.equals(sFrameStateTag)) {
			else if (eName.equals(sTag_Command))
			{

				if (bCommandBuffer_isActive)
				{
					/**
					 * <CommandBuffer> ... <Cmd ...>
					 */

					if (bCommandQueue_isActive)
					{
						/**
						 * <CommandBuffer> ... <CmdQueue> <br>
						 * ... <Cmd ...>
						 */

						// readCommandQueueData( attrs, true );
						ICommand lastCommand = readCommandData(attrs, true);

						if (lastCommand != null)
						{
							commandQueueIter.addCmdToQueue(lastCommand);
						}
						else
						{
							// generalManager.logMsg(
							// "CommandQueue: no Command to add. skip it.",
							// LoggerType.VERBOSE );
						}
					}
					else
					{
						/**
						 * <CommandBuffer> ... <Cmd ...>
						 */

						// readCommandQueueData( attrs, true );
						ICommand lastCommand = readCommandData(attrs, true);

						if (lastCommand == null)
						{
							// generalManager.logMsg(
							// "Command: can not execute command due to error while parsing. skip it."
							// ,
							// LoggerType.VERBOSE );
						}

					}

				}
				else
				{
					throw new SAXException("<" + sTag_Command + "> opens without <"
							+ sOpeningTag + "> being opened!");
				}
			}
			else if (eName.equals(sTag_Command))
			{

				/**
				 * <CmdQueue ...>
				 */
				if (bCommandBuffer_isActive)
				{

					if (bCommandQueue_isActive)
					{
						throw new SAXException("<" + sTag_CommandQueue + "> opens inside a <"
								+ sTag_CommandQueue + "> block!");
					}

					bCommandQueue_isActive = true;

					readCommandQueueData(attrs, true);

				}
				else
				{
					throw new SAXException("<" + sTag_Command + "> opens without <"
							+ sOpeningTag + "> being opened!");
				}

			}
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#endElement(Stringt, Stringt, Stringt)
	 */
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException
	{

		String eName = ("".equals(localName)) ? qName : localName;

		if (null != eName)
		{
			if (eName.equals(sOpeningTag))
			{

				/* </CommandBuffer> */
				if (bCommandBuffer_isActive)
				{
					bCommandBuffer_isActive = false;

					/**
					 * section (xml block) finished, call callback function from
					 * IXmlParserManager
					 */
					xmlParserManager.sectionFinishedByHandler(this);

					return;
				}
				else
				{
					throw new SAXException("<" + sOpeningTag + "> was already closed.");
				}

			}
			else if (eName.equals(sTag_Command))
			{

				/* </cmd> */
				if (!bCommandBuffer_isActive)
				{
					throw new SAXException("<" + sTag_Command + "> opens without "
							+ sOpeningTag + " being opened.");
				}

			}
			else if (eName.equals(sTag_CommandQueue))
			{

				/**
				 * </CmdQueue ...>
				 */
				if (bCommandBuffer_isActive)
				{

					bCommandQueue_isActive = false;
				}
				else
				{
					throw new SAXException("<" + sTag_CommandQueue + "> opens without "
							+ sOpeningTag + " being opened.");
				}
			}

			// end:else if (eName.equals(...)) {
		} // end: if (null != eName) {

	}

	/**
	 * Cleanup called by Manager after Handler is not used any more.
	 */
	public void destroyHandler()
	{
		super.destroyHandler();
	}
}
