package com.internship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.internship.entity.File;
import org.springframework.web.multipart.MultipartFile;

public interface GradFileService extends IService<File> {
    
    File upload(MultipartFile file, Integer uploaderId, String relationType, Integer relationId);
    
    File getFileById(Integer fileId);
    
    void validatePdfPageCount(MultipartFile file, int maxPages) throws Exception;
}
