package com.bezkoder.spring.login.controllers;



import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bezkoder.spring.login.models.User;
import com.bezkoder.spring.login.payload.request.LoginRequest;
import com.bezkoder.spring.login.payload.request.SignupRequest;
import com.bezkoder.spring.login.payload.response.UserInfoResponse;
import com.bezkoder.spring.login.payload.response.MessageResponse;
import com.bezkoder.spring.login.repository.UserRepository;
import com.bezkoder.spring.login.security.jwt.JwtUtils;
import com.bezkoder.spring.login.security.services.UserDetailsImpl;

import java.time.LocalDateTime;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  private static final int MAX_LOGIN_ATTEMPTS = 3;
  private static final int LOCKOUT_DURATION_MINUTES = 10;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    // Check if the user exists
    Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());
    if (!optionalUser.isPresent()) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid credentials!"));
    }

    User user = optionalUser.get();

    if (user.isAccountLocked()) {
      LocalDateTime lockoutEndTime = user.getLockoutEndTime();
      LocalDateTime currentTime = LocalDateTime.now();

      if (currentTime.isBefore(lockoutEndTime)) {
        return ResponseEntity.badRequest().body(new MessageResponse("Error: Account is locked!"));
      }

      // Unlock the account if the lockout period has passed
      user.setAccountLocked(false);
      user.setLockoutEndTime(null);
      user.setLoginAttempts(0);
      userRepository.save(user);
    }

    // Check if the password is correct
    if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
      // Incorrect password, increment login attempts
      user.setLoginAttempts(user.getLoginAttempts() + 1);

      if (user.getLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
        // Maximum attempts reached, lock the account
        user.setAccountLocked(true);
        user.setLockoutEndTime(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
        userRepository.save(user);

        return ResponseEntity.badRequest().body(new MessageResponse("Error: Account is locked!"));
      }

      userRepository.save(user);

      return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid credentials!"));
    }

    // Reset login attempts on successful login
    user.setLoginAttempts(0);
    userRepository.save(user);

    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .body(new UserInfoResponse(userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail()));
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(signUpRequest.getUsername(),
            signUpRequest.getEmail(),
            encoder.encode(signUpRequest.getPassword()));

    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  @PostMapping("/signout")
  public ResponseEntity<?> logoutUser() {
    ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(new MessageResponse("You've been signed out!"));
  }
}
