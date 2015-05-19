package org.iviPro.editors.annotationeditor.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.EditorPart;
import org.iviPro.application.Application;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationContentType;
import org.iviPro.editors.annotationeditor.annotationfactory.AnnotationType;
import org.iviPro.editors.annotationeditor.components.positioneditors.OverlayEditor;
import org.iviPro.editors.common.BeanNameGenerator;
import org.iviPro.editors.common.ScreenAreaSelector;
import org.iviPro.editors.events.SivaEvent;
import org.iviPro.editors.events.SivaEventConsumerI;
import org.iviPro.editors.events.SivaEventType;
import org.iviPro.model.PictureGallery;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.ScreenArea;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.annotation.GlobalAnnotationSaveOperation;
import org.iviPro.theme.Icons;
import org.iviPro.utils.ImageHelper;

public class GlobalAnnotationDefineWidget extends AbstractAnnotationDefineWidget {

	public GlobalAnnotationDefineWidget(Composite parent, int style,
			final INodeAnnotation annotation, AnnotationType annotationType, CTabItem it, EditorPart editor) {
		super(parent, style, annotation, annotationType, it);		
		createContent();
	}

	/**
	 * erstellt den Content
	 */
	protected void createContent() {
		
		GridLayout adWLayout = new GridLayout(1, false);
		setLayout(adWLayout);

		// Composite für den oberen Bereich
		editorComposite = new Composite(this, SWT.TOP | SWT.BORDER);
		GridLayout leftLayout = new GridLayout(4, false);
		editorComposite.setLayout(leftLayout);
		GridData topGD = new GridData();
		topGD.widthHint = 640;
		topGD.verticalAlignment = SWT.TOP;
		editorComposite.setLayoutData(topGD);

		// Composite für den Inhaltseditorbereich
		contentEditorComposite = new Composite(this, SWT.TOP);
		GridData bottomGD = new GridData(SWT.CENTER, SWT.FILL, false, true);
		bottomGD.widthHint = 644;		
		contentEditorComposite.setLayoutData(bottomGD);
		GridLayout rightLayout = new GridLayout(1, false);
		rightLayout.marginWidth = 0;
		contentEditorComposite.setLayout(rightLayout);
		
		Composite saveNameComposite = new Composite(editorComposite, SWT.CENTER);
		saveNameComposite.setLayout(new GridLayout(1, false));
		
		// Button zum Abspeichern der Annotation
		final Button saveButton = new Button(saveNameComposite, SWT.CENTER);
		saveButton.setText(Messages.AnnotationDefineWidget_Text_SaveButton);
		
		// Bild für den Button
		ImageHelper.setButtonImage(saveButton, Icons.ACTION_ANNOTATION_SAVE);
		saveButton.setToolTipText(Messages.AnnotationDefineWidget_Tooltip_SaveButton);
		saveButton.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				executeSaveOperation();
			}
		});

		// Composite für den Namen
		Group titleGroup = new Group(saveNameComposite, SWT.CENTER);
		titleGroup.setLayout(new GridLayout(1, false));
		titleGroup.setText(Messages.AnnotationDefineWidget_Label_Identifier);
		tmpTitle = new Text(titleGroup, SWT.SINGLE | SWT.BORDER);
		tmpTitle.setText(annotation.getTitle());

		tmpTitle.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				// setze die Annotation auf dirty
				updateDirty();
			}
		});
		GridData titleGD = new GridData();
		titleGD.widthHint = 150;
		titleGD.heightHint = 15;
		tmpTitle.setLayoutData(titleGD);

		// Composite for description
		Group descriptionGroup = new Group(saveNameComposite, SWT.CENTER);
		descriptionGroup.setLayout(new GridLayout(1, false));
		descriptionGroup.setText(Messages.AnnotationDefineWidget_Label_Title);
		tmpDescription = new Text(descriptionGroup, SWT.SINGLE | SWT.BORDER);
		tmpDescription.setText(annotation.getTitle());

		tmpDescription.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				// setze die Annotation auf dirty
				updateDirty();
			}
		});
		GridData descriptionGD = new GridData();
		descriptionGD.widthHint = 150;
		descriptionGD.heightHint = 15;
		tmpDescription.setLayoutData(descriptionGD);

		// für Bilder auswahl zum Umschalten zwischen Bild und Bildergallerie
		if (annotation instanceof NodeAnnotationPicture) {
		    Group group = new Group(editorComposite, SWT.SHADOW_IN);
		    group.setLayout(new GridLayout(2, false));
		    group.setText(Messages.PictureOrPictureGalleryText);
		    picButton = new Button(group, SWT.RADIO);
		    picButton.setText(Messages.PictureButton);
		    galButton = new Button(group, SWT.RADIO);
		    galButton.setText(Messages.PictureGalleryButton);			
		    final Composite columnsComp = new Composite(group, SWT.CENTER);
		    GridData columnsCompGD = new GridData();
		    if (((NodeAnnotationPicture) annotation).getContentType() == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
		    	columnsCompGD.heightHint = 20;
		    }
		    columnsCompGD.horizontalSpan = 2;
		    columnsComp.setLayoutData(columnsCompGD);
		    columnsComp.setLayout(new GridLayout(2, false));
		    Label pictureAnnoColumnFieldLab = new Label(columnsComp, SWT.CENTER);
		    pictureAnnoColumnFieldLab.setText(Messages.NumberColumns);		    
		    pictureAnnoColumnField = new Text(columnsComp, SWT.SINGLE);
		    if (((NodeAnnotationPicture) annotation).getContentType() == NodeAnnotationPicture.CONTENT_PICTUREGALLERY) {
		    	pictureAnnoColumnField.setEnabled(true);
		    	int col = ((NodeAnnotationPicture) annotation).getPictureGallery().getNumberColumns();
				pictureAnnoColumnField.setText(Integer.toString(col));	
			} else {
		    	pictureAnnoColumnField.setEnabled(false);
		    	pictureAnnoColumnField.setText("1"); //$NON-NLS-1$
		    }
		    GridData gd = new GridData();
		    gd.widthHint = 40;
		    pictureAnnoColumnField.setLayoutData(gd);
		    pictureAnnoColumnField.addVerifyListener(new VerifyListener() {
				@Override
				public void verifyText(VerifyEvent ve) {
					String string = ve.text;
					char [] chars = new char [string.length ()];
					string.getChars (0, chars.length, chars, 0);
					for (int i=0; i<chars.length; i++) {
						if (!('0' <= chars [i] && chars [i] <= '9')) {
							ve.doit = false;
							return;
						}
					}					
				}
		    });		
		    pictureAnnoColumnField.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent arg0) {					
					if (tmpPicture != null && pictureAnnoColumnField != null) {
						if (pictureAnnoColumnField.getText().length() > 0) {
							Integer cols = Integer.parseInt(pictureAnnoColumnField.getText());
							tmpPicture.setColumns(cols);
						}					
					}
				}
		    	
		    });		  
		    
		    final MessageBox warnBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
		    warnBox.setMessage(Messages.PictureEditor_SwitchWarningMsg);
		    warnBox.setText(Messages.PictureEditor_SwitchWarningText);
		    
		    picButton.addMouseListener(new MouseListener() {

				@Override
				public void mouseDoubleClick(MouseEvent arg0) {
				}

				@Override
				public void mouseDown(MouseEvent arg0) {
					if (!picButton.getSelection()) {
						int bla = warnBox.open();
						if (bla == SWT.OK) {
							picButton.setSelection(true);
							galButton.setSelection(false);
							pictureAnnoColumnField.setEnabled(false);
							Display.getCurrent().asyncExec(new Runnable() {
								@Override
								public void run() {
									tmpPicture.setPicture(((NodeAnnotationPicture) annotation).getPicture());	
									pictureAnnoColumnField.setText("1"); //$NON-NLS-1$
									updateDirty();
								}
							});
						}
					}
				}

				@Override
				public void mouseUp(MouseEvent arg0) {
				}		    	
		    });
		    
		    galButton.addMouseListener(new MouseListener() {

				@Override
				public void mouseDoubleClick(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseDown(MouseEvent arg0) {
					if (!galButton.getSelection()) {
						int bla = warnBox.open();
						if (bla == SWT.OK) {							
							galButton.setSelection(true);
							picButton.setSelection(false);
							final PictureGallery picGal = ((NodeAnnotationPicture) annotation).getPictureGallery();
							pictureAnnoColumnField.setEnabled(true);
							pictureAnnoColumnField.setText("" + picGal.getNumberColumns()); //$NON-NLS-1$

							Display.getCurrent().asyncExec(new Runnable() {
								@Override
								public void run() {
									tmpPicture.setPictureGallery(picGal, picGal.getNumberColumns());
									updateDirty();
								}
								
							});
						}
					}
				}

				@Override
				public void mouseUp(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}
		    	
		    });
		}			
		
		// Screen Area wird für Audio/Subtitle Annotationen nicht benötigt
		if (!(annotationType.getContentType().equals(AnnotationContentType.SUBTITLE))) {
			
			Group contentPositionGroup = new Group(editorComposite, SWT.CENTER);
			contentPositionGroup.setLayout(new GridLayout(1, false));
			contentPositionGroup.setText(Messages.AnnotationDefineWidget_Label_Content_Position);				
			
			// setze die Screen Area entsprechend der Annotation
			if (annotation.getScreenArea() != null) {
				tmpScreenArea = annotation.getScreenArea();			
			}		
			tmpOpItems = annotation.getOverlayPath();
			areaSelector = new ScreenAreaSelector(contentPositionGroup, SWT.CENTER, tmpScreenArea);
			areaSelector.addSivaEventConsumer(new SivaEventConsumerI() {
				@Override
				public void handleEvent(SivaEvent event) {
					if (event.getEventType().equals(SivaEventType.SCREEN_AREA_CHANGED)) {
						tmpScreenArea = (ScreenArea) event.getValue();
						if (tmpScreenArea.equals(ScreenArea.OVERLAY)) {
							if (checkContentSet()) {
								final OverlayEditor opEditor = new OverlayEditor(tmpOpItems, editorContent);
								opEditor.addListener(SWT.Close, new Listener() {
									@Override
									public void handleEvent(Event event) {
										tmpOpItems = opEditor.getOverlayPathItems();
										updateDirty();
									}									
								});
								opEditor.open();
								setFocus();
							}
						}	
					}
					updateDirty();
				}			
			});
		}		
		
		// Gruppe für Mute und Pause, wird ausgeblendet wenn beides nicht verwendet wird
		Group groupPauseMute = new Group(editorComposite, SWT.LEFT);
		groupPauseMute.setLayout(new GridLayout(1, false));
		groupPauseMute.setText(Messages.AnnotationDefineWidget_Label_Toggles);
	    boolean groupPauseMuteIsEmpty = true;
		
		// Mute Button für Audio Annotationen
		if (annotationType.getContentType().equals(AnnotationContentType.AUDIO) || 
			annotationType.getContentType().equals(AnnotationContentType.VIDEO)) {
			
			Composite muteContainer = new Composite(groupPauseMute, SWT.CENTER);
			GridLayout muteContainerGL = new GridLayout(2, false);
			muteContainerGL.marginWidth = 0;
			muteContainer.setLayout(muteContainerGL);
			Label muteVideoLabel = new Label(muteContainer, SWT.CENTER);
			muteVideoLabel.setText(Messages.AnnotationDefineWidget_Label_MuteVideo);
			muteVideoButton = new Button(muteContainer, SWT.CENTER | SWT.CHECK);				
			muteVideoButton.setSelection(annotation.isMuteVideo());			
			muteVideoButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					updateDirty();
				}				
			});
			groupPauseMuteIsEmpty = false;
		}
		
		if (groupPauseMuteIsEmpty) {
			groupPauseMute.setVisible(false);
		}
		
		annotation.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (isDisposed()) {
					return;
				}
				// man kann hier alles gleichzeitig setzen und muss nicht auf einzelne 
				// Properties prüfen, aber jedes Property dass geändert wird muss auch
				// dein Event feuern (in der entspr. Modelklasse) z.B. INodeAnnotation
				if (annotation != null) {					
					tmpTitle.setText(annotation.getTitle());				
					
					if (!(annotationType.getContentType().equals(AnnotationContentType.SUBTITLE))) {
						
						if (!areaSelector.isDisposed()) {
							tmpScreenArea = annotation.getScreenArea();
							areaSelector.setScreenArea(tmpScreenArea);						
						}
						tmpOpItems = annotation.getOverlayPath();
					} 	
					
					if (muteVideoButton != null) {
						muteVideoButton.setSelection(annotation.isMuteVideo());
					}
					
					if (!tabItem.isDisposed()) {
						tabItem.setText(annotation.getTitle());
					}
					
					if (event.getPropertyName().equals(INodeAnnotation.PROP_SETCONTENT)) {
						initContentEditors();
					}
				}
			}			
		});
		
		// entferne die Listener wieder wenn das Widget disposed ist
		addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event event) {
			}
		});

		// initialisiere die konkrete Annotation
		initContentEditors();
	}
	
	@Override
	public boolean executeSaveOperation() {
		// prüfe ob der Inhalt gesetzt wurde
		if (!checkContentSet()) {
			return false;
		}
				
		// generiere den Namen
		BeanNameGenerator nameGen = new BeanNameGenerator(tmpTitle.getText(), annotation, Application.getCurrentProject().getGlobalAnnotations(), Messages.GlobalAnnotationPrefix);
		String newTitle = nameGen.generate();
		// falls die Namensgenerierung abgebrochen wurde, wird nicht gespeichert
		if (!nameGen.getCancelState()) {
			tmpTitle.setText(newTitle);	
			if (isDirty()) {
				boolean mute = false;
				if (muteVideoButton != null) {
					mute = muteVideoButton.getSelection();
				}
				IAbstractOperation op = new GlobalAnnotationSaveOperation(annotation, 
						annotationType, tmpTitle.getText(), tmpDescription.getText(), 
					Messages.GlobalAnnotationDefineWidget_4, mute, editorContent, tmpScreenArea, tmpOpItems);
				try {
					OperationHistory.execute(op);
					tabItem.setText(annotation.getTitle());
				} catch (ExecutionException e) {
					e.printStackTrace();
					return false;
				}
			}	
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean localIsDirty() {
		// Die Annotation ist auf jeden Fall Dirty falls sie noch nicht gespeichert wurde
		if (!Application.getCurrentProject().getGlobalAnnotations().contains(annotation)) {
			return true;			
		}
		return false;
	}
	

}
