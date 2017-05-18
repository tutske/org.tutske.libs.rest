package org.tutske.rest.jwt;


public interface Crypt {

	public byte [] sign (byte [] data);
	public JsonWebToken sign (JsonWebToken token);
	public boolean validate (JsonWebToken token);
	public JsonWebToken encrypt (JsonWebToken token);
	public JsonWebToken decrypt (JsonWebToken token);

}
