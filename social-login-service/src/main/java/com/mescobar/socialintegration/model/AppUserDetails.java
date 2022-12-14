package com.mescobar.socialintegration.model;

import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AppUserDetails extends User implements UserDetails {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public AppUserDetails(final User user) {
    super(user);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {

    return getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
        .collect(Collectors.toSet());
  }

  @Override
  public boolean isAccountNonExpired() {
    return isActive();
  }

  @Override
  public boolean isAccountNonLocked() {
    return isActive();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return isActive();
  }

  @Override
  public boolean isEnabled() {
    return isActive();
  }

}
