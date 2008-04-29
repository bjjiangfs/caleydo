package org.caleydo.core.command.data.filter;

//import java.util.ArrayList;
//import java.util.Iterator;

import org.caleydo.core.data.collection.IStorage;
import org.caleydo.core.command.CommandQueueSaxType;
import org.caleydo.core.command.base.ACmdCreate_IdTargetLabelAttrDetail;
import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.StorageType;
import org.caleydo.core.manager.ICommandManager;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.util.exception.CaleydoRuntimeException;
//import org.caleydo.core.data.collection.storage.FlatThreadStorageSimple;


/**
 * @author Alexander Lex
 * 
 *  This class calculates the min and the max value of a storage or a set
 *  It is implemented as a command and as a filter.
 *
 *	TODO: Min max for set not implemented yet
 *
 */

public class CmdDataFilterMinMax 
extends ACmdCreate_IdTargetLabelAttrDetail 
{

	
	private ISet mySet = null;
	private IStorage myStorage = null;
	private StorageType myStorageType = null;
	
	private int iMinValue = Integer.MAX_VALUE;
	private int iMaxValue = Integer.MIN_VALUE;	
	private float fMinValue = Float.MAX_VALUE;
	private float fMaxValue = Float.MIN_VALUE;
	private double dMinValue = Double.MAX_VALUE;
	private double dMaxValue = Double.MIN_VALUE;
	
//	private int iSetMinValue;
//	private int iSetMaxValue;	
//	private float fSetMinValue;
//	private float fSetMaxValue;
//	private double dSetMinValue;
//	private double dSetMaxValue;
	
	/**
	 * Constructor.
	 * 
	 * @param refGeneralManager
	 * @param refCommandManager
	 * @param refCommandQueueSaxType
	 */
	public CmdDataFilterMinMax(IGeneralManager refGeneralManager,
			ICommandManager refCommandManager,
			CommandQueueSaxType refCommandQueueSaxType) {

		super(refGeneralManager, refCommandManager, refCommandQueueSaxType);
		
	//	iAlStorageId = new ArrayList<Integer>();
	}
	
	/**	
	 * Calculates the minimum and the maximum of either a set or a storage
	 * depending on what has been set using the setAttributes methods	
	 */
	public void doCommand() throws CaleydoRuntimeException 
	{
		if(myStorage == null && mySet != null)
		{
			calculateMinMaxOnSet();
		}
		else if(myStorage != null && mySet == null)
		{
			calculateMinMaxOnStorage();
		}
		else
		{
		//	throw CaleydoRuntimeException("You have to initialize the filter before using it");
		}		
	}

	/*
	 * (non-Javadoc)
	 * @see org.caleydo.core.command.ICommand#undoCommand()
	 */
	public void undoCommand() throws CaleydoRuntimeException 
	{
		// TODO Auto-generated method stub
	}
	
	/**
	 * You have to set the attributes of the command before executing doCommand()
	 * This can be done with this method, if you want to calculate the min and max
	 * of a storage (in contrast to a set). 
	 * 
	 * @param myStorage The storage 
	 * @param myStorageType The type of the storage (int, float, or double is supported)
	 */
	public void setAttributes(IStorage myStorage, final StorageType myStorageType)
	{
		this.myStorage = myStorage;
		this.myStorageType = myStorageType;
	}
	
	/**
	 * You have to set the attributes of the command before executing doCommand()
	 * This can be done with this method, if you want to calculate the min and max
	 * of a set (in contrast to a storage). The calculated min and max are the 
	 * absolute min and max of all contained storages. 
	 * 
	 * @param mySet The set
	 * @param myStorageType The type of storage (int, float or double is supported)
	 */
	
	public void setAttributes(ISet mySet, final StorageType myStorageType)
	{
		this.mySet = mySet;
		this.myStorageType = myStorageType;
	}
	
	private void calculateMinMaxOnStorage()
	{		
		
		//TODO: change to case if clear that a storage can be of only one type a
		// and throw an exception if other type is encountered
		if (myStorageType == StorageType.INT)
		{		
			for(int iCount = 0; iCount < myStorage.getArrayInt().length; iCount++)
			{
				int iCurrentValue = (myStorage.getArrayInt())[iCount];
				
				// TODO handle NaN values
				
				if(iCurrentValue < iMinValue)
					iMinValue = iCurrentValue;
				if(iCurrentValue > iMaxValue)
					iMaxValue = iCurrentValue;				
			}
		}
		
		if (myStorageType == StorageType.FLOAT)
		{
			for(int iCount = 0; iCount < myStorage.getArrayFloat().length; iCount++)
			{
				float fCurrentValue = (myStorage.getArrayFloat())[iCount];

				if (Float.isNaN(fCurrentValue))
					continue;
				
				if(fCurrentValue < fMinValue)
				{
					fMinValue = fCurrentValue;
					continue;
				}
				if(fCurrentValue > fMaxValue)
					fMaxValue = fCurrentValue;				
			}			
		}
		
		if (myStorageType == StorageType.DOUBLE)
		{
			for(int iCount = 0; iCount < myStorage.getArrayDouble().length; iCount++)
			{
				double dCurrentValue = (myStorage.getArrayDouble())[iCount];
				if (iCount == 0)
				{
					dMinValue = dCurrentValue;
					dMaxValue = dCurrentValue;	
					continue;
				}
				if(dCurrentValue < dMinValue)
				{
					dMinValue = dCurrentValue;
					continue;
				}
				if(dCurrentValue > dMaxValue)
					dMaxValue = dCurrentValue;				
			}			
		}
	}
	
	private void calculateMinMaxOnSet()
	{
		// TODO: Implement Sets here
		
		
	}

	// TODO Handle if they are not set!
	
	public int getIMinValue() {
	
		return iMinValue;
	}

	
	public int getIMaxValue() {
	
		return iMaxValue;
	}

	
	public float getFMinValue() {
	
		return fMinValue;
	}

	
	public float getFMaxValue() {
	
		return fMaxValue;
	}

	
	public double getDMinValue() {
	
		return dMinValue;
	}

	
	public double getDMaxValue() {
	
		return dMaxValue;
	}
	
}
