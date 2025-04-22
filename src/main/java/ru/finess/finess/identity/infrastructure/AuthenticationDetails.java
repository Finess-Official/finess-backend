package ru.finess.finess.identity.infrastructure;

import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthoritiesContainer;
import ru.finess.finess.identity.domain.Session;
import ru.finess.finess.identity.domain.UserId;

public record AuthenticationDetails(UserId user) implements GrantedAuthoritiesContainer {

  public static AuthenticationDetails authenticated(@NonNull Session session) {
    return new AuthenticationDetails(session.user());
  }

  @Override
  public Collection<? extends GrantedAuthority> getGrantedAuthorities() {
    return List.of();
  }
}
