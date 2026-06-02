package com.internship.dto;

import lombok.Data;

@Data
public class SelectionReviewDTO {
    private String status;  // approved 或 rejected
    private String comment;
}
