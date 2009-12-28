package org.caleydo.core.view.opengl.canvas.grouper;

import javax.media.opengl.GL;

import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.PickingManager;

import com.sun.opengl.util.j2d.TextRenderer;

public class VAElementDrawingStrategyMouseOver
	extends AVAElementDrawingStrategyRectangular {

	private PickingManager pickingManager;
	private int iViewID;
	
	public VAElementDrawingStrategyMouseOver(PickingManager pickingManager, int iViewID) {
		this.pickingManager = pickingManager;
		this.iViewID = iViewID;
	}
	
	@Override
	public void draw(GL gl, VAElementRepresentation elementRepresentation, TextRenderer textRenderer) {
		
		
		gl.glPushName(pickingManager.getPickingID(iViewID, EPickingType.GROUPER_VA_ELEMENT_SELECTION,
			elementRepresentation.getID()));
		gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_CURRENT_BIT | GL.GL_LINE_BIT);

		gl.glColor4fv(GrouperRenderStyle.TEXT_BG_COLOR, 0);

		drawElementRectangular(gl, elementRepresentation, textRenderer);
		
		gl.glLineWidth(3.0f);
		gl.glColor4fv(GrouperRenderStyle.MOUSE_OVER_COLOR, 0);
		
		drawRectangularBorder(gl, elementRepresentation, textRenderer);
		
		gl.glPopAttrib();

		gl.glPopName();

	}

}
