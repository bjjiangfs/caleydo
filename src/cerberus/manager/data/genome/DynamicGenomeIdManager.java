/**
 * 
 */
package cerberus.manager.data.genome;

import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

//import cerberus.base.map.MultiHashArrayMap;
import cerberus.data.mapping.GenomeIdType;
import cerberus.data.mapping.GenomeMappingDataType;
import cerberus.data.mapping.GenomeMappingType;
//import cerberus.base.map.MultiHashArrayStringMap;
import cerberus.base.map.MultiHashArrayIntegerMap;
import cerberus.base.map.MultiHashArrayStringMap;
import cerberus.manager.IGeneralManager;
import cerberus.manager.ILoggerManager.LoggerType;
import cerberus.manager.base.AAbstractManager;
import cerberus.manager.data.IGenomeIdManager;
import cerberus.manager.type.ManagerObjectType;
import cerberus.manager.type.ManagerType;
import cerberus.util.exception.CerberusRuntimeExceptionType;
import cerberus.util.exception.CerberusRuntimeException;


/**
 * @author Michael Kalkusch
 *
 */
public class DynamicGenomeIdManager
extends AAbstractManager
implements IGenomeIdManager {

	private AtomicBoolean bHasMapActiveWriter = new AtomicBoolean(false); 
	
	private GenomeMappingType currentEditingType;
	
	private IGenomeIdMap currentGenomeIdMap;
	
	protected HashMap<GenomeMappingType, IGenomeIdMap> hashType2Map;
	
	protected HashMap<GenomeMappingType, MultiHashArrayIntegerMap> hashType2MultiMapInt;
	
	protected HashMap<GenomeMappingType, MultiHashArrayStringMap> hashType2MultiMapString;
	
	public static final int iInitialSizeHashMap = 1000;
	
	public static final int iInitialCountAllLookupTables = 10;
	
	public static final int iInitialCountMultiMapLookupTables = 4;
	
	
	
	/**
	 * @param setGeneralManager
	 */
	public DynamicGenomeIdManager(IGeneralManager setGeneralManager) {

		super(setGeneralManager, 66, ManagerType.GENOME_ID );
		
		hashType2Map = new HashMap<GenomeMappingType, IGenomeIdMap> (iInitialCountAllLookupTables);
		
		hashType2MultiMapInt = new  HashMap<GenomeMappingType, MultiHashArrayIntegerMap> (iInitialCountMultiMapLookupTables);
		
		hashType2MultiMapString = new HashMap<GenomeMappingType, MultiHashArrayStringMap> (iInitialCountMultiMapLookupTables);
	}

	
	public final boolean createMapByType( final GenomeMappingType codingLutType, 
			final GenomeMappingDataType dataType ) {
		
		return createMapByType(codingLutType, 
				dataType, 
				DynamicGenomeIdManager.iInitialSizeHashMap );
	}
	
	public boolean createMapByType( final GenomeMappingType codingLutType, 
			final GenomeMappingDataType dataType,
			final int iSetInitialSizeHashMap ) {
		
		/* conisitency check */
		int iCurrentInitialSizeHashMap = iSetInitialSizeHashMap;
		
		if ( hashType2Map.containsKey( codingLutType ) ) 
		{
			refGeneralManager.getSingelton().logMsg(
					"createMapByType(" + 
					codingLutType.toString() + "," +
					dataType.toString() + ",*) WARNING! type is already registered!",
					LoggerType.VERBOSE);
			
			return false;
		}
		
		if  (iSetInitialSizeHashMap < 2)
		{
			iCurrentInitialSizeHashMap = iInitialSizeHashMap;
		}
		
		IGenomeIdMap newMap = null;
		
		refSingelton.logMsg("createMapByType(" +
				codingLutType.toString() + "," +
				dataType.toString() + ",*) ...",
				LoggerType.VERBOSE);
		
		try //catch ( OutOfMemoryError oee ) 
		{
			
			switch ( dataType ) 
			{
			case INT2INT:
				newMap = new GenomeIdMapInt2Int(dataType,iCurrentInitialSizeHashMap);
				break;
				
			case INT2STRING:
				newMap = new GenomeIdMapInt2String(dataType, iCurrentInitialSizeHashMap);
				break;
				
			case STRING2INT:
				newMap = new GenomeIdMapString2Int(dataType, iCurrentInitialSizeHashMap);
				break;
				
			case STRING2STRING:
				newMap = new GenomeIdMapString2String(dataType,iCurrentInitialSizeHashMap);
				break;
		
			/* Multi Map's */
				
			case MULTI_STRING2STRING:
				MultiHashArrayStringMap newMultiMapString = new MultiHashArrayStringMap(iCurrentInitialSizeHashMap);
				hashType2MultiMapString.put( codingLutType, newMultiMapString );
				return true;
				
			case MULTI_INT2INT:
//			case MULTI_STRING2STRING_USE_LUT:
//				MultiHashArrayIntegerMap newMultiMap = new MultiHashArrayIntegerMap(iCurrentInitialSizeHashMap);
//				hashType2MultiMapInt.put( codingLutType, newMultiMap );
//				return true;
				
			default:
				assert false : "createMap() type=" + dataType + " is not supported";
				return false;
				
			} // switch ( dataType ) 
			
			hashType2Map.put( codingLutType, newMap );
			
		} 
		catch ( OutOfMemoryError oee ) 
		{
			System.err.println(" Could not allocate memory for HashMap [" +
					codingLutType + "] tpye=[" + dataType + "]");
					
			throw oee;
//			return false;
		}
		
		return true;
	}
	
	public final IGenomeIdMap getMapByType( final GenomeMappingType type ) {
		
		return hashType2Map.get( type );
	}
	
	public final MultiHashArrayIntegerMap getMultiMapIntegerByType( final GenomeMappingType type ) {
		
		return hashType2MultiMapInt.get( type );
	}
	
	public final MultiHashArrayStringMap getMultiMapStringByType( final GenomeMappingType type ) {
		
		return hashType2MultiMapString.get( type );
	}
	
	/**
	 * @see cerberus.manager.data.IGenomeIdManager#hasAnyMapByType(cerberus.data.mapping.GenomeMappingType)
	 */
	public final boolean hasAnyMapByType( final GenomeMappingType codingLutType ) {
		
		if (  hasMapByType( codingLutType ) ) {
			return true;
		}
		
		return hasMultiMapByType(codingLutType);
	}
	
	public final boolean hasMapByType( final GenomeMappingType codingLutType ) {
		
		return hashType2Map.containsKey( codingLutType );
	}
	
	public final boolean hasMultiMapByType( final GenomeMappingType codingLutType ) {
		
		if ( hashType2MultiMapInt.containsKey( codingLutType ) ) 
		{
			return true;
		}
		
		if ( hashType2MultiMapString.containsKey( codingLutType ) ) 
		{
			return true;
		}
		
		return false;
	}	
	
	/* (non-Javadoc)
	 * @see cerberus.manager.data.IGenomeIdManager#buildLUT_startEditingSetTypes(cerberus.data.mapping.GenomeIdType, cerberus.data.mapping.GenomeIdType)
	 */
	public boolean buildLUT_startEditing( final GenomeMappingType type ) {

		if ( ! bHasMapActiveWriter.compareAndSet(false,true) ) {
			return false;
		}
		
		currentEditingType = type;
		
		if ( type.isMultiMap() )
		{
			//TODO: register multi hash map to!
			//assert false :  "TODO: register multi hash map to!";
			
			return true;
		}
		else
		{ // if ( type.isMultiMap() ) ... else 
			currentGenomeIdMap = hashType2Map.get( type );
			
			if ( currentGenomeIdMap == null ) {
				throw new CerberusRuntimeException(
						"buildLUT_startEditingSetTypes(" + 
						type + ") is not allocated!",
						CerberusRuntimeExceptionType.DATAHANDLING);
			}
			
			return true;
		} //if ( type.isMultiMap() )

	}

	


	/* (non-Javadoc)
	 * @see cerberus.manager.data.IGenomeIdManager#buildLUT_stopEditing()
	 */
	public boolean buildLUT_stopEditing( final GenomeMappingType type ) {

		if ( ! bHasMapActiveWriter.compareAndSet(true,false) ) 
		{
			return false;
		}
		
		/* consistency check */
		
		if ( type.isMultiMap() )
		{
			//TODO: register multi hash map to!
			//assert false :  "TODO: register multi hash map to!";
			
			currentEditingType = GenomeMappingType.NON_MAPPING;
			
			return true;
		} // if ( type.isMultiMap() ) ... else 
		
		if ( ! currentEditingType.equals( type ) ) 
		{
			throw new CerberusRuntimeException("buildLUT_stopEditing(" + type +
					") differs from current type=[" +
					currentEditingType + "]");
		}

		currentEditingType = GenomeMappingType.NON_MAPPING;
		
		return true;
	}

	
	public int getIdIntFromStringByMapping(
			final String sCerberusId, 
			final GenomeMappingType type) {

		IGenomeIdMap buffer = hashType2Map.get( type );
		
		assert buffer != null : "getIdFromStringByMapping(" + type +") type is not allocated";
		
		return buffer.getIntByString( sCerberusId );
	}


	public int getIdIntFromIntByMapping(
			final int iCerberusId, 
			final GenomeMappingType type) {

		IGenomeIdMap buffer = hashType2Map.get( type );
		
		assert buffer != null : "getIdFromIntByMapping(" + type +") type is not allocated";
		
		return buffer.getIntByInt( iCerberusId );
	}
	
	public String getIdStringFromStringByMapping(
			final String sCerberusId, 
			final GenomeMappingType type) {

		IGenomeIdMap buffer = hashType2Map.get( type );
		
		assert buffer != null : "getStringIdFromStringByMapping(" + type +") type is not allocated";
		
		return buffer.getStringByString( sCerberusId );
	}


	public String getIdStringFromIntByMapping(
			final int iCerberusId, 
			final GenomeMappingType type) {

		IGenomeIdMap buffer = hashType2Map.get( type );
		
		assert buffer != null : "getStringIdFromIntByMapping(" + type +") type is not allocated";
		
		return buffer.getStringByInt( iCerberusId );
	}


	public ArrayList<Integer> getIdListByType(int iCerberusId, GenomeIdType type) {

		// TODO Auto-generated method stub
		return null;
	}


	public final boolean isBuildLUTinProgress() {

		return bHasMapActiveWriter.get();
	}


	public boolean hasItem(int iItemId) {

		//assert false : "methode not implemented";
		return false;
	}


	public Object getItem(int iItemId) {

		assert false : "methode not implemented";
		return null;
	}


	public int size() {

		return 0;
	}


	public boolean registerItem(Object registerItem, int iItemId, ManagerObjectType type) {

		assert false : "methode not implemented";
		return false;
	}


	public boolean unregisterItem(int iItemId, ManagerObjectType type) {

		assert false : "methode not implemented";
		return false;
	}


	/**
	 * @see cerberus.manager.data.IGenomeIdManager#setMapByType(cerberus.data.mapping.GenomeMappingType, java.lang.Object)
	 * 
	 * @see cerberus.manager.data.genome.IGenomeIdMap
	 * @see cerberus.base.map.MultiHashArrayStringMap
	 * @see cerberus.base.map.MultiHashArrayIntegerMap
	 */
	public void setMapByType(final GenomeMappingType codingLutType, 
			Object map) {
		
		if (map.getClass().equals(MultiHashArrayIntegerMap.class)) {
			hashType2MultiMapInt.put(codingLutType, (MultiHashArrayIntegerMap)map);
			return;
		}
		
		if (map.getClass().equals(MultiHashArrayStringMap.class)) {
			hashType2MultiMapString.put(codingLutType, (MultiHashArrayStringMap)map);
			return;
		}
		
		try {
			hashType2Map.put(codingLutType, (IGenomeIdMap) map);
		}
		catch (NullPointerException npe) {
			throw new CerberusRuntimeException("setMapByType(final GenomeMappingType codingLutType, Object map) unsupported object=" +
					map.getClass().toString(),
					CerberusRuntimeExceptionType.DATAHANDLING);		
		}
	}

}
