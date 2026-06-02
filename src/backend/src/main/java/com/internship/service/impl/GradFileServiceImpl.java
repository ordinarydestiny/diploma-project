package com.internship.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.internship.entity.File;
import com.internship.mapper.FileMapper;
import com.internship.service.GradFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradFileServiceImpl extends ServiceImpl<FileMapper, File> implements GradFileService {

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Override
    public File upload(MultipartFile file, Integer uploaderId, String relationType, Integer relationId) {
        try {
            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + ext;
            Path filePath = Paths.get(uploadPath, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM")), fileName);
            
            Files.createDirectories(filePath.getParent());
            Files.copy(file.getInputStream(), filePath);

            File fileEntity = new File();
            fileEntity.setOriginalName(originalName);
            fileEntity.setStoragePath(filePath.toString());
            fileEntity.setFileSize(file.getSize());
            fileEntity.setMimeType(file.getContentType());
            fileEntity.setUploaderId(uploaderId);
            fileEntity.setUploadTime(LocalDateTime.now());
            fileEntity.setRelationType(relationType);
            fileEntity.setRelationId(relationId);
            fileEntity.setIsDeleted(false);
            
            save(fileEntity);
            log.info("文件上传成功：{}", originalName);
            return fileEntity;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public File getFileById(Integer fileId) {
        return getById(fileId);
    }

    @Override
    public void validatePdfPageCount(MultipartFile file, int maxPages) throws Exception {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            int pageCount = document.getNumberOfPages();
            
            if (pageCount > maxPages) {
                throw new RuntimeException("PDF页数(" + pageCount + ")超过限制(" + maxPages + "页)");
            }
            
            if (pageCount < 1) {
                throw new RuntimeException("PDF文件为空");
            }
        }
    }
}
