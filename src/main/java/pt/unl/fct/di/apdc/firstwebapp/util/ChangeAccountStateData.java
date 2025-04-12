package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAccountStateData {

	public String tokenId;
    public String accountState;
    public String username;
    
    
    public ChangeAccountStateData(){
    	
    }
    
    public ChangeAccountStateData(String tokenId, String username, String accountState) {
    	this.tokenId = tokenId;
    	this.accountState = accountState;
    	this.username = username;
    }


    
}