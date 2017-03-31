package com.frolicdev.crypto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.kms.model.GenerateDataKeyRequest;
import com.amazonaws.services.kms.model.GenerateDataKeyResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

/**
 * 
 * @author sdhankar
 *
 */

public class S3CryptoClient {

    private static final Logger logger = Logger.getLogger(S3CryptoClient.class.getName());
    private static S3CryptoClient instance;
    private String masterKeyId = null;
    private static final String DATA_KEY_HEADER = "x-amz-data-key";
    private Map<String, DataKey> keyMap = new HashMap<>();
    private AWSKMSClient awsKmsClient = null;
    private DataKey currentKey = null;
    private AmazonS3 s3client = null;
    private TimeKeeper keeper = null;

    public static S3CryptoClient getInstance(String region, String masterKeyId) {
	if (instance == null) {
	    instance = new S3CryptoClient(region, masterKeyId);
	}
	return instance;
    }

    public static S3CryptoClient getInstance(String accessKey, String secretKey, String region, String masterKeyId) {
	if (instance == null) {
	    instance = new S3CryptoClient(accessKey, secretKey, region, masterKeyId);
	}
	return instance;
    }

    private S3CryptoClient(String accessKey, String secretKey, String region, String masterKeyId) {

	this.masterKeyId = masterKeyId;
	AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
	    @Override
	    public AWSCredentials getCredentials() {
		return new AWSCredentials() {
		    @Override
		    public String getAWSAccessKeyId() {
			return accessKey;
		    }

		    @Override
		    public String getAWSSecretKey() {
			return secretKey;
		    }
		};
	    }

	    @Override
	    public void refresh() {

	    }
	};

	awsKmsClient = (AWSKMSClient) AWSKMSClientBuilder.standard().withRegion(region)
		.withCredentials(awsCredentialsProvider).build();

	s3client = AmazonS3ClientBuilder.standard().withRegion(region).withCredentials(awsCredentialsProvider).build();
	this.keeper = new TimeKeeper(this);
	new Thread(keeper).start();

    }

    private S3CryptoClient(String region, String masterKeyId) {
	logger.info("building the crypto client with the region : " + region);
	this.masterKeyId = masterKeyId;
	awsKmsClient = (AWSKMSClient) AWSKMSClientBuilder.standard().withRegion(region).build();
	s3client = AmazonS3ClientBuilder.standard().withRegion(region).build();

	this.keeper = new TimeKeeper(this);
	new Thread(keeper).start();
    }

    protected void finalize() {
	this.keeper.stopIt = true;
    }

    public Map<String, DataKey> getKeyMap() {
	return keyMap;
    }

    public DataKey getCurrentKey() {
	return currentKey;
    }

    public void setCurrentKey(DataKey currentKey) {
	this.currentKey = currentKey;
    }

    public AmazonS3 getS3Client() {
	return s3client;
    }

    public PutObjectResult putEncByteArrayToBucket(String bucketName, String key, byte[] object,
	    ObjectMetadata metaData) throws Exception {

	refreshKey();

	byte[] encryptedBytes = encrypt(object);

	metaData.setContentLength(encryptedBytes.length);

	metaData.getUserMetadata().put(DATA_KEY_HEADER, currentKey.getEncryptedKey());
	return s3client
		.putObject(new PutObjectRequest(bucketName, key, new ByteArrayInputStream(encryptedBytes), metaData));

    }

    public PutObjectResult putEncByteArrayToBucket(String bucketName, String key, byte[] object) throws Exception {

	return putEncByteArrayToBucket(bucketName, key, object, new ObjectMetadata());
    }

    private byte[] encrypt(byte[] object) throws Exception {

	if (currentKey == null) {
	    refreshKey();
	}
	currentKey.setLastUsed(System.currentTimeMillis());
	return currentKey.getCrypto().encrypt(object);
    }

    private synchronized void refreshKey() throws Exception {

	logger.info("generating the data key using CMK : "+ this.masterKeyId);
	// TODO this has to be environment variable
	GenerateDataKeyResult dataKeyResult = awsKmsClient
		.generateDataKey(new GenerateDataKeyRequest().withKeyId(this.masterKeyId).withKeySpec("AES_128"));

	if (dataKeyResult == null) {
	    logger.warning("Could not find data key to encrypt");
	    throw new Exception("Could not find data key to encrypt");
	}
	String encKey = Base64.getEncoder().encodeToString(dataKeyResult.getCiphertextBlob().array());
	byte[] plainKey = dataKeyResult.getPlaintext().array();
	currentKey = new DataKey(encKey, plainKey);

    }

    public byte[] getEncByteArrayFromBucket(String bucketName, String key) throws Exception {

	S3Object object = s3client.getObject(bucketName, key);
	InputStream inStream = object.getObjectContent();
	byte[] output = IOUtils.toByteArray(inStream);
	String encryptedDataKey = object.getObjectMetadata().getUserMetadata().get(DATA_KEY_HEADER);

	byte[] decryptedData = decrypt(output, encryptedDataKey.trim());

	return decryptedData;
    }

    private byte[] decrypt(byte[] output, String encryptedDataKey) throws Exception {

	DataKey dataKey = keyMap.get(encryptedDataKey);
	if (dataKey == null) {
	    DecryptResult decryptResult = awsKmsClient.decrypt(new DecryptRequest()
		    .withCiphertextBlob(ByteBuffer.wrap(Base64.getDecoder().decode(encryptedDataKey))));
	    if (decryptResult == null) {
		logger.warning("Error getting the key decrypted for value: " + encryptedDataKey);
		throw new Exception("Error getting the key decrypted for value: " + encryptedDataKey);
	    }

	    dataKey = new DataKey(encryptedDataKey, decryptResult.getPlaintext().array());
	    keyMap.put(encryptedDataKey, dataKey);

	}

	dataKey.setLastUsed(System.currentTimeMillis());
	return dataKey.getCrypto().decrypt(output);

    }

}
