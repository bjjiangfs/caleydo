package org.caleydo.core.application.mapping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

/**
 * Helper class that extracts multiple accession numbers mapped to the same storage index. separated. EXAMPLE:
 * Input NM_012156.2;NM_177996.1 1770 Output: NM_012156.2;1770 NM_177996.1;1770
 * 
 * @author Marc Streit
 */
public class MultipleAccessionCodeExtractor {

	protected static char cAccessionToExpressionStorageIndexDelimiter = ';';

	protected static String sMultipleAccessionSeparator = ",";

	protected PrintWriter writer;

	public MultipleAccessionCodeExtractor()
		throws IOException {

		writer = new PrintWriter("data/genome/mapping/accession_code_2_microarray_expression_storage_index.map");
	}

	protected void convertData() throws IOException {

		// Reading input by lines
		BufferedReader in =
			new BufferedReader(new FileReader(
				"data/genome/mapping/accession_code_2_microarray_expression_storage_index_ORIG.map"));

		String sInputLine = "";
		String sAccessionCodes = "";
		String sMicroarrayExpressionStorageIndex = "";

		while ((sInputLine = in.readLine()) != null) {
			sAccessionCodes =
				sInputLine.substring(0, sInputLine.indexOf(cAccessionToExpressionStorageIndexDelimiter));

			StringTokenizer strTokenText = new StringTokenizer(sAccessionCodes, sMultipleAccessionSeparator);

			// Nothing todo because there is only one or none accession
			if (strTokenText.countTokens() <= 1) {
				// Write out original input line without modification
				writer.println(sInputLine);
			}
			else {
				sMicroarrayExpressionStorageIndex =
					sInputLine.substring(sInputLine.indexOf(cAccessionToExpressionStorageIndexDelimiter) + 1,
						sInputLine.length());

				while (strTokenText.hasMoreTokens()) {
					writer.println(strTokenText.nextToken() + cAccessionToExpressionStorageIndexDelimiter
						+ sMicroarrayExpressionStorageIndex);
				}
			}
		}

		in.close();

		writer.flush();

	}

	public static void main(String[] args) {

		try {
			MultipleAccessionCodeExtractor enzymeCodeConverter = new MultipleAccessionCodeExtractor();

			enzymeCodeConverter.convertData();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}