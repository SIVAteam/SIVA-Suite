package org.iviPro.projectsettings;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class Startmode extends Composite {

	public boolean fullscreen;
	private Button fButton;
	private Button wButton;
	public Startmode(Composite parent, int style) {
		super(parent, style);
		
		ClassLoader loader = Startmode.class.getClassLoader();
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginLeft = 10;
		gridLayout.marginTop = 10;
		gridLayout.marginRight = 30;
		setLayout(gridLayout);
		fButton = new Button(this, SWT.RADIO);
		fButton.setText(Messages.Startmode_FullscreenButton);
		fButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Startmode.this.fullscreen = true;
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		Label fDesc = new Label(this, SWT.None);
		fDesc.setText(Messages.Startmode_Description_Fullscreen);
		
		// Bild Fullscreen
		Label label1 = new Label(this, SWT.BORDER);
		URL location1 = loader.getResource("org/iviPro/projectsettings/mode_fullscreen.png"); //$NON-NLS-1$
		ImageDescriptor descriptor1 = ImageDescriptor.createFromURL(location1);
		Image image1 = descriptor1.createImage();
		label1.setBackgroundImage(image1);
		GridData layoutdata = new GridData();
		layoutdata.widthHint = image1.getBounds().width;
		layoutdata.heightHint = image1.getBounds().height;
		layoutdata.horizontalSpan = 2;
		label1.setLayoutData(layoutdata);	
		
		wButton = new Button(this, SWT.RADIO);
		wButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Startmode.this.fullscreen = false;
				
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		wButton.setText(Messages.Startmode_WindowedButton);

		SelectionListener selList = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				notifyListeners(SWT.Modify, new Event());
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		};
		Label wDesc = new Label(this, SWT.None);
		//wDesc.setBackground(Display.getCurrent().getSystemColor(SWT.color_));
		wDesc.setText(Messages.Startmode_Descripton_Windowed);
		fButton.addSelectionListener(selList);
		wButton.addSelectionListener(selList);
		
		// Bild Windowed
		Label label2 = new Label(this, SWT.BORDER);
		URL location2 = loader.getResource("org/iviPro/projectsettings/mode_window.png"); //$NON-NLS-1$
		ImageDescriptor descriptor2 = ImageDescriptor.createFromURL(location2);
		Image image2 = descriptor2.createImage();
		label2.setBackgroundImage(image2);
		layoutdata = new GridData();
		layoutdata.widthHint = image2.getBounds().width;
		layoutdata.heightHint = image2.getBounds().height;
		layoutdata.horizontalSpan = 2;
		label2.setLayoutData(layoutdata);
		
	}
	
	public void init() {
		fButton.setSelection(true);
		wButton.setSelection(false);
		fullscreen = true;
		notifyListeners(SWT.Modify, new Event());
	}
	
	public void setField() {
		if (fullscreen == true) {
			fButton.setSelection(true);
		} else {
			wButton.setSelection(true);
		}
	}

}
