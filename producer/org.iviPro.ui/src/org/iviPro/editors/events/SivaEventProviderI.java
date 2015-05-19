package org.iviPro.editors.events;

// Provider der Änderungen mitteilt. Wird für die Annotationseditoren verwendet
// um ein AnnotationDefineWidget über Änderungen im Editor zu informieren
public interface SivaEventProviderI {

	public void addSivaEventConsumer(SivaEventConsumerI consumer);	
	public void removeSivaEventConsumer(SivaEventConsumerI consumer);
	public void notifySivaEventConsumers(SivaEvent event);	
	public void forwardEvent(SivaEvent event);
}
