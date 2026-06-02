package com.internship.config;

import com.alibaba.fastjson2.JSON;
import com.internship.annotation.LogOperation;
import com.internship.entity.OperationLog;
import com.internship.mapper.OperationLogMapper;
import com.internship.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final JwtUtil jwtUtil;

    @Around("@annotation(logOperation)")
    public Object around(ProceedingJoinPoint point, LogOperation logOperation) throws Throwable {
        Object[] args = point.getArgs();
        
        Object result = null;
        String resultStatus = "success";
        String errorMessage = null;
        
        try {
            result = point.proceed();
            return result;
        } catch (Exception e) {
            resultStatus = "failure";
            errorMessage = e.getMessage();
            log.error("操作执行失败: {}", logOperation.value(), e);
            throw e;
        } finally {
            try {
                saveOperationLog(point, logOperation, args, result, resultStatus, errorMessage);
            } catch (Exception ex) {
                log.error("保存操作日志失败", ex);
            }
        }
    }

    private void saveOperationLog(ProceedingJoinPoint point, LogOperation logOperation, 
                                   Object[] args, Object result, String resultStatus, String errorMessage) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null);
            operationLog.setUserRole(jwtUtil.getCurrentRole());
            operationLog.setOperation(logOperation.value());
            
            HttpServletRequest request = ((ServletRequestAttributes) 
                RequestContextHolder.getRequestAttributes()).getRequest();
            operationLog.setIpAddress(getClientIp(request));
            operationLog.setRequestUrl(request.getRequestURI());
            operationLog.setRequestMethod(request.getMethod());
            operationLog.setUserAgent(request.getHeader("User-Agent"));
            operationLog.setResult(resultStatus);
            operationLog.setErrorMessage(errorMessage);
            
            Map<String, Object> detail = new HashMap<>();
            detail.put("method", signature.getName());
            detail.put("args", args);
            if (result != null) {
                detail.put("result", result);
            }
            operationLog.setDetail(JSON.toJSONString(detail));
            
            operationLog.setCreatedAt(java.time.LocalDateTime.now());
            
            operationLogMapper.insert(operationLog);
            
            log.info("操作日志已记录: {} - {}", logOperation.value(), request.getRequestURI());
        } catch (Exception e) {
            log.error("构建操作日志对象失败", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
