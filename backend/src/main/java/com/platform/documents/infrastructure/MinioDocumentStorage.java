package com.platform.documents.infrastructure;

import com.platform.documents.application.DocumentStorage;
import io.minio.*;
import java.io.ByteArrayInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MinioDocumentStorage implements DocumentStorage {
    private final MinioClient client;
    private final String bucket;

    public MinioDocumentStorage(MinioClient client, @Value("${app.storage.bucket}") String bucket) {
        this.client = client;
        this.bucket = bucket;
    }

    @Override
    public void store(String key, byte[] content, String contentType) {
        try {
            ensureBucket();
            client.putObject(PutObjectArgs.builder().bucket(bucket).object(key)
                .stream(new ByteArrayInputStream(content), (long) content.length, -1L)
                .contentType(contentType).build());
        } catch (Exception exception) {
            throw new DocumentStorageException("Document could not be stored", exception);
        }
    }

    @Override
    public byte[] load(String key) {
        try (var stream = client.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build())) {
            return stream.readAllBytes();
        } catch (Exception exception) {
            throw new DocumentStorageException("Document could not be loaded", exception);
        }
    }

    @Override
    public void delete(String key) {
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception exception) {
            throw new DocumentStorageException("Document could not be deleted", exception);
        }
    }

    private void ensureBucket() throws Exception {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
