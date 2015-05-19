package org.iviPro.editors.events;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.swt.widgets.Display;

/**
 * Standard Implementation des Siva Event Provider Interface
 * @author juhoffma
 *
 */
public class SivaEventProvider implements SivaEventProviderI {
	
	private LinkedList<SivaEventConsumerI> consumers = new LinkedList<SivaEventConsumerI>();

	@Override
	public void addSivaEventConsumer(SivaEventConsumerI consumer) {
		consumers.add(consumer);
	}

	@Override
	public void notifySivaEventConsumers(final SivaEvent event) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				Iterator<SivaEventConsumerI> it = consumers.iterator();
				while(it.hasNext()) {
					it.next().handleEvent(event);
				}					
			}					
		});
	}

	@Override
	public void removeSivaEventConsumer(SivaEventConsumerI consumer) {
		consumers.remove(consumer);
	}

	@Override
	public void forwardEvent(SivaEvent event) {
		// TODO Auto-generated method stub
		
	}

}
