
package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class MessageRequest {
    @NotBlank(message = "sessionId는 필수입니다.")
    private String sessionId;

    @NotBlank(message = "userId는 필수입니다.")
    private String userId;

    @NotBlank(message = "message는 필수입니다.")
    private String message;

    @NotBlank(message = "languageCode는 필수입니다.")
    private String languageCode;
}
