package com.frolicdev.crypto;

import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author sdhankar
 *
 *         NOTE:: These tests have dependencies with actual CMK and s3 bucket.
 *         When required enable the tests and update the values
 */
public class S3CryptoClientTest {

    @Ignore
    public void testCryptoBucket() {

	TimeKeeper.maxIdleTimeInMillis = 600000;
	S3CryptoClient client = S3CryptoClient.getInstance("us-west-2", "s3CMK");

	try {
	    client.putEncByteArrayToBucket("mybucket", "s3cryptoTest",
		    "this is sample test data".getBytes());
	} catch (Exception e) {
	    fail(e.getMessage());
	}
    }

    @Ignore
    public void testDecrypto() {
	TimeKeeper.maxIdleTimeInMillis = 600000;
	S3CryptoClient client = S3CryptoClient.getInstance("us-west-2", "s3CMK");

	try {
	    byte[] result = client.getEncByteArrayFromBucket("mybucket", "s3cryptoTest");
	    System.out.println(new String(result));
	} catch (Exception e) {
	    fail(e.getMessage());
	}
    }

    @Ignore
    public void testKeyExpiration() {

	TimeKeeper.maxIdleTimeInMillis = 2000;

	S3CryptoClient client = S3CryptoClient.getInstance("us-west-2", "s3CMK");

	try {
	    client.putEncByteArrayToBucket("mybucket", "s3cryptoTest",
		    "this is sample test data".getBytes());

	    assert (client.getCurrentKey() != null);
	    Thread.sleep(1000);
	    assert (client.getCurrentKey() != null);
	    Thread.sleep(2000);
	    assert (client.getCurrentKey() == null);

	} catch (Exception e) {
	    fail(e.getMessage());
	}

    }

}
