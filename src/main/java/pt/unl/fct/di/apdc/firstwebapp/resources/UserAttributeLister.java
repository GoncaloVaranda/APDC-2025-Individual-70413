package pt.unl.fct.di.apdc.firstwebapp.resources;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.Timestamp;

public class UserAttributeLister {
	public String username;
	
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	UserAttributeLister(String username){
		this.username = username;
	}
	
	
	public String[] listAttributes(String username) {
		
		Transaction txn = datastore.newTransaction(); 
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
        Entity user = txn.get(userKey);
        if(user==null)return null;
		
		String email = user.getString("user_email");
    	String fullname = user.getString("user_fullname");
    	String phone = user.getString("user_phone");
    	String password = (String) user.getString("user_pwd");
    	String profile = user.getString("user_profile");
    	String citizenCardNumber = user.getString("user_citizenCardNumber");
    	String role = user.getString("user_role");
    	String userNif = user.getString("user_userNif");
    	String employer = user.getString("user_employer");
    	String job = user.getString("user_job");
    	String address = user.getString("user_address");
    	String employerNif = user.getString("user_employerNif");
    	String accountState = user.getString("user_accountState");
    	
    	String[] attributeArr = {email,fullname,phone,password,profile,citizenCardNumber,role,userNif,employer,job,address
    							,employerNif,accountState};
    	txn.rollback();
    	
    	return attributeArr;
	}
	

}
