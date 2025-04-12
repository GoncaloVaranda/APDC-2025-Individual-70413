package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAttributesData {

	public String tokenId;
    public String username;
    public String attributeType;
    public String newValue;
    
    public ChangeAttributesData(){
    	
    }
    
    public ChangeAttributesData(String tokenId, String username, String attributeType, String newValue) {
    	this.tokenId = tokenId;
    	this.username = username;
    	this.attributeType = attributeType;
    	this.newValue = newValue;
    }


    
}