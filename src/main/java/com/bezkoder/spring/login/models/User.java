package com.bezkoder.spring.login.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "username")
  private String username;

  @Column(name = "email")
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "login_attempts")
  private int loginAttempts;

  @Column(name = "account_locked")
  private boolean accountLocked;

  @Column(name = "lockout_end_time")
  private LocalDateTime lockoutEndTime;

  // Constructors, getters, and setters

  public User() {
  }

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getLoginAttempts() {
    return loginAttempts;
  }

  public void setLoginAttempts(int loginAttempts) {
    this.loginAttempts = loginAttempts;
  }

  public boolean isAccountLocked() {
    return accountLocked;
  }

  public void setAccountLocked(boolean accountLocked) {
    this.accountLocked = accountLocked;
  }

  public LocalDateTime getLockoutEndTime() {
    return lockoutEndTime;
  }

  public void setLockoutEndTime(LocalDateTime lockoutEndTime) {
    this.lockoutEndTime = lockoutEndTime;
  }
}
