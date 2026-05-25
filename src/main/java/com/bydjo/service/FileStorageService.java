package com.bydjo.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file, String subDirectory);
    void deleteFile(String fileUrl);
    String getBaseUrl();
}
