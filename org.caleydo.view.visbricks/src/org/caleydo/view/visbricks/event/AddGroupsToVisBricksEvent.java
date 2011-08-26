package org.caleydo.view.visbricks.event;

import java.util.ArrayList;

import org.caleydo.core.data.container.ADimensionGroupData;
import org.caleydo.core.data.container.TableBasedDimensionGroupData;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.graph.tree.ClusterNode;
import org.caleydo.core.data.perspective.DimensionPerspective;
import org.caleydo.core.manager.event.AEvent;

public class AddGroupsToVisBricksEvent extends AEvent {

	private String dimensionPerspectiveID;
	private String recordPerspectiveID;
	private ArrayList<ClusterNode> selectedNodes;

	ArrayList<ADimensionGroupData> dimensionGroupData = null;

	public AddGroupsToVisBricksEvent() {
	}

	public AddGroupsToVisBricksEvent(String dataDomainID, String dimensionPerspectiveID,
			String recordPerspectiveID, ArrayList<ClusterNode> selectedNodes) {
		this.dataDomainID = dataDomainID;
		this.dimensionPerspectiveID = dataDomainID;
		this.recordPerspectiveID = recordPerspectiveID;
		this.selectedNodes = selectedNodes;
	}

	public boolean checkIntegrity() {
		if (dimensionGroupData == null
				&& (dataDomainID == null || dimensionPerspectiveID == null
						|| recordPerspectiveID == null || selectedNodes == null))
			return false;

		return true;
	}

	public void setDimensionGroupData(ArrayList<ADimensionGroupData> dimensionGroupData) {
		this.dimensionGroupData = dimensionGroupData;
	}

	public ArrayList<ADimensionGroupData> getDimensionGroupData() {
		if (dimensionGroupData != null)
			return dimensionGroupData;

		dimensionGroupData = new ArrayList<ADimensionGroupData>(selectedNodes.size());
		for (ClusterNode node : selectedNodes) {
			TableBasedDimensionGroupData data = new TableBasedDimensionGroupData(
					(ATableBasedDataDomain) DataDomainManager.get().getDataDomainByID(
							dataDomainID), dimensionPerspectiveID, recordPerspectiveID,
					node, DimensionPerspective.class);
			dimensionGroupData.add(data);
		}
		return dimensionGroupData;
	}

}
