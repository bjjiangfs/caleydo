package org.caleydo.core.data.collection.virtualarray.iterator;


/**
 * Types of selections.
 * 
 * @author Michael Kalkusch
 *
 * @see prometheus.data.set.SelectionInterface
 */
public enum MultiVirtualArrayIterationType 
{

	FIRST_TO_LAST_SUCCESSIVE("Abstract IVirtualArray, that has not been instaniated"),
	FIRST_COLUMN_EACH_ROW("Abstract Virtual Array, that has not been defined"),
	FIRST_ROW_EACH_COLUMN("Virtual Array of a single block"),
	NONE("No type set");

	/**
	 * Brief description, what the IVirtualArray does.
	 */
	private final String sDescription;
	
	/**
	 * Constructor for the Enumeration.
	 * 
	 * @param sSetDescription
	 */
	private MultiVirtualArrayIterationType(String sSetDescription) {
		sDescription = sSetDescription;
	}
	
	public String getDescription() {
		return sDescription;
	}
}
