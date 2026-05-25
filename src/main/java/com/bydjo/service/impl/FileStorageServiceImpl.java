package com.bydjo.service.impl;

import com.bydjo.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        String originalFileName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");

        // Validate file
        if (originalFileName.contains("..")) {
            throw new RuntimeException("Invalid file name: " + originalFileName);
        }

        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }

        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetDir = Paths.get(uploadDir, subDirectory).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);

            Path targetLocation = targetDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored: {}/{}", subDirectory, fileName);
            return "/uploads/" + subDirectory + "/" + fileName;

        } catch (IOException ex) {
            log.error("Failed to store file: {}", originalFileName, ex);
            throw new RuntimeException("Failed to store file: " + originalFileName, ex);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
                Path filePath = Paths.get(uploadDir, fileUrl.substring("/uploads/".length())).toAbsolutePath();
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ex) {
            log.warn("Failed to delete file: {}", fileUrl);
        }
    }

    @Override
    public String getBaseUrl() {
        return "/uploads/";
    }
}
