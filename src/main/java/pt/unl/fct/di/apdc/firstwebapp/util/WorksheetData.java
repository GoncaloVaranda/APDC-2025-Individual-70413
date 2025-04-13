package pt.unl.fct.di.apdc.firstwebapp.util;	

public class WorksheetData {
	
	private static final String ACCOUNT_STATE_DEACTIVATED= "deactivated";
	private static final String ACCOUNT_STATE_ACTIVATED= "activated";
	private static final String ACCOUNT_STATE_SUSPENDED= "suspended";
	private static final String ROLE_ENDUSER= "enduser";
	private static final String ROLE_BACKOFFICE= "backoffice";
	private static final String ROLE_ADMIN= "admin";
	private static final String ROLE_PARTNER= "partner";
	private static final String PROFILE_PUBLIC= "public";
	private static final String PROFILE_PRIVATE= "private";
	
	public String workId;
	public String description;
	public String type;
	public String adjudication;
	public String phone;
	public String profile;
	public String citizenCardNumber;
	public String role;
	public String userNif;
	public String employer;
	public String job;
	public String address;
	public String employerNif;
	public String accountState;
	
	
	public WorksheetData() {
		
	}
	
	public WorksheetData(String email, String username, String fullname,String phone, String password, 
										String confirmation , String profile, String citizenCardNumber , String role,
										String userNif, String employer, String job, String address, String employerNif,
										String accountState) {
		this.email = email.trim();
		this.username = username.trim();
		this.fullname = fullname.trim();
		this.password = password;
		this.confirmation = confirmation;
		this.phone = phone.trim();
		this.profile = profile;
		this.citizenCardNumber = citizenCardNumber ; 
		this.role = role;
		this.userNif = userNif; 
		this.employer = employer; 
		this.job = job; 
		this.address = address; 
		this.employerNif = employerNif;
		this.accountState = accountState;
		
	}
	
	private boolean nonEmptyOrBlankField(String field) {
		return field != null && !field.isBlank();
	}
	
	public boolean validRegistration() {
		 	
		return nonEmptyOrBlankField(username) &&
			   nonEmptyOrBlankField(fullname) &&
			   nonEmptyOrBlankField(password) &&
			   nonEmptyOrBlankField(phone) &&
			   nonEmptyOrBlankField(email) &&
			   nonEmptyOrBlankField(profile) &&
			   validProfile()&&
			   validEmail() &&
			   validRole() &&
			   validAccountState() &&
			   password.equals(confirmation);
			   
	}

	public boolean validEmail() {
		return email.contains("@");
	}
	
	public boolean validProfile() {
		return profile.equals(PROFILE_PUBLIC) || profile.equals(PROFILE_PRIVATE);
	}
	
	public boolean validRole() {
		if(role.isBlank() || role == null) role=ROLE_ENDUSER;
		return role.equals(ROLE_ENDUSER) || role.equals(ROLE_BACKOFFICE)
				|| role.equals(ROLE_ADMIN) || role.equals(ROLE_PARTNER);
	}
	
	public boolean validAccountState() {
		if(accountState.isBlank() || accountState == null) accountState = ACCOUNT_STATE_DEACTIVATED;
		return accountState.equals(ACCOUNT_STATE_ACTIVATED) || accountState.equals(ACCOUNT_STATE_DEACTIVATED) || accountState.equals(ACCOUNT_STATE_SUSPENDED);
	}
	
}
