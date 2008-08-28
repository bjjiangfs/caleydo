package org.caleydo.core.data.view.rep.renderstyle;

import org.caleydo.core.data.view.camera.IViewFrustum;

/**
 * Render styles for the parallel coordinates
 * 
 * @author Alexander Lex
 */

public class ParCoordsRenderStyle
	extends GeneralRenderStyle
{

	public static final float[] POLYLINE_NO_OCCLUSION_PREV_COLOR = { 0.0f, 0.0f, 0.0f, 1.0f };

	public static final float[] POLYLINE_SELECTED_COLOR = { 0f, 1.0f, 0.0f, 1.0f };

	public static final float SELECTED_POLYLINE_LINE_WIDTH = 4.0f;

	public static final float[] POLYLINE_MOUSE_OVER_COLOR = { 1.0f, 0.0f, 0.0f, 1.0f };

	public static final float MOUSE_OVER_POLYLINE_LINE_WIDTH = 4.0f;

	public static final float DESELECTED_POLYLINE_LINE_WIDTH = 1.0f;

	public static final float[] Y_AXIS_COLOR = { 0.0f, 0.0f, 0.0f, 1.0f };

	public static final float Y_AXIS_LINE_WIDTH = 1.0f;

	public static final float[] Y_AXIS_SELECTED_COLOR = SELECTED_COLOR;// {1.0f,
	// 0.0f,
	// 0.0f,
	// 1.0f
	// };

	public static final float Y_AXIS_SELECTED_LINE_WIDTH = 4.0f;

	public static final float[] Y_AXIS_MOUSE_OVER_COLOR = SELECTED_COLOR;//{0.0f
	// ,
	// 1.0f
	// ,
	// 0.0f
	// ,
	// 1.0f
	// }
	// ;

	public static final float Y_AXIS_MOUSE_OVER_LINE_WIDTH = 4.0f;

	public static final float[] X_AXIS_COLOR = { 0.0f, 0.0f, 1.0f, 1.0f };

	public static final float X_AXIS_LINE_WIDTH = 3.0f;

	public static final float AXIS_Z = 0.0f;

	public static final float[] CANVAS_COLOR = { 1.0f, 1.0f, 1.0f, 1.0f };

	public static final float[] GATE_COLOR = { 0.61f, 0.705f, 1.0f, 0.8f };

	public static final float[] ANGULAR_COLOR = { 0, 0, 1, 1 };

	public static final float ANGLUAR_LINE_WIDTH = 2;

	// Line widths
	public static final float POLYLINE_LINE_WIDTH = 1.0f;

	public static final int NUMBER_AXIS_MARKERS = 9;

	// modifiable colors
	protected float[] polylineOcclusionPrevColor = { 0.0f, 0.0f, 0.0f, 1.0f };

	protected float[] polylineDeselectedColor = { 0.0f, 0.0f, 0.0f, 0.1f };

	protected float fOcclusionPrevAlpha = 0.1f;

	// how much room between the axis?
	private float fAxisSpacing = 0.3f;

	// --- constants to scale ---

	// coordinate system
	private static final float COORDINATE_TOP_SPACING = 0.07f;

	private static final float COORDINATE_SIDE_SPACING = 0.05f;

	private static final float COORDINATE_BOTTOM_SPACING = 0.04f;

	public static final float Y_AXIS_LOW = -0.1f;

	public static final float AXIS_MARKER_WIDTH = 0.01f;

	// gates
	private static final float GATE_WIDTH = 0.005f;

	private static final float GATE_NEGATIVE_Y_OFFSET = -0.01f;

	private static final float GATE_TIP_HEIGHT = 0.01f;

	// buttons below axis
	private static final float AXIS_BUTTONS_Y_OFFSET = 0.04f;

	private static final float fAxisSpacingLowerLimit = 0.03f;

	/**
	 * Constructor.
	 * 
	 * @param viewFrustum
	 */
	public ParCoordsRenderStyle(IViewFrustum viewFrustum)
	{

		super(viewFrustum);
	}

	public float[] getPolylineOcclusionPrevColor(int iNumberOfRenderedLines)
	{

		fOcclusionPrevAlpha = (float) (6 / Math.sqrt(iNumberOfRenderedLines));

		polylineOcclusionPrevColor[3] = fOcclusionPrevAlpha;

		return polylineOcclusionPrevColor;
	}

	public float[] getPolylineDeselectedOcclusionPrevColor(int iNumberOfRenderedLines)
	{

		polylineDeselectedColor[3] = (float) (0.5 / Math.sqrt(iNumberOfRenderedLines));
		return polylineDeselectedColor;
	}

	public float getAxisSpacing(final int iNumberOfAxis)
	{

		fAxisSpacing = (viewFrustum.getWidth() - COORDINATE_SIDE_SPACING * 2 * getScaling())
				/ (iNumberOfAxis - 1);

		if (fAxisSpacing < fAxisSpacingLowerLimit * getScaling())
			return fAxisSpacingLowerLimit * getScaling();

		return fAxisSpacing;
	}

	public float getAxisHeight()
	{

		return viewFrustum.getHeight() - (COORDINATE_TOP_SPACING + COORDINATE_BOTTOM_SPACING)
				* getScaling();
	}

	public float getXSpacing()
	{

		return COORDINATE_SIDE_SPACING * getScaling();
	}

	public float getBottomSpacing()
	{

		return COORDINATE_BOTTOM_SPACING * getScaling();
	}

	public float getAxisButtonYOffset()
	{

		return AXIS_BUTTONS_Y_OFFSET * getScaling();
	}

	public float getGateWidth()
	{

		return GATE_WIDTH * getScaling();
	}

	public float getGateYOffset()
	{

		return GATE_NEGATIVE_Y_OFFSET * getScaling();
	}

	public float getGateTipHeight()
	{

		return GATE_TIP_HEIGHT * getScaling();
	}

	public float getAxisCaptionSpacing()
	{

		return COORDINATE_TOP_SPACING / 3 * getScaling();
	}
	// GATE_WIDTH = 0.015f;
	// private static final float GATE_NEGATIVE_Y_OFFSET = -0.04f;
	// private static final float GATE_TIP_HEIGHT
}
