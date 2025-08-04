package com.maybank.maybank_assessment.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTransactionRequest {
    @NotBlank(message="Description is required")
    @Size(max = 255, message="Description too long")
    private String description;
    // getter/setter
}
