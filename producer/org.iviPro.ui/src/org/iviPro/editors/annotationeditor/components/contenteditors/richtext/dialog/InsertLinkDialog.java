package org.iviPro.editors.annotationeditor.components.contenteditors.richtext.dialog;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.iviPro.theme.Icons;

public class InsertLinkDialog extends JDialog {

	public static final String LINK_PREFIX = "http://";

	private String input;
	private JButton btnOk;
	private final JTextField txtSrc = new JTextField(LINK_PREFIX);

	public InsertLinkDialog(Frame parent) {
		super(parent, Messages.LinkDialog_Title);
		setLayout(new GridBagLayout());
		setSize(new Dimension(400, 150));
		// fensert in der mitte des bildschirms plazieren
		setLocationRelativeTo(null);
		// fenster icon setzen
		setIconImage(Icons.OBJECT_MEDIA_TEXT_PLAIN.getImageIcon().getImage());
		// komponenten erzeugen
		createControlPane();
	}

	/**
	 * Erzeugt die Controll-Elemente zum Einfuegen und Abbrechen.
	 */
	private void createControlPane() {
		// control position
		GridBagConstraints c = new GridBagConstraints();

		// erstellt ein eingabefeld fuer die laenge des bildes
		JLabel lblSrc = new JLabel(Messages.LinkDialog_InputLabel);

		// button zum einfuegen einer grafik inzufuegen
		btnOk = new JButton(Messages.LinkDialog_OK);
		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				input = txtSrc.getText();
				setVisible(false);
			}
		});
		// button zum schlieszen des dialog feldes
		JButton btnCancle = new JButton(Messages.LinkDialog_Cancel);
		btnCancle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.weighty = 1.0;
		c.gridy = 0;
		c.gridx = 0;
		this.add(lblSrc, c);
		c.weightx = 1.0;
		c.gridx = 1;
		this.add(txtSrc, c);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_START;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridy = 1;
		c.gridx = 0;
		this.add(btnOk, c);
		c.gridx = 1;
		this.add(btnCancle, c);
	}

	public String getInput() {
		return txtSrc.getText();
	}

	public void addOkListener(ActionListener al) {
		btnOk.addActionListener(al);
	}

}
