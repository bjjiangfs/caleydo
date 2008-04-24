/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package org.caleydo.core.manager.memento;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;

import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.IMementoManager;
import org.caleydo.core.manager.base.AAbstractManager;
import org.caleydo.core.manager.type.ManagerType;
import org.caleydo.core.manager.type.ManagerObjectType;
//import java.util.Enumeration;

import org.caleydo.core.command.memento.IMemento;
//import prometheus.data.collection.Set;
import org.caleydo.core.util.exception.CaleydoRuntimeExceptionType;
import org.caleydo.core.util.exception.CaleydoRuntimeException;

/**
 * Simple IMemento Manager, that stores all IMemento's in a Vector.
 * 
 * @author Michael Kalkusch
 *
 */
public class MementoManager 
 extends AAbstractManager
 implements IMementoManager {
	
	private int iVecMementoStorageSize;
	
	protected Vector<IMemento> vecMemento;
	
	protected Hashtable<Integer,Integer> hashMementoId2Index;
	
	protected final int iInitSizeMementoVector = 40;
	
	protected IGeneralManager refGeneralManager = null;
	
	/**
	 * Constructor. 
	 * 
	 * Allocates Vector and Hashtable.
	 */
	public MementoManager(final IGeneralManager setGeneralManager) {
		
		super( setGeneralManager,
				IGeneralManager.iUniqueId_TypeOffset_Memento,
				ManagerType.MEMENTO );
		
		vecMemento = new Vector<IMemento>(iInitSizeMementoVector);
		hashMementoId2Index = new Hashtable<Integer,Integer>(iInitSizeMementoVector*2);
		iVecMementoStorageSize = 0;
		
		assert setGeneralManager !=null : "MementoSimpleManager init with null-pointer for SingeltonManager";
		refGeneralManager = setGeneralManager;
		
		refGeneralManager.getSingleton().setMementoManager( this );
	}

	
	/* (non-Javadoc)
	 * @see prometheus.command.memento.manager.MementoManager#setMemento(prometheus.command.memento.Memento)
	 */
	public final int pushMemento( final IMemento addMemento) {
		final int iUniqueId = createId( ManagerObjectType.MEMENTO );
		
		try {
			vecMemento.add(addMemento);
			iVecMementoStorageSize = vecMemento.size();			
			hashMementoId2Index.put( iUniqueId, iVecMementoStorageSize-1 );
			return iUniqueId;

		} catch (Exception e) {
			throw new CaleydoRuntimeException("setMemento(IMemento) failed. " + e.toString(),
					CaleydoRuntimeExceptionType.MEMENTO );
		}
	}

	
	/* (non-Javadoc)
	 * @see prometheus.command.memento.manager.MementoManager#getMemento(int)
	 */
	public IMemento getMemento(int iMementoId) {
		
		final int iVectorIndex = hashMementoId2Index.get( iMementoId );
		
		try {
			return vecMemento.get(iVectorIndex);
		} catch (ArrayIndexOutOfBoundsException ae) {
			assert false:"getMemento(int) failed due to wrong iMementoId. " + ae.toString();
			return null;
		}
	}
	
	public IMemento pullMemento( final int iMementoId )
	{
		final int iIndex = hashMementoId2Index.get( iMementoId );
		
		try {
			IMemento pullMemento = vecMemento.get(iIndex);
			vecMemento.removeElementAt(iIndex);
			return pullMemento;
		} 
		catch (ArrayIndexOutOfBoundsException ae) {
			assert false:"getMemento(int) failed due to wrong iMementoId. " + ae.toString();
			return null;
		}
	}
	
	public boolean pullMemento( IMemento pullMemento )
	{
		return vecMemento.contains( pullMemento );
	}

	
	/* (non-Javadoc)
	 * @see prometheus.command.memento.manager.MementoManager#isMementoId(int)
	 */
	public boolean isMementoId(int iMementoId) {
		
		return this.hashMementoId2Index.contains( iMementoId );
	}
	

	/* (non-Javadoc)
	 * @see prometheus.data.manager.GeneralManagerInterface#size()
	 */
	public final int size() {		
		return vecMemento.size();
	}
	
	public String toString() {
		final String newLine = "\n";
		String result ="[";
		
		// Show content of Vector ...
		
		int iCounter = 0;
		Iterator<IMemento> iter  = vecMemento.iterator();		
		
		if ( ! iter.hasNext() ) {
			result += "Vector: is empty" + newLine;		
		}
		
		while ( iter.hasNext() ) {
			IMemento buffer = iter.next();
			
			if (buffer != null ) {
				result += " #" + iCounter + ": " + buffer.toString() + newLine;
			} else {
				result += " #" + iCounter + ": null" + newLine;
			}
			
			iCounter++;
		}
		
		result += "]"+newLine;
		
		return result;
	}
	
	/**
	 * Not used.
	 */
	public void optimize() {
		
	}

	 
	 /*
	  *  (non-Javadoc)
	  * @see prometheus.data.manager.MementoManager#clearAllMementos()
	  */
	 public void clearAllMementos() {
		 vecMemento.clear();
		 hashMementoId2Index.clear();
		 iVecMementoStorageSize = vecMemento.size();		 
	 }
	 
	
	/**
	 * Mementos are internal structures and can not be search for.
	 * 
	 * @see org.caleydo.core.manager.IGeneralManager#hasItem(int)
	 */
	public final boolean hasItem(final int iItemId) {
		return false;
	}
	
	public final Object getItem( final int iItemId) {
		return null;
	}
	
	/**
	 * Writes all current stored mementos to the ObjectOutputStream.
	 * 
	 * @return true on success
	 */
	public boolean writeToOutputStream( ObjectOutputStream outStream) {
		
		//TODO: code this
		return true;
	}
	
	/**
	 *  Reads stored mementos from ObjectInputStream.
	 * 
	 * @return true on success
	 */
	public boolean readFromInputStream( ObjectInputStream inStream ) {
		
		//TODO: code this
		
		return true;
	}
	
	public boolean unregisterItem( final int iItemId,
			final ManagerObjectType type  ) {
		
		assert false:"not done yet";
		return false;
	}

	public boolean registerItem( final Object registerItem, 
			final int iItemId , 
			final ManagerObjectType type ) {
		
		
		assert false:"not done yet";
		return false;
	}

}
