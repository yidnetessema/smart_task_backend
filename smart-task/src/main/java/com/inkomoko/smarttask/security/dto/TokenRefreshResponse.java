package com.inkomoko.smarttask.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenRefreshResponse {
    private String token;

    private String refreshToken;

    private String type = "Bearer";
}
