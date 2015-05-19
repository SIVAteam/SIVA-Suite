package org.iviPro.editors.quiz.view;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.iviPro.editors.quiz.std.NodeModel;
import org.iviPro.model.quiz.Question;

/**
 * Diese Klasse implementiert die graphische Darstellung der Fragenuebersicht
 * (als ExpandBarItem).
 * 
 * @author Sabine Gattermann
 * 
 */
public class NodeViewPartQuestionOverview {

	private static Composite parent;
	private static NodeModel iFace;
	private static Color highlightColor;

	/**
	 * Konstruktor
	 * 
	 * @param parent
	 *            Das Eltern-Composite.
	 * @param iFace
	 *            Die Model-Klasse.
	 */
	public NodeViewPartQuestionOverview(Composite parent, NodeModel iFace) {
		NodeViewPartQuestionOverview.parent = parent;
		NodeViewPartQuestionOverview.iFace = iFace;
		highlightColor = new Color(parent.getDisplay(), 211, 211, 211);
	}

	/**
	 * Oeffnet die graphische Ausgabe in einer ExpandBar.
	 * 
	 * @param expandIt
	 *            Der Indikator (true/false) ob ExpandBarItem expandiert ist.
	 */
	public static void open(boolean expandIt) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(highlightColor);
		GridLayout layout = new GridLayout();
		layout.marginLeft = layout.marginTop = layout.marginRight = layout.marginBottom = 5;
		layout.verticalSpacing = 5;
		composite.setLayout(layout);

		Tree tree = new Tree(composite, SWT.SINGLE | SWT.BORDER);
		tree.setRedraw(false);

		LinkedList<Question> questionList = iFace.getQuestions();
		
TreeItem titleItem = new TreeItem(tree, SWT.NONE);
		String title = iFace.getTestTitle();
		if (title.length() > 37)
			title = title.substring(0, 37) + "...";
		titleItem.setText("Titel: " + title);

		for (int i = 0; i < questionList.size(); i++) {
			TreeItem item = new TreeItem(titleItem, SWT.NONE);
			String content = questionList.get(i).getQuestionText();

			// Bold-Styling ausblenden
			content = content.replaceAll("<b>", "");
			content = content.replaceAll("</b>", "");

			if (content.contains("\n")) {
				content = content.substring(0, content.indexOf("\n"));
				if (content.length() > 25)
					content = content.substring(0, 25) + "...";
			}
			item.setText("Frage " + (i + 1) + ":  " + content);
			item.setExpanded(true);

		}
		tree.setRedraw(true);

		GridData treeGridData = new GridData();
		treeGridData.grabExcessHorizontalSpace = true;
		tree.setLayoutData(treeGridData);

		titleItem.setExpanded(true);
		tree.layout();
		composite.layout();

		ExpandItem item = new ExpandItem((ExpandBar) parent, SWT.NONE, 1);
		item.setText("Fragen-Übersicht");
		item.setHeight(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

		item.setControl(composite);
		item.setData("position", "1");
		item.setExpanded(expandIt);

	}
}
