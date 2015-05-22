package hu.controller.common;

import hu.backingbeans.common.LoginBean;
import hu.backingbeans.users.AuthenticatedUserBean;
import hu.controller.AController;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IPersistenceProvider;
import hu.persistence.IUserStore;
import hu.util.ELocale;
import hu.util.SecurityUtils;
import hu.util.SessionData;

import java.io.IOException;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.servlet.http.HttpServletRequest;


/**
 * This class provides the login and logout functionality which allows users to
 * authenticate themselves and manage their session.
 */
@ManagedBean
@RequestScoped
public class AuthenticationAction extends AController {
    private static final String LOGIN_FACELET = "/xhtml/users/login";
    private static final String LOGOUT_FACELET = "/xhtml/users/logout";
    private static final String START_FACELET = "/xhtml/common/start";
    
    private static final String MSG_LOGIN_BANNED = "login_banned";
    private static final String MSG_LOGIN_FAILED = "login_failed";
    
    @ManagedProperty("#{loginBean}")
    private LoginBean loginBean;

    @ManagedProperty("#{authenticatedUserBean}")
    private AuthenticatedUserBean authenticatedUserBean;

    @ManagedProperty("#{PersistenceProvider}")
    private IPersistenceProvider persistenceProvider;

    @ManagedProperty("#{sessionData}")
    private SessionData session;

    /**
     * Set {@link LoginBean} using injection.
     * @param loginBean
     *            to inject.
     */
    public void setLoginBean(LoginBean loginBean) {
        this.loginBean = loginBean;
    }

    /**
     * Set {@link AuthenticatedUserBean} using injection.
     * @param authenticatedUserBean
     *            to inject.
     */
    public void setAuthenticatedUserBean(
            AuthenticatedUserBean authenticatedUserBean) {
        this.authenticatedUserBean = authenticatedUserBean;
    }

    /**
     * Set {@link IPersistenceProvider} using injection for database access.
     * @param persistenceProvider
     *            to inject.
     */
    public void setPersistenceProvider(IPersistenceProvider persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
    }

    /**
     * Set {@link SessionData} using injection.
     * @param session
     *            to inject.
     */
    public void setSession(SessionData session) {
        this.session = session;
    }

    /**
     * 
     * Check if the credentials provided by the user are valid and that the
     * {@link User} is not banned.
     * Only user of {@link EUserType#Anonymous} have permission to use.
     * @return the next page to show as {@link String}.
     * @throws IOException 
     */
    public String login() throws IOException {
        IUserStore userStore = this.persistenceProvider.getUserStore();

        User user = userStore.findByEmail(this.loginBean.getUsername());
        String hash = SecurityUtils.hash(loginBean.getPassword());

        if (user != null && user.isBanned()) {
            
            this.sendGlobalMessageToFacelet(this.getCommonMessage(MSG_LOGIN_BANNED));
            return LOGIN_FACELET; 
        }
        if (user != null && hash.equals(user.getPasswordHash())) {
            this.session.setUserId(user.getId());
            this.session.setUserType(user.getUserType());
            if(this.loginBean.getRedirectURL() == null || this.loginBean.getRedirectURL().equals("") || this.loginBean.getRedirectURL().contains("login") || this.loginBean.getRedirectURL().contains("logout") || this.loginBean.getRedirectURL().contains("register")){
        	return START_FACELET;
            }
            else{
        	this.getCurrentFcInstance().getExternalContext().redirect(this.loginBean.getRedirectURL());
        	return null;
            }
        }
  
        this.sendGlobalMessageToFacelet(this.getCommonMessage(MSG_LOGIN_FAILED));

        return LOGIN_FACELET;
    }

    /**
     * Set the actual {@link User} from {@link SessionData} in
     * {@link AuthenticatedUserBean}. Set also the {@link User}'s locale if it is not set yet.
     */
    public void prePopulateUsername() {
    	
    	// Get the user's data if he is logged in
        if (this.session.getUserId() != null) {
            IUserStore userStore = this.persistenceProvider.getUserStore();
            User user = userStore.getById(session.getUserId());
            authenticatedUserBean.setUser(user);
        }
        
        // Try to find mobile browser name in user agent to set mobile version of page 
        String userAgent;
        if(!this.isJUnitTest())
            userAgent = ((HttpServletRequest)this.getCurrentFcInstance().getExternalContext().getRequest()).getHeader("user-agent").toLowerCase();
        else
            userAgent = "";
        if(userAgent.matches("(android|bb\\d+|meego).+mobile|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino")
        		|| userAgent.matches("1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\\-(n|u)|c55\\/|capi|ccwa|cdm\\-|cell|chtm|cldc|cmd\\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\\-s|devi|dica|dmob|do(c|p)o|ds(12|\\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\\-|_)|g1 u|g560|gene|gf\\-5|g\\-mo|go(\\.w|od)|gr(ad|un)|haie|hcit|hd\\-(m|p|t)|hei\\-|hi(pt|ta)|hp( i|ip)|hs\\-c|ht(c(\\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\\-(20|go|ma)|i230|iac( |\\-|\\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\\/)|klon|kpt |kwc\\-|kyo(c|k)|le(no|xi)|lg( g|\\/(k|l|u)|50|54|\\-[a-w])|libw|lynx|m1\\-w|m3ga|m50\\/|ma(te|ui|xo)|mc(01|21|ca)|m\\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\\-2|po(ck|rt|se)|prox|psio|pt\\-g|qa\\-a|qc(07|12|21|32|60|\\-[2-7]|i\\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\\-|oo|p\\-)|sdk\\/|se(c(\\-|0|1)|47|mc|nd|ri)|sgh\\-|shar|sie(\\-|m)|sk\\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\\-|v\\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\\-|tdg\\-|tel(i|m)|tim\\-|t\\-mo|to(pl|sh)|ts(70|m\\-|m3|m5)|tx\\-9|up(\\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\\-|your|zeto|zte\\-")){
            this.session.setMobile(true);
        }
        
        // set the user's locale if not set
        if(this.session.getLocale() == null){
        	
            // Try to get the locale from the browser
            Locale locale = null;
            if(!this.isJUnitTest()){
        	locale = this.getCurrentFcInstance().getViewRoot().getLocale();
            }
            if(locale != null){
        	changeLocale(locale.toString().split("_")[0]);
            }
        	
            // Set locale to 'en' if retrieving the locale from the browser was not successful
            if(this.session.getLocale() == null){
        	changeLocale("en");
            }
        }
        if(!this.isJUnitTest()){
            this.getCurrentFcInstance().getViewRoot().setLocale(new Locale(this.session.getLocale().toString()));
        }
    }

    /**
     * End the session of a {@link User}.
     * Only {@link User}s have permission to use.
     * @return the next page to show as {@link String}.
     */
    public String logout() {

        if (this.session != null && this.session.getUserId() != null) {
            this.session.setUserId(null);
            return LOGOUT_FACELET + "?faces-redirect=true";
        }

        return START_FACELET + "?faces-redirect=true";
    }
    
    /**
     * Set the {@link User}'s locale.
     * 
     * @param locale to set.
     * 
     * @return an empty String to redirect again to current page.
     */
    public String changeLocale(String locale){
    	for(ELocale l: ELocale.values()){
    		if(l.toString().equalsIgnoreCase(locale)){
    			this.session.setLocale(l);
    		}
    	}
    	return "";
    }
}