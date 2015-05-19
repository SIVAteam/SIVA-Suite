package org.iviPro.dialogs.projectcreate;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.iviPro.dialogs.projectcreate.messages"; //$NON-NLS-1$

	public static String ProjectCreateNewAction_DefaultProjectName;

	public static String ProjectCreateWizard_Error_FailedCreateProjectFile1;

	public static String ProjectCreateWizard_Error_FailedCreateProjectFile2;

	public static String ProjectCreateWizard_Error_FailedToCreateProjectDirectory1;

	public static String ProjectCreateWizard_Error_FailedToCreateProjectDirectory2;

	public static String ProjectCreateWizard_Error_MsgBoxTitle;

	public static String ProjectCreateWizard_WindowTitle;
	public static String ProjectCreateWizardPage_ButtonDirectory;
	public static String ProjectCreateWizardPage_ChooseDirDialog_Description;
	public static String ProjectCreateWizardPage_ChooseDirDialog_Title;
	public static String ProjectCreateWizardPage_Error_HintProjectDirExistsAlready;
	public static String ProjectCreateWizardPage_Error_ParentDirectoryInvalid;
	public static String ProjectCreateWizardPage_Error_ProjectNameTooShort1;
	public static String ProjectCreateWizardPage_Error_ProjectNameTooShort2;
	public static String ProjectCreateWizardPage_LabelDirectory;
	public static String ProjectCreateWizardPage_LabelName;
	public static String ProjectCreateWizardPage_WizardDescripton;
	public static String ProjectCreateWizardPage_WizardTitle;

	/**
	 * Initialize resource bundle
	 */
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
