package hu.controller.videos;

import hu.backingbeans.users.UserBean;
import hu.backingbeans.users.UserListBean;
import hu.backingbeans.users.UserListBean.UserListEntryBean;
import hu.backingbeans.videos.VideoPublicationBean;
import hu.backingbeans.videos.UsersInvitedToVideoBean;
import hu.backingbeans.videos.VideoPublicationBean.TokenListEntryBean;
import hu.controller.AController;
import hu.controller.users.EUserListAction;
import hu.model.EParticipationRestriction;
import hu.model.Group;
import hu.model.Video;
import hu.model.Token;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IGroupStore;
import hu.persistence.IPersistenceProvider;
import hu.persistence.ITokenStore;
import hu.persistence.IUserStore;
import hu.persistence.InconsistencyException;
import hu.persistence.IVideoStore;
import hu.util.Configuration;
import hu.util.MailService;
import hu.util.SecurityUtils;
import hu.util.SessionData;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

/**
 * This class provides the functionality to edit the publication settings of a
 * {@link Video} and invite {@link User}s to participate in a
 * {@link Video}.
 */
@ManagedBean
@RequestScoped
public class VideoPublicationAction extends AController {
    private static final String CFG_MAX_ROWS = "max_rows_per_table";
    
    private static final String MSG_TOKEN_MAIL_SUCCESS = "token_mail_success";
    private static final String MSG_TOKEN_MAIL_SUBJECT = "token_mail_subject";
    private static final String MSG_TOKEN_MAIL_MESSAGE = "token_mail_message";
    private static final String MSG_UNINVITED = "inviteUser_uninvited";
    private static final String MSG_NO_ONE_INVITED = "inviteUser_no_one_invited";
    private static final String MSG_NOT_INVITED = "inviteUser_not_invited";
    private static final String MSG_INVITED = "inviteUser_invited";
    private static final String MSG_ALREADY_INVITED = "inviteUser_already_invited";
    private static final String MSG_TOKEN_ADDED_FAILURE = "generateToken_failure";
    
    private static final String LIST_USERS_TABLE = "listUsersTable";
    private static final String EMAIL_PATTERN = 
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-.]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final String RESTRICTION_ERROR_FACELET = "/xhtml/errors/restrictionError";
    
    private static final int AMOUNT_OF_COLUMNS_IN_TOKEN_LIST = 5;

    @ManagedProperty("#{videoPublicationBean}")
    private VideoPublicationBean videoPublicationBean;

    @ManagedProperty("#{userListBean}")
    private UserListBean userListBean;

    @ManagedProperty("#{usersInvitedToVideoBean}")
    private UsersInvitedToVideoBean invitedUsersBean;

    @ManagedProperty("#{PersistenceProvider}")
    private IPersistenceProvider persistenceProvider;

    @ManagedProperty("#{mailService}")
    private MailService mailService;
    
    @ManagedProperty("#{sessionData}")
    private SessionData session;
    
    @ManagedProperty("#{userBean}")
    private UserBean userBean;
    
    @ManagedProperty("#{configuration}")
    private Configuration configuration;
    
    /**
     * @param configuration the configuration to set
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Set {@link UserBean} using injection.
     * 
     * @param userBean to inject.
     */
    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    /**
     * Set {@link VideoPublicationBean} using injection.
     * 
     * @param videoPublicationBean
     *            to inject.
     */
    public void setVideoPublicationBean(
            VideoPublicationBean videoPublicationBean) {
        this.videoPublicationBean = videoPublicationBean;
    }

    /**
     * Set {@link UserListBean} using injection.
     * 
     * @param userListBean
     *            to inject.
     */
    public void setUserListBean(UserListBean userListBean) {
        this.userListBean = userListBean;
    }

    /**
     * Set {@link UsersInvitedToVideoBean} using injection.
     * 
     * @param invitedUsersBean
     *            to inject.
     */
    public void setInvitedUsersBean(
            UsersInvitedToVideoBean invitedUsersBean) {
        this.invitedUsersBean = invitedUsersBean;
    }

    /**
     * Set {@link IPersistenceProvider} using injection for database access.
     * 
     * @param persistenceProvider
     *            to inject.
     */
    public void setPersistenceProvider(IPersistenceProvider persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
    }

    /**
     * Set {@link MailService} using injection.
     * 
     * @param mailService
     *            to inject.
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * Set {@link SessionData} using injection.
     * 
     * @param session
     *          to inject.
     */
    public void setSession(SessionData session) {
        this.session = session;
    }

    /**
     * Show a list of {@link User}s which can be invited or uninvited from a
     * {@link Video}.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor}, who are owners of the {@link Video},
     * have permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String manageInvitations() {
        IGroupStore groupStore = this.persistenceProvider.getGroupStore();
        IUserStore userStore = this.persistenceProvider.getUserStore();
        IVideoStore videoStore = this.persistenceProvider
                .getVideoStore();

        // Check if group and video exists or id is not null, redirect
        // if not.
        if (videoPublicationBean.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        // check if video publication is "token"
        Video video = videoStore.getById(videoPublicationBean
                .getId());

        if (!(video.getParticipationRestriction()
                .equals(EParticipationRestriction.Token))) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;

        }
        
        Group group = groupStore
                .getForVideo(videoPublicationBean.getId());
        if (group == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        // Fetch the owners and attendants of the group.
        Set<User> owners = new HashSet<User>(userStore.getByOwnership(group));
        Set<User> attendants = new HashSet<User>();

        // get invited to display
        if (invitedUsersBean.getInvitedUsers() != null
                && invitedUsersBean.getInvitedUsers().get(
                        videoPublicationBean.getId()) != null) {

            Set<Integer> set = new HashSet<Integer>(invitedUsersBean
                    .getInvitedUsers()
                    .get(videoPublicationBean.getId()));

            // attendants with new added
            for (Integer i : set) {
                attendants.add(userStore.getById(i));
            }
        }
        // Check if current user is allowed to use this page
        User currentUser = null;
        if (this.session.getUserId() != null) {
            currentUser = userStore.getById(this.session.getUserId());
        }
        if (currentUser == null
                || (currentUser.getUserType() != EUserType.Administrator && !owners
                        .contains(currentUser))) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // Do not run if a search was submitted.
        if (!this.getCurrentFcInstance().isPostback()) {
            // Perform add/remove operation and update list.
            if (this.userListBean.getAction() == EUserListAction.add) {

                // check if user to add exist
                if (userBean.getId() != null) {
                    if (userStore.findById(userBean.getId()) == null) {
                        this.redirectTo(RESTRICTION_ERROR_FACELET);
                        return null;
                    }
                }
                this.inviteUser();

                Set<Integer> set = new HashSet<Integer>(invitedUsersBean
                        .getInvitedUsers().get(
                                videoPublicationBean.getId()));

                // attendants with new added
                for (Integer i : set) {
                    attendants.add(userStore.getById(i));
                }
            } else if (this.userListBean.getAction() == EUserListAction.remove) {
                // check if user to add exist
                if (userBean.getId() != null) {
                    if (userStore.findById(userBean.getId()) == null) {
                        this.redirectTo(RESTRICTION_ERROR_FACELET);
                        return null;
                    }
                }
                this.uninviteUser();
                Set<Integer> set = new HashSet<Integer>(invitedUsersBean
                        .getInvitedUsers().get(
                                videoPublicationBean.getId()));

                // attendants with new removed
                if (set.isEmpty()) {
                    attendants.clear();
                } else {
                    for (Integer i : set) {
                        attendants.clear();
                        attendants.add(userStore.getById(i));
                    }
                }
            }
        }

        // get invited to display
        if (invitedUsersBean.getInvitedUsers() != null
                && invitedUsersBean.getInvitedUsers().get(
                        videoPublicationBean.getId()) != null) {
            Set<Integer> set = new HashSet<Integer>(invitedUsersBean
                    .getInvitedUsers()
                    .get(videoPublicationBean.getId()));

            // attendants with new added
            for (Integer i : set) {
                attendants.add(userStore.getById(i));
            }
        }

        // Go to first page on new search request.
        if (this.getCurrentFcInstance().isPostback()) {
            this.userListBean.setPage(0);
        }

        // Fetch users if no search query was entered.
        final int maxEntries = this.configuration.getInteger(CFG_MAX_ROWS);
        List<User> users;
        int numUsers;
        if (this.userListBean.getSearchQuery().equals("")) {
            users = userStore.getAll(this.userListBean.getSortColumn(),
                    this.userListBean.getSortDirection(),
                    this.userListBean.getUserType(),
                    this.userListBean.getPage() * maxEntries, maxEntries);

            numUsers = userStore.getCountOfAll(this.userListBean.getUserType());

            // Fetch users for an entered search query.
        } else {
            users = userStore.search(this.userListBean.getSearchQuery(),
                    this.userListBean.getSortColumn(),
                    this.userListBean.getSortDirection(),
                    this.userListBean.getUserType(),
                    this.userListBean.getPage() * maxEntries, maxEntries);

            numUsers = userStore.getCountOfSearch(
                    this.userListBean.getSearchQuery(),
                    this.userListBean.getUserType());
        }
        // Calculate number of pages.
        int pages = numUsers / maxEntries
                + ((numUsers % maxEntries != 0) ? 1 : 0);
        this.userListBean.setPages(pages);

        // Populate list of users to be displayed.
        List<UserListEntryBean> list = new ArrayList<UserListEntryBean>();
        for (User user : users) {
            UserListEntryBean entry = new UserListEntryBean();
            entry.setUser(user);

            // Determine which buttons to show.
            boolean isAttendant = attendants.contains(user);
            entry.setAdded(isAttendant);
            entry.setRemovable(isAttendant);
            entry.setAddable(!isAttendant);

            list.add(entry);
        }
        this.userListBean.setList(list);

        return null;
    }

    /**
     * Invite a {@link User} to a {@link Video}.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor}, who are owners of the {@link Video},
     * have permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String inviteUser() {
        Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
        Set<Integer> set = new HashSet<Integer>();

        IUserStore userStore = this.persistenceProvider.getUserStore();
        EUserType userType = userStore.getById(session.getUserId())
                .getUserType();

        IGroupStore groupStore = this.persistenceProvider.getGroupStore();
        Group group = groupStore
                .getForVideo(videoPublicationBean.getId());
        List<Group> groups = groupStore.getByOwner(session.getUserId());

        // permission check
        if (userType != EUserType.Administrator && !groups.contains(group)) {
            return RESTRICTION_ERROR_FACELET;
        }

        // some users already invited
        if (invitedUsersBean.getInvitedUsers() != null) {
            map = invitedUsersBean.getInvitedUsers();
            if(invitedUsersBean
                    .getInvitedUsers().get(
                            videoPublicationBean.getId()) != null) {
            set = map.get(videoPublicationBean.getId());
            }
            if (set.contains(userBean.getId())) {
                this.sendGlobalMessageToFacelet(this
                        .getCommonMessage(MSG_ALREADY_INVITED));
                return null;
            }
        }
        set.add(userBean.getId());
        map.put(videoPublicationBean.getId(), set);
        invitedUsersBean.setInvitedUsers(map);
        this.sendMessageTo(LIST_USERS_TABLE,
                (this.getCommonMessage(MSG_INVITED)));
        return null;
    }

    /**
     * Void the invitation of a {@link User} to a {@link Video}.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor}, who are owners of the {@link Video},
     * have permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String uninviteUser() {
        Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
        Set<Integer> set = new HashSet<Integer>();

        IUserStore userStore = this.persistenceProvider.getUserStore();
        EUserType userType = userStore.getById(session.getUserId())
                .getUserType();

        IGroupStore groupStore = this.persistenceProvider.getGroupStore();
        Group group = groupStore
                .getForVideo(videoPublicationBean.getId());
        List<Group> groups = groupStore.getByOwner(session.getUserId());

        // permission check
        if (userType != EUserType.Administrator && !groups.contains(group)) {
            return RESTRICTION_ERROR_FACELET;
        }
        

        // are there invited users
        if (invitedUsersBean.getInvitedUsers() != null) {
            map = invitedUsersBean.getInvitedUsers();

        } else {
            this.sendGlobalMessageToFacelet(this
                    .getCommonMessage(MSG_NO_ONE_INVITED));
            return null;
        }
        set = map.get(videoPublicationBean.getId());

        // is the user invited
        if (!set.contains(userBean.getId())) {
            this.sendGlobalMessageToFacelet(this
                    .getCommonMessage(MSG_NOT_INVITED));
            return null;
        }

        set.remove(userBean.getId());
        map.put(videoPublicationBean.getId(), set);
        invitedUsersBean.setInvitedUsers(map);

        this.sendMessageTo(LIST_USERS_TABLE,
                (this.getCommonMessage(MSG_UNINVITED)));
        return null;
    }
    
    /**
     * Send the invitation to a {@link Video} to a {@link User} with a
     * mail.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor}, who are owners of the {@link Video},
     * have permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String sendToken() {
        
        IUserStore userStore = this.persistenceProvider.getUserStore();
        EUserType userType = userStore.getById(session.getUserId())
                .getUserType();
        IGroupStore groupStore = this.persistenceProvider.getGroupStore();
        Group group = groupStore
                .getForVideo(videoPublicationBean.getId());
        List<Group> groups = groupStore.getByOwner(session.getUserId());
        
        //get context path
        String completePath = buildContextPath();
        
        if(invitedUsersBean.getInvitedUsers() == null ) {
            this.sendGlobalMessageToFacelet(
                    this.getCommonMessage(MSG_NO_ONE_INVITED));
            return null;
        }
        
        // permission check
        if (userType != EUserType.Administrator && !groups.contains(group)) {
            return RESTRICTION_ERROR_FACELET;
        }

        Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
        Set<Integer> set = new HashSet<Integer>();
        List<Token> token = new LinkedList<Token>();
        Map<String, String> tokenMail = new HashMap<String, String>();

        
        // get the invited users
        map = invitedUsersBean.getInvitedUsers();
        set = map.get(videoPublicationBean.getId());

        // set the number of token to generate
        videoPublicationBean.setNumTokens(set.size());
        videoPublicationBean.setTokenLength(32);
        generateParticipationToken();

        token = videoPublicationBean.getTokens();
        Iterator<Token> it = token.iterator();

        
        // add the mail address if each user with a token to the map
        for (Integer i : set) {
            User user = (userStore.getById(i));
            tokenMail.put(user.getEmail(), it.next().getToken());

        }
        
        // send mails to the whole map
        sendToMap(completePath, tokenMail);
        return null;
    }

    /**
     * Send email's with url's and tokens to the given addresse.
     * 
     * @param completePath
     *            to the page.
     * @param tokenMail
     *            is a map with String pairs, the email address and the token.
     */
    private void sendToMap(String completePath, Map<String, String> tokenMail) {
        String msg = MessageFormat.format(
                this.getCommonMessage(MSG_TOKEN_MAIL_MESSAGE), completePath);
        this.mailService.sendMultipleMail(tokenMail,
                this.getCommonMessage(MSG_TOKEN_MAIL_SUBJECT), msg);
        this.sendMessageTo(LIST_USERS_TABLE,
                this.getCommonMessage(MSG_TOKEN_MAIL_SUCCESS));
    }

    /**
     * Build the context path of the web application.
     * 
     * @return the context path of the web application.
     */
    private String buildContextPath() {
        FacesContext ctx = this.getCurrentFcInstance();
        String path = ctx.getExternalContext().getRequestContextPath();
        String host = ctx.getExternalContext().getRequestServerName();
        String protocol = ctx.getExternalContext().getRequestScheme();
        String port = Integer.toString(ctx.getExternalContext().getRequestServerPort());
        String completePath = protocol+"://"+host+":"+port+path;
        return completePath;
    }
    
    /**
     * Get separate email addresses from the entered {@link String}.
     * 
     * @param emailList to set.
     * @return a list of email addresses.
     */
    private List<String> trimMailString(String emailList) {
        String[] mailArray = emailList.split("\n");
        
        List<String> mails = new LinkedList<String>();
        
        for (String s: mailArray) {
            String trimmed = s.trim();
            trimmed = trimmed.replace("\n", "");
            trimmed = trimmed.replace("\r", "");
            
            mails.add(trimmed);
        }
        return mails;
        
    }
    
    /**
     * Send tokens to the specified list of email addresses.
     * 
     * @return the destination for redirecting.
     */
    public String sendToList() {
        IUserStore userStore = this.persistenceProvider.getUserStore();
        EUserType userType = userStore.getById(session.getUserId())
                .getUserType();

        IGroupStore groupStore = this.persistenceProvider.getGroupStore();
        Group group = groupStore
                .getForVideo(videoPublicationBean.getId());
        List<Group> groups = groupStore.getByOwner(session.getUserId());

        // get context path
        String completePath = buildContextPath();

        // are there typed in addresses
        if (videoPublicationBean.getEmailList().equals("")) {
            this.sendGlobalMessageToFacelet(this
                    .getCommonMessage(MSG_NO_ONE_INVITED));
            return null;
        }

        // permission check
        if (userType != EUserType.Administrator && !groups.contains(group)) {
            return RESTRICTION_ERROR_FACELET;
        }

        List<Token> token = new LinkedList<Token>();
        Map<String, String> tokenMail = new HashMap<String, String>();

        // split string to single mail addresses
        List<String> mails = trimMailString(videoPublicationBean
                .getEmailList());
        
        // set the number of token to generate
        videoPublicationBean.setNumTokens(mails.size());
        videoPublicationBean.setTokenLength(32);
        generateParticipationToken();

        token = videoPublicationBean.getTokens();
        Iterator<Token> it = token.iterator();

        // add the mail address of each user with a token to the map
        for (String s : mails) {
        	
            Pattern pattern = Pattern.compile(EMAIL_PATTERN);
            Matcher matcher = pattern.matcher(s);

            if (!matcher.matches()) {
            	this.sendGlobalMessageToFacelet(this
                        .getCommonMessage("inviteUser_email_wrong_format"));
                return null;
            }
        

            tokenMail.put(s, it.next().getToken());
        }

        // send to whole map
        sendToMap(completePath, tokenMail);
        return null;
    }
    
    /**
     * Send tokens to all attendants of the group.
     * 
     * @return the destination for redirecting.
     */
    public String sendToAll() {
        IUserStore userStore = this.persistenceProvider.getUserStore();
        EUserType userType = userStore.getById(session.getUserId())
                .getUserType();

        IGroupStore groupStore = this.persistenceProvider.getGroupStore();
        Group group = groupStore
                .getForVideo(videoPublicationBean.getId());
        List<Group> groups = groupStore.getByOwner(session.getUserId());

        List<Token> token = new LinkedList<Token>();
        Map<String, String> tokenMail = new HashMap<String, String>();

        // retrieve all users for the group
        List<User> attendants = userStore.getByAttendance(group);

        // get context path
        String completePath = buildContextPath();

        // permission check
        if (userType != EUserType.Administrator && !groups.contains(group)) {
            return RESTRICTION_ERROR_FACELET;
        }

        // set the number of token to generate
        videoPublicationBean.setNumTokens(attendants.size());
        videoPublicationBean.setTokenLength(32);
        generateParticipationToken();

        token = videoPublicationBean.getTokens();
        Iterator<Token> it = token.iterator();

        // add the mail address if each user with a token to the map
        for (User u : attendants) {

            tokenMail.put(u.getEmail(), it.next().getToken());

        }

        // send to whole map
        sendToMap(completePath, tokenMail);
        return null;
    }

    /**
     * Generate {@link Token} for participating in a {@link Video}.
     * 
     * @return the next Page to show as {@link String}
     */
    public String generateParticipationToken() {
        IUserStore uStore = this.persistenceProvider.getUserStore();
        ITokenStore tStore = this.persistenceProvider.getTokenStore();
        IVideoStore videoStore = this.persistenceProvider.getVideoStore();
        VideoPublicationBean qpb = this.videoPublicationBean;

        FacesContext fcxt = this.getCurrentFcInstance();
        
        Video video = null;
        
        // Verify that a video id is provided.
        if (qpb.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        //verify that video is valid
        video = videoStore.findById(qpb.getId());
        
        if (video == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // Fetch the current user to do authentication checks.
        User currentUser = null;
        
        if (this.session.getUserId() != null) {
            currentUser = uStore.findById(this.session.getUserId());
            
            if (currentUser == null) {
                this.redirectTo(RESTRICTION_ERROR_FACELET);
                return null;
            }
        }

        // Current User must own the video or be admin
        // group.
        if (!uStore.isUserOwnerOfVideo(currentUser.getId(),
                        qpb.getId()) && currentUser.getUserType() != EUserType.Administrator){
            
            //user is not priviliged to generate Token
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        //check if video is tokenRestricted
        if (video.getParticipationRestriction() != EParticipationRestriction.Token) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        //if only authentication check is done while preRender quit here
        if (!fcxt.isPostback() && !fcxt.isValidationFailed()) {
            return null;
        }
        
        List<Token> tokens = new LinkedList<Token>();
        
        // Generate Token and save to database
        for (int i = 1; i <= qpb.getNumTokens(); i++) {
            String random = null;

            //token must be unique
            while (random == null || tStore.find(random) != null) {
                random = SecurityUtils.randomString(qpb.getTokenLength());
            }

            Token token = new Token(random);
            token.setForParticipation(true);

            try {
                token = tStore.create(token, qpb.getId());
                tokens.add(token);
            } catch (InconsistencyException ignored) {
            }
        }

        //prepare tokenListEntryBean for viewing the token in a multicolumn table.
        TokenListEntryBean entry;
        List<TokenListEntryBean> tokenOutputList = new LinkedList<TokenListEntryBean>();
        
        for (int i = 0; i < tokens.size();) {
            entry = new TokenListEntryBean();
            Token[] columns = new Token[AMOUNT_OF_COLUMNS_IN_TOKEN_LIST];

            for (int j = 0; j < AMOUNT_OF_COLUMNS_IN_TOKEN_LIST; i++, j++) {
                if (i < tokens.size()) {
                    columns[j] = tokens.get(i);
                } else {
                    columns[j] = new Token("");
                }
            }
            entry.setColumns(columns);
            tokenOutputList.add(entry);
        }
        this.videoPublicationBean.setTokenOutputList(tokenOutputList);

        //populate bean with tokens
        if (tokens.size() == qpb.getNumTokens()) {
            qpb.setTokens(tokens);
            qpb.setTokenAvailable(true);
        } else {
            this.sendGlobalMessageToFacelet(this
                    .getCommonMessage(MSG_TOKEN_ADDED_FAILURE));
        }
        return null;
    }
    
    /**
     * Save the id of the {@link Video} from the viewparam to a hidden
     * field.
     * 
     * @return always null.
     */
    public String recoverId() {
        Integer videoId = null;
        if (videoPublicationBean.getId() != null) {
            videoId = videoPublicationBean.getId();
        } else if (videoPublicationBean.getHiddenIdValue() != null) {
            try {
                videoId = Integer
                        .parseInt((String) videoPublicationBean
                                .getHiddenIdValue());
            } catch (NumberFormatException ignored) {
                
            }
        }
        videoPublicationBean.setId(videoId);
        videoPublicationBean.setHiddenIdValue(videoId);

        return null;
    }
}
