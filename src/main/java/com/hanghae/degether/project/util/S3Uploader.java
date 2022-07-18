package com.hanghae.degether.project.util;


import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.hanghae.degether.exception.CustomException;
import com.hanghae.degether.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;  // S3 버킷 이름
    @Value("${cloud.aws.s3.baseUrl}")
    public String baseUrl;  // S3 버킷 이름
    public Long maxUploadSize = 1024 * 1024 * 50L; // 50mb

    // 이미지 업로드
    public String upload(MultipartFile file, String dirName) {
        if (file.getSize() > maxUploadSize) {
            throw new CustomException(ErrorCode.OVER_UPLOAD_SIZE);
        }

        // 1. S3에 업로드할 파일 이름 생성
        String fileName = createFileName(file.getOriginalFilename(), dirName);
        // 2. S3에 저장할 객체 메타데이터 생성 및 값 set(사이즈, 컨텐트 타입)
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        // 3. MultipartFile에서 InputStream 가져온 후 S3에 업로드
        try (InputStream inputStream = file.getInputStream()) {
            s3UploadImg(inputStream, objectMetadata, fileName);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "파일 변환 중 에러가 발생하였습니다. (%s)", file.getOriginalFilename()
                    )
            );
        }
        // 4. S3에 업로드된 이미지 URL 가져오기
        return getFileUrl(fileName);
    }

    // S3에 업로드된 이미지 파일 URL 가져오기
    private String getFileUrl(String fileName) {
        // 버킷과 네임과 파일이름으로 업로드된 이미지 파일 URL 가져오기
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    // S3에 업로드될 이미지 파일이름 생성
    private String createFileName(String originalFilename, String dirName) {
        // 랜덤한 이미지파일 이름 생성
        return dirName + "/" + UUID.randomUUID() + originalFilename;
    }
    public String getOriginalFileName(String fileUrl, String dirName) {
        // 원본 파일 이름
        return getFileName(fileUrl).replace(dirName+"/","").substring(36);
    }
    public String getFileName(String fileUrl) {
        // 원본 파일 이름
        return fileUrl.replace(baseUrl, "");
    }

    // S3에 이미지 업로드
    private void s3UploadImg(InputStream inputStream, ObjectMetadata objectMetadata, String fileName) {
        // 파일 객체 생성 후 S3에 버킷 업로드
        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead)
        );
    }

    // 업로드된 S3 파일 삭제
    public void deleteFromS3(String source) {
        System.out.println(source);
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, source));
    }
}
