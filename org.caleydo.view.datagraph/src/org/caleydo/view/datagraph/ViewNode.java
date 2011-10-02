package org.caleydo.view.datagraph;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.media.opengl.GL2;

import org.caleydo.core.data.container.ADimensionGroupData;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.camera.CameraProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.Column;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.layout.LayoutManager;
import org.caleydo.core.view.opengl.layout.LayoutTemplate;
import org.caleydo.core.view.opengl.layout.Row;
import org.caleydo.core.view.opengl.layout.util.BorderedAreaRenderer;
import org.caleydo.core.view.opengl.layout.util.LabelRenderer;
import org.caleydo.core.view.opengl.layout.util.LineSeparatorRenderer;
import org.caleydo.core.view.opengl.layout.util.TextureRenderer;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingType;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.core.view.opengl.util.draganddrop.IDraggable;
import org.caleydo.core.view.opengl.util.draganddrop.IDropArea;
import org.caleydo.view.visbricks.GLVisBricks;
import org.caleydo.view.visbricks.event.AddGroupsToVisBricksEvent;

public class ViewNode extends ADraggableDataGraphNode implements IDropArea {

	private final static int SPACING_PIXELS = 4;
	private final static int CAPTION_HEIGHT_PIXELS = 16;
	private final static int LINE_SEPARATOR_HEIGHT_PIXELS = 3;
	private final static int OVERVIEW_COMP_GROUP_HEIGHT_PIXELS = 32;

	private LayoutManager layoutManager;
	private OverviewDataContainerRenderer compGroupOverviewRenderer;
	private AGLView representedView;
	private Set<IDataDomain> dataDomains;
	private List<ADimensionGroupData> dimensionGroups;
	private String title;
	private String iconPath;

	public ViewNode(ForceDirectedGraphLayout graphLayout, GLDataGraph view,
			DragAndDropController dragAndDropController, int id,
			AGLView representedView, String title, String iconPath) {
		super(graphLayout, view, dragAndDropController, id);

		dimensionGroups = new ArrayList<ADimensionGroupData>();
		dimensionGroups.add(new FakeDimensionGroupData(0));
		dimensionGroups.add(new FakeDimensionGroupData(1));
		dimensionGroups.add(new FakeDimensionGroupData(2));
		dimensionGroups.add(new FakeDimensionGroupData(5));
		dimensionGroups.add(new FakeDimensionGroupData(4));

		this.representedView = representedView;
		this.title = title;
		this.iconPath = iconPath;

		// TODO: this is not nice
		if (representedView instanceof GLVisBricks) {
			view.addSingleIDPickingListener(new APickingListener() {

				@Override
				public void dragged(Pick pick) {
					DragAndDropController dragAndDropController = ViewNode.this.dragAndDropController;
					if (dragAndDropController.isDragging()
							&& dragAndDropController.getDraggingMode().equals(
									"DimensionGroupDrag")) {
						dragAndDropController.setDropArea(ViewNode.this);
					}
				}
			}, PickingType.DATA_GRAPH_NODE.name(), id);
		}

		setupLayout();
	}

	private void setupLayout() {
		layoutManager = new LayoutManager(new ViewFrustum());
		LayoutTemplate layoutTemplate = new LayoutTemplate();

		Row baseRow = new Row("baseRow");

		baseRow.setFrameColor(0, 0, 1, 0);

		baseRow.setRenderer(new BorderedAreaRenderer(view,
				PickingType.DATA_GRAPH_NODE, id));

		ElementLayout spacingLayoutX = new ElementLayout("spacingLayoutX");
		spacingLayoutX.setPixelGLConverter(pixelGLConverter);
		spacingLayoutX.setPixelSizeX(SPACING_PIXELS);
		spacingLayoutX.setRatioSizeY(0);

		Column baseColumn = new Column();
		baseColumn.setPixelGLConverter(pixelGLConverter);

		baseRow.append(spacingLayoutX);
		baseRow.append(baseColumn);
		baseRow.append(spacingLayoutX);

		Row titleRow = new Row("titleRow");
		titleRow.setYDynamic(true);

		if (iconPath != null) {
			ElementLayout iconLayout = new ElementLayout("icon");
			iconLayout.setPixelGLConverter(pixelGLConverter);
			iconLayout.setPixelSizeX(CAPTION_HEIGHT_PIXELS);
			iconLayout.setPixelSizeY(CAPTION_HEIGHT_PIXELS);
			iconLayout.setRenderer(new TextureRenderer(iconPath, view
					.getTextureManager(), true));
			titleRow.append(iconLayout);
			titleRow.append(spacingLayoutX);
		}

		ElementLayout captionLayout = new ElementLayout("caption");
		captionLayout.setPixelGLConverter(pixelGLConverter);
		captionLayout.setPixelSizeY(CAPTION_HEIGHT_PIXELS);
		captionLayout.setRatioSizeX(1);
		captionLayout.setRenderer(new LabelRenderer(view, title,
				PickingType.DATA_GRAPH_NODE, id));

		titleRow.append(captionLayout);

		ElementLayout lineSeparatorLayout = new ElementLayout("lineSeparator");
		lineSeparatorLayout.setPixelGLConverter(pixelGLConverter);
		lineSeparatorLayout.setPixelSizeY(LINE_SEPARATOR_HEIGHT_PIXELS);
		lineSeparatorLayout.setRatioSizeX(1);
		lineSeparatorLayout.setRenderer(new LineSeparatorRenderer(false));

		Row bodyRow = new Row("bodyRow");
		bodyRow.addBackgroundRenderer(new ViewNodeBackGroundRenderer(
				new float[] { 1, 1, 1, 1 }, iconPath, view.getTextureManager(),
				true));

		Column bodyColumn = new Column("bodyColumn");

		ElementLayout compGroupLayout = new ElementLayout("compGroupOverview");
		compGroupOverviewRenderer = new OverviewDataContainerRenderer(this,
				view, dragAndDropController, getDimensionGroups());
		compGroupLayout.setPixelGLConverter(pixelGLConverter);
		compGroupLayout.setPixelSizeY(OVERVIEW_COMP_GROUP_HEIGHT_PIXELS);
		// compGroupLayout.setPixelSizeX(compGroupOverviewRenderer.getMinWidthPixels());
		compGroupLayout.setRenderer(compGroupOverviewRenderer);

		ElementLayout spacingLayoutY = new ElementLayout("spacingY");
		spacingLayoutY.setPixelGLConverter(pixelGLConverter);
		spacingLayoutY.setPixelSizeY(SPACING_PIXELS);
		spacingLayoutY.setRatioSizeX(0);

		bodyColumn.append(compGroupLayout);
		bodyColumn.append(spacingLayoutY);

		bodyRow.append(bodyColumn);

		baseColumn.append(spacingLayoutY);
		baseColumn.append(bodyRow);
		baseColumn.append(spacingLayoutY);
		baseColumn.append(lineSeparatorLayout);
		baseColumn.append(titleRow);
		baseColumn.append(spacingLayoutY);
		layoutTemplate.setBaseElementLayout(baseRow);
		layoutManager.setTemplate(layoutTemplate);
	}

	@Override
	public List<ADimensionGroupData> getDimensionGroups() {
//		List<ADimensionGroupData> groups = representedView.getDimensionGroups();
//		if (groups == null) {
//			groups = new ArrayList<ADimensionGroupData>();
//		}
		
		List<ADimensionGroupData> groups = new ArrayList<ADimensionGroupData>();
		FakeDimensionGroupData data = new FakeDimensionGroupData(0);
		data.setDimensionPerspectiveID("ColumnPerspec2");
		data.setRecordPerspectiveID("Row1");
		groups.add(data);

		data = new FakeDimensionGroupData(1);
		data.setDimensionPerspectiveID("ColumnPerspec2");
		data.setRecordPerspectiveID("AnotherRow");
		groups.add(data);

		data = new FakeDimensionGroupData(2);
		data.setDimensionPerspectiveID("ColumnPerspec2");
		data.setRecordPerspectiveID("YetAnotherRow");
		groups.add(data);

		data = new FakeDimensionGroupData(3);
		data.setDimensionPerspectiveID("ColumnPerspec2");
		data.setRecordPerspectiveID("RowPerspec2");
		groups.add(data);

		data = new FakeDimensionGroupData(4);
		data.setDimensionPerspectiveID("AnotherColumn2");
		data.setRecordPerspectiveID("Row1");
		groups.add(data);
		
		data = new FakeDimensionGroupData(5);
		data.setDimensionPerspectiveID("YetAnotherColumn2");
		data.setRecordPerspectiveID("YetAnotherRow");
		groups.add(data);

		return groups;
	}

	@Override
	public void render(GL2 gl) {
		Point2D position = graphLayout.getNodePosition(this, true);
		float x = pixelGLConverter.getGLWidthForPixelWidth((int) position
				.getX());
		float y = pixelGLConverter.getGLHeightForPixelHeight((int) position
				.getY());
		float spacingWidth = pixelGLConverter
				.getGLWidthForPixelWidth(getWidthPixels());
		float spacingHeight = pixelGLConverter
				.getGLHeightForPixelHeight(getHeightPixels());
		gl.glPushMatrix();
		gl.glTranslatef(x - spacingWidth / 2.0f, y - spacingHeight / 2.0f, 0f);

		// layoutManager.setViewFrustum(new ViewFrustum(
		// ECameraProjectionMode.ORTHOGRAPHIC, x - spacingWidth, x
		// + spacingWidth, y - spacingHeight, y + spacingHeight,
		// -1, 20));
		layoutManager.setViewFrustum(new ViewFrustum(
				CameraProjectionMode.ORTHOGRAPHIC, 0, spacingWidth, 0,
				spacingHeight, -1, 20));

		layoutManager.render(gl);
		gl.glPopMatrix();
	}

	@Override
	public int getHeightPixels() {
		return 4 * SPACING_PIXELS + CAPTION_HEIGHT_PIXELS
				+ LINE_SEPARATOR_HEIGHT_PIXELS
				+ OVERVIEW_COMP_GROUP_HEIGHT_PIXELS;
	}

	@Override
	public int getWidthPixels() {
		return 2 * SPACING_PIXELS
				+ Math.max(200, compGroupOverviewRenderer.getMinWidthPixels());
	}

	@Override
	public Pair<Point2D, Point2D> getTopDimensionGroupAnchorPoints(
			ADimensionGroupData dimensionGroup) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pair<Point2D, Point2D> getBottomDimensionGroupAnchorPoints(
			ADimensionGroupData dimensionGroup) {
		Point2D position = graphLayout.getNodePosition(this, true);
		float x = pixelGLConverter.getGLWidthForPixelWidth((int) position
				.getX());
		float y = pixelGLConverter.getGLHeightForPixelHeight((int) position
				.getY());
		float width = pixelGLConverter
				.getGLWidthForPixelWidth(getWidthPixels());
		float height = pixelGLConverter
				.getGLHeightForPixelHeight(getHeightPixels());
		float spacingX = pixelGLConverter
				.getGLWidthForPixelWidth(SPACING_PIXELS);
		float spacingY = pixelGLConverter
				.getGLHeightForPixelHeight(SPACING_PIXELS);

		Pair<Point2D, Point2D> anchorPoints = compGroupOverviewRenderer
				.getAnchorPointsOfDimensionGroup(dimensionGroup);

		Point2D first = (Point2D) anchorPoints.getFirst().clone();
		Point2D second = (Point2D) anchorPoints.getSecond().clone();

		first.setLocation(anchorPoints.getFirst().getX() + x - width / 2.0f
				+ spacingX, anchorPoints.getFirst().getY() + y - height / 2.0f
				+ spacingY);
		second.setLocation(anchorPoints.getSecond().getX() + x - width / 2.0f
				+ spacingX, anchorPoints.getSecond().getY() + y - height / 2.0f
				+ spacingY);

		return new Pair<Point2D, Point2D>(first, second);
	}

	@Override
	public Pair<Point2D, Point2D> getTopAnchorPoints() {
		Point2D position = graphLayout.getNodePosition(this, true);
		float x = pixelGLConverter.getGLWidthForPixelWidth((int) position
				.getX());
		float y = pixelGLConverter.getGLHeightForPixelHeight((int) position
				.getY());
		float width = pixelGLConverter
				.getGLWidthForPixelWidth(getWidthPixels());
		float height = pixelGLConverter
				.getGLHeightForPixelHeight(getHeightPixels());

		Pair<Point2D, Point2D> anchorPoints = new Pair<Point2D, Point2D>();

		anchorPoints.setFirst(new Point2D.Float(x - width / 2.0f, y + height
				/ 2.0f));
		anchorPoints.setSecond(new Point2D.Float(x + width / 2.0f, y + height
				/ 2.0f));

		return anchorPoints;
	}

	@Override
	public Pair<Point2D, Point2D> getBottomAnchorPoints() {
		Point2D position = graphLayout.getNodePosition(this, true);
		float x = pixelGLConverter.getGLWidthForPixelWidth((int) position
				.getX());
		float y = pixelGLConverter.getGLHeightForPixelHeight((int) position
				.getY());
		float width = pixelGLConverter
				.getGLWidthForPixelWidth(getWidthPixels());
		float height = pixelGLConverter
				.getGLHeightForPixelHeight(getHeightPixels());

		Pair<Point2D, Point2D> anchorPoints = new Pair<Point2D, Point2D>();

		anchorPoints.setFirst(new Point2D.Float(x - width / 2.0f, y - height
				/ 2.0f));
		anchorPoints.setSecond(new Point2D.Float(x + width / 2.0f, y - height
				/ 2.0f));

		return anchorPoints;
	}

	public void setDataDomains(Set<IDataDomain> dataDomains) {
		this.dataDomains = dataDomains;
	}

	public Set<IDataDomain> getDataDomains() {
		return dataDomains;
	}

	@Override
	public Pair<Point2D, Point2D> getLeftAnchorPoints() {
		Point2D position = graphLayout.getNodePosition(this, true);
		float x = pixelGLConverter.getGLWidthForPixelWidth((int) position
				.getX());
		float y = pixelGLConverter.getGLHeightForPixelHeight((int) position
				.getY());
		float width = pixelGLConverter
				.getGLWidthForPixelWidth(getWidthPixels());
		float height = pixelGLConverter
				.getGLHeightForPixelHeight(getHeightPixels());

		Pair<Point2D, Point2D> anchorPoints = new Pair<Point2D, Point2D>();

		anchorPoints.setFirst(new Point2D.Float(x - width / 2.0f, y + height
				/ 2.0f));
		anchorPoints.setSecond(new Point2D.Float(x - width / 2.0f, y - height
				/ 2.0f));

		return anchorPoints;
	}

	@Override
	public Pair<Point2D, Point2D> getRightAnchorPoints() {
		Point2D position = graphLayout.getNodePosition(this, true);
		float x = pixelGLConverter.getGLWidthForPixelWidth((int) position
				.getX());
		float y = pixelGLConverter.getGLHeightForPixelHeight((int) position
				.getY());
		float width = pixelGLConverter
				.getGLWidthForPixelWidth(getWidthPixels());
		float height = pixelGLConverter
				.getGLHeightForPixelHeight(getHeightPixels());

		Pair<Point2D, Point2D> anchorPoints = new Pair<Point2D, Point2D>();

		anchorPoints.setFirst(new Point2D.Float(x + width / 2.0f, y + height
				/ 2.0f));
		anchorPoints.setSecond(new Point2D.Float(x + width / 2.0f, y - height
				/ 2.0f));

		return anchorPoints;
	}

	@Override
	public Point2D getPosition() {
		Point2D position = graphLayout.getNodePosition(this, true);
		float x = pixelGLConverter.getGLWidthForPixelWidth((int) position
				.getX());
		float y = pixelGLConverter.getGLHeightForPixelHeight((int) position
				.getY());
		return new Point2D.Float(x, y);
	}

	@Override
	public float getHeight() {
		return pixelGLConverter.getGLHeightForPixelHeight(getHeightPixels());
	}

	@Override
	public float getWidth() {
		return pixelGLConverter.getGLWidthForPixelWidth(getWidthPixels());
	}

	public AGLView getRepresentedView() {
		return representedView;
	}

	@Override
	public void handleDragOver(GL2 gl, Set<IDraggable> draggables,
			float mouseCoordinateX, float mouseCoordinateY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleDrop(GL2 gl, Set<IDraggable> draggables,
			float mouseCoordinateX, float mouseCoordinateY,
			DragAndDropController dragAndDropController) {
		ArrayList<ADimensionGroupData> dimensionGroupData = new ArrayList<ADimensionGroupData>();
		for (IDraggable draggable : draggables) {
			if (draggable instanceof DimensionGroupRenderer) {
				DimensionGroupRenderer comparisonGroupRepresentation = (DimensionGroupRenderer) draggable;
				dimensionGroupData.add(comparisonGroupRepresentation
						.getDimensionGroupData());
			}
		}

		if (!dimensionGroupData.isEmpty()) {
			AddGroupsToVisBricksEvent event = new AddGroupsToVisBricksEvent();
			event.setDimensionGroupData(dimensionGroupData);
			event.setSender(this);
			GeneralManager.get().getEventPublisher().triggerEvent(event);
		}

		dragAndDropController.clearDraggables();

	}

	@Override
	public void update() {
		compGroupOverviewRenderer.setDimensionGroups(getDimensionGroups());
	}

}
