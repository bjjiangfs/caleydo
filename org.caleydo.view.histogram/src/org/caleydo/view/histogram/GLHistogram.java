package org.caleydo.view.histogram;

import static org.caleydo.view.histogram.HistogramRenderStyle.SIDE_SPACING;

import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;

import javax.management.InvalidAttributeValueException;
import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;

import org.caleydo.core.data.collection.Histogram;
import org.caleydo.core.data.collection.table.DataTableDataType;
import org.caleydo.core.data.id.IDType;
import org.caleydo.core.data.selection.ElementConnectionInformation;
import org.caleydo.core.event.view.tablebased.RedrawViewEvent;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.format.Formatter;
import org.caleydo.core.util.mapping.color.ColorMarkerPoint;
import org.caleydo.core.util.mapping.color.UpdateColorMappingEvent;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.ATableBasedView;
import org.caleydo.core.view.opengl.canvas.DetailLevel;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.core.view.opengl.picking.PickingType;
import org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle;
import org.caleydo.core.view.opengl.util.GLCoordinateUtils;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;
import org.eclipse.swt.widgets.Composite;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Rendering the histogram.
 * 
 * @author Alexander Lex
 */
public class GLHistogram extends ATableBasedView {

	public final static String VIEW_TYPE = "org.caleydo.view.histogram";

	private boolean bUseDetailLevel = true;

	private boolean useColor = true;

	private boolean renderColorBars = true;

	private Histogram histogram;
	// private HistogramRenderStyle renderStyle;

	private boolean bUpdateColorPointPosition = false;
	private boolean bUpdateLeftSpread = false;
	private boolean bUpdateRightSpread = false;
	private boolean bIsFirstTimeUpdateColor = false;
	private float fColorPointPositionOffset = 0.0f;
	private int iColorMappingPointMoved = -1;

	private static float[] SPREAD_LINE_COLOR = { 0.5f, 0.5f, 0.5f };

	float fRenderWidth;

	private float sideSpacing = SIDE_SPACING;

	/**
	 * Constructor.
	 * 
	 * @param glCanvas
	 * @param viewLabel
	 * @param viewFrustum
	 */
	public GLHistogram(GLCanvas glCanvas, Composite parentComposite,
			ViewFrustum viewFrustum) {

		super(glCanvas, parentComposite, viewFrustum);

		viewType = VIEW_TYPE;
		viewLabel = "Histogram";

		renderStyle = new HistogramRenderStyle(this, viewFrustum);
		textRenderer = new CaleydoTextRenderer(18);
		// registerEventListeners();

		detailLevel = DetailLevel.HIGH;
	}

	public void setRenderColorBars(boolean renderColorBars) {
		this.renderColorBars = renderColorBars;
	}

	@Override
	public void init(GL2 gl) {
		displayListIndex = gl.glGenLists(1);

	}

	@Override
	public void initLocal(GL2 gl) {
		init(gl);
	}

	@Override
	public void initRemote(final GL2 gl, final AGLView glParentView,
			final GLMouseListener glMouseListener) {
		this.glMouseListener = glMouseListener;
		init(gl);
	}

	@Override
	public void initData() {
		super.initData();
		if (histogram == null) {
			histogram = dataContainer.getContainerStatistics().getHistogram();
		}
	}

	public void setHistogram(Histogram histogram) {
		this.histogram = histogram;
	}

	@Override
	public void setDetailLevel(DetailLevel detailLevel) {
		super.setDetailLevel(detailLevel);
		if (bUseDetailLevel) {
			// renderStyle.setDetailLevel(detailLevel);
			if (detailLevel == DetailLevel.LOW) {
				sideSpacing = 0;
			} else {
				sideSpacing = SIDE_SPACING;
			}
		}
	}

	@Override
	public void displayLocal(GL2 gl) {
		if (!lazyMode)
			pickingManager.handlePicking(this, gl);
		display(gl);
	}

	@Override
	public void displayRemote(GL2 gl) {
		display(gl);

	}

	@Override
	public void display(GL2 gl) {
		if (bUpdateColorPointPosition || bUpdateLeftSpread || bUpdateRightSpread)
			updateColorPointPosition(gl);

		if (isDisplayListDirty) {
			buildDisplayList(gl, displayListIndex);
			isDisplayListDirty = false;
		}

		gl.glCallList(displayListIndex);

		if (!lazyMode)
			checkForHits(gl);
	}

	private void buildDisplayList(final GL2 gl, int iGLDisplayListIndex) {
		gl.glNewList(iGLDisplayListIndex, GL2.GL_COMPILE);
		renderHistogram(gl);
		if (renderColorBars && detailLevel != DetailLevel.LOW)
			renderColorBars(gl);
		gl.glEndList();
	}

	/**
	 * Render the histogram itself
	 * 
	 * @param gl
	 */
	private void renderHistogram(GL2 gl) {

		float fSpacing = (viewFrustum.getWidth() - 2 * sideSpacing) / histogram.size();
		float fContinuousColorRegion = 1.0f / histogram.size();

		float fOneHeightValue = (viewFrustum.getHeight() - 2 * sideSpacing)
				/ histogram.getLargestValue();

		int iCount = 0;

		for (Integer iValue : histogram) {

			if (useColor)
				gl.glColor3fv(
						dataDomain.getColorMapper().getColor(
								fContinuousColorRegion * iCount), 0);

			gl.glLineWidth(3.0f);
			gl.glBegin(GL2.GL_POLYGON);

			gl.glVertex3f(fSpacing * iCount + sideSpacing, sideSpacing, 0);
			gl.glVertex3f(fSpacing * iCount + sideSpacing, sideSpacing + iValue
					* fOneHeightValue, 0);
			// gl.glColor3fv(colorMapping.getColor(fContinuousColorRegion *
			// (iCount + 1)), 0);
			gl.glVertex3f(fSpacing * (iCount + 1) + sideSpacing, sideSpacing + iValue
					* fOneHeightValue, 0);
			gl.glVertex3f(fSpacing * (iCount + 1) + sideSpacing, sideSpacing, 0);
			gl.glEnd();

			gl.glBegin(GL2.GL_LINE);
			gl.glVertex3f(0, 0, 0);
			gl.glVertex3f(2, 2, 2);
			gl.glEnd();

			iCount++;
		}

	}

	/**
	 * Render the color bars for selecting the color mapping
	 * 
	 * @param gl
	 */
	private void renderColorBars(GL2 gl) {

		fRenderWidth = (viewFrustum.getWidth() - 2 * sideSpacing);
		ArrayList<ColorMarkerPoint> markerPoints = dataDomain.getColorMapper()
				.getMarkerPoints();

		int iCount = 0;

		for (ColorMarkerPoint markerPoint : markerPoints) {
			int iColorLinePickingID = pickingManager.getPickingID(uniqueID,
					PickingType.HISTOGRAM_COLOR_LINE, iCount);

			boolean bIsFirstOrLast = false;
			float fPickingScaling = 0.8f;
			if (iCount == 0 || iCount == markerPoints.size() - 1)
				bIsFirstOrLast = true;

			if (markerPoint.hasLeftSpread()) {

				float fLeftSpread = markerPoint.getLeftSpread();
				int iLeftSpreadPickingID = pickingManager.getPickingID(uniqueID,
						PickingType.HISTOGRAM_LEFT_SPREAD_COLOR_LINE, iCount);

				// the left polygon between the central line and the spread
				gl.glColor4f(markerPoint.getColor()[0], markerPoint.getColor()[1],
						markerPoint.getColor()[2], 0.3f);

				float fLeft = sideSpacing + (markerPoint.getMappingValue() - fLeftSpread)
						* fRenderWidth;
				float fRight = sideSpacing + markerPoint.getMappingValue() * fRenderWidth;

				// the right part which picks the central line
				if (!bIsFirstOrLast)
					gl.glPushName(iColorLinePickingID);
				gl.glBegin(GL2.GL_POLYGON);
				gl.glVertex3f(fRight + fPickingScaling * (fLeft - fRight), sideSpacing,
						-0.1f);
				gl.glVertex3f(fRight + fPickingScaling * (fLeft - fRight),
						viewFrustum.getHeight() - sideSpacing, -0.1f);
				gl.glVertex3f(fRight, viewFrustum.getHeight() - sideSpacing, -0.1f);
				gl.glVertex3f(fRight, sideSpacing, -0.001f);
				gl.glEnd();
				if (!bIsFirstOrLast)
					gl.glPopName();

				// the left part which picks the spread
				gl.glPushName(iLeftSpreadPickingID);
				gl.glBegin(GL2.GL_POLYGON);
				gl.glVertex3f(fLeft, sideSpacing, -0.1f);
				gl.glVertex3f(fLeft, viewFrustum.getHeight() - sideSpacing, -0.1f);
				gl.glVertex3f(fRight + fPickingScaling * (fLeft - fRight),
						viewFrustum.getHeight() - sideSpacing, -0.1f);
				gl.glVertex3f(fRight + fPickingScaling * (fLeft - fRight), sideSpacing,
						-0.001f);
				gl.glEnd();
				gl.glPopName();

				// the left spread line
				gl.glColor3fv(SPREAD_LINE_COLOR, 0);
				gl.glPushName(iLeftSpreadPickingID);
				gl.glBegin(GL2.GL_LINES);
				gl.glVertex3f(sideSpacing + (markerPoint.getMappingValue() - fLeftSpread)
						* fRenderWidth, 0, 0);
				gl.glVertex3f(sideSpacing + (markerPoint.getMappingValue() - fLeftSpread)
						* fRenderWidth, viewFrustum.getHeight(), 0);
				gl.glEnd();
				gl.glPopName();
				if (fLeftSpread > HistogramRenderStyle.SPREAD_CAPTION_THRESHOLD)
					renderCaption(gl, markerPoint.getMappingValue() - fLeftSpread);

			}

			if (markerPoint.hasRightSpread()) {
				float fRightSpread = markerPoint.getRightSpread();

				float fLeft = sideSpacing + markerPoint.getMappingValue() * fRenderWidth;
				float fRight = sideSpacing
						+ (markerPoint.getMappingValue() + fRightSpread) * fRenderWidth;

				int iRightSpreadPickingID = pickingManager.getPickingID(uniqueID,
						PickingType.HISTOGRAM_RIGHT_SPREAD_COLOR_LINE, iCount);

				// the polygon between the central line and the right spread
				// the first part which picks the central line
				gl.glColor4f(markerPoint.getColor()[0], markerPoint.getColor()[1],
						markerPoint.getColor()[2], 0.3f);
				if (!bIsFirstOrLast)
					gl.glPushName(iColorLinePickingID);
				gl.glBegin(GL2.GL_POLYGON);
				gl.glVertex3f(fLeft, sideSpacing, -0.011f);
				gl.glVertex3f(fLeft, viewFrustum.getHeight() - sideSpacing, -0.1f);
				gl.glVertex3f(fLeft + fPickingScaling * (fRight - fLeft),
						viewFrustum.getHeight() - sideSpacing, -0.1f);
				gl.glVertex3f(fLeft + fPickingScaling * (fRight - fLeft), sideSpacing,
						-0.1f);
				gl.glEnd();
				if (!bIsFirstOrLast)
					gl.glPopName();

				// the second part which picks the spread
				gl.glPushName(iRightSpreadPickingID);
				gl.glBegin(GL2.GL_POLYGON);
				gl.glVertex3f(fLeft + fPickingScaling * (fRight - fLeft), sideSpacing,
						-0.011f);
				gl.glVertex3f(fLeft + fPickingScaling * (fRight - fLeft),
						viewFrustum.getHeight() - sideSpacing, -0.1f);
				gl.glVertex3f(fRight, viewFrustum.getHeight() - sideSpacing, -0.1f);
				gl.glVertex3f(fRight, sideSpacing, -0.1f);
				gl.glEnd();
				gl.glPopName();

				// the right spread line
				gl.glColor3fv(SPREAD_LINE_COLOR, 0);
				gl.glPushName(iRightSpreadPickingID);
				gl.glBegin(GL2.GL_LINES);
				gl.glVertex3f(sideSpacing
						+ (markerPoint.getMappingValue() + fRightSpread) * fRenderWidth,
						0, 0);
				gl.glVertex3f(sideSpacing
						+ (markerPoint.getMappingValue() + fRightSpread) * fRenderWidth,
						viewFrustum.getHeight(), 0);
				gl.glEnd();
				gl.glPopName();
				if (fRightSpread > HistogramRenderStyle.SPREAD_CAPTION_THRESHOLD)
					renderCaption(gl, markerPoint.getMappingValue() + fRightSpread);

			}

			// the central line
			// gl.glColor3f(0, 0, 1);
			// if (!bIsFirstOrLast)
			// gl.glPushName(iColorLinePickingID);
			// gl.glBegin(GL2.GL_LINES);
			// gl.glVertex3f(SIDE_SPACING + markerPoint.getValue() *
			// fRenderWidth,
			// 0, 0);
			// gl.glVertex3f(SIDE_SPACING + markerPoint.getValue() *
			// fRenderWidth,
			// viewFrustum.getHeight(), 0);
			// gl.glEnd();
			// if (!bIsFirstOrLast)
			// gl.glPopName();

			renderCaption(gl, markerPoint.getMappingValue());

			iCount++;
		}

	}

	private void renderCaption(GL2 gl, float normalizedValue) {

		if (getParentGLCanvas().getSize().getWidth() < 500
				|| dataDomain.getTable().getTableType() != DataTableDataType.NUMERIC)
			return;

		textRenderer.begin3DRendering();
		textRenderer.setColor(0, 0, 0, 1);
		gl.glDisable(GL2.GL_DEPTH_TEST);

		double correspondingValue = dataDomain.getTable().getRawForNormalized(
				normalizedValue);

		String text = Formatter.formatNumber(correspondingValue);

		textRenderer.draw3D(text, sideSpacing + normalizedValue * fRenderWidth
				+ HistogramRenderStyle.CAPTION_SPACING,
				HistogramRenderStyle.CAPTION_SPACING, 0.001f,
				GeneralRenderStyle.HEADING_FONT_SCALING_FACTOR);
		// textRenderer.flush();
		textRenderer.end3DRendering();
	}

	/**
	 * React on drag operations of the color lines and areas
	 * 
	 * @param gl
	 */
	private void updateColorPointPosition(GL2 gl) {
		if (glMouseListener.wasMouseReleased()) {
			// send out a major update which tells the hhm to update its
			// textures
			UpdateColorMappingEvent event = new UpdateColorMappingEvent();
			event.setSender(this);
			eventPublisher.triggerEvent(event);

			bUpdateColorPointPosition = false;
			bUpdateLeftSpread = false;
			bUpdateRightSpread = false;
		}

		setDisplayListDirty();
		Point currentPoint = glMouseListener.getPickedPoint();

		float[] fArTargetWorldCoordinates = GLCoordinateUtils
				.convertWindowCoordinatesToWorldCoordinates(gl, currentPoint.x,
						currentPoint.y);

		ArrayList<ColorMarkerPoint> markerPoints = dataDomain.getColorMapper()
				.getMarkerPoints();
		ColorMarkerPoint markerPoint = markerPoints.get(iColorMappingPointMoved);

		float fClickedPointX = fArTargetWorldCoordinates[0];

		if (bIsFirstTimeUpdateColor && bUpdateColorPointPosition) {
			bIsFirstTimeUpdateColor = false;
			fColorPointPositionOffset = fClickedPointX - sideSpacing
					- markerPoint.getMappingValue()
					* (viewFrustum.getWidth() - 2 * sideSpacing);
			fClickedPointX -= fColorPointPositionOffset;
		} else if (bUpdateColorPointPosition) {
			fClickedPointX -= fColorPointPositionOffset;
		}

		if (fClickedPointX < sideSpacing)
			fClickedPointX = sideSpacing;
		if (fClickedPointX > viewFrustum.getWidth() - sideSpacing)
			fClickedPointX = viewFrustum.getWidth() - sideSpacing;

		fClickedPointX = (fClickedPointX - sideSpacing)
				/ (viewFrustum.getWidth() - 2 * sideSpacing);

		if (iColorMappingPointMoved > 0) {
			ColorMarkerPoint previousPoint = markerPoints
					.get(iColorMappingPointMoved - 1);
			float fRightOfPrevious = previousPoint.getMappingValue();

			fRightOfPrevious += previousPoint.getRightSpread();

			float fCurrentLeft = fClickedPointX;
			if (bUpdateColorPointPosition) {
				fCurrentLeft -= markerPoint.getLeftSpread();
				if (fCurrentLeft <= fRightOfPrevious + 0.01f)
					fClickedPointX = fRightOfPrevious + 0.01f
							+ markerPoint.getLeftSpread();
			}
			if (bUpdateLeftSpread) {
				if (fCurrentLeft <= fRightOfPrevious + 0.01f)
					fClickedPointX = fRightOfPrevious + 0.01f;
			}

		}

		if (iColorMappingPointMoved < markerPoints.size() - 1) {
			ColorMarkerPoint nextPoint = markerPoints.get(iColorMappingPointMoved + 1);
			float fLeftOfNext = nextPoint.getMappingValue();

			fLeftOfNext -= nextPoint.getLeftSpread();

			float fCurrentRight = fClickedPointX;
			if (bUpdateColorPointPosition) {
				fCurrentRight += markerPoint.getRightSpread();
				if (fCurrentRight >= fLeftOfNext - 0.01f)
					fClickedPointX = fLeftOfNext - 0.01f - markerPoint.getRightSpread();
			}
			if (bUpdateRightSpread) {
				if (fCurrentRight >= fLeftOfNext - 0.01f)
					fClickedPointX = fLeftOfNext - 0.01f;
			}

		}

		if (bUpdateColorPointPosition) {
			if (fClickedPointX < 0)
				fClickedPointX = 0;
			if (fClickedPointX > 1)
				fClickedPointX = 1;
			markerPoint.setMappingValue(fClickedPointX);
		} else if (bUpdateLeftSpread) {
			float fTargetValue = markerPoint.getMappingValue() - fClickedPointX;
			if (fTargetValue < 0.01f)
				fTargetValue = 0.01f;
			markerPoint.setLeftSpread(fTargetValue);
		} else if (bUpdateRightSpread) {
			float fTargetValue = fClickedPointX - markerPoint.getMappingValue();
			if (fTargetValue < 0.01f)
				fTargetValue = 0.01f;
			markerPoint.setRightSpread(fTargetValue);
		}
		dataDomain.getColorMapper().update();

		RedrawViewEvent event = new RedrawViewEvent();
		event.setSender(this);
		event.setDataDomainID(dataDomain.getDataDomainID());
		eventPublisher.triggerEvent(event);
	}

	@Override
	protected void handlePickingEvents(PickingType pickingType, PickingMode pickingMode,
			int externalID, Pick pick) {
		if (detailLevel == DetailLevel.VERY_LOW) {
			return;
		}
		switch (pickingType) {

		case HISTOGRAM_COLOR_LINE:

			switch (pickingMode) {
			case CLICKED:
				bUpdateColorPointPosition = true;
				bIsFirstTimeUpdateColor = true;
				iColorMappingPointMoved = externalID;
				break;
			case MOUSE_OVER:

				break;
			default:
				return;
			}
			setDisplayListDirty();
			break;
		case HISTOGRAM_LEFT_SPREAD_COLOR_LINE:
			switch (pickingMode) {
			case CLICKED:
				bUpdateLeftSpread = true;
				iColorMappingPointMoved = externalID;
				break;
			case MOUSE_OVER:

				break;
			default:
				return;
			}
			setDisplayListDirty();
			break;
		case HISTOGRAM_RIGHT_SPREAD_COLOR_LINE:
			switch (pickingMode) {
			case CLICKED:
				bUpdateRightSpread = true;
				iColorMappingPointMoved = externalID;
				break;
			case MOUSE_OVER:

				break;
			default:
				return;
			}
			setDisplayListDirty();
			break;
		}
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedHistogramView serializedForm = new SerializedHistogramView(
				dataDomain.getDataDomainID());
		serializedForm.setViewID(this.getID());
		return serializedForm;
	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();

	}

	@Override
	public void unregisterEventListeners() {
		super.unregisterEventListeners();
		if (redrawViewListener != null) {
			eventPublisher.removeListener(redrawViewListener);
			redrawViewListener = null;
		}
		if (clearSelectionsListener != null) {
			eventPublisher.removeListener(clearSelectionsListener);
			clearSelectionsListener = null;
		}
	}

	@Override
	public int getMinPixelHeight() {
		// TODO: Calculate depending on content
		return 100;
	}

	@Override
	public int getMinPixelWidth() {
		// TODO: Calculate depending on content
		return 150;
	}

	@Override
	public int getMinPixelHeight(DetailLevel detailLevel) {
		switch (detailLevel) {
		case HIGH:
			return 300;
		case MEDIUM:
			return 130;
		case LOW:
			return 50;
		default:
			return 50;
		}
	}

	@Override
	public int getMinPixelWidth(DetailLevel detailLevel) {
		switch (detailLevel) {
		case HIGH:
			return 300;
		case MEDIUM:
			return 130;
		case LOW:
			return 50;
		default:
			return 50;
		}
	}

	/**
	 * Determines color mode of histogram.
	 * 
	 * @param useColor
	 *            If false the histogram is rendered B/W
	 */
	public void setUseColor(boolean useColor) {
		this.useColor = useColor;
	}

	@Override
	protected ArrayList<ElementConnectionInformation> createElementConnectionInformation(
			IDType idType, int id) throws InvalidAttributeValueException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDataView() {
		return false;
	}
}
