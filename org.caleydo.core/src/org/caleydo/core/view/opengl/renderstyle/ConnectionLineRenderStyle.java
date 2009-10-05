package org.caleydo.core.view.opengl.renderstyle;

public class ConnectionLineRenderStyle

{

	public static final float[] CONNECTION_AREA_COLOR = { 0.812f, 0.812f, 0.116f, 0.65f };

	public static final float[] CONNECTION_LINE_COLOR_1 = { 1f, 1f, 0f, 1f };
	public static final float[] CONNECTION_LINE_COLOR_2 = { 0.25f, 0.6f, 1f, 1f };

	public static final float[] CONNECTION_LINE_COLOR = GeneralRenderStyle.MOUSE_OVER_COLOR;
//	public static final float[] CONNECTION_LINE_COLOR = { 0.54f, 0.17f, 0.89f, 1f}; // blue-violet
//	public static final float[] CONNECTION_LINE_COLOR = { 0.79f, 1f, 0.44f, 1f}; // dark olive green
//	public static final float[] CONNECTION_LINE_COLOR = { 1f, 0.49f, 0.31f, 1f}; // coral	

	public static final float CONNECTION_LINE_WIDTH = 3f;

	public static final float[] CONNECTION_LINE_SHADOW_COLOR = { 0.4f, 0.4f, 0.4f, 0.8f };
	
	public static final float CONNECTION_LINE_HALO_WIDTH = 2f;
	
	public static final int LINE_ANTI_ALIASING_QUALITY = 5;
	
	public static final int ANIMATION_SPEED_IN_MILLIS = 1500;
}
