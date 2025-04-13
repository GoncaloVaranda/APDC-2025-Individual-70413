package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.io.IOException;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;
import java.util.ArrayList;

import org.apache.commons.codec.digest.DigestUtils;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import com.google.cloud.tasks.v2.*;
import com.google.cloud.tasks.v2.HttpMethod;
import com.google.gson.Gson;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeAccountStateData;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeAttributesData;
import pt.unl.fct.di.apdc.firstwebapp.util.ListUsersData;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.resources.UserAttributeLister;




@Path("/listusers")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8") 
public class ListUsersResources {

	private static final Logger LOG = Logger.getLogger(ListUsersResources.class.getName()); 
	private final Gson g = new Gson();

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	public ListUsersResources() {} //nothing to be done here @GET

	
	    @POST
	    @Path("/")
	    @Consumes(MediaType.APPLICATION_JSON)
	    public Response changeAccountState(ListUsersData data) {
	    	
	    	Transaction txn = datastore.newTransaction(); 
	    	Key authtokenId = datastore.newKeyFactory().setKind("Token").newKey(data.tokenId);
	        Entity authtoken = txn.get(authtokenId);
	        
	        
			if (authtoken == null ) {
				return Response.status(Status.FORBIDDEN)
						.entity("Authentication Token not valid")
						.build();
			}
			
			// Verificar a validade do token
		    long currentTime = System.currentTimeMillis();
		    long tokenExpiryTime = authtoken.getLong("validity_to");
		    if (currentTime > tokenExpiryTime) {
		        return Response.status(Status.FORBIDDEN)
		                .entity("Authentication Token has expired")
		                .build();
		    }
		    
		    String tokenRole = authtoken.getString("user_role");
		    if(tokenRole.equals("partner")) {
		    	return Response.status(Status.FORBIDDEN)
		                .entity("Partners are not allowed to list users")
		                .build();
		    }
		    
		    Query<Key> query = Query.newKeyQueryBuilder().setKind("User").build();
		    QueryResults<Key> userKeysIt = datastore.run(query);
	    	
            

	    	ArrayList<String> attributeList=new ArrayList<String>();
	    	while(userKeysIt.hasNext()) {
	    		Key currentUserKey = userKeysIt.next();
	    		Entity currentUser = txn.get(currentUserKey);
	    		
	    		Key emailKey = datastore.newKeyFactory().setKind("Email").newKey(currentUser.getString("user_email"));
	    		Entity email = txn.get(emailKey);
	    		String username = email.getString("user_name");
	    		
	    		
	    		String userEmail = currentUser.getString("user_email");
	        	String userFullname = currentUser.getString("user_fullname");
	        	String userPhone = currentUser.getString("user_phone");
	    		String userProfile = currentUser.getString("user_profile");
	    		String userCitizenCardNumber = currentUser.getString("user_citizenCardNumber");	
	    		String userRole = currentUser.getString("user_role");
	    		String userNif = currentUser.getString("user_userNif");
	        	String userEmployer = currentUser.getString("user_employer");
	        	String userJob = currentUser.getString("user_job");
	        	String userAddress = currentUser.getString("user_address");
	        	String userEmployerNif = currentUser.getString("user_employerNif");
	        	String userState = currentUser.getString("user_accountState");
	        	
	        	String[] attArr = {userEmail,userFullname,userPhone,userProfile,userCitizenCardNumber,
	        			userRole,userNif,userEmployer,userJob,userAddress,userEmployerNif,userState};
	        	int i = 0;
	        	while(i<attArr.length) {
	        		if(attArr[i] == null || attArr[i].isBlank()) {
	        			attArr[i] = "NOT DEFINED";
	        		}
	        		i++;
	        	}
	    		
	    		
	    		if(tokenRole.equals("enduser") && userRole.equals("enduser") 
	    				&& userProfile.equals("public") && userState.equals("activated")) {
	    			attributeList.add(username + "-> " + attArr[0] + ", " + attArr[1]);
	    		}else if(tokenRole.equals("backoffice") && userRole.equals("enduser") ) {
	    			attributeList.add(username + "-> " + attArr[0] + ", " + attArr[1] + ", " + attArr[2] +
	    					 ", " + attArr[3] +  ", " + attArr[4] + ", " + attArr[5] + ", " + attArr[6] +
	    					 ", " + attArr[7] +  ", " + attArr[8] + ", " + attArr[9] + ", " + attArr[10] +
	    				     ", " + attArr[11]);
	    		}else {
	    			attributeList.add(username + "-> " + attArr[0] + ", " + attArr[1] + ", " + attArr[2] +
	    					 ", " + attArr[3] +  ", " + attArr[4] + ", " + attArr[5] + ", " + attArr[6] +
	    					 ", " + attArr[7] +  ", " + attArr[8] + ", " + attArr[9] + ", " + attArr[10] +
	    				     ", " + attArr[11]);
	    		}
	    			
	    	}
	    	 
		    
	      
	        try {
	        	Iterator<String> stringIt = attributeList.iterator();
	        	String response = "";
	        	while(stringIt.hasNext()) {
	        		response = response + "\n" + stringIt.next();
	        	}
	            return Response.ok().entity(response).build();
	        } catch (DatastoreException e) {
	            txn.rollback();
	            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error in the process of listing user attributes").build();
	        } finally {
	            if (txn.isActive()) {
	                txn.rollback();
	            }
	        }
	    }
	    
	        
	
	
}