/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.view.tourguide.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.view.tourguide.api.compute.ComputeScoreFilters;
import org.caleydo.view.tourguide.api.query.EDataDomainQueryMode;
import org.caleydo.view.tourguide.api.score.DefaultComputedReferenceGroupScore;
import org.caleydo.view.tourguide.api.score.MultiScore;
import org.caleydo.view.tourguide.api.score.ui.ACreateGroupScoreDialog;
import org.caleydo.view.tourguide.api.state.BrowseStratificationState;
import org.caleydo.view.tourguide.api.state.EWizardMode;
import org.caleydo.view.tourguide.api.state.IReactions;
import org.caleydo.view.tourguide.api.state.ISelectGroupState;
import org.caleydo.view.tourguide.api.state.IState;
import org.caleydo.view.tourguide.api.state.IStateMachine;
import org.caleydo.view.tourguide.api.state.SimpleState;
import org.caleydo.view.tourguide.api.state.SimpleTransition;
import org.caleydo.view.tourguide.impl.algorithm.JaccardIndex;
import org.caleydo.view.tourguide.spi.IScoreFactory;
import org.caleydo.view.tourguide.spi.score.IRegisteredScore;
import org.caleydo.view.tourguide.spi.score.IScore;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Samuel Gratzl
 *
 */
public class JaccardIndexScoreFactory implements IScoreFactory {
	private final static Color color = Color.decode("#ffb380");
	private final static Color bgColor = Color.decode("#ffe6d5");

	private IRegisteredScore createJaccard(String label, Perspective reference, Group group) {
		return new DefaultComputedReferenceGroupScore(label, reference, group, JaccardIndex.get(), null, color, bgColor);
	}

	private IRegisteredScore createJaccardME(String label, Perspective reference, Group group) {
		return new DefaultComputedReferenceGroupScore(label, reference, group, JaccardIndex.get(),
				ComputeScoreFilters.MUTUAL_EXCLUSIVE, color, bgColor);
	}

	@Override
	public void fillStateMachine(IStateMachine stateMachine, List<TablePerspective> existing, EWizardMode mode,
			TablePerspective source) {
		if (mode != EWizardMode.GLOBAL || existing.isEmpty()) // nothing to compare
			return;

		IState start = stateMachine.get(IStateMachine.ADD_STRATIFICATIONS);
		IState browse = stateMachine.addState("JaccardIndexBrowse", new UpdateAndBrowseJaccardIndex());
		IState target = stateMachine.addState("JaccardIndex", new CreateJaccardScoreState(browse));
		stateMachine.addTransition(start, new SimpleTransition(target, "Find large overlap with displayed clusters"));
	}

	private void createJaccardScore(TablePerspective tablePerspective, Group group, IReactions reactions) {
		String label = String.format("Sim. to %s %s %s", tablePerspective.getDataDomain().getLabel(), tablePerspective
				.getRecordPerspective().getLabel(), group.getLabel());
		reactions.addScoreToTourGuide(EDataDomainQueryMode.STRATIFICATIONS,
				createJaccardME(label, tablePerspective.getRecordPerspective(), group));
	}

	private class CreateJaccardScoreState extends SimpleState implements ISelectGroupState {
		private final IState target;

		public CreateJaccardScoreState(IState target) {
			super("Select query group by clicking on a brick in one of the displayed columns\n"
					+ "Change query by clicking on other brick at any time");
			this.target = target;
		}

		@Override
		public boolean apply(Pair<TablePerspective, Group> pair) {
			return true;
		}

		@Override
		public void select(TablePerspective tablePerspective, Group group, IReactions reactions) {
			createJaccardScore(tablePerspective, group, reactions);
			reactions.switchTo(target);
		}
	}

	private class UpdateAndBrowseJaccardIndex extends BrowseStratificationState implements ISelectGroupState {
		public UpdateAndBrowseJaccardIndex() {
			super("Select a stratification in the Tour Guide to preview.\n" + "Then confirm or cancel your selection"
					+ "Change query by clicking on other brick at any time");
		}

		@Override
		public boolean apply(Pair<TablePerspective, Group> pair) {
			return true;
		}

		@Override
		public void select(TablePerspective tablePerspective, Group group, IReactions reactions) {
			createJaccardScore(tablePerspective, group, reactions);
		}
	}

	@Override
	public Iterable<ScoreEntry> createGroupEntries(TablePerspective strat, Group group) {
		Collection<ScoreEntry> col = new ArrayList<>();
		col.add(new ScoreEntry("Score group", (IScore) createJaccard(null, strat.getRecordPerspective(), group)));
		col.add(new ScoreEntry("Score group  (Mutual Exclusive)", (IScore) createJaccardME(null,
				strat.getRecordPerspective(), group)));
		return col;
	}

	@Override
	public Iterable<ScoreEntry> createStratEntries(TablePerspective strat) {
		Collection<ScoreEntry> col = new ArrayList<>();
		final Perspective rs = strat.getRecordPerspective();
		MultiScore composite = new MultiScore(rs.getLabel(), color, bgColor);
		for (Group group : rs.getVirtualArray().getGroupList()) {
			composite.add(createJaccard(null, rs, group));
		}
		col.add(new ScoreEntry("Score all groups in column", (IScore) composite));
		composite = new MultiScore(rs.getLabel(), color, bgColor);
		for (Group group : rs.getVirtualArray().getGroupList()) {
			composite.add(createJaccardME(null, rs, group));
		}
		col.add(new ScoreEntry("Score all groups in column (Mutual Exclusive)", (IScore) composite));
		return col;
	}

	@Override
	public boolean supports(EDataDomainQueryMode mode) {
		return mode == EDataDomainQueryMode.STRATIFICATIONS;
	}

	@Override
	public Dialog createCreateDialog(Shell shell, Object sender) {
		return new CreateJaccardIndexScoreDialog(shell, sender);
	}

	class CreateJaccardIndexScoreDialog extends ACreateGroupScoreDialog {
		private Button mututalExclusiveUI;

		public CreateJaccardIndexScoreDialog(Shell shell, Object sender) {
			super(shell, sender);
		}

		@Override
		protected String getLabel() {
			return "Jaccard Index Score";
		}

		@Override
		protected void addTypeSpecific(Composite c) {
			Label l = new Label(c, SWT.NONE);
			l.setText("");
			mututalExclusiveUI = new Button(c, SWT.CHECK);
			mututalExclusiveUI.setText("Mutual Exclusive");
		}

		@Override
		protected IRegisteredScore createScore(String label, Perspective per, Group g) {
			boolean m = mututalExclusiveUI.getSelection();
			if (m)
				return createJaccardME(label, per, g);
			else
				return createJaccard(label, per, g);
		}
	}
}

