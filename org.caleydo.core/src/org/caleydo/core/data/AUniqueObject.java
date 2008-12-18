package org.caleydo.core.data;


/**
 * Abstract class providing methods defined in IUniqueObject.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 */
public abstract class AUniqueObject
	implements IUniqueObject
{
	/**
	 * Unique Id
	 */
	final protected int iUniqueID;

	/**
	 * Constructor.
	 * 
	 * @param iUniqueID the unique ID generated by the system
	 * @param generalManager the general manager
	 */
	protected AUniqueObject(final int iUniqueID)
	{
		this.iUniqueID = iUniqueID;
	}

	@Override
	public final int getID()
	{
		return this.iUniqueID;
	}
}
