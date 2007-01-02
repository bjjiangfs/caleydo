/**
 * 
 */
package cerberus.xml.parser.handler.importer.ascii.lookuptable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import cerberus.data.mapping.GenomeMappingType;
import cerberus.manager.IGeneralManager;
import cerberus.manager.ILoggerManager.LoggerType;
import cerberus.xml.parser.handler.importer.ascii.LookupTableLoaderProxy;


/**
 * @author Michael Kalkusch
 *
 */
public class LookupTableIntIntLoader extends ALookupTableLoader
		implements ILookupTableLoader {

	protected HashMap <Integer,Integer> refHashMap;
	
	/**
	 * @param setGeneralManager
	 * @param setFileName
	 */
	public LookupTableIntIntLoader(final IGeneralManager setGeneralManager,
			final String setFileName,
			final GenomeMappingType genometype,
			final LookupTableLoaderProxy setLookupTableLoaderProxy ) {

		super(setGeneralManager, setFileName, genometype, setLookupTableLoaderProxy);
		
	}
	
	public void setHashMap_IntegerInteger( HashMap <Integer,Integer> setHashMap ) {
		
		
		Class buffer = setHashMap.getClass();
		
		this.refHashMap = (HashMap <Integer,Integer>) setHashMap;
	}

	/* (non-Javadoc)
	 * @see cerberus.xml.parser.handler.importer.ascii.lookuptable.ILookupTableLoader#loadDataParseFileLUT(java.io.BufferedReader, int)
	 */
	public boolean loadDataParseFileLUT(BufferedReader brFile,
			int iNumberOfLinesInFile ) throws IOException {

		String sLine;
		
		int iLineInFile = 1;
		int iStartParsingAtLine = refLookupTableLoaderProxy.getStartParsingAtLine();
		int iStopParsingAtLine  = refLookupTableLoaderProxy.getStopParsingAtLine();
		
	    while ( ((sLine = brFile.readLine()) != null)&&
	    		( iLineInFile <= iStopParsingAtLine) )  
	    {
			
	    	/**
	    	 * Start parsing if current line 
	    	 * iLineInFile is larger than iStartParsingAtLine ..
	    	 */
			if( iLineInFile > iStartParsingAtLine ){
				
				boolean bMaintainLoop = true;
				StringTokenizer strTokenText = 
					new StringTokenizer(sLine, 
							refLookupTableLoaderProxy.getTokenSeperator() );
				
				/**
				 * Read all tokens
				 */
				while (( strTokenText.hasMoreTokens() )&&(bMaintainLoop)) {
					
					/**
					 * Excpect two Integer values in one row!
					 */
					
					try {
						int iFirst = new Integer( strTokenText.nextToken() );
						
						if  ( strTokenText.hasMoreTokens() ) 
						{
							int iSecond = new Integer(strTokenText.nextToken());
							
							refHashMap.put(iFirst,iSecond);
						}
						
					
					} catch ( NoSuchElementException  nsee) {
						/* no ABORT was set. 
						 * since no more tokens are in ParserTokenHandler skip rest of line..*/
						bMaintainLoop = false;
					} catch ( NullPointerException npe ) {
						bMaintainLoop = false;
						System.out.println( "NullPointerException! " + npe.toString() );
						npe.printStackTrace();
					}
				
				} // end of: while (( strToken.hasMoreTokens() )&&(bMaintainLoop)) {
				
				
				refLookupTableLoaderProxy.progressBarStoredIncrement();
				
			} // end of: if( iLineInFile > this.iHeaderLinesSize) {			
			
			iLineInFile++;
			
		
	    } // end: while ((sLine = brFile.readLine()) != null) { 
	 
		return true;
	}

}
