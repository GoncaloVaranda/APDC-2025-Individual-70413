package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeRoleData {

	public String tokenId;
    public String username;
    public String role;
    
    public ChangeRoleData(){
    	
    }
    
    public ChangeRoleData(String tokenId, String username, String role) {
    	this.tokenId = tokenId;
    	this.username = username;
    	this.role = role;
    }


    
}