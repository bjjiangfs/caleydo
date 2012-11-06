package org.caleydo.data.importer.tcga.qualitycontrol;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveAction;

import org.caleydo.core.manager.GeneralManager;
import org.caleydo.data.importer.tcga.EDataSetType;
import org.caleydo.data.importer.tcga.utils.IOUtils;

public class TCGAQCDataSetTypeTask extends RecursiveAction {

	private final EDataSetType datasetType;
	private TCGAQCSettings settings;

	public TCGAQCDataSetTypeTask(EDataSetType datasetType, TCGAQCSettings settings) {
		this.datasetType = datasetType;
		this.settings = settings;
	}

	@Override
	public void compute() {
		Collection<TCGAQCTask> tasks = new ArrayList<>();
		for (String tumorType : settings.getTumorTypes()) {
			tasks.add(new TCGAQCTask(tumorType, datasetType, settings));
		}
		invokeAll(tasks);

		StringBuilder b = new StringBuilder();
		for (TCGAQCTask task : tasks) {
			try {
				b.append(task.get()).append("\n,");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (b.length() != 0) // remove last ,
			b.setLength(b.length() - 1);
		generateJSONReport(b, datasetType, settings.getDataDirectory(datasetType.name()));
	}

	private void generateJSONReport(StringBuilder report, EDataSetType dataSetType, String dataSetTypeSpecificOutputPath) {
		String tmp = report.toString().replace("\"null\"", "null");

		tmp = "{\"analysisRun\":\"" + dataSetType + "\",\"details\":[" + tmp + "],\"caleydoVersion\":\""
				+ GeneralManager.VERSION + "\"}\n";

		IOUtils.dumpToFile(tmp, new File(dataSetTypeSpecificOutputPath, dataSetType + ".json"));
	}
}
