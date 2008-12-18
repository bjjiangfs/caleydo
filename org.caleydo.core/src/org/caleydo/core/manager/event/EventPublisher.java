package org.caleydo.core.manager.event;

import java.util.Collection;
import java.util.HashMap;
import org.caleydo.core.data.IUniqueObject;
import org.caleydo.core.data.selection.ISelectionDelta;
import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.manager.AManager;
import org.caleydo.core.manager.IEventPublisher;
import org.caleydo.core.manager.event.mediator.EMediatorType;
import org.caleydo.core.manager.event.mediator.IMediator;
import org.caleydo.core.manager.event.mediator.IMediatorReceiver;
import org.caleydo.core.manager.event.mediator.IMediatorSender;
import org.caleydo.core.manager.event.mediator.Mediator;

/**
 * Implements event mediator pattern.
 * 
 * @author Marc Streit
 * @author Alexander Lex
 */
public class EventPublisher
	extends AManager<IMediator>
	implements IEventPublisher
{
	private HashMap<EMediatorType, IMediator> hashMediatorType2Mediator;

	/**
	 * Constructor.
	 * 
	 */
	public EventPublisher()
	{
		hashMediatorType2Mediator = new HashMap<EMediatorType, IMediator>();
	}

	@Override
	public void addSender(EMediatorType eMediatorType, IMediatorSender sender)
	{
		// Lazy mediator creation
		if (!hashMediatorType2Mediator.containsKey(eMediatorType))
			hashMediatorType2Mediator.put(eMediatorType, new Mediator(eMediatorType));

		hashMediatorType2Mediator.get(eMediatorType).register(sender);

	}

	@Override
	public void addReceiver(EMediatorType eMediatorType, IMediatorReceiver receiver)
	{
		// Lazy mediator creation
		if (!hashMediatorType2Mediator.containsKey(eMediatorType))
			hashMediatorType2Mediator.put(eMediatorType, new Mediator(eMediatorType));

		hashMediatorType2Mediator.get(eMediatorType).register(receiver);

	}

	@Override
	public void triggerUpdate(EMediatorType eMediatorType, IUniqueObject eventTrigger,
			ISelectionDelta selectionDelta, Collection<SelectionCommand> colSelectionCommand)
	{
		if (!hashMediatorType2Mediator.containsKey(eMediatorType))
		{
			throw new IllegalStateException("Sender " + eventTrigger.getID()
					+ "is not a sender in the mediator group " + eMediatorType);
		}
		hashMediatorType2Mediator.get(eMediatorType).triggerUpdate(eventTrigger,
				selectionDelta, colSelectionCommand);
	}

	@Override
	public void removeSender(EMediatorType eMediatorType, IMediatorSender sender)
	{
		if (!hashMediatorType2Mediator.containsKey(eMediatorType))
			return;

		hashMediatorType2Mediator.get(eMediatorType).unregister(sender);
	}

	@Override
	public void removeReceiver(EMediatorType eMediatorType, IMediatorReceiver receiver)
	{
		if (!hashMediatorType2Mediator.containsKey(eMediatorType))
			return;

		hashMediatorType2Mediator.get(eMediatorType).unregister(receiver);
	}

	public void removeSenderFromAllGroups(IMediatorSender sender)
	{
		for (IMediator mediator : hashMediatorType2Mediator.values())
		{
			mediator.unregister(sender);
		}
	}

	public void removeReceiverFromAllGroups(IMediatorReceiver receiver)
	{
		for (IMediator mediator : hashMediatorType2Mediator.values())
		{
			mediator.unregister(receiver);
		}
	}
}
