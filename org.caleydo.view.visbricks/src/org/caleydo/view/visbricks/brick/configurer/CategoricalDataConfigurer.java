package org.caleydo.view.visbricks.brick.configurer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.media.opengl.GL2;
import org.caleydo.core.data.container.DataContainer;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.layout.LayoutRenderer;
import org.caleydo.core.view.opengl.layout.util.ViewLayoutRenderer;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import org.caleydo.view.visbricks.PickingType;
import org.caleydo.view.visbricks.brick.EContainedViewType;
import org.caleydo.view.visbricks.brick.GLBrick;
import org.caleydo.view.visbricks.brick.layout.ABrickLayoutConfiguration;
import org.caleydo.view.visbricks.brick.layout.CollapsedBrickLayoutTemplate;
import org.caleydo.view.visbricks.brick.layout.CompactHeaderBrickLayoutTemplate;
import org.caleydo.view.visbricks.brick.layout.DefaultBrickLayoutTemplate;
import org.caleydo.view.visbricks.brick.layout.DetailBrickLayoutTemplate;
import org.caleydo.view.visbricks.brick.layout.HeaderBrickLayoutTemplate;
import org.caleydo.view.visbricks.brick.layout.TitleOnlyHeaderBrickLayoutTemplate;
import org.caleydo.view.visbricks.brick.ui.BrickViewSwitchingButton;
import org.caleydo.view.visbricks.brick.ui.OverviewHeatMapRenderer;
import org.caleydo.view.visbricks.brick.viewcreation.HeatMapCreator;
import org.caleydo.view.visbricks.brick.viewcreation.HistogramCreator;

/**
 * Configurer for bricks to display categroical data
 * 
 * @author Partl
 * 
 */
public class CategoricalDataConfigurer extends ATableBasedDataConfigurer {

	protected static final int HEATMAP_BUTTON_ID = 1;
	protected static final int HISTOGRAM_BUTTON_ID = 3;

	public CategoricalDataConfigurer(DataContainer dataContainer) {
		super(dataContainer);
	}

	@Override
	public void configure(HeaderBrickLayoutTemplate layoutTemplate) {

		BrickViewSwitchingButton histogramButton = new BrickViewSwitchingButton(
				PickingType.BRICK_TOOLBAR_VIEW_SWITCHING_BUTTONS.name(),
				HISTOGRAM_BUTTON_ID, EIconTextures.HISTOGRAM_ICON,
				EContainedViewType.HISTOGRAM_VIEW);

		ArrayList<BrickViewSwitchingButton> viewSwitchingButtons = new ArrayList<BrickViewSwitchingButton>();
		viewSwitchingButtons.add(histogramButton);

		ArrayList<ElementLayout> headerBarElements = createHeaderBarElements(layoutTemplate);
		ArrayList<ElementLayout> toolBarElements = createToolBarElements(layoutTemplate,
				viewSwitchingButtons);
		ArrayList<ElementLayout> footerBarElements = createFooterBarElements(layoutTemplate);

		layoutTemplate.setHeaderBarElements(headerBarElements);
		layoutTemplate.setToolBarElements(toolBarElements);
		layoutTemplate.setFooterBarElements(footerBarElements);

		HashSet<EContainedViewType> validViewTypes = new HashSet<EContainedViewType>();
		validViewTypes.add(EContainedViewType.HISTOGRAM_VIEW);
		validViewTypes.add(EContainedViewType.PARCOORDS_VIEW);
		validViewTypes.add(EContainedViewType.OVERVIEW_HEATMAP);

		layoutTemplate.setValidViewTypes(validViewTypes);
		layoutTemplate.setDefaultViewType(EContainedViewType.HISTOGRAM_VIEW);

		layoutTemplate.showFooterBar(false);
		layoutTemplate.showToolBar(false);

	}

	@Override
	public void configure(CollapsedBrickLayoutTemplate layoutTemplate) {

		HashSet<EContainedViewType> validViewTypes = new HashSet<EContainedViewType>();
		validViewTypes.add(EContainedViewType.OVERVIEW_HEATMAP_COMPACT);

		layoutTemplate.setValidViewTypes(validViewTypes);
		layoutTemplate.setDefaultViewType(EContainedViewType.OVERVIEW_HEATMAP_COMPACT);

		ArrayList<ElementLayout> footerBarElements = createFooterBarElements(layoutTemplate);
		layoutTemplate.setFooterBarElements(footerBarElements);
	}

	@Override
	public void configure(DefaultBrickLayoutTemplate layoutTemplate) {

		BrickViewSwitchingButton heatMapButton = new BrickViewSwitchingButton(
				PickingType.BRICK_TOOLBAR_VIEW_SWITCHING_BUTTONS.name(),
				HEATMAP_BUTTON_ID, EIconTextures.HEAT_MAP_ICON,
				EContainedViewType.HEATMAP_VIEW);

		ArrayList<BrickViewSwitchingButton> viewSwitchingButtons = new ArrayList<BrickViewSwitchingButton>();
		viewSwitchingButtons.add(heatMapButton);

		ArrayList<ElementLayout> headerBarElements = createHeaderBarElements(layoutTemplate);
		if (headerBarElements != null)
			layoutTemplate.setHeaderBarElements(headerBarElements);

		ArrayList<ElementLayout> toolBarElements = createToolBarElements(layoutTemplate,
				viewSwitchingButtons);
		layoutTemplate.setToolBarElements(toolBarElements);

		ArrayList<ElementLayout> footerBarElements = createFooterBarElements(layoutTemplate);
		layoutTemplate.setFooterBarElements(footerBarElements);

		HashSet<EContainedViewType> validViewTypes = new HashSet<EContainedViewType>();
		validViewTypes.add(EContainedViewType.HEATMAP_VIEW);

		layoutTemplate.setValidViewTypes(validViewTypes);
		layoutTemplate.setDefaultViewType(EContainedViewType.HEATMAP_VIEW);

		layoutTemplate.setShowToolBar(false);
		layoutTemplate.setShowFooterBar(false);

	}

	@Override
	public void configure(DetailBrickLayoutTemplate layoutTemplate) {
		BrickViewSwitchingButton heatMapButton = new BrickViewSwitchingButton(
				PickingType.BRICK_TOOLBAR_VIEW_SWITCHING_BUTTONS.name(),
				HEATMAP_BUTTON_ID, EIconTextures.HEAT_MAP_ICON,
				EContainedViewType.HEATMAP_VIEW);

		ArrayList<BrickViewSwitchingButton> viewSwitchingButtons = new ArrayList<BrickViewSwitchingButton>();
		viewSwitchingButtons.add(heatMapButton);

		ArrayList<ElementLayout> toolBarElements = createToolBarElements(layoutTemplate,
				viewSwitchingButtons);
		layoutTemplate.setToolBarElements(toolBarElements);

		ArrayList<ElementLayout> footerBarElements = createFooterBarElements(layoutTemplate);
		layoutTemplate.setFooterBarElements(footerBarElements);

		HashSet<EContainedViewType> validViewTypes = new HashSet<EContainedViewType>();
		validViewTypes.add(EContainedViewType.HEATMAP_VIEW);

		layoutTemplate.setValidViewTypes(validViewTypes);
		layoutTemplate.setDefaultViewType(EContainedViewType.HEATMAP_VIEW);

		layoutTemplate.showFooterBar(true);
	}

	@Override
	public void setBrickViews(GLBrick brick, GL2 gl, GLMouseListener glMouseListener,
			ABrickLayoutConfiguration brickLayout) {

		HashMap<EContainedViewType, AGLView> views = new HashMap<EContainedViewType, AGLView>();
		HashMap<EContainedViewType, LayoutRenderer> containedViewRenderers = new HashMap<EContainedViewType, LayoutRenderer>();

		if (!(brickLayout instanceof HeaderBrickLayoutTemplate)) {
			HeatMapCreator heatMapCreator = new HeatMapCreator();
			AGLView heatMap = heatMapCreator.createRemoteView(brick, gl, glMouseListener);
			LayoutRenderer heatMapLayoutRenderer = new ViewLayoutRenderer(heatMap);
			views.put(EContainedViewType.HEATMAP_VIEW, heatMap);
			containedViewRenderers.put(EContainedViewType.HEATMAP_VIEW,
					heatMapLayoutRenderer);
		}

		HistogramCreator histogramCreator = new HistogramCreator();
		AGLView histogram = histogramCreator.createRemoteView(brick, gl, glMouseListener);
		LayoutRenderer histogramLayoutRenderer = new ViewLayoutRenderer(histogram);
		views.put(EContainedViewType.HISTOGRAM_VIEW, histogram);
		containedViewRenderers.put(EContainedViewType.HISTOGRAM_VIEW,
				histogramLayoutRenderer);
		
		LayoutRenderer overviewHeatMapRenderer = new OverviewHeatMapRenderer(
				brick.getDataContainer(), brick.getDataDomain().getTable(), true);
		containedViewRenderers.put(EContainedViewType.OVERVIEW_HEATMAP,
				overviewHeatMapRenderer);
		LayoutRenderer compactOverviewHeatMapRenderer = new OverviewHeatMapRenderer(
				brick.getDataContainer(), brick.getDataDomain().getTable(), false);
		containedViewRenderers.put(EContainedViewType.OVERVIEW_HEATMAP_COMPACT,
				compactOverviewHeatMapRenderer);

		brick.setViews(views);
		brick.setContainedViewRenderers(containedViewRenderers);

	}

	@Override
	public void configure(CompactHeaderBrickLayoutTemplate layoutTemplate) {
		HashSet<EContainedViewType> validViewTypes = new HashSet<EContainedViewType>();
		validViewTypes.add(EContainedViewType.OVERVIEW_HEATMAP_COMPACT);

		layoutTemplate.setValidViewTypes(validViewTypes);
		layoutTemplate.setDefaultViewType(EContainedViewType.OVERVIEW_HEATMAP_COMPACT);

		ArrayList<ElementLayout> headerBarElements = createHeaderBarElements(layoutTemplate);
		layoutTemplate.setHeaderBarElements(headerBarElements);
		ArrayList<ElementLayout> footerBarElements = createFooterBarElements(layoutTemplate);
		layoutTemplate.setFooterBarElements(footerBarElements);

		layoutTemplate.showFooterBar(true);
	}

	@Override
	public void configure(TitleOnlyHeaderBrickLayoutTemplate layoutTemplate) {
		HashSet<EContainedViewType> validViewTypes = new HashSet<EContainedViewType>();
		validViewTypes.add(EContainedViewType.OVERVIEW_HEATMAP_COMPACT);

		layoutTemplate.setValidViewTypes(validViewTypes);
		layoutTemplate.setDefaultViewType(EContainedViewType.OVERVIEW_HEATMAP_COMPACT);

		ArrayList<ElementLayout> headerBarElements = createHeaderBarElements(layoutTemplate);
		layoutTemplate.setHeaderBarElements(headerBarElements);
	}
	
	@Override
	public boolean useDefaultWidth() {
		return true;
	}

	@Override
	public int getDefaultWidth() {
		return 50;
	}
}
