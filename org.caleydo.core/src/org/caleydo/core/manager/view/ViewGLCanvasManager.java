package org.caleydo.core.manager.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import javax.media.opengl.GLEventListener;
import javax.swing.JFrame;
import org.caleydo.core.command.ECommandType;
import org.caleydo.core.data.collection.ESetType;
import org.caleydo.core.manager.AManager;
import org.caleydo.core.manager.IViewGLCanvasManager;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.id.EManagedObjectType;
import org.caleydo.core.manager.picking.PickingManager;
import org.caleydo.core.view.IView;
import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLEventListener;
import org.caleydo.core.view.opengl.canvas.GLCaleydoCanvas;
import org.caleydo.core.view.opengl.canvas.cell.GLCell;
import org.caleydo.core.view.opengl.canvas.glyph.gridview.GLGlyph;
import org.caleydo.core.view.opengl.canvas.glyph.sliderview.GLGlyphSliderView;
import org.caleydo.core.view.opengl.canvas.panel.GLSelectionPanel;
import org.caleydo.core.view.opengl.canvas.pathway.GLPathway;
import org.caleydo.core.view.opengl.canvas.remote.GLRemoteRendering;
import org.caleydo.core.view.opengl.canvas.remote.glyph.GLRemoteGlyph;
import org.caleydo.core.view.opengl.canvas.storagebased.heatmap.GLHeatMap;
import org.caleydo.core.view.opengl.canvas.storagebased.heatmap.GLHierarchicalHeatMap;
import org.caleydo.core.view.opengl.canvas.storagebased.parcoords.GLParallelCoordinates;
import org.caleydo.core.view.opengl.canvas.wii.GLCanvasWiiTest;
import org.caleydo.core.view.opengl.renderstyle.layout.ARemoteViewLayoutRenderStyle;
import org.caleydo.core.view.opengl.util.infoarea.GLInfoAreaManager;
import org.caleydo.core.view.swt.browser.GenomeHTMLBrowserViewRep;
import org.caleydo.core.view.swt.browser.HTMLBrowserViewRep;
import org.caleydo.core.view.swt.data.search.DataEntitySearcherViewRep;
import org.caleydo.core.view.swt.glyph.GlyphDataExportViewRep;
import org.caleydo.core.view.swt.glyph.GlyphMappingConfigurationViewRep;
import org.caleydo.core.view.swt.image.ImageViewRep;
import org.caleydo.core.view.swt.jogl.SwtJoglGLCanvasViewRep;
import org.caleydo.core.view.swt.mixer.MixerViewRep;
import org.caleydo.core.view.swt.tabular.TabularDataViewRep;
import org.caleydo.core.view.swt.undoredo.UndoRedoViewRep;
import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;

/**
 * Manage all canvas, view, ViewReps and GLCanvas objects.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 * @author Alexander Lex
 */
public class ViewGLCanvasManager
	extends AManager<IView>
	implements IViewGLCanvasManager
{
	protected HashMap<Integer, GLCaleydoCanvas> hashGLCanvasID2GLCanvas;

	protected HashMap<Integer, ArrayList<AGLEventListener>> hashGLCanvasID2GLEventListeners;

	protected HashMap<Integer, AGLEventListener> hashGLEventListenerID2GLEventListener;

	protected ArrayList<JFrame> arWorkspaceJFrame;

	private Animator fpsAnimator;

	private DataEntitySearcherViewRep dataEntitySearcher;

	private PickingManager pickingManager;

	private ConnectedElementRepresentationManager selectionManager;

	private GLInfoAreaManager infoAreaManager;

	/**
	 * Constructor.
	 * 
	 */
	public ViewGLCanvasManager()
	{
		pickingManager = new PickingManager();
		selectionManager = new ConnectedElementRepresentationManager();
		infoAreaManager = new GLInfoAreaManager();

		hashGLCanvasID2GLCanvas = new HashMap<Integer, GLCaleydoCanvas>();
		hashGLCanvasID2GLEventListeners = new HashMap<Integer, ArrayList<AGLEventListener>>();
		hashGLEventListenerID2GLEventListener = new HashMap<Integer, AGLEventListener>();

		arWorkspaceJFrame = new ArrayList<JFrame>();

		fpsAnimator = new FPSAnimator(null, 60);
	}

	@Override
	public boolean hasItem(int iItemId)
	{

		if (hashItems.containsKey(iItemId))
			return true;

		if (hashGLCanvasID2GLCanvas.containsKey(iItemId))
			return true;

		if (hashGLEventListenerID2GLEventListener.containsKey(iItemId))
			return true;

		return false;
	}

	public GLCaleydoCanvas getCanvas(int iItemID)
	{
		return hashGLCanvasID2GLCanvas.get(iItemID);
	}

	public AGLEventListener getGLEventListener(int iItemID)
	{
		return hashGLEventListenerID2GLEventListener.get(iItemID);
	}

	@Override
	public void registerItem(IView view)
	{
		super.registerItem(view);

		switch (view.getViewType())
		{
			case SWT_DATA_EXPLORER:
			case SWT_HTML_BROWSER:
				this.addViewRep(view);
				break;

			// default: // do nothing
		}
	}

	@Override
	public IView createView(final EManagedObjectType type, final int iParentContainerID,
			final String sLabel)
	{
		IView view = null;

		switch (type)
		{
			case VIEW:
				break;
			case VIEW_SWT_PATHWAY:
				// return new Pathway2DViewRep(generalManager, iViewID,
				// iParentContainerID, sLabel);
				break;
			case VIEW_SWT_TABULAR_DATA_VIEWER:
				view = new TabularDataViewRep(iParentContainerID, sLabel);
				break;
			case VIEW_SWT_MIXER:
				view = new MixerViewRep(iParentContainerID, sLabel);
				break;
			case VIEW_SWT_BROWSER_GENERAL:
				view = new HTMLBrowserViewRep(iParentContainerID, sLabel);
				break;
			case VIEW_SWT_BROWSER_GENOME:
				view = new GenomeHTMLBrowserViewRep(iParentContainerID, sLabel);
				break;
			case VIEW_SWT_IMAGE:
				view = new ImageViewRep(iParentContainerID, sLabel);
				break;
			case VIEW_SWT_UNDO_REDO:
				view = new UndoRedoViewRep(iParentContainerID, sLabel);
				break;
			case VIEW_SWT_DATA_ENTITY_SEARCHER:
				view = new DataEntitySearcherViewRep(iParentContainerID, sLabel);
				break;
			case VIEW_SWT_GLYPH_DATAEXPORT:
				view = new GlyphDataExportViewRep(iParentContainerID, sLabel);
				break;
			case VIEW_SWT_GLYPH_MAPPINGCONFIGURATION:
				view = new GlyphMappingConfigurationViewRep(iParentContainerID, sLabel);
				break;
			default:
				throw new RuntimeException(
						"StorageManagerSimple.createView() failed due to unhandled type ["
								+ type.toString() + "]");
		}

		registerItem(view);

		return view;
	}

	@Override
	public IView createGLView(final EManagedObjectType useViewType,
			final int iParentContainerID, final String sLabel)
	{
		IView view = null;

		switch (useViewType)
		{
			case VIEW_GL_CANVAS:
				view = new SwtJoglGLCanvasViewRep(iParentContainerID, sLabel);
				break;

			default:
				throw new RuntimeException("Unhandled view type [" + useViewType.toString()
						+ "]");
		}

		registerItem(view);

		return view;
	}

	@Override
	public AGLEventListener createGLEventListener(ECommandType type, final int iGLCanvasID,
			final String sLabel, final IViewFrustum viewFrustum)
	{
		GeneralManager.get().getLogger().log(
				Level.INFO,
				"Creating GL canvas view from type: [" + type + "] and label: [" + sLabel
						+ "]");

		AGLEventListener glEventListener = null;

		switch (type)
		{
			case CREATE_GL_HEAT_MAP_3D:
				glEventListener = new GLHeatMap(ESetType.GENE_EXPRESSION_DATA, iGLCanvasID,
						sLabel, viewFrustum);
				break;
			case CREATE_GL_TEXTURE_HEAT_MAP_3D:
				glEventListener = new GLHierarchicalHeatMap(ESetType.GENE_EXPRESSION_DATA,
						iGLCanvasID, sLabel, viewFrustum);
				break;

			case CREATE_GL_PATHWAY_3D:
				glEventListener = new GLPathway(iGLCanvasID, sLabel, viewFrustum);
				break;

			case CREATE_GL_PARALLEL_COORDINATES_GENE_EXPRESSION:
				glEventListener = new GLParallelCoordinates(ESetType.GENE_EXPRESSION_DATA,
						iGLCanvasID, sLabel, viewFrustum);
				break;

			case CREATE_GL_PARALLEL_COORDINATES_CLINICAL:
				glEventListener = new GLParallelCoordinates(ESetType.CLINICAL_DATA,
						iGLCanvasID, sLabel, viewFrustum);
				break;
			case CREATE_GL_GLYPH:
				glEventListener = new GLGlyph(iGLCanvasID, sLabel, viewFrustum);
				break;

			case CREATE_GL_GLYPH_SLIDER:
				glEventListener = new GLGlyphSliderView(iGLCanvasID, sLabel, viewFrustum);
				break;

			case CREATE_GL_CELL:
				glEventListener = new GLCell(iGLCanvasID, sLabel, viewFrustum);
				break;

			case CREATE_GL_BUCKET_3D:
				glEventListener = new GLRemoteRendering(iGLCanvasID, sLabel, viewFrustum,
						ARemoteViewLayoutRenderStyle.LayoutMode.BUCKET);
				break;

			case CREATE_GL_JUKEBOX_3D:
				glEventListener = new GLRemoteRendering(iGLCanvasID, sLabel, viewFrustum,
						ARemoteViewLayoutRenderStyle.LayoutMode.JUKEBOX);
				break;

			case CREATE_GL_WII_TEST:
				glEventListener = new GLCanvasWiiTest(iGLCanvasID, sLabel, viewFrustum);
				break;

			case CREATE_GL_REMOTE_GLYPH:
				glEventListener = new GLRemoteGlyph(iGLCanvasID, sLabel, viewFrustum);
				break;

			case CREATE_GL_PANEL_SELECTION:
				glEventListener = new GLSelectionPanel(iGLCanvasID, sLabel, viewFrustum);
				break;

			default:
				throw new RuntimeException(
						"ViewJoglManager.createGLCanvasUser() failed due to unhandled type ["
								+ type.toString() + "]");
		}

		registerGLEventListenerByGLCanvasID(iGLCanvasID, glEventListener);

		return glEventListener;
	}

	@Override
	public boolean registerGLCanvas(final GLCaleydoCanvas glCanvas)
	{
		int iGLCanvasID = glCanvas.getID();

		if (hashGLCanvasID2GLCanvas.containsKey(iGLCanvasID))
		{
			generalManager.getLogger()
					.log(
							Level.WARNING,
							"GL Canvas with ID " + iGLCanvasID
									+ " is already registered! Do nothing.");

			return false;
		}

		hashGLCanvasID2GLCanvas.put(iGLCanvasID, glCanvas);
		fpsAnimator.add(glCanvas);

		return true;
	}

	@Override
	public boolean unregisterGLCanvas(final int iGLCanvasId)
	{
		if (hashGLCanvasID2GLCanvas.containsKey(iGLCanvasId))
		{
			fpsAnimator.remove(hashGLCanvasID2GLCanvas.get(iGLCanvasId));

			hashGLCanvasID2GLCanvas.remove(iGLCanvasId);
			hashGLCanvasID2GLEventListeners.remove(iGLCanvasId);
		}

		return false;
	}

	@Override
	public void registerGLEventListenerByGLCanvasID(final int iGLCanvasID,
			final AGLEventListener gLEventListener)
	{
		hashGLEventListenerID2GLEventListener.put((gLEventListener).getID(), gLEventListener);

		if (iGLCanvasID == -1)
			return;

		if (!hashGLCanvasID2GLEventListeners.containsKey(iGLCanvasID))
			hashGLCanvasID2GLEventListeners
					.put(iGLCanvasID, new ArrayList<AGLEventListener>());

		hashGLCanvasID2GLEventListeners.get(iGLCanvasID).add(gLEventListener);
	}

	@Override
	public void cleanup()
	{

		hashGLCanvasID2GLCanvas.clear();
		hashGLCanvasID2GLEventListeners.clear();
		hashGLEventListenerID2GLEventListener.clear();
		hashItems.clear();
	}

	@Override
	public void unregisterGLEventListener(final int iGLEventListenerID)
	{
		GLEventListener gLEventListenerToRemove = hashGLEventListenerID2GLEventListener
				.get(iGLEventListenerID);

		GLCaleydoCanvas parentGLCanvas = ((AGLEventListener) gLEventListenerToRemove)
				.getParentGLCanvas();

		if (parentGLCanvas != null)
		{
			parentGLCanvas.removeGLEventListener(gLEventListenerToRemove);

			if (hashGLCanvasID2GLEventListeners.containsKey(parentGLCanvas.getID()))
				hashGLCanvasID2GLEventListeners.get(parentGLCanvas.getID()).remove(
						gLEventListenerToRemove);
		}

		hashGLEventListenerID2GLEventListener.remove(iGLEventListenerID);
	}

	public void addViewRep(final IView view)
	{

		try
		{
			switch (view.getViewType())
			{
				case SWT_DATA_ENTITY_SEARCHER:
					dataEntitySearcher = (DataEntitySearcherViewRep) view;

				default:
					assert false : "unsupported ViewType " + view.getViewType();
			} // switch ( view.getViewType() )
		}
		catch (NullPointerException npe)
		{
			System.err.println("addViewRep(IView) getViewType() returned unexpected (class)!");

			assert false : "Error,  getViewType() returned unexpected (class)";
		} // try .. catch ( NullPointerException npe)
	}

	@Override
	public void removeViewRep(final IView view)
	{
		try
		{
			switch (view.getViewType())
			{

				case SWT_DATA_EXPLORER:
					// arDataExplorerViewRep.remove( (DataExplorerViewRep)
					// view);
					return;
				case SWT_HTML_BROWSER:
					// arDataExplorerViewRep.remove( (DataExplorerViewRep)
					// view);
					return;

				default:
					assert false : "unsupported ViewType " + view.getViewType();
			} // switch ( view.getViewType() )

		}
		catch (NullPointerException npe)
		{
			System.err
					.println("removedViewRep(IView) getViewType() returned unexpected (class)!");

			assert false : "Error,  getViewType() returned unexpected (class)";
		} // try .. catch ( NullPointerException npe)
	}

	@Override
	public Collection<GLCaleydoCanvas> getAllGLCanvasUsers()
	{
		return hashGLCanvasID2GLCanvas.values();
	}

	@Override
	public Collection<AGLEventListener> getAllGLEventListeners()
	{
		return hashGLEventListenerID2GLEventListener.values();
	}

	public DataEntitySearcherViewRep getDataEntitySearcher()
	{

		return dataEntitySearcher;
	}

	public PickingManager getPickingManager()
	{

		return pickingManager;
	}

	public ConnectedElementRepresentationManager getConnectedElementRepresentationManager()
	{

		return selectionManager;
	}

	public GLInfoAreaManager getInfoAreaManager()
	{

		return infoAreaManager;
	}

	@Override
	public void startAnimator()
	{
		// // add all canvas objects before starting animator
		// // this is needed because all the views are fully filled with needed
		// data at that time.
		// for (GLCaleydoCanvas glCanvas : hashGLCanvasID2GLCanvas.values())
		// {
		// fpsAnimator.add(glCanvas);
		// }

		fpsAnimator.start();
	}
}
