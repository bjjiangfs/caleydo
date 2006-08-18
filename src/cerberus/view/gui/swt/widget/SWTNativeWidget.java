/*
 * Project: GenView
 * 
 * Author: Marc Streit
 * 
 * Creation date: 26-07-2006
 *  
 */

package cerberus.view.gui.swt.widget;

import cerberus.view.gui.swt.widget.ASWTWidget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;


/**
 * Class takes a composite in the constructor
 * and provides this composite over the getSWTWidget()
 * method.
 * 
 * @author Marc Streit
 */
public class SWTNativeWidget extends ASWTWidget 
{
	protected Composite refComposite;
	
	/**
	 * Constructor 
	 * 
	 * @param refComposite Reference to the composite 
	 * that is supposed to be filled.
	 */
	public SWTNativeWidget(Composite refParentComposite, int iWidth, int iHeight)
	{
		super(refParentComposite);
		
		refComposite = new Composite(refParentComposite, SWT.NONE);
		
		GridData gridData = new GridData();
		
		if (iWidth != -1)
		{
			gridData.widthHint = iWidth;
			gridData.horizontalAlignment = GridData.CENTER;
		}
		else
		{
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
		}
		
		if (iHeight != -1)
		{
			gridData.heightHint = iHeight;
			gridData.verticalAlignment = GridData.CENTER;
		}
		else
		{
			gridData.verticalAlignment = GridData.FILL;
			gridData.grabExcessVerticalSpace = true;
		}
		
		refComposite.setLayoutData(gridData);
	}

	/**
	 * Get the SWT container in which the View can place the content.
	 * 
	 * @return SWT container
	 */
	final public Composite getSWTWidget()
	{
		return refComposite;
	}
}
