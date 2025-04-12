package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.UUID;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000*60*60*2;
	
	public String username;
	public String tokenID;
	public String role;
	public long validity_from;
	public long validity_to;
	
	public AuthToken() {

	}
	
	public AuthToken(String username, String role) {
		this.username = username;
		this.role = role;
		this.tokenID = UUID.randomUUID().toString();
		this.validity_from = System.currentTimeMillis();
		this.validity_to = this.validity_from + EXPIRATION_TIME;
	}
	
	public boolean authIsValid() {
		long currentinstant = System.currentTimeMillis();
		return currentinstant<validity_to;
	}
	
}
