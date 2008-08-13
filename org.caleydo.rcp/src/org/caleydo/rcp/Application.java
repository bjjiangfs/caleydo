package org.caleydo.rcp;

import java.util.Map;

import org.caleydo.core.application.core.CaleydoBootloader;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.rcp.wizard.firststart.FirstStartWizard;
import org.caleydo.rcp.wizard.project.CaleydoProjectWizard;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class Application
	implements IApplication
{
	public static CaleydoBootloader caleydoCore;

	/**
	 * Getter method for the Caleydo general manager. Use this reference to get
	 * access to all specialized managers.
	 */
	public static IGeneralManager generalManager;

	public static ApplicationWorkbenchAdvisor applicationWorkbenchAdvisor;

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.
	 * IApplicationContext)
	 */
	@SuppressWarnings("unchecked")
	public Object start(IApplicationContext context) throws Exception
	{
		System.out.println("Caleydo RCP: bootstrapping ...");

		String sCaleydoXMLfile = "";
		Map<String, Object> map = (Map<String, Object>) context.getArguments();

		if (map.size() > 0)
		{
			String[] info = (String[]) map.get("application.args");

			if (info != null)
			{
				if (info.length > 0)
				{
					sCaleydoXMLfile = info[0];
					System.out.println("XML config file:" + sCaleydoXMLfile);

					if (info.length > 1)
					{
						System.err
								.println("Caleydo cannot handle more than on argument! ignor other argumets.");
					}
				}
			}
		}
		
		// Create Caleydo core
		caleydoCore = new CaleydoBootloader(false);
		generalManager = caleydoCore.getGeneralManager();
		
		Display display = PlatformUI.createDisplay();
		
		// Check if Caleydo will be started the first time
		if (caleydoCore.getGeneralManager().getPreferenceStore().getBoolean("firstStart"))
		{
			WizardDialog firstStartWizard = new WizardDialog(display.getActiveShell(), 
					new FirstStartWizard());
			firstStartWizard.open();
		}
		
//		startCaleydoCore(sCaleydoXMLfile);
		
		try
		{
			applicationWorkbenchAdvisor = new ApplicationWorkbenchAdvisor();

			int returnCode = PlatformUI.createAndRunWorkbench(display,
					applicationWorkbenchAdvisor);
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IApplication.EXIT_RESTART;
			else
				return IApplication.EXIT_OK;
		}
		finally
		{
			disposeCaleydoCore();
			display.dispose();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop()
	{

		final IWorkbench workbench = PlatformUI.getWorkbench();

		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable()
		{
			public void run()
			{
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}

	public void startCaleydoCore(final String sXmlFileName)
	{
		// If no file is provided as command line argument a XML file open
		// dialog is opened
		if (sXmlFileName == "")
		{
			Display display = PlatformUI.createDisplay();
			Shell shell = new Shell(display);
			shell.setText("Open project file");

			WizardDialog projectWizardDialog = new WizardDialog(shell,
					new CaleydoProjectWizard());
			projectWizardDialog.open();

			// FileOpenProjectAction openProjectAction = new
			// FileOpenProjectAction(shell);
			// openProjectAction.run();

			shell.dispose();
		}
		// Load as command line argument provided XML config file name.
		else
		{
			caleydoCore.setXmlFileName(sXmlFileName);
			caleydoCore.start();
		}
	}

	protected void disposeCaleydoCore()
	{

		if (caleydoCore != null)
		{
			if (caleydoCore.isRunning())
			{
				caleydoCore.stop();
				caleydoCore = null;
			}
		}
	}
}
