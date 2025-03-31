package ru.finess.finess.identity.application;

import lombok.NonNull;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserId;

public interface UserRepository {

  boolean exists(@NonNull UserId userId);

  void save(@NonNull User user);
}
