package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

@Path("/register")
public class RegisterResource {

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private final Gson g = new Gson();


	public RegisterResource() {}	// Default constructor, nothing to do
	
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerUser(RegisterData data) {
		LOG.fine("Attempt to register user: " + data.username);

		if (!data.validRegistration()) {
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
		}

		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);
			Key userEmailKey = datastore.newKeyFactory().setKind("Email").newKey(data.email);
			Entity userEmail = txn.get(userEmailKey);
			
			// If the entity does not exist null is returned
			if (user != null || userEmail != null) {
				txn.rollback();
				return Response.status(Status.CONFLICT).entity("Username or Email already exists.").build();
			} else {
				
				Entity userNew = Entity.newBuilder(userKey).set("user_email", data.email).set("user_fullname", data.fullname)
						.set("user_phone", data.phone).set("user_pwd", DigestUtils.sha512Hex(data.password))
						.set("user_profile", data.profile).set("user_citizenCardNumber", data.citizenCardNumber)
						.set("user_role", data.role)
						.set("user_userNif", data.userNif).set("user_employer", data.employer)
						.set("user_job", data.job).set("user_address", data.address)
						.set("user_employerNif", data.employerNif).set("user_accountState", data.accountState)
						.set("user_creation_time", Timestamp.now()).build();
				
				Key userEmailNewKey = datastore.newKeyFactory().setKind("Email").newKey(data.email);
				Entity userEmailNew = Entity.newBuilder(userEmailNewKey).set("user_name", data.username).build();
				
				
				// get() followed by put() inside a transaction is ok...
				txn.put(userNew,userEmailNew);
				txn.commit();
				LOG.info("User registered " + data.username);
				return Response.ok().entity("User Created with success").build();
			}
		}
		catch (DatastoreException e) {
			if (txn.isActive()) {
				txn.rollback();
			}
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}	

	
}
