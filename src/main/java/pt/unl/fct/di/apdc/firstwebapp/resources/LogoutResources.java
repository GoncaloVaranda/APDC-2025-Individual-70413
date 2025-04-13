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

import com.google.gson.Gson;


import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;



import pt.unl.fct.di.apdc.firstwebapp.util.LogoutData;


@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8") 
public class LogoutResources {

	private static final Logger LOG = Logger.getLogger(LogoutResources.class.getName()); 
	private final Gson g = new Gson();

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

	private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

	public LogoutResources() {} //nothing to be done here @GET

	
	    @POST
	    @Path("/")
	    @Consumes(MediaType.APPLICATION_JSON)
	    public Response changeRole(LogoutData data) {
		 
	    	
	    	Transaction txn = datastore.newTransaction(); 
	    	Key authtokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.tokenId);
	        Entity authtoken = txn.get(authtokenKey);
	        
			if (authtoken == null ) {
				return Response.status(Status.FORBIDDEN)
						.entity("You are already logged out")
						.build();
			}
			
			
			// Verificar a validade do token
		    long currentTime = System.currentTimeMillis();
		    long tokenExpiryTime = authtoken.getLong("validity_to");
		    if (currentTime > tokenExpiryTime) {
		        return Response.status(Status.FORBIDDEN).entity("You are already logged out, your session expired").build();
		    }
	        
		    
	        try {
	            txn.delete(authtokenKey);
	            txn.commit();

	            return Response.ok().entity("Logged out with success").build();
	        } catch (DatastoreException e) {
	            txn.rollback();
	            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error in the process of logging out").build();
	        } finally {
	            if (txn.isActive()) {
	                txn.rollback();
	            }
	        }
	    }
	    
}