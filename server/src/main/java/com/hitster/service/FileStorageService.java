package com.hitster.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads/songs/";

    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || !originalName.toLowerCase().endsWith(".mp3")) {
            throw new IllegalArgumentException("Only MP3 files are allowed.");
        }

        Files.createDirectories(Paths.get(UPLOAD_DIR));

        String safeOriginalName = Paths.get(originalName).getFileName().toString();
        String safeFileName = UUID.randomUUID() + "_" + safeOriginalName;
        Path targetPath = Paths.get(UPLOAD_DIR, safeFileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return "/audio/" + safeFileName;
    }

    public boolean deleteByAudioUrl(String audioUrl) {
        if (audioUrl == null || audioUrl.isBlank()) {
            return false;
        }

        String prefix = "/audio/";
        if (!audioUrl.startsWith(prefix)) {
            return false;
        }

        String fileName = audioUrl.substring(prefix.length()).trim();
        if (fileName.isEmpty()) {
            return false;
        }

        Path targetPath = Paths.get(UPLOAD_DIR, fileName);

        try {
            return Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}