/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.core.util.base;

/**
 * Abstract class providing methods defined in IUniqueObject.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 * @author Alexander Lex
 */
public abstract class AUniqueObject
	implements IUniqueObject {

	/** Unique Id */
	protected int uniqueID;

	/**
	 * default no-arg constructor
	 */
	public AUniqueObject() {
	}

	/**
	 * Constructor.
	 * 
	 * @param uniqueID
	 *            the unique ID generated by the system
	 * @param generalManager
	 *            the general manager
	 */
	protected AUniqueObject(final int uniqueID) {
		this.uniqueID = uniqueID;
	}

	@Override
	public final int getID() {
		return this.uniqueID;
	}

	/**
	 * Returns the internal unique-id as hash code
	 * 
	 * @return internal unique-id as hash code
	 */
	@Override
	public int hashCode() {
		return getID();
	}

	/**
	 * Checks if the given object is equals to this one by comparing the internal unique-id
	 * 
	 * @return <code>true</code> if the 2 objects are equal, <code>false</code> otherwise
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof IUniqueObject) {
			return this.getID() == ((IUniqueObject) other).getID();
		}
		else {
			return false;
		}
	}
}
