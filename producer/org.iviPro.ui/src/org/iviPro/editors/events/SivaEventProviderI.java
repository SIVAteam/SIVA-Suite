package org.iviPro.editors.events;

// Provider der �nderungen mitteilt. Wird f�r die Annotationseditoren verwendet
// um ein AnnotationDefineWidget �ber �nderungen im Editor zu informieren
public interface SivaEventProviderI {

	public void addSivaEventConsumer(SivaEventConsumerI consumer);	
	public void removeSivaEventConsumer(SivaEventConsumerI consumer);
	public void notifySivaEventConsumers(SivaEvent event);	
	public void forwardEvent(SivaEvent event);
}
