package com.internship.controller;

import com.internship.common.Result;
import com.internship.entity.File;
import com.internship.service.GradFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件管理")
public class GradFileController {

    private final GradFileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "通用文件上传")
    public Result<File> upload(@RequestParam("file") MultipartFile file,
                               @RequestParam Integer uploaderId,
                               @RequestParam String relationType,
                               @RequestParam(required = false) Integer relationId) throws Exception {
        return Result.success(fileService.upload(file, uploaderId, relationType, relationId));
    }
}
