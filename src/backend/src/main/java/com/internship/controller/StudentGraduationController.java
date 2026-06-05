package com.internship.controller;

import com.internship.common.Result;
import com.internship.dto.MyGraduationDTO;
import com.internship.service.StudentGraduationService;
import com.internship.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学生个人毕设信息查询Controller
 * 提供学生视角的完整毕设数据查询接口
 */
@RestController
@RequestMapping("/api/v1/graduation/student")
@RequiredArgsConstructor
@Tag(name = "学生毕设信息")
public class StudentGraduationController {

    private final StudentGraduationService studentGraduationService;
    private final JwtUtil jwtUtil;

    @GetMapping("/my-info")
    @Operation(summary = "获取我的毕设完整信息", description = "一次性返回学生的批次、选题、任务书、中期检查、最终检查、答辩、成绩等全部信息")
    public Result<MyGraduationDTO> getMyGraduationInfo() {
        // 从JWT Token中获取当前用户ID
        Long userId = jwtUtil.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录或登录已过期");
        }

        MyGraduationDTO info = studentGraduationService.getMyGraduationInfo(userId.intValue());
        return Result.success(info);
    }
}
