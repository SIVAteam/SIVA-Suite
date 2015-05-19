package org.iviPro.editors.common;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.swt.widgets.Composite;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventProviderI;

/**
 * Siva Composite wird für die Editorkomponenten verwendet, erweitert ein normales
 * Composite um Siva Consumer/Listener Funktionalität
 * @author juhoffma
 *
 */
public class SivaComposite extends Composite implements SivaEventProviderI {
	
	private LinkedList<SivaEventConsumerI> consumers = new LinkedList<SivaEventConsumerI>();
	
	public SivaComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public void addSivaEventConsumer(SivaEventConsumerI consumer) {
		consumers.add(consumer);
	}

	@Override
	public void notifySivaEventConsumers(SivaEvent event) {
		Iterator<SivaEventConsumerI> it = consumers.iterator();
		while(it.hasNext()) {
			it.next().handleEvent(event);
		}
	}

	@Override
	public void removeSivaEventConsumer(SivaEventConsumerI consumer) {
		consumers.remove(consumer);
	}

	@Override
	public void forwardEvent(SivaEvent event) {
		notifySivaEventConsumers(event);
	}
}
