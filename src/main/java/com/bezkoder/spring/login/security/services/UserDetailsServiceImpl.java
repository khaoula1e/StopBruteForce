package com.bezkoder.spring.login.security.services;

import com.bezkoder.spring.login.models.User;
import com.bezkoder.spring.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  UserRepository userRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<User> userOptional = userRepository.findByUsername(username);
    if (!userOptional.isPresent()) {
      throw new UsernameNotFoundException("User not found with username: " + username);
    }

    User user = userOptional.get();

    return UserDetailsImpl.build(user);
  }
}
