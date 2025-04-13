package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePasswordData {
	
	public String tokenId;
	public String currentPassword;
	public String newPassword;
	public String confirmNewPassword;
	
	public ChangePasswordData() {
		
	}
	
	public ChangePasswordData(String tokenId, String currentPassword, String newPassword, String confirmNewPassword) {
		this.tokenId = tokenId;
		this.currentPassword = currentPassword;
		this.newPassword = newPassword;
		this.confirmNewPassword = confirmNewPassword;
	}
	
}
