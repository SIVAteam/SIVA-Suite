package org.iviPro.editors.scenegraph.subeditors;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.editors.quiz.systemstart.QuizGenerator;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.quiz.DbQueries;
import org.iviPro.model.quiz.Test;
import org.iviPro.model.quiz.TestManager;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.graph.ModifyNodeQuizOperation;

public class NodeQuizEditor extends TitledNodeEditor {
	
	private static int WIDTH = 300;

	private final NodeQuiz quiz;

	private QuizGenerator quizGenerator;

	private Combo quizCombo;

	public NodeQuizEditor(NodeQuiz quiz) {
		super(Messages.NodeQuizEditor_Shell_Title, quiz, WIDTH);
		this.quizGenerator = new QuizGenerator();
		this.quiz = quiz;

		final Group quizGroup = new Group(contentComposite, SWT.NONE);
		GridLayout quizGroupLayout = new GridLayout(1, false);
		quizGroupLayout.verticalSpacing = 10;
		quizGroup.setLayout(quizGroupLayout);
		quizGroup.setText(Messages.NodeQuizEditor_Group_Quiz);
		quizGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		quizCombo = new Combo(quizGroup, SWT.DROP_DOWN | SWT.BORDER);
		// Custom read only implementation. Using SWT.READ_ONLY flag causes the
		// combo to irreversibly change its color to gray
		quizCombo.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
		quizCombo.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
			}			
			@Override
			public void mouseDown(MouseEvent e) {
				quizCombo.setListVisible(true);
			}		
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		quizCombo.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				e.doit = false;
			}
			@Override
			public void keyPressed(KeyEvent e) {
				e.doit = false;
			}
		});
		
		List<Test> testList = DbQueries
				.getUserTests(QuizGenerator.getCurrentUser());
		for (Test test : testList) {
			quizCombo.add(test.getTitle());
		}
		
		if (quiz.getTestId() != -1) {
			Test currentTest = DbQueries.getTestData(quiz.getTestId());
			quizCombo.setText(currentTest.getTitle());
		}

		quizCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		
	
		// BUTTONS
		Composite buttonPanel = new Composite(quizGroup, SWT.NONE);
		GridLayout buttonPanelLayout = new GridLayout(3, false);
		buttonPanelLayout.marginHeight = 0;
		buttonPanelLayout.marginWidth = 0;
		buttonPanel.setLayout(buttonPanelLayout);
		buttonPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Button createQuizButton = new Button(buttonPanel, SWT.PUSH);
		createQuizButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				onCreateQuiz();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});			
		createQuizButton.setText(Messages.Common_Button_Create);
		GridData createGd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		createGd.widthHint = 80;
		createQuizButton.setLayoutData(createGd);

		Button editButton = new Button(buttonPanel, SWT.PUSH);
		editButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				onEdit();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		editButton.setText(Messages.Common_Button_Edit);
		GridData editGd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
		editGd.widthHint = 80;
		editButton.setLayoutData(editGd);
	
		Button deleteButton = new Button(buttonPanel, SWT.PUSH);
		deleteButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				onDelete();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		deleteButton.setText(Messages.Common_Button_Delete);
		GridData deleteGd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		deleteGd.widthHint = 80;
		deleteButton.setLayoutData(deleteGd);
	}

	private void onCreateQuiz() {
		int id = quiz.setNewTest();
		quiz.setTestId(quizGenerator.show(id, this));
		shell.close();
	}

	private void onEdit() {
		quiz.setTestId(quizGenerator.show(TestManager.getInstance()
				.getTestIdByTitle(quizCombo.getText()), this));
		shell.close();
	}

	public void onDelete() {
		int id = TestManager.getInstance().getTestIdByTitle(quizCombo.getText());
		onDelete(id);
	}

	public void onDelete(int id) {
		if (id != -1) {
			Test test = TestManager.getInstance().getTestObject(id);
			if (test != null) {
				DbQueries.deleteTest(id, test.getCategory());
				if(Arrays.asList(quizCombo.getItems()).contains(quizCombo.getText())){
					quizCombo.remove(quizCombo.getText());
					quiz.setTestId(-1);
				}
			}
		}
	}

	@Override
	protected void executeChangeOperation() {
		int id = TestManager.getInstance().getTestIdByTitle(quizCombo.getText());
		
		ModifyNodeQuizOperation op = 
				new ModifyNodeQuizOperation(quiz, 
						titleComp.getTitle(), id);
		try {
			OperationHistory.execute(op);
		} catch (ExecutionException e) {
			Shell shell = Display.getCurrent().getActiveShell();
			MessageDialog.openError(shell,
					Messages.Common_ErrorDialog_Title, e
					.getMessage());
		}
	}
}
