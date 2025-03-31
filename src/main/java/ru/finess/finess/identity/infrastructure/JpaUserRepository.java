package ru.finess.finess.identity.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserId;

public interface JpaUserRepository extends JpaRepository<User, UserId> {}
