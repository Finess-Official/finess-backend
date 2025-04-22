package ru.finess.finess.identity.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Repository;
import ru.finess.finess.identity.application.UserRepository;

@Repository
@RequiredArgsConstructor
public class RelationalUserRepository implements UserRepository {

  @Delegate(types = UserRepository.class)
  private final JpaUserRepository delegate;
}
