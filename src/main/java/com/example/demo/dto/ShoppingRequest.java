package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ShoppingRequest {
    @NotBlank(message = "sessionId는 필수입니다.")
    private String sessionId;

    @NotBlank(message = "userId는 필수입니다.")
    private String userId;

    @NotBlank(message = "query는 필수입니다.")
    private String query;

    @NotBlank(message = "languageCode는 필수입니다.")
    private String languageCode;
}
