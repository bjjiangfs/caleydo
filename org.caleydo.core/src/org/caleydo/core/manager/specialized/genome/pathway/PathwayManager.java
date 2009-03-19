package org.caleydo.core.manager.specialized.genome.pathway;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.caleydo.core.data.graph.pathway.core.PathwayGraph;
import org.caleydo.core.manager.AManager;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.specialized.genome.IPathwayManager;
import org.caleydo.core.parser.xml.sax.handler.specialized.pathway.PathwayImageMap;
import org.caleydo.util.graph.EGraphItemHierarchy;
import org.caleydo.util.graph.core.Graph;

/**
 * The pathway manager is in charge of creating and handling the pathways. The class is implemented as a
 * singleton.
 * 
 * @author Marc Streit
 */
public class PathwayManager
	extends AManager<PathwayGraph>
	implements IPathwayManager, Serializable {
	private static final long serialVersionUID = 1L;

	private HashMap<Integer, Boolean> hashPathwayIdToVisibilityState;

	private HashMap<String, Integer> hashPathwayTitleToPathwayId;

	private HashMap<EPathwayDatabaseType, PathwayDatabase> hashPathwayDatabase;

	/**
	 * Root pathway contains all nodes that are loaded into the system. Therefore it represents the overall
	 * topological network. (The root pathway is independent from the representation of the nodes.)
	 */
	private Graph rootPathwayGraph;

	/**
	 * Used for pathways where only images can be loaded. The image map defines the clickable regions on that
	 * pathway image.
	 */
	private PathwayImageMap currentPathwayImageMap;

	private PathwayGraph currentPathwayGraph;

	private boolean bIsPathwayLoadingFinished;

	/**
	 * Constructor.
	 */
	public PathwayManager() {
		hashPathwayTitleToPathwayId = new HashMap<String, Integer>();
		hashPathwayDatabase = new HashMap<EPathwayDatabaseType, PathwayDatabase>();
		hashPathwayIdToVisibilityState = new HashMap<Integer, Boolean>();

		rootPathwayGraph = new Graph(0);
	}

	public void createPathwayDatabase(final EPathwayDatabaseType type, final String sXMLPath,
		final String sImagePath, final String sImageMapPath) {
		// Check if requested pathway database is already loaded (e.g. using
		// caching)
		if (hashPathwayDatabase.containsKey(type))
			return;

		PathwayDatabase tmpPathwayDatabase = new PathwayDatabase(type, sXMLPath, sImagePath, sImagePath);

		hashPathwayDatabase.put(type, tmpPathwayDatabase);

		GeneralManager.get().getLogger().log(
			Level.INFO,
			"Setting pathway loading path: database-type:[" + type + "] " + "xml-path:["
				+ tmpPathwayDatabase.getXMLPath() + "] image-path:[" + tmpPathwayDatabase.getImagePath()
				+ "] image-map-path:[" + tmpPathwayDatabase.getImageMapPath() + "]");
	}

	@Override
	public void triggerParsingPathwayDatabases() {
		new PathwayLoaderThread(hashPathwayDatabase.values());
	}

	@Override
	public PathwayGraph createPathway(final EPathwayDatabaseType type, final String sName,
		final String sTitle, final String sImageLink, final String sExternalLink) {
		PathwayGraph pathway = new PathwayGraph(type, sName, sTitle, sImageLink, sExternalLink);

		registerItem(pathway);
		hashPathwayTitleToPathwayId.put(sTitle, pathway.getID());
		hashPathwayIdToVisibilityState.put(pathway.getID(), false);

		rootPathwayGraph.addGraph(pathway, EGraphItemHierarchy.GRAPH_CHILDREN);

		currentPathwayGraph = pathway;

		return pathway;
	}

	@Override
	public int searchPathwayIdByName(final String sPathwayName, EPathwayDatabaseType ePathwayDatabaseType) {
		waitUntilPathwayLoadingIsFinished();

		Iterator<String> iterPathwayName = hashPathwayTitleToPathwayId.keySet().iterator();
		Pattern pattern = Pattern.compile(sPathwayName, Pattern.CASE_INSENSITIVE);
		Matcher regexMatcher;
		String sTmpPathwayName;

		while (iterPathwayName.hasNext()) {
			sTmpPathwayName = iterPathwayName.next();
			regexMatcher = pattern.matcher(sTmpPathwayName);

			if (regexMatcher.find()) {
				int iPathwayID = hashPathwayTitleToPathwayId.get(sTmpPathwayName);

				// Ignore the found pathway if it has the same name but is
				// contained
				// in a different database
				if (getItem(iPathwayID).getType() != ePathwayDatabaseType) {
					continue;
				}

				return iPathwayID;
			}
		}

		return -1;
	}

	public Graph getRootPathway() {
		return rootPathwayGraph;
	}

	@Override
	public Collection<PathwayGraph> getAllItems() {
		waitUntilPathwayLoadingIsFinished();

		return super.getAllItems();
	}

	@Override
	public void setPathwayVisibilityStateByID(final int iPathwayID, final boolean bVisibilityState) {
		waitUntilPathwayLoadingIsFinished();

		hashPathwayIdToVisibilityState.put(iPathwayID, bVisibilityState);
	}

	@Override
	public void resetPathwayVisiblityState() {
		waitUntilPathwayLoadingIsFinished();

		for (int iPathwayID : hashPathwayIdToVisibilityState.keySet()) {
			hashPathwayIdToVisibilityState.put(iPathwayID, false);
		}
	}

	@Override
	public boolean isPathwayVisible(final int iPathwayID) {
		waitUntilPathwayLoadingIsFinished();

		return hashPathwayIdToVisibilityState.get(iPathwayID);
	}

	@Override
	public void createPathwayImageMap(final String sImageLink) {
		currentPathwayImageMap = new PathwayImageMap(sImageLink);
	}

	@Override
	public PathwayDatabase getPathwayDatabaseByType(EPathwayDatabaseType type) {
		return hashPathwayDatabase.get(type);
	}

	protected PathwayGraph getCurrenPathwayGraph() {
		return currentPathwayGraph;
	}

	@Override
	public PathwayImageMap getCurrentPathwayImageMap() {
		return currentPathwayImageMap;
	}

	public void notifyPathwayLoadingFinished(boolean bIsPathwayLoadingFinisched) {
		this.bIsPathwayLoadingFinished = bIsPathwayLoadingFinisched;
	}

	public void waitUntilPathwayLoadingIsFinished() {
		while (!bIsPathwayLoadingFinished) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				throw new IllegalThreadStateException("Pathway loader thread has been interrupted!");
			}
		}
	}
}
