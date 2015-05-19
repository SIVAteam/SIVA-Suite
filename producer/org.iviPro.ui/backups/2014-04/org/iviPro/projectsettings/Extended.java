package org.iviPro.projectsettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

public class Extended extends Composite {
	private final static String PATH = "/MTTServer/API/Project/AddProject/";
	private final static String PARAMETER_USER = "account.username";
	private final static String PARAMETER_PASSWORD = "account.password";
	private final static String PARAMETER_PROJECT = "project.name";
	private final static String COLON = ":";
	private final static String HTTP = "http://";
	private final static String UNDEFINED = "undefined";

	public int reloadTime;
	public boolean reload;
	public String projectName;
	public int projectCollaborationID;
	private boolean settingFields = false;
	private Button relCheck;
	private Text relText;
	private Text proNameText;
	private Label collaborationID;

	public Extended(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));

		Composite relComp = new Composite(this, SWT.NONE);
		relComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		relComp.setLayout(new GridLayout(4, false));
		relCheck = new Button(relComp, SWT.CHECK);
		relCheck.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				reload = relCheck.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		Label relFirst = new Label(relComp, SWT.NONE);
		relFirst.setText(Messages.Extended_Reload_First_Text);

		relText = new Text(relComp, SWT.BORDER | SWT.RIGHT);
		relText.addVerifyListener(verifyInput());
		relText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (!settingFields) {
					try {
						reloadTime = Integer.parseInt(relText.getText());
					} catch (NumberFormatException e1) {

					}
				}

			}
		});
		relText.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				if (relText.getText() == "") { //$NON-NLS-1$
					settingFields = true;
					relText.setText("0"); //$NON-NLS-1$
					reloadTime = 0;
					settingFields = false;
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
			}
		});
		GridData textWidth = new GridData();
		textWidth.widthHint = 20;
		relText.setLayoutData(textWidth);

		Label relSecond = new Label(relComp, SWT.None);
		relSecond.setText(Messages.Extended_Reload_Second_Text);

		Composite proName = new Composite(this, SWT.None);
		proName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		proName.setLayout(new GridLayout(2, false));

		Label proNameTitle = new Label(proName, SWT.None);
		proNameTitle.setText(Messages.Extended_ProjectNameTitle);

		proNameText = new Text(proName, SWT.BORDER);
		proNameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (!settingFields) {
					projectName = proNameText.getText();
				}

			}
		});

		@SuppressWarnings("unused")
		Composite collaboration = new Collaboration(this, style);
	}

	/**
	 * Implements the Composite for the Collaboration tab. Collaborations
	 * (enabling viewers of the video to add text or other content like links to
	 * a finished interactive non-linear video) can be enabled for the edited
	 * video here.
	 * 
	 * In order to have collaborations for a video, the video has to be
	 * registered at a server, returning an ID. Supporting that ID, the viewer
	 * can use the collaboration feature while viewing the video.
	 */
	private class Collaboration extends Composite {

		private Group collGroup;

		private Composite checkComp;
		private Button checkButton;
		private boolean collaborationsAllowed;
		private Label checkLabel;

		private Composite inputComp;
		private Label serverLabel;
		private Label portLabel;
		private Label userLabel;
		private Label passwordLabel;
		private Text serverText;
		private Text portText;
		private Text userText;
		private Text passwordText;

		private String server;
		private String port;
		private String user;
		private String password;

		private Composite confirmComp;
		private Button confirm;

		/**
		 * Standard constructor, setting up the different sub Composites for the
		 * Collaboration tab.
		 * 
		 * @param parent
		 *            The parent Composite for the tab.
		 * @param style
		 *            The Style for the tab.
		 */
		public Collaboration(Composite parent, int style) {
			super(parent, style);
			setLayout(new GridLayout(1, false));
			setData(new GridData(SWT.FILL, SWT.FILL, true, true));
			this.collaborationsAllowed = false;

			// Create the group (space that encases all elements) for the
			// Collaboration tab
			collGroup = new Group(this, SWT.LEFT);
			collGroup.setLayout(new GridLayout(1, false));
			collGroup.setData(new GridData(SWT.FILL, SWT.FILL, true, true));
			collGroup.setText(Messages.Collaboration_Group_Title);

			collaborationID = new Label(collGroup, SWT.None);
			defineCollaborationID(projectCollaborationID);

			// Create sub elements of the Collaboration tab
			createCheckBox();
			createInputFields();
			createConfirmButton();

			// By default, the controls for Collaboration are turned
			// off/disabled
			enableCollaborationField(false);
		}

		/**
		 * Creates a checkbox, allowing to enable or disable the content of the
		 * Collaboration tab.
		 */
		private void createCheckBox() {
			checkComp = new Composite(collGroup, SWT.NONE);
			checkComp.setLayout(new GridLayout(2, false));

			checkButton = new Button(checkComp, SWT.CHECK);
			checkButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					collaborationsAllowed = checkButton.getSelection();
					enableCollaborationField(collaborationsAllowed);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			checkLabel = new Label(checkComp, SWT.NONE);
			checkLabel.setText(Messages.Collaboration_Allow_Collaborations);
		}

		/**
		 * Creates the input fields for the interaction with the server. A
		 * server address, port, username and password have to be supported.
		 */
		private void createInputFields() {
			inputComp = new Composite(collGroup, SWT.NONE);
			inputComp.setLayout(new GridLayout(2, false));

			GridData serverData = new GridData();
			serverData.widthHint = 150;
			GridData portData = new GridData();
			portData.widthHint = 50;
			GridData textData = new GridData();
			textData.widthHint = 100;

			serverLabel = new Label(inputComp, SWT.NONE);
			serverLabel.setText(Messages.Collaboration_Server_Label);
			serverText = new Text(inputComp, SWT.BORDER);
			serverText.setLayoutData(serverData);
			serverText.setText(Messages.Collaboration_Standard_Server);
			server = Messages.Collaboration_Standard_Server;
			serverText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					if (!settingFields) {
						server = serverText.getText();
					}
				}
			});

			portLabel = new Label(inputComp, SWT.NONE);
			portLabel.setText(Messages.Collaboration_Port_Label);
			portText = new Text(inputComp, SWT.BORDER);
			portText.setLayoutData(portData);
			portText.setText(Messages.Collaboration_Standard_Port);
			port = Messages.Collaboration_Standard_Port;
			portText.addVerifyListener(verifyInput());
			portText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					if (!settingFields) {
						port = portText.getText();
					}
				}
			});

			userLabel = new Label(inputComp, SWT.NONE);
			userLabel.setText(Messages.Collaboration_User_Label);
			userText = new Text(inputComp, SWT.BORDER);
			userText.setLayoutData(textData);
			userText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					if (!settingFields) {
						user = userText.getText();
					}
				}
			});

			passwordLabel = new Label(inputComp, SWT.NONE);
			passwordLabel.setText(Messages.Collaboration_Password_Label);
			passwordText = new Text(inputComp, SWT.PASSWORD | SWT.BORDER);
			passwordText.setLayoutData(textData);
			passwordText.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					if (!settingFields) {
						password = passwordText.getText();
					}
				}
			});

		}

		/**
		 * Creates the button that sends the supported information from the
		 * input fields to the server via http post request.
		 */
		private void createConfirmButton() {
			confirmComp = new Composite(collGroup, SWT.NONE);
			GridLayout confirmLayout = new GridLayout();
			confirmLayout.marginLeft = 200;
			confirmComp.setLayout(confirmLayout);

			confirm = new Button(confirmComp, SWT.PUSH);
			confirm.setText(Messages.Collaboration_Confirmbutton_Text);
			GridData confirmData = new GridData();
			confirmData.horizontalAlignment = GridData.END;
			confirmData.grabExcessHorizontalSpace = true;
			confirm.setLayoutData(confirmData);
			confirm.addListener(SWT.MouseDown, new Listener() {

				@Override
				public void handleEvent(Event event) {
					// Uses the supported information to send a request to the
					// server, requesting the registration of the project -
					// answer will be the ID for the project
					requestID();
				}
			});
		}

		/**
		 * Function to enable/disable the elements of the Collaboration tab.
		 * 
		 * @param enabled
		 *            True, if the elements are to be enabled, false else.
		 */
		private void enableCollaborationField(boolean enabled) {
			serverText.setEnabled(enabled);
			portText.setEnabled(enabled);
			userText.setEnabled(enabled);
			passwordText.setEnabled(enabled);
			confirm.setEnabled(enabled);
		}

		/**
		 * Method to request the ID for the collaboration of the currently
		 * edited video.
		 * 
		 * Sends a http post request to the server, supporting the server
		 * adress, port, user name and password. Gets an http response back from
		 * the server, containing the ID.
		 */
		private void requestID() {
			String serverAdress = HTTP + server + COLON + port + PATH;

			HttpClient httpClient = new DefaultHttpClient();

			HttpPost httpPost = new HttpPost(serverAdress);

			// Generate the parameters for the request
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair(PARAMETER_USER, user));
			parameters
					.add(new BasicNameValuePair(PARAMETER_PASSWORD, password));
			parameters.add(new BasicNameValuePair(PARAMETER_PROJECT,
					projectName));
			
			// MessageBox used for errors while sending the request
	        MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ERROR | SWT.OK);
	        messageBox.setText(Messages.Collaboration_Error_Title);

			try {
				httpPost.setEntity(new UrlEncodedFormEntity(parameters));

				HttpResponse response = httpClient.execute(httpPost);

				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));

				// Response consists of only one line that contains a number
				String line = reader.readLine();

				// Get the projectID from the response and
				// set it in the project settings
				// A negative int is returned,
				// when the registration has failed, a positive ID integer else.
				int projectID = Integer.parseInt(line);

				if (projectID > -1) {
					projectCollaborationID = projectID;
					collaborationID.setText(Messages.Collaboration_Current_ID
							+ projectCollaborationID);
				} else {
			        messageBox.setMessage(Messages.Collaboration_Error_RU);
			        messageBox.open();
				}

				// Release all used system resources
				EntityUtils.consume(entity);
				reader.close();
				content.close();
			} catch (UnsupportedEncodingException e) {
		        messageBox.setMessage(Messages.Collaboration_Error_UEE);
		        messageBox.open();
//				e.printStackTrace();
			} catch (ClientProtocolException e) {
		        messageBox.setMessage(Messages.Collaboration_Error_CPE);
		        messageBox.open();
//				e.printStackTrace();
			} catch (HttpHostConnectException e) {
				messageBox.setMessage(Messages.Collaboration_Error_HHCE);
		        messageBox.open();
//				e.printStackTrace();
			} catch (UnknownHostException e) {
				messageBox.setMessage(Messages.Collaboration_Error_UHE);
		        messageBox.open();
//				e.printStackTrace();	
			} catch (IOException e) {
		        messageBox.setMessage(Messages.Collaboration_Error_IO);
		        messageBox.open();
//				e.printStackTrace();
			} finally {
				httpPost.releaseConnection();
			}
		}
	}

	/**
	 * Sets the text for the collaboration ID Label. When no ID is defined yet,
	 * it is set to 0 in the model. "Undefined" is then display instead of 0.
	 * 
	 * @param ID
	 *            The ID that is to be set for the Label.
	 */
	private void defineCollaborationID(int ID) {
		if (ID == 0) {
			this.collaborationID.setText(Messages.Collaboration_Current_ID
					+ UNDEFINED);
		} else {
			this.collaborationID
					.setText(Messages.Collaboration_Current_ID + ID);
		}
	}

	public void init() {
		reload = false;
		reloadTime = 0;
		projectName = ""; //$NON-NLS-1$
		setField();
		notifyListeners(SWT.Modify, new Event());
	}

	public void setField() {
		settingFields = true;
		relCheck.setSelection(reload);
		relText.setText(String.valueOf(reloadTime));
		proNameText.setText(projectName);
		defineCollaborationID(projectCollaborationID);
		settingFields = false;
	}

	private VerifyListener verifyInput() {
		return new VerifyListener() {

			@Override
			public void verifyText(VerifyEvent e) {
				if (settingFields) {
					e.doit = true;
					return;
				}

				e.doit = false;
				char myChar = e.character;
				// Allow 0-9
				if (Character.isLetter(myChar))
					e.doit = false;
				if (Character.isDigit(myChar))
					e.doit = true;

				// Allow backspace
				if (myChar == '\b' || myChar == SWT.DEL)
					e.doit = true;
			}
		};
	}
}
