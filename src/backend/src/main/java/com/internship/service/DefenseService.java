package com.internship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.internship.entity.Defense;
import java.math.BigDecimal;

public interface DefenseService extends IService<Defense> {
    
    Defense submitByStudent(Integer selectionId, String defenseScore, Integer recordFileId);
    
    Defense submitByTeacher(Integer selectionId, String defenseScore, BigDecimal defenseScoreNum, Integer recordFileId);
    
    void reviewDefense(Integer defenseId, String status, String comment);
}
