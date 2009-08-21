package org.caleydo.core.view.opengl.canvas.radial;

import gleem.linalg.Vec2f;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.xml.bind.JAXBException;

import org.caleydo.core.data.graph.tree.Tree;
import org.caleydo.core.data.graph.tree.TreePorter;
import org.caleydo.core.data.mapping.EIDType;
import org.caleydo.core.data.selection.ESelectionType;
import org.caleydo.core.data.selection.EVAOperation;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.delta.ISelectionDelta;
import org.caleydo.core.data.selection.delta.SelectionDelta;
import org.caleydo.core.data.selection.delta.SelectionDeltaItem;
import org.caleydo.core.manager.event.view.ClearSelectionsEvent;
import org.caleydo.core.manager.event.view.ClusterNodeSelectionEvent;
import org.caleydo.core.manager.event.view.radial.ChangeColorModeEvent;
import org.caleydo.core.manager.event.view.radial.DetailOutsideEvent;
import org.caleydo.core.manager.event.view.radial.GoBackInHistoryEvent;
import org.caleydo.core.manager.event.view.radial.GoForthInHistoryEvent;
import org.caleydo.core.manager.event.view.radial.SetMaxDisplayedHierarchyDepthEvent;
import org.caleydo.core.manager.event.view.storagebased.RedrawViewEvent;
import org.caleydo.core.manager.event.view.storagebased.SelectionUpdateEvent;
import org.caleydo.core.manager.event.view.storagebased.UpdateViewEvent;
import org.caleydo.core.manager.id.EManagedObjectType;
import org.caleydo.core.manager.picking.EPickingMode;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.Pick;
import org.caleydo.core.manager.usecase.EUseCaseMode;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.clusterer.ClusterNode;
import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLEventListener;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.GLCaleydoCanvas;
import org.caleydo.core.view.opengl.canvas.listener.ClearSelectionsListener;
import org.caleydo.core.view.opengl.canvas.listener.ClusterNodeSelectionListener;
import org.caleydo.core.view.opengl.canvas.listener.IClusterNodeEventReceiver;
import org.caleydo.core.view.opengl.canvas.listener.ISelectionUpdateHandler;
import org.caleydo.core.view.opengl.canvas.listener.IViewCommandHandler;
import org.caleydo.core.view.opengl.canvas.listener.RedrawViewListener;
import org.caleydo.core.view.opengl.canvas.listener.SelectionUpdateListener;
import org.caleydo.core.view.opengl.canvas.listener.UpdateViewListener;
import org.caleydo.core.view.opengl.canvas.radial.listener.ChangeColorModeListener;
import org.caleydo.core.view.opengl.canvas.radial.listener.DetailOutsideListener;
import org.caleydo.core.view.opengl.canvas.radial.listener.GoBackInHistoryListener;
import org.caleydo.core.view.opengl.canvas.radial.listener.GoForthInHistoryListener;
import org.caleydo.core.view.opengl.canvas.radial.listener.SetMaxDisplayedHierarchyDepthListener;
import org.caleydo.core.view.opengl.canvas.remote.IGLCanvasRemoteRendering;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.util.overlay.contextmenu.container.GeneContextMenuItemContainer;
import org.caleydo.core.view.opengl.util.overlay.contextmenu.item.DetailOutsideItem;
import org.caleydo.core.view.opengl.util.overlay.infoarea.GLInfoAreaManager;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;

/**
 * This class is responsible for rendering the radial hierarchy and receiving user events and events from
 * other views.
 * 
 * @author Christian Partl
 */
public class GLRadialHierarchy
	extends AGLEventListener
	implements IClusterNodeEventReceiver, IViewCommandHandler, ISelectionUpdateHandler {

	public static final int DISP_HIER_DEPTH_DEFAULT = 7;
	private static final int MIN_PIXELS_PER_DISPLAYED_LEVEL = 10;
	private static final int MIN_DISPLAY_WIDTH = 350;
	private static final int MIN_DISPLAY_HEIGHT = 300;

	private int iMaxDisplayedHierarchyDepth;
	private int iUpwardNavigationSliderID;
	private int iUpwardNavigationSliderButtonID;
	private int iUpwardNavigationSliderBodyID;

	private boolean bIsAnimationActive;
	private boolean bIsNewSelection;

	/**
	 * Tree where all partial discs are stored.
	 */
	private Tree<PartialDisc> partialDiscTree;
	/**
	 * Hashmap for partial disc picking.
	 */
	private HashMap<Integer, PartialDisc> hashPartialDiscs;

	private PartialDisc pdRealRootElement;
	private PartialDisc pdCurrentRootElement;
	private PartialDisc pdCurrentSelectedElement;

	private DrawingController drawingController;
	private NavigationHistory navigationHistory;
	private OneWaySlider upwardNavigationSlider;
	private Rectangle controlBox;

	private ClusterNodeSelectionListener clusterNodeMouseOverListener;
	private RedrawViewListener redrawViewListener;
	private GoBackInHistoryListener goBackInHistoryListener;
	private GoForthInHistoryListener goForthInHistoryListener;
	private ChangeColorModeListener changeColorModeListener;
	private SetMaxDisplayedHierarchyDepthListener setMaxDisplayedHierarchyDepthListener;
	private DetailOutsideListener detailOutsideListener;
	private UpdateViewListener updateViewListener;
	private SelectionUpdateListener selectionUpdateListener;
	private ClearSelectionsListener clearSelectionsListener;

	private SelectionManager selectionManager;
	boolean bUseDetailLevel = true;

	// ISet set;

	/**
	 * Constructor.
	 */
	public GLRadialHierarchy(GLCaleydoCanvas glCanvas, final String sLabel, final IViewFrustum viewFrustum) {
		super(glCanvas, sLabel, viewFrustum, true);

		viewType = EManagedObjectType.GL_RADIAL_HIERARCHY;

		ArrayList<ESelectionType> alSelectionTypes = new ArrayList<ESelectionType>();
		alSelectionTypes.add(ESelectionType.NORMAL);
		alSelectionTypes.add(ESelectionType.MOUSE_OVER);
		alSelectionTypes.add(ESelectionType.SELECTION);

		renderStyle = new RadialHierarchyRenderStyle(viewFrustum);
		renderStyle.setMinViewDimensions(MIN_DISPLAY_WIDTH + MIN_PIXELS_PER_DISPLAYED_LEVEL
			* DISP_HIER_DEPTH_DEFAULT, MIN_DISPLAY_HEIGHT + MIN_PIXELS_PER_DISPLAYED_LEVEL
			* DISP_HIER_DEPTH_DEFAULT, this);

		hashPartialDiscs = new HashMap<Integer, PartialDisc>();
		partialDiscTree = new Tree<PartialDisc>();
		// iMaxDisplayedHierarchyDepth = DISP_HIER_DEPTH_DEFAULT;
		navigationHistory = new NavigationHistory(this, null);
		drawingController = new DrawingController(this, navigationHistory);
		DrawingStrategyManager.init(pickingManager, iUniqueID);
		navigationHistory.setDrawingController(drawingController);
		iUpwardNavigationSliderButtonID = 0;
		iUpwardNavigationSliderID = 0;
		iUpwardNavigationSliderBodyID = 0;

		selectionManager = new SelectionManager.Builder(EIDType.CLUSTER_NUMBER).build();

		glKeyListener = new GLRadialHierarchyKeyListener(this);

		bIsAnimationActive = false;
		// bIsNewSelection = false;
	}

	@Override
	public void init(GL gl) {
		Tree<ClusterNode> tree = set.getClusteredTreeGenes();
		if (tree != null) {
			// initHierarchy(tree);
		}
		else {
			// initTestHierarchy();
		}

		gl.glEnable(GL.GL_LINE_SMOOTH);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		if (set == null)
			return;
	}

	@Override
	public void initLocal(GL gl) {

		iGLDisplayListIndexLocal = gl.glGenLists(1);
		iGLDisplayListToCall = iGLDisplayListIndexLocal;

		// Register keyboard listener to GL canvas
		parentGLCanvas.getParentComposite().getDisplay().asyncExec(new Runnable() {
			public void run() {
				parentGLCanvas.getParentComposite().addKeyListener(glKeyListener);
			}
		});

		init(gl);
	}

	@Override
	public void initRemote(final GL gl, final AGLEventListener glParentView,
		final GLMouseListener glMouseListener, final IGLCanvasRemoteRendering remoteRenderingGLCanvas,
		GLInfoAreaManager infoAreaManager) {

		// Register keyboard listener to GL canvas
		glParentView.getParentGLCanvas().getParentComposite().getDisplay().asyncExec(new Runnable() {
			public void run() {
				glParentView.getParentGLCanvas().getParentComposite().addKeyListener(glKeyListener);
			}
		});

		this.remoteRenderingGLView = remoteRenderingGLCanvas;

		this.glMouseListener = glMouseListener;

		iGLDisplayListIndexRemote = gl.glGenLists(1);
		iGLDisplayListToCall = iGLDisplayListIndexRemote;
		init(gl);

	}

	/**
	 * Initializes a new hierarchy of partial discs according to a tree of cluster nodes.
	 * 
	 * @param tree
	 *            Tree of cluster nodes which is used to build the partial disc tree.
	 */
	public void initHierarchy(Tree<ClusterNode> tree) {

		// wpuff: default is set in constructor, possible initialization from serialized-form

		hashPartialDiscs.clear();
		selectionManager.resetSelectionManager();
		partialDiscTree = new Tree<PartialDisc>();
		navigationHistory.reset();
		drawingController.setDrawingState(EDrawingStateType.DRAWING_STATE_FULL_HIERARCHY);
		LabelManager.get().clearLabels();
		DrawingStrategyManager.init(pickingManager, iUniqueID);

		ClusterNode cnRoot = tree.getRoot();
		PartialDisc pdRoot = new PartialDisc(partialDiscTree, cnRoot);
		partialDiscTree.setRootNode(pdRoot);
		hashPartialDiscs.put(cnRoot.getClusterNr(), pdRoot);
		selectionManager.initialAdd(cnRoot.getClusterNr());
		buildTree(tree, cnRoot, pdRoot);
		pdRoot.calculateHierarchyLevels(0);
		pdRoot.calculateLargestChildren();
		iMaxDisplayedHierarchyDepth = DISP_HIER_DEPTH_DEFAULT;

		pdCurrentRootElement = pdRoot;
		pdCurrentSelectedElement = pdRoot;
		pdRealRootElement = pdRoot;

		navigationHistory.addNewHistoryEntry(drawingController.getCurrentDrawingState(),
			pdCurrentRootElement, pdCurrentSelectedElement, iMaxDisplayedHierarchyDepth);

		selectionManager.addToType(ESelectionType.SELECTION, pdCurrentRootElement.getElementID());

		controlBox = new Rectangle(0, 0, 0.3f, 0.2f);
		upwardNavigationSlider =
			new OneWaySlider(new Vec2f(controlBox.getMinX() + 0.1f, controlBox.getMinY() + 0.1f), 0.2f, 1f,
				pdRealRootElement.getHierarchyLevel(), 1, 0, pdRealRootElement.getHierarchyDepth() - 1);
		upwardNavigationSlider.setMinSize(80);

	}

	/**
	 * Recursively builds the partial disc tree according to a cluster node tree. Note that the root element
	 * of the partial disc tree has to be set separately.
	 * 
	 * @param tree
	 *            Tree of cluster nodes which is used to build the partial disc tree.
	 * @param clusterNode
	 *            Current parent cluster node whose children are used for the creation of the children of
	 *            partialDisc. Initially this variable should be the root element of the cluster node tree.
	 * @param partialDisc
	 *            Current parent partial disc whose children will be created. Initially this variable should
	 *            be the root element of the partial disc tree.
	 */
	private void buildTree(Tree<ClusterNode> tree, ClusterNode clusterNode, PartialDisc partialDisc) {

		ArrayList<ClusterNode> alChildNodes = tree.getChildren(clusterNode);
		ArrayList<PartialDisc> alChildDiscs = new ArrayList<PartialDisc>();

		if (alChildNodes != null) {
			for (ClusterNode cnChild : alChildNodes) {
				PartialDisc pdCurrentChildDisc = new PartialDisc(partialDiscTree, cnChild);
				try {
					alChildDiscs.add(pdCurrentChildDisc);
					partialDiscTree.addChild(partialDisc, pdCurrentChildDisc);
					hashPartialDiscs.put(cnChild.getClusterNr(), pdCurrentChildDisc);
					selectionManager.initialAdd(cnChild.getClusterNr());
					buildTree(tree, cnChild, pdCurrentChildDisc);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	// TODO: Remove when really not needed any more.
	private void initTestHierarchy() {

		Tree<ClusterNode> tree = new Tree<ClusterNode>();
		TreePorter treePorter = new TreePorter();

		// "data/clustering/experiment_tree_nonbinar.xml"
		try {
			tree = treePorter.importTree("data/clustering/gen_tree_nonbinar.xml");
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		initHierarchy(tree);
	}

	@Override
	public void setDetailLevel(EDetailLevel detailLevel) {
		if (bUseDetailLevel) {
			super.setDetailLevel(detailLevel);
			// renderStyle.setDetailLevel(detailLevel);
		}

	}

	@Override
	public void displayLocal(GL gl) {

		pickingManager.handlePicking(this, gl);

		if (bIsDisplayListDirtyLocal && !bIsAnimationActive) {
			buildDisplayList(gl, iGLDisplayListIndexLocal);
			bIsDisplayListDirtyLocal = false;
		}
		iGLDisplayListToCall = iGLDisplayListIndexLocal;

		display(gl);
		checkForHits(gl);

		if (eBusyModeState != EBusyModeState.OFF) {
			renderBusyMode(gl);
		}
	}

	@Override
	public void displayRemote(GL gl) {

		if (bIsDisplayListDirtyRemote && !bIsAnimationActive) {
			buildDisplayList(gl, iGLDisplayListIndexRemote);
			bIsDisplayListDirtyRemote = false;
		}
		iGLDisplayListToCall = iGLDisplayListIndexRemote;

		display(gl);
		checkForHits(gl);

		// glMouseListener.resetEvents();
	}

	@Override
	public void display(GL gl) {
		processEvents();

		if (pdRealRootElement != null && pdCurrentRootElement != null) {

			if (upwardNavigationSlider.handleDragging(gl, glMouseListener)) {
				updateHierarchyAccordingToNavigationSlider();
			}
			// clipToFrustum(gl);
			//
			if (bIsAnimationActive) {
				float fXCenter = viewFrustum.getWidth() / 2;
				float fYCenter = viewFrustum.getHeight() / 2;

				gl.glLoadIdentity();
				upwardNavigationSlider
					.draw(gl, pickingManager, textureManager, iUniqueID, iUpwardNavigationSliderID,
						iUpwardNavigationSliderButtonID, iUpwardNavigationSliderBodyID);

				float fCurrentSliderWidth = upwardNavigationSlider.getScaledWidth(gl);
				float fCurrentSliderHeight = upwardNavigationSlider.getScaledHeight(gl);

				controlBox.setRectangle(0, 0, fCurrentSliderWidth * 2, fCurrentSliderHeight + fCurrentSliderWidth);
				LabelManager.get().setControlBox(controlBox);
				drawingController.draw(fXCenter, fYCenter, gl, new GLU());
			}
			else
				gl.glCallList(iGLDisplayListToCall);

			if (!isRenderedRemote())
				contextMenu.render(gl, this);
		}
		else {
			renderSymbol(gl);
		}

	}

	/**
	 * Builds the display list for a given display list index.
	 * 
	 * @param gl
	 *            Instance of GL.
	 * @param iGLDisplayListIndex
	 *            Index of the display list.
	 */
	private void buildDisplayList(final GL gl, int iGLDisplayListIndex) {
		if (pdRealRootElement != null && pdCurrentRootElement != null) {

			gl.glNewList(iGLDisplayListIndex, GL.GL_COMPILE);

			float fXCenter = viewFrustum.getWidth() / 2;
			float fYCenter = viewFrustum.getHeight() / 2;

			gl.glLoadIdentity();

			upwardNavigationSlider.draw(gl, pickingManager, textureManager, iUniqueID,
				iUpwardNavigationSliderID, iUpwardNavigationSliderButtonID, iUpwardNavigationSliderBodyID);

			float fCurrentSliderWidth = upwardNavigationSlider.getScaledWidth(gl);
			float fCurrentSliderHeight = upwardNavigationSlider.getScaledHeight(gl);

			controlBox.setRectangle(0, 0, fCurrentSliderWidth * 2, fCurrentSliderHeight + fCurrentSliderWidth);
			LabelManager.get().setControlBox(controlBox);
			drawingController.draw(fXCenter, fYCenter, gl, new GLU());

			gl.glEndList();
		}
		else {
			renderSymbol(gl);
		}
	}

	/**
	 * Renders the symbol of the radial hierarchy. (Called when there's nothing to display.)
	 * 
	 * @param gl
	 *            GL Object that shall be used for rendering.
	 */
	private void renderSymbol(GL gl) {
		float fXButtonOrigin = 0.33f * renderStyle.getScaling();
		float fYButtonOrigin = 0.33f * renderStyle.getScaling();
		Texture tempTexture = textureManager.getIconTexture(gl, EIconTextures.HEAT_MAP_SYMBOL);
		tempTexture.enable();
		tempTexture.bind();

		TextureCoords texCoords = tempTexture.getImageTexCoords();

		gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_LINE_BIT);
		gl.glColor4f(1f, 1, 1, 1f);
		gl.glBegin(GL.GL_POLYGON);

		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(fXButtonOrigin, fYButtonOrigin, 0.01f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top());
		gl.glVertex3f(fXButtonOrigin, 2 * fYButtonOrigin, 0.01f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(fXButtonOrigin * 2, 2 * fYButtonOrigin, 0.01f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(fXButtonOrigin * 2, fYButtonOrigin, 0.01f);
		gl.glEnd();
		gl.glPopAttrib();
		tempTexture.disable();

		// if(textRenderer == null)
		// textRenderer = new CaleydoTextRenderer(new Font("Arial", Font.PLAIN, 24), false);
		//		
		// textRenderer.setColor(0, 0, 0, 0.5f);
		//		
		// textRenderer.renderText(gl, "Hallo!", 0, 0, 0, 0.02f, 10);
		//		
		// IntBuffer buffer = BufferUtil.newIntBuffer(4);
		// gl.glGetIntegerv(GL.GL_VIEWPORT, buffer);
		//		
		// textRenderer.beginRendering(buffer.get(2), buffer.get(3));
		//		
		//		
		// textRenderer.draw3D("Hallo�!", 300, 300, 0, 1.5f);
		// textRenderer.flush();
		//		
		// textRenderer.endRendering();

	}

	/**
	 * Updates the current root element according to the position of the upward navigation silder, i.e. a
	 * parent of the current root element with the hierarchy level of the slider position will be the new root
	 * element.
	 */
	private void updateHierarchyAccordingToNavigationSlider() {
		PartialDisc pdNewRootElement =
			pdCurrentRootElement.getParentWithLevel(upwardNavigationSlider.getSelectedValue());
		if (pdNewRootElement != null) {
			pdCurrentRootElement = pdNewRootElement;

			bIsNewSelection = true;

			PartialDisc pdSelectedElement = drawingController.getCurrentDrawingState().getSelectedElement();
			if (pdSelectedElement != null) {
				pdCurrentSelectedElement = pdSelectedElement;
			}
			navigationHistory.addNewHistoryEntry(drawingController.getCurrentDrawingState(),
				pdCurrentRootElement, pdCurrentSelectedElement, iMaxDisplayedHierarchyDepth);
			setDisplayListDirty();

			setNewSelection(ESelectionType.SELECTION, pdCurrentSelectedElement, pdCurrentRootElement);
		}
	}

	@Override
	public String getDetailedInfo() {
		return new String("");
	}

	@Override
	protected void handleEvents(EPickingType ePickingType, EPickingMode pickingMode, int iExternalID,
		Pick pick) {
		if (detailLevel == EDetailLevel.VERY_LOW) {
			return;
		}
		switch (ePickingType) {

			case RAD_HIERARCHY_PDISC_SELECTION:

				PartialDisc pdPickedElement = hashPartialDiscs.get(iExternalID);

				switch (pickingMode) {
					case CLICKED:
						if (pdPickedElement != null)
							drawingController.handleSelection(pdPickedElement);
						break;

					case MOUSE_OVER:
						if (pdPickedElement != null)
							drawingController.handleMouseOver(pdPickedElement);
						break;

					case RIGHT_CLICKED:
						if (pdPickedElement != null) {
							// Prevent handling of non genetic data in context menu
							if (generalManager.getUseCase().getUseCaseMode() != EUseCaseMode.GENETIC_DATA)
								break;
							if (!pdPickedElement.hasChildren()) {
								GeneContextMenuItemContainer geneContextMenuItemContainer =
									new GeneContextMenuItemContainer();
								geneContextMenuItemContainer.setID(EIDType.EXPRESSION_INDEX, iExternalID);
								contextMenu.addItemContanier(geneContextMenuItemContainer);
							}
							else {
								DetailOutsideItem detailOutsideItem = new DetailOutsideItem(iExternalID);
								contextMenu.addContextMenueItem(detailOutsideItem);
							}

							if (!isRenderedRemote()) {
								contextMenu.setLocation(pick.getPickedPoint(),
									getParentGLCanvas().getWidth(), getParentGLCanvas().getHeight());
								contextMenu.setMasterGLView(this);
							}
							break;
						}
						drawingController.handleAlternativeSelection(pdPickedElement);
						break;

					default:
						return;
				}
				break;

			case RAD_HIERARCHY_SLIDER_SELECTION:
				switch (pickingMode) {
					case CLICKED:
						if (iExternalID == iUpwardNavigationSliderID) {
							if (upwardNavigationSlider.handleSliderSelection(ePickingType)) {
								updateHierarchyAccordingToNavigationSlider();
								setDisplayListDirty();
							}
						}
						break;

					default:
						return;
				}
				break;

			case RAD_HIERARCHY_SLIDER_BUTTON_SELECTION:
				switch (pickingMode) {
					case CLICKED:
						if (iExternalID == iUpwardNavigationSliderButtonID) {
							if (upwardNavigationSlider.handleSliderSelection(ePickingType)) {
								updateHierarchyAccordingToNavigationSlider();
								setDisplayListDirty();
							}
						}
						break;

					default:
						return;
				}
				break;

			case RAD_HIERARCHY_SLIDER_BODY_SELECTION:
				switch (pickingMode) {
					case CLICKED:
						if (iExternalID == iUpwardNavigationSliderBodyID) {
							if (upwardNavigationSlider.handleSliderSelection(ePickingType)) {
								updateHierarchyAccordingToNavigationSlider();
								setDisplayListDirty();
							}
						}
						break;

					default:
						return;
				}
				break;
		}
	}

	/**
	 * Go one step back in navigation history.
	 */
	public void goBackInHistory() {
		navigationHistory.goBack();
	}

	/**
	 * Go one step back in navigation history.
	 */
	public void goForthInHistory() {
		// handleMouseOver(10);
		navigationHistory.goForth();
	}

	/**
	 * Change the color mode from rainbow to gene expression or vice versa.
	 */
	public void changeColorMode() {

		// ClusterNodeSelectionEvent event = new ClusterNodeSelectionEvent();
		// event.setClusterNumber(1073741840);
		// event.setSelectionType(ESelectionType.SELECTION);
		//		
		// eventPublisher.triggerEvent(event);

		DrawingStrategyManager drawingStrategyManager = DrawingStrategyManager.get();
		if (drawingStrategyManager.getDefaultDrawingStrategy().getDrawingStrategyType() == EPDDrawingStrategyType.RAINBOW_COLOR) {
			drawingStrategyManager.setDefaultStrategy(EPDDrawingStrategyType.EXPRESSION_COLOR);
		}
		else {
			drawingStrategyManager.setDefaultStrategy(EPDDrawingStrategyType.RAINBOW_COLOR);
		}
		setDisplayListDirty();
	}

	/**
	 * Gets the real root element of the hierarchy.
	 * 
	 * @return Real root element of the hierarchy.
	 */
	public PartialDisc getRealRootElement() {
		return pdRealRootElement;
	}

	/**
	 * Gets the element that will be displayed as root element.
	 * 
	 * @return Element that will be displayed as root element.
	 */
	public PartialDisc getCurrentRootElement() {
		return pdCurrentRootElement;
	}

	/**
	 * Sets an element that will be displayed as root element.
	 * 
	 * @param pdCurrentRootElement
	 *            New element that will be displayed as root element.
	 */
	public void setCurrentRootElement(PartialDisc pdCurrentRootElement) {
		this.pdCurrentRootElement = pdCurrentRootElement;
		upwardNavigationSlider.setSelectedValue(pdCurrentRootElement.getHierarchyLevel());
	}

	/**
	 * Gets the current selected element.
	 * 
	 * @return The current selected element.
	 */
	public PartialDisc getCurrentSelectedElement() {
		return pdCurrentSelectedElement;
	}

	/**
	 * Sets the current selected element.
	 * 
	 * @param pdCurrentSelectedElement
	 *            Element that will be the current selected element.
	 */
	public void setCurrentSelectedElement(PartialDisc pdCurrentSelectedElement) {
		this.pdCurrentSelectedElement = pdCurrentSelectedElement;
	}

	/**
	 * Returns whether an element has been newly selected, either by the view itself or externally.
	 * 
	 * @return true if an element has been newly selected, false otherwise.
	 */
	public boolean isNewSelection() {
		return bIsNewSelection;
	}

	/**
	 * Gets the maximum displayed hierarchy depth.
	 * 
	 * @return The maximum displayed hierarchy depth.
	 */
	public int getMaxDisplayedHierarchyDepth() {
		return iMaxDisplayedHierarchyDepth;
	}

	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	/**
	 * Sets the maximum displayed hierarchy depth.
	 * 
	 * @param iMaxDisplayedHierarchyDepth
	 */
	public void setMaxDisplayedHierarchyDepth(int iMaxDisplayedHierarchyDepth) {
		if (this.iMaxDisplayedHierarchyDepth != iMaxDisplayedHierarchyDepth) {

			renderStyle.setMinViewDimensions(MIN_DISPLAY_WIDTH + MIN_PIXELS_PER_DISPLAYED_LEVEL
				* iMaxDisplayedHierarchyDepth, MIN_DISPLAY_HEIGHT + MIN_PIXELS_PER_DISPLAYED_LEVEL
				* iMaxDisplayedHierarchyDepth, this);

			bIsNewSelection = false;
			this.iMaxDisplayedHierarchyDepth = iMaxDisplayedHierarchyDepth;
			navigationHistory.setCurrentMaxDisplayedHierarchyDepth(iMaxDisplayedHierarchyDepth);
			setDisplayListDirty();
		}
	}

	/**
	 * Sets if an animation is currently active. If true, no display lists will be built or called.
	 * 
	 * @param bIsAnimationActive
	 *            Determines if the animation is active.
	 */
	public void setAnimationActive(boolean bIsAnimationActive) {
		this.bIsAnimationActive = bIsAnimationActive;
	}

	/**
	 * Returns the partial disc with the specified ID.
	 * 
	 * @param elementID
	 *            ID of the partial disc to obtain.
	 * @return Partial disc with the specified ID, null if no partial disc with the specified ID is present.
	 */
	public PartialDisc getPartialDisc(int elementID) {
		return hashPartialDiscs.get(elementID);
	}

	/**
	 * A new selection will be set in the selection manager with the specified parameters. A
	 * ClusterNodeSelectionEvent with the new selection will be triggered. If the selected element corresponds
	 * to a gene, a SelectionUpdateEvent will also be triggered.
	 * 
	 * @param selectionType
	 *            Type of selection.
	 * @param pdSelected
	 *            Element that has been selected.
	 * @param pdRoot
	 *            Root element.
	 */
	public void setNewSelection(ESelectionType selectionType, PartialDisc pdSelected, PartialDisc pdRoot) {

		selectionManager.clearSelections();
		selectionManager.addToType(selectionType, pdSelected.getElementID());

		ClearSelectionsEvent clearSelectionsEvent = new ClearSelectionsEvent();
		clearSelectionsEvent.setSender(this);
		eventPublisher.triggerEvent(clearSelectionsEvent);

		if (!pdSelected.hasChildren()) {
			SelectionDelta delta = new SelectionDelta(EIDType.EXPRESSION_INDEX);
			delta.addSelection(pdSelected.getElementID(), selectionType);
			SelectionUpdateEvent selectionUpdateEvent = new SelectionUpdateEvent();
			selectionUpdateEvent.setSender(this);
			selectionUpdateEvent.setSelectionDelta((SelectionDelta) delta);
			selectionUpdateEvent.setInfo(getShortInfo());
			eventPublisher.triggerEvent(selectionUpdateEvent);
		}

		ClusterNodeSelectionEvent event = new ClusterNodeSelectionEvent();
		event.setSender(this);
		event.setSelectionDelta(selectionManager.getDelta());
		// Specific elements for other RadialHierarchy Views
		event.setSelectedElementID(pdCurrentSelectedElement.getElementID());
		event.setSelectedElementStartAngle(pdCurrentSelectedElement.getCurrentStartAngle());
		event.setRootElementID(pdRoot.getElementID());
		event.setRootElementStartAngle(pdRoot.getCurrentStartAngle());
		event.setDrawingStateType(drawingController.getCurrentDrawingState().getType());
		event.setMaxDisplayedHierarchyDepth(iMaxDisplayedHierarchyDepth);
		event.setDefaultDrawingStrategyType(DrawingStrategyManager.get().getDefaultDrawingStrategy()
			.getDrawingStrategyType());
		event.setSenderRadialHierarchy(true);

		if (selectionType == ESelectionType.SELECTION) {
			bIsNewSelection = true;
		}
		else {
			bIsNewSelection = false;
		}

		event.setNewSelection(bIsNewSelection);
		eventPublisher.triggerEvent(event);
	}

	public void setNewSelection(boolean bIsNewSelection) {
		this.bIsNewSelection = bIsNewSelection;
	}

	@Override
	public void broadcastElements(EVAOperation type) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumberOfSelections(ESelectionType selectionType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getShortInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearAllSelections() {
		selectionManager.clearSelections();

	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedRadialHierarchyView serializedForm = new SerializedRadialHierarchyView();
		serializedForm.setViewID(this.getID());
		serializedForm.setMaxDisplayedHierarchyDepth(iMaxDisplayedHierarchyDepth);
		serializedForm.setNewSelection(bIsNewSelection);
		serializedForm.setDefaultDrawingStrategyType(DrawingStrategyManager.get().getDefaultDrawingStrategy()
			.getDrawingStrategyType());

		ADrawingState currentDrawingState = drawingController.getCurrentDrawingState();

		if (pdCurrentRootElement != null) {
			if ((currentDrawingState.getType() == EDrawingStateType.DRAWING_STATE_DETAIL_OUTSIDE)
				|| (currentDrawingState.getType() == EDrawingStateType.DRAWING_STATE_FULL_HIERARCHY)) {

				serializedForm.setDrawingStateType(currentDrawingState.getType());
				serializedForm.setRootElementID(pdCurrentRootElement.getElementID());
				serializedForm.setSelectedElementID(pdCurrentSelectedElement.getElementID());
				serializedForm.setRootElementStartAngle(pdCurrentRootElement.getCurrentStartAngle());
				serializedForm.setSelectedElementStartAngle(pdCurrentSelectedElement.getCurrentStartAngle());
			}
			else {
				HistoryEntry historyEntry = navigationHistory.getCurrentHistoryEntry();
				serializedForm.setDrawingStateType(historyEntry.getDrawingState().getType());
				serializedForm.setRootElementID(historyEntry.getRootElement().getElementID());
				serializedForm.setSelectedElementID(historyEntry.getSelectedElement().getElementID());
				serializedForm.setRootElementStartAngle(historyEntry.getRootElementStartAngle());
				serializedForm.setSelectedElementStartAngle(historyEntry.getSelectedElementStartAngle());
			}
		}

		return serializedForm;
	}

	@Override
	public void initFromSerializableRepresentation(ASerializedView ser) {

		Tree<ClusterNode> tree = set.getClusteredTreeGenes();
		if (tree != null) {
			initHierarchy(tree);
		}

		SerializedRadialHierarchyView serializedView = (SerializedRadialHierarchyView) ser;
		setupDisplay(serializedView.getDrawingStateType(), serializedView.getDefaultDrawingStrategyType(),
			serializedView.isNewSelection(), serializedView.getRootElementID(), serializedView
				.getSelectedElementID(), serializedView.getRootElementStartAngle(), serializedView
				.getSelectedElementStartAngle(), serializedView.getMaxDisplayedHierarchyDepth());
	}

	/**
	 * Sets up the current display of the RadialHierarchy view according to the specified parameters.
	 * 
	 * @param drawingStateType
	 *            DrawingState that shall be used.
	 * @param drawingStrategyType
	 *            Default drawing strategy that shall be used.
	 * @param isNewSelection
	 *            Determines if the selected element has been newly selected.
	 * @param rootElementID
	 *            ID of current root element.
	 * @param selectedElementID
	 *            ID of selected element.
	 * @param rootElementStartAngle
	 *            Start angle of the root element.
	 * @param selectedElementStartAngle
	 *            Start angle of the selected element.
	 * @param maxDisplayedHierarchyDepth
	 *            Maximum hierarchy depth that shall be displayed.
	 */
	private void setupDisplay(EDrawingStateType drawingStateType, EPDDrawingStrategyType drawingStrategyType,
		boolean isNewSelection, int rootElementID, int selectedElementID, float rootElementStartAngle,
		float selectedElementStartAngle, int maxDisplayedHierarchyDepth) {

		this.iMaxDisplayedHierarchyDepth = maxDisplayedHierarchyDepth;
		bIsNewSelection = isNewSelection;
		PartialDisc pdTemp = hashPartialDiscs.get(rootElementID);
		drawingController.setDrawingState(drawingStateType);
		DrawingStrategyManager.get().setDefaultStrategy(drawingStrategyType);

		if (pdTemp != null) {
			setCurrentRootElement(pdTemp);
			pdCurrentSelectedElement = hashPartialDiscs.get(selectedElementID);
			pdCurrentRootElement.setCurrentStartAngle(rootElementStartAngle);
			if (pdCurrentSelectedElement != null) {
				pdCurrentSelectedElement.setCurrentStartAngle(selectedElementStartAngle);
			}
		}
		setDisplayListDirty();
	}

	@Override
	public void handleClusterNodeSelection(ClusterNodeSelectionEvent event) {

		SelectionDelta selectionDelta = event.getSelectionDelta();

		if (selectionDelta.getIDType() == EIDType.CLUSTER_NUMBER) {
			if (event.isSenderRadialHierarchy()) {
				setupDisplay(event.getDrawingStateType(), event.getDefaultDrawingStrategyType(), event
					.isNewSelection(), event.getRootElementID(), event.getSelectedElementID(), event
					.getRootElementStartAngle(), event.getSelectedElementStartAngle(), event
					.getMaxDisplayedHierarchyDepth());
			}
			selectionManager.clearSelections();
			selectionManager.setDelta(selectionDelta);
			bIsNewSelection = true;
			setDisplayListDirty();
		}
	}

	@Override
	public void registerEventListeners() {
		redrawViewListener = new RedrawViewListener();
		redrawViewListener.setHandler(this);
		eventPublisher.addListener(RedrawViewEvent.class, redrawViewListener);

		clusterNodeMouseOverListener = new ClusterNodeSelectionListener();
		clusterNodeMouseOverListener.setHandler(this);
		eventPublisher.addListener(ClusterNodeSelectionEvent.class, clusterNodeMouseOverListener);

		goBackInHistoryListener = new GoBackInHistoryListener();
		goBackInHistoryListener.setHandler(this);
		eventPublisher.addListener(GoBackInHistoryEvent.class, goBackInHistoryListener);

		goForthInHistoryListener = new GoForthInHistoryListener();
		goForthInHistoryListener.setHandler(this);
		eventPublisher.addListener(GoForthInHistoryEvent.class, goForthInHistoryListener);

		changeColorModeListener = new ChangeColorModeListener();
		changeColorModeListener.setHandler(this);
		eventPublisher.addListener(ChangeColorModeEvent.class, changeColorModeListener);

		setMaxDisplayedHierarchyDepthListener = new SetMaxDisplayedHierarchyDepthListener();
		setMaxDisplayedHierarchyDepthListener.setHandler(this);
		eventPublisher.addListener(SetMaxDisplayedHierarchyDepthEvent.class,
			setMaxDisplayedHierarchyDepthListener);

		detailOutsideListener = new DetailOutsideListener();
		detailOutsideListener.setHandler(this);
		eventPublisher.addListener(DetailOutsideEvent.class, detailOutsideListener);

		updateViewListener = new UpdateViewListener();
		updateViewListener.setHandler(this);
		eventPublisher.addListener(UpdateViewEvent.class, updateViewListener);

		selectionUpdateListener = new SelectionUpdateListener();
		selectionUpdateListener.setHandler(this);
		eventPublisher.addListener(SelectionUpdateEvent.class, selectionUpdateListener);

		clearSelectionsListener = new ClearSelectionsListener();
		clearSelectionsListener.setHandler(this);
		eventPublisher.addListener(ClearSelectionsEvent.class, clearSelectionsListener);
	}

	@Override
	public void unregisterEventListeners() {
		if (redrawViewListener != null) {
			eventPublisher.removeListener(redrawViewListener);
			redrawViewListener = null;
		}
		if (clusterNodeMouseOverListener != null) {
			eventPublisher.removeListener(clusterNodeMouseOverListener);
			clusterNodeMouseOverListener = null;
		}
		if (goBackInHistoryListener != null) {
			eventPublisher.removeListener(goBackInHistoryListener);
			goBackInHistoryListener = null;
		}
		if (goForthInHistoryListener != null) {
			eventPublisher.removeListener(goForthInHistoryListener);
			goForthInHistoryListener = null;
		}
		if (changeColorModeListener != null) {
			eventPublisher.removeListener(changeColorModeListener);
			changeColorModeListener = null;
		}
		if (setMaxDisplayedHierarchyDepthListener != null) {
			eventPublisher.removeListener(setMaxDisplayedHierarchyDepthListener);
			setMaxDisplayedHierarchyDepthListener = null;
		}
		if (detailOutsideListener != null) {
			eventPublisher.removeListener(detailOutsideListener);
			detailOutsideListener = null;
		}
		if (updateViewListener != null) {
			eventPublisher.removeListener(updateViewListener);
			updateViewListener = null;
		}
		if (selectionUpdateListener != null) {
			eventPublisher.removeListener(selectionUpdateListener);
			selectionUpdateListener = null;
		}
		if (clearSelectionsListener != null) {
			eventPublisher.removeListener(clearSelectionsListener);
			clearSelectionsListener = null;
		}
	}

	@Override
	public void handleClearSelections() {
		selectionManager.clearSelections();
	}

	@Override
	public void handleRedrawView() {
		setDisplayListDirty();
	}

	@Override
	public void handleUpdateView() {
		Tree<ClusterNode> tree = set.getClusteredTreeGenes();
		if (tree != null) {
			initHierarchy(tree);
		}
		else {
			hashPartialDiscs.clear();
			navigationHistory.reset();
			pdCurrentRootElement = null;
			pdCurrentSelectedElement = null;
			pdRealRootElement = null;
		}
		setDisplayListDirty();
	}

	/**
	 * Handles the alternative partial disc selection triggered by pressing a key.
	 */
	public void handleKeyboardAlternativeDiscSelection() {

		Set<Integer> setSelection = selectionManager.getElements(ESelectionType.SELECTION);
		PartialDisc pdCurrentMouseOverElement = null;
		int iDisplayedHierarchyDepth =
			Math.min(iMaxDisplayedHierarchyDepth, pdCurrentRootElement.getHierarchyDepth());

		if ((setSelection != null)) {
			for (Integer elementID : setSelection) {
				pdCurrentMouseOverElement = hashPartialDiscs.get(elementID);
				if (pdCurrentMouseOverElement.isCurrentlyDisplayed(pdCurrentRootElement,
					iDisplayedHierarchyDepth)) {
					drawingController.handleAlternativeSelection(pdCurrentMouseOverElement);
					return;
				}
			}
		}

		Set<Integer> setMouseOver = selectionManager.getElements(ESelectionType.MOUSE_OVER);
		if ((setMouseOver != null)) {
			for (Integer elementID : setMouseOver) {
				pdCurrentMouseOverElement = hashPartialDiscs.get(elementID);
				if (pdCurrentMouseOverElement.isCurrentlyDisplayed(pdCurrentRootElement,
					iDisplayedHierarchyDepth)) {
					drawingController.handleAlternativeSelection(pdCurrentMouseOverElement);
					return;
				}
			}
		}
	}

	public void handleAlternativeSelection(int elementID) {
		PartialDisc pdSelected = hashPartialDiscs.get(elementID);
		if (pdSelected != null) {
			drawingController.handleAlternativeSelection(pdSelected);
		}
	}

	@Override
	public void handleSelectionUpdate(ISelectionDelta selectionDelta, boolean scrollToSelection, String info) {
		if (selectionDelta.getIDType() == EIDType.EXPRESSION_INDEX) {
			selectionManager.clearSelections();
			Collection<SelectionDeltaItem> deltaItems = selectionDelta.getAllItems();

			for (SelectionDeltaItem item : deltaItems) {
				selectionManager.addToType(item.getSelectionType(), item.getPrimaryID());
			}

			bIsNewSelection = true;
			setDisplayListDirty();
		}
	}

	public Rectangle getControlBox() {
		return controlBox;
	}

}
