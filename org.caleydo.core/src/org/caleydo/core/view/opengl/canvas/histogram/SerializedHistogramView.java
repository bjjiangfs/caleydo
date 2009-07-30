package org.caleydo.core.view.opengl.canvas.histogram;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.command.ECommandType;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.opengl.camera.ViewFrustum;

/**
 * Serialized form of the remote-rendering view (bucket). 
 * @author Werner Puff
 */
@XmlRootElement
@XmlType
public class SerializedHistogramView 
	extends ASerializedView {

	@Override
	public ECommandType getCreationCommandType() {
		return ECommandType.CREATE_GL_HISTOGRAM;
	}

	@Override
	public ViewFrustum getViewFrustum() {
		return null;
	}

}
