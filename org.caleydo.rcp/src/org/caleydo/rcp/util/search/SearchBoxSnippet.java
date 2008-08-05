package org.caleydo.rcp.util.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class SearchBoxSnippet
{

	public static void main(String[] args)
	{
		String items[] = { "Lions", "Tigers", "Bears", "Alpha", "Bravo", "Charlie", "Delta",
				"Echo", "Foxtrot", "Golf", "Hotel", "India", "Juliet", "Kilo", "Lima", "Mike",
				"November", "Oscar", "Papa", "Quebec", "Romeo", "Sierra", "Tango", "Uniform",
				"Victor", "Whiskey", "X-Ray", "Yankee", "Zulu" };
		Display display = Display.getDefault();
		Shell shell1 = new Shell(display);
		shell1.setLayout(new GridLayout());
		shell1.setText("SearchBox");
		shell1.setLocation(400, 150);
		Label l = new Label(shell1, SWT.NORMAL);
		l.setText("Click any key to open list ...");
		SearchBox sb = new SearchBox(shell1, SWT.NONE);
		sb.setItems(items);
		shell1.pack();
		shell1.open();
		while (!shell1.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}