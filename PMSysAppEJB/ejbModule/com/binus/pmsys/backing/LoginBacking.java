package com.binus.pmsys.backing;

import java.io.Serializable;
import java.util.Arrays;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.codec.binary.Hex;

import com.binus.pmsys.entity.Staff;
import com.binus.pmsys.rules.LoginRules;
import com.binus.pmsys.utils.HashHelper;
import com.binus.pmsys.utils.SessionManager;

@Named
@RequestScoped
public class LoginBacking extends BasicBacking implements Serializable {
	private static final long serialVersionUID = 1988449267982435477L;
	
	@EJB
	private LoginRules rules;
	
	@Inject
	private UserBacking userSession;
	
	private String username;
	private char[] password;
	private String temppassword;
	
	public LoginBacking() { }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTemppassword() {
		return temppassword;
	}

	public void setTemppassword(String temppassword) {
		password = Hex.encodeHexString(HashHelper.getHash(temppassword.toCharArray(), rules.getUserSalt(username).getBytes() , 40000, 256), false).toCharArray();
		temppassword = null;
	}
	
	private void clearInputs() {
		username = null;
		Arrays.fill(password, (Character) '0');
		password = null;
	}
	
	public String loginUser() {
		String redirect = null;
		FacesContext context = FacesContext.getCurrentInstance();
		
		try {
			Staff user = null;
			
			if(!user.equals(null) || user == null) { //TODO: Verify properly
				userSession.init(user);
				SessionManager.recordSession(context, user.getsID());
				redirect = "";
			} else {
				//TODO: MESSAGE
				redirect = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			clearInputs();
		}
		
		return redirect;
	}

	public String doLogout() {
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		return "/menu.xhtml?faces-redirect=true";
	}
}
