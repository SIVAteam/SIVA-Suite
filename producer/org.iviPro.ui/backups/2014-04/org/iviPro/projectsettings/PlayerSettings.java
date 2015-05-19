package org.iviPro.projectsettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PlayerSettings extends Composite {

	private static final String DEFAULT_FONTCOLOR = "004080"; //$NON-NLS-1$
	private static final String DEFAULT_BORDERCOLOR = "0000FF"; //$NON-NLS-1$
	private static final String DEFAULT_BACKGOUNDCOLOR = "0080FF"; //$NON-NLS-1$
	private static final String DEFAULT_PRIMARYCOLOR = "808080"; //$NON-NLS-1$
	private Group playerBehaviour;
	private Group primaryColor;
	private Group ownDesign;
	private Group design;
	private Group schema;
	private Group colorAdjustment;
	private Group textFont;
	private Color color;
	private GridData colorLabelGD;
	private GridData rgbTextGD;
	private GridData designformGD;
	private GridData labelGD;

	final Shell shell;
	public Button save;
	public String designName;
	public String designPath;
	public String designSchema;
	public String colorSchema;
	public String backgroundColor;
	public String borderColor;
	public String fontColor;
	public String font;
	public String fontSize;
	private Combo designSchemaCombo;
	private Combo colorSchemaCombo;
	public Combo designChooser;
	private Combo fontCombo;
	private Combo fontSizeCombo;
	private Label backgroundColorChooser;
	private Label borderColorChooser;
	private Label fontColorChooser;
	private Text rgbBackgroundColor;
	private Text rgbBorderColor;
	private Text rgbTextColor;
	private Label hash;
	private int preDesCount = 0;
	private String[][] cSchemes = new String[2][3];
	private Composite desComp;
	private boolean playerDesignsField = false;
	private boolean predefColorSchema;
	private boolean setToDefColorSchema;

	private Button autoPlayButton;
	private Button primaryColorButton;
	private Button notPrimaryColorButton;
	private Label primaryColorChooser;
	private Text rgbPrimaryColorChooser;
	public boolean autoPlay;
	public String primaryColorValue;
	public boolean isPrimaryColor;

	public PlayerSettings(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(2, false));

		desComp = new Composite(this, SWT.NONE);
		desComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		desComp.setLayout(new GridLayout(1, false));
		shell = new Shell(desComp.getDisplay());
		createLayoutData();

		autoPlay = true;
		predefColorSchema = true;
		setToDefColorSchema = false;
		isPrimaryColor = true;
		addColorToColorSchema();
		createPlayerBehaviourField();
		createPrimaryColorField();
		createOwnDesignField();
		addListener();

		playerDesignsField = false;
	}

	private void createPlayerBehaviourField() {
		playerBehaviour = new Group(desComp, SWT.CENTER);
		playerBehaviour
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		playerBehaviour.setLayout(new GridLayout(2, false));
		
		autoPlayButton = new Button(playerBehaviour, SWT.CHECK);
		autoPlayButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				autoPlay = autoPlayButton.getSelection();
				notifyListeners(SWT.Modify, new Event());
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		autoPlayButton.setText(Messages.PlayerSettings_behaviour_autoPlay);		
	}

	private void createOwnDesignField() {
		ownDesign = new Group(desComp, SWT.CENTER);
		ownDesign
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		ownDesign.setLayout(new GridLayout(1, true));

		createRadioButton();
		createDesignNameField();
		createDesignSchemaField();
		createColorSchemaField();
		createColorChooserField();
		createFontField();
		createFontSizeField();
	}

	private void createFontSizeField() {
		Label fontSize = new Label(textFont, SWT.NONE);
		fontSize.setLayoutData(labelGD);
		fontSize.setText(Messages.PlayerSettings_textFont_fontSize);

		fontSizeCombo = new Combo(textFont, SWT.DROP_DOWN);
		fontSizeCombo.addFocusListener(comboFocusListener(fontSizeCombo));
		fontSizeCombo.setLayoutData(designformGD);
		for (int i = 6; i <= 16; ++i) {
			fontSizeCombo.add("" + i); //$NON-NLS-1$
		}
	}

	private void createFontField() {
		textFont = new Group(ownDesign, SWT.CENTER);
		textFont.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		textFont.setLayout(new GridLayout(2, false));

		GridData fontGD = new GridData();
		fontGD.widthHint = 85;

		Label font = new Label(textFont, SWT.NONE);
		font.setLayoutData(labelGD);
		font.setText(Messages.PlayerSettings_textFont_font);

		fontCombo = new Combo(textFont, SWT.DROP_DOWN);
		fontCombo.setLayoutData(designformGD);
		fontCombo.add("Arial"); //$NON-NLS-1$
		fontCombo.add("Comic Sans MS"); //$NON-NLS-1$
		fontCombo.add("Courier"); //$NON-NLS-1$
		fontCombo.add("Courier New"); //$NON-NLS-1$
		fontCombo.add("Georgia"); //$NON-NLS-1$
		fontCombo.add("Tahoma"); //$NON-NLS-1$
		fontCombo.add("Times New Roman"); //$NON-NLS-1$ 
		fontCombo.add("Verdana"); //$NON-NLS-1$
		fontCombo.addFocusListener(comboFocusListener(fontCombo));
	}

	private void createColorChooserField() {
		colorAdjustment = new Group(ownDesign, SWT.CENTER);
		colorAdjustment.setText(Messages.PlayerSettings_colorAdjustment);
		colorAdjustment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		GridLayout gridlayout = new GridLayout(4, false);
		gridlayout.marginLeft = 90;
		colorAdjustment.setLayout(gridlayout);
		color = new Color(this.getDisplay(), new RGB(0, 0, 0));

		Label backgroundColor = new Label(colorAdjustment, SWT.NONE);
		hash = new Label(colorAdjustment, SWT.NONE);
		hash.setText("#"); //$NON-NLS-1$
		rgbBackgroundColor = new Text(colorAdjustment, SWT.BORDER);
		backgroundColorChooser = new Label(colorAdjustment, SWT.NONE);
		backgroundColor
				.setText(Messages.PlayerSettings_colorAdjustment_backgroundColor);
		createColorChooserHelp(rgbBackgroundColor, backgroundColorChooser);

		Label borderColor = new Label(colorAdjustment, SWT.NONE);
		hash = new Label(colorAdjustment, SWT.NONE);
		hash.setText("#"); //$NON-NLS-1$
		rgbBorderColor = new Text(colorAdjustment, SWT.BORDER);
		borderColorChooser = new Label(colorAdjustment, SWT.NONE);
		borderColor.setText(Messages.PlayerSettings_colorAdjustment_borderColor);
		createColorChooserHelp(rgbBorderColor, borderColorChooser);

		Label fontColor = new Label(colorAdjustment, SWT.NONE);
		hash = new Label(colorAdjustment, SWT.NONE);
		hash.setText("#"); //$NON-NLS-1$
		rgbTextColor = new Text(colorAdjustment, SWT.BORDER);
		fontColorChooser = new Label(colorAdjustment, SWT.NONE);
		fontColor.setText(Messages.PlayerSettings_colorAdjustment_textColor);
		createColorChooserHelp(rgbTextColor, fontColorChooser);
	}

	private void createColorChooserHelp(Text textField, Label colorLabel) {
		textField.setLayoutData(rgbTextGD);
		textField.setTextLimit(6);

		colorLabel.setLayoutData(colorLabelGD);
		colorLabel.setText(" "); //$NON-NLS-1$
		colorLabel.setBackground(color);
		colorLabel.addMouseListener(colorMouseListener(colorLabel, textField));
		textField.addFocusListener(colorFocusListener(textField, colorLabel));
	}

	private void createColorSchemaField() {
		Label colorSchema = new Label(schema, SWT.NONE);
		colorSchema.setLayoutData(labelGD);
		colorSchema.setText(Messages.PlayerSettings_schema_colorSchema);

		colorSchemaCombo = new Combo(schema, SWT.DROP_DOWN);
		colorSchemaCombo.setLayoutData(designformGD);
		colorSchemaCombo.add(Messages.PlayerSettings_schema_colorSchema_blue);
		colorSchemaCombo.add(Messages.PlayerSettings_schema_colorSchema_red);
		colorSchemaCombo.add(Messages.PlayerSettings_schema_colorSchema_user);
		colorSchemaCombo.addFocusListener(comboFocusListener(colorSchemaCombo));
		colorSchemaCombo.addSelectionListener(new SelectionListener() {

			/*
			 * preDefColorschema zeigt an das das ein vordefiniertes Schema ist,
			 * und sich der Wert der Farb Schema Combo nicht ändern muss weitere
			 * vordefinierte farbschema mit neuen case anweisungen einbinden und
			 * die indexe des String[][] arrays korrekt setzen. Erster index
			 * beinhaltet die Nummer des Schemas, der andere index bezeichnet
			 * backgroundcolor, bordercolor und textcolor
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				switch (colorSchemaCombo.getSelectionIndex()) {
				case 0: {
					rgbBackgroundColor.setText(new String(cSchemes[0][0]));
					predefColorSchema = true;
					rgbBackgroundColor.notifyListeners(SWT.FocusOut,
							new Event());

					rgbBorderColor.setText(new String(cSchemes[0][1]));
					predefColorSchema = true;
					rgbBorderColor.notifyListeners(SWT.FocusOut, new Event());

					rgbTextColor.setText(new String(cSchemes[0][2]));
					predefColorSchema = true;
					setToDefColorSchema = true;
					rgbTextColor.notifyListeners(SWT.FocusOut, new Event());
					break;
				}
				case 1: {
					rgbBackgroundColor.setText(new String(cSchemes[1][0]));
					predefColorSchema = true;
					rgbBackgroundColor.notifyListeners(SWT.FocusOut,
							new Event());

					rgbBorderColor.setText(new String(cSchemes[1][1]));
					predefColorSchema = true;
					rgbBorderColor.notifyListeners(SWT.FocusOut, new Event());

					rgbTextColor.setText(new String(cSchemes[1][2]));
					predefColorSchema = true;
					setToDefColorSchema = true;
					rgbTextColor.notifyListeners(SWT.FocusOut, new Event());
					break;
				}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void createDesignSchemaField() {
		schema = new Group(ownDesign, SWT.CENTER);
		schema.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		schema.setLayout(new GridLayout(2, false));
		Label formSchema = new Label(schema, SWT.NONE);
		formSchema.setLayoutData(labelGD);
		formSchema.setText(Messages.PlayerSettings_schema_formSchema);

		designSchemaCombo = new Combo(schema, SWT.DROP_DOWN);
		designSchemaCombo.setLayoutData(designformGD);
		designSchemaCombo.add(Messages.PlayerSettings_schema_designSchema_round);
		designSchemaCombo.add(Messages.PlayerSettings_schema_designSchema_angled);
		designSchemaCombo
				.addFocusListener(comboFocusListener(designSchemaCombo));
	}

	private void createRadioButton() {
		notPrimaryColorButton = new Button(ownDesign, SWT.RADIO);
		notPrimaryColorButton
				.setText(Messages.PlayerSettings_schema_ownDesignLabel);
		notPrimaryColorButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				isPrimaryColor = false;
				primaryColorButton.setSelection(false);
				enableOwnDesign(true);
				notifyListeners(SWT.Modify, new Event());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void createDesignNameField() {
		design = new Group(ownDesign, SWT.CENTER);
		design.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		design.setLayout(new GridLayout(3, false));

		/*
		 * Bei Auswahl des Designs, die jeweilige XML datei importieren.
		 */
		Label designLabel = new Label(design, SWT.NONE);
		designLabel.setLayoutData(labelGD);
		designLabel.setText(Messages.PlayerSettings_skin_skinLabel);
		designChooser = new Combo(design, SWT.DROP_DOWN);
		designChooser.setLayoutData(designformGD);
		designChooser.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!playerDesignsField) {
					if (designChooser.getSelectionIndex() <= preDesCount) {
						designPath = "C:\\Users\\" + System.getProperty("user.name") + "\\SIVA-Suite\\Player Designs\\Predefined Designs" + designChooser.getText() + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					} else {
						designPath = "C:\\Users\\" + System.getProperty("user.name") + "\\SIVA-Suite\\Player Designs\\User Designs" + designChooser.getText() + ".xml"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					}
					xmlImport(designChooser.getText(),
							designChooser.getSelectionIndex());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		/*
		 * FileDialog zum wählen des Namens
		 */
		save = new Button(design, SWT.PUSH);
		GridData saveGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		save.setLayoutData(saveGD);
		save.setText(Messages.PlayerSettings_skin_SaveButton);
		save.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				/*
				 * eigener Filedialog zum namen wählen, out of order wegen focus
				 * bug. Shell fileDialog = new Shell(desComp.getDisplay());
				 * fileDialog.setText("File Dialog");
				 * createFileDialog(fileDialog);
				 * 
				 * fileDialog.pack(); fileDialog.open();
				 * 
				 * while (!fileDialog.isDisposed()) {
				 * if(!desComp.getDisplay().readAndDispatch()) {
				 * desComp.getDisplay().sleep(); } }
				 */
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setFilterPath("C:\\Users\\" + System.getProperty("user.name") + "\\SIVA-Suite\\Player Designs\\User Designs"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				dialog.setFilterExtensions(new String[] { "*.xml*" }); //$NON-NLS-1$
				dialog.setFileName(designChooser.getText());
				String path = dialog.open();
				String fileName = dialog.getFileName();
				if (!fileName.isEmpty()) {
					xmlExport(fileName, false);
					designPath = "C:\\Users\\" //$NON-NLS-1$
							+ System.getProperty("user.name") //$NON-NLS-1$
							+ "\\SIVA-Suite\\Player Designs\\User Designs\\" //$NON-NLS-1$
							+ fileName;
					setDesignNames();
					designChooser.setText(fileName);

				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	private void createPrimaryColorField() {
		primaryColor = new Group(desComp, SWT.CENTER);
		primaryColor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		primaryColor.setLayout(new GridLayout(4, false));

		primaryColorButton = new Button(primaryColor, SWT.RADIO);
		primaryColorButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				isPrimaryColor = true;
				notPrimaryColorButton.setSelection(false);
				enableOwnDesign(false);
				notifyListeners(SWT.Modify, new Event());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		primaryColorButton
				.setText(Messages.PlayerSettings_schema_primaryColorLabel);

		hash = new Label(primaryColor, SWT.NONE);
		hash.setText("#"); //$NON-NLS-1$

		rgbPrimaryColorChooser = new Text(primaryColor, SWT.BORDER);
		rgbPrimaryColorChooser.setLayoutData(rgbTextGD);
		rgbPrimaryColorChooser.setTextLimit(6);

		color = new Color(this.getDisplay(), new RGB(0, 0, 0));
		primaryColorChooser = new Label(primaryColor, SWT.NONE);
		primaryColorChooser.setLayoutData(colorLabelGD);
		primaryColorChooser.setText(" "); //$NON-NLS-1$
		primaryColorChooser.setBackground(color);
		primaryColorChooser.addMouseListener(colorMouseListener(
				primaryColorChooser, rgbPrimaryColorChooser));

		rgbPrimaryColorChooser.addFocusListener(colorFocusListener(
				rgbPrimaryColorChooser, primaryColorChooser));
	}

	private void createLayoutData() {
		colorLabelGD = new GridData();
		colorLabelGD.widthHint = 42;
		rgbTextGD = new GridData();
		rgbTextGD.widthHint = 75;
		designformGD = new GridData();
		designformGD.widthHint = 115;
		labelGD = new GridData();
		labelGD.widthHint = 85;
	}

	/**
	 * die Farb Schemas fest einprogrammiert, neue hier adden. Jedes Schema hat
	 * 3 Strings für die Farben.
	 */
	private void addColorToColorSchema() {
		// blau
		cSchemes[0][0] = new String(DEFAULT_BACKGOUNDCOLOR); //$NON-NLS-1$
		cSchemes[0][1] = new String(DEFAULT_BORDERCOLOR); //$NON-NLS-1$
		cSchemes[0][2] = new String(DEFAULT_FONTCOLOR); //$NON-NLS-1$
		// rot
		cSchemes[1][0] = new String("FF8080"); //$NON-NLS-1$
		cSchemes[1][1] = new String("804040"); //$NON-NLS-1$
		cSchemes[1][2] = new String("FF0000"); //$NON-NLS-1$
	}

	/**
	 * Diese Methode initialisiert die Designwahl-Seite und laedt die
	 * Einstellungen des default-Design als abgelegte xml-Datei aus dem
	 * Dateisystem. Wenn das default-Design nicht vorhanden wird, so wird es
	 * erstellt und abgespeichert.
	 */
	public void init() {
		playerDesignsField = true;

		final String defaultName = "default"; //$NON-NLS-1$

		File xmlFile = new File("C:\\Users\\" + System.getProperty("user.name") //$NON-NLS-1$ //$NON-NLS-2$
				+ "\\SIVA-Suite\\Player Designs\\Predefined Designs\\" //$NON-NLS-1$
				+ defaultName + ".xml"); //$NON-NLS-1$

		// Index bei xmlImport aendern, wenn mehr predefined design
		// eincodiert werden!!
		if (xmlFile.exists()) {
			xmlImport(defaultName, 0);
		} else {
			createDefaultDesign();
			setDesignNames();
			xmlImport(defaultName, 0);
		}

		setDesignNames();
		designChooser.setText(designName);

		autoPlay = true;
		isPrimaryColor = true;
		primaryColorButton.setSelection(true);
		primaryColorValue = DEFAULT_PRIMARYCOLOR;
		enableOwnDesign(!isPrimaryColor);
		setFields();

		playerDesignsField = false;
	}

	/**
	 * Erstelle Default-Design und exportiere es ins Dateisystem.
	 */
	private void createDefaultDesign() {
		final String defaultName = "default"; //$NON-NLS-1$

		designName = defaultName;
		designChooser.setText(defaultName);
		designSchemaCombo
				.setText(Messages.PlayerSettings_schema_designSchema_angled);
		colorSchemaCombo.setText(Messages.PlayerSettings_schema_colorSchema_blue);
		rgbBackgroundColor.setText(DEFAULT_BACKGOUNDCOLOR);
		rgbBorderColor.setText(DEFAULT_BORDERCOLOR);
		rgbTextColor.setText(DEFAULT_FONTCOLOR);
		fontCombo.setText("Arial"); //$NON-NLS-1$
		fontSizeCombo.setText("12"); //$NON-NLS-1$

		xmlExport(defaultName, true);
	}

	/*
	 * Dialog zum dateinamen auswählen und speichern, wird zZ nicht verwendet da
	 * Focus bug. private void createFileDialog(final Shell fileDialog) {
	 * fileDialog.setLayout(new GridLayout(3, true));
	 * 
	 * Label fDFileName = new Label(fileDialog, SWT.NONE);
	 * fDFileName.setText("File Name"); final Text fdFileNameText = new
	 * Text(fileDialog, SWT.BORDER);
	 * 
	 * Button fdSave = new Button(fileDialog, SWT.PUSH); fdSave.setText("Save");
	 * fdSave.addSelectionListener(new SelectionListener() {
	 * 
	 * @Override public void widgetSelected(SelectionEvent e) {
	 * xmlExport(fdFileNameText.getText()); designName =
	 * "C:\\Users\\"+System.getProperty
	 * ("user.name")+"\\SIVA-Suite\\Player Designs\\User Designs\\"
	 * +fdFileNameText.getText()+".xml"; //$NON-NLS-1$ //$NON-NLS-2$
	 * //$NON-NLS-3$ //$NON-NLS-4$ setDesignNames();
	 * skinChooser.setText(fdFileNameText.getText()); }
	 * 
	 * @Override public void widgetDefaultSelected(SelectionEvent e) {
	 * 
	 * }
	 * 
	 * }); }
	 */

	/**
	 * Enablen/Disablen aller nicht benoetigten Einstellungsfelder.
	 */
	private void enableOwnDesign(boolean enable) {
		primaryColorChooser.setEnabled(!enable);
		rgbPrimaryColorChooser.setEnabled(!enable);

		save.setEnabled(enable);
		designChooser.setEnabled(enable);
		designSchemaCombo.setEnabled(enable);
		colorSchemaCombo.setEnabled(enable);
		rgbBackgroundColor.setEnabled(enable);
		rgbBorderColor.setEnabled(enable);
		rgbTextColor.setEnabled(enable);
		backgroundColorChooser.setEnabled(enable);
		borderColorChooser.setEnabled(enable);
		fontColorChooser.setEnabled(enable);
		fontCombo.setEnabled(enable);
		fontSizeCombo.setEnabled(enable);
	}

	/**
	 * erstellt einen neuen mouselistener und colordialog. aendert das
	 * colorlabel das dazugehoert.
	 */
	public MouseListener colorMouseListener(final Label newColorLabel,
			final Text newRGBText) {
		MouseListener newMouseListener = new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				ColorDialog dlg = new ColorDialog(shell);
				dlg.setRGB(newColorLabel.getBackground().getRGB());
				dlg.setText(Messages.PlayerSettings_designSettings_colorDialog);
				RGB rgb = dlg.open();
				/*
				 * Da Integer.toHexString() nicht das gewünschte Format liefert
				 * (2 HexZiffern pro Farbe, bei Schwarz wird nur 0 0 0
				 * zurückgegeben) werden hier die Werte umgerechnet.
				 */
				if (rgb != null) {
					color.dispose();
					color = new Color(shell.getDisplay(), rgb);
					char[] hexvalues = new char[6];
					for (int i = 0; i < hexvalues.length; ++i) {
						hexvalues[i] = '0';
					}
					String r = new String(Integer.toString(color.getRed(), 16));
					char[] rc = new char[r.toCharArray().length];
					rc = r.toCharArray();

					if (rc.length == 1) {
						hexvalues[1] = rc[0];
					} else {
						hexvalues[0] = rc[0];
						hexvalues[1] = rc[1];
					}

					String g = new String(
							Integer.toString(color.getGreen(), 16));
					char[] gc = new char[g.toCharArray().length];
					gc = g.toCharArray();

					if (gc.length == 1) {
						hexvalues[3] = gc[0];
					} else {
						hexvalues[2] = gc[0];
						hexvalues[3] = gc[1];
					}

					String b = new String(Integer.toString(color.getBlue(), 16));
					char[] bc = new char[b.toCharArray().length];
					bc = b.toCharArray();

					if (bc.length == 1) {
						hexvalues[5] = bc[0];
					} else {
						hexvalues[4] = bc[0];
						hexvalues[5] = bc[1];
					}

					String output = new String();
					for (int i = 0; i < hexvalues.length; ++i) {
						output = output + hexvalues[i];
					}

					newRGBText.setText(output.toUpperCase());
					newColorLabel.setBackground(color);

					if (!isPrimaryColor) {
						predefColorSchema = false;
					}

					if (predefColorSchema == false) {
						colorSchemaCombo
								.setText(Messages.PlayerSettings_schema_colorSchema_user);
					}

					notifyChanges(newRGBText, output);
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
		};

		return newMouseListener;
	}

	/**
	 * Listener um die im Textfeld eigegebenen Hexwerte zu überprüfen und dann
	 * das entsprechende colorLabel zu ändern. Um die Werte zu überprüfen
	 * braucht man die einzelnen chars. Die Umwandlung in die Strings ist nötig
	 * um die Integer.parseInt() Funktion nutzen zu können.
	 */
	public FocusListener colorFocusListener(final Text newRGBText,
			final Label newColorLabel) {
		FocusListener newFocusListener = new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				boolean wrong = false;
				MessageBox rgbMessageBox = new MessageBox(shell,
						SWT.ICON_INFORMATION | SWT.OK);
				rgbMessageBox.setText(Messages.PlayerSettings_messageBox_title);
				rgbMessageBox.setMessage(Messages.PlayerSettings_messageBox_text);

				if (newRGBText.getText().length() == 6) {
					char[] hexvalues = new char[6];
					hexvalues = newRGBText.getText().toCharArray();
					for (int i = 0; i < hexvalues.length; ++i) {
						if (String.valueOf(hexvalues[i]).matches("[0-9a-fA-F]")) { //$NON-NLS-1$
							if ((i == 5) && (wrong == false)) {
								String newHexvalues = String.valueOf(hexvalues);
								String r = newHexvalues.substring(0, 2);
								String g = newHexvalues.substring(2, 4);
								String b = newHexvalues.substring(4, 6);

								color = new Color(shell.getDisplay(), new RGB(
										Integer.parseInt(r, 16),
										Integer.parseInt(g, 16),
										Integer.parseInt(b, 16)));
								newColorLabel.setBackground(color);

								if (predefColorSchema == false
										&& e.widget != rgbPrimaryColorChooser) {
									colorSchemaCombo
											.setText(Messages.PlayerSettings_schema_colorSchema_user);
								}

								notifyChanges(newRGBText, newHexvalues);

							} else if ((i == 5) && (wrong == true)) {
								falseColorInput(newRGBText, newColorLabel,
										rgbMessageBox);

							}
						} else {
							wrong = true;
							if (i == 5) {
								falseColorInput(newRGBText, newColorLabel,
										rgbMessageBox);
							}
						}
					}
				} else if (newRGBText.getText().isEmpty()) {
					falseColorInput(newRGBText, newColorLabel, rgbMessageBox);

				} else {
					falseColorInput(newRGBText, newColorLabel, rgbMessageBox);
				}

			}

			/**
			 * Bei falscher Eingabe wird die Texteingabe auf den vorherigen Wert
			 * gesetzt.
			 */
			private void falseColorInput(final Text newRGBText,
					final Label newColorLabel, MessageBox rgbMessageBox) {
				setFields();
				rgbMessageBox.open();
			}

			@Override
			public void focusGained(FocusEvent e) {

			}
		};
		return newFocusListener;
	}

	/**
	 * ModifyListener fuer Farbtextfelder, damit Änderungen an der Farbe das
	 * derzeit ausgewählte Farbschema auf "eigenes" gesetzt wird.
	 * 
	 * @return ModifyListener
	 */
	public ModifyListener colorSchemaChangeListener() {
		ModifyListener newModifyListener = new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text rgbText = (Text) e.widget;

				if (!setToDefColorSchema && predefColorSchema) {
					if (rgbText == rgbBackgroundColor && backgroundColor != null) {
						if (!backgroundColor.equals(rgbBackgroundColor
								.getText())) {
							predefColorSchema = false;
						}
					} else if (rgbText == rgbBorderColor && borderColor != null) {
						if (!borderColor.equals(rgbBorderColor.getText())) {
							predefColorSchema = false;
						}

					} else if (rgbText == rgbTextColor && fontColor != null) {
						if (!fontColor.equals(rgbTextColor.getText())) {
							predefColorSchema = false;
						}
					}

					if (!predefColorSchema) {
						colorSchemaCombo
								.setText(Messages.PlayerSettings_schema_colorSchema_user);
					}
				}
			}
		};
		return newModifyListener;
	}

	/**
	 * FocusListener für ComboBoxen.
	 * 
	 * @return FocusListener für ComboBoxen.
	 */
	public FocusListener comboFocusListener(final Combo combo) {
		FocusListener newFocusListener = new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				String comboInput = combo.getText();

				MessageBox msg = new MessageBox(shell, SWT.ICON_INFORMATION
						| SWT.OK);
				msg.setText(Messages.PlayerSettings_messageBox_title);
				msg.setMessage(Messages.PlayerSettings_messageBox_text);

				boolean found = false;

				for (int i = 0; i < combo.getItemCount(); i++) {
					if (comboInput.equals(combo.getItem(i))) {
						found = true;
					}
				}

				if (!found) {
					msg.open();

				} else {
					if (combo == designSchemaCombo) {
						designSchema = combo.getText();
					} else if (combo == colorSchemaCombo) {
						colorSchema = combo.getText();
					} else if (combo == fontCombo) {
						font = combo.getText();
					} else if (combo == fontSizeCombo) {
						fontSize = combo.getText();
					}
				}

				setFields();
			}

		};
		return newFocusListener;
	}

	/**
	 * Diese Methode sorgt dafür, dass Aenderungen an den Farbtextfeldern
	 * uebernommen und richtig angezeigt werden.
	 * 
	 * @param newRGBText
	 *            Textfeld, das geaendert wurde
	 * @param newHexvalues
	 *            neuer Farbwert als Hexadezimalzahl
	 */
	private void notifyChanges(final Text newRGBText, String newHexvalues) {
		newHexvalues.toUpperCase();

		if (newRGBText == rgbBackgroundColor) {
			backgroundColor = newHexvalues;
		} else if (newRGBText == rgbBorderColor) {
			borderColor = newHexvalues;
		} else if (newRGBText == rgbTextColor) {
			fontColor = newHexvalues;
		} else if (newRGBText == rgbPrimaryColorChooser) {
			primaryColorValue = newHexvalues;
		}
		notifyListeners(SWT.Modify, new Event());
	}

	/**
	 * Diese Methode added die noetigen Listener, um auf Aenderungen der
	 * PlayerDesign zu reagieren.
	 */
	private void addListener() {
		SelectionListener newSelection = selectionListener();
		ModifyListener newModify = colorSchemaChangeListener();
		designSchemaCombo.addSelectionListener(newSelection);
		fontCombo.addSelectionListener(newSelection);
		fontSizeCombo.addSelectionListener(newSelection);
		rgbBackgroundColor.addModifyListener(newModify);
		rgbBorderColor.addModifyListener(newModify);
		rgbTextColor.addModifyListener(newModify);
	}

	/**
	 * Listener, der auf Änderungen der Combo-Selektion reagiert.
	 * 
	 * @return neuer Listener
	 */
	private SelectionListener selectionListener() {
		return new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo eventCombo = (Combo) e.widget;
				String oldValue = null;

				if (playerDesignsField) {
					return;
				}

				if (eventCombo.equals(fontCombo)) {
					oldValue = font;
					font = fontCombo.getText();
					if (!oldValue.equals(font)) {
						setFields();
					}

				} else if (eventCombo.equals(fontSizeCombo)) {
					oldValue = fontSize;
					fontSize = fontSizeCombo.getText();
					if (!oldValue.equals(fontSize)) {
						setFields();
					}
				}
			};

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
	}

	/**
	 * Setze Felder der PlayerSettingsPage entsprechend der ProjectSettings.
	 */
	public void setFields() {

		playerDesignsField = true;

		autoPlayButton.setSelection(autoPlay);
		
		if (isPrimaryColor) {
			primaryColorButton.setSelection(true);
			notPrimaryColorButton.setSelection(false);
			enableOwnDesign(false);
		} else {
			primaryColorButton.setSelection(false);
			notPrimaryColorButton.setSelection(true);
			enableOwnDesign(true);
		}

		rgbPrimaryColorChooser.setText(primaryColorValue);
		rgbPrimaryColorChooser.notifyListeners(SWT.FocusOut, new Event());

		setDesignNames();
		designChooser.setText(designName);
		designSchemaCombo.setText(designSchema);
		colorSchemaCombo.setText(colorSchema);
		fontCombo.setText(font);
		fontSizeCombo.setText(fontSize);
		rgbBackgroundColor.setText(backgroundColor);
		rgbBorderColor.setText(borderColor);
		rgbTextColor.setText(fontColor);

		// Refresh der Farbfelder
		rgbBackgroundColor.notifyListeners(SWT.FocusOut, new Event());
		rgbBorderColor.notifyListeners(SWT.FocusOut, new Event());
		rgbTextColor.notifyListeners(SWT.FocusOut, new Event());

		setToDefColorSchema = false;

		playerDesignsField = false;
		notifyListeners(SWT.Modify, new Event());
	}

	/**
	 * Einlesen der Filenames um den DesignChooser mit den gespeicherten Designs
	 * zu füllen. Beim Speichern eines neuen Designs wird die Liste gelöscht und
	 * neu befüllt wodurch die Designs wieder sortiert in der Liste stehen.
	 * Insbesondere wird die vorherige Selektion des Choosers auf "" gesetzt.
	 */
	public void setDesignNames() {
		if (designChooser.getItemCount() > 0) {
			designChooser.removeAll();
			setDesignNames();
		} else {
			File directory1 = new File(
					"C:\\Users\\" + System.getProperty("user.name") + "\\SIVA-Suite\\Player Designs\\Predefined Designs"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			File directory2 = new File(
					"C:\\Users\\" + System.getProperty("user.name") + "\\SIVA-Suite\\Player Designs\\User Designs"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (directory1.exists() && directory2.exists()) {
				preDesCount = 0;
				String[] files = directory1.list();
				String tmpName = new String();
				for (int i = 0; i < files.length; ++i) {
					tmpName = files[i].substring(0, files[i].length() - 4);
					designChooser.add(tmpName);
					preDesCount = preDesCount + 1;
				}
				files = directory2.list();
				for (int i = 0; i < files.length; ++i) {
					tmpName = files[i].substring(0, files[i].length() - 4);
					designChooser.add(tmpName);
				}
			} else {
				directory1.mkdirs();
				directory2.mkdirs();
				setDesignNames();
			}
		}
	}

	/**
	 * XML Import, XML-Schema Änderungen hier durchführen
	 * 
	 * @param design
	 *            Name des Designs, das importiert werden soll
	 * @param selectedEntry
	 *            Indexnummer des Designs im DesignChooser, das importiert
	 *            werden soll
	 */
	private void xmlImport(String design, int selectedEntry) {
		DocumentBuilder parser;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(false);
		try {
			parser = factory.newDocumentBuilder();
			File xmlFile;
			if (selectedEntry <= preDesCount) {
				xmlFile = new File(
						"C:\\Users\\" + System.getProperty("user.name") + "\\SIVA-Suite\\Player Designs\\Predefined Designs\\" + design + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			} else {
				xmlFile = new File(
						"C:\\Users\\" + System.getProperty("user.name") + "\\SIVA-Suite\\Player Designs\\User Designs\\" + design + ".xml"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			if (xmlFile.exists()) {
				Document doc = parser.parse(xmlFile);
				doc.getDocumentElement().normalize();

				NodeList nList = doc.getElementsByTagName("playerDesign"); //$NON-NLS-1$

				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;

						designName = getTagValue("designName", eElement); //$NON-NLS-1$
						designChooser.setText(designName);

						designSchema = getTagValue("designSchema", eElement); //$NON-NLS-1$
						designSchemaCombo.setText(designSchema);

						colorSchema = getTagValue("colorSchema", eElement); //$NON-NLS-1$
						colorSchemaCombo.setText(colorSchema);

						font = getTagValue("font", eElement); //$NON-NLS-1$
						fontCombo.setText(font);

						fontSize = getTagValue("fontSize", eElement); //$NON-NLS-1$
						fontSizeCombo.setText(fontSize);
					}
				}

				nList = doc.getElementsByTagName("colorAdjustments"); //$NON-NLS-1$
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;

						backgroundColor = getTagValue(
								"backgroundColor", eElement); //$NON-NLS-1$
						rgbBackgroundColor.setText(backgroundColor);
						rgbBackgroundColor.notifyListeners(SWT.FocusOut,
								new Event());
						predefColorSchema = true;

						borderColor = getTagValue("borderColor", eElement); //$NON-NLS-1$
						rgbBorderColor.setText(borderColor);
						rgbBorderColor.notifyListeners(SWT.FocusOut,
								new Event());
						predefColorSchema = true;

						fontColor = getTagValue("fontColor", eElement); //$NON-NLS-1$
						rgbTextColor.setText(fontColor);
						rgbTextColor.notifyListeners(SWT.FocusOut, new Event());
						predefColorSchema = true;
					}
				}

				notifyListeners(SWT.Modify, new Event());
				predefColorSchema = true;
			}
		} catch (SAXException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} catch (ParserConfigurationException e) {
			System.out.println(e);
		} catch (NullPointerException e) {
			System.out.println(e);
		}
	}

	/**
	 * Hilfsfunktion zum erhalten der Werte.
	 */
	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();
		Node nValue = nlList.item(0);

		return nValue.getNodeValue();
	}

	/**
	 * XML Export, XML-Schema Änderungen hier durchführen.
	 * 
	 * @param fileName
	 *            Name des xml-Datei und des Designs
	 * @param newDefault
	 *            <b>true</b> falls ein neues default Design erstellt wird,
	 *            sonst <b>false</b>
	 */
	private void xmlExport(String fileName, boolean newDefault) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();

			Document doc = parser.newDocument();

			Element playerDesign = doc.createElement("playerDesign"); //$NON-NLS-1$
			doc.appendChild(playerDesign);

			Element designName = doc.createElement("designName"); //$NON-NLS-1$
			org.w3c.dom.Text designNameValue = doc.createTextNode(designChooser
					.getText());
			designName.appendChild(designNameValue);
			playerDesign.appendChild(designName);

			Element designSchema = doc.createElement("designSchema"); //$NON-NLS-1$
			org.w3c.dom.Text designSchemaValue = doc
					.createTextNode(designSchemaCombo.getText());
			designSchema.appendChild(designSchemaValue);
			playerDesign.appendChild(designSchema);

			Element colorSchema = doc.createElement("colorSchema"); //$NON-NLS-1$
			org.w3c.dom.Text colorSchemaValue = doc
					.createTextNode(colorSchemaCombo.getText());
			colorSchema.appendChild(colorSchemaValue);
			playerDesign.appendChild(colorSchema);

			Element font = doc.createElement("font"); //$NON-NLS-1$
			org.w3c.dom.Text fontValue = doc
					.createTextNode(fontCombo.getText());
			font.appendChild(fontValue);
			playerDesign.appendChild(font);

			Element fontSize = doc.createElement("fontSize"); //$NON-NLS-1$
			org.w3c.dom.Text fontSizeValue = doc.createTextNode(fontSizeCombo
					.getText());
			fontSize.appendChild(fontSizeValue);
			playerDesign.appendChild(fontSize);

			Element colorAdjustment = doc.createElement("colorAdjustments"); //$NON-NLS-1$
			playerDesign.appendChild(colorAdjustment);

			Element backgroundColor = doc.createElement("backgroundColor"); //$NON-NLS-1$
			org.w3c.dom.Text backgroundColorValue = doc
					.createTextNode(rgbBackgroundColor.getText());
			backgroundColor.appendChild(backgroundColorValue);
			colorAdjustment.appendChild(backgroundColor);

			Element borderColor = doc.createElement("borderColor"); //$NON-NLS-1$
			org.w3c.dom.Text borderColorValue = doc
					.createTextNode(rgbBorderColor.getText());
			borderColor.appendChild(borderColorValue);
			colorAdjustment.appendChild(borderColor);

			Element fontColor = doc.createElement("fontColor"); //$NON-NLS-1$
			org.w3c.dom.Text fontColorValue = doc.createTextNode(rgbTextColor
					.getText());
			fontColor.appendChild(fontColorValue);
			colorAdjustment.appendChild(fontColor);

			Transformer trans = TransformerFactory.newInstance()
					.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); //$NON-NLS-1$
			trans.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
			trans.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			DOMSource source = new DOMSource(doc);

			String file = ""; //$NON-NLS-1$
			if (newDefault) {
				file = "C:\\Users\\" + System.getProperty("user.name") //$NON-NLS-1$ //$NON-NLS-2$
						+ "\\SIVA-Suite\\Player Designs\\Predefined Designs\\" //$NON-NLS-1$
						+ fileName + ".xml"; //$NON-NLS-1$
			} else {
				file = "C:\\Users\\" + System.getProperty("user.name") //$NON-NLS-1$ //$NON-NLS-2$
						+ "\\SIVA-Suite\\Player Designs\\User Designs\\" //$NON-NLS-1$
						+ fileName + ".xml"; //$NON-NLS-1$
			}

			File f = new File(file);
			if (f.getParentFile() != null) {
				f.getParentFile().mkdirs();
			}			
			FileOutputStream os = new FileOutputStream(f); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			StreamResult result = new StreamResult(os);
			trans.transform(source, result);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}