package com.lovemaster.service.impl;

import com.lovemaster.config.UploadProperties;
import com.lovemaster.dto.response.ImageUploadResponse;
import com.lovemaster.exception.BusinessException;
import com.lovemaster.exception.ErrorCode;
import com.lovemaster.service.FileStorageService;
import com.lovemaster.service.ImageFileGuard;
import com.lovemaster.service.ImageObjectNameGenerator;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "lovemaster.upload", name = "storage-type", havingValue = "cos")
public class CosFileStorageServiceImpl implements FileStorageService {

    private final UploadProperties uploadProperties;
    private final ImageFileGuard imageFileGuard;
    private final ImageObjectNameGenerator imageObjectNameGenerator;

    @Override
    public ImageUploadResponse storeImage(MultipartFile file) {
        String filename = imageObjectNameGenerator.generate(imageFileGuard.validateAndGetExtension(file));
        String objectKey = buildObjectKey(uploadProperties.getCos().getKeyPrefix(), filename);

        COSClient cosClient = createClient();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            cosClient.putObject(new PutObjectRequest(
                    uploadProperties.getCos().getBucket(),
                    objectKey,
                    file.getInputStream(),
                    metadata
            ));
        } catch (IOException | RuntimeException e) {
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        } finally {
            cosClient.shutdown();
        }

        return new ImageUploadResponse(buildPublicUrl(objectKey), objectKey, file.getContentType(), file.getSize());
    }

    private COSClient createClient() {
        UploadProperties.Cos cos = uploadProperties.getCos();
        if (!StringUtils.hasText(cos.getSecretId())
                || !StringUtils.hasText(cos.getSecretKey())
                || !StringUtils.hasText(cos.getRegion())
                || !StringUtils.hasText(cos.getBucket())) {
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }

        COSCredentials credentials = new BasicCOSCredentials(cos.getSecretId(), cos.getSecretKey());
        return new COSClient(credentials, new ClientConfig(new Region(cos.getRegion())));
    }

    private String buildObjectKey(String keyPrefix, String filename) {
        if (!StringUtils.hasText(keyPrefix)) {
            return filename;
        }
        return keyPrefix.replaceAll("^/+", "").replaceAll("/+$", "") + "/" + filename;
    }

    private String buildPublicUrl(String objectKey) {
        String publicBaseUrl = uploadProperties.getCos().getPublicBaseUrl();
        if (StringUtils.hasText(publicBaseUrl)) {
            return publicBaseUrl.replaceAll("/+$", "") + "/" + objectKey;
        }

        UploadProperties.Cos cos = uploadProperties.getCos();
        return "https://" + cos.getBucket() + ".cos." + cos.getRegion() + ".myqcloud.com/" + objectKey;
    }
}
