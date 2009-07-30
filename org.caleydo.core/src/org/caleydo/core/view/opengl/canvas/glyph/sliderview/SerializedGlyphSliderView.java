package org.caleydo.core.view.opengl.canvas.glyph.sliderview;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.command.ECommandType;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.opengl.camera.EProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;

/**
 * Serialized form of a heatmap-view. 
 * @author Werner Puff
 */
@XmlRootElement
@XmlType
public class SerializedGlyphSliderView 
	extends ASerializedView {
	
	/**
	 * Default constructor with default initialization
	 */
	public SerializedGlyphSliderView() {

	}

	@Override
	public ECommandType getCreationCommandType() {
		return ECommandType.CREATE_GL_GLYPH_SLIDER;
	}

	@Override
	public ViewFrustum getViewFrustum() {
		ViewFrustum viewFrustum = new ViewFrustum(EProjectionMode.ORTHOGRAPHIC, 0, 8, 0, 8, -20, 20);
		return viewFrustum;
	}

}
