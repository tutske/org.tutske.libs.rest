package org.tutske.rest.jwt;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import javax.crypto.spec.SecretKeySpec;


public abstract class CryptTest {

	protected abstract Crypt cryptFor (SecretKeySpec key);

	private static class Auth {
		public final String sub = "jhon.doe@example.com";
	}

	private SecretKeySpec key = new SecretKeySpec ("test".getBytes (), "HmacSHA256");
	private Crypt crypt = cryptFor (key);
	private Auth auth = new Auth ();
	private JsonWebToken token = JsonWebToken.fromData (auth);

	@Test
	public void it_should_sign_a_token () {
		crypt.sign (token);
	}

	@Test
	public void it_should_say_unsigned_tokens_are_invalid () {
		assertThat (crypt.validate (token), is (false));
	}

	@Test
	public void it_should_validate_signed_tokens () {
		JsonWebToken signed = crypt.sign (token);
		assertThat (crypt.validate (signed), is (true));
	}

	@Test
	public void it_should_not_validate_tokens_signed_with_a_different_key () {
		JsonWebToken signed = crypt.sign (token);
		SecretKeySpec key = new SecretKeySpec ("other".getBytes (), "HmacSHA256");
		assertThat (cryptFor(key).validate (signed), is (false));
	}

	@Test
	public void it_should_create_a_token_from_a_string_representation () {
		JsonWebToken signed = crypt.sign (token);
		JsonWebToken duplicate = JsonWebToken.fromString (signed.toString ());

		assertThat (duplicate.getPayload (Auth.class), not (nullValue ()));
		assertThat (duplicate.getPayload (Auth.class).sub, is (auth.sub));
	}

}
