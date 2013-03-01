package org.caleydo.view.tourguide.v3.ui.detail;

import org.caleydo.core.util.format.Formatter;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.tourguide.v3.model.ARankColumnModel;
import org.caleydo.view.tourguide.v3.model.IRow;
import org.caleydo.view.tourguide.v3.model.mixin.ICollapseableColumnMixin;
import org.caleydo.view.tourguide.v3.model.mixin.IMappedColumnMixin;
import org.caleydo.view.tourguide.v3.model.mixin.IRankableColumnMixin;
import org.caleydo.view.tourguide.v3.ui.TableColumnUI;

public class ScoreBarRenderer implements IGLRenderer {
	private final IRankableColumnMixin model;

	public ScoreBarRenderer(IRankableColumnMixin model) {
		this.model = model;
	}

	@Override
	public void render(GLGraphics g, float w, float h, GLElement parent) {
		final IRow r = parent.getLayoutDataAs(IRow.class, null);
		float v = model.getValue(r);
		if (Float.isNaN(v) || v <= 0)
			return;
		if (isCollapsed((TableColumnUI)parent.getParent())) {
			g.color(1 - v, 1 - v, 1 - v, 1).fillRect(w * 0.1f, h * 0.1f, w * 0.8f, h * 0.8f);
		} else {
			g.color(model.getColor()).fillRect(0, h * 0.1f, w * v, h * 0.8f);
			if (model.getTable().getSelectedRow() == r) {
				String text = (model instanceof IMappedColumnMixin) ? ((IMappedColumnMixin) model).getRawValue(r)
						: Formatter.formatNumber(v);
				renderLabel(g, h * 0.2f, w, h * 0.45f, text, v, parent);
			}
		}
	}
	/**
	 * @return
	 */
	private static boolean isCollapsed(TableColumnUI p) {
		ARankColumnModel m = p.getModel();
		if (m instanceof ICollapseableColumnMixin && ((ICollapseableColumnMixin)m).isCollapsed())
			return true;
		return false;
	}

	static void renderLabel(GLGraphics g, float y, float w, float h, String text, float v, GLElement parent) {
		if (h < 7)
			return;
		float tw = g.text.getTextWidth(text, h);
		boolean hasFreeSpace = parent.getLayoutDataAs(Boolean.class, Boolean.TRUE);

		if (tw < w * v)
			g.drawText(text, 1, y, w * v - 2, h, VAlign.RIGHT);
		else if (tw < w && hasFreeSpace) {
			VAlign alignment = parent.getLayoutDataAs(VAlign.class, VAlign.LEFT);
			if (alignment == VAlign.LEFT)
				g.drawText(text, w * v + 1, y, w - w * v, h);
			else
				g.drawText(text, -w + w * v, y, w, h, VAlign.RIGHT);
		}
	}
}