
package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageResponse {
    private String userId;
    private String response;
}
