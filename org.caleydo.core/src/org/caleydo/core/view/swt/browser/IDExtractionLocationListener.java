package org.caleydo.core.view.swt.browser;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.caleydo.core.data.collection.set.selection.SetSelection;
import org.caleydo.core.data.graph.item.vertex.PathwayVertexGraphItem;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.util.system.StringConversionTool;
import org.caleydo.util.graph.EGraphItemProperty;
import org.caleydo.util.graph.IGraphItem;


public class IDExtractionLocationListener 
extends LocationAdapter 
{
	private IGeneralManager refGeneralManager;
	
	private HookedBrowser browser;
	
	private int iBrowserId;
	
	private int iSelectionSetId;
	
	private boolean bSkipNextChangeEvent = false;
	
	public IDExtractionLocationListener(final IGeneralManager refGeneralManager,
			final HookedBrowser browser,
			final int iBrowserId,
			final int iSelectionSetId) {
	
		this.refGeneralManager = refGeneralManager;
		this.browser = browser;
		this.iBrowserId = iBrowserId;
		this.iSelectionSetId = iSelectionSetId;

	}
	
	@Override
	public void changed(LocationEvent event) {
		
	}

	@Override
	// http://www.genome.jp/dbget-bin/show_pathway?map00020+1.1.1.37
	// http://www.genome.jp/dbget-bin/www_bget?hsa+4190
	// http://www.genome.jp/dbget-bin/www_bget?compound+C00003
	// http://www.genome.jp/dbget-bin/www_bget?enzyme+3.6.3.5
	public void changing(LocationEvent event) {
		
		if (bSkipNextChangeEvent == true)
		{
			bSkipNextChangeEvent = false;
			return;
		}

		bSkipNextChangeEvent = false;
		
		String sSearchPhrase_NCBIGeneId = "http://www.genome.jp/dbget-bin/www_bget?hsa+";
		String sSearchPhrase_Pathway = "http://www.genome.jp/dbget-bin/show_pathway?hsa";
		
		ArrayList <Integer> iAlSelectionId = null;
		ArrayList <Integer> iAlSelectionDepth = null;
		ArrayList <Integer> iAlOptional = null;
		
		if(event.location.contains(sSearchPhrase_NCBIGeneId))
		{
			String sExtractedID = 
				event.location.substring(sSearchPhrase_NCBIGeneId.length());

			int iPathwayGraphItemId = refGeneralManager.getSingleton().getPathwayItemManager()
				.getPathwayVertexGraphItemIdByNCBIGeneId(StringConversionTool.convertStringToInt(sExtractedID, -1));
		
			if (iPathwayGraphItemId == -1)
				return;
			
			PathwayVertexGraphItem vertexItemBuffer = (PathwayVertexGraphItem) refGeneralManager.getSingleton()
				.getPathwayItemManager().getItem(iPathwayGraphItemId);
			
			Iterator <IGraphItem> iterList = 
				vertexItemBuffer.getAllItemsByProp(EGraphItemProperty.ALIAS_CHILD).iterator();
			
			iAlSelectionId = new ArrayList <Integer>();
			iAlSelectionDepth = new ArrayList <Integer> ();
			
			while (iterList.hasNext()) 
			{
				IGraphItem bufferItem = iterList.next();					
				iAlSelectionId.add( bufferItem.getId());
				iAlSelectionDepth.add(0);
			}
			
			
		}	
		else if (event.location.contains(sSearchPhrase_Pathway))
		{
			// Prevent loading of clicked pathway URL
			event.doit = false;
			
			int iPathwayId = 0;
			int iPathwayIdIndex = 0;
	
			// Extract clicked pathway ID
			if (event.location.contains("map0"))
			{
				iPathwayIdIndex = event.location.lastIndexOf("map0") + 4;
			}
			else if (event.location.contains("hsa0"))
			{
				iPathwayIdIndex = event.location.lastIndexOf("hsa0") + 4;			
			}
			else
				return;
			
			iPathwayId = StringConversionTool.convertStringToInt(event.location
					.substring(iPathwayIdIndex, event.location.lastIndexOf('+')), 0);
			
//			iArSelectionId = new int[0];
//			iArSelectionDepth = new int[0];
			
			iAlOptional.add(iPathwayId);
		}
		else
		{
			return;
		}
		
		SetSelection tmpSelectionSet = 
			(SetSelection) refGeneralManager.getSingleton().getSetManager().getItemSet(iSelectionSetId);
		tmpSelectionSet.updateSelectionSet(iBrowserId, 
				iAlSelectionId,
				iAlSelectionDepth, 
				iAlOptional);
	}
	
	public void updateSkipNextChangeEvent(boolean bSkipNextChangeEvent) {
		
		this.bSkipNextChangeEvent = bSkipNextChangeEvent;
	}
}
