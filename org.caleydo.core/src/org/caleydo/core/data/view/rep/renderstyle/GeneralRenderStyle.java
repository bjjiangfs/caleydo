package org.caleydo.core.data.view.rep.renderstyle;

import org.caleydo.core.data.view.camera.IViewFrustum;

/**
 * Render Styles for the whole system
 * 
 * @author Alexander Lex
 */
public class GeneralRenderStyle
{

	private static final float VERY_SMALL_FONT_SCALING_FACTOR = 0.0005f;

	private static final float SMALL_FONT_SCALING_FACTOR = 0.0008f;

	private static final float HEADING_FONT_SCALING_FACTOR = 0.001f;

	public static final float INFO_AREA_Z = 0.02f;

	public static final float INFO_AREA_CONNECTION_Z = 0.01f;

	public static final float MINIVEW_Z = 0.02f;

	public static final float[] SELECTED_COLOR = { 1, 1, 0, 1 };

	public static final float[] MOUSE_OVER_COLOR = { 1, 1, 0, 1 };

	public static final float SELECTED_LINE_WIDTH = 3;

	public static final float MOUSE_OVER_LINE_WIDTH = 3;

	// protected float fFrustumHeight = 0;
	//
	// protected float fFrustumWidth = 0;

	// protected float fScaling = 1;

	protected static final float BUTTONS_SPACING = 0.005f;

	protected static final float BUTTON_WIDTH = 0.018f;

	protected IViewFrustum viewFrustum;

	/**
	 * Default constructor.
	 */
	public GeneralRenderStyle()
	{

	}

	/**
	 * Constructor.
	 */
	public GeneralRenderStyle(IViewFrustum viewFrustum)
	{
		this.viewFrustum = viewFrustum;
		// fFrustumWidth = viewFrustum.getRight() - viewFrustum.getLeft();
		// fFrustumHeight = viewFrustum.getTop() - viewFrustum.getBottom();
		// scaling is set to the smaller of the two

	}

	public float getSmallFontScalingFactor()
	{

		return SMALL_FONT_SCALING_FACTOR * getScaling();
	}

	public float getVerySmallFontScalingFactor()
	{

		return VERY_SMALL_FONT_SCALING_FACTOR * getScaling();
	}

	public float getHeadingFontScalingFactor()
	{

		return HEADING_FONT_SCALING_FACTOR * getScaling();
	}

	public float getButtonSpacing()
	{

		return BUTTONS_SPACING * getScaling();
	}

	public float getButtonWidht()
	{

		return BUTTON_WIDTH * getScaling();
	}

	public float getScaling()
	{
		float fScaling;
		if (viewFrustum.getWidth() > viewFrustum.getHeight())
			fScaling = viewFrustum.getWidth();
		else
			fScaling = viewFrustum.getHeight();
		return fScaling;
	}

}
