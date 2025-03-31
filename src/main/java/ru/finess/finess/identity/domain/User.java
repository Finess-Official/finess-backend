package ru.finess.finess.identity.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;

@Entity
@Getter
@Table(name = "users")
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

  @Builder
  private static User create(@NonNull UserEncodedPassword encodedPassword) {
    return new User(UserId.random(), encodedPassword);
  }

  @EmbeddedId private UserId id;

  private UserEncodedPassword hashedPassword;
}
