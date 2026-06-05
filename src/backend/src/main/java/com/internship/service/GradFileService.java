package com.internship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.internship.entity.File;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface GradFileService extends IService<File> {
    
    File upload(MultipartFile file, Integer uploaderId, String relationType, Integer relationId);
    
    File getFileById(Integer fileId);
    
    Resource loadFileAsResource(String storagePath) throws Exception;
    
    void validatePdfPageCount(MultipartFile file, int maxPages) throws Exception;
}
