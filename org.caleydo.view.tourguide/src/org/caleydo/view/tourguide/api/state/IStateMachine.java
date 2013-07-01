/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.tourguide.api.state;

import java.util.Collection;
import java.util.Set;

import org.caleydo.view.tourguide.api.query.EDataDomainQueryMode;

/**
 * @author Samuel Gratzl
 *
 */
public interface IStateMachine {
	// common well defined states within this state machine
	String ADD_PATHWAY = EDataDomainQueryMode.PATHWAYS.name();
	String ADD_OTHER = EDataDomainQueryMode.OTHER.name();
	String ADD_STRATIFICATIONS = EDataDomainQueryMode.STRATIFICATIONS.name();
	String BROWSE_PATHWAY = EDataDomainQueryMode.PATHWAYS.name() + "_browse";
	String BROWSE_OTHER = EDataDomainQueryMode.OTHER.name() + "_browse";
	String BROWSE_STRATIFICATIONS = EDataDomainQueryMode.STRATIFICATIONS.name() + "_browse";

	/**
	 * adds another state to this state machine
	 * 
	 * @param id
	 *            unique id for this state to refer it using {@link #get(String)}
	 * @param state
	 * @return
	 */
	IState addState(String id, IState state);

	/**
	 * adds an outgoing transition of the given state
	 * 
	 * @param source
	 * @param transition
	 */
	void addTransition(IState source, ITransition transition);

	IState get(String id);

	/**
	 * list of all registered states
	 * 
	 * @return
	 */
	Set<String> getStates();

	/**
	 * returns the current state
	 * 
	 * @return
	 */
	IState getCurrent();

	/**
	 * returns all outgoing transitions of a given state
	 * 
	 * @param state
	 * @return
	 */
	Collection<ITransition> getTransitions(IState state);
}
