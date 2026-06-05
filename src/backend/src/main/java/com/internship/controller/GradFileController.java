package com.internship.controller;

import com.internship.common.Result;
import com.internship.entity.File;
import com.internship.service.GradFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
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

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileId}")
    @Operation(summary = "下载文件")
    public ResponseEntity<Resource> downloadFile(@PathVariable Integer fileId) {
        try {
            // 查询文件信息
            File fileInfo = fileService.getFileById(fileId);
            if (fileInfo == null) {
                return ResponseEntity.notFound().build();
            }

            // 加载文件资源
            Resource resource = fileService.loadFileAsResource(fileInfo.getStoragePath());

            // 设置响应头
            String encodedFileName = URLEncoder.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                    .body(resource);
        } catch (Exception e) {
            log.error("下载文件失败: fileId={}", fileId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 在线预览文件（主要用于PDF）
     */
    @GetMapping("/preview/{fileId}")
    @Operation(summary = "在线预览文件")
    public ResponseEntity<Resource> previewFile(@PathVariable Integer fileId) {
        try {
            // 查询文件信息
            File fileInfo = fileService.getFileById(fileId);
            if (fileInfo == null) {
                return ResponseEntity.notFound().build();
            }

            // 加载文件资源
            Resource resource = fileService.loadFileAsResource(fileInfo.getStoragePath());

            // 根据MIME类型设置Content-Type
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (fileInfo.getMimeType() != null && !fileInfo.getMimeType().isEmpty()) {
                try {
                    mediaType = MediaType.parseMediaType(fileInfo.getMimeType());
                } catch (Exception e) {
                    log.warn("无法解析MIME类型: {}", fileInfo.getMimeType());
                }
            }

            // 设置为内联显示（inline）而不是下载（attachment）
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileInfo.getOriginalName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("预览文件失败: fileId={}", fileId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{fileId}")
    @Operation(summary = "获取文件信息")
    public Result<File> getFileInfo(@PathVariable Integer fileId) {
        File fileInfo = fileService.getFileById(fileId);
        if (fileInfo == null) {
            return Result.fail(404, "文件不存在");
        }
        return Result.success(fileInfo);
    }
}
