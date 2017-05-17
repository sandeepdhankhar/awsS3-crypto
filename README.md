# awsS3-crypto
Sdk to store encrypted objects to S3


The work stems from the following usage of AWS KMS to encrypt objects

http://docs.aws.amazon.com/kms/latest/developerguide/workflow.html


This librrary is a thin wrapper over AWS S3 Java client to allow us to utilize the
AWS KMS for data encryption. This wrapper is useful in case the write and read operations to S3
exceed the throughput supported by KMS API. As of now the KMS API throughput is very limited.
The wrapper circumvents the limitation by storing the data key in the memory for a little longer duration
which is configurable.


