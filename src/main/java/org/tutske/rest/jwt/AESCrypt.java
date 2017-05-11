package org.tutske.rest.jwt;

import static org.tutske.rest.jwt.JsonWebToken.Keys.*;

import org.tutske.rest.jwt.JsonWebToken.Keys;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class AESCrypt implements Crypt {

	private static final String HASH_NAME = "SHA-256";
	private static final String HMAC_ALG = "HmacSHA256";
	private static final String AES_KEY_NAME = "AES";
	private static final String AES_SCHEME = "AES/CTR/PKCS5Padding";

	private final Random random = new SecureRandom ();
	private final SecretKeySpec secret;

	public AESCrypt (String key) {
		this (hash (key.getBytes ()));
	}

	public AESCrypt (byte [] key) {
		this (new SecretKeySpec (key, HMAC_ALG));
	}

	public AESCrypt (SecretKeySpec secret) {
		this.secret = secret;
	}

	private static byte [] hash (byte [] data) {
		try { return MessageDigest.getInstance (HASH_NAME).digest (data); }
		catch ( NoSuchAlgorithmException not_possible ) { return new byte [] {}; }
	}

	public byte [] sign (byte [] data) {
		return getMac (secret).doFinal (data);
	}

	public JsonWebToken sign (JsonWebToken token) {
		Mac mac = getMac (secret);
		for ( Keys k : Keys.values () ) {
			if ( k == Authentication ) { continue; }
			mac.update (token.get (k));
		}
		return token.with (Authentication, mac.doFinal ());
	}

	public boolean validate (byte [] data, byte [] signature) {
		return Arrays.equals (sign (data), signature);
	}

	public boolean validate (JsonWebToken token) {
		return Arrays.equals (sign (token).get (Authentication), token.get (Authentication));
	}

	public JsonWebToken encrypt (JsonWebToken token) {
		try {
			IvParameterSpec iv = new IvParameterSpec (generateBytes (16), 0, 16);
			SecretKeySpec shared = new SecretKeySpec (generateBytes (16), AES_KEY_NAME);
			SecretKeySpec secret = new SecretKeySpec (this.secret.getEncoded (), 0, 16, AES_KEY_NAME);

			Cipher key = Cipher.getInstance (AES_SCHEME);
			key.init (Cipher.ENCRYPT_MODE, secret, iv);

			Cipher payload = Cipher.getInstance (AES_SCHEME);
			payload.init (Cipher.ENCRYPT_MODE, shared, iv);

			return token.with (Iv, iv.getIV ())
				.with (EncryptedKey, key.doFinal (shared.getEncoded ()))
				.with (Payload, payload.doFinal (token.get (Payload)));
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}

	public JsonWebToken decrypt (JsonWebToken token) {
		try {
			IvParameterSpec iv = new IvParameterSpec (token.get (Iv), 0, 16);
			SecretKeySpec secret = new SecretKeySpec (this.secret.getEncoded (), 0, 16, AES_KEY_NAME);

			Cipher key = Cipher.getInstance (AES_SCHEME);
			key.init (Cipher.DECRYPT_MODE, secret, iv);

			SecretKeySpec shared = new SecretKeySpec (key.doFinal (token.get (EncryptedKey)), AES_KEY_NAME);

			Cipher payload = Cipher.getInstance (AES_SCHEME);
			payload.init (Cipher.DECRYPT_MODE, shared, iv);

			return token.with (Payload, payload.doFinal (token.get (Payload)));
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}

	private Mac getMac (SecretKeySpec key) {
		try {
			Mac mac = Mac.getInstance (HMAC_ALG);
			mac.init (key);
			return mac;
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}

	private byte [] generateBytes (int length) {
		byte [] next = new byte [length];
		random.nextBytes (next);
		return next;
	}

}
