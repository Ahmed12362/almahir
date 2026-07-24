package com.almahir.iti.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadFile(MultipartFile file, String folderName);

    String uploadRawFile(byte[] fileBytes, String fileName, String folderName);
}