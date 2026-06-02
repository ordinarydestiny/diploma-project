package com.internship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.internship.entity.FinalCheck;

public interface FinalCheckService extends IService<FinalCheck> {
    
    void submitFinalCheck(Integer selectionId, Integer fileId);
    
    void reviewFinalCheck(Integer checkId, String status, String comment, Integer score);
    
    FinalCheck getFinalVersion(Integer selectionId);
}
