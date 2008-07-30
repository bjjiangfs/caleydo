package org.caleydo.core.manager.view;

public enum EPickingType
{
	// bucket
	BUCKET_MOVE_IN_ICON_SELECTION, BUCKET_MOVE_OUT_ICON_SELECTION, BUCKET_MOVE_LEFT_ICON_SELECTION, BUCKET_MOVE_RIGHT_ICON_SELECTION, BUCKET_LOCK_ICON_SELECTION,
	// BUCKET_REMOVE_ICON_SELECTION,
	// BUCKET_SWITCH_ICON_SELECTION,
	// BUCKET_SEARCH_PATHWAY,
	VIEW_SELECTION,

	// parallel coordinates
	POLYLINE_SELECTION, X_AXIS_SELECTION, Y_AXIS_SELECTION, LOWER_GATE_TIP_SELECTION, LOWER_GATE_BODY_SELECTION, LOWER_GATE_BOTTOM_SELECTION,
	// UPPER_GATE_SELECTION,
	PC_ICON_SELECTION, MOVE_AXIS_LEFT, MOVE_AXIS_RIGHT, REMOVE_AXIS, DUPLICATE_AXIS, ANGULAR_UPPER, ANGULAR_LOWER,

	// pathway manager
	PATHWAY_ELEMENT_SELECTION, PATHWAY_TEXTURE_SELECTION, MEMO_PAD_SELECTION,

	// heat map
	HEAT_MAP_FIELD_SELECTION,

	// glyph
	GLYPH_FIELD_SELECTION
}
