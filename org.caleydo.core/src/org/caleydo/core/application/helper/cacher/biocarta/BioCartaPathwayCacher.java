package org.caleydo.core.application.helper.cacher.biocarta;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import org.caleydo.core.application.helper.PathwayListGenerator;
import org.caleydo.core.application.helper.cacher.APathwayCacher;
import org.caleydo.core.command.system.CmdFetchPathwayData;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import de.phleisch.app.itsucks.constants.ApplicationConstants;
import de.phleisch.app.itsucks.core.Dispatcher;
import de.phleisch.app.itsucks.event.Event;
import de.phleisch.app.itsucks.event.EventObserver;
import de.phleisch.app.itsucks.filter.download.impl.DownloadJobFilter;
import de.phleisch.app.itsucks.filter.download.impl.RegExpJobFilter;
import de.phleisch.app.itsucks.filter.download.impl.RegExpJobFilter.RegExpFilterAction;
import de.phleisch.app.itsucks.filter.download.impl.RegExpJobFilter.RegExpFilterRule;
import de.phleisch.app.itsucks.job.Job;
import de.phleisch.app.itsucks.job.download.impl.DownloadJobFactory;
import de.phleisch.app.itsucks.job.download.impl.UrlDownloadJob;
import de.phleisch.app.itsucks.job.event.JobChangedEvent;

/**
 * Fetch tool for BioCarta HTML and image files.
 * 
 * @author Marc Streit
 * 
 */
public class BioCartaPathwayCacher
	extends APathwayCacher
{	
	/**
	 * Constructor.
	 */
	public BioCartaPathwayCacher(final Display display, final ProgressBar progressBar,
			final CmdFetchPathwayData triggeringCommand)
	{
		this.display = display;
		this.progressBar = progressBar;
		this.triggeringCommand = triggeringCommand;
		
		iExpectedDownloads = 891;
	}

	@Override
	public void run()
	{
		super.run();

		// load spring application context
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				ApplicationConstants.CORE_SPRING_CONFIG_FILE);

		// load dispatcher from spring
		Dispatcher dispatcher = (Dispatcher) context.getBean("Dispatcher");

		// configure an download job filter
		DownloadJobFilter downloadFilter = new DownloadJobFilter();
		downloadFilter.setAllowedHostNames(new String[] { "cgap.*" });
		downloadFilter.setMaxRecursionDepth(2);
		downloadFilter.setSaveToDisk(new String[] { ".*BioCarta/h_.*", ".*h_.*gif" });

		// add the filter to the dispatcher
		dispatcher.addJobFilter(downloadFilter);

		RegExpJobFilter regExpFilter = new RegExpJobFilter();
		RegExpFilterRule regExpFilterRule = new RegExpJobFilter.RegExpFilterRule(
				".*Gene.*|.*m_.*|.*Kegg.*,.*Tissues.*|.*SAGE.*");

		RegExpFilterAction regExpFilterAction = new RegExpJobFilter.RegExpFilterAction();
		regExpFilterAction.setAccept(false);

		regExpFilterRule.setMatchAction(regExpFilterAction);

		regExpFilter.addFilterRule(regExpFilterRule);

		dispatcher.addJobFilter(regExpFilter);

		// create an job factory
		DownloadJobFactory jobFactory = (DownloadJobFactory) context.getBean("JobFactory");

		String sOutputFileName = System.getProperty("user.home")
				+ System.getProperty("file.separator") + "/.caleydo";

		// create an initial job
		UrlDownloadJob job = jobFactory.createDownloadJob();

		try
		{
			job.setUrl(new URL("http://cgap.nci.nih.gov/Pathways/BioCarta_Pathways"));
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}

		job.setSavePath(new File(sOutputFileName));
		job.setIgnoreFilter(true);

		dispatcher.addJob(job);
		
		processJobs(dispatcher);

		triggerPathwayListGeneration();

		if (triggeringCommand != null)
			triggeringCommand.setFinishedBioCartaCacher();
	}

	protected void triggerPathwayListGeneration()
	{
		// Trigger pathway list generation
		PathwayListGenerator pathwayListLoader = new PathwayListGenerator();

		try
		{
			pathwayListLoader.run(PathwayListGenerator.INPUT_FOLDER_PATH_BIOCARTA,
					PathwayListGenerator.INPUT_IMAGE_PATH_BIOCARTA,
					PathwayListGenerator.OUTPUT_FILE_NAME_BIOCARTA);
		}
		catch (FileNotFoundException fnfe)
		{
			throw new RuntimeException("Cannot generate BioCarta pathway list.");
		}
	}
}
