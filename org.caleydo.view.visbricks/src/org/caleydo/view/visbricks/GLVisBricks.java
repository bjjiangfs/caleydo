package org.caleydo.view.visbricks;

import gleem.linalg.Vec3f;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import org.caleydo.core.data.container.DataContainer;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.id.IDCategory;
import org.caleydo.core.data.id.IDType;
import org.caleydo.core.data.mapping.IDMappingManagerRegistry;
import org.caleydo.core.data.selection.RecordSelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.selection.SelectionTypeEvent;
import org.caleydo.core.data.selection.delta.SelectionDelta;
import org.caleydo.core.data.selection.events.ClearSelectionsListener;
import org.caleydo.core.data.selection.events.ISelectionUpdateHandler;
import org.caleydo.core.data.virtualarray.EVAOperation;
import org.caleydo.core.data.virtualarray.RecordVirtualArray;
import org.caleydo.core.data.virtualarray.similarity.RelationAnalyzer;
import org.caleydo.core.event.data.RelationsUpdatedEvent;
import org.caleydo.core.event.view.ClearSelectionsEvent;
import org.caleydo.core.event.view.DataContainersChangedEvent;
import org.caleydo.core.event.view.tablebased.ConnectionsModeEvent;
import org.caleydo.core.event.view.tablebased.SelectionUpdateEvent;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.core.view.IDataContainerBasedView;
import org.caleydo.core.view.opengl.camera.CameraProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.DetailLevel;
import org.caleydo.core.view.opengl.canvas.listener.IViewCommandHandler;
import org.caleydo.core.view.opengl.canvas.remote.IGLRemoteRenderingView;
import org.caleydo.core.view.opengl.layout.Column;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.layout.LayoutManager;
import org.caleydo.core.view.opengl.layout.Row;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.GLCoordinateUtils;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.core.view.opengl.util.spline.ConnectionBandRenderer;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;
import org.caleydo.core.view.opengl.util.vislink.NURBSCurve;
import org.caleydo.datadomain.pathway.data.PathwayDimensionGroupData;
import org.caleydo.view.visbricks.brick.configurer.CategoricalDataConfigurer;
import org.caleydo.view.visbricks.brick.configurer.IBrickConfigurer;
import org.caleydo.view.visbricks.brick.configurer.NumericalDataConfigurer;
import org.caleydo.view.visbricks.brick.sorting.AverageValueSortingStrategy;
import org.caleydo.view.visbricks.dimensiongroup.DimensionGroup;
import org.caleydo.view.visbricks.dimensiongroup.DimensionGroupManager;
import org.caleydo.view.visbricks.dimensiongroup.DimensionGroupSpacingRenderer;
import org.caleydo.view.visbricks.event.AddGroupsToVisBricksEvent;
import org.caleydo.view.visbricks.listener.AddGroupsToVisBricksListener;
import org.caleydo.view.visbricks.listener.ConnectionsModeListener;
import org.caleydo.view.visbricks.listener.GLVisBricksKeyListener;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

/**
 * VisBricks main view
 * 
 * @author Marc Streit
 * @author Alexander Lex
 */

public class GLVisBricks extends AGLView implements IDataContainerBasedView,
		IGLRemoteRenderingView, IViewCommandHandler, ISelectionUpdateHandler {

	public final static class PickingTypes {
		public final static String DIMENSION_GROUP = "DIMENSION_GROUP";
	}

	public final static String VIEW_TYPE = "org.caleydo.view.visbricks";

	private final static int ARCH_PIXEL_HEIGHT = 150;
	private final static float ARCH_BOTTOM_PERCENT = 1f;
	private final static float ARCH_STAND_WIDTH_PERCENT = 0.05f;

	private final static int DIMENSION_GROUP_SPACING_MIN_PIXEL_WIDTH = 30;
	public final static int DIMENSION_GROUP_SIDE_SPACING = 50;

	public final static float[] ARCH_COLOR = { 0f, 0f, 0f, 0.1f };

	private AddGroupsToVisBricksListener addGroupsToVisBricksListener;
	private ClearSelectionsListener clearSelectionsListener;
	private ConnectionsModeListener trendHighlightModeListener;

	private DimensionGroupManager dimensionGroupManager;

	private ConnectionBandRenderer connectionRenderer;

	private LayoutManager centerLayoutManager;
	private LayoutManager leftLayoutManager;
	private LayoutManager rightLayoutManager;

	private Row centerRowLayout;
	private Column leftColumnLayout;
	private Column rightColumnLayout;

	/** thickness of the arch at the sides */
	private float archSideThickness = 0;
	private float archInnerWidth = 0;
	private float archTopY = 0;
	private float archBottomY = 0;
	private float archHeight = 0;

	/** Flag signaling if a group needs to be moved out of the center */
	boolean resizeNecessary = false;
	boolean lastResizeDirectionWasToLeft = true;

	boolean isLayoutDirty = false;

	/** Flag telling whether a detail is shown left of its dimension group */
	private boolean isLeftDetailShown = false;
	/** Same as {@link #isLeftDetailShown} for right */
	private boolean isRightDetailShown = false;

	private Queue<DimensionGroup> uninitializedDimensionGroups = new LinkedList<DimensionGroup>();

	private DragAndDropController dragAndDropController;

	private RelationAnalyzer relationAnalyzer;

	private ElementLayout leftDimensionGroupSpacing;
	private ElementLayout rightDimensionGroupSpacing;

	/**
	 * The id category used to map between the records of the dimension groups.
	 * Only data with the same recordIDCategory can be connected
	 */
	private IDCategory recordIDCategory;

	/**
	 * The selection manager for the records, used for highlighting the visual
	 * links
	 */
	private RecordSelectionManager recordSelectionManager;

	private boolean connectionsOn = true;
	private boolean connectionsHighlightDynamic = false;

	private int selectedConnectionBandID = -1;

	/**
	 * Determines the connection focus highlight dynamically in a range between
	 * 0 and 1
	 */
	private float connectionsFocusFactor;

	private boolean isHorizontalMoveDraggingActive = false;
	private int movedDimensionGroup = -1;

	private float previousXCoordinate = Float.NaN;

	/** Needed for selecting the elments when a connection band is picked **/
	private HashMap<Integer, RecordVirtualArray> hashConnectionBandIDToRecordVA = new HashMap<Integer, RecordVirtualArray>();

	private SelectionType volatieBandSelectionType;

	private int connectionBandIDCounter = 0;

	private boolean isConnectionLinesDirty = true;

	private Set<IDataDomain> dataDomains;
	private List<DataContainer> dataContainers;

	/**
	 * Constructor.
	 * 
	 */
	public GLVisBricks(GLCanvas glCanvas, Composite parentComposite,
			ViewFrustum viewFrustum) {

		super(glCanvas, parentComposite, viewFrustum);

		viewType = GLVisBricks.VIEW_TYPE;
		viewLabel = "VisBricks";

		connectionRenderer = new ConnectionBandRenderer();

		dragAndDropController = new DragAndDropController(this);

		dimensionGroupManager = new DimensionGroupManager();

		glKeyListener = new GLVisBricksKeyListener();

		relationAnalyzer = new RelationAnalyzer();

		dataDomains = new HashSet<IDataDomain>();
		dataContainers = new ArrayList<DataContainer>();

		parentGLCanvas.removeMouseWheelListener(glMouseListener);
		parentGLCanvas.addMouseWheelListener(glMouseWheelListener);
		registerPickingListeners();
	}

	@Override
	public void init(GL2 gl) {
		displayListIndex = gl.glGenLists(1);

		textRenderer = new CaleydoTextRenderer(24);

		detailLevel = DetailLevel.HIGH;
		connectionRenderer.init(gl);

	}

	private void initLayouts() {

		dimensionGroupManager.getDimensionGroupSpacers().clear();

		initCenterLayout();

		initLeftLayout();
		initRightLayout();

		updateConnectionLinesBetweenDimensionGroups();
	}

	private void initLeftLayout() {
		ViewFrustum leftArchFrustum = new ViewFrustum(viewFrustum.getProjectionMode(), 0,
				archSideThickness, 0, archBottomY, 0, 1);
		leftLayoutManager = new LayoutManager(leftArchFrustum, pixelGLConverter);
		leftColumnLayout = new Column("leftArchColumn");

		initSideLayout(leftColumnLayout, leftLayoutManager, 0,
				dimensionGroupManager.getCenterGroupStartIndex());
	}

	private void initRightLayout() {
		ViewFrustum rightArchFrustum = new ViewFrustum(viewFrustum.getProjectionMode(),
				0, archSideThickness, 0, archBottomY, 0, 1);
		rightColumnLayout = new Column("rightArchColumn");
		rightLayoutManager = new LayoutManager(rightArchFrustum, pixelGLConverter);
		initSideLayout(rightColumnLayout, rightLayoutManager,
				dimensionGroupManager.getRightGroupStartIndex(), dimensionGroupManager
						.getDimensionGroups().size());
	}

	public int getSideArchWidthPixels() {
		return pixelGLConverter.getPixelWidthForGLWidth(viewFrustum.getWidth()
				* ARCH_STAND_WIDTH_PERCENT);
	}

	/**
	 * Init the layout for the center region, showing the horizontal bar of the
	 * arch plus all sub-bricks above and below
	 */
	private void initCenterLayout() {

		archSideThickness = viewFrustum.getWidth() * ARCH_STAND_WIDTH_PERCENT;
		if (isRightDetailShown || isLeftDetailShown) {
			archInnerWidth = 0;
		} else {
			archInnerWidth = viewFrustum.getWidth() * (ARCH_STAND_WIDTH_PERCENT + 0.024f);
		}

		archHeight = pixelGLConverter.getGLHeightForPixelHeight(ARCH_PIXEL_HEIGHT);
		archBottomY = viewFrustum.getHeight() * ARCH_BOTTOM_PERCENT - archHeight / 2f;

		archTopY = archBottomY + archHeight;

		int dimensionGroupCountInCenter = dimensionGroupManager.getRightGroupStartIndex()
				- dimensionGroupManager.getCenterGroupStartIndex();

		float centerLayoutWidth = viewFrustum.getWidth() - 2 * (archInnerWidth);
		// float centerLayoutWidth = viewFrustum.getWidth();

		centerRowLayout = new Row("centerArchRow");

		centerRowLayout.setPriorityRendereing(true);
		centerRowLayout.setFrameColor(1, 1, 0, 1);
		// centerRowLayout.setDebug(false);

		leftDimensionGroupSpacing = new ElementLayout("firstCenterDimGrSpacing");
		// leftDimensionGroupSpacing.setDebug(true);

		DimensionGroupSpacingRenderer dimensionGroupSpacingRenderer = null;

		// Handle special case where center contains no groups
		if (dimensionGroupCountInCenter < 1) {
			dimensionGroupSpacingRenderer = new DimensionGroupSpacingRenderer(null,
					connectionRenderer, null, null, this);
		} else {
			dimensionGroupSpacingRenderer = new DimensionGroupSpacingRenderer(null,
					connectionRenderer, null, dimensionGroupManager.getDimensionGroups()
							.get(dimensionGroupManager.getCenterGroupStartIndex()), this);
		}

		leftDimensionGroupSpacing.setRenderer(dimensionGroupSpacingRenderer);
		dimensionGroupSpacingRenderer.setLineLength(archHeight);

		if (dimensionGroupCountInCenter > 1)
			leftDimensionGroupSpacing.setPixelSizeX(DIMENSION_GROUP_SIDE_SPACING);
		else
			leftDimensionGroupSpacing.setGrabX(true);

		centerRowLayout.append(leftDimensionGroupSpacing);

		for (int dimensionGroupIndex = dimensionGroupManager.getCenterGroupStartIndex(); dimensionGroupIndex < dimensionGroupManager
				.getRightGroupStartIndex(); dimensionGroupIndex++) {

			ElementLayout dynamicDimensionGroupSpacing;

			DimensionGroup group = dimensionGroupManager.getDimensionGroups().get(
					dimensionGroupIndex);
			group.setCollapsed(false);
			group.setArchHeight(ARCH_PIXEL_HEIGHT);
			centerRowLayout.append(group.getLayout());
			// centerRowLayout.setDebug(true);

			if (dimensionGroupIndex != dimensionGroupManager.getRightGroupStartIndex() - 1) {
				dynamicDimensionGroupSpacing = new ElementLayout("dynamicDimGrSpacing");
				dimensionGroupSpacingRenderer = new DimensionGroupSpacingRenderer(
						relationAnalyzer, connectionRenderer, group,
						dimensionGroupManager.getDimensionGroups().get(
								dimensionGroupIndex + 1), this);
				dynamicDimensionGroupSpacing.setGrabX(true);
				dynamicDimensionGroupSpacing.setRenderer(dimensionGroupSpacingRenderer);
				centerRowLayout.append(dynamicDimensionGroupSpacing);

			} else {
				rightDimensionGroupSpacing = new ElementLayout("lastDimGrSpacing");
				dimensionGroupSpacingRenderer = new DimensionGroupSpacingRenderer(null,
						connectionRenderer, group, null, this);

				if (dimensionGroupCountInCenter > 1)
					rightDimensionGroupSpacing
							.setPixelSizeX(DIMENSION_GROUP_SIDE_SPACING);
				else
					rightDimensionGroupSpacing.setGrabX(true);

				rightDimensionGroupSpacing.setRenderer(dimensionGroupSpacingRenderer);
				centerRowLayout.append(rightDimensionGroupSpacing);
			}
			// dimensionGroupSpacing.setDebug(true);

			dimensionGroupSpacingRenderer.setLineLength(archHeight);
		}

		ViewFrustum centerArchFrustum = new ViewFrustum(viewFrustum.getProjectionMode(),
				0, centerLayoutWidth, 0, viewFrustum.getHeight(), 0, 1);
		centerLayoutManager = new LayoutManager(centerArchFrustum, pixelGLConverter);
		centerLayoutManager.setBaseElementLayout(centerRowLayout);

		centerLayoutManager.updateLayout();
	}

	/**
	 * Initialize the layout for the sides of the arch
	 * 
	 * @param columnLayout
	 * @param layoutTemplate
	 * @param layoutManager
	 * @param dimensinoGroupStartIndex
	 * @param dimensinoGroupEndIndex
	 */
	private void initSideLayout(Column columnLayout, LayoutManager layoutManager,
			int dimensinoGroupStartIndex, int dimensinoGroupEndIndex) {

		layoutManager.setBaseElementLayout(columnLayout);

		columnLayout.setFrameColor(1, 1, 0, 1);
		// columnLayout.setDebug(true);
		columnLayout.setBottomUp(true);

		ElementLayout dimensionGroupSpacing = new ElementLayout("firstSideDimGrSpacing");

		// dimensionGroupSpacing.setDebug(true);
		dimensionGroupSpacing.setGrabY(true);

		columnLayout.append(dimensionGroupSpacing);

		DimensionGroupSpacingRenderer dimensionGroupSpacingRenderer = null;

		// Handle special case where arch stand contains no groups
		if (dimensinoGroupStartIndex == 0 || dimensinoGroupStartIndex == dimensinoGroupEndIndex) {
			dimensionGroupSpacingRenderer = new DimensionGroupSpacingRenderer(null,
					connectionRenderer, null, null, this);
		} else {
			dimensionGroupSpacingRenderer = new DimensionGroupSpacingRenderer(null,
					connectionRenderer, null, dimensionGroupManager.getDimensionGroups()
							.get(dimensionGroupManager.getCenterGroupStartIndex()), this);
		}

		dimensionGroupSpacing.setRenderer(dimensionGroupSpacingRenderer);

		dimensionGroupSpacingRenderer.setVertical(false);
		dimensionGroupSpacingRenderer.setLineLength(archSideThickness);

		for (int dimensionGroupIndex = dimensinoGroupStartIndex; dimensionGroupIndex < dimensinoGroupEndIndex; dimensionGroupIndex++) {

			DimensionGroup group = dimensionGroupManager.getDimensionGroups().get(
					dimensionGroupIndex);

			group.getLayout().setAbsoluteSizeY(archSideThickness);
			// group.getLayout().setDebug(true);
			group.setArchHeight(-1);
			columnLayout.append(group.getLayout());

			group.setCollapsed(true);

			dimensionGroupSpacing = new ElementLayout("sideDimGrSpacing");
			// dimensionGroupSpacing.setDebug(true);
			dimensionGroupSpacing.setGrabY(true);

			dimensionGroupSpacingRenderer = new DimensionGroupSpacingRenderer(null, null,
					group, null, this);
			columnLayout.append(dimensionGroupSpacing);

			dimensionGroupSpacing.setRenderer(dimensionGroupSpacingRenderer);

			dimensionGroupSpacingRenderer.setVertical(false);
			dimensionGroupSpacingRenderer.setLineLength(archSideThickness);

		}

		layoutManager.updateLayout();

	}

	@Override
	public void initLocal(GL2 gl) {

		// Register keyboard listener to GL2 canvas
		parentComposite.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				parentComposite.addKeyListener(glKeyListener);
			}
		});

		init(gl);
	}

	@Override
	public void initRemote(final GL2 gl, final AGLView glParentView,
			final GLMouseListener glMouseListener) {

	}

	@Override
	public void displayLocal(GL2 gl) {

		if (!uninitializedDimensionGroups.isEmpty()) {
			while (uninitializedDimensionGroups.peek() != null) {
				uninitializedDimensionGroups.poll().initRemote(gl, this, glMouseListener);
			}
			initLayouts();
		}
		if (isDisplayListDirty) {
			buildDisplayList(gl, displayListIndex);
			isDisplayListDirty = false;
		}

		for (DimensionGroup group : dimensionGroupManager.getDimensionGroups()) {
			group.processEvents();
		}

		pickingManager.handlePicking(this, gl);

		display(gl);

		if (!lazyMode)
			checkForHits(gl);
	}

	@Override
	public void displayRemote(GL2 gl) {
	}

	@Override
	public void display(GL2 gl) {
		handleHorizontalMoveDragging(gl);
		if (isLayoutDirty) {
			isLayoutDirty = false;
			centerLayoutManager.updateLayout();
			float minWidth = pixelGLConverter
					.getGLWidthForPixelWidth(DIMENSION_GROUP_SPACING_MIN_PIXEL_WIDTH);
			for (ElementLayout layout : centerRowLayout) {
				if (!(layout.getRenderer() instanceof DimensionGroupSpacingRenderer))
					continue;
				if (resizeNecessary)
					break;

				if (layout.getSizeScaledX() < minWidth - 0.01f) {
					resizeNecessary = true;
					break;
				}
			}
		}

		if (resizeNecessary) {
			int size = centerRowLayout.size();
			if (size >= 3) {
				if (lastResizeDirectionWasToLeft) {
					dimensionGroupManager.setCenterGroupStartIndex(dimensionGroupManager
							.getCenterGroupStartIndex() + 1);

					float width = centerRowLayout.getElements().get(0).getSizeScaledX()
							+ centerRowLayout.getElements().get(1).getSizeScaledX()
							+ centerRowLayout.getElements().get(2).getSizeScaledX();
					centerRowLayout.remove(0);
					centerRowLayout.remove(0);
					leftDimensionGroupSpacing = centerRowLayout.getElements().get(0);

					leftDimensionGroupSpacing.setAbsoluteSizeX(width);
					((DimensionGroupSpacingRenderer) leftDimensionGroupSpacing
							.getRenderer()).setLeftDimGroup(null);
					initLeftLayout();

					// if (size == 3)
					// leftDimensionGroupSpacing.setGrabX(true);

				} else {
					dimensionGroupManager.setRightGroupStartIndex(dimensionGroupManager
							.getRightGroupStartIndex() - 1);

					// float width = centerRowLayout.getElements().get(size - 1)
					// .getSizeScaledX()
					// + centerRowLayout.getElements().get(size - 2)
					// .getSizeScaledX()
					// + centerRowLayout.getElements().get(size - 3)
					// .getSizeScaledX();
					centerRowLayout.remove(centerRowLayout.size() - 1);
					centerRowLayout.remove(centerRowLayout.size() - 1);
					rightDimensionGroupSpacing = centerRowLayout.getElements().get(
							centerRowLayout.size() - 1);
					// rightDimensionGroupSpacing.setAbsoluteSizeX(width);
					rightDimensionGroupSpacing.setGrabX(true);
					((DimensionGroupSpacingRenderer) rightDimensionGroupSpacing
							.getRenderer()).setRightDimGroup(null);
					initRightLayout();

					// if (size == 3)
					// rightDimensionGroupSpacing.setGrabX(true);

				}
			}
			centerLayoutManager.updateLayout();
			resizeNecessary = false;
		}
		// float angle = 70f;
		// viewCamera.setCameraRotation(new Rotf());

		// gl.glRotatef(angle, 1, 0, 0);

		if (isConnectionLinesDirty)
			performConnectionLinesUpdate();

		for (DimensionGroup dimensionGroup : dimensionGroupManager.getDimensionGroups()) {
			dimensionGroup.display(gl);
		}

		if (!isRightDetailShown && !isLeftDetailShown) {
			leftLayoutManager.render(gl);
		}

		gl.glTranslatef(archInnerWidth, 0, 0);
		centerLayoutManager.render(gl);
		gl.glTranslatef(-archInnerWidth, 0, 0);

		if (!isRightDetailShown && !isLeftDetailShown) {
			float rightArchStand = (1 - ARCH_STAND_WIDTH_PERCENT)
					* viewFrustum.getWidth();
			gl.glTranslatef(rightArchStand, 0, 0);
			rightLayoutManager.render(gl);
			gl.glTranslatef(-rightArchStand, 0, 0);
		}

		if (!isRightDetailShown && !isLeftDetailShown) {
			renderArch(gl);
		}

		// gl.glRotatef(-angle, 1, 0, 0);

		// call after all other rendering because it calls the onDrag methods
		// which need alpha blending...
		dragAndDropController.handleDragging(gl, glMouseListener);
	}

	/**
	 * Switches to detail mode where the detail brick is on the right side of
	 * the specified dimension group
	 */
	public void switchToDetailModeRight(DimensionGroup dimensionGroup) {

		int dimensionGroupIndex = dimensionGroupManager
				.indexOfDimensionGroup(dimensionGroup);
		// false only if this is the rightmost DimensionGroup. If true we move
		// anything beyond the next dimension group out
		if (dimensionGroupIndex != dimensionGroupManager.getRightGroupStartIndex() - 1) {
			dimensionGroupManager.setRightGroupStartIndex(dimensionGroupIndex + 2);

		}
		// false only if this is the leftmost DimensionGroup. If true we move
		// anything further left out
		if (dimensionGroupIndex != dimensionGroupManager.getRightGroupStartIndex()) {
			dimensionGroupManager.setCenterGroupStartIndex(dimensionGroupIndex);

		}
		isRightDetailShown = true;

		initLeftLayout();
		initCenterLayout();
		initRightLayout();
	}

	/**
	 * Switches to detail mode where the detail brick is on the left side of the
	 * specified dimension group
	 */
	public void switchToDetailModeLeft(DimensionGroup dimensionGroup) {

		int dimensionGroupIndex = dimensionGroupManager
				.indexOfDimensionGroup(dimensionGroup);

		// false only if this is the left-most dimension group. If true we move
		// out everything right of this dimension group
		if (dimensionGroupIndex != dimensionGroupManager.getCenterGroupStartIndex()) {
			dimensionGroupManager.setCenterGroupStartIndex(dimensionGroupIndex - 1);
		}
		// false only if this is the right-most dimension group
		if (dimensionGroupIndex != dimensionGroupManager.getRightGroupStartIndex()) {
			dimensionGroupManager.setRightGroupStartIndex(dimensionGroupIndex + 1);
		}
		isLeftDetailShown = true;

		initLeftLayout();
		initCenterLayout();
		initRightLayout();
	}

	/**
	 * Hide the detail brick which is shown right of its parent dimension group
	 */
	public void switchToOverviewModeRight() {
		isRightDetailShown = false;
		initLeftLayout();
		initCenterLayout();
		initRightLayout();
	}

	/**
	 * Hide the detail brich which is shown left of its parent dimension group
	 */
	public void switchToOverviewModeLeft() {
		isLeftDetailShown = false;
		initLeftLayout();
		initCenterLayout();
		initRightLayout();
	}

	private void buildDisplayList(final GL2 gl, int iGLDisplayListIndex) {
		gl.glNewList(iGLDisplayListIndex, GL2.GL_COMPILE);

		renderArch(gl);

		gl.glEndList();
	}

	private void renderArch(GL2 gl) {

		// Left arch

		gl.glLineWidth(1);

		// gl.glColor4fv(ARCH_COLOR, 0);
		//
		// gl.glBegin(GL2.GL_POLYGON);
		// gl.glVertex3f(0, 0, 0f);
		// gl.glVertex3f(0, archBottomY, 0f);
		// gl.glVertex3f(archSideThickness, archBottomY, 0f);
		// gl.glVertex3f(archSideThickness, 0, 0f);
		// gl.glEnd();

		ArrayList<Vec3f> inputPoints = new ArrayList<Vec3f>();
		inputPoints.add(new Vec3f(0, archBottomY, 0));
		inputPoints.add(new Vec3f(0, archTopY, 0));
		inputPoints.add(new Vec3f(archInnerWidth * 0.9f, archTopY, 0));

		NURBSCurve curve = new NURBSCurve(inputPoints, 10);

		ArrayList<Vec3f> outputPointsTop = curve.getCurvePoints();
		outputPointsTop.add(new Vec3f(archInnerWidth, archTopY, 0));

		inputPoints.clear();
		inputPoints.add(new Vec3f(archInnerWidth, archBottomY, 0));
		inputPoints.add(new Vec3f(archSideThickness, archBottomY, 0));
		inputPoints.add(new Vec3f(archSideThickness, archBottomY * 0.8f, 0));

		curve = new NURBSCurve(inputPoints, 10);

		ArrayList<Vec3f> outputPointsBottom = new ArrayList<Vec3f>();
		outputPointsBottom.addAll(curve.getCurvePoints());

		ArrayList<Vec3f> outputPoints = new ArrayList<Vec3f>();

		outputPoints.addAll(outputPointsTop);
		outputPoints.add(new Vec3f(archInnerWidth, archBottomY, 0));
		outputPoints.addAll(outputPointsBottom);

		// connectionRenderer.render(gl, outputPoints);

		gl.glColor4f(ARCH_COLOR[0], ARCH_COLOR[1], ARCH_COLOR[2], ARCH_COLOR[3] * 2);
		gl.glBegin(GL2.GL_LINE_STRIP);
		for (Vec3f point : outputPointsTop)
			gl.glVertex3f(point.x(), point.y(), point.z());
		gl.glEnd();

		gl.glBegin(GL2.GL_LINE_STRIP);
		for (Vec3f point : outputPointsBottom)
			gl.glVertex3f(point.x(), point.y(), point.z());
		gl.glEnd();

		gl.glColor4f(ARCH_COLOR[0], ARCH_COLOR[1], ARCH_COLOR[2], ARCH_COLOR[3] * 2);

		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3f(0, archBottomY, 0f);
		gl.glVertex3f(0, 0, 0f);
		gl.glEnd();

		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3f(archSideThickness, archBottomY * 0.8f, 0f);
		gl.glVertex3f(archSideThickness, 0, 0f);
		gl.glEnd();

		// Right arch

		// gl.glColor4fv(ARCH_COLOR, 0);
		// gl.glBegin(GL2.GL_POLYGON);
		// gl.glVertex3f(viewFrustum.getWidth(), 0, 0f);
		// gl.glVertex3f(viewFrustum.getWidth(), archBottomY, 0f);
		// gl.glVertex3f(viewFrustum.getWidth() - archSideThickness,
		// archBottomY, 0f);
		// gl.glVertex3f(viewFrustum.getWidth() - archSideThickness, 0, 0f);
		// gl.glEnd();

		inputPoints.clear();
		inputPoints.add(new Vec3f(viewFrustum.getWidth(), archBottomY, 0));
		inputPoints.add(new Vec3f(viewFrustum.getWidth(), archTopY, 0));
		inputPoints.add(new Vec3f(viewFrustum.getWidth() - archInnerWidth * 0.9f,
				archTopY, 0));

		curve = new NURBSCurve(inputPoints, 10);
		outputPointsTop.clear();
		outputPointsTop = curve.getCurvePoints();
		outputPointsTop.add(new Vec3f(viewFrustum.getWidth() - archInnerWidth, archTopY,
				0));

		inputPoints.clear();
		inputPoints
				.add(new Vec3f(viewFrustum.getWidth() - archInnerWidth, archBottomY, 0));
		inputPoints.add(new Vec3f(viewFrustum.getWidth() - archSideThickness,
				archBottomY, 0));
		inputPoints.add(new Vec3f(viewFrustum.getWidth() - archSideThickness,
				archBottomY * 0.8f, 0));

		curve = new NURBSCurve(inputPoints, 10);

		outputPointsBottom.clear();
		outputPointsBottom = new ArrayList<Vec3f>();
		outputPointsBottom.addAll(curve.getCurvePoints());

		outputPoints.clear();

		outputPoints.addAll(outputPointsTop);
		outputPoints.add(new Vec3f(viewFrustum.getWidth() - archInnerWidth, archBottomY,
				0));
		outputPoints.addAll(outputPointsBottom);

		// connectionRenderer.render(gl, outputPoints);

		gl.glColor4f(ARCH_COLOR[0], ARCH_COLOR[1], ARCH_COLOR[2], ARCH_COLOR[3] * 2);
		gl.glBegin(GL2.GL_LINE_STRIP);
		for (Vec3f point : outputPointsTop)
			gl.glVertex3f(point.x(), point.y(), point.z());
		gl.glEnd();

		gl.glBegin(GL2.GL_LINE_STRIP);
		for (Vec3f point : outputPointsBottom)
			gl.glVertex3f(point.x(), point.y(), point.z());
		gl.glEnd();

		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3f(viewFrustum.getWidth(), 0, 0f);
		gl.glVertex3f(viewFrustum.getWidth(), archBottomY, 0f);
		gl.glEnd();

		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3f(viewFrustum.getWidth() - archSideThickness, archBottomY * 0.8f,
				0.01f);
		gl.glVertex3f(viewFrustum.getWidth() - archSideThickness, 0, 0.1f);
		gl.glEnd();
	}

	/**
	 * Handles the left-right dragging of the whole dimension group. Does
	 * collision handling and moves dimension groups to the sides if necessary.
	 * 
	 * @param gl
	 */
	private void handleHorizontalMoveDragging(GL2 gl) {
		if (!isHorizontalMoveDraggingActive)
			return;
		if (glMouseListener.wasMouseReleased()) {
			isHorizontalMoveDraggingActive = false;
			previousXCoordinate = Float.NaN;
			return;
		}

		Point currentPoint = glMouseListener.getPickedPoint();

		float[] pointCordinates = GLCoordinateUtils
				.convertWindowCoordinatesToWorldCoordinates(gl, currentPoint.x,
						currentPoint.y);

		if (Float.isNaN(previousXCoordinate)) {
			previousXCoordinate = pointCordinates[0];
			return;
		}

		float change = pointCordinates[0] - previousXCoordinate;

		// float change = -0.1f;
		// isHorizontalMoveDraggingActive = false;
		if (change > 0)
			lastResizeDirectionWasToLeft = false;
		else
			lastResizeDirectionWasToLeft = true;
		previousXCoordinate = pointCordinates[0];

		ElementLayout leftSpacing = null;
		int leftIndex = 0;
		int rightIndex = 0;
		ElementLayout rightSpacing = null;

		DimensionGroupSpacingRenderer spacingRenderer;
		int count = 0;
		for (ElementLayout layout : centerRowLayout) {
			if (layout.getRenderer() instanceof DimensionGroupSpacingRenderer) {
				spacingRenderer = (DimensionGroupSpacingRenderer) layout.getRenderer();
				if (spacingRenderer.getRightDimGroup() != null) {
					if (spacingRenderer.getRightDimGroup().getID() == movedDimensionGroup) {
						leftSpacing = layout;
						leftIndex = count;

					}
				}
				if (spacingRenderer.getLeftDimGroup() != null) {
					if (spacingRenderer.getLeftDimGroup().getID() == movedDimensionGroup) {
						rightSpacing = layout;
						rightIndex = count;

					}
				}
				if (count < centerRowLayout.size() - 1) {
					layout.setGrabX(false);
					layout.setAbsoluteSizeX(layout.getSizeScaledX());
				} else
					layout.setGrabX(true);
			}
			count++;
		}

		if (leftSpacing == null || rightSpacing == null)
			return;

		float leftSizeX = leftSpacing.getSizeScaledX();
		float rightSizeX = rightSpacing.getSizeScaledX();
		float minWidth = pixelGLConverter
				.getGLWidthForPixelWidth(DIMENSION_GROUP_SPACING_MIN_PIXEL_WIDTH);

		if (change > 0) {
			if (rightSizeX - change > minWidth) {
				rightSpacing.setAbsoluteSizeX(rightSizeX - change);
			} else {
				rightSpacing.setAbsoluteSizeX(minWidth);
				float savedSize = rightSizeX - minWidth;
				float remainingChange = change - savedSize;

				while (remainingChange > 0) {
					if (centerRowLayout.size() < rightIndex + 2)
						break;

					rightIndex += 2;
					ElementLayout spacing = centerRowLayout.getElements().get(rightIndex);
					if (spacing.getSizeScaledX() - remainingChange > minWidth + 0.001f) {
						spacing.setAbsoluteSizeX(spacing.getSizeScaledX()
								- remainingChange);
						remainingChange = -1;
					} else {
						savedSize = spacing.getSizeScaledX() - minWidth;
						remainingChange -= savedSize;
						if (rightIndex == centerRowLayout.size() - 1)
							spacing.setAbsoluteSizeX(0f);
						else
							spacing.setAbsoluteSizeX(minWidth);
					}
				}
			}
			leftSpacing.setAbsoluteSizeX(leftSizeX + change);
		} else {

			if (leftSizeX + change > minWidth) {
				leftSpacing.setAbsoluteSizeX(leftSizeX + change);
			} else {
				leftSpacing.setAbsoluteSizeX(minWidth);
				float savedSize = leftSizeX - minWidth;
				float remainingChange = change + savedSize;

				while (remainingChange < 0) {
					if (leftIndex < 2)
						break;

					leftIndex -= 2;
					ElementLayout spacing = centerRowLayout.getElements().get(leftIndex);
					if (spacing.getSizeScaledX() + remainingChange > minWidth + 0.001f) {
						spacing.setAbsoluteSizeX(spacing.getSizeScaledX()
								+ remainingChange);
						remainingChange = 1;
					} else {
						savedSize = spacing.getSizeScaledX() + minWidth;
						remainingChange += savedSize;
						if (leftIndex == 0)
							spacing.setAbsoluteSizeX(0);
						else
							spacing.setAbsoluteSizeX(minWidth);
					}
				}
			}
			rightSpacing.setAbsoluteSizeX(rightSizeX - change);
		}

		updateLayout();
	}

	protected void registerPickingListeners() {

		addTypePickingListener(new APickingListener() {
			@Override
			public void clicked(Pick pick) {
				selectedConnectionBandID = pick.getID();
				selectElementsByConnectionBandID(selectedConnectionBandID);
			}
		}, PickingType.BRICK_CONNECTION_BAND.name());

		addTypePickingListener(new APickingListener() {
			@Override
			public void clicked(Pick pick) {
				dragAndDropController.setDraggingStartPosition(pick.getPickedPoint());
				dragAndDropController.addDraggable((DimensionGroup) generalManager
						.getViewManager().getGLView(pick.getID()));

			}

			@Override
			public void dragged(Pick pick) {
				if (dragAndDropController.hasDraggables()) {
					if (glMouseListener.wasRightMouseButtonPressed())
						dragAndDropController.clearDraggables();
					else if (!dragAndDropController.isDragging())
						dragAndDropController.startDragging();
				}

			}

		}, PickingType.DIMENSION_GROUP.name());

		addTypePickingListener(new APickingListener() {
			@Override
			public void dragged(Pick pick) {

				dragAndDropController.setDropArea(dimensionGroupManager
						.getDimensionGroupSpacers().get(pick.getID()));
			};

		}, PickingType.DIMENSION_GROUP_SPACER.name());

		addTypePickingListener(new APickingListener() {
			@Override
			public void clicked(Pick pick) {
				isHorizontalMoveDraggingActive = true;
				movedDimensionGroup = pick.getID();
			};

		}, PickingType.MOVE_HORIZONTALLY_HANDLE.name());
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedVisBricksView serializedForm = new SerializedVisBricksView();
		serializedForm.setViewID(this.getID());
		return serializedForm;
	}

	@Override
	public String toString() {
		return "TODO: ADD INFO THAT APPEARS IN THE LOG";
	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();

		addGroupsToVisBricksListener = new AddGroupsToVisBricksListener();
		addGroupsToVisBricksListener.setHandler(this);
		eventPublisher.addListener(AddGroupsToVisBricksEvent.class,
				addGroupsToVisBricksListener);

		clearSelectionsListener = new ClearSelectionsListener();
		clearSelectionsListener.setHandler(this);
		eventPublisher.addListener(ClearSelectionsEvent.class, clearSelectionsListener);

		trendHighlightModeListener = new ConnectionsModeListener();
		trendHighlightModeListener.setHandler(this);
		eventPublisher
				.addListener(ConnectionsModeEvent.class, trendHighlightModeListener);

	}

	@Override
	public void unregisterEventListeners() {
		super.unregisterEventListeners();

		if (addGroupsToVisBricksListener != null) {
			eventPublisher.removeListener(addGroupsToVisBricksListener);
			addGroupsToVisBricksListener = null;
		}

		if (clearSelectionsListener != null) {
			eventPublisher.removeListener(clearSelectionsListener);
			clearSelectionsListener = null;
		}

		if (trendHighlightModeListener != null) {
			eventPublisher.removeListener(trendHighlightModeListener);
			trendHighlightModeListener = null;
		}
	}

	@Override
	public void handleSelectionUpdate(SelectionDelta selectionDelta,
			boolean scrollToSelection, String info) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleRedrawView() {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleClearSelections() {
		clearAllSelections();

	}

	public void clearAllSelections() {
		recordSelectionManager.clearSelections();
		updateConnectionLinesBetweenDimensionGroups();
	}

	@Override
	public void broadcastElements(EVAOperation type) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumberOfSelections(SelectionType SelectionType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<AGLView> getRemoteRenderedViews() {
		return null;
	}

	/**
	 * <p>
	 * Creates visible groups of bricks for the specified list of dimension
	 * group data.
	 * </p>
	 * <p>
	 * As VisBricks can only map between data sets that share a mapping between
	 * records, the imprinting of the IDType and IDCategory for the records is
	 * done here if there is no data set yet.
	 * </p>
	 * 
	 * @param newDataContainers
	 * @param brickConfigurer
	 *            The brick configurer can be specificed externally (e.g.,
	 *            pathways, kaplan meier). If null, the
	 *            {@link NumericalDataConfigurer} will be used.
	 */
	public void addDimensionGroups(List<DataContainer> newDataContainers,
			IBrickConfigurer brickConfigurer) {

		if (newDataContainers == null || newDataContainers.size() == 0) {
			Logger.log(new Status(Status.WARNING, this.toString(),
					"newDataContainers in addDimensionGroups was null or empty"));
			return;
		}

		// if this is the first data container set, we imprint VisBricks
		if (recordIDCategory == null) {
			ATableBasedDataDomain dataDomain = newDataContainers.get(0).getDataDomain();
			imprintVisBricks(dataDomain);
		}

		ArrayList<DimensionGroup> dimensionGroups = dimensionGroupManager
				.getDimensionGroups();

		for (DataContainer dataContainer : newDataContainers) {
			if (!dataContainer.getDataDomain().getRecordIDCategory().equals(recordIDCategory)) {
				Logger.log(new Status(
						Status.ERROR,
						this.toString(),
						"Data container "
								+ dataContainer
								+ "does not match the recordIDCategory of Visbricks - no mapping possible."));
			}
			boolean dimensionGroupExists = false;
			for (DimensionGroup dimensionGroup : dimensionGroups) {
				if (dimensionGroup.getDataContainer().getID() == dataContainer.getID()) {
					dimensionGroupExists = true;
					break;
				}
			}

			if (!dimensionGroupExists) {
				DimensionGroup dimensionGroup = (DimensionGroup) GeneralManager
						.get()
						.getViewManager()
						.createGLView(
								DimensionGroup.class,
								getParentGLCanvas(),
								parentComposite,
								new ViewFrustum(CameraProjectionMode.ORTHOGRAPHIC, 0, 1,
										0, 1, -1, 1));

				/**
				 * If no brick configurer was specified in the
				 * {@link AddGroupsToVisBricksEvent}, then the numerical
				 * configurer is created by default
				 **/
				if (brickConfigurer == null) {
					// FIXME this is a hack to make dataContainers that have
					// only one dimension categorical data
					if (dataContainer.getNrDimensions() == 1) {
						brickConfigurer = new CategoricalDataConfigurer(dataContainer);
					} else {
						brickConfigurer = new NumericalDataConfigurer(dataContainer);
					}
				}

				dimensionGroup.setBrickConfigurer(brickConfigurer);
				dimensionGroup.setDataDomain(dataContainer.getDataDomain());
				dimensionGroup.setDataContainer(dataContainer);
				dimensionGroup.setRemoteRenderingGLView(this);
				dimensionGroup.setVisBricksView(this);
				dimensionGroup.initialize();

				dimensionGroups.add(dimensionGroup);
				dataContainers.add(dataContainer);

				uninitializedDimensionGroups.add(dimensionGroup);
				dataDomains.add(dataContainer.getDataDomain());

			}
		}

		DataContainersChangedEvent event = new DataContainersChangedEvent(this);
		event.setSender(this);
		GeneralManager.get().getEventPublisher().triggerEvent(event);
	}

	/**
	 * Imprints VisBricks to a particular record ID Category by setting the
	 * {@link #recordIDCategory}, and initializes the
	 * {@link #recordSelectionManager}.
	 * 
	 * @param dataDomain
	 */
	private void imprintVisBricks(ATableBasedDataDomain dataDomain) {
		recordIDCategory = dataDomain.getRecordIDCategory();
		IDType mappingRecordIDType = recordIDCategory.getPrimaryMappingType();
		recordSelectionManager = new RecordSelectionManager(IDMappingManagerRegistry
				.get().getIDMappingManager(recordIDCategory), mappingRecordIDType);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

		super.reshape(drawable, x, y, width, height);

		initLayouts();

	}

	@Override
	public void setDisplayListDirty() {
		super.setDisplayListDirty();
	}

	public void moveDimensionGroup(DimensionGroupSpacingRenderer spacer,
			DimensionGroup movedDimGroup, DimensionGroup referenceDimGroup) {
		movedDimGroup.getLayout().reset();
		clearDimensionGroupSpacerHighlight();

		if (movedDimGroup == referenceDimGroup)
			return;

		boolean insertComplete = false;

		ArrayList<DimensionGroup> dimensionGroups = dimensionGroupManager
				.getDimensionGroups();
		for (ElementLayout leftLayout : leftColumnLayout.getElements()) {
			if (spacer == leftLayout.getRenderer()) {

				dimensionGroupManager.setCenterGroupStartIndex(dimensionGroupManager
						.getCenterGroupStartIndex() + 1);

				dimensionGroups.remove(movedDimGroup);
				if (referenceDimGroup == null) {
					dimensionGroups.add(0, movedDimGroup);
				} else {
					dimensionGroups.add(dimensionGroups.indexOf(referenceDimGroup),
							movedDimGroup);
				}

				insertComplete = true;
				break;
			}
		}

		if (!insertComplete) {
			for (ElementLayout rightLayout : rightColumnLayout.getElements()) {
				if (spacer == rightLayout.getRenderer()) {

					dimensionGroupManager.setRightGroupStartIndex(dimensionGroupManager
							.getRightGroupStartIndex() - 1);

					dimensionGroups.remove(movedDimGroup);
					if (referenceDimGroup == null) {
						dimensionGroups.add(dimensionGroups.size(), movedDimGroup);
					} else {
						dimensionGroups.add(
								dimensionGroups.indexOf(referenceDimGroup) + 1,
								movedDimGroup);
					}

					insertComplete = true;
					break;
				}
			}
		}

		if (!insertComplete) {
			for (ElementLayout centerLayout : centerRowLayout.getElements()) {
				if (spacer == centerLayout.getRenderer()) {

					if (dimensionGroups.indexOf(movedDimGroup) < dimensionGroupManager
							.getCenterGroupStartIndex())
						dimensionGroupManager
								.setCenterGroupStartIndex(dimensionGroupManager
										.getCenterGroupStartIndex() - 1);
					else if (dimensionGroups.indexOf(movedDimGroup) >= dimensionGroupManager
							.getRightGroupStartIndex())
						dimensionGroupManager
								.setRightGroupStartIndex(dimensionGroupManager
										.getRightGroupStartIndex() + 1);

					dimensionGroups.remove(movedDimGroup);
					if (referenceDimGroup == null) {
						dimensionGroups.add(
								dimensionGroupManager.getCenterGroupStartIndex(),
								movedDimGroup);
					} else {

						dimensionGroups.add(
								dimensionGroups.indexOf(referenceDimGroup) + 1,
								movedDimGroup);

					}

					insertComplete = true;
					break;
				}
			}
		}

		initLayouts();

		RelationsUpdatedEvent event = new RelationsUpdatedEvent();
		event.setSender(this);
		eventPublisher.triggerEvent(event);
	}

	public void clearDimensionGroupSpacerHighlight() {
		// Clear previous spacer highlights
		for (ElementLayout element : centerRowLayout.getElements()) {
			if (element.getRenderer() instanceof DimensionGroupSpacingRenderer)
				((DimensionGroupSpacingRenderer) element.getRenderer())
						.setRenderSpacer(false);
		}

		for (ElementLayout element : leftColumnLayout.getElements()) {
			if (element.getRenderer() instanceof DimensionGroupSpacingRenderer)
				((DimensionGroupSpacingRenderer) element.getRenderer())
						.setRenderSpacer(false);
		}

		for (ElementLayout element : rightColumnLayout.getElements()) {
			if (element.getRenderer() instanceof DimensionGroupSpacingRenderer)
				((DimensionGroupSpacingRenderer) element.getRenderer())
						.setRenderSpacer(false);
		}
	}

	public DimensionGroupManager getDimensionGroupManager() {
		return dimensionGroupManager;
	}

	public void updateConnectionLinesBetweenDimensionGroups() {

		isConnectionLinesDirty = true;
	}

	private void performConnectionLinesUpdate() {
		connectionBandIDCounter = 0;

		if (centerRowLayout != null) {
			for (ElementLayout elementLayout : centerRowLayout.getElements()) {
				if (elementLayout.getRenderer() instanceof DimensionGroupSpacingRenderer) {
					((DimensionGroupSpacingRenderer) elementLayout.getRenderer()).init();
				}
			}
		}

		isConnectionLinesDirty = false;
	}

	public void updateLayout() {
		isLayoutDirty = true;
	}

	/**
	 * Set whether the last resize of any sub-brick was to the left(true) or to
	 * the right. Important for determining, which dimensionGroup to kick next.
	 * 
	 * @param lastResizeDirectionWasToLeft
	 */
	public void setLastResizeDirectionWasToLeft(boolean lastResizeDirectionWasToLeft) {
		this.lastResizeDirectionWasToLeft = lastResizeDirectionWasToLeft;
	}

	public float getArchTopY() {
		return archTopY;
	}

	public float getArchBottomY() {
		return archBottomY;
	}

	public RecordSelectionManager getRecordSelectionManager() {
		return recordSelectionManager;
	}

	public GLVisBricksKeyListener getKeyListener() {
		return (GLVisBricksKeyListener) glKeyListener;
	}

	public void handleTrendHighlightMode(boolean connectionsOn,
			boolean connectionsHighlightDynamic, float focusFactor) {

		this.connectionsOn = connectionsOn;
		this.connectionsHighlightDynamic = connectionsHighlightDynamic;
		this.connectionsFocusFactor = focusFactor;

		updateConnectionLinesBetweenDimensionGroups();
	}

	public boolean isConnectionsOn() {
		return connectionsOn;
	}

	public boolean isConnectionsHighlightDynamic() {
		return connectionsHighlightDynamic;
	}

	public float getConnectionsFocusFactor() {
		return connectionsFocusFactor;
	}

	public int getSelectedConnectionBandID() {
		return selectedConnectionBandID;
	}

	public HashMap<Integer, RecordVirtualArray> getHashConnectionBandIDToRecordVA() {
		return hashConnectionBandIDToRecordVA;
	}

	private void selectElementsByConnectionBandID(int connectionBandID) {

		recordSelectionManager.clearSelections();

		ClearSelectionsEvent cse = new ClearSelectionsEvent();
		cse.setSender(this);
		eventPublisher.triggerEvent(cse);

		recordSelectionManager.clearSelection(recordSelectionManager.getSelectionType());

		// Create volatile selection type
		volatieBandSelectionType = new SelectionType("Volatile band selection type",
				recordSelectionManager.getSelectionType().getColor(), 1, true, true, 1);

		volatieBandSelectionType.setManaged(false);

		SelectionTypeEvent selectionTypeEvent = new SelectionTypeEvent(
				volatieBandSelectionType);
		GeneralManager.get().getEventPublisher().triggerEvent(selectionTypeEvent);

		RecordVirtualArray recordVA = hashConnectionBandIDToRecordVA
				.get(connectionBandID);
		for (Integer recordID : recordVA) {
			recordSelectionManager.addToType(recordSelectionManager.getSelectionType(),
					recordVA.getIdType(), recordID);
		}

		SelectionUpdateEvent event = new SelectionUpdateEvent();
		event.setSender(this);
		SelectionDelta delta = recordSelectionManager.getDelta();
		event.setSelectionDelta(delta);
		GeneralManager.get().getEventPublisher().triggerEvent(event);

		updateConnectionLinesBetweenDimensionGroups();
	}

	public int getNextConnectionBandID() {
		return connectionBandIDCounter++;
	}

	public RelationAnalyzer getRelationAnalyzer() {
		return relationAnalyzer;
	}

	public float getArchInnerWidth() {
		return archInnerWidth;
	}

	@Override
	public Set<IDataDomain> getDataDomains() {
		return dataDomains;
	}

	@Override
	public boolean isDataView() {
		return true;
	}

	/** Adds the specified data container to the view */
	@Override
	public void setDataContainer(DataContainer dataContainer) {
		List<DataContainer> dataContainerWrapper = new ArrayList<DataContainer>();
		dataContainerWrapper.add(dataContainer);
		addDimensionGroups(dataContainerWrapper, null);
	}

	@Override
	public List<DataContainer> getDataContainers() {
		return dataContainers;
	}

	public int getArchHeight() {
		return ARCH_PIXEL_HEIGHT;
	}

}
