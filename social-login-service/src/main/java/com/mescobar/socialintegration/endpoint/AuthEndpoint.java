package com.mescobar.socialintegration.endpoint;

import java.net.URI;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.mescobar.socialintegration.exception.BadRequestException;
import com.mescobar.socialintegration.exception.EmailAlreadyExistsException;
import com.mescobar.socialintegration.exception.UsernameAlreadyExistsException;
import com.mescobar.socialintegration.model.Profile;
import com.mescobar.socialintegration.model.Role;
import com.mescobar.socialintegration.model.User;
import com.mescobar.socialintegration.payload.ApiResponse;
import com.mescobar.socialintegration.payload.FacebookLoginRequest;
import com.mescobar.socialintegration.payload.JwtAuthenticationResponse;
import com.mescobar.socialintegration.payload.LoginRequest;
import com.mescobar.socialintegration.payload.SignUpRequest;
import com.mescobar.socialintegration.service.FacebookService;
import com.mescobar.socialintegration.service.JwtTokenProvider;
import com.mescobar.socialintegration.service.UserService;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class AuthEndpoint {

  @Autowired
  private UserService userService;
  @Autowired
  private FacebookService facebookService;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Autowired
  private AuthenticationManager authenticationManager;


  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    String token = this.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
    return ResponseEntity.ok(new JwtAuthenticationResponse(token));
  }


  @PostMapping("/facebook/signin")
  public ResponseEntity<?> facebookAuth(
      @Valid @RequestBody FacebookLoginRequest facebookLoginRequest) {
    log.info("facebook login {}", facebookLoginRequest);
    String token = facebookService.loginUser(facebookLoginRequest.getAccessToken());
    return ResponseEntity.ok(new JwtAuthenticationResponse(token));
  }

  @PostMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> createUser(@Valid @RequestBody SignUpRequest payload) {
    log.info("creating user {}", payload.getUsername());

    User user = User.builder().username(payload.getUsername()).email(payload.getEmail())
        .password(payload.getPassword())
        .userProfile(Profile.builder().displayName(payload.getName()).build()).build();

    try {

      userService.registerUser(user, Role.USER);

    } catch (UsernameAlreadyExistsException | EmailAlreadyExistsException e) {
      throw new BadRequestException(e.getMessage());
    }

    URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/users/{username}")
        .buildAndExpand(user.getUsername()).toUri();

    return ResponseEntity.created(location)
        .body(new ApiResponse(true, "User registered successfully"));
  }


  /**
   * @param username
   * @param password
   * @return
   */
  private String loginUser(String username, String password) {
    Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(username, password));

    return tokenProvider.generateToken(authentication);
  }

}
