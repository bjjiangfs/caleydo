package org.caleydo.core.data.collection.table;

import org.caleydo.core.data.collection.storage.AStorage;
import org.caleydo.core.data.collection.storage.EDataRepresentation;
import org.caleydo.core.data.collection.storage.NumericalStorage;
import org.caleydo.core.data.virtualarray.StorageVirtualArray;

/**
 * This class encapsulates all uncertainty-related functionality of the data table
 * 
 * @author Alexander Lex
 * @author Marc Streit
 */
public class Uncertainty {

	private DataTable table;
	private MetaData metaData;

	/**
	 * the uncertainties for the whole storage aggregated across the storageVA based on the normalized
	 * uncertainty values
	 */
	private float[] aggregatedNormalizedUncertainties;
	/**
	 * the uncertainties for the whole storage aggregated across the storageVA based on the raw uncertainty
	 * values
	 */
	private float[] aggregatedRawUncertainties;

	public Uncertainty(DataTable table) {
		this.table = table;
		this.metaData = table.getMetaData();
	}

	public float getNormalizedUncertainty(int contentIndex) {

		if (aggregatedRawUncertainties == null) {
			// calculateRawAverageUncertainty();
			// calculateNormalizedAverageUncertainty(2, 3);
			throw new IllegalStateException("Certainty has not been calculated yet.");

		}

		return aggregatedNormalizedUncertainties[contentIndex];
	}

	public float[] getNormalizedUncertainty() {

		// if (aggregatedRawUncertainties == null) {
		// calculateRawAverageUncertainty();
		// // throw new IllegalStateException("Certainty has not been calculated yet.");
		// }

		return aggregatedNormalizedUncertainties;
	}

	public float[] getRawUncertainty() {

		if (aggregatedRawUncertainties == null)
			throw new IllegalStateException("Certainty has not been calculated yet.");

		return aggregatedRawUncertainties;
	}

	public void calculateNormalizedAverageUncertainty(float invalidThreshold, float validThreshold) {

		for (AStorage storage : table.hashStorages.values()) {

			if (storage instanceof NumericalStorage)
				((NumericalStorage) storage).normalizeUncertainty(invalidThreshold, validThreshold);
		}

		aggregatedNormalizedUncertainties = new float[metaData.depth()];
		for (int contentIndex = 0; contentIndex < metaData.depth(); contentIndex++) {
			// float aggregatedUncertainty = calculateMaxUncertainty(contentIndex);
			float aggregatedUncertainty =
				calcualteAverageUncertainty(contentIndex, EDataRepresentation.UNCERTAINTY_NORMALIZED);
			aggregatedNormalizedUncertainties[contentIndex] = aggregatedUncertainty;
		}
	}

	public void calculateRawAverageUncertainty() {
		aggregatedRawUncertainties = new float[metaData.depth()];
		for (int contentIndex = 0; contentIndex < metaData.depth(); contentIndex++) {
			float aggregatedUncertainty;

			aggregatedUncertainty =
				calcualteAverageUncertainty(contentIndex, EDataRepresentation.UNCERTAINTY_RAW);

			// aggregatedUncertainty =
			// calculateMaxUncertainty(contentIndex, EDataRepresentation.UNCERTAINTY_RAW);

			aggregatedRawUncertainties[contentIndex] = aggregatedUncertainty;
		}
	}

	private float calcualteAverageUncertainty(int contentIndex, EDataRepresentation dataRepresentation) {
		float uncertaintySum = 0;
		StorageVirtualArray storageVA = table.hashStorageData.get(DataTable.STORAGE).getStorageVA();
		for (Integer storageID : storageVA) {
			try {
				uncertaintySum +=
					table.hashStorages.get(storageID).getFloat(dataRepresentation, contentIndex);
			}
			catch (Exception e) {
				System.out.println("storageID: " + storageID);
			}
		}
		return uncertaintySum / storageVA.size();
	}

	@SuppressWarnings("unused")
	private float calculateMaxUncertainty(int contentIndex, EDataRepresentation dataRepresentation) {
		float maxUncertainty = Float.MAX_VALUE;
		for (Integer storageID : table.hashStorageData.get(DataTable.STORAGE).getStorageVA()) {
			float cellUncertainty = 0;
			try {
				cellUncertainty =
					table.hashStorages.get(storageID).getFloat(dataRepresentation, contentIndex);
			}
			catch (Exception e) {
				System.out.println("storageID: " + storageID);

			}
			if (cellUncertainty < maxUncertainty) {
				maxUncertainty = cellUncertainty;
			}
		}
		return maxUncertainty;
	}

}