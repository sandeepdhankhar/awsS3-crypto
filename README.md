# awsS3-crypto
Sdk to store encrypted objects to S3


The work stems from the following usage of AWS KMS to encrypt objects

http://docs.aws.amazon.com/kms/latest/developerguide/workflow.html


This librrary is a thin wrapper over AWS S3 Java client to allow us to utilize the
AWS KMS for data encryption. This wrapper is useful in case the write and read operations to S3
exceed the throughput supported by KMS API. As of now the KMS API throughput is very limited.
The wrapper circumvents the limitation by storing the data key in the memory for a little longer duration
which is configurable.

The time for which the data key is kept in memory can be configured by setting:

TimeKeeper.maxIdleTimeInMillis
(The default value is 5 minutes)

Usage :

//The static method takes in the region and the alias for customer master key as input
S3CryptoClient s3Client = S3CryptoClient.getInstance("us-west-2","CMK");

//To add an object to S3 bucket
PutObjectResult putObjectResult = s3Client.putEncByteArrayToBucket(String bucketName, String key, byte[] object,ObjectMetadata metaData) ;

//To retrieve a object using the key
byte[] byteArray = s3Client.getEncByteArrayFromBucket(String bucketName, String key)





