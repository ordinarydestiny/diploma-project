package com.internship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.internship.entity.Defense;
import java.math.BigDecimal;

public interface DefenseService extends IService<Defense> {
    
    Defense submitByStudent(Integer selectionId, String defenseScore, Integer recordFileId);
    
    Defense submitByStudentFull(Integer selectionId, String defenseScore, Integer recordFileId, 
                               String selfEvaluation, String pptFileId, String defenseDatetime);
    
    Defense submitByTeacher(Integer selectionId, String defenseScore, BigDecimal defenseScoreNum, Integer recordFileId);
    
    Defense submitByTeacherFull(Integer selectionId, String defenseScore, BigDecimal defenseScoreNum, 
                              Integer recordFileId, String defenseDatetime, String location, 
                              String committee, String comment);
    
    void reviewDefense(Integer defenseId, String status, String comment,
                       String defenseScore, BigDecimal defenseScoreNum);
}
