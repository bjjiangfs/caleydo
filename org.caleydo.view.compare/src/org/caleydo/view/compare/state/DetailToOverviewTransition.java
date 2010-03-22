package org.caleydo.view.compare.state;

import gleem.linalg.Vec3f;

import java.util.ArrayList;

import javax.media.opengl.GL;

import org.caleydo.core.manager.IUseCase;
import org.caleydo.core.manager.picking.PickingManager;
import org.caleydo.core.manager.usecase.EDataDomain;
import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.util.animation.MovementVector2;
import org.caleydo.core.view.opengl.util.animation.MovementVector3;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.core.view.opengl.util.texture.TextureManager;
import org.caleydo.view.compare.GLCompare;
import org.caleydo.view.compare.HeatMapWrapper;
import org.caleydo.view.compare.SetBar;
import org.caleydo.view.compare.layout.AHeatMapLayout;
import org.caleydo.view.compare.layout.HeatMapLayoutConfigurable;
import org.caleydo.view.compare.rendercommand.RenderCommandFactory;

import com.sun.opengl.util.j2d.TextRenderer;

public class DetailToOverviewTransition extends ACompareViewStateTransition {

	private ArrayList<AHeatMapLayout> destinationLayouts;

	public DetailToOverviewTransition(GLCompare view, int viewID,
			TextRenderer textRenderer, TextureManager textureManager,
			PickingManager pickingManager, GLMouseListener glMouseListener,
			SetBar setBar, RenderCommandFactory renderCommandFactory,
			EDataDomain dataDomain, IUseCase useCase,
			DragAndDropController dragAndDropController,
			CompareViewStateController compareViewStateController) {
		super(view, viewID, textRenderer, textureManager, pickingManager,
				glMouseListener, setBar, renderCommandFactory, dataDomain,
				useCase, dragAndDropController, compareViewStateController);
		animationDuration = 0.5f;
	}

	@Override
	public void drawActiveElements(GL gl) {
		if (animationStarted) {
			for (HeatMapWrapper heatMapWrapper : heatMapWrappers) {
				heatMapWrapper.drawRemoteItems(gl, glMouseListener,
						pickingManager);
			}
		}
	}

	@Override
	public void drawDisplayListElements(GL gl) {
		if (animationStarted) {
			for (HeatMapWrapper heatMapWrapper : heatMapWrappers) {
				heatMapWrapper.drawLocalItems(gl, textureManager,
						pickingManager, glMouseListener, viewID);
			}

			IViewFrustum viewFrustum = view.getViewFrustum();

			setBar.setWidth(viewFrustum.getWidth());
			setBar.render(gl);

			if (areTargetsReached()) {
				finish();
			}
		}
	}

	protected void finish() {
		for (int i = 0; i < heatMapWrappers.size(); i++) {
			HeatMapWrapper heatMapWrapper = heatMapWrappers.get(i);
			AHeatMapLayout layout = destinationLayouts.get(i);
			heatMapWrapper.setLayout(layout);
		}

		compareViewStateController
				.setCurrentState(ECompareViewStateType.OVERVIEW);
		view.setDisplayListDirty();
		animationStarted = false;
	}

	@Override
	public ECompareViewStateType getStateType() {
		return ECompareViewStateType.DETAIL_TO_OVERVIEW_TRANSITION;
	}

	@Override
	public void init(GL gl) {
		isInitialized = true;

		ACompareViewState overviewState = compareViewStateController
				.getState(ECompareViewStateType.OVERVIEW);
		ACompareViewState detailViewState = compareViewStateController
				.getState(ECompareViewStateType.DETAIL_VIEW);

		setBar.setViewState(overviewState);
		setBar.adjustSelectionWindowSizeCentered(overviewState
				.getNumSetsInFocus());
		setBar.setMaxSelectedItems(overviewState.getMaxSetsInFocus());
		setBar.setMinSelectedItems(overviewState.getMinSetsInFocus());
		if (!overviewState.isInitialized()) {
			overviewState.init(gl);
		}
		overviewState.setSetsInFocus(setBar.getSetsInFocus());

		heatMapWrappers = overviewState.getHeatMapWrappers();

		ArrayList<HeatMapWrapper> sourceHeatMapWrappers = detailViewState
				.getHeatMapWrappers();

		int indexOffset = 0;

		for (int i = 0; i < heatMapWrappers.size(); i++) {
			HeatMapWrapper heatMapWrapper = heatMapWrappers.get(i);
			if (heatMapWrapper.getSet().getID() == sourceHeatMapWrappers.get(0)
					.getSet().getID()) {
				indexOffset = i;
				break;
			}
		}

		layouts.clear();
		captionPositions.clear();
		captionTextDimensions.clear();
		captionTextSpacing.clear();
		heatMapDimensions.clear();
		heatMapPositions.clear();

		ArrayList<AHeatMapLayout> sourceLayouts = detailViewState.getLayouts();
		destinationLayouts = overviewState.getLayouts();
		ArrayList<AHeatMapLayout> focusLayouts = new ArrayList<AHeatMapLayout>();

		for (int i = 0; i < sourceLayouts.size(); i++) {
			AHeatMapLayout srcLayout = sourceLayouts.get(i);
			AHeatMapLayout destLayout = destinationLayouts.get(indexOffset + i);
			HeatMapLayoutConfigurable transitionLayout = new HeatMapLayoutConfigurable(
					renderCommandFactory);
			transitionLayout.setLocalRenderCommands(destLayout
					.getRenderCommandsOfLocalItems());
			transitionLayout.setRemoteRenderCommands(destLayout
					.getRenderCommandsOfRemoteItems());
			focusLayouts.add(transitionLayout);

			createMovementValues(gl, indexOffset + i, srcLayout, destLayout);

			if (i == 0) {
				createOffsets(true, indexOffset + i);
			} else if (i == sourceLayouts.size() - 1) {
				createOffsets(false, indexOffset + i);
			}
		}

		for (int i = 0; i < destinationLayouts.size(); i++) {
			AHeatMapLayout destLayout = destinationLayouts.get(i);
			if (i < indexOffset) {
				createMovementValuesSourceOffset(gl, i, destLayout, true);
				HeatMapLayoutConfigurable transitionLayout = new HeatMapLayoutConfigurable(
						renderCommandFactory);
				transitionLayout.setLocalRenderCommands(destLayout
						.getRenderCommandsOfLocalItems());
				transitionLayout.setRemoteRenderCommands(destLayout
						.getRenderCommandsOfRemoteItems());
				layouts.add(transitionLayout);
			} else if (i > indexOffset + sourceLayouts.size() - 1) {
				createMovementValuesSourceOffset(gl, i, destLayout, false);
				HeatMapLayoutConfigurable transitionLayout = new HeatMapLayoutConfigurable(
						renderCommandFactory);
				transitionLayout.setLocalRenderCommands(destLayout
						.getRenderCommandsOfLocalItems());
				transitionLayout.setRemoteRenderCommands(destLayout
						.getRenderCommandsOfRemoteItems());
				layouts.add(transitionLayout);
			} else {
				layouts.add(focusLayouts.get(i - indexOffset));
			}
			heatMapWrappers.get(i).setLayout(layouts.get(i));
			layouts.get(i).setHeatMapWrapper(heatMapWrappers.get(i));
		}
		view.setDisplayListDirty();
	}

	protected void createMovementValuesSourceOffset(GL gl, int id,
			AHeatMapLayout destLayout, boolean isLowerOffset) {

		int index = isLowerOffset ? 0 : 1;

		HeatMapWrapper heatMapWrapper = heatMapWrappers.get(id);

		float textWidth = getCaptionLabelTextWidth(gl, heatMapWrapper
				.getCaption(), destLayout);
		Vec3f captionTargetPosition = destLayout
				.getCaptionLabelPosition(textWidth);
		Vec3f captionStartPosition = new Vec3f(captionTargetPosition.x()
				- captionPositionOffset[index].x(), captionTargetPosition.y()
				- captionPositionOffset[index].y(), captionTargetPosition.z()
				- captionPositionOffset[index].z());

		float captionTargetWidth = destLayout.getCaptionLabelWidth();
		float captionTargetHeight = destLayout.getCaptionLabelHeight();
		float captionStartWidth = captionTargetWidth
				- captionTextDimensionsOffset[index].x();
		float captionStartHeight = captionTargetHeight
				- captionTextDimensionsOffset[index].y();

		float captionTargetSpacingX = destLayout
				.getCaptionLabelHorizontalSpacing();
		float captionTargetSpacingY = destLayout
				.getCaptionLabelVerticalSpacing();
		float captionStartSpacingX = captionTargetSpacingX
				- captionTextSpacingOffset[index].x();
		float captionStartSpacingY = captionTargetSpacingY
				- captionTextSpacingOffset[index].y();

		Vec3f heatMapTargetPosition = destLayout.getOverviewHeatMapPosition();
		Vec3f heatMapStartPosition = new Vec3f(heatMapTargetPosition.x()
				- heatMapPositionOffset[index].x(), heatMapTargetPosition.y()
				- heatMapPositionOffset[index].y(), heatMapTargetPosition.z()
				- heatMapPositionOffset[index].z());

		float heatMapTargetWidth = destLayout.getOverviewHeatMapWidth();
		float heatMapTargetHeight = destLayout.getOverviewHeight();
		float heatMapStartWidth = heatMapTargetWidth
				- heatMapDimensionsOffset[index].x();
		float heatMapStartHeight = heatMapTargetHeight
				- heatMapDimensionsOffset[index].y();

		MovementVector3 captionPosition = new MovementVector3(
				captionStartPosition, captionTargetPosition, animationDuration);
		captionPositions.put(id, captionPosition);

		MovementVector2 captionDimenstions = new MovementVector2(
				captionStartWidth, captionTargetWidth, captionStartHeight,
				captionTargetHeight, animationDuration);
		captionTextDimensions.put(id, captionDimenstions);

		MovementVector2 captionSpacings = new MovementVector2(
				captionStartSpacingX, captionTargetSpacingX,
				captionStartSpacingY, captionTargetSpacingY, animationDuration);
		captionTextSpacing.put(id, captionSpacings);

		MovementVector3 heatMapPosition = new MovementVector3(
				heatMapStartPosition, heatMapTargetPosition, animationDuration);
		heatMapPositions.put(id, heatMapPosition);

		MovementVector2 heatMapDims = new MovementVector2(heatMapStartWidth,
				heatMapTargetWidth, heatMapStartHeight, heatMapTargetHeight,
				animationDuration);
		heatMapDimensions.put(id, heatMapDims);
	}

}
