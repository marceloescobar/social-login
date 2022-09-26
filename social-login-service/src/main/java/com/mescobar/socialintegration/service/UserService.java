package com.mescobar.socialintegration.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.mescobar.socialintegration.exception.EmailAlreadyExistsException;
import com.mescobar.socialintegration.exception.UsernameAlreadyExistsException;
import com.mescobar.socialintegration.model.AppUserDetails;
import com.mescobar.socialintegration.model.Role;
import com.mescobar.socialintegration.model.User;
import com.mescobar.socialintegration.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByUsername(username).map(AppUserDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
  }


  public User registerUser(User user, Role role) {
    log.info("registering user {}", user.getUsername());

    if (userRepository.existsByUsername(user.getUsername())) {
      log.warn("username {} already exists.", user.getUsername());

      throw new UsernameAlreadyExistsException(
          String.format("username %s already exists", user.getUsername()));
    }

    if (userRepository.existsByEmail(user.getEmail())) {
      log.warn("email {} already exists.", user.getEmail());

      throw new EmailAlreadyExistsException(
          String.format("email %s already exists", user.getEmail()));
    }
    user.setActive(true);
    user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
    user.setRoles(new HashSet<>() {
      {
        add(role);
      }
    });

    return userRepository.save(user);
  }

  public List<User> findAll() {
    log.info("retrieving all users");
    return userRepository.findAll();
  }

  public Optional<User> findByUsername(String username) {
    log.info("retrieving user {}", username);
    return userRepository.findByUsername(username);
  }

  public Optional<User> findById(String id) {
    log.info("retrieving user {}", id);
    return userRepository.findById(id);
  }
}
