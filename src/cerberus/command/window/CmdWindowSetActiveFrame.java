/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package cerberus.command.window;

import cerberus.manager.IGeneralManager;
import cerberus.manager.IViewCanvasManager;
import cerberus.command.CommandQueueSaxType;
import cerberus.command.ICommand;
import cerberus.command.CommandType;
import cerberus.command.base.ACommand;
//import cerberus.net.dwt.swing.jogl.WorkspaceSwingFrame;
import cerberus.util.exception.CerberusRuntimeException;

/**
 * @author Michael Kalkusch
 *
 */
public class CmdWindowSetActiveFrame 
extends ACommand
implements ICommand {

	private final IGeneralManager refGeneralManager;
	
	private final IViewCanvasManager refViewCanvasManager;
	
	private int iTargetFrameId = -1;
	
	private int iCallerFrameId = -1;

	
	/**
	 * 
	 */
	public CmdWindowSetActiveFrame(final IGeneralManager setRefGeneralManager,
			final String details ) {
		super( -1,
				setRefGeneralManager,
				setRefGeneralManager.getSingelton().getCommandManager(),
				null);
		
		refGeneralManager = setRefGeneralManager;
		
		refViewCanvasManager = refGeneralManager.getSingelton().getViewCanvasManager();
		
		if ( details == null ) {
			return;
		}
		
		int iIndex_seperator = details.indexOf(" ");					
		
		try {
			iCallerFrameId = Integer.valueOf( 
					details.substring( 0, iIndex_seperator-1 ) );
			
			iTargetFrameId =Integer.valueOf( 
					details.substring( iIndex_seperator, details.length()-1 ) );
		} catch (NumberFormatException nfe) {
//			assert false : "CmdWindowSetActiveFrame() can not convert id ["+
//				details + "] from String to int";
		}
		
		this.setCommandQueueSaxType(CommandQueueSaxType.WINDOW_SET_ACTIVE_FRAME);

	}

	public void setCallerAndTargetFrameId( final int iSetCallerFrameId, 
			final int iSetTargetFrameId ) {
		iCallerFrameId = iSetCallerFrameId;
		iTargetFrameId = iSetTargetFrameId;
	}
	
	/* (non-Javadoc)
	 * @see cerberus.command.ICommand#doCommand()
	 */
	public void doCommand() throws CerberusRuntimeException {
		
//		WorkspaceSwingFrame targetFrame = 
//			refViewCanvasManager.getItemWorkspace( iTargetFrameId );
//		
//		WorkspaceSwingFrame callerFrame = 
//			refViewCanvasManager.getItemWorkspace( iCallerFrameId );
//				
//		if (( callerFrame != null ) && (targetFrame != null)) {
//			callerFrame.setTargetFrame( targetFrame );
//		}
//		else {
//			assert false :"doCommand() falied because either callerFrameId or tragetFrameId were not valid";
//		}

	}

	/* (non-Javadoc)
	 * @see cerberus.command.ICommand#undoCommand()
	 */
	public void undoCommand() throws CerberusRuntimeException {
		// TODO Auto-generated method stub

	}

}
