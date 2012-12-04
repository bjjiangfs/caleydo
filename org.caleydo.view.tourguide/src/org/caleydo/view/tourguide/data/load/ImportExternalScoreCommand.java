package org.caleydo.view.tourguide.data.load;

import java.util.Collection;
import java.util.Collections;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.view.tourguide.data.Scores;
import org.caleydo.view.tourguide.data.load.ui.ImportExternalGroupLabelScoreDialog;
import org.caleydo.view.tourguide.data.load.ui.ImportExternalIDTypeScoreDialog;
import org.caleydo.view.tourguide.data.score.ExternalGroupLabelScore;
import org.caleydo.view.tourguide.data.score.ExternalIDTypeScore;
import org.caleydo.view.tourguide.data.score.IScore;
import org.caleydo.view.tourguide.data.serialize.ISerializeableScore;
import org.caleydo.view.tourguide.event.AddScoreColumnEvent;
import org.caleydo.view.tourguide.vendingmachine.ScoreQueryUI;
import org.eclipse.swt.widgets.Shell;

public final class ImportExternalScoreCommand implements Runnable {
	private final ATableBasedDataDomain dataDomain;

	private final ScoreQueryUI sender;
	private final boolean inDimensionDirection;
	private final Class<? extends ISerializeableScore> type;


	public ImportExternalScoreCommand(ATableBasedDataDomain dataDomain, boolean dimensionDirection,
			Class<? extends ISerializeableScore> type, ScoreQueryUI scoreQueryUI) {
		this.dataDomain = dataDomain;
		this.inDimensionDirection = dimensionDirection;
		this.type = type;
		this.sender = scoreQueryUI;
	}

	@Override
	public void run() {
		Collection<ISerializeableScore> scores;
		if (type == ExternalIDTypeScore.class)
			scores = importExternalIDScore();
		else if (type == ExternalGroupLabelScore.class)
			scores = importExternalLabelGroup();
		else
			return; // unknown type

		final Scores scoreManager = Scores.get();

		IScore last = null;
		for (ISerializeableScore score : scores) {
			last = scoreManager.addPersistentScoreIfAbsent(score);
		}
		if (last != null) // add the last newly created one to the list
			GeneralManager.get().getEventPublisher().triggerEvent(new AddScoreColumnEvent(last, sender));
	}

	private Collection<ISerializeableScore> importExternalIDScore() {
		IDCategory category = inDimensionDirection ? dataDomain.getDimensionIDCategory() : dataDomain
				.getRecordIDCategory();
		ScoreParseSpecification spec = new ImportExternalIDTypeScoreDialog(new Shell(), category).call();
		if (spec == null)
			return Collections.emptyList();
		IDType target = inDimensionDirection ? dataDomain.getDimensionIDType() : dataDomain.getRecordIDType();

		Collection<ISerializeableScore> scores = new ExternalIDTypeScoreParser(spec, target).call();
		return scores;
	}

	private Collection<ISerializeableScore> importExternalLabelGroup() {
		GroupLabelParseSpecification spec = new ImportExternalGroupLabelScoreDialog(new Shell(), dataDomain,
				inDimensionDirection).call();
		if (spec == null)
			return Collections.emptyList();
		Collection<ISerializeableScore> scores = new ExternalGroupLabelScoreParser(spec).call();
		return scores;
	}

}