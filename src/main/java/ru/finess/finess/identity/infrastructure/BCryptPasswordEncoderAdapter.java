package ru.finess.finess.identity.infrastructure;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import ru.finess.finess.identity.application.PasswordEncoder;
import ru.finess.finess.identity.domain.UserEncodedPassword;
import ru.finess.finess.identity.domain.UserPassword;

@Component
@RequiredArgsConstructor
public class BCryptPasswordEncoderAdapter implements PasswordEncoder {

  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Override
  public UserEncodedPassword encode(@NonNull UserPassword password) {
    String encode = passwordEncoder.encode(password.value());
    return new UserEncodedPassword(encode);
  }
}
