package org.iviPro.editors.quiz.view;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.iviPro.editors.quiz.std.NodeModel;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;
import org.iviPro.model.quiz.AdditionalInfo;

/**
 * Diese Klasse implementiert die graphische Darstellung der Link-ZusatzInfos
 * (als ExpandBarItem).
 * 
 * @author Sabine Gattermann
 * 
 */
public class NodeViewPartLinks {

	private static Composite parent;
	private static LinkedList<Text> linkTextFields;
	private static LinkedList<Button> removeLinkButtons;
	private static int linkCounter;
	private static Composite compositeLinks;
	private static Text linkInputTextField;
	private static NodeModel iFace;
	private static ExpandItem item;
	private static LinkedList<Label> linkLabelList;
	private static Color highlightColor;

	/**
	 * Konstruktor
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 * @param iFace
	 *            Die Model-Klasse.
	 */
	public NodeViewPartLinks(Composite parent, NodeModel iFace) {
		NodeViewPartLinks.parent = parent;
		NodeViewPartLinks.iFace = iFace;
		highlightColor = new Color(parent.getDisplay(), 211, 211, 211);
	}

	/**
	 * Oeffnet die graphische Ausgabe in einer ExpandBar.
	 * 
	 * @param expandIt
	 *            Der Indikator (true/false) ob ExpandBarItem expandiert ist.
	 */
	public static void open(boolean expandIt) {

		linkLabelList = new LinkedList<Label>();
		linkTextFields = new LinkedList<Text>();
		removeLinkButtons = new LinkedList<Button>();
		linkCounter = 0;
		item = new ExpandItem((ExpandBar) parent, SWT.NONE, 3);

		// Basis-Composite
		compositeLinks = new Composite(parent, SWT.NONE);
		GridLayout linkLayout = new GridLayout(3, false);
		compositeLinks.setLayout(linkLayout);
		GridData linkGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		linkGridData.minimumWidth = 500;
		compositeLinks.setLayoutData(linkGridData);
		compositeLinks.setBackground(highlightColor);

		// erstellt Maske
		linkInputTextField = new Text(compositeLinks, SWT.BORDER);
		GridData litfGridData = new GridData(420, 20);
		litfGridData.horizontalSpan = 2;
		linkInputTextField.setLayoutData(litfGridData);
		Button b = new Button(compositeLinks, SWT.PUSH);
		b.setText("Link hinzufügen");
		b.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {

				try {
					new URL(linkInputTextField.getText());

					// neue Link-Zeile einfuegen

					linkCounter++;

					Label l = new Label(compositeLinks, SWT.NONE);
					l.setText("Link " + linkCounter + ": ");
					l.setBackground(highlightColor);
					linkLabelList.add(l);

					Text t = new Text(compositeLinks, SWT.READ_ONLY | SWT.WRAP);
					GridData tGridData = new GridData();
					tGridData.widthHint = 385;
					t.setLayoutData(tGridData);
					t.setText(linkInputTextField.getText());
					t.setData("linkCounter", String.valueOf(linkCounter));
					linkTextFields.add(t);

					Button b = new Button(compositeLinks, SWT.PUSH);
					b.setText("entfernen");
					b.setData("linkCounter", linkCounter);
					b.setData("idMedia", linkInputTextField.getData("idMedia"));
					removeLinkButtons.add(b);
					b.addListener(SWT.Selection, new Listener() {

						@Override
						public void handleEvent(Event arg0) {
							String counter = String.valueOf(arg0.widget
									.getData("linkCounter"));

							// bei null wurde link noch nicht in die db
							// geschrieben!!
							if (arg0.widget.getData("idMedia") != null) {
								String str = String.valueOf(arg0.widget
										.getData("idMedia"));

								int delId = Integer.parseInt(str);
								if (delId > 0)
									iFace.deleteMedia(delId);
							}
							Text t = null;
							int atIndex = 0;
							boolean found = false;

							while (!found && atIndex < linkTextFields.size()) {
								t = linkTextFields.get(atIndex);
								if (t.getData("linkCounter").equals(counter))
									found = true;
								atIndex++;
							}

							if (found == true) {
								Label labelToDel = linkLabelList
										.get(atIndex - 1);
								linkLabelList.remove(atIndex - 1);
								for (int i = 0; i < linkLabelList.size(); i++) {
									Label l = linkLabelList.get(i);
									l.setText("Link " + (i + 1) + ": ");
								}
								labelToDel.dispose();
								linkTextFields.remove(atIndex - 1);
								t.dispose();
								arg0.widget.dispose();
								linkCounter--;
							}

							compositeLinks.layout();
							// Neue Groesse berechnen
							compositeLinks.setSize(compositeLinks.computeSize(
									SWT.DEFAULT, SWT.DEFAULT));
							// Focus auf Textfeld der neuen Zeile
							linkInputTextField.setFocus();
							compositeLinks.layout();
							item.setHeight(compositeLinks.computeSize(
									SWT.DEFAULT, SWT.DEFAULT).y);
							parent.layout();
						}
					});

					linkInputTextField.setText("");
					linkInputTextField.setFocus();

					// Composite-Updaten
					compositeLinks.layout(new Control[] { t, b });
					// Neue Groesse berechnen
					compositeLinks.setSize(compositeLinks.computeSize(
							SWT.DEFAULT, SWT.DEFAULT));
					// Focus auf Textfeld der neuen Zeile
					linkInputTextField.setFocus();
					compositeLinks.layout();
					item.setHeight(compositeLinks.computeSize(SWT.DEFAULT,
							SWT.DEFAULT).y);
					parent.layout();
				} catch (MalformedURLException e1) {
					QuizGenerator
							.errorDialog("Bitte geben Sie einen vollständigen Link an!\n\n"
									+ "Format-Beispiele:\n"
									+ "    http://www.link.de \n"
									+ "    ftp://xy.de \n" + "\n");
				}
			}
		});

		LinkedList<AdditionalInfo> links = iFace.getCurrentLinkList();
		for (int i = 0; i < links.size(); i++) {
			linkInputTextField.setText(links.get(i).getAddress());
			linkInputTextField.setData("idMedia", links.get(i)
					.getIdAdditionalInfo());
			b.setSelection(true);
			Event event = new Event();
			event.type = SWT.Selection;
			event.widget = b;
			b.notifyListeners(SWT.Selection, event);
			linkInputTextField.setText("");
		}

		compositeLinks.setSize(compositeLinks.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

		item.setText("Links");
		item.setHeight(compositeLinks.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		item.setControl(compositeLinks);
		item.setData("position", "3");
		item.setExpanded(expandIt);
		parent.layout();

	}

	/**
	 * Getter fuer eingegebene / gespeicherte Links.
	 * 
	 * @return Die Liste der Links.
	 * 
	 *         BugFixing Links wurden rausgelöscht. Die Linkliste ist somit
	 *         immer leer die zurückgegeben wird!
	 */
	public static LinkedList<String> getLinksFromTextFields() {
//		int count = linkTextFields.size();
//		LinkedList<String> links = new LinkedList<String>();
//
//		for (int i = 0; i < count; i++) {
//			links.add(i, linkTextFields.get(i).getText());
//		}
//
//		return links;
		return new LinkedList<String>();
	}

}
