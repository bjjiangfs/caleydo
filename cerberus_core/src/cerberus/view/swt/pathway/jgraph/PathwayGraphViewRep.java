package cerberus.view.swt.pathway.jgraph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphUndoManager;

import cerberus.data.collection.ISet;
import cerberus.data.collection.IStorage;
import cerberus.data.collection.set.SetFlatThreadSimple;
import cerberus.data.collection.set.selection.ISetSelection;
import cerberus.data.graph.core.PathwayGraph;
import cerberus.data.graph.item.vertex.EPathwayVertexShape;
import cerberus.data.graph.item.vertex.PathwayVertexGraphItem;
import cerberus.data.graph.item.vertex.PathwayVertexGraphItemRep;
import cerberus.data.view.rep.pathway.renderstyle.PathwayRenderStyle;
import cerberus.data.view.rep.pathway.renderstyle.PathwayRenderStyle.EdgeArrowHeadStyle;
import cerberus.data.view.rep.pathway.renderstyle.PathwayRenderStyle.EdgeLineStyle;
import cerberus.manager.IGeneralManager;
import cerberus.manager.IViewGLCanvasManager;
import cerberus.manager.ILoggerManager.LoggerType;
import cerberus.manager.type.ManagerObjectType;
import cerberus.util.system.StringConversionTool;
import cerberus.view.swt.pathway.APathwayGraphViewRep;
import cerberus.view.swt.widget.SWTEmbeddedGraphWidget;

/**
 * In this class the real drawing of the Pathway happens. For the drawing the
 * JGraph package is used. We can decide here if we want to draw in a new widget
 * or if we want to draw in an existing one.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 * 
 */

public class PathwayGraphViewRep 
extends APathwayGraphViewRep {

	/**
	 * Pathway element positions are read from XML files. The scaling factor can
	 * scale the positions to blow up or shrink the pathway.
	 */
	protected static final float SCALING_FACTOR = 1.15f;

	protected float fScalingFactor = 1.0f;
	
	protected PathwayGraph refCurrentPathway;

	protected GraphModel refGraphModel;

	protected GraphLayoutCache refGraphLayoutCache;

	protected JGraph refPathwayGraph;

	protected HashMap<Integer, DefaultGraphCell> vertexIdToCellLUT;

	protected boolean bGraphSet = false;

	protected boolean bShowBackgroundOverlay = true;

	protected GraphUndoManager refUndoManager;

	protected GPOverviewPanel refOverviewPanel;

	protected Vector<DefaultEdge> vecRelationEdges;

	protected Vector<DefaultEdge> vecReactionEdges;

	protected Vector<DefaultGraphCell> vecVertices;

	protected ArrayList<DefaultGraphCell> arSelectedVertices;

	/**
	 * Holds the cell that was recently clicked by the user using the mouse. The
	 * variable is needed for updating the neighborhood distance in the menu on
	 * the fly without selecting a new cell.
	 */
	protected DefaultGraphCell lastClickedGraphCell;

	/**
	 * Integer storage of selected vertices Container is needed for selection
	 * updates.
	 */
	protected LinkedList<Integer> iLLSelectedVertices;

	/**
	 * Neighbor distance from currently selected vertex. Container is needed for
	 * selection updates.
	 */
	protected LinkedList<Integer> iLLNeighborDistance;

	/**
	 * Specifies how deep the neighborhood recursion should visualize
	 * surrounding elements. Default value is 0.
	 */
	protected int iNeighbourhoodDistance = 0;

	/**
	 * Counts how often the neighbour recursion is called. This is needed for
	 * the UNDO operation on the next selection.
	 */
	protected int iNeighbourhoodUndoCount = 0;

	/**
	 * Flag shows if the neighbours of a cell are currently displayed. Flag is
	 * needed for UNDO of previous neigbour highlighning.
	 */
	protected boolean bNeighbourhoodShown = false;

	protected HashSet<DefaultGraphCell> hashSetVisitedNeighbors;

	protected HashMap<PathwayVertexGraphItemRep, DefaultGraphCell> hashVertexRep2GraphCell;

	protected boolean bShowReactionEdges = false;

	protected boolean bShowRelationEdges = false;

	public PathwayGraphViewRep(IGeneralManager refGeneralManager,
			int iParentContainerId) {

		super(refGeneralManager, -1, iParentContainerId, "");

		vertexIdToCellLUT = new HashMap<Integer, DefaultGraphCell>();

		vecRelationEdges = new Vector<DefaultEdge>();
		vecReactionEdges = new Vector<DefaultEdge>();
		vecVertices = new Vector<DefaultGraphCell>();

		fScalingFactor = SCALING_FACTOR;

		iLLSelectedVertices = new LinkedList<Integer>();
		iLLNeighborDistance = new LinkedList<Integer>();

		hashVertexRep2GraphCell = new HashMap<PathwayVertexGraphItemRep, DefaultGraphCell>();

		hashSetVisitedNeighbors = new HashSet<DefaultGraphCell>();

		arSelectedVertices = new ArrayList<DefaultGraphCell>();
	}

	/**
	 * Method uses the parent container ID to retrieve the GUI widget by calling
	 * the createWidget method from the SWT GUI Manager.
	 * 
	 */
	protected void retrieveGUIContainer() {

		SWTEmbeddedGraphWidget refSWTEmbeddedGraphWidget = (SWTEmbeddedGraphWidget) refGeneralManager
				.getSingelton().getSWTGUIManager().createWidget(
						ManagerObjectType.GUI_SWT_EMBEDDED_JGRAPH_WIDGET,
						refEmbeddedFrameComposite, iWidth, iHeight);

		refSWTEmbeddedGraphWidget.createEmbeddedComposite();
		refEmbeddedFrame = refSWTEmbeddedGraphWidget.getEmbeddedFrame();
	}

	public void initView() {

		retrieveGUIContainer();
		extractCurrentPathwayFromSet();

		class PathwayMarqueeHandler extends BasicMarqueeHandler {

			public boolean isForceMarqueeEvent(MouseEvent event) {

				if (SwingUtilities.isLeftMouseButton(event)
						&& event.getClickCount() == 1)
					return true;
				else
					return super.isForceMarqueeEvent(event);
			}

			public void mousePressed(final MouseEvent event) {

				if (refCurrentPathway != null)
				{
					DefaultGraphCell clickedCell = (DefaultGraphCell) refPathwayGraph
							.getFirstCellForLocation(event.getX(), event.getY());

					// Do nothing when there no specific node was clicked.
					if (clickedCell == null)
						return;

					lastClickedGraphCell = clickedCell;

					// Check if cell has an user object attached
					if (lastClickedGraphCell.getUserObject() == null)
					{
						super.mousePressed(event);
						return;
					}

					if (!clickedCell.getUserObject().getClass().getSimpleName()
							.equals(PathwayVertexGraphItemRep.class.getSimpleName()))
					{
						super.mousePressed(event);
						return;
					}

					processSelectedCell();

					int[] iArSelectedVertices = null;
					int[] iArNeighborDistance = null;

					// Convert Link List to int[]
					Iterator<Integer> iter_I = iLLSelectedVertices.iterator();
					iArSelectedVertices = new int[iLLSelectedVertices.size()];
					for (int i = 0; iter_I.hasNext(); i++)
					{
						iArSelectedVertices[i] = iter_I.next().intValue();
					}

					iter_I = iLLNeighborDistance.iterator();
					iArNeighborDistance = new int[iLLNeighborDistance.size()];
					for (int i = 0; iter_I.hasNext(); i++)
					{
						iArNeighborDistance[i] = iter_I.next().intValue();
					}

					alSetSelection.get(0).updateSelectionSet(iParentContainerId,
							iArSelectedVertices, new int[0],
							iArNeighborDistance);

				}// if(refCurrentPathway != 0)
				else if (refCurrentPathwayImageMap != null)
				{
					String sLink = refCurrentPathwayImageMap
							.processPoint(new Point(event.getX(), event.getY()));

					if (sLink == null || sLink.equals(""))
					{
						refGeneralManager
								.getSingelton()
								.logMsg(
										this.getClass().getSimpleName()
												+ ":mousePressed(): No pathway link is available for that clicked point. Click ignored.",
										LoggerType.VERBOSE);

						return;
					}

					// Append file path
					sLink = refGeneralManager.getSingelton()
							.getPathwayManager().getPathwayImageMapPath()
							+ sLink;

					iPathwayLevel++;
					if (iPathwayLevel >= 3)
					{
						// fScalingFactor = SCALING_FACTOR;
						// bShowBackgroundOverlay = true;
						extractClickedPathway(sLink);
						// loadPathwayFromFile(sLink);
					} else
					{
						loadImageMapFromFile(sLink);
					}
				}
			}
		}

		refGraphModel = new DefaultGraphModel();

		refGraphLayoutCache = new GraphLayoutCache(refGraphModel,
				new GPCellViewFactory(), true);

		refPathwayGraph = new JGraph(refGraphModel, refGraphLayoutCache);

		// // Set own cell view factory
		// refPathwayGraph.getGraphLayoutCache().setFactory(
		// new GPCellViewFactory());

		// Control-drag should clone selection
		refPathwayGraph.setCloneable(true);

		// Turn on anti-aliasing
		refPathwayGraph.setAntiAliased(true);

		refPathwayGraph.setMarqueeHandler(new PathwayMarqueeHandler());

		// Create and register Undo Manager
		refUndoManager = new GraphUndoManager();
		refGraphModel.addUndoableEditListener(refUndoManager);
	}

	protected void processSelectedCell() {

		// Remove old selected vertices
		iLLSelectedVertices.clear();
		iLLNeighborDistance.clear();
		arSelectedVertices.clear();

		// Check if a node or edge was hit.
		// If not undo neighborhood visualization and return.
		if (lastClickedGraphCell == null)
		{
			for (int iUndoCount = 0; iUndoCount < iNeighbourhoodUndoCount; iUndoCount++)
			{
				refUndoManager.undo(refGraphLayoutCache);
			}

			iNeighbourhoodUndoCount = 0;
			bNeighbourhoodShown = false;
			return;
		}

		// Check if clicked object is a cell
		if (!lastClickedGraphCell.getUserObject().getClass().getSimpleName()
				.equals(PathwayVertexGraphItemRep.class.getSimpleName()))
		{
			return;
		}

		final String sUrl = ((PathwayVertexGraphItemRep) lastClickedGraphCell
				.getUserObject()).getPathwayVertexGraphItem().getExternalLink();

		if (extractClickedPathway(sUrl) == false)
		{
			loadNodeInformationInBrowser(sUrl);

			// UNDO old neighborhood visualization
			if (bNeighbourhoodShown == true)
			{
				for (int iUndoCount = 0; iUndoCount < iNeighbourhoodUndoCount; iUndoCount++)
				{
					refUndoManager.undo(refGraphLayoutCache);
				}
				iNeighbourhoodUndoCount = 0;
				bNeighbourhoodShown = false;
			}

			// Highlight current cell
			highlightCell(lastClickedGraphCell, Color.RED);
//			dataMappingTest(((PathwayVertexRep) lastClickedGraphCell
//					.getUserObject()));

			// The clicked vertex will be added with neighborhood distance of 0
			iLLNeighborDistance.add(0);

			bNeighbourhoodShown = true;
			iNeighbourhoodUndoCount++;

			// Add selected vertex itself because neighborhood algorithm
			// only adds neighbor vertices.
			iLLSelectedVertices.add(((PathwayVertexGraphItemRep) lastClickedGraphCell
					.getUserObject()).getPathwayVertexGraphItem().getId());

			arSelectedVertices.add(lastClickedGraphCell);

			if (iNeighbourhoodDistance != 0)
			{
				showNeighbourhoodBFS(iNeighbourhoodDistance);
				bNeighbourhoodShown = true;
			}
		}// if(sUrl.contains((CharSequence)sSearchPattern))
	}

	public void drawView() {

		// TODO: add try catch for pathway null object
		if (refCurrentPathway != null)
		{
			extractVertices(refCurrentPathway);
			//extractEdges(refCurrentPathway);

			finishGraphBuilding();
		}
		// else if (iPathwayLevel == 1)
		// {
		// refCurrentPathwayImageMap =
		// refGeneralManager.getSingelton().getPathwayManager().getCurrentPathwayImageMap();
		//			
		// loadBackgroundOverlayImage(refCurrentPathwayImageMap.getImageLink(),
		// refCurrentPathway);
		// }

		// Check if graph is already added to the frame
		if (bGraphSet == false)
		{
			// final Dimension dimOverviewMap = new Dimension(200, 200);
			final Dimension dimPathway = new Dimension(iWidth, iHeight);

			JScrollPane refScrollPane = new JScrollPane(refPathwayGraph);
			refScrollPane.setMinimumSize(dimPathway);
			refScrollPane.setMaximumSize(dimPathway);
			refScrollPane.setPreferredSize(dimPathway);
			refScrollPane.setAlignmentX(0.5f);
			refScrollPane.setAlignmentY(0.5f);
			refEmbeddedFrame.add(refScrollPane);

			refOverviewPanel = new GPOverviewPanel(refPathwayGraph,
					refScrollPane);

			// showOverviewMapInNewWindow(dimOverviewMap);

			bGraphSet = true;
		}
	}

	@SuppressWarnings("unchecked")
	public void createVertex(PathwayVertexGraphItemRep vertexRep,
			PathwayGraph refContainingPathway) {

		// create node
		DefaultGraphCell refGraphCell = new DefaultGraphCell(vertexRep);

		hashVertexRep2GraphCell.put(vertexRep, refGraphCell);

		AttributeMap changedMap = refGraphCell.getAttributes();

		EPathwayVertexShape shape = vertexRep.getShapeType();

		Rectangle2D vertexRect = null;

		if (shape.equals(EPathwayVertexShape.roundrectangle))
		{
			vertexRect = new Rectangle2D.Double(
						(int) ((vertexRep.getXPosition() - (vertexRep.getWidth() / 2)) * fScalingFactor),
						(int) ((vertexRep.getYPosition() - (vertexRep.getHeight() / 2)) * fScalingFactor),
						vertexRep.getWidth(), vertexRep.getHeight());
			
			// Set vertex type to round rect
			GPCellViewFactory.setViewClass(refGraphCell.getAttributes(),
					"cerberus.view.swt.pathway.jgraph.JGraphMultilineView");

			GraphConstants.setBackground(changedMap, refRenderStyle.getPathwayNodeColor(false));
		} 
		else if (shape.equals(EPathwayVertexShape.circle))
		{
			vertexRect = new Rectangle2D.Double(
						(int) ((vertexRep.getXPosition() - (vertexRep.getWidth() / 2)) * fScalingFactor),
						(int) ((vertexRep.getYPosition() - (vertexRep.getHeight() / 2)) * fScalingFactor),
						vertexRep.getWidth(), vertexRep.getHeight());
			
			// Set vertex type to ellipse
			GPCellViewFactory.setViewClass(refGraphCell.getAttributes(),
					"cerberus.view.swt.pathway.jgraph.JGraphEllipseView");

			if (!bShowBackgroundOverlay)
			{
				GraphConstants.setAutoSize(changedMap, true);
			}

			GraphConstants.setBackground(changedMap, refRenderStyle.getCompoundNodeColor(false));
		} 
		else if (shape.equals(EPathwayVertexShape.rectangle))
		{
			vertexRect = new Rectangle2D.Double(
						(int) ((vertexRep.getXPosition() - (vertexRep.getWidth() / 2)) * fScalingFactor),
						(int) ((vertexRep.getYPosition() - (vertexRep.getHeight() / 2)) * fScalingFactor),
						refRenderStyle.getEnzymeNodeWidth(false), refRenderStyle.getEnzymeNodeHeight(false));
			
			GraphConstants.setBackground(changedMap, refRenderStyle.getEnzymeNodeColor(false));
		}

		GraphConstants.setBounds(changedMap, vertexRect);

		// Some global attributes
		GraphConstants.setOpaque(changedMap, true);
		GraphConstants.setSelectable(changedMap, false);
		GraphConstants.setFont(changedMap, new Font("Arial", Font.BOLD, 11));
		// GraphConstants.setAutoSize(refGraphCell.getAttributes(), true);

		vecVertices.add(refGraphCell);

		vertexIdToCellLUT.put(vertexRep.getPathwayVertexGraphItem().getId(),
				refGraphCell);
	}

//	public void createEdge(int iVertexId1, int iVertexId2, boolean bDrawArrow,
//			APathwayEdge refPathwayEdge) {
//
//		DefaultPort port1 = new DefaultPort();
//		DefaultGraphCell cell1 = vertexIdToCellLUT.get(iVertexId1);
//
//		DefaultPort port2 = new DefaultPort();
//		DefaultGraphCell cell2 = vertexIdToCellLUT.get(iVertexId2);
//
//		if (cell1 == null || cell2 == null)
//		{
//			System.err.println("Unknown Error during creating edge! SKIP");
//			return;
//		}
//
//		cell1.add(port1);
//		cell2.add(port2);
//
//		DefaultEdge edge = new DefaultEdge(refPathwayEdge);
//		edge.setSource(cell1.getChildAt(0));
//		edge.setTarget(cell2.getChildAt(0));
//
//		// Retrieve existing edges between nodes
//		Object[] existingEdges = DefaultGraphModel.getEdgesBetween(
//				refGraphModel, edge.getSource(), edge.getTarget(), false);
//
//		// Return if edge of same type between two nodes already exists
//		for (int iEdgeCount = 0; iEdgeCount < existingEdges.length; iEdgeCount++)
//		{
//			if (((APathwayEdge) ((DefaultEdge) existingEdges[iEdgeCount])
//					.getUserObject()).getEdgeType() == refPathwayEdge
//					.getEdgeType())
//			{
//				return;
//			}
//		}
//
//		AttributeMap changedMap = edge.getAttributes();
//		EdgeLineStyle edgeLineStyle = null;
//		EdgeArrowHeadStyle edgeArrowHeadStyle = null;
//		Color edgeColor = null;
//
//		GraphConstants.setLineWidth(changedMap, 2);
//		GraphConstants.setSelectable(changedMap, false);
//		// GraphConstants.setRouting(changedMap,
//		// JGraphParallelRouter.getSharedInstance());
//		// GraphConstants.setRouting(edge.getAttributes(),
//		// GraphConstants.ROUTING_SIMPLE);
//
//		// Differentiate between Relations and Reactions
//		if (refPathwayEdge.getEdgeType() == EdgeType.REACTION)
//		{
//			edgeLineStyle = refRenderStyle.getReactionEdgeLineStyle();
//			edgeArrowHeadStyle = refRenderStyle.getReactionEdgeArrowHeadStyle();
//			edgeColor = refRenderStyle.getReactionEdgeColor();
//
//			GraphConstants.setLineColor(changedMap, edgeColor);
//
//			vecReactionEdges.add(edge);
//		} else if (refPathwayEdge.getEdgeType() == EdgeType.RELATION)
//		{
//			// In case when relations are maplinks
//			if (((PathwayRelationEdge) refPathwayEdge).getEdgeRelationType() == EdgeRelationType.maplink)
//			{
//				edgeLineStyle = refRenderStyle.getMaplinkEdgeLineStyle();
//				edgeArrowHeadStyle = refRenderStyle
//						.getMaplinkEdgeArrowHeadStyle();
//				edgeColor = refRenderStyle.getMaplinkEdgeColor();
//			} else
//			{
//				edgeLineStyle = refRenderStyle.getRelationEdgeLineStyle();
//				edgeArrowHeadStyle = refRenderStyle
//						.getRelationEdgeArrowHeadStyle();
//				edgeColor = refRenderStyle.getRelationEdgeColor();
//			}
//
//			GraphConstants.setLineColor(changedMap, edgeColor);
//
//			vecRelationEdges.add(edge);
//
//		}// (refPathwayEdge.getEdgeType() == EdgeType.RELATION)
//
//		// Assign render style
//		if (edgeLineStyle == EdgeLineStyle.DASHED)
//		{
//			GraphConstants.setDashPattern(changedMap, new float[]
//			{ 4, 4 });
//		}
//
//		// Draw arrow
//		if (bDrawArrow == true)
//		{
//			GraphConstants.setLineEnd(edge.getAttributes(),
//					GraphConstants.ARROW_TECHNICAL);
//		}
//
//		if (edgeArrowHeadStyle == EdgeArrowHeadStyle.FILLED)
//		{
//			GraphConstants.setEndFill(changedMap, true);
//		} else if (edgeArrowHeadStyle == EdgeArrowHeadStyle.EMPTY)
//		{
//			GraphConstants.setEndFill(changedMap, false);
//		}
//
//		refPathwayGraph.getGraphLayoutCache().insert(edge);
//	}

	public void finishGraphBuilding() {

		try
		{
			refPathwayGraph.getGraphLayoutCache().insert(vecVertices.toArray());
			// refPathwayGraph.getGraphLayoutCache().insert(
			// vecRelationEdges.toArray());
			// refPathwayGraph.getGraphLayoutCache().insert(
			// vecReactionEdges.toArray());

		} catch (NullPointerException npe)
		{
			refGeneralManager.getSingelton()
					.logMsg("Error while rendering JGraph part!",
							LoggerType.ERROR_ONLY);

			System.out.println("ERROR! : " + vecVertices.toString());

			npe.printStackTrace();
		}
	}

	public void loadPathwayFromFile(int iNewPathwayId) {

		//Load pathway
		boolean bLoadingOK = 
			refGeneralManager.getSingelton().getPathwayManager().loadPathwayById(iNewPathwayId);
		
		if (!bLoadingOK)
			return;

		// Clean up
		refCurrentPathway = null;
		refCurrentPathwayImageMap = null;
		lastClickedGraphCell = null;
		resetPathway();

		extractCurrentPathwayFromSet();

		refPathwayGraph.setBackgroundImage(null);

		showBackgroundOverlay(bShowBackgroundOverlay);
	}

	public void loadImageMapFromFile(String sImageMapPath) {

//		refCurrentPathway = null;
//		refCurrentPathwayImageMap = null;
//		resetPathway();
//
//		refGeneralManager.getSingelton().getXmlParserManager()
//				.parseXmlFileByName(sImageMapPath);
//
//		refCurrentPathwayImageMap = refGeneralManager.getSingelton()
//				.getPathwayManager().getCurrentPathwayImageMap();
//
//		loadBackgroundOverlayImage(refGeneralManager.getSingelton()
//				.getPathwayManager().getPathwayImagePath()
//				+ refCurrentPathwayImageMap.getImageLink());
	}

	public void zoomOrig() {

		refPathwayGraph.setScale(1.0);
	}

	public void zoomIn() {

		refPathwayGraph.setScale(1.2 * refPathwayGraph.getScale());
	}

	public void zoomOut() {

		refPathwayGraph.setScale(refPathwayGraph.getScale() / 1.2);
	}

	/**
	 * Method visualizes the neighborhood of a certain cell. The last clicked
	 * cell is stored in the lastClickedGraphCell member variable.
	 * 
	 * BFS Algorithm: for each vertex v in Q do for all edges e incident on v do
	 * if edge e is unexplored then let w be the other endpoint of e. if vertex
	 * w is unexpected then - mark e as a discovery edge - insert w into Q
	 * 
	 * @param iDistance
	 *            Neighborhood distance. because the method is called recursive.
	 */
	@SuppressWarnings("unchecked")
	public void showNeighbourhoodBFS(int iDistance) {

		Map<DefaultGraphCell, Map> nested = new Hashtable<DefaultGraphCell, Map>();
		Map attributeMap = new Hashtable();

		hashSetVisitedNeighbors.clear();
		// hashSetVisitedNeighbors.add(lastClickedGraphCell);

		ArrayList<DefaultGraphCell> queueBFS = new ArrayList<DefaultGraphCell>();
		ArrayList<DefaultGraphCell> queueBFSNext = new ArrayList<DefaultGraphCell>();
		queueBFS.add(lastClickedGraphCell);

		DefaultGraphCell tmpCell = null;
		DefaultEdge tmpEdge = null;
		List<DefaultGraphCell> neighbourCells = null;
		ArrayList<DefaultGraphCell> filteredNeighborCells = new ArrayList<DefaultGraphCell>();
		Iterator<DefaultGraphCell> iterCells = null;
		Color nodeColor = null;

		for (int iDistanceIndex = 0; iDistanceIndex <= iDistance; iDistanceIndex++)
		{
			iterCells = queueBFS.iterator();
			nested.clear();

			if (iDistanceIndex < PathwayRenderStyle.neighborhoodNodeColorArraysize)
			{
				nodeColor = refRenderStyle
						.getNeighborhoodNodeColorByDepth(iDistanceIndex);
			} else
			{
				assert false : "can not find color for selection depth";
			}

			GraphConstants.setBackground(attributeMap, nodeColor);

			while (iterCells.hasNext())
			{
				tmpCell = iterCells.next();

				if (!hashSetVisitedNeighbors.contains(tmpCell))
				{
					hashSetVisitedNeighbors.add(tmpCell);

					neighbourCells = refGraphLayoutCache.getNeighbours(tmpCell,
							hashSetVisitedNeighbors, false, false);

					List<DefaultEdge> listEdges = refGraphLayoutCache
							.getOutgoingEdges(tmpCell, null, false, false);

					listEdges.addAll(refGraphLayoutCache.getIncomingEdges(
							tmpCell, null, false, false));

					for (int iEdgeIndex = 0; iEdgeIndex < listEdges.size(); iEdgeIndex++)
					{
						tmpEdge = listEdges.get(iEdgeIndex);

						// Add cells from neighbors that are
						// connected by a visible edge
						if (refGraphLayoutCache.isVisible(tmpEdge) || bShowBackgroundOverlay)
						{
							if (neighbourCells.contains(((DefaultPort) (tmpEdge.getSource())).getParent()))
							{
								filteredNeighborCells.add((DefaultGraphCell)((DefaultPort) (tmpEdge.getSource())).getParent());
							} 
							else if (neighbourCells.contains(((DefaultPort) (tmpEdge.getTarget())).getParent()))
							{
								filteredNeighborCells.add((DefaultGraphCell)((DefaultPort) (tmpEdge.getTarget())).getParent());								
							}
						}
					}

					queueBFSNext.addAll(filteredNeighborCells);

					// Mark cell
					nested.put(tmpCell, attributeMap);

					// // Add selected vertex to selection arrays
					iLLSelectedVertices.add(((PathwayVertexGraphItemRep) tmpCell
							.getUserObject()).getPathwayVertexGraphItem().getId());
					iLLNeighborDistance.add(iDistanceIndex);
				}
			}

			refGraphLayoutCache.edit(nested, null, null, null);
			iNeighbourhoodUndoCount++;
			queueBFS = (ArrayList<DefaultGraphCell>) queueBFSNext.clone();
			queueBFSNext.clear();
		}

		return;
	}

	public void highlightCell(final DefaultGraphCell refCell, final Color color) {

		Map<DefaultGraphCell, Map> nested = new Hashtable<DefaultGraphCell, Map>();
		Map attributeMap = new Hashtable();

		GraphConstants.setBackground(attributeMap, color);

		nested.put(refCell, attributeMap);
		refGraphLayoutCache.edit(nested, null, null, null);
	}

	/**
	 * Methods puts the overview map in a new JFrame and displays the frame.
	 * 
	 * @param dim
	 */
	public void showOverviewMapInNewWindow(Dimension dim) {

		IViewGLCanvasManager refViewCanvasMng = refGeneralManager
				.getSingelton().getViewGLCanvasManager();
		JFrame workspaceFrame = refViewCanvasMng.createWorkspace(
				ManagerObjectType.VIEW_NEW_FRAME, "");

		JFrame wnd = (JFrame) workspaceFrame;
		wnd.setLocation(800, 500);
		wnd.setSize(dim);
		wnd.setVisible(true);

		wnd.add(refOverviewPanel);
	}

	public void setNeighbourhoodDistance(int iNeighbourhoodDistance) {

		this.iNeighbourhoodDistance = iNeighbourhoodDistance;

		// Update neighborhood visualization on the fly
		processSelectedCell();

		int[] iArSelectedVertices = null;
		int[] iArNeighborDistance = null;

		// Convert Link List to int[]
		Iterator<Integer> iter_I = iLLSelectedVertices.iterator();
		iArSelectedVertices = new int[iLLSelectedVertices.size()];
		for (int i = 0; iter_I.hasNext(); i++)
		{
			iArSelectedVertices[i] = iter_I.next().intValue();
		}

		iter_I = iLLNeighborDistance.iterator();
		iArNeighborDistance = new int[iLLNeighborDistance.size()];
		for (int i = 0; iter_I.hasNext(); i++)
		{
			iArNeighborDistance[i] = iter_I.next().intValue();
		}

		alSetSelection.get(0).updateSelectionSet(iParentContainerId,
				iArSelectedVertices, new int[0], iArNeighborDistance);
	}

//	public void showHideEdgesByType(boolean bShowEdges, EdgeType edgeType) {
//
//		refGraphModel.removeUndoableEditListener(refUndoManager);
//
//		if (edgeType == EdgeType.REACTION)
//		{
//			refGraphLayoutCache.setVisible(vecRelationEdges.toArray(),
//					bShowEdges);
//
//			bShowReactionEdges = bShowEdges;
//		} else if (edgeType == EdgeType.RELATION)
//		{
//			refGraphLayoutCache.setVisible(vecReactionEdges.toArray(),
//					bShowEdges);
//
//			bShowRelationEdges = bShowEdges;
//		}
//
//		refGraphModel.addUndoableEditListener(refUndoManager);
//		
//		processSelectedCell();
//	}
//
//	public boolean getEdgeVisibilityStateByType(EdgeType edgeType) {
//
//		if (edgeType == EdgeType.REACTION)
//		{
//			return (bShowReactionEdges);
//		} else if (edgeType == EdgeType.RELATION)
//		{
//			return (bShowRelationEdges);
//		}
//
//		assert false : "Invalid edge type specified!";
//		return false;
//	}

	public void showBackgroundOverlay(boolean bTurnOn) {

		if (refCurrentPathway == null)
			return;

		bShowBackgroundOverlay = bTurnOn;

		if (bShowBackgroundOverlay == true)
		{
			// Build current pathway file path of GIF
			String sPathwayImageFilePath = refCurrentPathway.getName();
			sPathwayImageFilePath = sPathwayImageFilePath.substring(5);
			sPathwayImageFilePath = refGeneralManager.getSingelton()
					.getPathwayManager().getPathwayImagePath()
					+ sPathwayImageFilePath + ".gif";

			refGeneralManager.getSingelton().logMsg(
					"Load background pathway from file: "
							+ sPathwayImageFilePath, LoggerType.VERBOSE);

			// Set background image
			refPathwayGraph.setBackgroundImage(new ImageIcon(
					sPathwayImageFilePath));

			// Set scaling factor so that background image is a direct overlay
			fScalingFactor = 1.0f;
		} else
		{
			refPathwayGraph.setBackgroundImage(null);
			fScalingFactor = SCALING_FACTOR;
		}

		resetPathway();
		hashVertexRep2GraphCell.clear();
		// extractCurrentPathwayFromSet();
		// Attention: Performance problem.
		drawView();

//		// Adapt edge visiblitly state
//		showHideEdgesByType(bShowReactionEdges, EdgeType.REACTION);
//		showHideEdgesByType(bShowRelationEdges, EdgeType.RELATION);

		if (lastClickedGraphCell != null)
		{
			// Check if selected cell is a vertex and if it is valid
			if (lastClickedGraphCell.getUserObject().getClass()
				.equals(PathwayVertexGraphItemRep.class))
			{
				// Map previously selected cell to new pathway JGraph.
				lastClickedGraphCell = hashVertexRep2GraphCell
						.get((PathwayVertexGraphItem) lastClickedGraphCell.getUserObject());

				// Rehighlight previously selected cells
				processSelectedCell();
			}
		}
	}

	public void resetPathway() {

		// refCurrentPathway = null;
		// refCurrentPathwayImageMap = null;

		refGraphModel = new DefaultGraphModel();
		refPathwayGraph.setModel(refGraphModel);
		refGraphLayoutCache.setModel(refGraphModel);

		// Recreate and register Undo Manager
		// refUndoManager = new GraphUndoManager();
		refGraphModel.addUndoableEditListener(refUndoManager);

		vecVertices.removeAllElements();
		vecRelationEdges.removeAllElements();
		vecReactionEdges.removeAllElements();

		iNeighbourhoodUndoCount = 0;
		bNeighbourhoodShown = false;
	}

	public void loadBackgroundOverlayImage(String sPathwayImageFilePath) {

		refGeneralManager.getSingelton().logMsg(
				"Load background pathway image from file: "
						+ sPathwayImageFilePath, LoggerType.VERBOSE);

		// Set background image
		refPathwayGraph
				.setBackgroundImage(new ImageIcon(sPathwayImageFilePath));

		// Set scaling factor so that background image is an direct overlay
		fScalingFactor = 1.0f;

		// // Set edges to visible
		// refGraphLayoutCache.setVisible(
		// vecReactionEdges.toArray(), false);
	}

	/**
	 * Method extracts the current pathway from the pathway storage and sets the
	 * local pathway.
	 * 
	 */
	protected void extractCurrentPathwayFromSet() {

		if (!alSetData.isEmpty())
		{
			// Assumes that the set consists of only one storage
			IStorage tmpStorage = ((SetFlatThreadSimple) alSetData.get(0))
				.getStorageByDimAndIndex(0, 0);

			// Assumes that the storage contains only one pathway item
			
			//Load pathway
			boolean bLoadingOK = 
				refGeneralManager.getSingelton()
					.getPathwayManager().loadPathwayById(tmpStorage.getArrayInt()[0]);
			
			if (!bLoadingOK)
				return;

			refCurrentPathway = (PathwayGraph) refGeneralManager.getSingelton()
				.getPathwayManager().getItem(tmpStorage.getArrayInt()[0]);
			
			return;
		}

		refGeneralManager.getSingelton().logMsg("no valid Set",
				LoggerType.ERROR_ONLY);
	}

	/**
	 * Method checks is the clicked URL is a pathway. If it is it calls the
	 * extraction method.
	 * 
	 * @param sUrl
	 * @return TRUE if a contained pathway was clicked.
	 */
	protected boolean extractClickedPathway(String sUrl) {

		int iPathwayIdIndex = 0;

		// Extract clicked pathway ID
		if (sUrl.contains("map0"))
		{
			iPathwayIdIndex = sUrl.lastIndexOf("map0") + 4;
		}
		else if (sUrl.contains("hsa0"))
		{
			iPathwayIdIndex = sUrl.lastIndexOf("hsa0") + 4;			
		}
		else
		{
			// Do nothing if pathway is not reference and not HSA
			return false;
		}
		
		int iNewPathwayId = StringConversionTool.convertStringToInt(sUrl
				.substring(iPathwayIdIndex, sUrl.lastIndexOf('.')), 0);
		
		// Load pathway
		loadPathwayFromFile(iNewPathwayId);
		
		triggerPathwayUpdate(iNewPathwayId);
		
		return true;
	}
	
	private void triggerPathwayUpdate(final int iPathwayId) 
	{
		// Trigger update with current pathway that dependent pathways 
		// know which pathway is currently under interaction
		IStorage refTmpStorage = alSetData.get(0).getStorageByDimAndIndex(0, 0);
		int[] tmp = new int[1];
		tmp[0] = iPathwayId;
		refTmpStorage.setArrayInt(tmp);
		alSetSelection.get(0).updateSelectionSet(iParentContainerId,
				new int[0], new int[0], tmp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cerberus.manager.event.mediator.IMediatorReceiver#updateReceiver(java.lang.Object,
	 *      cerberus.data.collection.ISet)
	 */
	public void updateReceiver(Object eventTrigger, ISet updatedSet) {

		ISetSelection refSetSelection = (ISetSelection) updatedSet;

		refGeneralManager.getSingelton().logMsg(
				"2D Pathway update called by "
						+ eventTrigger.getClass().getSimpleName(),
				LoggerType.VERBOSE);

		// TODO: Make own selection array for pathway ID 
		IStorage refTmpStorage = alSetData.get(0).getStorageByDimAndIndex(0, 0);
		int[] tmp = new int[1];
		tmp[0] = refSetSelection.getOptionalDataArray()[0];
		refTmpStorage.setArrayInt(tmp);
		loadPathwayFromFile(tmp[0]);

//		// Remove old selected vertices
//		iLLSelectedVertices.clear();
//		// iLLNeighborDistance.clear();
//
//		// Read selected vertex IDs
//		//int[] iArSelectedElements = refSetSelection.getSelectionIdArray();
//		
//		// Read neighbor data
//		// int[] iArSelectionNeighborDistance =
//		// selectionSet.getOptionalDataArray();
//
//		for (int iSelectedVertexIndex = 0; iSelectedVertexIndex < ((IStorage) refSetSelection
//				.getStorageByDimAndIndex(0, 0)).getSize(StorageType.INT); iSelectedVertexIndex++)
//		{
//
//			PathwayVertex selectedVertex = refGeneralManager.getSingelton()
//					.getPathwayElementManager().getVertexLUT().get(
//							iArSelectedElements[iSelectedVertexIndex]);
//			
//			// FIXME: name of the method is not good because inside
//			// resetPathway() and drawPathway() are called.
//			showBackgroundOverlay(bShowBackgroundOverlay);
//
//			// //ATTENTION: Performance problem!
//			// resetPathway();
//			// drawView();
//
//			// Ignore vertex if is NOT in the current pathway!
//			if (!refCurrentPathway.isVertexInPathway(selectedVertex))
//				return;
//
//			iLLSelectedVertices.add(selectedVertex.getElementId());
//
//			highlightCell(hashVertexRep2GraphCell.get(selectedVertex
//					.getVertexRepByIndex(0)), refRenderStyle.getHighlightedNodeColor());
//
//			bNeighbourhoodShown = true;
//			iNeighbourhoodUndoCount++;
//
//		}
	}
}
