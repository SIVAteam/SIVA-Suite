package org.iviPro.editors.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationFactory;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeAnnotationAudio;
import org.iviPro.model.graph.NodeAnnotationPdf;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationSubtitle;
import org.iviPro.model.graph.NodeAnnotationVideo;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.RichText;
import org.iviPro.model.resources.Scene;
import org.iviPro.model.resources.Subtitle;

public class BeanNameGenerator {
	
	// Die Bean für die ein Name generiert werden soll
	private IAbstractBean bean;
	
	// die Liste der bereits existierenden Beans
	private BeanList<IAbstractBean> compareList;
	
	// der aktuell eingegebene Title
	private String title;
	
	private boolean canceled = false;
	private boolean ok = false;
	
	// der in der Shell eingegebene Text
	private String shellText;
	
	// zusätzliches prefix bei neuen Namen
	private String prefix;

	/**
	 * generiert einen Namen für das Bean
	 * @param title
	 * @param bean
	 * @param beanList
	 */
	@SuppressWarnings("unchecked")
	public BeanNameGenerator(String newTitle, IAbstractBean bean, BeanList beanList, String prefix) {
		this.title = newTitle;
		this.bean = bean;
		this.compareList = beanList;
		this.prefix = prefix;
	}
	
	public String generate() {
		return generateName(title, true);
	}
	
	public String generateAuto() {
		return generateName(title, false);
	}
	
	public boolean getCancelState() {
		return canceled;
	}
	
	/**
	 * generiert den neuen Namen
	 * @param interactive gibt an ob es eine Userrückmeldung geben soll
	 * @return der neue Name
	 */
	private String generateName(String newTitle, boolean interactive) {
		boolean generated = false;
		while (!generated && !canceled) {	
			ok = false;
			if (existsName(newTitle)) {
				// generiere einen möglichen Namen
				// falls das Bean bereits einen Title hat ist dieser nach wie vor gültig	
				if (bean != null && bean.getTitle().length() > 0) {
					newTitle = bean.getTitle();
				} else {
					newTitle = getPossibleName(newTitle);
				}
				if (interactive) {
					// Nachrichtenbox, dass ein Name vorgeschlagen wird und 
					// der neue bereits existiert
					Display display = Display.getCurrent();
					final Shell shell = new Shell(display.getActiveShell(), SWT.TITLE | SWT.PRIMARY_MODAL);
					shell.setSize(400, 200);
					
					Rectangle bounds = display.getActiveShell().getBounds();
					int x = bounds.x + (bounds.width - 400) / 2;
				    int y = bounds.y + (bounds.height - 200) / 2;
				    shell.setLocation(x, y);
					GridLayout shellLayout = new GridLayout(1, false);
					shell.setLayout(shellLayout);
					shell.setText(Messages.BeanNameGenerator_ShellLabel);
					Label messageLabel = new Label(shell, SWT.BORDER| SWT.WRAP);				
					messageLabel.setText(Messages.BeanComparator_ShellMessage);
					GridData messageLabelGD = new GridData(380, 50);
					messageLabel.setLayoutData(messageLabelGD);
					
					new Label(shell, SWT.NONE).setText(Messages.BeanNameGenerator_GeneratedNameLabel);
					final Text message = new Text(shell, SWT.BORDER);
					message.setText(newTitle);
					message.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					Composite buttonComp = new Composite(shell, SWT.CENTER);
					GridLayout buttonCompGL = new GridLayout(2, false);
					buttonComp.setLayout(buttonCompGL);
					Button okButton = new Button(buttonComp, SWT.PUSH);
					okButton.setText(Messages.BeanNameGenerator_OK_BUTTON);
					okButton.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetDefaultSelected(SelectionEvent arg0) {
							widgetSelected(arg0);
						}
	
						@Override
						public void widgetSelected(SelectionEvent arg0) {
							ok = true;
							shell.close();
						}					
					});
					Button cancelButton = new Button(buttonComp, SWT.PUSH);
					cancelButton.setText(Messages.BeanNameGenerator_CANCEL_BUTTON);
					cancelButton.addSelectionListener(new SelectionListener() {
						@Override
						public void widgetDefaultSelected(SelectionEvent arg0) {
							widgetSelected(arg0);
						}
	
						@Override
						public void widgetSelected(SelectionEvent arg0) {
							canceled = true;
							shell.close();
						}					
					});		
					shell.addListener(SWT.Close, new Listener() {
						@Override
						public void handleEvent(Event arg0) {
							shellText = message.getText();
						}					
					});
					shell.open();
					while (!shell.isDisposed()) {
						if (!display.readAndDispatch()) {
							display.sleep();
						}
					}
					
					// falls ok geklickt wurde, wird der Text auf den Text 
					// der Textbox gesetzt (falls sich nochmal was geändert hat)
					if (ok) {
						newTitle = shellText;
					}
				}
			} else {
				generated = true;
			}
		}
		return newTitle;
	}
	
	/**
	 * erstellt einen möglichen Namen für das Bean
	 * @return möglicher Name
	 */
	private String getPossibleName(String newTitle) {
		int number = 1;
		// falls der neue Name leer ist, setze die Prefixes
		if (newTitle.length() == 0) {
			// Scene
			if (bean instanceof Scene) {
				Scene scene = (Scene) bean;
				newTitle = prefix + scene.getVideo().getTitle(); // + Messages.BeanNameGenerator_ScenePostfix;
			} else		
			// Annotation
			if (bean instanceof INodeAnnotation) {
				newTitle = prefix + generateAnnotationPrefix((INodeAnnotation) bean);
			} else
			// Richtext Filename
			if (bean instanceof RichText) {
				newTitle = prefix + Messages.BeanNameGenerator_RichtextFilePostfix;
			} else
			// Audio Filename
			if (bean instanceof AudioPart) {
				newTitle = prefix + ((AudioPart) bean).getAudio().getTitle(); // + Messages.BeanNameGenerator_AudioPartPostfix;
			} else
			// Subtitle
			if (bean instanceof Subtitle) {
				newTitle = prefix;
			} else
			// Picture Gallery
			if (bean instanceof PictureGallery) {
				newTitle = prefix + Messages.BeanNameGenerator_GalleryFilePostfix;
			} else {
				return Messages.BeanNameGenerator_UnknownAnnotationTitle;
			}
				
		}
		// bestimme den möglichen Namen
		boolean found = false;
		String posName = newTitle;
		while (!found) {
			int oldnumber = number;
			for (IAbstractBean bean : compareList) {
				// falls der Name gleich ist erhöhe die Nummer
				if (bean.getTitle().equals(posName)) {
					number++;
					posName = newTitle + "-" + number; //$NON-NLS-1$
					break;
				}
			}

			// falls sich die Nummer nicht weiter erhöht ist der Name gültig
			if (oldnumber == number) {
				newTitle = posName;
				found = true;
			}
		}
		return newTitle;
	}
	
	/**
	 * prüft ob der Name für das Bean bereits existiert
	 * @param title
	 * @return true, falls ja
	 */
	private boolean existsName(String title) {
		// einen leeren Namen erlauben wird nicht
		if (title.isEmpty()) {
			return true;
		}
		// falls der neue Name dem alten entspricht
		// erlauben wir das 
		if (title.equals(bean.getTitle())) {
			return false;
		}
		for (IAbstractBean bean : compareList) {
			if (bean.getTitle().equals(title)) {
				return true;
			}
		}
		return false;
	}	
	
	private String generateAnnotationPrefix(INodeAnnotation annotation) {
			String genPrefix = ""; //$NON-NLS-1$
			
			INodeAnnotation contentAnnotation = AnnotationFactory.getContentAnnotation(annotation);
									
			if (contentAnnotation instanceof NodeAnnotationAudio) {
				genPrefix = Messages.AudioPrefix;
			}
			if (contentAnnotation instanceof NodeAnnotationSubtitle) {
				genPrefix = Messages.SubtitlePrefix;
			}
			if (contentAnnotation instanceof NodeAnnotationRichtext) {
				genPrefix = Messages.RichtextPrefix;
			}
			if (contentAnnotation instanceof NodeAnnotationVideo) {
				genPrefix = Messages.VideoPrefix;
			}
			if (contentAnnotation instanceof NodeAnnotationPicture) {
				genPrefix = Messages.PicturePrefix;
			}
			if (contentAnnotation instanceof NodeAnnotationPdf) {
				genPrefix = "Pdf";
			}
			if (prefix.length() != 0) {
				return "-" + genPrefix; //$NON-NLS-1$
			}
		return genPrefix;
	}
}
