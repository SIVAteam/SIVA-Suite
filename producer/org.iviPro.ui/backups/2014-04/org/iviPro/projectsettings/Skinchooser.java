package org.iviPro.projectsettings;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.iviPro.model.ProjectSettings;

public class Skinchooser extends Composite {
	
	public int skin = ProjectSettings.SKIN_SIMPLE;
	private static final String SIMPLE = Messages.Skinchooser_SimpleButton; 
	private static final String DARK = Messages.Skinchooser_DarkButton; 
	private static final String WHITE = Messages.Skinchooser_WhiteButton; 
	private Button skin1;
	private Button skin2;
	private Button skin3;
	public Skinchooser(Composite parent, int style) {
		super(parent, style);

		ClassLoader loader = Skinchooser.class.getClassLoader();
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginLeft = 10;
		gridLayout.marginTop = 10;
		gridLayout.marginRight = 30;
		setLayout(gridLayout);		
		
		GridData layoutdata = new GridData();
		skin1 = new Button(this, SWT.RADIO);
		skin1.setText(SIMPLE);
		Label label1 = new Label(this, SWT.BORDER);
		URL location1 = loader.getResource("org/iviPro/projectsettings/style1.png"); //$NON-NLS-1$
		ImageDescriptor descriptor1 = ImageDescriptor.createFromURL(location1);
		Image image1 = descriptor1.createImage();
		label1.setBackgroundImage(image1);
		layoutdata.widthHint = image1.getBounds().width;
		layoutdata.heightHint = image1.getBounds().height;
		label1.setLayoutData(layoutdata);
		
		skin2 = new Button(this, SWT.RADIO);
		skin2.setText(DARK);
		Label label2 = new Label(this, SWT.BORDER);
		URL location2 = loader.getResource("org/iviPro/projectsettings/style2.png"); //$NON-NLS-1$
		ImageDescriptor descriptor2 = ImageDescriptor.createFromURL(location2);
		Image image2 = descriptor2.createImage();
		label2.setBackgroundImage(image2);
		layoutdata.widthHint = image2.getBounds().width;
		layoutdata.heightHint = image2.getBounds().height;
		label2.setLayoutData(layoutdata);
//		skin3 = new Button(this, SWT.RADIO);
//		skin3.setText(WHITE);
//		Label label3 = new Label(this, SWT.BORDER);
//		label3.setSize(100, 100);
//		label3.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
//		label3.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Listener selList = new Listener() {
			@Override
			public void handleEvent(Event e) {
				String choosen = ((Button) e.widget).getText();
				if (choosen.equals(SIMPLE)) {
					skin = ProjectSettings.SKIN_SIMPLE;
				} else if (choosen.equals(DARK)) {
					skin = ProjectSettings.SKIN_DARK;
				} else if (choosen.equals(WHITE)) {
					skin = ProjectSettings.SKIN_WHITE;
				}
				notifyListeners(SWT.Modify, new Event());
			}
			
		};
		skin1.addListener(SWT.Selection, selList);
		skin2.addListener(SWT.Selection, selList);
//		skin3.addListener(SWT.Selection, selList);
	}
	
	public void init() {
		skin = ProjectSettings.SKIN_SIMPLE;
		skin2.setSelection(false);
//		skin3.setSelection(false);
		setField();
		notifyListeners(SWT.Modify, new Event());
	}
	
	public void setField() {
		if (skin == ProjectSettings.SKIN_SIMPLE) {
			skin1.setSelection(true);
		} else if (skin == ProjectSettings.SKIN_DARK) {
			skin2.setSelection(true);
		} else if (skin == ProjectSettings.SKIN_WHITE) {
			skin3.setSelection(true);
		}
	}

}
