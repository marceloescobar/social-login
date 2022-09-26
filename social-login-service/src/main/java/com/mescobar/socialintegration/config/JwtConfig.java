package com.mescobar.socialintegration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Component
public class JwtConfig {

  @Value("${security.jwt.uri:}")
  private String Uri;

  @Value("${security.jwt.header:}")
  private String header;

  @Value("${security.jwt.prefix:}")
  private String prefix;

  @Value("${security.jwt.expiration:1234}")
  private int expiration;

  @Value("${security.jwt.secret:}")
  private String secret;
}
