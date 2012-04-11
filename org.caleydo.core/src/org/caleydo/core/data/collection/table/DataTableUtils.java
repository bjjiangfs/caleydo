package org.caleydo.core.data.collection.table;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.caleydo.core.command.CommandType;
import org.caleydo.core.command.data.parser.CmdParseIDMapping;
import org.caleydo.core.data.collection.ExternalDataRepresentation;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.id.IDType;
import org.caleydo.core.data.importing.DataSetDescription;
import org.caleydo.core.data.virtualarray.group.DimensionGroupList;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.RecordGroupList;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.parser.ascii.TabularDataParser;

/**
 * Utility class that features creating, loading and saving sets and dimensions.
 * 
 * @author Werner Puff
 * @author Alexander Lex
 */
public class DataTableUtils {

	/** prefix for temporary set-file */
	public static final String DATA_FILE_PREFIX = "setfile";

	/** prefix for temporary gene-tree--file */
	public static final String RECORD_TREE_FILE_PREFIX = "recordtree";

	/** prefix for temporary experiment-tree-file */
	public static final String DIMENSION_TREE_FILE_PREFIX = "dimensiontree";

	/**
	 * Loads the set-file as specified in the {@link IDataDomain}'s
	 * {@link LoadDataParameters} and stores the raw-data in the useCase
	 * 
	 * @param useCase
	 */
	public static byte[] loadSetFile(DataSetDescription dataSetDescription) {
		String dataPath = dataSetDescription.getDataSourcePath();
		if (dataPath == null) {
			throw new RuntimeException("No set-file name specified in use case");
		}

		File file = new File(dataPath);
		byte[] buffer;
		try {
			FileInputStream is = new FileInputStream(file);
			if (file.length() > Integer.MAX_VALUE) {
				throw new RuntimeException(
						"set-file is larger than maximum internal file-dimension-size");
			}
			buffer = new byte[(int) file.length()];
			is.read(buffer, 0, buffer.length);
		} catch (IOException ex) {
			throw new RuntimeException("Could not read from specified set-file '"
					+ dataPath + "'", ex);
		}
		return buffer;
	}

	/**
	 * Saves the set-data contained in the useCase in a new created temp-file.
	 * The {@link LoadDataParameters} of the useCase are set according to the
	 * created set-file
	 * 
	 * @param parameters
	 *            set-load parameters to store the filename;
	 * @param data
	 *            set-data to save
	 */
	public static void saveTableFile(DataSetDescription parameters, byte[] data) {
		File homeDir = new File(GeneralManager.CALEYDO_HOME_PATH);
		File setFile;
		try {
			setFile = File.createTempFile(DATA_FILE_PREFIX, "csv", homeDir);
			parameters.setDataSourcePath(setFile.getCanonicalPath());
		} catch (IOException ex) {
			throw new RuntimeException(
					"Could not create temporary file to store the set file", ex);
		}
		saveFile(data, setFile);
	}

	/**
	 * Saves the given data in the given file.
	 * 
	 * @param data
	 *            data to save.
	 * @param target
	 *            file to store the data.
	 */
	public static void saveFile(byte[] data, File setFile) {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(setFile);
			os.write(data);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(
					"Could not create temporary file to store the set file", ex);
		} catch (IOException ex) {
			throw new RuntimeException("Could not write to temportary set file", ex);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException ex) {
					// nothing to do here, assuming output stream is already
					// closed
				}
			}
		}
	}

	/**
	 * Creates the {@link DataTable} from a previously prepared dimension
	 * definition.
	 * 
	 * @param dataDomain
	 * @param createDefaultDimensionPerspectives
	 * @param createDefaultRecordPerspective
	 * @return
	 */
	public static void loadData(ATableBasedDataDomain dataDomain,
			DataSetDescription dataSetDescription,
			boolean createDefaultDimensionPerspectives,
			boolean createDefaultRecordPerspective) {

		// --------- load dynamic mapping ---------------
		CmdParseIDMapping cmdParseIDMapping = (CmdParseIDMapping) GeneralManager.get()
				.getCommandManager().createCommandByType(CommandType.PARSE_ID_MAPPING);

		IDType rowTargetIDType;
		if (dataDomain.isColumnDimension())
			rowTargetIDType = dataDomain.getRecordIDType();
		else
			rowTargetIDType = dataDomain.getDimensionIDType();

		String mappingPattern = dataSetDescription.getRowIDSpecification().getIdType()
				+ "_2_" + rowTargetIDType + " REVERSE";

		cmdParseIDMapping.setAttributes(dataSetDescription.getDataSourcePath(),
				dataSetDescription.getNumberOfHeaderLines(), -1, mappingPattern,
				dataSetDescription.getDelimiter(), "", rowTargetIDType.getIDCategory());
		// if (stringConverter == null)
		// stringConverter = loadDataParameters.getRowIDStringConverter();
		// cmdParseIDMapping.setStringConverter(stringConverter);

		cmdParseIDMapping.doCommand();

		// --------- data loading ---------------

		TabularDataParser parser = new TabularDataParser(dataDomain, dataSetDescription);
		parser.loadData();
		DataTable table = dataDomain.getTable();

		// TODO re-enable this
		if (createDefaultDimensionPerspectives)
			table.createDefaultDimensionPerspective();

		if (createDefaultRecordPerspective)
			table.createDefaultRecordPerspective();
		// TODO re-enable this
		// loadTrees(loadDataParameters, set);

		if (dataSetDescription.getMin() != null) {
			table.getMetaData().setMin(dataSetDescription.getMin());
		}
		if (dataSetDescription.getMax() != null) {
			table.getMetaData().setMax(dataSetDescription.getMax());
		}

		boolean isSetHomogeneous = dataSetDescription.isDataHomogeneous();

		if (dataSetDescription.getMathFilterMode().equalsIgnoreCase("Normal")) {
			table.setExternalDataRepresentation(ExternalDataRepresentation.NORMAL,
					isSetHomogeneous);
		} else if (dataSetDescription.getMathFilterMode().equalsIgnoreCase("Log10")) {
			table.setExternalDataRepresentation(ExternalDataRepresentation.LOG10,
					isSetHomogeneous);
		} else if (dataSetDescription.getMathFilterMode().equalsIgnoreCase("Log2")) {
			table.setExternalDataRepresentation(ExternalDataRepresentation.LOG2,
					isSetHomogeneous);
		} else
			throw new IllegalStateException("Unknown data representation type");
	}

	/**
	 * Switch the representation of the data. When this is called the data in
	 * normalized is replaced with data calculated from the mode specified.
	 * 
	 * @param externalDataRep
	 *            Determines how the data is visualized. For options see
	 *            {@link ExternalDataRepresentation}
	 * @param bIsSetHomogeneous
	 *            Determines whether a set is homogeneous or not. Homogeneous
	 *            means that the sat has a global maximum and minimum, meaning
	 *            that all dimensions in the set contain equal data. If false,
	 *            each dimension is treated separately, has it's own min and max
	 *            etc. Sets that contain nominal data MUST be inhomogeneous.
	 */
	public static void setExternalDataRepresentation(DataTable table,
			ExternalDataRepresentation externalDataRep, boolean isSetHomogeneous) {
		table.setExternalDataRepresentation(externalDataRep, isSetHomogeneous);
	}

	/**
	 * Creates a contentGroupList from the group information read from a stored
	 * file
	 * 
	 * @param set
	 * @param vaType
	 *            specify for which va type this is valid
	 * @param groupInfo
	 *            the array list extracted from the file
	 */
	public static void setContentGroupList(DataTable table, String vaType, int[] groupInfo) {

		int cluster = 0, cnt = 0;

		RecordGroupList contentGroupList = table.getRecordPerspective(vaType)
				.getVirtualArray().getGroupList();
		contentGroupList.clear();

		for (int i = 0; i < groupInfo.length; i++) {
			Group group = null;
			if (cluster != groupInfo[i]) {
				group = new Group(cnt);
				contentGroupList.append(group);
				cluster++;
				cnt = 0;
			}
			cnt++;
			if (i == groupInfo.length - 1) {
				group = new Group(cnt);
				contentGroupList.append(group);
			}
		}
	}

	/**
	 * Creates a dimensionGroupList from the group information read from a
	 * stored file
	 * 
	 * @param set
	 * @param vaType
	 *            specify for which va type this is valid
	 * @param groupInfo
	 *            the array list extracted from the file
	 */
	public static void setDimensionGroupList(DataTable table, String vaType,
			int[] groupInfo) {
		int cluster = 0, cnt = 0;

		DimensionGroupList dimensionGroupList = table.getDimensionPerspective(vaType)
				.getVirtualArray().getGroupList();
		dimensionGroupList.clear();

		for (int i = 0; i < groupInfo.length; i++) {
			Group group = null;
			if (cluster != groupInfo[i]) {
				group = new Group(cnt, 0);
				dimensionGroupList.append(group);
				cluster++;
				cnt = 0;
			}
			cnt++;
			if (i == groupInfo.length - 1) {
				group = new Group(cnt, 0);
				dimensionGroupList.append(group);
			}
		}
	}

	/**
	 * Set representative elements for contentGroupLists read from file
	 * 
	 * @param set
	 * @param recordPerspectiveID
	 * @param groupReps
	 */
	public static void setRecordGroupRepresentatives(DataTable table,
			String recordPerspectiveID, int[] groupReps) {

		int group = 0;

		RecordGroupList contentGroupList = table
				.getRecordPerspective(recordPerspectiveID).getVirtualArray()
				.getGroupList();

		contentGroupList.get(group).setRepresentativeElementIndex(0);
		group++;

		for (int i = 1; i < groupReps.length; i++) {
			if (groupReps[i] != groupReps[i - 1]) {
				contentGroupList.get(group).setRepresentativeElementIndex(i);
				group++;
			}
		}
	}

	/**
	 * Set representative elements for dimensionGroupLists read from file
	 * 
	 * @param set
	 * @param dimensionPerspectiveID
	 * @param groupReps
	 */
	public static void setDimensionGroupRepresentatives(DataTable table,
			String dimensionPerspectiveID, int[] groupReps) {

		int group = 0;

		DimensionGroupList dimensionGroupList = table
				.getDimensionPerspective(dimensionPerspectiveID).getVirtualArray()
				.getGroupList();

		dimensionGroupList.get(group).setRepresentativeElementIndex(0);
		group++;

		for (int i = 1; i < groupReps.length; i++) {
			if (groupReps[i] != groupReps[i - 1]) {
				dimensionGroupList.get(group).setRepresentativeElementIndex(i);
				group++;
			}
		}
	}

}
