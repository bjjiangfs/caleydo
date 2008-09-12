package org.caleydo.core.util.slerp;

import org.caleydo.core.view.opengl.util.hierarchy.RemoteHierarchyLayer;

/**
 * Slerp action in 3D scene.
 * 
 * @author Marc Streit
 */
public class SlerpAction
{

	private int iElementId = -1;

	private RemoteHierarchyLayer originHierarchyLayer;

	private RemoteHierarchyLayer destinationHierarchyLayer;

	private int iOriginPosIndex = -1;

	private int iDestinationPosIndex = -1;

	/**
	 * Constructor.
	 * 
	 * @param iElementId
	 * @param originHierarchyLayer
	 * @param bSlerpUpInHierarchy
	 */
	public SlerpAction(int iElementId, RemoteHierarchyLayer originHierarchyLayer,
			boolean bSlerpUpInHierarchy)
	{

		if (bSlerpUpInHierarchy)
			destinationHierarchyLayer = originHierarchyLayer.getParentLayer();
		else
			destinationHierarchyLayer = originHierarchyLayer.getChildLayer();

		init(iElementId, originHierarchyLayer, destinationHierarchyLayer);
	}

	/**
	 * Constructor.
	 * 
	 * @param iElementId
	 * @param originHierarchyLayer
	 * @param destinationHierarchyLayer
	 */
	public SlerpAction(int iElementId, RemoteHierarchyLayer originHierarchyLayer,
			RemoteHierarchyLayer destinationHierarchyLayer)
	{

		init(iElementId, originHierarchyLayer, destinationHierarchyLayer);
	}

	/**
	 * Constructor.
	 * 
	 * @param iElementId
	 * @param originHierarchyLayer
	 * @param destinationHierarchyLayer
	 * @param iDestinationPosIndex
	 */
	public SlerpAction(int iElementId, RemoteHierarchyLayer originHierarchyLayer,
			RemoteHierarchyLayer destinationHierarchyLayer, int iDestinationPosIndex)
	{

		this.iDestinationPosIndex = iDestinationPosIndex;
		init(iElementId, originHierarchyLayer, destinationHierarchyLayer);
	}

	private void init(int iElementId, RemoteHierarchyLayer originHierarchyLayer,
			RemoteHierarchyLayer destinationHierarchyLayer)
	{

		this.iElementId = iElementId;
		this.originHierarchyLayer = originHierarchyLayer;
		this.destinationHierarchyLayer = destinationHierarchyLayer;
	}

	public void start()
	{

		iOriginPosIndex = originHierarchyLayer.getPositionIndexByElementId(iElementId);

		if (iDestinationPosIndex == -1)
		{
			this.iDestinationPosIndex = destinationHierarchyLayer.getNextPositionIndex();
		}

		originHierarchyLayer.removeElement(iElementId);

		if (destinationHierarchyLayer.getElementList().size() < destinationHierarchyLayer
				.getCapacity())
		{
			destinationHierarchyLayer.addElement(iElementId);
		}
		else
		{
			destinationHierarchyLayer.replaceElement(iElementId, iDestinationPosIndex);
		}
	}

	public int getElementId()
	{

		return iElementId;
	}

	public RemoteHierarchyLayer getOriginHierarchyLayer()
	{

		if (originHierarchyLayer == null)
			throw new IllegalStateException("Slerp origin layer is null!");

		return originHierarchyLayer;
	}

	public RemoteHierarchyLayer getDestinationHierarchyLayer()
	{

		if (destinationHierarchyLayer == null)
			throw new IllegalStateException("Slerp destination layer is null!");

		return destinationHierarchyLayer;
	}

	public int getOriginPosIndex()
	{

		if (iOriginPosIndex == -1)
			throw new IllegalStateException("Invalid slerp origin position (-1)!");

		return iOriginPosIndex;
	}

	public int getDestinationPosIndex()
	{

		if (iDestinationPosIndex == -1)
			throw new IllegalStateException("Invalid slerp destination position (-1)!");

		return iDestinationPosIndex;
	}
}
