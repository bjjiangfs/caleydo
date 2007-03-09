package cerberus.command.view.swt;

import cerberus.command.CommandQueueSaxType;
import cerberus.command.ICommand;
import cerberus.command.base.ACmdCreate_IdTargetLabelParentXY;
import cerberus.manager.ICommandManager;
import cerberus.manager.IGeneralManager;
import cerberus.manager.IViewManager;
import cerberus.manager.type.ManagerObjectType;
import cerberus.util.exception.CerberusRuntimeException;
import cerberus.view.gui.swt.data.exchanger.NewSetEditorViewRep;
//import cerberus.view.gui.swt.data.exchanger.SetEditorViewRep;
import cerberus.xml.parser.parameter.IParameterHandler;

/**
 * Class implementes the command for 
 * the data exchanger view.
 * 
 * @author Michael Kalkusch
 *
 */
public class CmdViewCreateSetEditor 
extends ACmdCreate_IdTargetLabelParentXY 
implements ICommand {
	
	/**
	 * Constructor.
	 * 
	 */
	public CmdViewCreateSetEditor(
			final IGeneralManager refGeneralManager,
			final ICommandManager refCommandManager,
			final CommandQueueSaxType refCommandQueueSaxType) {
		
		super(refGeneralManager, 
				refCommandManager,
				refCommandQueueSaxType);
	}

	/**
	 * Method creates a data exchanger view, sets the attributes 
	 * and calls the init and draw method.
	 */
	public void doCommand() throws CerberusRuntimeException {
		
		IViewManager viewManager = ((IViewManager) refGeneralManager
				.getManagerByBaseType(ManagerObjectType.VIEW));
		
		NewSetEditorViewRep setEditorView = (NewSetEditorViewRep)viewManager
				.createView(ManagerObjectType.VIEW_SWT_DATA_SET_EDITOR,
							iUniqueTargetId, 
							iParentContainerId, 
							sLabel);
		
		viewManager.registerItem(
				setEditorView, 
				iUniqueTargetId, 
				ManagerObjectType.VIEW);

		viewManager.addViewRep(setEditorView);

		setEditorView.setAttributes(iWidthX, iHeightY);
		setEditorView.retrieveGUIContainer();
		setEditorView.initView();
		setEditorView.drawView();
		
		refCommandManager.runDoCommand(this);
	}

	public void setParameterHandler( final IParameterHandler refParameterHandler ) {
		
		assert refParameterHandler != null: "ParameterHandler object is null!";	
		
		super.setParameterHandler(refParameterHandler);	
	}
	
	public void undoCommand() throws CerberusRuntimeException {

		refCommandManager.runUndoCommand(this);
	}
}
