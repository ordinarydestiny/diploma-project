package com.internship.dto;

import lombok.Data;

@Data
public class SelectionSubmitDTO {
    private Integer batchId;
    private Integer topicId;  // NULL表示自主命题
    private String topicType; // library 或 self
    private String selfTopicName;
    private String selfTopicDesc;
}
