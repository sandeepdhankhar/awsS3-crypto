package com.frolicdev.crypto;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * 
 * @author sdhankar
 *
 */
public class SymmetricCrypto {

    public static final String AES_ALGORITHM = "AES";
    public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    public SymmetricCrypto(String keyStr, String algorithm, String cipherAlgorithm) throws Exception {

	Key key = SecurityUtil.generateKeyFromString(keyStr, algorithm);
	init(key, cipherAlgorithm);

    }

    private void init(Key key, String cipherAlgorithm) throws Exception {
	byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	IvParameterSpec ivspec = new IvParameterSpec(iv);

	encryptCipher = Cipher.getInstance(cipherAlgorithm);

	encryptCipher.init(Cipher.ENCRYPT_MODE, key, ivspec);

	decryptCipher = Cipher.getInstance(cipherAlgorithm);

	decryptCipher.init(Cipher.DECRYPT_MODE, key, ivspec);
    }

    public SymmetricCrypto(byte[] keyStr, String algorithm, String cipherAlgorithm) throws Exception {

	Key key = SecurityUtil.generateKeyFromString(keyStr, algorithm);

	init(key, cipherAlgorithm);

    }

    private Cipher encryptCipher;
    private Cipher decryptCipher;

    public byte[] encrypt(byte[] bytes) throws Exception {
	return encryptCipher.doFinal(bytes);
    }

    public byte[] decrypt(byte[] bytes) throws Exception {

	return decryptCipher.doFinal(bytes);
    }

    public String encrypt(String input) throws Exception {

	return Base64.getEncoder().encodeToString(encrypt(input.getBytes()));
    }

    public String decrypt(String input) throws Exception {

	return new String(decrypt(Base64.getDecoder().decode(input)));
    }

    public static String encrypt(String keyStr, String input, String algorithm) throws Exception {
	Key key = SecurityUtil.generateKeyFromString(keyStr, algorithm);
	Cipher enCipher = Cipher.getInstance(algorithm);
	enCipher.init(Cipher.ENCRYPT_MODE, key);
	byte[] result = enCipher.doFinal(input.getBytes());
	return new String(result, "UTF-8");

    }

    public static String decrypt(String keyStr, String input, String algorithm) throws Exception {

	Key key = SecurityUtil.generateKeyFromString(keyStr, algorithm);

	Cipher enCipher = Cipher.getInstance(algorithm);
	enCipher.init(Cipher.DECRYPT_MODE, key);
	byte[] result = enCipher.doFinal(input.getBytes());
	return new String(result, "UTF-8");

    }

}
