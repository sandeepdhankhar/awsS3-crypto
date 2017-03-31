package com.frolicdev.crypto;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * 
 * @author sdhankar
 *
 */
public class TimeKeeper implements Runnable {

    private S3CryptoClient s3CryptoClient;
    public static long maxIdleTimeInMillis = 300000;
    private static Logger logger = Logger.getLogger(TimeKeeper.class.getName());

    public TimeKeeper(S3CryptoClient client) {
	s3CryptoClient = client;
    }

    public boolean stopIt = false;

    public void run() {

	while (!stopIt) {
	    try {

		// check the main key
		if (s3CryptoClient.getCurrentKey() != null) {

		    if (checkIfInvalid(s3CryptoClient.getCurrentKey())) {
			s3CryptoClient.setCurrentKey(null);
		    }

		}

		Iterator<Map.Entry<String, DataKey>> iter = this.s3CryptoClient.getKeyMap().entrySet().iterator();
		while (iter.hasNext()) {
		    Entry<String, DataKey> dataKey = iter.next();
		    if (checkIfInvalid(dataKey.getValue())) {
			iter.remove();
		    }
		}

		Thread.sleep(maxIdleTimeInMillis);

	    } catch (Throwable t) {
		
		logger.warning(t.getMessage());
	    }

	}

    }

    private boolean checkIfInvalid(DataKey currentKey) {
	if ((System.currentTimeMillis() - currentKey.getLastUsed()) > maxIdleTimeInMillis) {
	    return true;
	}
	return false;
    }
}
