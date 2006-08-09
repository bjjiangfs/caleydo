package cerberus.view.gui;

import java.util.Vector;

public interface ViewInter
{
	public void initView();

	public void drawView();

	public void retrieveNewGUIContainer();

	public void retrieveExistingGUIContainer();
	
	public void setAttributes( Vector <String> attributes );
}