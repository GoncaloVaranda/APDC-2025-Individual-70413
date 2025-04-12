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

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeAttributesData;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangeRoleData;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;


@Path("/changerole")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8") 
public class ChangeRoleResource {

	private static final Logger LOG = Logger.getLogger(ChangeRoleResource.class.getName()); 
	private final Gson g = new Gson();

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	public ChangeRoleResource() {} //nothing to be done here @GET

	
	    @POST
	    @Path("/")
	    @Consumes(MediaType.APPLICATION_JSON)
	    public Response changeRole(ChangeRoleData data) {
		 
	    	
	    	Transaction txn = datastore.newTransaction(); 
	    	Key authtokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.tokenId);
	        Entity authtoken = txn.get(authtokenKey);
	        
	        
	        // Verificar a role do utilizador alvo da alteração
	        Key targetUserKey = userKeyFactory.newKey(data.username);
	        Entity targetUser = txn.get(targetUserKey);
	        String updatedRole = data.role;
	        
	        if(!(updatedRole.equals("partner") || updatedRole.equals("backoffice")
	        		|| updatedRole.equals("admin") || updatedRole.equals("enduser"))) {
	        	return Response.status(Status.FORBIDDEN)
						.entity("Invalid Role").build();
	        }
	        
	        
	        if(targetUser == null) {
	        	return Response.status(Status.FORBIDDEN)
						.entity("User not found").build();
	        }
	        
	        String targetUserRole = targetUser.getString("user_role");
	        
	        
			if (authtoken == null ) {
				return Response.status(Status.FORBIDDEN)
						.entity("Authentication Token not valid").build();
			}
			
			
			
			
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
	    	


	        // Verificar se o utilizador tem permissões para alterar o role
		    String authUserRole = authUser.getString("user_role");
	        if (authUserRole.equals("enduser") || authUserRole.equals("partner")) {
	            return Response.status(Response.Status.FORBIDDEN).entity("You don't have permission to change roles").build();
	        }

	        
	        
	        
	        // Verificar as permissões de troca de roles do backoffice
	        if (authUserRole.equals("backoffice") ) {
	        	if(updatedRole.equals("partner") || updatedRole.equals("enduser")) {
	        		return Response.status(Response.Status.FORBIDDEN).entity("The Backoffice role does not have permission to change that role").build();
	        	}
	            // O BACKOFFICE pode trocar o role de ENDUSER para PARTNER ou vice-versa
	            if (!(targetUserRole.equals("partner") || targetUserRole.equals("enduser"))) {
	                return Response.status(Response.Status.FORBIDDEN).entity("The Backoffice role does not have permission to change that role").build();
	            }
	        }
	        
	        // Atualizar o role do utilizador
	
	        try {
	            Entity updatedUser = Entity.newBuilder(targetUser)
	                    .set("user_role", data.role)  // Alterar o role
	                    .build();

	            txn.put(updatedUser);
	            txn.commit();

	            return Response.ok().entity("Role changed with success").build();
	        } catch (DatastoreException e) {
	            txn.rollback();
	            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error in the process of altering role").build();
	        } finally {
	            if (txn.isActive()) {
	                txn.rollback();
	            }
	        }
	    }
	    
	    
	
	
}