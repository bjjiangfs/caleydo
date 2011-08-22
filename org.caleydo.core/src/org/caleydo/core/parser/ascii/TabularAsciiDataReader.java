package org.caleydo.core.parser.ascii;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.caleydo.core.data.collection.EDimensionType;
import org.caleydo.core.data.collection.dimension.ADimension;
import org.caleydo.core.data.collection.dimension.NominalDimension;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.util.logging.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Loader for tabular data.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 * @author Alexander Lex
 */
public class TabularAsciiDataReader
	extends AbstractLoader {

	/**
	 * Imports data from file to this table. uses first dimension and overwrites first selection.
	 */
	protected ArrayList<ADimension> targetDimensions;

	protected ArrayList<EDimensionType> columnDataTypes;

	private ArrayList<int[]> intArrays;

	private ArrayList<int[]> groupInfo;

	private ArrayList<float[]> floatArrays;

	private ArrayList<ArrayList<String>> stringLists;

	private boolean useExperimentClusterInfo;

	private ATableBasedDataDomain dataDomain;

	/**
	 * Constructor.
	 */
	public TabularAsciiDataReader(final String sFileName, ATableBasedDataDomain dataDomain) {
		super(sFileName);

		this.dataDomain = dataDomain;
		targetDimensions = new ArrayList<ADimension>();
		columnDataTypes = new ArrayList<EDimensionType>();

		intArrays = new ArrayList<int[]>();
		floatArrays = new ArrayList<float[]>();
		stringLists = new ArrayList<ArrayList<String>>();

		groupInfo = new ArrayList<int[]>();
	}

	/**
	 * Defines a pattern for parsing
	 */
	public final boolean setTokenPattern(final String tokenPattern) {

		boolean areAllTokensProper = true;

		StringTokenizer tokenizer = new StringTokenizer(tokenPattern);

		final String sTokenPatternParserSeperator = GeneralManager.sDelimiter_Parser_DataType;

		while (tokenizer.hasMoreTokens()) {
			String buffer = tokenizer.nextToken(sTokenPatternParserSeperator);

			if (buffer.equalsIgnoreCase("abort")) {
				columnDataTypes.add(EDimensionType.ABORT);

				return areAllTokensProper;
			}
			else if (buffer.equalsIgnoreCase("skip")) {
				columnDataTypes.add(EDimensionType.SKIP);
			}
			else if (buffer.equalsIgnoreCase("int")) {
				columnDataTypes.add(EDimensionType.INT);
			}
			else if (buffer.equalsIgnoreCase("float")) {
				columnDataTypes.add(EDimensionType.FLOAT);
			}
			else if (buffer.equalsIgnoreCase("string")) {
				columnDataTypes.add(EDimensionType.STRING);
			}
			else if (buffer.equalsIgnoreCase("certainty")) {
				columnDataTypes.add(EDimensionType.CERTAINTY);
			}

			else {
				areAllTokensProper = false;

				Logger.log(new Status(IStatus.WARNING, GeneralManager.PLUGIN_ID, "Unknown column data type: "
					+ buffer + " in " + tokenPattern));
			}
		}

		return areAllTokensProper;
	}

	public void setTargetDimensions(final ArrayList<Integer> targetDimensionIDs) {
		for (int dimensionID : targetDimensionIDs) {
			targetDimensions.add(GeneralManager.get().getDimensionManager().getItem(dimensionID));
		}
	}

	protected void allocateDimensionBufferForTokenPattern() {

		int lineCount = 0;

		for (EDimensionType dimensionType : columnDataTypes) {

			switch (dimensionType) {
				case INT:
					intArrays.add(new int[nrLinesToRead]);
					break;
				case FLOAT:
					if (useExperimentClusterInfo)
						floatArrays.add(new float[nrLinesToReadWithClusterInfo]);
					else
						floatArrays.add(new float[nrLinesToRead]);
					lineCount++;
					break;
				case STRING:
					stringLists.add(new ArrayList<String>(nrLinesToRead));
					break;
				case CERTAINTY:
					floatArrays.add(new float[nrLinesToRead]);
					break;
				case SKIP:
					break;
				case ABORT:
					if (useExperimentClusterInfo) {
						groupInfo.add(new int[lineCount]);
						groupInfo.add(new int[lineCount]);
					}
					return;

				default:
					throw new IllegalStateException("Unknown token pattern detected: "
						+ dimensionType.toString());
			}
		}
	}

	@Override
	protected void loadDataParseFile(BufferedReader bufferReader, final int numberOfLines) throws IOException {

		allocateDimensionBufferForTokenPattern();

		// Init progress bar
		swtGuiManager.setProgressBarText("Load data file " + this.getFileName());

		String line;
		String tempToken;
		int columnIndex = 0;
		float fProgressBarFactor = 100f / iStopParsingAtLine;

		int max = iStopParsingAtLine - parsingStartLine + 1;

		while ((line = bufferReader.readLine()) != null && lineInFile <= iStopParsingAtLine) {

			// Check if line should be ignored
			if (lineInFile < this.parsingStartLine || line.isEmpty()) {
				lineInFile++;
				continue;
			}

			// Replace empty cells with NaN
			line = line.replace(tokenSeperator + tokenSeperator, tokenSeperator + "NaN" + tokenSeperator);
			// line = line.replace(tokenSeperator + System.getProperty("line.separator"), tokenSeperator +
			// "NaN" + System.getProperty("line.separator"));

			// Take care of empty cells in first column because they are not embedded between two token
			// separators
			if (line.substring(0, 1).equals(tokenSeperator))
				line = "NaN" + line;

			StringTokenizer strTokenLine = new StringTokenizer(line, tokenSeperator);

			columnIndex = 0;
			int intIndex = 0;
			int floatIndex = 0;
			int stringIndex = 0;

			for (EDimensionType columnDataType : columnDataTypes) {
				if (strTokenLine.hasMoreTokens()) {
					switch (columnDataType) {
						case INT:
							intArrays.get(intIndex)[lineInFile - parsingStartLine] =
								Integer.valueOf(strTokenLine.nextToken()).intValue();
							columnIndex++;
							intIndex++;
							break;
						case FLOAT:
						case CERTAINTY:
							Float value;
							tempToken = strTokenLine.nextToken();
							try {
								value = Float.parseFloat(tempToken);
							}
							catch (NumberFormatException nfe) {

								String sErrorMessage =
									"Unable to parse the data file. \""
										+ tempToken
										+ "\" at ["
										+ (columnIndex + 2)
										+ ", "
										+ (lineInFile - parsingStartLine)
										+ "] cannot be converted to a number. Please change the data selection and try again.";
								// MessageDialog.openError(new Shell(), "Error during parsing",
								// sErrorMessage);

								Logger
									.log(new Status(IStatus.ERROR, GeneralManager.PLUGIN_ID, sErrorMessage));
								value = Float.NaN;
							}

							if (useExperimentClusterInfo) {
								if (lineInFile < max - 1)
									floatArrays.get(floatIndex)[lineInFile - parsingStartLine] = value;
								// FIXME check indices here
								else if (lineInFile == max - 1)
									groupInfo.get(2)[columnIndex] = Integer.valueOf(tempToken).intValue();
								else if (lineInFile == max)
									groupInfo.get(3)[columnIndex] = Integer.valueOf(tempToken).intValue();
							}
							else
								floatArrays.get(floatIndex)[lineInFile - parsingStartLine] = value;

							floatIndex++;
							columnIndex++;
							break;
						case STRING:
							String token = strTokenLine.nextToken();
							stringLists.get(stringIndex).add(token);
							stringIndex++;
							columnIndex++;
							break;
						case SKIP: // do nothing
							strTokenLine.nextToken();
							break;
						case ABORT:
							columnIndex = columnDataTypes.size();
							break;
						default:
							throw new IllegalStateException("Unknown token pattern detected: "
								+ columnDataType.toString());
					}

					// Check if the line is finished or early aborted
					if (columnIndex == columnDataTypes.size()) {
						continue;
					}
				}
			}

			lineInFile++;

			// Update progress bar only on each 100th line
			if (lineInFile % 1000 == 0) {
				swtGuiManager.setProgressBarPercentage((int) (fProgressBarFactor * lineInFile));
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void setArraysToDimensions() {

		int intArrayIndex = 0;
		int floatArrayIndex = 0;
		int stringArrayIndex = 0;
		int dimensionIndex = 0;

		for (EDimensionType dimensionType : columnDataTypes) {
			// if(iDimensionIndex + 1 == targetDimensions.size())
			// break;
			switch (dimensionType) {
				case INT:
					targetDimensions.get(dimensionIndex).setRawData(intArrays.get(intArrayIndex));
					intArrayIndex++;
					dimensionIndex++;
					break;
				case FLOAT:
					targetDimensions.get(dimensionIndex).setRawData(floatArrays.get(floatArrayIndex));
					floatArrayIndex++;
					dimensionIndex++;
					break;
				case CERTAINTY:
					targetDimensions.get(dimensionIndex - 1).setUncertaintyData(
						floatArrays.get(floatArrayIndex));
					dataDomain.getTable().setContainsUncertaintyData(true);
					floatArrayIndex++;
					break;
				case STRING:
					ArrayList<String> rawStringData = stringLists.get(stringArrayIndex);
					// rawStringData = fillUp(rawStringData);
					((NominalDimension<String>) targetDimensions.get(dimensionIndex))
						.setRawNominalData(rawStringData);
					// stringLists.add(new ArrayList<String>(iStopParsingAtLine - parsingStartLine));
					stringArrayIndex++;
					dimensionIndex++;
					break;
				case SKIP: // do nothing
					break;
				case ABORT:
					return;

				default:
					throw new IllegalStateException("Unknown token pattern detected: "
						+ dimensionType.toString());
			}
		}
	}

	/**
	 * Method masking errors with import of categorical data FIXME: find the underlying error instead
	 * 
	 * @param rawStringData
	 * @return
	 */
	// private ArrayList<String> fillUp(ArrayList<String> rawStringData) {
	// int missingValues = nrLinesToRead - rawStringData.size();
	// if (missingValues > 0) {
	// for (int count = rawStringData.size(); count < nrLinesToRead; count++)
	// rawStringData.add("ARTIFICIAL");
	//
	// Logger.log(new Status(IStatus.ERROR, GeneralManager.PLUGIN_ID, "Had to fill up stroarge with "
	// + missingValues + " artificial strings"));
	//
	// }
	// return rawStringData;
	//
	// }

	public ArrayList<EDimensionType> getColumnDataTypes() {
		return columnDataTypes;
	}
}
