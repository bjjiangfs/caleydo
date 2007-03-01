/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package cerberus.data.collection.set;

import java.util.Iterator;
import java.util.Vector;

import cerberus.manager.IGeneralManager;
import cerberus.manager.type.ManagerObjectType;

import cerberus.data.collection.IMetaData;
import cerberus.data.collection.IVirtualArray;
import cerberus.data.collection.IStorage;
import cerberus.data.collection.ISet;
import cerberus.data.collection.SetType;
import cerberus.data.collection.parser.CollectionSetSaxParserHandler;
//import cerberus.data.collection.selection.iterator.SelectionVectorIterator;
import cerberus.data.collection.set.ASetSimple;
import cerberus.data.collection.virtualarray.iterator.IVirtualArrayIterator;
import cerberus.xml.parser.ISaxParserHandler;

/**
 * Threadsafe set.
 * 
 * @author Michael Kalkusch
 * 
 * @deprecated Use SetFlatThreadSimple
 */
public class SetFlatSimple 
extends ASetSimple
implements ISet {

	/**
	 * Since only one Selections is stored only one 
	 * MetaData object is needed. 
	 */
	protected IMetaData refMetaDataAllAndAny = null;
	
	/**
	 * Store reference to the IVirtualArray.
	 */
	protected IVirtualArray[] refFlatSelection = null;
	
	/**
	 * Store reference to the Storages.
	 */
	protected IStorage[] refFlatStorage = null;
	
	/**
	 * Variable for the dimension of this set.
	 */
	protected int iSizeDimension = 1;
	

	/**
	 * 
	 */
	protected SetFlatSimple( int iSetCollectionId, 
			final IGeneralManager setGeneralManager,
			final SetType setType) {

		super( iSetCollectionId, 
				setGeneralManager, 
				setType );
		
	}

	public SetFlatSimple( int iSetCollectionId, 
			final IGeneralManager setGeneralManager) {

		super( iSetCollectionId, 
				setGeneralManager, 
				SetType.SET_RAW_DATA );
		
	}
	
	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#setSelection(cerberus.data.collection.IVirtualArray, int)
	 */
	public boolean setVirtualArrayByDim(IVirtualArray[] addVirtualArray, int iAtDimension) {
		if ( refFlatSelection != null ) {
			refFlatSelection = null;
		}
		
		//FIXME Test Range!
		refFlatSelection = addVirtualArray;
		
		return true;
	}
	
	public boolean setVirtualArrayByDimAndIndex( final IVirtualArray addVirtualArray, 
			final int iAtDimension, 
			final int iAtIndex ) {
	
		if ( refFlatSelection == null ) {
			refFlatSelection = new IVirtualArray[iSizeDimension];
		}
		if ( refFlatStorage == null ) {
			refFlatStorage = new IStorage[iSizeDimension];
		}
		
		refFlatSelection[iAtIndex] = addVirtualArray;
		
		return true;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#removeSelection(cerberus.data.collection.IVirtualArray, int)
	 */
	public boolean removeVirtualArray( final IVirtualArray[] removeVirtualArray, final int iFromDimension) {
		
		for ( int i=0; i< removeVirtualArray.length; i++ ) {
			if ( refFlatSelection[iFromDimension] == removeVirtualArray[i] ) {
				refFlatSelection[iFromDimension] = null;
				return true;
			}
		}
		return false;		
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#hasSelection(cerberus.data.collection.IVirtualArray, int)
	 */
	public boolean hasVirtualArray(IVirtualArray testVirtualArray, int iAtDimension) {
		
		assert testVirtualArray != null: "SetFlatSimple.hasSelection() test with null pointer!";
		
		//FIXME add range check...
		if ( refFlatSelection[iAtDimension] ==  testVirtualArray ) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#hasSelectionInSet(cerberus.data.collection.IVirtualArray)
	 */
	public boolean hasVirtualArrayInSet(IVirtualArray testVirtualArray) {
		
		//FIXME return index instead of true or false
		
		for ( int iIndex=0; iIndex < this.refFlatSelection.length ; iIndex++ ) {
			if ( refFlatSelection[iIndex] ==  testVirtualArray)
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getDimensionSizeForAllSelections()
	 */
	public int[] getDimensionSizeForAllVirtualArrays() {
		
		//FIXME what shall that function do?
		return null;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getDimensionSize(int)
	 */
	public int getDimensionSize(int iAtDimension) {
		//FIXME what shall that function do?
		return iSizeDimension;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getDimensions()
	 */
	public int getDimensions() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#setDimensionSize(int, int)
	 */
	public void setDimensionSize(int iIndexDimension, int iValueDimensionSize) {
		//FIXME what shall that function do?
		iSizeDimension = iValueDimensionSize;
	}





	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getSelection(int)
	 */
	public IVirtualArray[] getVirtualArrayByDim(int iAtDimension) {
		//FIXME add range check..
		return refFlatSelection;
	}

	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getSelectionByDimAndIndex(int, int)
	 */
	public IVirtualArray getVirtualArrayByDimAndIndex( final int iAtDimension, 
			final int iAtIndex ) {
		return refFlatSelection[iAtIndex];
	}

	/**
	 * Since only one selection is stored only one MetaData obejct is used.
	 * 
	 * @param setMetaData sets the meta data
	 * @see cerberus.data.collection.IMetaDataCollection#setMetaData(cerberus.data.collection.IMetaData)
	 * 
	 */
	public void setMetaData(IMetaData setMetaData) {
		
		assert setMetaData != null :"setMetaData() with null-pointer.";
		
		refMetaDataAllAndAny = setMetaData;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.IMetaDataCollection#getMetaData()
	 */
	public IMetaData getMetaData() {
		return refMetaDataAllAndAny;
	}
	
	/**
	 * Since only one selection is stored only one MetaData obejct is used.
	 * 
	 * @return curretn meta data
	 * 
	 * @see cerberus.data.collection.IMetaDataSet#getMetaData()
	 */
	public IMetaData getMetaDataAny() {
		return refMetaDataAllAndAny;
	}

	/**
	 * Since only one selection is stored only one MetaData obejct is used.
	 * 
	 * @param sets the meta data
	 * 
	 * @see cerberus.data.collection.IMetaDataSet#setMetaData(cerberus.data.collection.IMetaData)
	 */
	public void setMetaDataAny(IMetaData setMetaData) {
		setMetaData( setMetaData );
	}

	/**
	 * No subsets are available.
	 * 
	 * @see cerberus.data.collection.ISubSet#getSubSets()
	 */
	public ISet[] getSubSets() {
		
		assert false: "SetFlatSimple.getSubSets() SetFlatSimple does not supper ISubSet's.";
	
		return null;
	}

	/**
	 * No subsets are available.
	 * 
	 * @see cerberus.data.collection.ISubSet#hasSubSets()
	 */
	public boolean hasSubSets() {
		return false;
	}

	/**
	 * No subsets are available.
	 * 
	 * @see cerberus.data.collection.ISubSet#addSubSet(cerberus.data.collection.ISet)
	 */
	public boolean addSubSet(ISet addSet) {
		throw new RuntimeException("SetFlatSimple.addSubSet() SetFlatSimple does not supper ISubSet's.");
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISubSet#swapSubSet(cerberus.data.collection.ISet, cerberus.data.collection.ISet)
	 */
	public boolean swapSubSet(ISet fromSet, ISet toSet) {
		
		assert false: "SetFlatSimple.swapSubSet() SetFlatSimple does not supper ISubSet's.";
	
		return false;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISubSet#removeSubSet(cerberus.data.collection.ISet)
	 */
	public boolean removeSubSet(ISet addSet) {

		assert false: "SetFlatSimple.removeSubSet() SetFlatSimnple does not supper ISubSet's.";
	
		return false;
	}
	
	/**
	 * Restore state of object by update data from SaxHandler
	 * 
	 * @param refSaxHandler reference to SaxHandler
	 * @return TRUE if the provided handler provided same Id as object.
	 */
	public boolean setMementoXML_usingHandler( final ISaxParserHandler refSaxHandler ) {
		
		try {
			CollectionSetSaxParserHandler parser = 
				(CollectionSetSaxParserHandler) refSaxHandler;
			
			if ( parser.getDim() < 0 ) {
				assert false:"Parser does not return informations neede";
				return false;
			}
			/**
			 * Stores the a list of Selections and Storages used by this set.
			 */
			final int[] iListOfSellectionId = parser.getSelectByDim( 0 );
			final int[] iListOfStorageId = parser.getStorageByDim( 0 );
			
			
			/**
			 * Store reference to the Selections.
			 */
			refFlatSelection = new IVirtualArray[iListOfSellectionId.length];
			
			for ( int iIndex=0; iIndex< iListOfSellectionId.length ; iIndex++ ) {
				
				try {					
					Object buffer = getManager().getItem( iListOfSellectionId[iIndex] );
					refFlatSelection[iIndex] = (IVirtualArray) buffer;
				}
				catch ( NullPointerException npe) {
					npe.printStackTrace();
					throw npe; 
				}
			}
			
			/**
			 * Store reference to the Storages.
			 */
			refFlatStorage = new IStorage[iListOfStorageId.length];
			
			for ( int iIndex=0; iIndex< iListOfStorageId.length ; iIndex++ ) {
				
				try {					
					Object buffer = getManager().getItem( iListOfStorageId[iIndex] );
					refFlatStorage[iIndex] = (IStorage) buffer;
				}
				catch ( NullPointerException npe) {
					npe.printStackTrace();
					throw npe; 
				}
			}
			
			getManager().unregisterItem( getId(), 
					ManagerObjectType.SET_LINEAR );
			
			getManager().registerItem( this, 
					parser.getXML_DataComponent_Id(), 
					ManagerObjectType.SET_LINEAR );
			
			setId( parser.getXML_DataComponent_Id() );		
			
			return true;
		}
		catch (NullPointerException npe) {
			return false;
		}
		
	}
	
	/**
	 * @see cerberus.data.xml.IMementoXML#createMementoXML()
	 * @return String containing all information on the state 
	 * of the object in XML form with out a header.
	 */
	public String createMementoXML() {
		
//		final String openDetail = "<DataComponentItemDetails type=\"";
//		final String closeDetail = "</DataComponentItemDetails>\n";
		
		//FIXME IMemento is not created yet!
		
		assert false:"IMemento of ISet is not created yet!";
		
		return createMementoXML_Intro(
				ManagerObjectType.VIRTUAL_ARRAY_SINGLE_BLOCK.name())
			+ "</DataComponentItem>\n";
	}

	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.UniqueManagedInterface#getBaseType()
	 */
	public ManagerObjectType getBaseType() {
		return ManagerObjectType.SET_LINEAR;
	}
	
	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getStorage()
	 */
	public final IStorage[] getStorageByDim( final int iAtDimension ) {
		return refFlatStorage;
	}
	
	public final IStorage getStorageByDimAndIndex( final int iAtDimension, 
			final int iAtIndex ) {
		if ( refFlatStorage != null ) 
			return refFlatStorage[iAtIndex];
		return null;
	}
	
	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#setStorage(cerberus.data.collection.IStorage)
	 */
	public final void setStorageByDim(IStorage[] setStorage, final int iAtDimension ) {
		
		assert setStorage != null: "setStorage() with null-pointer";
		
		refFlatStorage = setStorage;
	}
	
	public boolean setStorageByDimAndIndex( final IStorage addStorage, 
			final int iAtDimension, 
			final int iAtIndex ) {
	
		if ( refFlatSelection == null ) 
		{
			refFlatSelection = new IVirtualArray[iSizeDimension];
		}
		if ( refFlatStorage == null )
		{
			refFlatStorage = new IStorage[iSizeDimension];
		}
		
		this.refFlatStorage[iAtIndex] = addStorage;
		
		return true;
	}

	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ICollection#getCacheId()
	 */
	public int getCacheId() {
		return this.iCacheId;
	}
	
	public Iterator<IVirtualArray> iteratorSelection() {
		
		Vector<IVirtualArray> vec_Selection = new Vector<IVirtualArray> (refFlatSelection.length);
		
		for ( int i=0; i <refFlatSelection.length; i++ ) {					
			vec_Selection.addElement( refFlatSelection[i] );
		}
		
		return vec_Selection.iterator();
	}
	
	public Iterator<IStorage> iteratorStorage() {
		
		Vector<IStorage> vec_Storage = new Vector<IStorage> (refFlatStorage.length);
		
		for ( int i=0; i <refFlatStorage.length; i++ ) {					
			vec_Storage.addElement( refFlatStorage[i] );
		}
		
		return vec_Storage.iterator();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ISet#iteratorStorageByDim(int)
	 */
	public Iterator<IStorage> iteratorStorageByDim( final int iAtDimension ) {
		
		return null;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ISet#iteratorSelectionByDim(int)
	 */
	public IVirtualArrayIterator iteratorVirtualArrayByDim( final int iAtDimension ) {
		
		return null;
	}
	
	public boolean setVirtualArrayByDim( final Vector<IVirtualArray> addVirtualArray, 
			final int iAtDimension ) {
		
		IVirtualArray [] createArray = new IVirtualArray [ addVirtualArray.size() ];
		
		Iterator <IVirtualArray> iter = addVirtualArray.iterator();
		
		for ( int i=0; iter.hasNext(); i++ )
		{
			createArray[i] = iter.next();
		}
		
		refFlatSelection = createArray;
		
		return true;
	}
	
	public boolean setStorageByDim( final Vector<IStorage> addStorage, 
			final int iAtDimension ) {
		
		IStorage [] setNewArray = new IStorage [ addStorage.size() ];
		
		Iterator <IStorage> iter = addStorage.iterator();
		
		for ( int i=0; iter.hasNext(); i++ )
		{
			setNewArray[i] = iter.next();
		}
		
		refFlatStorage = setNewArray;
		
		return true;
	}
	
	public Vector<IStorage> getStorageVectorByDim( final int iAtDimension ) {
		 int iLengthStorage = refFlatStorage.length;
		 
		 Vector<IStorage> vecResult = new Vector<IStorage> (iLengthStorage);
		 
		 for ( int i=0; i< iLengthStorage; i++) {
			 vecResult.addElement( refFlatStorage[i] );
		 }
		 
		 return vecResult;
	}

	public Vector<IVirtualArray> getVirtualArrayVectorByDim( final int iAtDimension ) {
		int iLengthSelection = refFlatSelection.length;
		
		Vector<IVirtualArray> vecResult = new Vector<IVirtualArray> (iLengthSelection);
		 
		for ( int i=0; i< iLengthSelection; i++) {
			vecResult.addElement( refFlatSelection[i] );
			}
 
		 return vecResult;
	}

	
}
