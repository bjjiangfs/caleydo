package org.caleydo.core.data;

/**
 * Abstract class providing methods defined in IUniqueObject.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 */
public abstract class AUniqueObject
	implements IUniqueObject {
	/**
	 * Unique Id
	 */
	final protected int iUniqueID;

	/**
	 * Constructor.
	 * 
	 * @param iUniqueID
	 *            the unique ID generated by the system
	 * @param generalManager
	 *            the general manager
	 */
	protected AUniqueObject(final int iUniqueID) {
		this.iUniqueID = iUniqueID;
	}

	@Override
	public final int getID() {
		return this.iUniqueID;
	}

	/**
	 * Returns the internal unique-id as hashcode
	 * @return internal unique-id as hashcode
	 */
	@Override
	public int hashCode() {
		return getID();
	}
	
	/**
	 * Checks if the given object is equals to this one by comparing the internal unique-id
	 * @return <code>true</code> if the 2 objects are equal, <code>false</code> otherwise
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof IUniqueObject) {
			return this.getID() == ((IUniqueObject) other).getID();
		} else {
			return false;
		}
	}
}
