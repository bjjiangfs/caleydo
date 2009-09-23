package org.caleydo.core.view.opengl.canvas.remote;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.command.ECommandType;
import org.caleydo.core.manager.IUseCase;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.specialized.genetic.GeneticUseCase;
import org.caleydo.core.manager.usecase.EDataDomain;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.glyph.gridview.SerializedGlyphView;
import org.caleydo.core.view.opengl.canvas.storagebased.SerializedHeatMapView;
import org.caleydo.core.view.opengl.canvas.storagebased.SerializedParallelCoordinatesView;
import org.eclipse.core.runtime.Status;

/**
 * Serialized form of the remote-rendering view (bucket).
 * 
 * @author Werner Puff
 */
@XmlRootElement
@XmlType
public class SerializedRemoteRenderingView
	extends ASerializedView {

	public static final String GUI_ID = "org.caleydo.rcp.views.opengl.GLRemoteRenderingView";

	/** @see org.caleydo.core.view.opengl.canvas.remote.GLRemoteRendering.pathwayTexturesEnabled */
	private boolean pathwayTexturesEnabled;

	/** @see org.caleydo.core.view.opengl.canvas.remote.GLRemoteRendering.geneMappingEnabled */
	private boolean geneMappingEnabled;

	/** @see org.caleydo.core.view.opengl.canvas.remote.GLRemoteRendering.neighborhoodEnabled */
	private boolean neighborhoodEnabled;

	/** @see org.caleydo.core.view.opengl.canvas.remote.GLRemoteRendering.connectionLinesEnabled */
	private boolean connectionLinesEnabled;

	/** list of view-ids contained in the focus-level */
	private List<ASerializedView> focusViews;

	/** list of view-ids contained in the stack-level */
	private List<ASerializedView> stackViews;

	/**
	 * No-Arg Constructor to create a serialized bucket-view with default parameters.
	 */
	public SerializedRemoteRenderingView() {
		this(EDataDomain.GENETIC_DATA);
	}

	public SerializedRemoteRenderingView(EDataDomain dataDomain) {
		super(dataDomain);
		setPathwayTexturesEnabled(true);
		setNeighborhoodEnabled(true);
		setGeneMappingEnabled(true);
		setConnectionLinesEnabled(true);

		ArrayList<ASerializedView> remoteViews = new ArrayList<ASerializedView>();

		// FIXME: This works only for genetic data - we need to ask the view about it's domain
		IUseCase usecase = GeneralManager.get().getUseCase(dataDomain);
		if (usecase != null && usecase instanceof GeneticUseCase
			&& !((GeneticUseCase) usecase).isPathwayViewerMode()) {

			// FIXME: This is just a temporary solution to check if glyph view
			// should be added to bucket.
			try {
				GeneralManager.get().getIDManager().getInternalFromExternalID(453010);
				SerializedGlyphView glyph1 = new SerializedGlyphView(EDataDomain.CLINICAL_DATA);
				remoteViews.add(glyph1);
				SerializedGlyphView glyph2 = new SerializedGlyphView(EDataDomain.CLINICAL_DATA);
				remoteViews.add(glyph2);
			}
			catch (IllegalArgumentException e) {
				GeneralManager.get().getLogger().log(
					new Status(Status.WARNING, GeneralManager.PLUGIN_ID,
						"Cannot add glyph to bucket! No glyph data loaded!"));
			}

			SerializedHeatMapView heatMap = new SerializedHeatMapView(dataDomain);
			remoteViews.add(heatMap);
			SerializedParallelCoordinatesView parCoords = new SerializedParallelCoordinatesView(dataDomain);
			remoteViews.add(parCoords);
		}

		ArrayList<ASerializedView> focusLevel = new ArrayList<ASerializedView>();
		if (remoteViews.size() > 0) {
			focusLevel.add(remoteViews.remove(0));
		}
		setFocusViews(focusLevel);
		setStackViews(remoteViews);
	}

	@Override
	public ECommandType getCreationCommandType() {
		return ECommandType.CREATE_GL_BUCKET_3D;
	}

	@Override
	public ViewFrustum getViewFrustum() {
		return null;
	}

	public boolean isPathwayTexturesEnabled() {
		return pathwayTexturesEnabled;
	}

	public void setPathwayTexturesEnabled(boolean pathwayTexturesEnabled) {
		this.pathwayTexturesEnabled = pathwayTexturesEnabled;
	}

	public boolean isGeneMappingEnabled() {
		return geneMappingEnabled;
	}

	public void setGeneMappingEnabled(boolean geneMappingEnabled) {
		this.geneMappingEnabled = geneMappingEnabled;
	}

	public boolean isNeighborhoodEnabled() {
		return neighborhoodEnabled;
	}

	public void setNeighborhoodEnabled(boolean neighborhoodEnabled) {
		this.neighborhoodEnabled = neighborhoodEnabled;
	}

	public boolean isConnectionLinesEnabled() {
		return connectionLinesEnabled;
	}

	public void setConnectionLinesEnabled(boolean connectionLinesEnabled) {
		this.connectionLinesEnabled = connectionLinesEnabled;
	}

	@XmlElementWrapper
	public List<ASerializedView> getFocusViews() {
		return focusViews;
	}

	public void setFocusViews(List<ASerializedView> focusViews) {
		this.focusViews = focusViews;
	}

	@XmlElementWrapper
	public List<ASerializedView> getStackViews() {
		return stackViews;
	}

	public void setStackViews(List<ASerializedView> stackViews) {
		this.stackViews = stackViews;
	}

	@Override
	public String getViewGUIID() {
		return GUI_ID;
	}

	@Override
	public EDataDomain getDataDomain() {
		return dataDomain;
	}
}
