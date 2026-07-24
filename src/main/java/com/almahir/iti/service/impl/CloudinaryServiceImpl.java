package com.almahir.iti.service.impl;

import com.almahir.iti.exception.ImageUploadException;
import com.almahir.iti.exception.ResourceNotFound;
import com.almahir.iti.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file, String folderName) {
        if (file == null || file.isEmpty()) {
            throw new ImageUploadException("File cannot be empty");
        }

        try {
            var uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folderName,
                            "resource_type", "auto"
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary", e);
            throw new ImageUploadException("Image upload failed. Please try again.");
        }
    }

    @Override
    public String uploadRawFile(byte[] fileBytes, String fileName, String folderName) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new ImageUploadException("File bytes cannot be empty");
        }

        try {
            var uploadResult = cloudinary.uploader().upload(
                    fileBytes,
                    ObjectUtils.asMap(
                            "folder", folderName,
                            "public_id", fileName,
                            "resource_type", "raw",
                            "overwrite", true
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            log.error("Failed to upload raw JSON file to Cloudinary", e);
            throw new ImageUploadException("Failed to upload tafsir JSON file. Please try again.");
        }
    }
}