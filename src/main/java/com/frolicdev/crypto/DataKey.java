package com.frolicdev.crypto;

/**
 * 
 * @author sdhankar
 *
 */

public class DataKey {

    public DataKey(String encKey, byte[] key) throws Exception {
	this.encryptedKey = encKey;

	this.crypto = new SymmetricCrypto(key, SymmetricCrypto.AES_ALGORITHM, SymmetricCrypto.CIPHER_ALGORITHM);
	this.lastUsed = System.currentTimeMillis();
    }

    public String getEncryptedKey() {
	return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
	this.encryptedKey = encryptedKey;
    }

    public long getLastUsed() {
	return lastUsed;
    }

    public void setLastUsed(long lastUsed) {
	this.lastUsed = lastUsed;
    }

    private String encryptedKey;
    private SymmetricCrypto crypto;

    public SymmetricCrypto getCrypto() {
	return crypto;
    }

    public void setCrypto(SymmetricCrypto crypto) {
	this.crypto = crypto;
    }

    private long lastUsed;

}
