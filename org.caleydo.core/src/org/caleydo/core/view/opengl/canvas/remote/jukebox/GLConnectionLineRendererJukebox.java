package org.caleydo.core.view.opengl.canvas.remote.jukebox;

import gleem.linalg.Mat4f;
import gleem.linalg.Rotf;
import gleem.linalg.Vec3f;
import java.util.ArrayList;
import java.util.Iterator;
import javax.media.opengl.GL;
import org.caleydo.core.data.selection.SelectedElementRep;
import org.caleydo.core.view.opengl.canvas.remote.AGLConnectionLineRenderer;
import org.caleydo.core.view.opengl.util.hierarchy.RemoteHierarchyLevel;

/**
 * Specialized connection line renderer for bucket view.
 * 
 * @author Marc Streit
 */
public class GLConnectionLineRendererJukebox
	extends AGLConnectionLineRenderer
{

	/**
	 * Constructor.
	 * 
	 * @param underInteractionLayer
	 * @param stackLayer
	 * @param poolLayer
	 */
	public GLConnectionLineRendererJukebox(final RemoteHierarchyLevel underInteractionLayer,
			final RemoteHierarchyLevel stackLayer, final RemoteHierarchyLevel poolLayer)
	{
		super(underInteractionLayer, stackLayer, poolLayer);
	}

	@Override
	protected void renderConnectionLines(final GL gl)
	{

		Vec3f vecTranslation;
		Vec3f vecScale;

		Rotf rotation;
		Mat4f matSrc = new Mat4f();
		Mat4f matDest = new Mat4f();
		matSrc.makeIdent();
		matDest.makeIdent();

		Iterator<Integer> iterSelectedElementID = connectedElementRepManager
				.getAllSelectedElements().iterator();

		ArrayList<ArrayList<Vec3f>> alPointLists = null;// 

		while (iterSelectedElementID.hasNext())
		{
			int iSelectedElementID = iterSelectedElementID.next();

			ArrayList<SelectedElementRep> alSelectedElementRep = connectedElementRepManager
					.getSelectedElementRepsByElementID(iSelectedElementID);

			for (int iStackPositionIndex = 0; iStackPositionIndex < stackLayer.getCapacity(); iStackPositionIndex++)
			{
				for (int iElementIndex = 0; iElementIndex < alSelectedElementRep.size(); iElementIndex++)
				{
					SelectedElementRep selectedElementRep = alSelectedElementRep
							.get(iElementIndex);

					// Check if element is in stack
					RemoteHierarchyLevel activeLayer = null;
					if (stackLayer.containsElement(selectedElementRep.getContainingViewID()))
					{
						activeLayer = stackLayer;
					}

					// Check if the element is in the currently iterated view in
					// the stack
					if (stackLayer.getPositionIndexByElementId(selectedElementRep
							.getContainingViewID()) != iStackPositionIndex
							&& stackLayer.getPositionIndexByElementId(selectedElementRep
									.getContainingViewID()) != iStackPositionIndex + 1)
					{
						continue;
					}

					if (activeLayer != null)
					{
						vecTranslation = activeLayer.getTransformByElementId(
								selectedElementRep.getContainingViewID()).getTranslation();
						vecScale = activeLayer.getTransformByElementId(
								selectedElementRep.getContainingViewID()).getScale();
						rotation = activeLayer.getTransformByElementId(
								selectedElementRep.getContainingViewID()).getRotation();

						ArrayList<Vec3f> alPoints = selectedElementRep.getPoints();
						ArrayList<Vec3f> alPointsTransformed = new ArrayList<Vec3f>();

						for (Vec3f vecCurrentPoint : alPoints)
						{
							alPointsTransformed.add(transform(vecCurrentPoint, vecTranslation,
									vecScale, rotation));
						}
						int iKey = selectedElementRep.getContainingViewID();

						alPointLists = hashViewToPointLists.get(iKey);
						if (alPointLists == null)
						{
							alPointLists = new ArrayList<ArrayList<Vec3f>>();
							hashViewToPointLists.put(iKey, alPointLists);
						}
						alPointLists.add(alPointsTransformed);

					}
				}

				if (hashViewToPointLists.size() > 1)
				{
					renderLineBundling(gl);
				}

				hashViewToPointLists.clear();
			}
		}
	}
}
