package com.mescobar.socialintegration.service;

import java.util.Optional;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import com.mescobar.socialintegration.client.FacebookClient;
import com.mescobar.socialintegration.exception.InternalServerException;
import com.mescobar.socialintegration.model.AppUserDetails;
import com.mescobar.socialintegration.model.Profile;
import com.mescobar.socialintegration.model.Role;
import com.mescobar.socialintegration.model.User;
import com.mescobar.socialintegration.model.facebook.FacebookUser;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FacebookService {

  @Autowired
  private FacebookClient facebookClient;
  @Autowired
  private UserService userService;
  @Autowired
  private JwtTokenProvider tokenProvider;

  public String loginUser(String fbAccessToken) {
    var facebookUser = facebookClient.getUser(fbAccessToken);

    return userService.findById(facebookUser.getId())
        .or(() -> Optional
            .ofNullable(userService.registerUser(convertTo(facebookUser), Role.FACEBOOK_USER)))
        .map(AppUserDetails::new)
        .map(userDetails -> new UsernamePasswordAuthenticationToken(userDetails, null,
            userDetails.getAuthorities()))
        .map(tokenProvider::generateToken).orElseThrow(() -> new InternalServerException(
            "unable to login facebook user id " + facebookUser.getId()));
  }

  private User convertTo(FacebookUser facebookUser) {
    return User.builder().id(facebookUser.getId()).email(facebookUser.getEmail())
        .username(generateUsername(facebookUser.getFirstName(), facebookUser.getLastName()))
        .password(generatePassword(8))
        .userProfile(Profile.builder()
            .displayName(
                String.format("%s %s", facebookUser.getFirstName(), facebookUser.getLastName()))
            .profilePictureUrl(facebookUser.getPicture().getData().getUrl()).build())
        .build();
  }

  private String generateUsername(String firstName, String lastName) {
    Random rnd = new Random();
    int number = rnd.nextInt(999999);

    return String.format("%s.%s.%06d", firstName, lastName, number);
  }

  private String generatePassword(int length) {
    String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
    String specialCharacters = "!@#$";
    String numbers = "1234567890";
    String combinedChars = capitalCaseLetters + lowerCaseLetters + specialCharacters + numbers;
    Random random = new Random();
    char[] password = new char[length];

    password[0] = lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length()));
    password[1] = capitalCaseLetters.charAt(random.nextInt(capitalCaseLetters.length()));
    password[2] = specialCharacters.charAt(random.nextInt(specialCharacters.length()));
    password[3] = numbers.charAt(random.nextInt(numbers.length()));

    for (int i = 4; i < length; i++) {
      password[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
    }
    return new String(password);
  }

}
