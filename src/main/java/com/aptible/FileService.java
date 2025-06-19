package com.aptible;

import com.aptible.config.AppProperties;
import com.aptible.database.FileEntity;
import com.aptible.database.FileRepository;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    final FileRepository fileRepository;
    final AppProperties appProperties;
    final S3AsyncClient s3AsyncClient;
    final S3Client s3Client;

    public static RateLimiterConfig config = RateLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(30))
            .limitRefreshPeriod(Duration.ofMinutes(60))
            .limitForPeriod(100)
            .build();

    RateLimiterRegistry registry = RateLimiterRegistry.of(config);

    public Mono<FileEntity> createFile(FilePart file, final FileEntity fileEntity) throws Exception {
        fileEntity.setOriginalFileName(file.filename());
        if (appProperties.getUseS3()) {
            return registry.rateLimiter("s3ratelimit").executeCallable(() -> file.content()
                    .reduce(0L, (sum, buf) -> sum + buf.readableByteCount()).flatMap(size -> {
                        log.info("s3 upload attempt");
                        String fileName = fileEntity.getS3Prefix() + "/" + UUID.randomUUID() + "-" + file.filename();

                        CompletableFuture<PutObjectResponse> response = null;

                        AsyncRequestBody body = AsyncRequestBody.fromPublisher(
                                file.content().map(DataBuffer::asByteBuffer)
                        );
                        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                .key(fileName)
                                .bucket(fileEntity.getFileBucket())
                                .contentType(file.headers().getContentType().toString())
                                .contentLength(size)
                                .build();
                        fileEntity.setS3Path(fileName);
                        return Mono.fromFuture(s3AsyncClient.putObject(putObjectRequest, body))
                                .map(s3Resp -> {
                                    log.info("s3 upload completed {}", s3Resp.eTag());
                                    //TODO use reactor throughout
                                    fileEntity.setUploadSuccess(true);
                                    return fileRepository.save(fileEntity);
                                });
                    }));
        } else {
            //TODO make config option
            String filePath = "target/" + UUID.randomUUID() + "-" + file.filename();
            Path destination = Paths.get(filePath);

            AsynchronousFileChannel channel;
            try {
                channel = AsynchronousFileChannel.open(destination, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            } catch (IOException e) {
                return Mono.error(e);
            }

            DataBufferUtils.write(file.content(), channel, 0)
                    .doOnComplete(() -> {
                        try {
                            channel.close();
                        } catch (IOException ignored) {
                        }
                    })
                    .subscribe();
            fileEntity.setLocalFilePath(destination.toAbsolutePath().toString());
            fileEntity.setUploadSuccess(true);
            return Mono.just(fileRepository.save(fileEntity));
        }
    }

    //TODO check for delete markers
    //TODO soft delete
    public void deleteFile(FileEntity fileEntity) {
        if (appProperties.getUseS3()) {
            String fileName = fileEntity.getS3Path();
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .key(fileName)
                    .bucket(fileEntity.getFileBucket())
                    .build();

            DeleteObjectResponse response = null;
            response = s3Client.deleteObject(deleteObjectRequest);
            log.info("deleted file with marker {}", response.deleteMarker());
        } else {
            String fileName = fileEntity.getLocalFilePath();
            File localFile = new File(fileName);
            localFile.delete();
        }
        fileRepository.deleteById(fileEntity.getId());
    }


    public byte[] getFileBytes(Integer fileId) throws IOException {
        FileEntity fileEntity = fileRepository.findById(fileId).get();
        //TODO also check file in s3
        if (appProperties.getUseS3()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .key(fileEntity.getS3Path())
                    .bucket(fileEntity.getFileBucket())
                    .build();

            ResponseInputStream<GetObjectResponse> response = null;
            response = s3Client.getObject(getObjectRequest);
            try {
                return response.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new FileInputStream(fileEntity.getLocalFilePath()).readAllBytes();
        }
    }
}
