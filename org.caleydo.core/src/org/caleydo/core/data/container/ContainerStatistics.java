/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 * 
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
/**
 * 
 */
package org.caleydo.core.data.container;

import java.util.ArrayList;

import org.caleydo.core.data.collection.Histogram;
import org.caleydo.core.data.collection.dimension.DataRepresentation;
import org.caleydo.core.data.collection.table.DataTable;
import org.caleydo.core.data.virtualarray.DimensionVirtualArray;
import org.caleydo.core.data.virtualarray.RecordVirtualArray;
import org.caleydo.core.data.virtualarray.VirtualArray;

/**
 * <p>
 * {@link ContainerStatistics} provides access and calculates derivable
 * meta-data for the data specified by a {@link DataContainer}, such as
 * averages, histograms, etc.
 * </p>
 * <p>
 * Everything is calculated lazily.
 * </p>
 * <p>
 * TODO: There is currently no way to mark this dirty once the perspectives in
 * the container or the container itself changed.
 * </p>
 * 
 * @author Alexander Lex
 */
public class ContainerStatistics {
	private DataContainer container;

	/** The average of all cells in the container */
	private float averageValue = Float.NEGATIVE_INFINITY;

	/** The histogram for the data in this container along the dimensions */
	private Histogram histogram = null;

	/** The fold-change properties of this container with another container */
	private FoldChange foldChange;

	private TTest tTest;

	private AdjustedRandIndex adjustedRandIndex;

	/**
	 * Optionally it is possible to specify the number of bins for the histogram
	 * manually. This should only be done if there really is a reason for it.
	 */
	private int numberOfBucketsForHistogram = Integer.MIN_VALUE;

	/**
	 * A list of averages across dimensions, one for every record in the data
	 * container. Sorted as the virtual array.
	 */
	private ArrayList<Average> averageRecords;

	/** Same as {@link #averageRecords} for dimensions */
	private ArrayList<Average> averageDimensions;

	public ContainerStatistics(DataContainer container) {
		this.container = container;
	}

	/**
	 * @return the averageValue, see {@link #averageValue}
	 */
	public double getAverageValue() {
		if (Float.isInfinite(averageValue))
			calculateAverageValue();
		return averageValue;
	}

	private void calculateAverageValue() {
		averageValue = 0;
		int count = 0;
		for (Integer recordID : container.getRecordPerspective().getVirtualArray()) {

			DimensionVirtualArray dimensionVA = container.getDimensionPerspective()
					.getVirtualArray();

			if (dimensionVA == null) {
				averageValue = 0;
				return;
			}
			for (Integer dimensionID : dimensionVA) {
				float value = container.getDataDomain().getTable()
						.getFloat(DataRepresentation.NORMALIZED, recordID, dimensionID);
				if (!Float.isNaN(value)) {
					averageValue += value;
					count++;
				}
			}
		}
		averageValue /= count;
	}

	/**
	 * This is optional! Read more: {@link #numberOfBucketsForHistogram}
	 * 
	 * @param numberOfBucketsForHistogram
	 *            setter, see {@link #numberOfBucketsForHistogram}
	 */
	public void setNumberOfBucketsForHistogram(int numberOfBucketsForHistogram) {
		this.numberOfBucketsForHistogram = numberOfBucketsForHistogram;
	}

	public Histogram getHistogram() {
		if (histogram == null)
			histogram = calculateHistogram(container.getDataDomain().getTable(),
					container.getRecordPerspective().getVirtualArray(), container
							.getDimensionPerspective().getVirtualArray(),
					numberOfBucketsForHistogram);
		return histogram;
	}

	public static Histogram calculateHistogram(DataTable dataTable,
			RecordVirtualArray recordVA, DimensionVirtualArray dimensionVA,
			int numberOfBucketsForHistogram) {
		if (!dataTable.isDataHomogeneous()) {
			throw new UnsupportedOperationException(
					"Tried to calcualte a set-wide histogram on a not homogeneous table. This makes no sense. Use dimension based histograms instead!");
		}

		int numberOfBuckets;

		if (numberOfBucketsForHistogram != Integer.MIN_VALUE)
			numberOfBuckets = numberOfBucketsForHistogram;
		else
			numberOfBuckets = (int) Math.sqrt(recordVA.size());
		Histogram histogram = new Histogram(numberOfBuckets);
		for (int iCount = 0; iCount < numberOfBuckets; iCount++) {
			histogram.add(0);
		}

		// FloatCContainerIterator iterator =
		// ((FloatCContainer)
		// hashCContainers.get(DataRepresentation.NORMALIZED)).iterator(recordVA);
		for (Integer dimensionID : dimensionVA) {
			{
				for (Integer recordID : recordVA) {
					float value = dataTable.getFloat(DataRepresentation.NORMALIZED,
							recordID, dimensionID);

					// this works because the values in the container are
					// already noramlized
					int iIndex = (int) (value * numberOfBuckets);
					if (iIndex == numberOfBuckets)
						iIndex--;
					Integer iNumOccurences = histogram.get(iIndex);
					histogram.set(iIndex, ++iNumOccurences);
				}
			}
		}

		return histogram;
	}

	/**
	 * Access anything related to fold-changes between this container and others
	 */
	public FoldChange foldChange() {
		if (foldChange == null)
			foldChange = new FoldChange();
		return foldChange;
	}

	/**
	 * Access anything related to Adjusted Rand Index scores.
	 * 
	 * @return
	 */
	public AdjustedRandIndex adjustedRandIndex() {
		if (adjustedRandIndex == null)
			adjustedRandIndex = new AdjustedRandIndex(container);
		return adjustedRandIndex;
	}

	/**
	 * Access anything related to t-tests
	 * 
	 * @return
	 */
	public TTest tTest() {
		if (tTest == null)
			tTest = new TTest();
		return tTest;
	}

	/**
	 * @return the averageRecords, see {@link #averageRecords}
	 */
	public ArrayList<Average> getAverageRecords() {
		if (averageRecords == null)
			calculateAverageRecords();
		return averageRecords;
	}

	/**
	 * Calculates the arithmetic mean and the standard deviation from the
	 * arithmetic mean of the records
	 */
	private void calculateAverageRecords() {
		averageRecords = new ArrayList<Average>();

		DimensionVirtualArray dimensionVA = container.getDimensionPerspective()
				.getVirtualArray();
		RecordVirtualArray recordVA = container.getRecordPerspective().getVirtualArray();

		for (Integer recordID : recordVA) {

			Average averageRecord = calculateAverage(dimensionVA, container
					.getDataDomain().getTable(), recordID);

			averageRecords.add(averageRecord);
		}
	}

	/**
	 * @return the averageRecords, see {@link #averageRecords}
	 */
	public ArrayList<Average> getAverageDimensions() {
		if (averageDimensions == null)
			calculateAverageDimensions();
		return averageDimensions;
	}

	/**
	 * Calculates the arithmetic mean and the standard deviation from the
	 * arithmetic mean of the dimensions
	 */
	private void calculateAverageDimensions() {
		averageDimensions = new ArrayList<Average>();

		DimensionVirtualArray dimensionVA = container.getDimensionPerspective()
				.getVirtualArray();
		RecordVirtualArray recordVA = container.getRecordPerspective().getVirtualArray();

		for (Integer dimensionID : dimensionVA) {
			Average averageDimension = calculateAverage(recordVA, container
					.getDataDomain().getTable(), dimensionID);
			averageDimensions.add(averageDimension);
		}
	}

	/**
	 * <p>
	 * Calculates the average of a dimension or row in the data table. Whether
	 * the average is calculated for the dimension or row is determined by the
	 * type of the {@link VirtualArray}.
	 * </p>
	 * <p>
	 * The objectID has to be of the "opposing" type, i.e. if the virtualArray
	 * is of type {@link RecordVirtualArray}, the id has to be a dimension id.
	 * </p>
	 * 
	 * @param virtualArray
	 * @param table
	 * @param objectID
	 * @return
	 */
	public static Average calculateAverage(VirtualArray<?, ?, ?> virtualArray,
			DataTable table, Integer objectID) {
		Average averageDimension = new Average();
		double sumOfValues = 0;
		double sumDeviation = 0;

		int nrValidValues = 0;
		for (Integer virtualArrayID : virtualArray) {
			Float value;
			if (virtualArray instanceof RecordVirtualArray) {
				value = table.getFloat(DataRepresentation.NORMALIZED, virtualArrayID,
						objectID);
			} else {
				value = table.getFloat(DataRepresentation.NORMALIZED, objectID,
						virtualArrayID);
			}
			if (!value.isNaN()) {
				sumOfValues += value;
				nrValidValues++;
			}
		}
		averageDimension.arithmeticMean = sumOfValues / nrValidValues;

		for (Integer recordID : virtualArray) {
			Float value;
			if (virtualArray instanceof RecordVirtualArray) {
				value = table.getFloat(DataRepresentation.NORMALIZED, recordID, objectID);
			} else {
				value = table.getFloat(DataRepresentation.NORMALIZED, objectID, recordID);
			}
			if (!value.isNaN()) {
				sumDeviation += Math.pow(value - averageDimension.arithmeticMean, 2);
			}
		}
		averageDimension.standardDeviation = Math.sqrt(sumDeviation / nrValidValues);
		return averageDimension;
	}

}
