package ru.finess.finess.identity.infrastructure;

import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.finess.finess.identity.application.UserRepository;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserId;

@Repository
@RequiredArgsConstructor
public class RelationalUserRepository implements UserRepository {

  private final JpaUserRepository delegate;

  @Override
  public Optional<User> find(@NonNull UserId userId) {
    return delegate.findById(userId);
  }

  @Override
  public boolean exists(@NonNull UserId userId) {
    return delegate.existsById(userId);
  }

  @Override
  public void save(@NonNull User user) {
    delegate.save(user);
  }
}
