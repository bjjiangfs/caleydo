/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package cerberus.data;

import cerberus.manager.GeneralManager;
//import cerberus.manager.type.BaseManagerType;
import cerberus.data.UniqueManagedInterface;
import cerberus.data.AbstractUniqueItem;
import cerberus.util.exception.CerberusRuntimeException;

/**
 * Abstract class providing methodes defiend in UniqueManagedInterface.
 * Stores reference to creator of item in private variable.
 * 
 * @author Michael Kalkusch
 *
 * @see cerberus.data.UniqueReManagedObject
 * @see prometheus.data.xml.MementiItemXML
 */
public abstract class UniqueManagedObject 
extends AbstractUniqueItem
implements UniqueManagedInterface {

	/**
	 * Reference to manager, who created this object.
	 */
	protected final GeneralManager refGeneralManager;
	
	/**
	 * 
	 */
	protected UniqueManagedObject( int iSetCollectionId, GeneralManager setGeneralManager ) {

		super( iSetCollectionId );
		
		assert setGeneralManager != null: "SetFlatSimple() with null pointer";
		
		refGeneralManager = setGeneralManager;

	}

	/* (non-Javadoc)
	 * @see prometheus.data.collection.UniqueManagedInterface#getManager()
	 */
	public final GeneralManager getManager() {
		return this.refGeneralManager;
	}

	/**
	 * Reset GeneralManger object
	 * 
	 * @see cerberus.data.UniqueReManagedObject#setManager(cerberus.manager.GeneralManager)
	 * @see cerberus.data.UniqueManagedInterface#getGeneralManager()
	 */
	final protected void setManager( final GeneralManager setGeneralManager) {
		throw new CerberusRuntimeException("setManager() prohibited inside this class!");
		//this.refGeneralManager = setGeneralManager;
	}
	
	
	//public abstract ManagerObjectType getBaseType();
	
}
