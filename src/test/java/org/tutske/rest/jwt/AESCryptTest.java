package org.tutske.rest.jwt;

import javax.crypto.spec.SecretKeySpec;


public class AESCryptTest extends CryptTest {

	@Override
	protected Crypt cryptFor (SecretKeySpec key) {
		return new AESCrypt (key);
	}

}
