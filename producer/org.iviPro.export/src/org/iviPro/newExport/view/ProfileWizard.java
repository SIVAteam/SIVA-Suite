package org.iviPro.newExport.view;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.Profile;

public class ProfileWizard extends Wizard {

	private final Profile profile;
	final static String WIZARD_TITLE = Messages.ProfileWizard_Title;
	@SuppressWarnings("unused")
	private final List<String> prohibitedTitles;
	private final GeneralPage generalPage;
	private final AudioPage audioPage;
	private final VideoPage videoPage;

	public ProfileWizard(Profile profile, List<String> prohibitedTitles) {
		super();
		setWindowTitle(WIZARD_TITLE);
		this.profile = profile;
		this.prohibitedTitles = prohibitedTitles;
		this.generalPage = new GeneralPage(profile.getGeneral(),
				prohibitedTitles);
		this.audioPage = new AudioPage(profile.getAudio());
		this.videoPage = new VideoPage(profile.getVideo());
	}

	@Override
	public void addPages() {
		addPage(generalPage);
		addPage(audioPage);
		addPage(videoPage);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public Profile getProfile() {
		return profile;
	}

}
