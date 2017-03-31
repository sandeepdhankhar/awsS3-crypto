package com.frolicdev.crypto;

import java.security.Key;
import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

/**
 * 
 * @author sdhankar
 *
 */
public class SecurityUtil {

    public static Key generateKeyFromString(final String secKey, String algorithm) throws Exception {

	final byte[] keyVal = Base64.getDecoder().decode(secKey.getBytes());
	final Key key = new SecretKeySpec(keyVal, algorithm);
	return key;
    }

    public static Key generateKeyFromString(final byte[] secKey, String algorithm) throws Exception {

	final Key key = new SecretKeySpec(secKey, algorithm);
	return key;
    }
}
