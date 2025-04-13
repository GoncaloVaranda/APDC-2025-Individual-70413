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
import com.google.protobuf.Timestamp;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeAttributesData;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangePasswordData;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;


@Path("/changepassword")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8") 
public class ChangePasswordResource {

	private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName()); 
	private final Gson g = new Gson();

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	public ChangePasswordResource() {} //nothing to be done here @GET

	
	    @POST
	    @Path("/")
	    @Consumes(MediaType.APPLICATION_JSON)
	    public Response changeRole(ChangePasswordData data) {
		 
	    	
	    	Transaction txn = datastore.newTransaction(); 
	    	Key authtokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.tokenId);
	        Entity authtoken = txn.get(authtokenKey);
	        
			if (authtoken == null ) {
				return Response.status(Status.FORBIDDEN)
						.entity("Authentication Token not valid")
						.build();
			}
			
			String username = authtoken.getString("user_name");
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
	        Entity user = txn.get(userKey);
	        
	        if(user == null) {
	        	return Response.status(Status.FORBIDDEN)
						.entity("User not found").build();
	        }
			
			// Verificar a validade do token
		    long currentTime = System.currentTimeMillis();
		    long tokenExpiryTime = authtoken.getLong("validity_to");
		    if (currentTime > tokenExpiryTime) {
		        return Response.status(Status.FORBIDDEN).entity("Authentication Token has expired").build();
		    }
	        
		    String hashedCurrentPWD = (String) user.getString("user_pwd");
		    
		    if(!data.newPassword.equals(data.confirmNewPassword)) {
		    	return Response.status(Status.FORBIDDEN).entity("Password confirmation failed. Not the same password").build();
		    }
		    
		    if(!hashedCurrentPWD.equals(DigestUtils.sha512Hex(data.currentPassword))) {
		    	return Response.status(Status.FORBIDDEN).entity("Current Password is incorrect").build();
		    }
		    
		    String hashedNewPWD = DigestUtils.sha512Hex(data.newPassword);
		    
		    if(hashedCurrentPWD.equals(hashedNewPWD)) {
		    	return Response.status(Status.FORBIDDEN).entity("New password is the same as the old one").build();
		    }
	        
	        try {
	            Entity updatedUser = Entity.newBuilder(user)
	                    .set("user_pwd", hashedNewPWD)  // Alterar o role
	                    .build();

	            txn.put(updatedUser);
	            txn.commit();

	            return Response.ok().entity("Password changed with success").build();
	        } catch (DatastoreException e) {
	            txn.rollback();
	            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error in the process of altering password").build();
	        } finally {
	            if (txn.isActive()) {
	                txn.rollback();
	            }
	        }
	    }
	    
}