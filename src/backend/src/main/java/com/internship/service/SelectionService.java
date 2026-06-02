package com.internship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.internship.dto.SelectionSubmitDTO;
import com.internship.dto.SelectionReviewDTO;
import com.internship.entity.StudentSelection;

public interface SelectionService extends IService<StudentSelection> {
    
    StudentSelection submitSelection(SelectionSubmitDTO dto);
    
    IPage<StudentSelection> listSelections(Integer page, Integer size, Integer batchId, String status);
    
    StudentSelection getCurrentSelection(Integer studentId, Integer batchId);
    
    void reviewSelection(Integer selectionId, SelectionReviewDTO dto);
    
    void resetSelection(Integer selectionId, String reason);
}
