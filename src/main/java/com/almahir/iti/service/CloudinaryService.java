package com.almahir.iti.service;

import com.almahir.iti.dto.response.CloudinaryRawFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudinaryService {
    String uploadFile(MultipartFile file, String folderName);

    String uploadRawFile(byte[] fileBytes, String fileName, String folderName);
    List<CloudinaryRawFile> getRawFiles(String folderName);
}