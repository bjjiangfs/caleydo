package org.caleydo.view.filterpipeline.representation;

import javax.media.opengl.GL2;
import org.caleydo.core.data.filter.ContentFilter;
import org.caleydo.core.data.filter.ContentMetaOrFilter;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.IVirtualArray;
import org.caleydo.core.data.virtualarray.delta.ContentVADelta;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.PickingManager;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;
import org.caleydo.view.filterpipeline.renderstyle.FilterPipelineRenderStyle;

/**
 * @author Thomas Geymayer
 *
 */
public class FilterRepresentationMetaOr
	extends FilterRepresentation
{
	public static boolean renderPassedAll = false;
	
	private int numElementsPassedAll = 0;
	private int[] subFilterSizes = new int[0];
	private boolean sizesDirty = true;

	public FilterRepresentationMetaOr( FilterPipelineRenderStyle renderStyle,
									   PickingManager pickingManager,
									   int viewId )
	{
		super(renderStyle, pickingManager, viewId);
	}
	
	@Override
	public void render(GL2 gl, CaleydoTextRenderer textRenderer)
	{
		if( sizesDirty )
			calculateSizes();

		heightLeft = getHeightLeft();
		heightRight = vSize.y() * (filter.getOutput().size()/100.f);

		// render filter
		gl.glPushName(iPickingID);
		renderShape
		(
			gl,
			GL2.GL_QUADS,
			renderStyle.FILTER_COMBINED_BACKGROUND_COLOR,
			Z_POS_BODY
		);
		
		for( int i = 0; i < subFilterSizes.length; ++i )
		{
			gl.glPushName(pickingManager.getPickingID(viewId, EPickingType.FILTERPIPE_SUB_FILTER, i));
			heightRight = vSize.y() * (subFilterSizes[i]/100.f);
			renderShape
			(
				gl,
				GL2.GL_QUADS,
				renderStyle.getFilterColorCombined(i),
				Z_POS_BODY
			);
			gl.glPopName();
		}
		
		if( renderPassedAll )
		{
			// render elements passed all filters
			heightRight = vSize.y() * (numElementsPassedAll/100.f);
			renderShape
			(
				gl,
				GL2.GL_QUADS,
				renderStyle.FILTER_PASSED_ALL_COLOR,
				Z_POS_BODY
			);
		}
		gl.glPopName();
		
		if( mouseOverItem >= 0 )
		{
			heightRight = vSize.y() * (subFilterSizes[mouseOverItem]/100.f);
			gl.glLineWidth(SelectionType.MOUSE_OVER.getLineWidth());
			
			renderShape
			(
				gl,
				GL2.GL_LINE_LOOP,
				SelectionType.MOUSE_OVER.getColor(),
				Z_POS_MARK
			);
		}
		
		// reset height
		heightRight = vSize.y() * (filter.getOutput().size()/100.f);
		
		// render selection/mouseover if needed
		if( selectionType != SelectionType.NORMAL && mouseOverItem < 0 )
		{
			gl.glLineWidth
			( 
				(selectionType == SelectionType.SELECTION)
					? SelectionType.SELECTION.getLineWidth()
	                : SelectionType.MOUSE_OVER.getLineWidth()
	        );
			
			renderShape
			(
				gl,
				GL2.GL_LINE_LOOP,
				(selectionType == SelectionType.SELECTION)
					? SelectionType.SELECTION.getColor()
					: SelectionType.MOUSE_OVER.getColor(),
				Z_POS_MARK
			);
		}
		
		// currently not filtered elements
		textRenderer.renderText
		(
			gl,
			""+filter.getOutput().size(),
			vPos.x() + vSize.x() - 0.4f,
			vPos.y() + heightRight + 0.05f,
			Z_POS_TEXT,
			0.007f,
			20
		);
		
		// label
		textRenderer.renderText
		(
			gl,
			(filter.getOutput().size() - filter.getInput().size())
			+ " (-"+filter.getSizeVADelta()+")",
			vPos.x() + 0.05f,
			vPos.y() + 0.05f,
			Z_POS_TEXT,
			0.007f,
			20
		);
	}
	
	private void calculateSizes()
	{
		sizesDirty  = false;
		
		// TODO also handle storage filter
		IVirtualArray<?,ContentVADelta,?> input =
			(IVirtualArray<?, ContentVADelta, ?>) filter.getInput().clone();
		
		for(ContentFilter subFilter : ((ContentMetaOrFilter)filter.getFilter()).getFilterList() )
			input.setDelta(subFilter.getVADelta());
		
		numElementsPassedAll = input.size();
		
		subFilterSizes = new int[((ContentMetaOrFilter)filter.getFilter()).getFilterList().size()];
		int i = 0;
		for(ContentFilter subFilter : ((ContentMetaOrFilter)filter.getFilter()).getFilterList() )
		{
			input =	(IVirtualArray<?, ContentVADelta, ?>) filter.getInput().clone();
			input.setDelta(subFilter.getVADelta());
			subFilterSizes[i++] = input.size();
		}
	}

}
