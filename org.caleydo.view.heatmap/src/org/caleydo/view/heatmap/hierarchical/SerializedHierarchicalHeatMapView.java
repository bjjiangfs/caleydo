package org.caleydo.view.heatmap.hierarchical;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.serialize.ASerializedTopLevelDataView;
import org.caleydo.core.view.opengl.camera.CameraProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;

/**
 * Serialized form of the {@link GLHierarchicalHeatMap} view.
 * 
 * @author Werner Puff
 */
@XmlRootElement
@XmlType
public class SerializedHierarchicalHeatMapView extends ASerializedTopLevelDataView {

	public SerializedHierarchicalHeatMapView() {
	}

	public SerializedHierarchicalHeatMapView(String dataDomainType) {
		super(dataDomainType);
	}

	@Override
	public ViewFrustum getViewFrustum() {
		ViewFrustum viewFrustum = new ViewFrustum(CameraProjectionMode.ORTHOGRAPHIC, 0,
				8, 0, 8, -20, 20);
		return viewFrustum;
	}

	@Override
	public String getViewType() {
		return GLHierarchicalHeatMap.VIEW_TYPE;
	}
}
