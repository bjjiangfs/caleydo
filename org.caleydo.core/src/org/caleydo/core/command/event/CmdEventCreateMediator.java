package org.caleydo.core.command.event;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.caleydo.core.command.CommandQueueSaxType;
import org.caleydo.core.command.base.ACmdCreate_IdTargetLabelAttrDetail;
import org.caleydo.core.manager.ICommandManager;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.IEventPublisher.MediatorType;
import org.caleydo.core.manager.event.mediator.MediatorUpdateType;
import org.caleydo.core.manager.type.ManagerObjectType;
import org.caleydo.core.util.exception.CaleydoRuntimeException;
import org.caleydo.core.util.system.StringConversionTool;
import org.caleydo.core.manager.IEventPublisher;
import org.caleydo.core.parser.parameter.IParameterHandler;

/**
 * Class creates a mediator, extracts the sender and receiver IDs
 * and calls the methods that handle the registration.
 * 
 * @author Marc Streit
 * @author Michael Kalkusch
 */
public class CmdEventCreateMediator 
extends ACmdCreate_IdTargetLabelAttrDetail {
	
	protected ArrayList<Integer> iArSenderIDs;

	protected ArrayList<Integer> iArReceiverIDs;
	
	protected MediatorType mediatorType;
	
	public CmdEventCreateMediator(
			final IGeneralManager refGeneralManager,
			final ICommandManager refCommandManager,
			final CommandQueueSaxType refCommandQueueSaxType) {

		super(refGeneralManager,
				refCommandManager,
				refCommandQueueSaxType);
		
		super.setId( refGeneralManager.getSingleton().getEventPublisher().createId( 
				ManagerObjectType.EVENT_MEDIATOR_CREATE));
		
		iArSenderIDs = new ArrayList<Integer>();
		iArReceiverIDs = new ArrayList<Integer>();
	}

	public void doCommand() throws CaleydoRuntimeException {
			
		((IEventPublisher)generalManager.
				getManagerByBaseType(ManagerObjectType.EVENT_PUBLISHER)).
					createMediator(iUniqueId,
							iArSenderIDs, 
							iArReceiverIDs, 
							mediatorType,
							MediatorUpdateType.MEDIATOR_DEFAULT);
		
		refCommandManager.runDoCommand(this);
	}
	
	public void setParameterHandler( final IParameterHandler refParameterHandler ) {
		
		assert refParameterHandler != null: "ParameterHandler object is null!";	
		
		super.setParameterHandler(refParameterHandler);	

		StringTokenizer senderToken = new StringTokenizer(
				sAttribute1,
				IGeneralManager.sDelimiter_Parser_DataItems);

		StringTokenizer receiverToken = new StringTokenizer(
				sAttribute2,
				IGeneralManager.sDelimiter_Parser_DataItems);

		while (senderToken.hasMoreTokens())
		{
			iArSenderIDs.add(StringConversionTool.convertStringToInt(
					senderToken.nextToken(), -1));
		}
		
		while (receiverToken.hasMoreTokens())
		{
			iArReceiverIDs.add(StringConversionTool.convertStringToInt(
					receiverToken.nextToken(), -1));
		}
		
		String sMediatorType = refParameterHandler.getValueString( 
				CommandQueueSaxType.TAG_DETAIL.getXmlKey());
		
		System.out.println("CmdEventCreateMediator.setParameterHandler() TYPE= [" + sMediatorType + "]");
		
		if ( sMediatorType.length() > 0 ) 
		{
			mediatorType = MediatorType.valueOf( sMediatorType );
		}
		else {
			/* assume DATA_MEDIATOR as default */
			mediatorType = MediatorType.DATA_MEDIATOR;
		}
	}

	public void setAttributes(int iEventMediatorId,
			ArrayList<Integer> iArSenderIDs,
			ArrayList<Integer> iArReceiverIDs, 
			MediatorType mediatorType) {
		
		this.iUniqueId = iEventMediatorId;
		this.iArSenderIDs = iArSenderIDs;
		this.iArReceiverIDs = iArReceiverIDs;
		this.mediatorType = mediatorType;
	}
	
	public void undoCommand() throws CaleydoRuntimeException {
		
		refCommandManager.runUndoCommand(this);		
	}
	
	public String getInfoText() {
		return super.getInfoText() + " -> " + this.iUniqueId + ": " + this.sLabel;
	}
}
