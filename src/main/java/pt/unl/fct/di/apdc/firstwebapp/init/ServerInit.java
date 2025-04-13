package pt.unl.fct.di.apdc.firstwebapp.init;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.ws.rs.core.Response;

@WebListener
public class ServerInit implements ServletContextListener{
	
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
		Transaction txn = datastore.newTransaction(); 
		
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey("root");
	        Entity user = txn.get(userKey);
	        if(user==null) {
	        	Entity userNew = Entity.newBuilder(userKey).set("user_email", "root@.com").set("user_fullname", "Goncalo V")
						.set("user_phone", "933011233").set("user_pwd", DigestUtils.sha512Hex("goncalo"))
						.set("user_profile", "private").set("user_citizenCardNumber", "13542563")
						.set("user_role", "admin")
						.set("user_userNif", "493498321").set("user_employer", "fct")
						.set("user_job", "estudante").set("user_address", "Rua Almeida Santos da Silva 2E lote 12")
						.set("user_employerNif", "125674325").set("user_accountState", "activated")
						.set("user_creation_time", Timestamp.now()).build();
				
				Key userEmailNewKey = datastore.newKeyFactory().setKind("Email").newKey("root@.com");
				Entity userEmailNew = Entity.newBuilder(userEmailNewKey).set("user_name", "root").build();
		        	
				txn.put(userNew);
				txn.put(userEmailNew);
	            txn.commit();
	        }else {
	        	txn.rollback();
	        }
	        
        } catch (DatastoreException e) {
            txn.rollback();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
		
	}

}
