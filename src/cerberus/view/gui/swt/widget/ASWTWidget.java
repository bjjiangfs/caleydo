/*
 * Project: GenView
 * 
 * Author: Marc Streit
 * 
 * Creation date: 26-07-2006
 *  
 */
package cerberus.view.gui.swt.widget;

import org.eclipse.swt.widgets.Composite;

import cerberus.data.IUniqueObject;
import cerberus.view.gui.swt.ISWTWidget;

abstract public class ASWTWidget 
implements IUniqueObject, ISWTWidget
{
	/**
	 * Composite in which the content of the View should be placed.
	 */
	protected final Composite refParentComposite;
	
	protected int iUniqueId;
	
	/**
	 * Constructor that takes the composite in which it should 
	 * place the content.
	 * 
	 * @param refComposite Reference to the composite 
	 * that is supposed to be filled.
	 */
	protected ASWTWidget(Composite refParentComposite)
	{
		this.refParentComposite = refParentComposite;
	}
	
	public void setId(int iUniqueId)
	{
		this.iUniqueId = iUniqueId;
	}

	public int getId()
	{
		return iUniqueId;
	}
}
