/**
 * 
 */
package cerberus.command.view.opengl;

import cerberus.command.CommandQueueSaxType;
import cerberus.manager.ICommandManager;
import cerberus.manager.IGeneralManager;
import cerberus.parser.parameter.IParameterHandler;
import cerberus.util.exception.GeneViewRuntimeException;
import cerberus.view.opengl.canvas.parcoords.GLCanvasParCoords;

/**
 * @author Michael Kalkusch
 * @author Marc Streit
 *
 */
public class CmdGlObjectParCoords 
extends ACmdGLObjectPathway3D {

	//protected ArrayList<Integer> iArSetIDs;
		
	/**
	 * Constructor.
	 * 
	 */
	public CmdGlObjectParCoords(
			final IGeneralManager refGeneralManager,
			final ICommandManager refCommandManager,
			final CommandQueueSaxType refCommandQueueSaxType)
	{
		super(refGeneralManager, 
				refCommandManager,
				refCommandQueueSaxType);
				
		//iArSetIDs = new ArrayList<Integer>();

		localManagerObjectType = CommandQueueSaxType.CREATE_GL_PARALLEL_COORDINATES;
	}

	public void setParameterHandler( final IParameterHandler refParameterHandler ) {
	
		super.setParameterHandler(refParameterHandler);
	}

	@Override
	public void doCommandPart() throws GeneViewRuntimeException {
		
		GLCanvasParCoords canvas = 
			(GLCanvasParCoords) openGLCanvasUser;		
		
		canvas.setOriginRotation(cameraOrigin, cameraRotation);
	}

	@Override
	public void undoCommandPart() throws GeneViewRuntimeException {

		GLCanvasParCoords canvas = 
			(GLCanvasParCoords) openGLCanvasUser;
		
		canvas.destroyGLCanvas();
		canvas = null;
	}
}
