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

import pt.unl.fct.di.apdc.firstwebapp.util.ChangeAccountStateData;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeAttributesData;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RemoveUserData;


@Path("/removeuser")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8") 
public class RemoveUserResource {

	private static final Logger LOG = Logger.getLogger(RemoveUserResource.class.getName()); 
	private final Gson g = new Gson();

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	public RemoveUserResource() {} //nothing to be done here @GET

	
	    @POST
	    @Path("/")
	    @Consumes(MediaType.APPLICATION_JSON)
	    public Response removeUser(RemoveUserData data) {
	    	
	    	Transaction txn = datastore.newTransaction(); 
	    	Key authtokenId = datastore.newKeyFactory().setKind("Token").newKey(data.tokenId);
	        Entity authtoken = txn.get(authtokenId);
	        
	        
	        Key targetUserKey = userKeyFactory.newKey(data.user);
	        Entity targetUser = txn.get(targetUserKey);
	        Key targetUserEmailKey = datastore.newKeyFactory().setKind("Email").newKey(data.user);
	        Entity targetUserEmail = txn.get(targetUserEmailKey);
	        
	        
	        
	        // Se houver email E username, dá prioridade ao username
	        if(targetUser == null) {
	        	if(targetUserEmail==null) {
	        		return Response.status(Status.FORBIDDEN)
							.entity("User not found").build();
	        	}else {
	        		targetUserKey = userKeyFactory.newKey(targetUserEmail.getString("user_name"));
	        		targetUser = txn.get(targetUserKey);
	        	}
	        }
	        
	        
			if (authtoken == null ) {
				return Response.status(Status.FORBIDDEN)
						.entity("Authentication Token not valid")
						.build();
			}
			
			
			
			String targetUserRole = targetUser.getString("user_role");
			
			// Verificar a validade do token
		    long currentTime = System.currentTimeMillis();
		    long tokenExpiryTime = authtoken.getLong("validity_to");
		    if (currentTime > tokenExpiryTime) {
		        return Response.status(Status.FORBIDDEN)
		                .entity("Authentication Token has expired")
		                .build();
		    }
		    
		    
		    // Obter o utilizador autenticado
		    String authUserName = authtoken.getString("user_name");
		    Key authUserKey = userKeyFactory.newKey(authUserName);
			Entity authUser = txn.get(authUserKey);


	        // Verificar se o utilizador tem permissões para remover conta
		    String authUserRole = authUser.getString("user_role");
	        if (authUserRole.equals("enduser") || authUserRole.equals("partner")) {
	            return Response.status(Response.Status.FORBIDDEN).entity("You don't have permission to remove users").build();
	        }

      
	        
	        if (authUserRole.equals("backoffice")) {
	            if(targetUserRole.equals("admin") || targetUserRole.equals("backoffice")) {
	            	return Response.status(Response.Status.FORBIDDEN).entity("The Backoffice role does not have permission to remove admin or backoffice users").build();
	            }
	        } 
	        
	        // Remover o utilizador
	
	        try {
	        	Key usersRespectiveEmailKey = datastore.newKeyFactory().setKind("Email").newKey(targetUser.getString("user_email"));
	            txn.delete(targetUserKey,usersRespectiveEmailKey);
	            txn.commit();

	            return Response.ok().entity("User removed with success").build();
	        } catch (DatastoreException e) {
	            txn.rollback();
	            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error in the process of removing user").build();
	        } finally {
	            if (txn.isActive()) {
	                txn.rollback();
	            }
	        }
	    }
	    
	    
	
	
}