package com.internship.service;

public interface ScoreComputationService {
    
    void recompute(Integer studentId, Integer batchId);
    
    void recomputeBySelectionId(Integer selectionId);
    
    void batchRecompute(Integer batchId);
    
    void publishScore(Integer selectionId);
    
    Double convertLevelToNum(String level);
    
    String determineGradeLevel(Double score);
}
