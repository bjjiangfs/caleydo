package org.caleydo.core.view.opengl.canvas.radial;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.data.selection.ESelectionType;

/**
 * This class represents the animation where a selected partial disc pops out to the detail View. When the
 * animation is finished the follow up drawing state ({@link DrawingStateDetailOutside}) will become active.
 * 
 * @author Christian Partl
 */
@XmlType
public class AnimationPopOutDetailOutside
	extends ADrawingStateAnimation {

	public static final float DEFAULT_ANIMATION_DURATION = 0.3f;

	private MovementValue mvDetailViewWidth;
	private MovementValue mvOverviewWidth;
	private MovementValue mvDetailViewInnerRadius;
	private MovementValue mvDetailViewStartAngle;
	private MovementValue mvDetailViewAngle;
	private int iDisplayedDetailViewDepth;
	private int iDisplayedOverviewDepth;
	private int iAnimationPart;

	/**
	 * Constructor.
	 * 
	 * @param drawingController
	 *            DrawingController that holds the drawing states.
	 * @param radialHierarchy
	 *            GLRadialHierarchy instance that is used.
	 * @param navigationHistory
	 *            NavigationHistory instance that shall be used.
	 */
	public AnimationPopOutDetailOutside(DrawingController drawingController,
		GLRadialHierarchy radialHierarchy, NavigationHistory navigationHistory) {
		super(drawingController, radialHierarchy, navigationHistory);
		fAnimationDuration = DEFAULT_ANIMATION_DURATION;
	}

	@Override
	public void draw(float fXCenter, float fYCenter, GL gl, GLU glu, double dTimePassed) {

		PartialDisc pdCurrentSelectedElement = radialHierarchy.getCurrentSelectedElement();
		PartialDisc pdCurrentRootElement = radialHierarchy.getCurrentRootElement();

		if (!bAnimationStarted) {
			initAnimationFirstPart(fXCenter, fYCenter, pdCurrentSelectedElement, pdCurrentRootElement);
			iAnimationPart = 1;
			bAnimationStarted = true;
			radialHierarchy.setAnimationActive(true);
		}

		moveValues(dTimePassed);

		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glTranslatef(fXCenter, fYCenter, 0);

		pdCurrentRootElement.setPDDrawingStrategyChildren(DrawingStrategyManager.get()
			.getDefaultDrawingStrategy(), iDisplayedOverviewDepth);
		pdCurrentSelectedElement.setPDDrawingStrategyChildren(DrawingStrategyManager.get()
			.getDrawingStrategy(EPDDrawingStrategyType.INVISIBLE), iDisplayedDetailViewDepth);

		pdCurrentRootElement.drawHierarchyFull(gl, glu, mvOverviewWidth.getMovementValue(),
			iDisplayedOverviewDepth);

		pdCurrentSelectedElement.setPDDrawingStrategyChildren(DrawingStrategyManager.get()
			.getDefaultDrawingStrategy(), iDisplayedDetailViewDepth);

		if (iAnimationPart == 1) {
			pdCurrentSelectedElement.drawHierarchyAngular(gl, glu, mvDetailViewWidth.getMovementValue(),
				iDisplayedDetailViewDepth, pdCurrentSelectedElement.getCurrentStartAngle(),
				pdCurrentSelectedElement.getCurrentAngle(), mvDetailViewInnerRadius.getMovementValue());
		}
		else {
			pdCurrentSelectedElement.drawHierarchyAngular(gl, glu, mvDetailViewWidth.getMovementValue(),
				iDisplayedDetailViewDepth, mvDetailViewStartAngle.getMovementValue(), mvDetailViewAngle
					.getMovementValue(), mvDetailViewInnerRadius.getMovementValue());
		}

		if (haveMovementValuesReachedTargets()) {
			iAnimationPart++;
			if (iAnimationPart == 2) {
				initAnimationSecondPart(fXCenter, fYCenter, pdCurrentSelectedElement, pdCurrentRootElement);
			}
			if (iAnimationPart > 2) {
				bAnimationStarted = false;
			}
		}

		if (!bAnimationStarted) {
			ADrawingState dsNext =
				drawingController.getDrawingState(EDrawingStateType.DRAWING_STATE_DETAIL_OUTSIDE);

			drawingController.setDrawingState(dsNext);
			radialHierarchy.setAnimationActive(false);

			navigationHistory.addNewHistoryEntry(dsNext, pdCurrentRootElement, pdCurrentSelectedElement,
				radialHierarchy.getMaxDisplayedHierarchyDepth());
			radialHierarchy.setNewSelection(ESelectionType.SELECTION, pdCurrentSelectedElement,
				pdCurrentRootElement);
			radialHierarchy.setDisplayListDirty();
		}
		gl.glPopMatrix();
	}

	/**
	 * Initializes the first part of the animation which pops out the selected element. Particularly all
	 * movement values needed for this part are initialized.
	 * 
	 * @param fXCenter
	 *            X coordinate of the hierarchy's center.
	 * @param fYCenter
	 *            Y coordinate of the hierarchy's center.
	 * @param pdCurrentSelectedElement
	 *            Currently selected partial disc.
	 * @param pdCurrentRootElement
	 *            Current root partial disc.
	 */
	private void initAnimationFirstPart(float fXCenter, float fYCenter, PartialDisc pdCurrentSelectedElement,
		PartialDisc pdCurrentRootElement) {

		float fCurrentRootWidth = pdCurrentRootElement.getCurrentWidth();

		int iMaxDisplayedHierarchyDepth = radialHierarchy.getMaxDisplayedHierarchyDepth();
		int iCurrentSelectedElementHierarchyDepth = pdCurrentSelectedElement.getHierarchyDepth();
		float fCurrentSelectedElementWidth = pdCurrentSelectedElement.getCurrentWidth();
		float fCurrentSelecedElementInnderRadius = pdCurrentSelectedElement.getCurrentInnerRadius();
		int iDepthToRoot = pdCurrentSelectedElement.getParentPathLength(pdCurrentRootElement);

		float fDetailViewScreenPercentage;

		iDisplayedDetailViewDepth =
			Math.min(iMaxDisplayedHierarchyDepth - iDepthToRoot, iCurrentSelectedElementHierarchyDepth);

		if (iMaxDisplayedHierarchyDepth <= RadialHierarchyRenderStyle.MIN_DISPLAYED_DETAIL_DEPTH + 1) {
			fDetailViewScreenPercentage = RadialHierarchyRenderStyle.MIN_DETAIL_SCREEN_PERCENTAGE;
		}
		else {
			float fPercentageStep =
				(RadialHierarchyRenderStyle.MAX_DETAIL_SCREEN_PERCENTAGE - RadialHierarchyRenderStyle.MIN_DETAIL_SCREEN_PERCENTAGE)
					/ ((float) (iMaxDisplayedHierarchyDepth
						- RadialHierarchyRenderStyle.MIN_DISPLAYED_DETAIL_DEPTH - 1));

			fDetailViewScreenPercentage =
				RadialHierarchyRenderStyle.MIN_DETAIL_SCREEN_PERCENTAGE
					+ (iDisplayedDetailViewDepth - RadialHierarchyRenderStyle.MIN_DISPLAYED_DETAIL_DEPTH)
					* fPercentageStep;
		}

		float fDetailViewTargetWidth =
			Math.min(fXCenter * fDetailViewScreenPercentage, fYCenter * fDetailViewScreenPercentage)
				/ iDisplayedDetailViewDepth;

		float fOverviewScreenPercentage =
			1.0f - (fDetailViewScreenPercentage + (1.0f - RadialHierarchyRenderStyle.USED_SCREEN_PERCENTAGE) + RadialHierarchyRenderStyle.DETAIL_RADIUS_DELTA_SCREEN_PERCENTAGE);
		iDisplayedOverviewDepth =
			Math.min(iMaxDisplayedHierarchyDepth, pdCurrentRootElement.getHierarchyDepth());

		float fTotalOverviewWidth =
			Math.min(fXCenter * fOverviewScreenPercentage, fYCenter * fOverviewScreenPercentage);
		float fOverviewTargetWidth = fTotalOverviewWidth / iDisplayedOverviewDepth;

		float fDetailViewTargetInnerRadius =
			fTotalOverviewWidth
				+ Math.min(fXCenter * RadialHierarchyRenderStyle.DETAIL_RADIUS_DELTA_SCREEN_PERCENTAGE,
					fYCenter * RadialHierarchyRenderStyle.DETAIL_RADIUS_DELTA_SCREEN_PERCENTAGE);

		alMovementValues.clear();

		mvOverviewWidth = createNewMovementValue(fCurrentRootWidth, fOverviewTargetWidth, fAnimationDuration);
		mvDetailViewWidth =
			createNewMovementValue(fCurrentSelectedElementWidth, fDetailViewTargetWidth, fAnimationDuration);
		mvDetailViewInnerRadius =
			createNewMovementValue(fCurrentSelecedElementInnderRadius, fDetailViewTargetInnerRadius,
				fAnimationDuration);
	}

	/**
	 * Initializes the second part of the animation which surrounds the overview with the detail view.
	 * Particularly all movement values needed for this part are initialized.
	 * 
	 * @param fXCenter
	 *            X coordinate of the hierarchy's center.
	 * @param fYCenter
	 *            Y coordinate of the hierarchy's center.
	 * @param pdCurrentSelectedElement
	 *            Currently selected partial disc.
	 * @param pdCurrentRootElement
	 *            Current root partial disc.
	 */
	private void initAnimationSecondPart(float fXCenter, float fYCenter,
		PartialDisc pdCurrentSelectedElement, PartialDisc pdCurrentRootElement) {

		float fCurrentSelectedElementStartAngle = pdCurrentSelectedElement.getCurrentStartAngle();
		float fCurrentSelectedElementAngle = pdCurrentSelectedElement.getCurrentAngle();

		float fMidAngle = fCurrentSelectedElementStartAngle + (fCurrentSelectedElementAngle / 2.0f);

		while (fMidAngle > 360) {
			fMidAngle -= 360;
		}
		while (fMidAngle < 0) {
			fMidAngle += 360;
		}
		float fAngleToAdd = (fMidAngle > fCurrentSelectedElementStartAngle) ? -180 : 180;
		float fCurrentSelectedElementTargetStartAngle = fMidAngle + fAngleToAdd;

		mvDetailViewAngle = createNewMovementValue(fCurrentSelectedElementAngle, 360, fAnimationDuration);
		mvDetailViewStartAngle =
			createNewMovementValue(fCurrentSelectedElementStartAngle,
				fCurrentSelectedElementTargetStartAngle, fAnimationDuration);
	}

	@Override
	public EDrawingStateType getType() {
		return EDrawingStateType.ANIMATION_POP_OUT_DETAIL_OUTSIDE;
	}
}
