package org.caleydo.core.data.graph.item.edge;

import java.io.Serializable;

import org.caleydo.util.graph.EGraphItemKind;
import org.caleydo.util.graph.item.GraphItem;

/**
 * Pathway relation edge representation belonging to the overall pathway graph.
 * Used for KEGG pathways.
 * 
 * @author Marc Streit
 *
 */
public class PathwayRelationEdgeGraphItemRep 
extends GraphItem   
implements Serializable 
{	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor.
	 * 
	 * @param iId
	 */
	public PathwayRelationEdgeGraphItemRep(
			final int iId) {
		
		super(iId, EGraphItemKind.EDGE);
	}
}