package com.mescobar.socialintegration.payload;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;

@Data
@RequiredArgsConstructor
public class JwtAuthenticationResponse {
  
  @NonNull
  private String accessToken;
  private String tokenType = "Bearer";
}
