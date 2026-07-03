package com.workerconnect.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;
    

    public String storeProfileImage(MultipartFile file) throws IOException {
        return storeFile(file, uploadDir + "/profiles", "profiles");
    }

    public String storeDocument(MultipartFile file) throws IOException {
        return storeFile(file, uploadDir + "/documents", "documents");
    }

    private String storeFile(MultipartFile file, String dirPath, String subPath) throws IOException {
        Path dir = Paths.get(dirPath);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf(".")) : "";
        String fileName = UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + subPath + "/" + fileName;
    }

    public String storeInvoice(byte[] content, String fileName) throws IOException {
        Path dir = Paths.get(uploadDir + "/invoices");
        if (!Files.exists(dir)) Files.createDirectories(dir);
        Files.write(dir.resolve(fileName), content);
        return "/uploads/invoices/" + fileName;
    }
}
