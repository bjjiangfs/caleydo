package org.caleydo.testing.unit.command.data.filter;
import org.caleydo.core.command.CommandType;
import org.caleydo.core.command.data.filter.CmdDataFilterMinMax;
import org.caleydo.core.data.collection.IStorage;
import org.caleydo.core.data.collection.EStorageType;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.testing.unit.testing_util.CoreStarter;


import junit.framework.TestCase;

/**
 * @author Alexander Lex
 *
 */
public class CmdDataFiterMinMaxTest extends TestCase {

	
	private CoreStarter myCoreStarter;
	private IGeneralManager myGeneralManager;

	/**
	 * Starting a Caleydo Core
	 */
	protected void setUp() throws Exception {
		super.setUp();
		myCoreStarter = new CoreStarter();
		myCoreStarter.startCaleydoCore("");
		myGeneralManager = myCoreStarter.getGeneralManager();

	}

	/**
	 * Deleting a Caleydo Core
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		myCoreStarter.disposeCaleydoCore();
		
	}	

	public void testForFloat() 
	{		
		
		IStorage myStorage = myGeneralManager.getStorageManager().getStorage(46301);
		
		CmdDataFilterMinMax createdCmd = (CmdDataFilterMinMax) myGeneralManager.getCommandManager().createCommandByType(CommandType.DATA_FILTER_MIN_MAX);
		
		createdCmd.setAttributes(myStorage, EStorageType.FLOAT);
		
		createdCmd.doCommand();		
		
		assertEquals(1524.0f, createdCmd.getFMinValue(), 0.001f);
		assertEquals(56131.0f, createdCmd.getFMaxValue(), 0.001f);
	}
	
	public void testForInt()
	{
		IStorage myStorage = myGeneralManager.getStorageManager().getStorage(45301);
		
		CmdDataFilterMinMax createdCmd = (CmdDataFilterMinMax) myGeneralManager.getCommandManager().createCommandByType(CommandType.DATA_FILTER_MIN_MAX);
		
		createdCmd.setAttributes(myStorage, EStorageType.INT);
		
		createdCmd.doCommand();		
		
		assertEquals(618, createdCmd.getIMinValue());
		assertEquals(56432, createdCmd.getIMaxValue());
		
	}
	
	public void testForDouble()
	{
		IStorage myStorage = myGeneralManager.getStorageManager().getStorage(47301);
		
		CmdDataFilterMinMax createdCmd = (CmdDataFilterMinMax) myGeneralManager.getCommandManager().createCommandByType(CommandType.DATA_FILTER_MIN_MAX);
		
		createdCmd.setAttributes(myStorage, StorageType.EStorageType);
		
		createdCmd.doCommand();		
		
		assertEquals(3626.0, createdCmd.getDMinValue(), 0.001);
		assertEquals(54981.0, createdCmd.getDMaxValue(), 0.001);
	}
	

}
