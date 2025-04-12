package pt.unl.fct.di.apdc.firstwebapp.util;

public class RemoveUserData {

	public String tokenId;
    public String user;
    
    
    public RemoveUserData(){
    	
    }
    
    public RemoveUserData(String tokenId, String user) {
    	this.tokenId = tokenId;
    	this.user = user;
    }


    
}