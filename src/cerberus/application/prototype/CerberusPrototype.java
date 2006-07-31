package cerberus.application.prototype;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cerberus.manager.gui.SWTGUIManagerSimple;
import cerberus.manager.singelton.OneForAllManager;
import cerberus.manager.view.ViewManagerSimple;
import cerberus.data.loader.MicroArrayLoader;
import cerberus.manager.type.ManagerObjectType;
import cerberus.xml.parser.kgml.KgmlSaxHandler;

public class CerberusPrototype 
{
	public static void main(String[] args) 
	{
		String sRawDataFileName = "data/MicroarrayData/slides30.gpr";
		OneForAllManager oneForAllManager = new OneForAllManager(null);
		
		//loading the raw data
		MicroArrayLoader microArrayLoader = 
			new MicroArrayLoader(oneForAllManager.getGeneralManager(), sRawDataFileName);
		microArrayLoader.loadData();
		
		//load the pathway data
		KgmlSaxHandler kgmlParser = new KgmlSaxHandler();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try
		{
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new File("data/XML/pathways/map00271.xml"),
					kgmlParser);

		} catch (Throwable t)
		{
			t.printStackTrace();
		}
		
		SWTGUIManagerSimple swtGuiManager = (SWTGUIManagerSimple) oneForAllManager.getManagerByBaseType(ManagerObjectType.GUI_SWT);
		
		ViewManagerSimple viewManager = (ViewManagerSimple) oneForAllManager.getManagerByBaseType(ManagerObjectType.VIEW);
		viewManager.createView(ManagerObjectType.PATHWAY_VIEW);
		viewManager.createView(ManagerObjectType.TABLE_VIEW);
		viewManager.createView(ManagerObjectType.GEARS_VIEW);

		swtGuiManager.runApplication();
	
	}
}
