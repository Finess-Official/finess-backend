package ru.finess.finess.identity.application;

import java.util.Optional;
import lombok.NonNull;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserId;

public interface UserRepository {

  Optional<User> findById(@NonNull UserId userId);

  boolean existsById(@NonNull UserId userId);

  void save(@NonNull User user);
}
