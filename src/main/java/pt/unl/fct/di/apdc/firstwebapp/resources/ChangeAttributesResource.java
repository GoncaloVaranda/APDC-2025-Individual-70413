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
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.resources.UserAttributeLister;


@Path("/changeattributes")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8") 
public class ChangeAttributesResource {

	private static final Logger LOG = Logger.getLogger(ChangeAttributesResource.class.getName()); 
	private final Gson g = new Gson();

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	public ChangeAttributesResource() {} //nothing to be done here @GET

	
	    @POST
	    @Path("/")
	    @Consumes(MediaType.APPLICATION_JSON)
	    public Response changeAttribute(ChangeAttributesData data) {
	    	
	    	Transaction txn = datastore.newTransaction(); 
	    	Key authtokenId = datastore.newKeyFactory().setKind("Token").newKey(data.tokenId);
	        Entity authtoken = txn.get(authtokenId);
	        
	        
	        Key targetUserKey = userKeyFactory.newKey(data.username);
	        Entity targetUser = txn.get(targetUserKey);
	 
	        
	        if(targetUser == null) {
	        	return Response.status(Status.FORBIDDEN)
						.entity("User not found").build();
	        }
	       
	        
			if (authtoken == null ) {
				return Response.status(Status.FORBIDDEN)
						.entity("Authentication Token not valid")
						.build();
			}
			
			
			//invalid attribute check
			if (!data.attributeType.equals("username") && !data.attributeType.equals("email") && !data.attributeType.equals("fullname") &&
					!data.attributeType.equals("phone") && !data.attributeType.equals("profile") && !data.attributeType.equals("citizenCardNumber") &&
					!data.attributeType.equals("role") && !data.attributeType.equals("userNif") && !data.attributeType.equals("employer") &&
					!data.attributeType.equals("job") && !data.attributeType.equals("address") && !data.attributeType.equals("employerNif") &&
					!data.attributeType.equals("accountState") ) {
				return Response.status(Status.FORBIDDEN)
						.entity("Invalid Attribute. Has to be either username, email, fullname, phone, profile, citizenCardNumber, role, \n  userNif, employer, job, address, employerNif, accountState ")
						.build();
			}
			
			if (data.newValue == null || data.newValue.isBlank()) {
				return Response.status(Status.FORBIDDEN)
						.entity("Invalid new value")
						.build();
			}
				
			
			String targetUserRole = targetUser.getString("user_role");
			String targetUserEmail = targetUser.getString("user_email");
			
			// Verificar a validade do token
		    long currentTime = System.currentTimeMillis();
		    long tokenExpiryTime = authtoken.getLong("validity_to");
		    if (currentTime > tokenExpiryTime) {
		        return Response.status(Status.FORBIDDEN)
		                .entity("Authentication Token has expired")
		                .build();
		    }
		    
		    if(data.attributeType.equals("accountState")) {
		    	if(!(data.newValue.equals("deactivated") || data.newValue.equals("activated")
		        		|| data.newValue.equals("suspended") )) {
		        	return Response.status(Status.FORBIDDEN)
							.entity("Invalid Accout State").build();
		        }
		    }
		    
		    if(data.attributeType.equals("profile")) {
		    	if(!(data.newValue.equals("private") || data.newValue.equals("public"))) {
		        	return Response.status(Status.FORBIDDEN)
							.entity("Invalid Profile Type").build();
		        }
		    }
		    
		    if(data.attributeType.equals("role")) {
		    	if(!(data.newValue.equals("backoffice") || data.newValue.equals("partner")
		        		|| data.newValue.equals("admin") || data.newValue.equals("enduser"))) {
		        	return Response.status(Status.FORBIDDEN)
							.entity("Invalid Role").build();
		        }
		    }
		    
	    	

		    String tokenRole = authtoken.getString("user_role");
		    String tokenUsername = authtoken.getString("user_name");
		    
		    Key tokenUserKey = userKeyFactory.newKey(tokenUsername);
		    Entity tokenEnt = txn.get(tokenUserKey);
		    
		    String tokenAccountState = tokenEnt.getString("user_accountState");
		    
		    if (tokenRole.equals("partner")) {
	            return Response.status(Response.Status.FORBIDDEN).entity("You don't have permission to change attributes").build();
	        }
		    
		    
	        if (tokenRole.equals("enduser")) {
	        	if(data.attributeType.equals("username") || data.attributeType.equals("email") 
	        			|| data.attributeType.equals("fullname") || data.attributeType.equals("accountState")
	        			|| data.attributeType.equals("role") || !tokenUsername.equals(data.username)) {
	        		return Response.status(Response.Status.FORBIDDEN).entity("You don't have permission to change those attributes").build();
	        	}  
	        }

      
	        // Verificar as permissões de troca de estado da conta
	        if (tokenRole.equals("backoffice")) {
	        	if( !tokenAccountState.equals("activated")) {
	        		return Response.status(Response.Status.FORBIDDEN).entity("Your Backoffice account is not activated").build();
	        	}
	            if(targetUserRole.equals("admin") || targetUserRole.equals("backoffice")) {
	            	return Response.status(Response.Status.FORBIDDEN).entity("The Backoffice role does not have permission to change that role's attributes").build();
	            }
	            if (data.attributeType.equals("username") || data.attributeType.equals("email")) {
	                return Response.status(Response.Status.FORBIDDEN).entity("The Backoffice role does not have permission to change the email or usarname").build();
	            }
	        } 
	        
	        
	        String updatedAttributeType = "user_" + data.attributeType;
	        Key userEmailNewKey = datastore.newKeyFactory().setKind("Email").newKey(data.newValue);
	        Key userEmailOldKey = datastore.newKeyFactory().setKind("Email").newKey(targetUserEmail);
	        Entity emailNew= txn.get(userEmailNewKey);
	        Entity emailOld= txn.get(userEmailOldKey);
	        Entity userNew= null;
	        Entity tokenNewEnt= null;
	        boolean changeUsername = data.attributeType.equals("username");
	        boolean changeEmail = data.attributeType.equals("email");
	        if(!changeUsername) { 
	        	if(changeEmail) { //caso o atributo a alterar seja o email
	        		
	        		if(emailNew!=null) 	return Response.status(Response.Status.FORBIDDEN).entity("Email already in use").build();
		        	
	        		
	        		emailNew = Entity.newBuilder(userEmailNewKey).set("user_name", data.username).build();
	        		
	        	}
	        }else {  //caso o atributo a alterar seja o username
		        	
		        	Key userNewKey = datastore.newKeyFactory().setKind("User").newKey(data.newValue);
		        	userNew = txn.get(userNewKey);
		        	
		        	if(userNew!=null) return Response.status(Response.Status.FORBIDDEN).entity("Username already in use").build();
		        	
		        	UserAttributeLister lister = new UserAttributeLister(data.username);
		        	String[] arr = lister.listAttributes(data.username);
		        	
		        	Timestamp time = targetUser.getTimestamp("user_creation_time");
		        	
		        	userNew = Entity.newBuilder(userNewKey).set("user_email", arr[0]).set("user_fullname", arr[1])
							.set("user_phone", arr[2]).set("user_pwd", arr[3])
							.set("user_profile", arr[4]).set("user_citizenCardNumber", arr[5])
							.set("user_role", arr[6])
							.set("user_userNif", arr[7]).set("user_employer", arr[8])
							.set("user_job", arr[9]).set("user_address", arr[10])
							.set("user_employerNif", arr[11]).set("user_accountState", arr[12])
							.set("user_creation_time", time) .build();
					
					
		        	//mudar o username do token
					tokenNewEnt = Entity.newBuilder(authtoken)
		                    .set("user_name", data.newValue)  
		                    .build();
					
					//mudar o username do token
					emailOld = Entity.newBuilder(emailOld)
		                    .set("user_name", data.newValue)  
		                    .build();
	        
	        	
	        }
	      
	        try {
		        if(!changeUsername) {
		        	if(changeEmail) {  //caso o atributo a alterar seja o email
		        		txn.delete(userEmailOldKey);
	        			txn.put(emailNew);
		        	}
		        	
		        	Entity updatedUser = Entity.newBuilder(targetUser)
		                    .set(updatedAttributeType, data.newValue)  
		                    .build();
		        	txn.put(updatedUser);
		            txn.commit();
		        }else { //case o atributo a alterar seja o username
		     
					txn.delete(targetUserKey); //apagar user antigo
					txn.put(tokenNewEnt);
					txn.put(emailOld);
	        		txn.put(userNew);
		            txn.commit();
		        }
	        	
	            return Response.ok().entity("Attribute changed with success").build();
	        } catch (DatastoreException e) {
	            txn.rollback();
	            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error in the process of altering attribute"	).build();
	        } finally {
	            if (txn.isActive()) {
	                txn.rollback();
	            }
	        }
	    }
	    
	        
	
	
}