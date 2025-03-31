package ru.finess.finess.identity.application;

import lombok.NonNull;
import ru.finess.finess.identity.domain.UserEncodedPassword;
import ru.finess.finess.identity.domain.UserPassword;

public interface PasswordEncoder {

  UserEncodedPassword encode(@NonNull UserPassword password);
}
